package sd.p04.day38;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Day38NotificationDispatcherTest {

    @Test
    @DisplayName("a healthy channel delivers on the first attempt")
    void healthyChannelDeliversImmediately() {
        EmailSender email = new EmailSender();
        NotificationDispatcher dispatcher = new NotificationDispatcher(
                Map.of("EMAIL", email), 3);

        dispatcher.dispatch(new Notification("user-1", "hello", "EMAIL"));

        assertThat(email.sentLog()).containsExactly("EMAIL to user-1: hello");
    }

    @Test
    @DisplayName("a flaky channel is retried until it succeeds, within the attempt budget")
    void retriesUntilSuccess() {
        FlakySender flaky = new FlakySender(2);
        NotificationDispatcher dispatcher = new NotificationDispatcher(
                Map.of("SMS", flaky), 3);

        dispatcher.dispatch(new Notification("user-1", "code: 1234", "SMS"));

        assertThat(flaky.callCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("exhausting the attempt budget rethrows the last failure")
    void exhaustsBudgetAndRethrows() {
        FlakySender alwaysFails = new FlakySender(10);
        NotificationDispatcher dispatcher = new NotificationDispatcher(
                Map.of("PUSH", alwaysFails), 3);

        assertThatThrownBy(() -> dispatcher.dispatch(new Notification("user-1", "hi", "PUSH")))
                .isInstanceOf(NotificationException.class);
        assertThat(alwaysFails.callCount())
                .as("exactly maxAttempts calls, no more")
                .isEqualTo(3);
    }

    @Test
    @DisplayName("an unregistered channel fails loudly, naming the channel")
    void unregisteredChannelThrows() {
        NotificationDispatcher dispatcher = new NotificationDispatcher(
                Map.of("EMAIL", new EmailSender()), 3);

        assertThatThrownBy(() -> dispatcher.dispatch(new Notification("user-1", "hi", "CARRIER_PIGEON")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CARRIER_PIGEON");
    }

    @Test
    @DisplayName("each channel has an independent attempt budget - one flaky channel does not affect another")
    void channelsAreIndependent() {
        EmailSender email = new EmailSender();
        FlakySender flakySms = new FlakySender(1);
        NotificationDispatcher dispatcher = new NotificationDispatcher(
                Map.of("EMAIL", email, "SMS", flakySms), 3);

        dispatcher.dispatch(new Notification("user-1", "hello", "EMAIL"));
        dispatcher.dispatch(new Notification("user-1", "code: 1234", "SMS"));

        assertThat(email.sentLog()).hasSize(1);
        assertThat(flakySms.callCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("EXTENSIBILITY: a brand-new channel works with zero changes to the dispatcher")
    void newChannelRequiresNoDispatcherChanges() {
        Map<String, NotificationSender> senders = new HashMap<>();
        senders.put("EMAIL", new EmailSender());
        java.util.List<String> webhookCalls = new java.util.ArrayList<>();
        senders.put("WEBHOOK", notification -> webhookCalls.add(notification.recipientId()));

        NotificationDispatcher dispatcher = new NotificationDispatcher(senders, 3);
        dispatcher.dispatch(new Notification("user-1", "ping", "WEBHOOK"));

        assertThat(webhookCalls).containsExactly("user-1");
    }

    @Test
    @DisplayName("a non-positive attempt budget is rejected at construction")
    void rejectsInvalidAttemptBudget() {
        assertThatThrownBy(() -> new NotificationDispatcher(Map.of(), 0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
