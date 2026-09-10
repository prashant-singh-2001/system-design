package sd.p01.day08;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * GIVEN - what a TCP connection actually costs you.
 *
 * <p>Opening a connection is a three-way handshake: one full round trip before a single byte
 * of your request moves. On a LAN that is a few hundred microseconds; across a continent it
 * is 150 ms. Add TLS and it is two more round trips.
 *
 * <p>That arithmetic is the entire reason connection pools, HTTP keep-alive and
 * {@code proxy_set_header Connection ""} in your nginx config exist.
 */
public final class ConnectionCostBenchmark {

    private ConnectionCostBenchmark() {
    }

    public record Result(String mode, int requests, Duration elapsed) {

        public double millisPerRequest() {
            return elapsed.toNanos() / 1_000_000.0 / requests;
        }
    }

    /** A fresh connection for every request - handshake, request, response, teardown. */
    public static Result newConnectionEachTime(int port, int requests) throws IOException {
        long began = System.nanoTime();
        for (int i = 0; i < requests; i++) {
            try (Socket socket = connect(port)) {
                writeRequest(socket, port, false);
                readResponse(socket);
            }
        }
        return new Result("new connection each time", requests,
                Duration.ofNanos(System.nanoTime() - began));
    }

    /** One connection, kept alive, reused for every request. */
    public static Result reusedConnection(int port, int requests) throws IOException {
        long began = System.nanoTime();
        try (Socket socket = connect(port)) {
            for (int i = 0; i < requests; i++) {
                writeRequest(socket, port, true);
                readResponse(socket);
            }
        }
        return new Result("one reused connection", requests,
                Duration.ofNanos(System.nanoTime() - began));
    }

    private static Socket connect(int port) throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress("127.0.0.1", port), 3_000);
        socket.setSoTimeout(5_000);
        socket.setTcpNoDelay(true);
        return socket;
    }

    private static void writeRequest(Socket socket, int port, boolean keepAlive) throws IOException {
        String request = "GET /health HTTP/1.1\r\n"
                + "Host: 127.0.0.1:" + port + "\r\n"
                + "Connection: " + (keepAlive ? "keep-alive" : "close") + "\r\n\r\n";
        OutputStream out = socket.getOutputStream();
        out.write(request.getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    /** Reads status line and headers, then exactly Content-Length bytes of body. */
    private static void readResponse(Socket socket) throws IOException {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        int contentLength = 0;
        String line;
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            if (line.toLowerCase().startsWith("content-length:")) {
                contentLength = Integer.parseInt(line.substring(15).trim());
            }
        }
        for (int i = 0; i < contentLength; i++) {
            if (in.read() < 0) {
                break;
            }
        }
    }
}
