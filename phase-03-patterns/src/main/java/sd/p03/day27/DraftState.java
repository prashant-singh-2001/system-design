package sd.p03.day27;

/**
 * TODO(day27): the starting state. Overrides exactly two of the three transitions:
 *
 * <ul>
 *   <li>{@code submit} - if {@code order.hasLines()} is false, throw
 *       {@code new IllegalStateException("cannot submit an order with no lines")}; otherwise
 *       return a new {@link SubmittedState}.</li>
 *   <li>{@code cancel} - always allowed from DRAFT; return a new {@link CancelledState}.</li>
 * </ul>
 *
 * <p>{@code pay} is deliberately NOT overridden - the inherited {@link OrderState#pay} already
 * does exactly the right thing by doing nothing at all.
 */
public final class DraftState extends OrderState {

    @Override
    public String name() {
        return "DRAFT";
    }

    @Override
    public OrderState submit(OrderLifecycle order) {
        throw new UnsupportedOperationException("TODO(day27): require lines, then move to SUBMITTED");
    }

    @Override
    public OrderState cancel(OrderLifecycle order) {
        throw new UnsupportedOperationException("TODO(day27): move to CANCELLED");
    }
}
