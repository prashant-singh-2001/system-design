package sd.p03.day30;

/** GIVEN - the same CHAIN OF RESPONSIBILITY shape from Day 26, applied to every route. */
@FunctionalInterface
public interface Filter {

    Response apply(Request request, Handler next);
}
