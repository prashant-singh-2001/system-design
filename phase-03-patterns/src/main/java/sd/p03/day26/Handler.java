package sd.p03.day26;

/** The terminal step of the pipeline - the actual endpoint logic, once every filter has passed. */
@FunctionalInterface
public interface Handler {

    Response handle(Request request);
}
