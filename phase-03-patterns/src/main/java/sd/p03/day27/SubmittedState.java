package sd.p03.day27;

/**
 * TODO(day27): overrides {@code pay} (-&gt; {@link PaidState}) and {@code cancel} (-&gt;
 * {@link CancelledState}). {@code submit} is not overridden - an already-submitted order cannot
 * be submitted again, and the inherited default already says so.
 */
public final class SubmittedState extends OrderState {

    @Override
    public String name() {
        return "SUBMITTED";
    }

    @Override
    public OrderState pay(OrderLifecycle order) {
        throw new UnsupportedOperationException("TODO(day27): move to PAID");
    }

    @Override
    public OrderState cancel(OrderLifecycle order) {
        throw new UnsupportedOperationException("TODO(day27): move to CANCELLED");
    }
}
