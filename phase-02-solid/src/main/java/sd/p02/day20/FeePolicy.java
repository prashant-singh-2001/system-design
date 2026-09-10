package sd.p02.day20;

/**
 * TODO(day20): the extension point for pricing (OCP, Day 12).
 *
 * <p>One method: {@code long feeCents(long daysLate)}. A single-method interface means a new
 * policy is a lambda, and the library never has to be edited to price a loan differently.
 */
public interface FeePolicy {
}
