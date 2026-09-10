package sd.p07.day68;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO(day68): the append-only log that IS the source of truth.
 *
 * <p>Event sourcing inverts the usual arrangement. Instead of storing current state and losing
 * how you got there, you store every event and derive current state by replaying them.
 *
 * <p>What that buys, and it is more than it first appears:
 * <ul>
 *   <li><b>A complete audit trail, by construction.</b> Not a log you remembered to write - the
 *       actual mechanism. In finance and healthcare this alone justifies the pattern.</li>
 *   <li><b>Time travel.</b> Replay to any point and see exactly what the state was.</li>
 *   <li><b>New read models for free.</b> A question nobody asked when the data was written can be
 *       answered by replaying history into a new projection. With stored current state, that data
 *       is simply gone.</li>
 *   <li><b>Debugging that actually works.</b> "How did this account reach a negative balance?" is
 *       answerable rather than a mystery.</li>
 * </ul>
 *
 * <p>The costs are real and worth stating: every query needs a projection, the event schema is
 * forever (you must be able to read events written years ago), replay gets slow without
 * snapshots, and GDPR-style deletion fights an append-only log by design.
 *
 * <p>Implement {@code append}, {@code eventsFor} (in order), and {@code allEvents}.
 */
public final class EventStore {

    private final List<AccountEvent> events = new ArrayList<>();

    public void append(AccountEvent event) {
        throw new UnsupportedOperationException("TODO(day68): append to the log");
    }

    public List<AccountEvent> eventsFor(String accountId) {
        throw new UnsupportedOperationException("TODO(day68): this account's events, in order");
    }

    public List<AccountEvent> allEvents() {
        throw new UnsupportedOperationException("TODO(day68): the whole log, in order");
    }

    public int size() {
        return events.size();
    }
}
