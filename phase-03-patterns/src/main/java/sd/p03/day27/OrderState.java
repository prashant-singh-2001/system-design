package sd.p03.day27;

/**
 * GIVEN - the TEMPLATE METHOD half of today. Every transition is defined here, once, with a
 * single default behaviour: refuse, with a message built the same way every time. A concrete
 * state overrides only the transitions IT allows; every transition it does not override keeps
 * refusing automatically, with zero repeated boilerplate.
 *
 * <p>Compare this with Day 18's {@code Order}, which checked
 * {@code if (status != DRAFT) throw ...} by hand inside every single method. Here, "this
 * transition is illegal from this state" is not a check anyone writes - it is simply what
 * happens when a subclass does not override the method. The STATE pattern moves the `if` out of
 * the code and into the class hierarchy.
 */
public abstract class OrderState {

    public OrderState submit(OrderLifecycle order) {
        throw illegal("submit");
    }

    public OrderState pay(OrderLifecycle order) {
        throw illegal("pay");
    }

    public OrderState cancel(OrderLifecycle order) {
        throw illegal("cancel");
    }

    public abstract String name();

    protected final IllegalStateException illegal(String action) {
        return new IllegalStateException("cannot " + action + " from " + name());
    }

    @Override
    public String toString() {
        return name();
    }
}
