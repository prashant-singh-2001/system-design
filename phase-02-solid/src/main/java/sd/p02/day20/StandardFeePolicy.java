package sd.p02.day20;

/**
 * TODO(day20): the default policy, preserving the legacy behaviour exactly.
 *
 * <p>50 cents per day late. Zero if returned on or before the due date - never negative,
 * which is a rule the legacy code enforces with {@code daysLate <= 0} and which you must
 * keep.
 */
public final class StandardFeePolicy implements FeePolicy {
}
