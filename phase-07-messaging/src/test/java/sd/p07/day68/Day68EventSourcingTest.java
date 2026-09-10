package sd.p07.day68;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class Day68EventSourcingTest {

    private EventStore store;

    @BeforeEach
    void setUp() {
        store = new EventStore();
        store.append(new AccountEvent.Opened("acc-1", "Alice"));
        store.append(new AccountEvent.Deposited("acc-1", 10_000));
        store.append(new AccountEvent.Withdrawn("acc-1", 2_500));
        store.append(new AccountEvent.Opened("acc-2", "Bob"));
        store.append(new AccountEvent.Deposited("acc-1", 500));
        store.append(new AccountEvent.Deposited("acc-2", 7_000));
    }

    @Test
    @DisplayName("the log keeps every event, in order")
    void logIsAppendOnly() {
        assertThat(store.size()).isEqualTo(6);
        assertThat(store.allEvents()).hasSize(6);
        assertThat(store.allEvents().get(0)).isInstanceOf(AccountEvent.Opened.class);
    }

    @Test
    @DisplayName("events are retrievable per aggregate, in order")
    void eventsPerAccount() {
        assertThat(store.eventsFor("acc-1")).hasSize(4);
        assertThat(store.eventsFor("acc-2")).hasSize(2);
        assertThat(store.eventsFor("nobody")).isEmpty();
    }

    @Test
    @DisplayName("current state is derived by replaying, not stored")
    void replayProducesCurrentState() {
        AccountBalance balance = BalanceProjection.replay(store.eventsFor("acc-1")).orElseThrow();

        assertThat(balance.owner()).isEqualTo("Alice");
        assertThat(balance.balanceCents())
                .as("10000 deposited, 2500 withdrawn, 500 deposited")
                .isEqualTo(8_000);
        assertThat(balance.open()).isTrue();
    }

    @Test
    @DisplayName("an account with no events is absent, not zero")
    void absentIsNotZero() {
        assertThat(BalanceProjection.replay(store.eventsFor("nobody")))
                .as("conflating 'no such account' with 'balance of zero' is a real bug")
                .isEmpty();
    }

    @Test
    @DisplayName("closing preserves the balance - the fact is recorded, nothing is erased")
    void closingIsJustAnotherEvent() {
        store.append(new AccountEvent.Closed("acc-1", "customer request"));

        AccountBalance balance = BalanceProjection.replay(store.eventsFor("acc-1")).orElseThrow();

        assertThat(balance.open()).isFalse();
        assertThat(balance.balanceCents())
                .as("an append-only log does not erase; it records that something ended")
                .isEqualTo(8_000);
    }

    @Test
    @DisplayName("time travel: replay to any point and see exactly what was true then")
    void timeTravel() {
        List<AccountEvent> history = store.eventsFor("acc-1");

        assertThat(BalanceProjection.replayUpTo(history, 1).orElseThrow().balanceCents())
                .as("just opened").isZero();
        assertThat(BalanceProjection.replayUpTo(history, 2).orElseThrow().balanceCents())
                .as("after the first deposit").isEqualTo(10_000);
        assertThat(BalanceProjection.replayUpTo(history, 3).orElseThrow().balanceCents())
                .as("after the withdrawal").isEqualTo(7_500);
        assertThat(BalanceProjection.replayUpTo(history, 4).orElseThrow().balanceCents())
                .as("""
                        'What was this balance on Tuesday?' is answerable by construction. With
                        stored current state it is unanswerable, because the history is gone.""")
                .isEqualTo(8_000);
    }

    @Test
    @DisplayName("a new read model can be built from history nobody planned for")
    void newProjectionsFromOldEvents() {
        // Nobody stored 'total deposited' - but the events were kept, so it is recoverable.
        long totalDeposited = store.allEvents().stream()
                .filter(AccountEvent.Deposited.class::isInstance)
                .map(AccountEvent.Deposited.class::cast)
                .mapToLong(AccountEvent.Deposited::amountCents)
                .sum();

        assertThat(totalDeposited)
                .as("""
                        This question was never anticipated. Because the facts were kept rather
                        than only their summary, it can still be answered - which is the single
                        best argument for event sourcing.""")
                .isEqualTo(17_500);
    }

    @Test
    @DisplayName("projections are disposable: rebuilding gives an identical answer")
    void projectionsAreDerived() {
        AccountBalance first = BalanceProjection.replay(store.eventsFor("acc-2")).orElseThrow();
        AccountBalance rebuilt = BalanceProjection.replay(store.eventsFor("acc-2")).orElseThrow();

        assertThat(rebuilt).isEqualTo(first);
        assertThat(rebuilt.balanceCents()).isEqualTo(7_000);
    }
}
