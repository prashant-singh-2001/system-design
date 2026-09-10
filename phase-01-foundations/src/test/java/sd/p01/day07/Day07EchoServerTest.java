package sd.p01.day07;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class Day07EchoServerTest {

    static Stream<Supplier<EchoServer>> servers() {
        return Stream.of(BlockingEchoServer::new, NioEchoServer::new);
    }

    @ParameterizedTest(name = "{index}: echoes a single message")
    @MethodSource("servers")
    void echoesOneMessage(Supplier<EchoServer> factory) throws Exception {
        try (EchoServer server = factory.get()) {
            assertThat(EchoClient.sendAndReceive(server.port(), "hello"))
                    .as("%s should echo the message verbatim", server.model())
                    .isEqualTo("hello");
        }
    }

    @ParameterizedTest(name = "{index}: handles 50 concurrent clients without mixing them up")
    @MethodSource("servers")
    void handlesConcurrentClients(Supplier<EchoServer> factory) throws Exception {
        int clients = 50;
        try (EchoServer server = factory.get();
             ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {

            List<Callable<String>> calls = new ArrayList<>();
            for (int i = 0; i < clients; i++) {
                String message = "client-" + i;
                calls.add(() -> EchoClient.sendAndReceive(server.port(), message));
            }

            List<Future<String>> futures = pool.invokeAll(calls);
            for (int i = 0; i < clients; i++) {
                assertThat(futures.get(i).get())
                        .as("%s crossed streams between connections", server.model())
                        .isEqualTo("client-" + i);
            }
        }
    }

    @Test
    @DisplayName("the selector server serves many connections from a single thread")
    void selectorUsesOneThread() throws Exception {
        try (EchoServer server = new NioEchoServer()) {
            for (int i = 0; i < 10; i++) {
                assertThat(EchoClient.sendAndReceive(server.port(), "ping-" + i))
                        .isEqualTo("ping-" + i);
            }

            long selectorThreads = Thread.getAllStackTraces().keySet().stream()
                    .filter(t -> t.getName().toLowerCase().contains("selector")
                            || t.getName().toLowerCase().contains("nio-echo"))
                    .count();

            System.out.printf("  selector-loop threads alive: %d%n", selectorThreads);
            assertThat(selectorThreads)
                    .as("the whole point is that one thread serves every connection")
                    .isLessThanOrEqualTo(2);
        }
    }
}
