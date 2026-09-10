package sd.p03.day24;

/**
 * TODO(day24): the third DECORATOR in today's stack, and the one that matters most for WHERE it
 * sits relative to the other two - see the reflect questions in the brief.
 *
 * <p>Call the delegate. If it throws a {@code RuntimeException}, try again, up to {@code
 * maxAttempts} TOTAL attempts (so {@code maxAttempts = 3} means at most three calls to the
 * delegate, not three retries after an initial try). If every attempt fails, rethrow the LAST
 * exception seen - swallowing it and returning null would turn a real failure into corrupted
 * data further up the stack.
 *
 * <p>{@code maxAttempts} must be at least 1; reject anything less in the constructor.
 */
public final class RetryDecorator implements SlowService {

    public RetryDecorator(SlowService delegate, int maxAttempts) {
        throw new UnsupportedOperationException("TODO(day24): validate maxAttempts >= 1, store both");
    }

    @Override
    public String fetch(String key) {
        throw new UnsupportedOperationException(
                "TODO(day24): retry up to maxAttempts times, then rethrow the last failure");
    }
}
