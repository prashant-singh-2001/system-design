package sd.p07.day68;

/** A read model. Derived state - always rebuildable, never the source of truth. */
public record AccountBalance(String accountId, String owner, long balanceCents, boolean open) {
}
