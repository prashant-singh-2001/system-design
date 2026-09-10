package sd.p05.day48;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * TODO(day48): a key-value store where every mutation is durable the instant the call returns -
 * WRITE-AHEAD, meaning the log entry is written and fsynced BEFORE the in-memory state changes,
 * never after. If a crash happens between those two steps, the log already has the fact; replay
 * will apply it. If you did it the other way round - mutate memory, then log - a crash in
 * between would lose the mutation entirely with no record it was ever attempted.
 *
 * <p>{@code put(key, value)}: {@code wal.append("PUT", key, value)}, THEN update the in-memory
 * map.
 *
 * <p>{@code delete(key)}: {@code wal.append("DELETE", key, "")}, THEN remove from the map.
 *
 * <p>{@code get(key)}: read the in-memory map directly - the WAL is a write path concern, not a
 * read path one.
 *
 * <p>{@code recover(walFile)}: a static factory. Build a fresh, empty store backed by a {@link
 * WriteAheadLog} over {@code walFile}, then replay every record the log actually has and apply
 * each to the in-memory map (PUT sets, DELETE removes) - this IS crash recovery: after a real
 * crash, the in-memory map is gone, and this is how you rebuild it from nothing but the durable
 * log.
 */
public final class DurableKeyValueStore {

    private final WriteAheadLog wal;
    private final Map<String, String> data = new HashMap<>();

    private DurableKeyValueStore(WriteAheadLog wal) {
        this.wal = wal;
    }

    public static DurableKeyValueStore recover(Path walFile) throws IOException {
        throw new UnsupportedOperationException(
                "TODO(day48): build a fresh store, replay the WAL, apply each record");
    }

    public void put(String key, String value) throws IOException {
        throw new UnsupportedOperationException("TODO(day48): log the PUT, then update the map");
    }

    public void delete(String key) throws IOException {
        throw new UnsupportedOperationException("TODO(day48): log the DELETE, then update the map");
    }

    public Optional<String> get(String key) {
        throw new UnsupportedOperationException("TODO(day48): read from the in-memory map");
    }
}
