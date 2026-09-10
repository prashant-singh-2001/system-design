package sd.p01.day07;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/** GIVEN. Opens a connection, sends one line, reads one line back. */
public final class EchoClient {

    private EchoClient() {
    }

    public static String sendAndReceive(int port, String message) throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", port), 3_000);
            socket.setSoTimeout(5_000);

            OutputStream out = socket.getOutputStream();
            out.write((message + "\n").getBytes(StandardCharsets.UTF_8));
            out.flush();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            return in.readLine();
        }
    }
}
