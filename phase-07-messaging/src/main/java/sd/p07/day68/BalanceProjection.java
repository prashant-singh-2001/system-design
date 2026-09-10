package sd.p07.day68;

import java.util.List;
import java.util.Optional;

/**
 * TODO(day68): a projection - the CQRS read side.
 *
 * <p>CQRS separates the write model (commands producing events) from the read model (projections
 * answering queries). They can have different shapes, different stores and different scaling
 * characteristics, because a projection is <b>derived</b> data and can always be thrown away and
 * rebuilt.
 *
 * <p>Implement {@code replay}: fold a list of events into an {@link AccountBalance}.
 * <ul>
 *   <li>{@code Opened} - starts the account, zero balance, open</li>
 *   <li>{@code Deposited} - adds</li>
 *   <li>{@code Withdrawn} - subtracts</li>
 *   <li>{@code Closed} - marks it closed, balance unchanged</li>
 * </ul>
 *
 * <p>Return {@code Optional.empty()} for an account with no events - absent is not the same as
 * zero, and conflating them is a real bug.
 *
 * <p>Use a switch over the sealed interface. Because {@link AccountEvent} is sealed (Day 19), the
 * compiler knows every case and needs no default - and it will refuse to compile this method if
 * someone adds a new event type without handling it here. That is exhaustiveness checking doing
 * the work that a code-review checklist otherwise has to.
 *
 * <p>{@code replayUpTo} is the same fold over the first {@code n} events: time travel, in one
 * line.
 */
public final class BalanceProjection {

    private BalanceProjection() {
    }

    public static Optional<AccountBalance> replay(List<AccountEvent> events) {
        throw new UnsupportedOperationException("TODO(day68): fold events into current state");
    }

    public static Optional<AccountBalance> replayUpTo(List<AccountEvent> events, int eventCount) {
        throw new UnsupportedOperationException("TODO(day68): fold only the first n events");
    }
}
