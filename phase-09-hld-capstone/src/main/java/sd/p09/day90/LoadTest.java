package sd.p09.day90;

import sd.p09.capstone.adapter.CachingLinkRepository;
import sd.p09.capstone.domain.LinkService;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.random.RandomGenerator;

/**
 * TODO(day90): measure the thing you built, and report it the way an SRE would read it.
 *
 * <p>Everything about this method is a decision you have already argued through:
 *
 * <ul>
 *   <li><b>A Zipfian workload</b> (Day 60), because a uniform key distribution makes any cache look
 *       useless and real traffic is never uniform. Choosing a representative workload is most of
 *       the work in benchmarking, and the most common place a benchmark quietly lies.</li>
 *   <li><b>Percentiles, not averages</b> (Day 60 again). The mean describes nobody's experience,
 *       and once you have averaged you can never recover the tail.</li>
 *   <li><b>Availability against an SLO</b> (Day 79), because "it felt fast" is not a result.</li>
 * </ul>
 *
 * <p>Implement {@code run}: issue {@code requests} resolves against a Zipfian selection of the
 * supplied codes, timing each one. Count a success when the resolve returns a value, a failure
 * when it is empty or throws - a degraded response is still a failure from the user's point of
 * view, and pretending otherwise is how dashboards lie.
 *
 * <p>Return p50 and p99 by nearest rank, plus the cache hit ratio.
 */
public final class LoadTest {

    private LoadTest() {
    }

    public static LoadTestResult run(LinkService service, CachingLinkRepository cache,
                                     List<String> codes, int requests, RandomGenerator random) {
        throw new UnsupportedOperationException("TODO(day90): skewed workload, timed, percentiles");
    }

    // ---------------------------------------------------------------- given

    /** Small indexes far more likely than large ones - real traffic, roughly. */
    static String zipfianPick(List<String> codes, RandomGenerator random) {
        double uniform = random.nextDouble();
        int index = (int) (uniform * uniform * codes.size());
        return codes.get(Math.min(index, codes.size() - 1));
    }

    /** Nearest-rank percentile over a mutable list of samples. */
    static Duration percentile(List<Duration> samples, double p) {
        if (samples.isEmpty()) {
            return Duration.ZERO;
        }
        List<Duration> sorted = new ArrayList<>(samples);
        Collections.sort(sorted);
        int index = (int) Math.ceil(p / 100.0 * sorted.size()) - 1;
        return sorted.get(Math.max(0, Math.min(index, sorted.size() - 1)));
    }
}
