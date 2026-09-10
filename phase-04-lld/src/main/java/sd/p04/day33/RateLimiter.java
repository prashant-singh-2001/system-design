package sd.p04.day33;

/** One decision, made fresh on every call: is this request allowed right now? */
public interface RateLimiter {

    boolean tryAcquire();
}
