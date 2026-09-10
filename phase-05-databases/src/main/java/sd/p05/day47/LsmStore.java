package sd.p05.day47;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * TODO(day47): the coordinator - one active {@link MemTable} taking writes, a list of immutable,
 * flushed {@link SSTable}s behind it, newest first.
 *
 * <p>{@code put(key, value)} / {@code delete(key)}: write to the active memtable (a delete is
 * just a {@code put} of {@link SSTable#TOMBSTONE}). If the memtable's size reaches {@code
 * flushThreshold}, flush it to a new SSTable file and start a fresh, empty memtable - this IS
 * "memtable, SSTable flush" from today's deliverable, happening automatically as writes arrive.
 *
 * <p>{@code get(key)}: check the active memtable FIRST - it is the freshest data. If absent,
 * check each SSTable from NEWEST to OLDEST, returning the first match. A key can legitimately
 * exist in several places at once; whichever one is newest wins, and a {@link
 * SSTable#TOMBSTONE} found anywhere along the way means "treat this as deleted", full stop -
 * do not keep searching older SSTables past a tombstone.
 *
 * <p>{@code compact()}: merge every current SSTable into ONE new SSTable. Walk them
 * OLDEST-to-newest, letting a later {@code put} into a {@code TreeMap<String,String>} overwrite
 * an earlier one for the same key - that gives you "newest value per key" for free from map
 * semantics. Then drop every entry whose value is a tombstone (this is where a deleted key's
 * disk space is actually reclaimed), write what remains as a single new SSTable (building a
 * throwaway {@link MemTable} from the merged, sorted data and calling {@link SSTable#flush} on
 * it is a clean way to reuse that logic), delete the old SSTable files, and replace the list
 * with just the new one.
 */
public final class LsmStore {

    private final Path directory;
    private final int flushThreshold;
    private MemTable activeMemTable = new MemTable();
    private final List<SSTable> sstables = new ArrayList<>();
    private int nextFileId = 0;

    public LsmStore(Path directory, int flushThreshold) {
        this.directory = directory;
        this.flushThreshold = flushThreshold;
    }

    public void put(String key, String value) throws IOException {
        throw new UnsupportedOperationException(
                "TODO(day47): write to the memtable, flush if it has reached flushThreshold");
    }

    public void delete(String key) throws IOException {
        throw new UnsupportedOperationException("TODO(day47): put(key, SSTable.TOMBSTONE)");
    }

    public Optional<String> get(String key) throws IOException {
        throw new UnsupportedOperationException(
                "TODO(day47): memtable first, then each SSTable newest to oldest");
    }

    public void compact() throws IOException {
        throw new UnsupportedOperationException(
                "TODO(day47): merge oldest-to-newest, drop tombstones, write one new SSTable");
    }

    public int sstableCount() {
        return sstables.size();
    }

    public int activeMemTableSize() {
        return activeMemTable.size();
    }

    private void flushActiveMemTable() throws IOException {
        Path file = directory.resolve("sstable-" + (nextFileId++) + ".txt");
        sstables.add(0, SSTable.flush(activeMemTable, file));
        activeMemTable = new MemTable();
    }
}
