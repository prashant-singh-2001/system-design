package sd.p07.day68;

/**
 * An event: a fact that happened, in the past tense, and is therefore immutable.
 *
 * <p>Naming matters here. {@code Deposited} is a fact; {@code SetBalance} is a command wearing an
 * event's clothes. A log of facts can be replayed into any shape you like; a log of commands has
 * already thrown the reasons away.
 */
public sealed interface AccountEvent {

    String accountId();

    record Opened(String accountId, String owner) implements AccountEvent {
    }

    record Deposited(String accountId, long amountCents) implements AccountEvent {
    }

    record Withdrawn(String accountId, long amountCents) implements AccountEvent {
    }

    record Closed(String accountId, String reason) implements AccountEvent {
    }
}
