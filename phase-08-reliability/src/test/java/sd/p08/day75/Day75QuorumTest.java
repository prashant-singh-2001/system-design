package sd.p08.day75;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Day75QuorumTest {

    @Test
    @DisplayName("W + R > N is the whole overlap rule")
    void overlapArithmetic() {
        assertThat(new QuorumConfig(3, 2, 2).guaranteesOverlap()).isTrue();
        assertThat(new QuorumConfig(3, 3, 1).guaranteesOverlap()).isTrue();
        assertThat(new QuorumConfig(3, 1, 3).guaranteesOverlap()).isTrue();

        assertThat(new QuorumConfig(3, 1, 1).guaranteesOverlap())
                .as("W=R=1 is fast and gives no guarantee that a read sees the latest write")
                .isFalse();
        assertThat(new QuorumConfig(5, 2, 2).guaranteesOverlap()).isFalse();
    }

    @Test
    @DisplayName("fault tolerance is N minus the quorum")
    void faultTolerance() {
        QuorumConfig balanced = QuorumConfig.balanced(3);

        assertThat(balanced.writeQuorum()).isEqualTo(2);
        assertThat(balanced.writeFaultTolerance()).isEqualTo(1);
        assertThat(balanced.readFaultTolerance()).isEqualTo(1);

        QuorumConfig fastReads = new QuorumConfig(3, 3, 1);
        assertThat(fastReads.writeFaultTolerance())
                .as("W=N means one dead replica blocks every write")
                .isZero();
        assertThat(fastReads.readFaultTolerance()).isEqualTo(2);
    }

    @Test
    @DisplayName("an impossible configuration is rejected")
    void validation() {
        assertThatThrownBy(() -> new QuorumConfig(3, 4, 2))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new QuorumConfig(3, 2, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("a healthy cluster reads what it wrote")
    void happyPath() {
        QuorumStore store = new QuorumStore(QuorumConfig.balanced(3));

        store.write("k", "v1");

        assertThat(store.read("k")).contains("v1");
    }

    @Test
    @DisplayName("with W=R=2 of 3, one dead replica changes nothing")
    void survivesOneFailure() {
        QuorumStore store = new QuorumStore(QuorumConfig.balanced(3));
        store.write("k", "v1");

        store.fail(2);

        store.write("k", "v2");
        assertThat(store.read("k"))
                .as("2 of 3 is still a quorum - this is why W=R=2 is the usual default")
                .contains("v2");
    }

    @Test
    @DisplayName("lose the quorum and the store refuses rather than lying")
    void quorumNotMet() {
        QuorumStore store = new QuorumStore(QuorumConfig.balanced(3));
        store.fail(1);
        store.fail(2);

        assertThatThrownBy(() -> store.write("k", "v"))
                .isInstanceOf(QuorumStore.QuorumNotMetException.class);
        assertThatThrownBy(() -> store.read("k"))
                .isInstanceOf(QuorumStore.QuorumNotMetException.class);
    }

    @Test
    @DisplayName("a failed write can still have landed somewhere - 'failed' is not 'did not happen'")
    void partialWritesAreVisible() {
        QuorumStore store = new QuorumStore(QuorumConfig.balanced(3));
        store.fail(1);
        store.fail(2);

        assertThatThrownBy(() -> store.write("k", "partial"))
                .isInstanceOf(QuorumStore.QuorumNotMetException.class);

        assertThat(store.peek(0, "k"))
                .as("""
                        The write was reported as failed and one replica has it anyway. Once the
                        others recover, that value can win a read. This is why a client that sees
                        a write error still cannot conclude the write did not happen - and why
                        idempotency (Day 74) is not optional.""")
                .isPresent();
    }

    @Test
    @DisplayName("a stale replica is outvoted by version, not by count")
    void newestVersionWins() {
        QuorumStore store = new QuorumStore(QuorumConfig.balanced(3));
        store.write("k", "old");

        store.fail(0);
        store.write("k", "new");          // lands on replicas 1 and 2
        store.recover(0);

        assertThat(store.read("k"))
                .as("replica 0 still holds 'old', but its version is lower")
                .contains("new");
    }

    @Test
    @DisplayName("read repair converges the cluster - every read is a chance to heal")
    void readRepair() {
        QuorumStore store = new QuorumStore(QuorumConfig.balanced(3));
        store.write("k", "old");

        store.fail(0);
        store.write("k", "new");
        store.recover(0);

        assertThat(store.peek(0, "k")).hasValueSatisfying(
                stale -> assertThat(stale.value()).isEqualTo("old"));

        int repaired = store.readRepair("k");

        assertThat(repaired).isEqualTo(1);
        assertThat(store.peek(0, "k")).hasValueSatisfying(
                healed -> assertThat(healed.value()).isEqualTo("new"));
    }

    @Test
    @DisplayName("W=R=1 is fast, and can return stale data")
    void weakQuorumIsStale() {
        QuorumStore store = new QuorumStore(new QuorumConfig(3, 1, 1));
        store.write("k", "v1");

        assertThat(store.config().guaranteesOverlap())
                .as("""
                        Nothing forces the read set to touch the write set, so a read may hit a
                        replica the write never reached. Fast, and a different guarantee - which
                        is a legitimate choice as long as it is a choice.""")
                .isFalse();
    }
}
