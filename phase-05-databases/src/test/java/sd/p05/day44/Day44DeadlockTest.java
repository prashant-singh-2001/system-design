package sd.p05.day44;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class Day44DeadlockTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    private static Connection setupConnection;
    private final DeadlockLab lab = new DeadlockLab();

    @BeforeAll
    static void createSchema() throws Exception {
        setupConnection = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
        try (Statement statement = setupConnection.createStatement()) {
            statement.execute("CREATE TABLE accounts (id INT PRIMARY KEY, balance INT NOT NULL)");
        }
    }

    @BeforeEach
    void resetAccounts() throws Exception {
        try (Statement statement = setupConnection.createStatement()) {
            statement.execute("TRUNCATE accounts");
            statement.execute("INSERT INTO accounts (id, balance) VALUES (1, 100), (2, 100)");
        }
    }

    private Connection newConnection() throws Exception {
        Connection conn = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
        conn.setAutoCommit(false);
        return conn;
    }

    private Runnable awaitBarrier(CyclicBarrier barrier) {
        return () -> {
            try {
                barrier.await(10, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Test
    @DisplayName("opposite-direction transfers with unordered locking genuinely deadlock")
    void deadlockActuallyHappens() throws Exception {
        CyclicBarrier barrier = new CyclicBarrier(2);
        Connection connA = newConnection();
        Connection connB = newConnection();
        // Throwable, not SQLException: an unimplemented lab method throws
        // UnsupportedOperationException, which must show up as a genuine test failure here
        // rather than silently vanishing past a catch block that only knew about SQLException.
        AtomicReference<Throwable> failureA = new AtomicReference<>();
        AtomicReference<Throwable> failureB = new AtomicReference<>();

        Thread threadA = new Thread(() -> {
            try {
                lab.naiveTransfer(connA, 1, 2, 10, awaitBarrier(barrier));
            } catch (Throwable e) {
                failureA.set(e);
            }
        });
        Thread threadB = new Thread(() -> {
            try {
                lab.naiveTransfer(connB, 2, 1, 10, awaitBarrier(barrier));
            } catch (Throwable e) {
                failureB.set(e);
            }
        });

        threadA.start();
        threadB.start();
        threadA.join(15_000);
        threadB.join(15_000);

        long deadlockFailures = java.util.Arrays.asList(failureA.get(), failureB.get()).stream()
                .filter(Objects::nonNull)
                .filter(e -> e instanceof SQLException se && "40P01".equals(se.getSQLState()))
                .count();

        assertThat(deadlockFailures)
                .as("Postgres's deadlock detector must abort exactly one of the two transactions")
                .isEqualTo(1);

        connA.close();
        connB.close();
    }

    @Test
    @DisplayName("the SAME crossing pattern, with ordered locking, never deadlocks")
    void orderedLockingAvoidsDeadlock() throws Exception {
        // No synchronization hook here, deliberately: ordered locking means BOTH directions
        // contend for the same row (the lower id) FIRST. Forcing them to rendezvous at a
        // barrier after that first lock - like the naive test does - would hang, since only
        // one thread can ever win that first lock and the other never reaches the barrier at
        // all. The natural row-lock contention is exactly what serializes these correctly.
        for (int i = 0; i < 15; i++) {
            Connection connA = newConnection();
            Connection connB = newConnection();
            AtomicReference<Throwable> failureA = new AtomicReference<>();
            AtomicReference<Throwable> failureB = new AtomicReference<>();

            Thread threadA = new Thread(() -> {
                try {
                    lab.orderedTransfer(connA, 1, 2, 10, null);
                } catch (Throwable e) {
                    failureA.set(e);
                }
            });
            Thread threadB = new Thread(() -> {
                try {
                    lab.orderedTransfer(connB, 2, 1, 10, null);
                } catch (Throwable e) {
                    failureB.set(e);
                }
            });

            threadA.start();
            threadB.start();
            threadA.join(15_000);
            threadB.join(15_000);

            // Cast to Object: SQLException implements Iterable<Throwable> (for exception
            // chains), which makes assertThat() genuinely ambiguous against a bare SQLException.
            assertThat((Object) failureA.get()).as("iteration %d, thread A", i).isNull();
            assertThat((Object) failureB.get()).as("iteration %d, thread B", i).isNull();

            connA.close();
            connB.close();
        }

        // Equal and opposite transfers, run 15 times: balances must be exactly back to where
        // they started - correctness, not just "no exception".
        try (Statement statement = setupConnection.createStatement();
             var rs = statement.executeQuery("SELECT id, balance FROM accounts ORDER BY id")) {
            rs.next();
            assertThat(rs.getInt("balance")).isEqualTo(100);
            rs.next();
            assertThat(rs.getInt("balance")).isEqualTo(100);
        }
    }

    @Test
    @DisplayName("MVCC: a plain read never blocks on another transaction's uncommitted write lock")
    void plainReadDoesNotBlockOnWriteLock() throws Exception {
        Connection writer = newConnection();
        Connection reader = newConnection();
        lab.lockRowForUpdate(writer, 1);   // holds an uncommitted row lock

        long start = System.nanoTime();
        int balance;
        try (Statement statement = reader.createStatement();
             var rs = statement.executeQuery("SELECT balance FROM accounts WHERE id = 1")) {
            rs.next();
            balance = rs.getInt(1);
        }
        long elapsedMillis = (System.nanoTime() - start) / 1_000_000;

        assertThat(elapsedMillis).as("a plain read must never wait for a writer's lock").isLessThan(1_000);
        assertThat(balance).as("readers see the last COMMITTED value, not a lock lookout").isEqualTo(100);

        reader.commit();
        writer.rollback();
        writer.close();
        reader.close();
    }

    @Test
    @DisplayName("a LOCKING read (FOR UPDATE) DOES block until the writer finishes")
    void lockingReadBlocksUntilWriterFinishes() throws Exception {
        Connection writer = newConnection();
        Connection reader = newConnection();
        lab.lockRowForUpdate(writer, 1);

        CountDownLatch acquired = new CountDownLatch(1);
        Thread blockedReader = new Thread(() -> {
            try {
                lab.lockRowForUpdate(reader, 1);
                acquired.countDown();
            } catch (SQLException ignored) {
                // not the point of this test
            }
        });
        blockedReader.start();

        assertThat(acquired.await(300, TimeUnit.MILLISECONDS))
                .as("a FOR UPDATE read must block while the row is locked")
                .isFalse();

        writer.commit();

        assertThat(acquired.await(5, TimeUnit.SECONDS))
                .as("releasing the writer's lock must unblock the waiting reader")
                .isTrue();
        blockedReader.join();

        reader.rollback();
        writer.close();
        reader.close();
    }
}
