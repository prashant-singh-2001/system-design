package sd.p03.day23;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class Day23EventBusTest {

    @Test
    @DisplayName("a subscriber only hears about the event type it registered for")
    void routesByExactType() {
        EventBus bus = new EventBus();
        List<OrderPlaced> placedEvents = new ArrayList<>();
        List<OrderCancelled> cancelledEvents = new ArrayList<>();

        bus.subscribe(OrderPlaced.class, placedEvents::add);
        bus.subscribe(OrderCancelled.class, cancelledEvents::add);

        bus.publish(new OrderPlaced("ord-1", 5_000));

        assertThat(placedEvents).containsExactly(new OrderPlaced("ord-1", 5_000));
        assertThat(cancelledEvents).isEmpty();
    }

    @Test
    @DisplayName("every subscriber to a type is notified, in no particular guaranteed order")
    void notifiesEverySubscriber() {
        EventBus bus = new EventBus();
        List<String> notified = new ArrayList<>();

        bus.subscribe(OrderPlaced.class, e -> notified.add("first"));
        bus.subscribe(OrderPlaced.class, e -> notified.add("second"));

        bus.publish(new OrderPlaced("ord-1", 100));

        assertThat(notified).containsExactlyInAnyOrder("first", "second");
    }

    @Test
    @DisplayName("unsubscribing stops further delivery")
    void unsubscribeStopsDelivery() {
        EventBus bus = new EventBus();
        List<OrderPlaced> received = new ArrayList<>();

        Subscription subscription = bus.subscribe(OrderPlaced.class, received::add);
        bus.publish(new OrderPlaced("ord-1", 100));
        subscription.unsubscribe();
        bus.publish(new OrderPlaced("ord-2", 200));

        assertThat(received).containsExactly(new OrderPlaced("ord-1", 100));
    }

    @Test
    @DisplayName("one subscriber throwing does not stop the others from being notified")
    void aFailingSubscriberDoesNotBreakDelivery() {
        EventBus bus = new EventBus();
        List<OrderPlaced> received = new ArrayList<>();

        bus.subscribe(OrderPlaced.class, e -> {
            throw new RuntimeException("boom");
        });
        bus.subscribe(OrderPlaced.class, received::add);

        assertThatCode(() -> bus.publish(new OrderPlaced("ord-1", 100)))
                .as("publish itself must not propagate a subscriber's exception")
                .doesNotThrowAnyException();
        assertThat(received).containsExactly(new OrderPlaced("ord-1", 100));
    }

    @Test
    @DisplayName("publishing with no subscribers registered is a quiet no-op")
    void publishingWithNoSubscribersIsFine() {
        EventBus bus = new EventBus();

        assertThatCode(() -> bus.publish(new OrderCancelled("ord-1")))
                .doesNotThrowAnyException();
    }
}
