package sd.p04.day37;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO(day37): three ways to divide one expense among participants, all of which must satisfy
 * the same invariant Day 16's {@code Money.allocate} taught you: the shares must sum to EXACTLY
 * the total, in minor units, with no rounding drift and no missing or extra cent.
 *
 * <ul>
 *   <li>{@code splitEqually(totalCents, participants)} - divide into
 *       {@code participants.size()} shares. Distribute the division's remainder one cent at a
 *       time to the FIRST participants in list order, so five people splitting 101 cents get
 *       {@code 21, 20, 20, 20, 20} - not {@code 20} each with a cent vanishing into rounding.</li>
 *   <li>{@code splitExactly(exactAmountsCents, totalCents)} - the caller states each person's
 *       share directly. Validate the amounts sum to EXACTLY {@code totalCents}; if they do not,
 *       throw {@code IllegalArgumentException} - a typo here is a real debt calculated wrong,
 *       not a rounding nicety to shrug off.</li>
 *   <li>{@code splitByPercentage(percentages, totalCents)} - validate the percentages sum to
 *       100.0 within a small epsilon (floating point), then compute each share and distribute
 *       the ROUNDING remainder exactly the way {@code splitEqually} distributes its remainder -
 *       to the first participants in map iteration order - so percentages-of-a-total sums to
 *       the total exactly, every time.</li>
 * </ul>
 *
 * <p>Use a {@link LinkedHashMap}-backed return so iteration order is predictable and testable.
 */
public final class ExpenseSplitter {

    public Map<String, Long> splitEqually(long totalCents, List<String> participants) {
        throw new UnsupportedOperationException(
                "TODO(day37): divide, then hand the remainder to the first participants");
    }

    public Map<String, Long> splitExactly(Map<String, Long> exactAmountsCents, long totalCents) {
        throw new UnsupportedOperationException(
                "TODO(day37): validate the amounts sum to totalCents exactly, then return them");
    }

    public Map<String, Long> splitByPercentage(Map<String, Double> percentages, long totalCents) {
        throw new UnsupportedOperationException(
                "TODO(day37): validate percentages sum to ~100, compute shares, fix up rounding");
    }
}
