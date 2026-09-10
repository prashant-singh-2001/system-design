package sd.p03.day23;

/**
 * TODO(day23): the seed of pub/sub - everything Kafka does in Phase 7 at datacenter scale, this
 * does in one process with a {@code Map} and no network.
 *
 * <p>{@code subscribe(eventType, subscriber)} registers interest in exactly one event class and
 * returns a {@link Subscription} whose {@code unsubscribe()} removes it again. {@code
 * publish(event)} notifies every current subscriber registered for {@code event.getClass()} -
 * not superclasses, not interfaces the event implements beyond {@link Event} itself, its exact
 * runtime type.
 *
 * <p>The one rule that makes this a real message bus and not just a fancy list of callbacks:
 * <b>a subscriber that throws must not stop delivery to the subscribers after it.</b> One
 * broken consumer bringing down every other consumer's notifications is precisely the failure
 * mode a message bus exists to prevent - catch it, and keep going. (What you do with the
 * exception once caught - log it, count it, feed it to a dead-letter path - is a real design
 * question; for today, catching and continuing is enough.)
 *
 * <p>Publishing to a type with zero subscribers is not an error. It is simply nobody being home.
 */
public final class EventBus {

    public <T extends Event> Subscription subscribe(Class<T> eventType, Subscriber<T> subscriber) {
        throw new UnsupportedOperationException("TODO(day23): register, return an unsubscribe handle");
    }

    public void publish(Event event) {
        throw new UnsupportedOperationException(
                "TODO(day23): notify every subscriber for event.getClass(), isolating failures");
    }
}
