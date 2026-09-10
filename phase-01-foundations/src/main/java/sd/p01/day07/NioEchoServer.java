package sd.p01.day07;

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

    public NioEchoServer() {
        throw new UnsupportedOperationException("TODO(day07): implement the Selector loop");
    }

    @Override
    public int port() {
        throw new UnsupportedOperationException("TODO(day07): return the bound port");
    }

    @Override
    public String model() {
        return "non-blocking, single-threaded selector";
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException("TODO(day07): stop the loop and close the channels");
    }
}
