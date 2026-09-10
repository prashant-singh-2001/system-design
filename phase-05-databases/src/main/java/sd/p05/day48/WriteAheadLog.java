package sd.p05.day48;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO(day48): durability, mechanically. A mutation is not "durable" because you believe you
 * wrote it - it is durable because it is on disk, in a form you can read back even if the
 * process dies the instant after writing it.
 *
 * <p>Encode each {@link LogRecord} as {@code operation\tkey\tvalue} (for a DELETE, {@code value}
 * can just be an empty string), turn that into UTF-8 bytes, and write it LENGTH-PREFIXED:
 * a 4-byte big-endian length, then that many bytes of record data. Length-prefixing is what
 * makes a TRUNCATED final record detectable on replay, rather than silently misread as valid
 * data or a hard crash of the reader - the same framing idea protects binary protocols and
 * Kafka's own log segments.
 *
 * <p>{@code append(operation, key, value)}: write one length-prefixed record to the end of the
 * file, then {@code force(true)} the underlying channel - an explicit fsync. Skipping the fsync
 * would mean "written" only means "handed to the OS's page cache", which a real power loss can
 * still lose.
 *
 * <p>{@code replay()}: read length-prefixed records from the start of the file, using a
 * {@code RandomAccessFile} and its {@code readFully(byte[])} - which throws {@code
 * EOFException} the moment it cannot fill the buffer completely, rather than silently handing
 * back a short read. Read 4 bytes for the length; catching {@code EOFException} here means a
 * clean end of file - stop normally. Read that many bytes for the record body; catching {@code
 * EOFException} HERE means the length prefix made it to disk but the record body did not - a
 * crash mid-write - stop here too, WITHOUT throwing out of {@code replay()} itself, and do not
 * include the partial record. Otherwise decode and add the record to the result. Return every
 * complete record found, in the order they were written.
 */
public final class WriteAheadLog {

    private final Path file;

    public WriteAheadLog(Path file) {
        this.file = file;
    }

    public void append(String operation, String key, String value) throws IOException {
        throw new UnsupportedOperationException(
                "TODO(day48): length-prefix the encoded record, append it, fsync");
    }

    public List<LogRecord> replay() throws IOException {
        throw new UnsupportedOperationException(
                "TODO(day48): read length-prefixed records, stopping cleanly at a torn final one");
    }
}
