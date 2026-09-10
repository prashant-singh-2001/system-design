package sd.p03.day29;

/**
 * GIVEN - the outcome of processing one {@link Job}. Exactly one of {@code value} or
 * {@code error} is set - a failed job is data to inspect, not an exception that took down its
 * worker thread.
 */
public record Result(String jobId, Integer value, String error) {

    public static Result success(String jobId, int value) {
        return new Result(jobId, value, null);
    }

    public static Result failure(String jobId, String error) {
        return new Result(jobId, null, error);
    }

    public boolean isSuccess() {
        return error == null;
    }
}
