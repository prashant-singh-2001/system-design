package sd.p01.day08;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class Day08HttpServerTest {

    private static HttpResponse<String> get(int port, String path) throws Exception {
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://127.0.0.1:" + port + path))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        }
    }

    @Test
    @DisplayName("GET /health returns 200 ok")
    void health() throws Exception {
        try (TinyHttpServer server = new TinyHttpServer()) {
            HttpResponse<String> response = get(server.port(), "/health");
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.body()).isEqualTo("ok");
        }
    }

    @Test
    @DisplayName("GET /echo?msg=... reflects the query parameter")
    void echo() throws Exception {
        try (TinyHttpServer server = new TinyHttpServer()) {
            assertThat(get(server.port(), "/echo?msg=hello").body()).isEqualTo("hello");
            assertThat(get(server.port(), "/echo").body())
                    .as("a missing parameter is not an error - return an empty body")
                    .isEmpty();
        }
    }

    @Test
    @DisplayName("an unknown path returns 404")
    void notFound() throws Exception {
        try (TinyHttpServer server = new TinyHttpServer()) {
            HttpResponse<String> response = get(server.port(), "/nope");
            assertThat(response.statusCode()).isEqualTo(404);
            assertThat(response.body()).isEqualTo("not found");
        }
    }

    @Test
    @DisplayName("reusing one connection beats reconnecting every time")
    void connectionReuseWins() throws IOException {
        int requests = 300;
        try (TinyHttpServer server = new TinyHttpServer()) {
            // Warm the server up so we compare steady state, not class loading.
            ConnectionCostBenchmark.reusedConnection(server.port(), 50);

            var fresh = ConnectionCostBenchmark.newConnectionEachTime(server.port(), requests);
            var reused = ConnectionCostBenchmark.reusedConnection(server.port(), requests);

            System.out.printf("%n  %-26s %8.3f ms/request%n",
                    fresh.mode(), fresh.millisPerRequest());
            System.out.printf("  %-26s %8.3f ms/request%n%n",
                    reused.mode(), reused.millisPerRequest());

            assertThat(reused.elapsed())
                    .as("""
                            Over loopback the handshake is cheap and this margin is small.
                            Across a datacenter it is a full round trip per request; across a
                            continent it is 150 ms. That is why connection pools exist.""")
                    .isLessThan(fresh.elapsed());
        }
    }
}
