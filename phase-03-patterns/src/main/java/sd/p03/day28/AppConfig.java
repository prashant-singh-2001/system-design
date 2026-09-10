package sd.p03.day28;

import java.util.Map;

/**
 * TODO(day28): a singleton done RIGHT, meaning lazily initialised and thread-safe without a
 * single {@code synchronized} keyword on the hot path.
 *
 * <p>The classic wrong answer is double-checked locking with a {@code volatile} field and two
 * null checks - it works, but it is easy to get subtly wrong and it is not what you would reach
 * for today. The idiom to use instead is the INITIALIZATION-ON-DEMAND HOLDER:
 *
 * <pre>
 *   public final class AppConfig {
 *       private AppConfig() { ... }
 *
 *       private static final class Holder {
 *           static final AppConfig INSTANCE = new AppConfig();
 *       }
 *
 *       public static AppConfig instance() {
 *           return Holder.INSTANCE;
 *       }
 *   }
 * </pre>
 *
 * <p>Why this is enough, with no {@code volatile} and no lock: the JVM already guarantees a
 * class is initialised at most once, and that initialisation happens-before any code that
 * triggers it can observe the result. {@code Holder} is not loaded until {@code instance()} is
 * first called, so construction is deferred (lazy) - and the class-loading guarantee gives you
 * thread safety for free, because it is the same mechanism that makes `static` initializers safe
 * in general.
 *
 * <p>Give {@code instance()} some config to hold - a {@code Map<String, String>} of settings is
 * enough - so a test can prove every caller, from every thread, sees the exact same instance.
 */
public final class AppConfig {

    public static AppConfig instance() {
        throw new UnsupportedOperationException("TODO(day28): return the holder's instance");
    }

    public Map<String, String> settings() {
        throw new UnsupportedOperationException("TODO(day28): return this instance's settings");
    }
}
