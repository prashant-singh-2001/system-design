package sd.p04.day38;

import java.util.ArrayList;
import java.util.List;

/** GIVEN - a stand-in for a real SMS provider. Always succeeds; records what it sent. */
public final class SmsSender implements NotificationSender {

    private final List<String> sentLog = new ArrayList<>();

    @Override
    public void send(Notification notification) {
        sentLog.add("SMS to " + notification.recipientId() + ": " + notification.message());
    }

    public List<String> sentLog() {
        return List.copyOf(sentLog);
    }
}
