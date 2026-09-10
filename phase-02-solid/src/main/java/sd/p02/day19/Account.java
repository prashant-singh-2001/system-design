package sd.p02.day19;

/** A value object: an immutable snapshot of an account's balance at a point in time. */
public record Account(String id, long balanceCents, boolean frozen) {
}
