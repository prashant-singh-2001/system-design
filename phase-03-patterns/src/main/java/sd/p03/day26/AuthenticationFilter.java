package sd.p03.day26;

import java.util.Set;

/**
 * TODO(day26): the first COMMAND in the chain - each call to {@code apply} packages up "run the
 * rest of the pipeline with this request" as a single object ({@code next}) that this filter can
 * choose to invoke, invoke and inspect, or never invoke at all.
 *
 * <p>If {@code request.apiKey()} is not in {@code validApiKeys}, return
 * {@code new Response(401, "unauthorized")} immediately - do not call {@code next}. Otherwise,
 * call {@code next.handle(request)} and return whatever it returns.
 */
public final class AuthenticationFilter implements Filter {

    public AuthenticationFilter(Set<String> validApiKeys) {
        throw new UnsupportedOperationException("TODO(day26): store the valid key set");
    }

    @Override
    public Response apply(Request request, Handler next) {
        throw new UnsupportedOperationException(
                "TODO(day26): 401 on a bad key, otherwise delegate to next");
    }
}
