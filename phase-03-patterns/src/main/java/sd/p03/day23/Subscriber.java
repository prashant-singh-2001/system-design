package sd.p03.day23;

/** The OBSERVER role. One method, so any subscriber can be written as a lambda. */
@FunctionalInterface
public interface Subscriber<T extends Event> {

    void onEvent(T event);
}
