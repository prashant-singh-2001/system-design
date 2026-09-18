package sd.p01.day07;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * TODO(day07): the same echo service on ONE thread, using a {@link java.nio.channels.Selector}.
 *
 * <p>The shape you are building:
 *
 * <pre>
 *   ServerSocketChannel server = ServerSocketChannel.open();
 *   server.bind(new InetSocketAddress(0));
 *   server.configureBlocking(false);
 *   Selector selector = Selector.open();
 *   server.register(selector, SelectionKey.OP_ACCEPT);
 *
 *   while (running) {
 *       selector.select(timeout);
 *       for (SelectionKey key : selector.selectedKeys()) {
 *           if (key.isAcceptable()) {
 *               // accept(), configureBlocking(false), register for OP_READ
 *           } else if (key.isReadable()) {
 *               // read into a ByteBuffer, flip it, write it straight back
 *               // read() returning -1 means the peer closed: cancel the key, close the channel
 *           }
 *       }
 *       selectedKeys().clear();   // forgetting this is the classic NIO bug
 *   }
 * </pre>
 *
 * <p>Bind to port 0 in the constructor and expose the real port via {@link #port()}, so the
 * test can find you. Run the select loop on a daemon thread.
 *
 * <p>Three traps worth knowing before you start:
 * <ul>
 *   <li>You must clear the selected-key set each pass, or you reprocess stale keys forever.</li>
 *   <li>{@code write()} is not guaranteed to write everything. For this exercise a single
 *       write of a small buffer is fine, but in production you register OP_WRITE and drain
 *       a pending buffer. Know that you are skipping it.</li>
 *   <li>Anything slow inside the loop blocks EVERY connection. One thread, no exceptions.</li>
 * </ul>
 *
 * <p>When it works, ask the real question of the day: this is far more code and far more
 * subtle than {@link BlockingEchoServer}. Given virtual threads, when is it still worth it?
 */
public final class NioEchoServer implements EchoServer {

    private static final long SELECT_TIMEOUT_MS = 1000;

    private final ServerSocketChannel serverChannel;
    private final Selector selector;
    private final int port;
    private volatile boolean running = true;

    public NioEchoServer() {
        try {
            this.serverChannel = ServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress(0));
            serverChannel.configureBlocking(false);

            this.selector = Selector.open();
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            this.port = serverChannel.socket().getLocalPort();
        } catch (IOException e) {
            throw new IllegalStateException("could not bind", e);
        }

        Thread loop = new Thread(this::selectLoop, "nio-echo-select");
        loop.setDaemon(true);
        loop.start();
    }

    private void selectLoop() {
        while (running) {
            try {
                selector.select(SELECT_TIMEOUT_MS);
            } catch (IOException e) {
                if (running) {
                    throw new IllegalStateException("select failed", e);
                }
                return; // selector closed during shutdown
            }

            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
            while (keys.hasNext()) {
                SelectionKey key = keys.next();
                keys.remove(); // TODO(day07): this line IS the "clear selectedKeys" trap - keep it

                try {
                    if (!key.isValid()) {
                        continue;
                    }
                    if (key.isAcceptable()) {
                        handleAccept(key);
                    } else if (key.isReadable()) {
                        handleRead(key);
                    }
                } catch (IOException e) {
                    key.cancel();
                    closeQuietly(key.channel());
                }
            }
        }
    }

    /**
     * TODO(day07): accept the pending connection on {@code key}'s channel, make the accepted
     * {@link SocketChannel} non-blocking, and register it with {@link #selector} for
     * {@code OP_READ}.
     */
    private void handleAccept(SelectionKey key) throws IOException {
        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);
    }

    /**
     * TODO(day07): read from {@code key}'s channel into a {@link ByteBuffer}, and write whatever
     * was read straight back (this is an echo server - no need to parse lines).
     *
     * <p>If {@code read()} returns {@code -1}, the peer closed the connection: cancel the key
     * and close the channel (see {@link #closeQuietly(java.nio.channels.Channel)}).
     */
    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int bytesRead = clientChannel.read(buffer);
        if (bytesRead == -1) {
            key.cancel();
            closeQuietly(clientChannel);
        } else {
            buffer.flip();
            clientChannel.write(buffer);
            buffer.clear();
        }
    }

    private static void closeQuietly(java.nio.channels.Channel channel) {
        try {
            channel.close();
        } catch (IOException ignored) {
            // shutting down
        }
    }

    @Override
    public int port() {
        return port;
    }

    @Override
    public String model() {
        return "non-blocking, single-threaded selector";
    }

    @Override
    public void close() {
        running = false;
        selector.wakeup();
        closeQuietly(serverChannel);
        // closeQuietly(selector);
    }
}
