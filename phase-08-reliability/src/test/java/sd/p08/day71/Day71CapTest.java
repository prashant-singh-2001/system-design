package sd.p08.day71;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sd.p08.day71.ConsistencyChooser.Requirements;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Day71CapTest {

    @Test
    @DisplayName("with no partition, both strategies are consistent AND available")
    void healthyNetworkGivesYouBoth() {
        for (PartitionStrategy strategy : PartitionStrategy.values()) {
            PartitionedStore store = new PartitionedStore(strategy);

            store.write(ReplicaSide.MAJORITY, "balance", "100");

            assertThat(store.read(ReplicaSide.MINORITY, "balance"))
                    .as("%s replicates normally when the network works - which is nearly always",
                            strategy)
                    .contains("100");
        }
    }

    @Test
    @DisplayName("CP: the minority side refuses rather than risk divergence")
    void cpSacrificesAvailability() {
        PartitionedStore store = new PartitionedStore(PartitionStrategy.CP);
        store.write(ReplicaSide.MAJORITY, "balance", "100");
        store.partition();

        store.write(ReplicaSide.MAJORITY, "balance", "150");

        assertThatThrownBy(() -> store.write(ReplicaSide.MINORITY, "balance", "999"))
                .as("""
                        A wrong balance costs more than no balance, so this side refuses. Users on
                        the minority side see an outage - the price of never being wrong.""")
                .isInstanceOf(PartitionedStore.UnavailableException.class);
    }

    @Test
    @DisplayName("AP: both sides accept, and the data diverges")
    void apSacrificesConsistency() {
        PartitionedStore store = new PartitionedStore(PartitionStrategy.AP);
        store.write(ReplicaSide.MAJORITY, "cart", "1 item");
        store.partition();

        store.write(ReplicaSide.MAJORITY, "cart", "2 items");
        store.write(ReplicaSide.MINORITY, "cart", "3 items");

        assertThat(store.read(ReplicaSide.MAJORITY, "cart")).contains("2 items");
        assertThat(store.read(ReplicaSide.MINORITY, "cart"))
                .as("nobody was refused, and now there are two truths")
                .contains("3 items");
    }

    @Test
    @DisplayName("healing reconciles - and last-write-wins silently discards one side")
    void healingIsLossy() {
        PartitionedStore store = new PartitionedStore(PartitionStrategy.AP);
        store.partition();

        store.write(ReplicaSide.MAJORITY, "cart", "majority version");
        store.write(ReplicaSide.MINORITY, "cart", "minority version");

        store.heal();

        assertThat(store.read(ReplicaSide.MAJORITY, "cart"))
                .as("""
                        One customer's change vanished, with no error and no record. That is rarely
                        an acceptable product decision even when it is an easy engineering one -
                        which is why real AP systems reach for vector clocks or CRDTs instead.""")
                .contains("minority version");
        assertThat(store.read(ReplicaSide.MINORITY, "cart")).contains("minority version");
    }

    @Test
    @DisplayName("a CP minority side can still read - it just cannot accept writes")
    void cpStillServesReads() {
        PartitionedStore store = new PartitionedStore(PartitionStrategy.CP);
        store.write(ReplicaSide.MAJORITY, "balance", "100");
        store.partition();

        assertThat(store.read(ReplicaSide.MINORITY, "balance"))
                .as("stale, but available - most 'CP' systems degrade rather than go dark")
                .contains("100");
    }

    @Test
    @DisplayName("the strongest applicable requirement decides the model")
    void choosingAConsistencyModel() {
        assertThat(ConsistencyChooser.choose(new Requirements(true, true, true, true)))
                .isEqualTo(ConsistencyModel.LINEARIZABLE);
        assertThat(ConsistencyChooser.choose(new Requirements(false, true, true, true)))
                .isEqualTo(ConsistencyModel.CAUSAL);
        assertThat(ConsistencyChooser.choose(new Requirements(false, false, true, true)))
                .isEqualTo(ConsistencyModel.READ_YOUR_WRITES);
        assertThat(ConsistencyChooser.choose(new Requirements(false, false, false, true)))
                .isEqualTo(ConsistencyModel.MONOTONIC_READS);
        assertThat(ConsistencyChooser.choose(new Requirements(false, false, false, false)))
                .isEqualTo(ConsistencyModel.EVENTUAL);
    }

    @Test
    @DisplayName("most 'we need strong consistency' requirements are really read-your-writes")
    void theCommonCase() {
        Requirements profileEdit = new Requirements(false, false, true, false);

        assertThat(ConsistencyChooser.choose(profileEdit))
                .as("""
                        Enormously cheaper than linearizable: a routing decision - send this user
                        to the replica holding their write - rather than a consensus round trip.""")
                .isEqualTo(ConsistencyModel.READ_YOUR_WRITES);
    }

    @Test
    @DisplayName("PACELC classifies the 99.9% of the time CAP ignores")
    void pacelcClassification() {
        assertThat(ConsistencyChooser.pacelc(PartitionStrategy.CP, false)).isEqualTo("PC/EC");
        assertThat(ConsistencyChooser.pacelc(PartitionStrategy.AP, true)).isEqualTo("PA/EL");
        assertThat(ConsistencyChooser.pacelc(PartitionStrategy.CP, true)).isEqualTo("PC/EL");
        assertThat(ConsistencyChooser.pacelc(PartitionStrategy.AP, false)).isEqualTo("PA/EC");
    }
}
