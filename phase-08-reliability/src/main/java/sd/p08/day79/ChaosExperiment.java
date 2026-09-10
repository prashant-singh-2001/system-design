package sd.p08.day79;

import java.util.function.Supplier;
import java.util.random.RandomGenerator;

/**
 * TODO(day79): chaos engineering - break it on purpose, while you are watching.
 *
 * <p>The argument is simple: your system already fails in production, at 3am, unobserved. A chaos
 * experiment moves that failure to a Tuesday afternoon, when the people who understand it are
 * awake and the blast radius is chosen rather than discovered.
 *
 * <p>The discipline that separates it from vandalism:
 * <ol>
 *   <li><b>State a hypothesis first.</b> "With one replica down, availability stays above our
 *       99.9% SLO." An experiment without a prediction is an outage with better PR.</li>
 *   <li><b>Bound the blast radius.</b> One instance, one region, 1% of traffic.</li>
 *   <li><b>Have an abort.</b> Know how to stop, and stop when the budget says so.</li>
 *   <li><b>Measure against the SLO</b>, not against a feeling.</li>
 * </ol>
 *
 * <p>Implement {@code run}: perform {@code requests} calls. With probability {@code failureRate},
 * inject a failure instead of calling {@code operation}. Otherwise call it, and count a failure if
 * it returns false. Return an {@link ExperimentResult}.
 */
public final class ChaosExperiment {

    private ChaosExperiment() {
    }

    public record ExperimentResult(int requests, int succeeded, int failed, double failureRate) {

        public double observedAvailability() {
            return requests == 0 ? 1.0 : (double) succeeded / requests;
        }

        /** TODO(day79): did availability stay at or above the target? */
        public boolean holdsHypothesis(Slo slo) {
            throw new UnsupportedOperationException("TODO(day79): compare against the SLO target");
        }
    }

    public static ExperimentResult run(int requests, double failureRate,
                                       Supplier<Boolean> operation, RandomGenerator random) {
        throw new UnsupportedOperationException("TODO(day79): inject failures, count outcomes");
    }
}
