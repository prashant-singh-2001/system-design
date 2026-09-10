package sd.p03.day23;

/** A handle returned by {@link EventBus#subscribe}, so a subscriber can walk away later. */
@FunctionalInterface
public interface Subscription {

    void unsubscribe();
}
