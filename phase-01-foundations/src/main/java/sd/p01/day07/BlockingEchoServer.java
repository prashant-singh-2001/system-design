package sd.p01.day07;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * GIVEN - thread per connection, the model every tutorial teaches.
 *
 * <p>Simple and readable: the call stack IS the connection state. The cost is one thread per
 * connection. At 10,000 idle-but-open connections that is 10,000 threads, mostly parked in
 * {@code read()}, each holding a stack. This is the C10K problem.
 *
 * <p>Note that virtual threads (Day 6) largely rescue this model - which is worth thinking
 * about while you write the Selector version.
 */
public final class BlockingEchoServer implements EchoServer {

    private final ServerSocket serverSocket;
    private final ExecutorService connections = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "blocking-echo-conn");
        t.setDaemon(true);
        return t;
    });
    private volatile boolean running = true;

    public BlockingEchoServer() {
        try {
            this.serverSocket = new ServerSocket(0);
        } catch (IOException e) {
            throw new IllegalStateException("could not bind", e);
        }
        Thread acceptor = new Thread(this::acceptLoop, "blocking-echo-accept");
        acceptor.setDaemon(true);
        acceptor.start();
    }

    private void acceptLoop() {
        while (running) {
            try {
                Socket socket = serverSocket.accept();
                connections.execute(() -> handle(socket));
            } catch (SocketException e) {
                return;                     // socket closed - normal shutdown
            } catch (IOException e) {
                if (running) {
                    throw new IllegalStateException("accept failed", e);
                }
            }
        }
    }

    private void handle(Socket socket) {
        try (socket;
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8)) {
            String line;
            while ((line = in.readLine()) != null) {
                out.println(line);
            }
        } catch (IOException e) {
            // client hung up mid-conversation; nothing to do
        }
    }

    @Override
    public int port() {
        return serverSocket.getLocalPort();
    }

    @Override
    public String model() {
        return "blocking, thread-per-connection";
    }

    @Override
    public void close() {
        running = false;
        try {
            serverSocket.close();
        } catch (IOException ignored) {
            // shutting down
        }
        connections.shutdownNow();
    }
}
