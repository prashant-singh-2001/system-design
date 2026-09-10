package sd.p01.day03;

/**
 * Day 3 - Little's Law and the queueing knee.
 *
 * <p>Little's Law: {@code L = lambda x W}. Concurrency equals throughput times latency.
 * It holds for any stable system, with no assumptions about arrival distribution. It is the
 * reason you can answer "how many threads do I need?" without guessing.
 *
 * <p>The second half of this class is the part that changes how you think: an M/M/1 queue's
 * response time is {@code 1 / (mu - lambda)}. As arrival rate approaches service rate, the
 * denominator approaches zero and latency goes to infinity. Not gradually. Vertically.
 * That cliff is why you run servers at 60-70% utilisation and not 95%.
 */
public final class LittlesLaw {

    private LittlesLaw() {
    }

    /**
     * TODO(day03): L = lambda x W.
     *
     * <p>How many requests are in flight at once, given a throughput and a per-request
     * latency? This sizes your thread pool and your connection pool.
     */
    public static double concurrencyNeeded(double throughputPerSecond, double latencySeconds) {
        throw new UnsupportedOperationException("TODO(day03): implement concurrencyNeeded");
    }

    /**
     * TODO(day03): the same law rearranged - lambda = L / W.
     *
     * <p>Given a fixed pool of N workers and a known latency, this is the hard ceiling on
     * throughput. No amount of tuning gets you past it; you either cut latency or add workers.
     */
    public static double maxThroughput(int concurrency, double latencySeconds) {
        throw new UnsupportedOperationException("TODO(day03): implement maxThroughput");
    }

    /** TODO(day03): rho = lambda / mu, the fraction of time the server is busy. */
    public static double utilisation(double arrivalRate, double serviceRate) {
        throw new UnsupportedOperationException("TODO(day03): implement utilisation");
    }

    /**
     * TODO(day03): mean response time of an M/M/1 queue, which is {@code 1 / (mu - lambda)}.
     *
     * <p>This includes queueing time, not just service time. Throw
     * IllegalArgumentException when {@code arrivalRate >= serviceRate}: the queue is
     * unstable and grows without bound, so there is no finite answer to give.
     */
    public static double averageResponseTime(double arrivalRate, double serviceRate) {
        throw new UnsupportedOperationException("TODO(day03): implement averageResponseTime");
    }

    /**
     * TODO(day03): mean number waiting in the queue, which is {@code rho^2 / (1 - rho)}.
     *
     * <p>Same instability rule as above.
     */
    public static double averageQueueLength(double arrivalRate, double serviceRate) {
        throw new UnsupportedOperationException("TODO(day03): implement averageQueueLength");
    }

    // ---------------------------------------------------------------- given helper

    /** Prints the response-time curve so you can see the knee rather than take it on faith. */
    public static void printUtilisationCurve(double serviceRate) {
        System.out.printf("  service rate = %.0f/s%n", serviceRate);
        for (double rho : new double[]{0.1, 0.3, 0.5, 0.7, 0.8, 0.9, 0.95, 0.99}) {
            double arrival = rho * serviceRate;
            System.out.printf("    utilisation %4.0f%%  ->  response time %8.4f s%n",
                    rho * 100, averageResponseTime(arrival, serviceRate));
        }
    }
}
