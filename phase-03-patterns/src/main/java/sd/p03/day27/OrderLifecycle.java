package sd.p03.day27;

/**
 * TODO(day27): the CONTEXT the State pattern revolves around. It holds the current
 * {@link OrderState} and delegates every transition to it, replacing its own field with
 * whatever state comes back.
 *
 * <p>Start in a {@link DraftState}. {@code addLine()} just flips {@code hasLines} to
 * {@code true} - no quantities or prices today, this class exists to demonstrate the pattern,
 * not to re-litigate Day 18's aggregate.
 *
 * <p>{@code submit()}, {@code pay()} and {@code cancel()} each do the same two things: call the
 * matching method on {@code currentState}, passing {@code this}, then store whatever
 * {@link OrderState} came back as the new {@code currentState}. {@code status()} returns
 * {@code currentState.name()}.
 */
public final class OrderLifecycle {

    private OrderState currentState = new DraftState();
    private boolean hasLines = false;

    public void addLine() {
        throw new UnsupportedOperationException("TODO(day27): record that this order has a line");
    }

    public boolean hasLines() {
        throw new UnsupportedOperationException("TODO(day27): implement hasLines");
    }

    public String status() {
        throw new UnsupportedOperationException("TODO(day27): delegate to currentState.name()");
    }

    public void submit() {
        throw new UnsupportedOperationException(
                "TODO(day27): currentState = currentState.submit(this)");
    }

    public void pay() {
        throw new UnsupportedOperationException(
                "TODO(day27): currentState = currentState.pay(this)");
    }

    public void cancel() {
        throw new UnsupportedOperationException(
                "TODO(day27): currentState = currentState.cancel(this)");
    }
}
