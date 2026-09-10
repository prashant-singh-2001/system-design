package sd.p04.day38;

/** The one port every channel implements - email, SMS, push, or anything added later. */
@FunctionalInterface
public interface NotificationSender {

    void send(Notification notification);
}
