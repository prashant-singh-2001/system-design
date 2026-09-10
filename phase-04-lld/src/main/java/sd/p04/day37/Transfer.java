package sd.p04.day37;

/** One payment that would settle part of the group's debts. */
public record Transfer(String from, String to, long amountCents) {
}
