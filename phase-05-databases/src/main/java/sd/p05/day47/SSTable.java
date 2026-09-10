package sd.p05.day47;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * TODO(day47): an immutable, sorted, on-disk snapshot of a {@link MemTable} at the moment it was
 * flushed. "Sorted String Table" is the classic name (LevelDB, Cassandra and Bigtable all use
 * one); today's version is a plain text file, one {@code key\tvalue} pair per line, sorted by
 * key - real ones use a binary format and a sparse index, which is this brief's Stretch.
 *
 * <p>A tombstone - a key whose value has been DELETED rather than merely never set - is written
 * as {@code key\t<TOMBSTONE>} using {@link #TOMBSTONE}. Storing the deletion as data, instead of
 * just leaving the key out, is what lets {@link #get} correctly report "deleted" even when an
 * OLDER SSTable still has a real value for that same key sitting on disk.
 *
 * <p>{@code flush(memtable, file)}: write every entry from {@code memtable.entries()} (already
 * sorted) to {@code file}, one line each, and return an {@code SSTable} wrapping that file.
 *
 * <p>{@code get(key)}: a straightforward linear scan of the file, line by line, looking for a
 * matching key. Correct, and O(n) per lookup - exactly what a sparse index (Stretch) exists to
 * fix.
 */
public final class SSTable {

    public static final String TOMBSTONE = "<TOMBSTONE>";

    private final Path file;

    private SSTable(Path file) {
        this.file = file;
    }

    public static SSTable flush(MemTable memtable, Path file) throws IOException {
        throw new UnsupportedOperationException(
                "TODO(day47): write memtable.entries() to file, sorted, one 'key\\tvalue' per line");
    }

    public static SSTable existing(Path file) {
        return new SSTable(file);
    }

    public Path file() {
        return file;
    }

    public Optional<String> get(String key) throws IOException {
        throw new UnsupportedOperationException(
                "TODO(day47): scan the file's lines for a matching key");
    }

    /** Every entry in the file, in the order it was written (already sorted by key). */
    public List<Map.Entry<String, String>> readAll() throws IOException {
        throw new UnsupportedOperationException("TODO(day47): parse every line into an entry");
    }
}
