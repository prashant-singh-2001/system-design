package sd.p05.day45;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class Day45PoolSizingTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    private final PoolSizingBenchmark benchmark = new PoolSizingBenchmark();
    private HikariDataSource dataSource;

    private HikariDataSource poolOfSize(int maxPoolSize) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(POSTGRES.getJdbcUrl());
        config.setUsername(POSTGRES.getUsername());
        config.setPassword(POSTGRES.getPassword());
        config.setMaximumPoolSize(maxPoolSize);
        dataSource = new HikariDataSource(config);
        return dataSource;
    }

    @AfterEach
    void closePool() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    @Test
    @DisplayName("a small pool caps concurrency at its configured size, no matter how many clients contend")
    void smallPoolCapsConcurrency() throws Exception {
        int maxPoolSize = 3;
        List<QueryTiming> timings = benchmark.runWorkload(poolOfSize(maxPoolSize), 10, 3);

        int observedOverlap = PoolSizingBenchmark.maxConcurrentOverlap(timings);

        assertThat(timings).hasSize(30);
        assertThat(observedOverlap)
                .as("the pool must never let more than maxPoolSize queries run at once")
                .isLessThanOrEqualTo(maxPoolSize);
        assertThat(observedOverlap)
                .as("with 10 clients wanting 3 connections, the pool should actually saturate")
                .isEqualTo(maxPoolSize);
    }

    @Test
    @DisplayName("a pool sized to match the client count lets nearly everything run at once")
    void largePoolAllowsFullConcurrency() throws Exception {
        int maxPoolSize = 10;
        List<QueryTiming> timings = benchmark.runWorkload(poolOfSize(maxPoolSize), 10, 3);

        int observedOverlap = PoolSizingBenchmark.maxConcurrentOverlap(timings);

        assertThat(timings).hasSize(30);
        assertThat(observedOverlap).isLessThanOrEqualTo(maxPoolSize);
        assertThat(observedOverlap)
                .as("with as many connections as clients, contention should mostly disappear")
                .isGreaterThanOrEqualTo(7);
    }
}
