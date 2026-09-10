package sd.p07.day65;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Day 65 - the outbox pattern, and the problem it solves.
 *
 * <p>You met this problem on Day 46 without a name for it. An order is placed, and two things
 * must happen: a row goes into the database, and an event goes onto Kafka. They are two different
 * systems, so there is no transaction spanning both, and either can fail independently.
 *
 * <ul>
 *   <li><b>Write the database, then publish.</b> If the publish fails, the order exists and
 *       nobody downstream ever hears about it. No email, no shipment, no analytics - and no error
 *       anywhere, because the order succeeded.</li>
 *   <li><b>Publish, then write the database.</b> If the write fails, you have announced an order
 *       that does not exist. Downstream services act on a phantom.</li>
 *   <li><b>Wrap both in a distributed transaction (2PC).</b> Slow, operationally miserable, and
 *       not supported by Kafka anyway.</li>
 * </ul>
 *
 * <p>The <b>outbox pattern</b> dissolves the problem instead of solving it. Write the order row
 * and an outbox row <b>in the same local database transaction</b>. One transaction, one system -
 * so it is atomic, with no distributed anything. A separate relay process then reads unpublished
 * outbox rows, publishes them to Kafka, and marks them published.
 *
 * <p>The relay may crash after publishing and before marking, so a row can be published twice.
 * That is at-least-once delivery, deliberately chosen: <b>duplicates you can absorb beat messages
 * you cannot recover.</b> Consumers handle it with idempotency (Day 74).
 *
 * <p>The cost you accept: events are published slightly late (relay latency), and you now operate
 * a relay and a table that needs pruning. In exchange you get a guarantee that is otherwise
 * genuinely unavailable: the event exists if and only if the order does.
 */
public final class OrderService {

    private final Connection connection;

    public OrderService(Connection connection) {
        this.connection = connection;
    }

    /**
     * TODO(day65): insert the order AND its outbox row in ONE transaction.
     *
     * <p>Steps:
     * <ol>
     *   <li>{@code setAutoCommit(false)}</li>
     *   <li>insert into {@code orders (id, customer_id, total_cents)}</li>
     *   <li>insert into {@code outbox (aggregate_id, event_type, payload, published)} with
     *       {@code published = false}</li>
     *   <li>{@code commit()}</li>
     * </ol>
     *
     * <p>On any failure, {@code rollback()} and rethrow. Both rows appear or neither does - and
     * that atomicity is the entire pattern.
     *
     * <p>Restore {@code autoCommit} in a finally block; leaving a pooled connection in manual mode
     * is a subtle, nasty bug for whoever borrows it next.
     */
    public void placeOrder(String orderId, String customerId, long totalCents, String payload)
            throws SQLException {
        throw new UnsupportedOperationException("TODO(day65): one transaction, two inserts");
    }

    /**
     * TODO(day65): the broken version, for contrast. Insert ONLY the order row - no outbox.
     *
     * <p>The test uses this to show the failure mode: an order exists that nothing downstream
     * will ever learn about.
     */
    public void placeOrderWithoutOutbox(String orderId, String customerId, long totalCents)
            throws SQLException {
        throw new UnsupportedOperationException("TODO(day65): insert the order only");
    }
}
