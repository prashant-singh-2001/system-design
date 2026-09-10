package sd.p04.day38;

/**
 * {@code channel} is a plain {@code String}, not an enum. That is deliberate: an enum is closed
 * - adding a channel means editing the enum, which means editing every switch over it. A string
 * key into a registry lets a brand-new channel be added by registering one more entry, with zero
 * changes to anything already written - the same "open for extension" property Day 12's carrier
 * registry and Day 21's hash-function factory both had.
 */
public record Notification(String recipientId, String message, String channel) {
}
