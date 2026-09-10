package sd.p04.day38;

/** Thrown by a {@link NotificationSender} when one attempt to deliver a notification fails. */
public final class NotificationException extends RuntimeException {

    public NotificationException(String message) {
        super(message);
    }
}
