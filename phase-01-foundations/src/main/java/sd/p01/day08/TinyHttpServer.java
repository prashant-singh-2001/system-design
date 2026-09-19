package sd.p01.day08;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * TODO(day08): a tiny HTTP server on the JDK's built-in {@code com.sun.net.httpserver.HttpServer}.
 *
 * <p>No framework. The goal is to see that an HTTP server is a socket, a parser and a routing
 * table - and that every framework you use is this plus conveniences.
 *
 * <p>Implement three routes:
 * <ul>
 *   <li>{@code GET /health}           -> 200, body {@code ok}</li>
 *   <li>{@code GET /echo?msg=hello}   -> 200, body {@code hello} (empty string if no msg)</li>
 *   <li>anything else                 -> 404, body {@code not found}</li>
 * </ul>
 *
 * <p>The shape:
 *
 * <pre>
 *   HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
 *   server.createContext("/health", exchange -> respond(exchange, 200, "ok"));
 *   server.createContext("/echo",   exchange -&gt; { ... });
 *   server.createContext("/",       exchange -> respond(exchange, 404, "not found"));
 *   server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
 *   server.start();
 * </pre>
 *
 * <p>Two things to get right, because both are real bugs people ship:
 * <ul>
 *   <li>{@code sendResponseHeaders} needs the byte length, not the string length. They differ
 *       the moment a non-ASCII character appears.</li>
 *   <li>You must close the response body stream, or the connection leaks.</li>
 * </ul>
 *
 * <p>Note {@code createContext("/")} is a PREFIX match, so it catches everything not matched
 * by a longer path. That is the whole routing algorithm.
 */
public final class TinyHttpServer implements AutoCloseable {

    HttpServer server;


    public TinyHttpServer() {
        try {
            server = HttpServer.create(new InetSocketAddress(0), 0);
            server.createContext("/health", exchange -> respond(exchange, 200, "ok"));
            server.createContext("/echo", exchange -> {
                String query = exchange.getRequestURI().getQuery();
                String msg = "";
                if (query != null && query.startsWith("msg=")) {
                    msg = query.substring(4);
                }
                respond(exchange, 200, msg);
            });
            server.createContext("/", exchange -> respond(exchange, 404, "not found"));
            server.setExecutor(java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor());
            server.start();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create HTTP server", e);
        }
    }

    /** The ephemeral port actually bound. */
    public int port() {
        return server.getAddress().getPort();
    }

    private static void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (var responseBody = exchange.getResponseBody()) {
            responseBody.write(bytes);
        }
    }

    @Override
    public void close() {
        server.stop(0);
    }
}
