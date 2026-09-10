package sd.p08.day73;

/** Thrown instead of calling a dependency the breaker believes is down. */
public final class CircuitBreakerOpenException extends RuntimeException {

    public CircuitBreakerOpenException(String message) {
        super(message);
    }
}
