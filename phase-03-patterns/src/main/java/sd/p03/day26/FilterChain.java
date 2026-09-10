package sd.p03.day26;

import java.util.List;

/**
 * TODO(day26): compose a {@code List<Filter>} and a terminal {@link Handler} into ONE {@link
 * Handler} - the entire pipeline, indistinguishable from a plain endpoint to whoever calls it.
 *
 * <p>Build it from the back: the terminal handler is "the rest of the chain" for the LAST
 * filter; the result of wrapping the last filter around the terminal handler becomes "the rest
 * of the chain" for the second-to-last filter; and so on to the front. A loop counting down from
 * {@code filters.size() - 1} to {@code 0}, each iteration rebinding a local {@code Handler}
 * variable, builds this correctly - reason through why it has to go back-to-front before writing
 * it forwards and getting the order backwards.
 *
 * <p>{@code filters.get(0)} ends up as the OUTERMOST layer - the first thing a request meets -
 * which is why registration order in the list is also execution order.
 */
public final class FilterChain {

    private FilterChain() {
    }

    public static Handler build(List<Filter> filters, Handler terminal) {
        throw new UnsupportedOperationException(
                "TODO(day26): fold the filters around the terminal handler, back to front");
    }
}
