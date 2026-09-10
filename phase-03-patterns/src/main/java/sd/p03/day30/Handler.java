package sd.p03.day30;

/** GIVEN - a route's actual endpoint logic. */
@FunctionalInterface
public interface Handler {

    Response handle(Request request);
}
