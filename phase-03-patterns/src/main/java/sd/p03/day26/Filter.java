package sd.p03.day26;

/**
 * One link in the CHAIN OF RESPONSIBILITY. A filter decides, given the request and a handle on
 * "everything after me", whether to call {@code next} or short-circuit with its own response.
 *
 * <p>{@code next} is itself a {@link Handler} - "the rest of the chain, followed by the real
 * endpoint" is indistinguishable, from a filter's point of view, from "the real endpoint". That
 * is what lets {@link FilterChain} build the whole pipeline out of nothing but this one shape,
 * recursively.
 */
@FunctionalInterface
public interface Filter {

    Response apply(Request request, Handler next);
}
