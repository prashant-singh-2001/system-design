package sd.p05.day47;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class Day47LsmTest {

    @Test
    @DisplayName("a value still in the memtable round-trips without ever touching disk")
    void putThenGetBeforeFlush(@TempDir Path dir) throws IOException {
        LsmStore store = new LsmStore(dir, 100);

        store.put("a", "1");

        assertThat(store.get("a")).contains("1");
        assertThat(store.sstableCount()).isZero();
    }

    @Test
    @DisplayName("exceeding the flush threshold writes an SSTable and resets the memtable")
    void exceedingThresholdFlushes(@TempDir Path dir) throws IOException {
        LsmStore store = new LsmStore(dir, 3);

        store.put("a", "1");
        store.put("b", "2");
        store.put("c", "3");   // hits the threshold

        assertThat(store.sstableCount()).isEqualTo(1);
        assertThat(store.activeMemTableSize()).isZero();
    }

    @Test
    @DisplayName("a value survives a flush - reads fall through to the SSTable")
    void readsFallThroughToFlushedData(@TempDir Path dir) throws IOException {
        LsmStore store = new LsmStore(dir, 2);

        store.put("a", "1");
        store.put("b", "2");   // flushes both to one SSTable

        assertThat(store.get("a")).contains("1");
        assertThat(store.get("b")).contains("2");
    }

    @Test
    @DisplayName("the newest write for a key wins, even across a flush boundary")
    void newestWriteWins(@TempDir Path dir) throws IOException {
        LsmStore store = new LsmStore(dir, 1);   // flush after every single write

        store.put("a", "old");
        store.put("a", "new");

        assertThat(store.get("a")).contains("new");
    }

    @Test
    @DisplayName("a deleted key reports absent, even though an older SSTable still has a value for it")
    void tombstoneHidesOlderValue(@TempDir Path dir) throws IOException {
        LsmStore store = new LsmStore(dir, 1);   // flush after every single write

        store.put("a", "1");   // flushed to its own SSTable
        store.delete("a");     // flushed as a tombstone to a NEWER SSTable

        assertThat(store.get("a")).isEmpty();
    }

    @Test
    @DisplayName("compaction merges everything into one SSTable and drops tombstoned keys")
    void compactionMergesAndDropsTombstones(@TempDir Path dir) throws IOException {
        LsmStore store = new LsmStore(dir, 1);
        store.put("a", "1");
        store.put("b", "2");
        store.put("a", "1-updated");
        store.delete("b");

        store.compact();

        assertThat(store.sstableCount()).as("everything merged into a single file").isEqualTo(1);
        assertThat(store.get("a")).contains("1-updated");
        assertThat(store.get("b")).as("a tombstoned key must not survive compaction").isEmpty();
    }

    @Test
    @DisplayName("compaction does not lose or corrupt any live key")
    void compactionPreservesLiveData(@TempDir Path dir) throws IOException {
        LsmStore store = new LsmStore(dir, 2);
        for (int i = 0; i < 10; i++) {
            store.put("key-" + i, "value-" + i);
        }

        store.compact();

        for (int i = 0; i < 10; i++) {
            assertThat(store.get("key-" + i)).contains("value-" + i);
        }
    }
}
