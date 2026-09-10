package sd.p05.day48;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class Day48WalTest {

    @Test
    @DisplayName("recovering from an untouched WAL rebuilds every put")
    void recoverRebuildsPuts(@TempDir Path dir) throws IOException {
        Path walFile = dir.resolve("wal.log");
        DurableKeyValueStore store = DurableKeyValueStore.recover(walFile);
        store.put("a", "1");
        store.put("b", "2");

        DurableKeyValueStore recovered = DurableKeyValueStore.recover(walFile);

        assertThat(recovered.get("a")).contains("1");
        assertThat(recovered.get("b")).contains("2");
    }

    @Test
    @DisplayName("a delete replays correctly - the deleted key stays gone after recovery")
    void recoverAppliesDeletes(@TempDir Path dir) throws IOException {
        Path walFile = dir.resolve("wal.log");
        DurableKeyValueStore store = DurableKeyValueStore.recover(walFile);
        store.put("a", "1");
        store.put("b", "2");
        store.delete("a");

        DurableKeyValueStore recovered = DurableKeyValueStore.recover(walFile);

        assertThat(recovered.get("a")).isEmpty();
        assertThat(recovered.get("b")).contains("2");
    }

    @Test
    @DisplayName("recovering from a file that does not exist yet is a fresh, empty store")
    void recoverFromMissingFileIsEmpty(@TempDir Path dir) throws IOException {
        Path walFile = dir.resolve("does-not-exist.log");

        DurableKeyValueStore store = DurableKeyValueStore.recover(walFile);

        assertThat(store.get("anything")).isEmpty();
    }

    @Test
    @DisplayName("THE core exercise: recovery survives a simulated kill mid-write of the last record")
    void recoverySurvivesATornFinalRecord(@TempDir Path dir) throws IOException {
        Path walFile = dir.resolve("wal.log");
        DurableKeyValueStore store = DurableKeyValueStore.recover(walFile);
        store.put("a", "1");
        store.put("b", "2");
        store.put("c", "3");   // this is the record we are about to tear

        long fullLength = Files.size(walFile);
        try (RandomAccessFile raf = new RandomAccessFile(walFile.toFile(), "rw")) {
            raf.setLength(fullLength - 2);   // chop the last 2 bytes - a torn write mid-record
        }

        DurableKeyValueStore recovered = null;
        try {
            recovered = DurableKeyValueStore.recover(walFile);
        } catch (Exception e) {
            org.junit.jupiter.api.Assertions.fail(
                    "recovery must handle a truncated final record gracefully, not throw", e);
        }

        assertThat(recovered.get("a")).as("records before the torn one must still recover").contains("1");
        assertThat(recovered.get("b")).contains("2");
        assertThat(recovered.get("c"))
                .as("the torn record was never fully durable - it must not appear at all")
                .isEmpty();
    }

    @Test
    @DisplayName("appending after a successful recovery keeps building on the same durable log")
    void appendingAfterRecoveryExtendsTheLog(@TempDir Path dir) throws IOException {
        Path walFile = dir.resolve("wal.log");
        DurableKeyValueStore store = DurableKeyValueStore.recover(walFile);
        store.put("a", "1");

        DurableKeyValueStore restarted = DurableKeyValueStore.recover(walFile);
        restarted.put("b", "2");

        DurableKeyValueStore recoveredAgain = DurableKeyValueStore.recover(walFile);
        assertThat(recoveredAgain.get("a")).contains("1");
        assertThat(recoveredAgain.get("b")).contains("2");
    }
}
