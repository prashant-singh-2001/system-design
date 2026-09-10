package sd.p04.day36;

/**
 * GIVEN, and the before-picture - "state" tracked as a handful of INDEPENDENT booleans instead
 * of one authoritative field.
 *
 * <p>Read {@code selectItem} and notice what it does NOT check: nothing stops
 * {@code hasCoins == false && isDispensing == true} from existing at the same time, because
 * these two fields are just fields - nothing links them, nothing enforces that exactly one
 * meaningful combination is ever true. Today that combination cannot actually arise because this
 * class is small and careful. The failure mode of this design is not "it is broken today" - it
 * is "the day a THIRD boolean gets added for a THIRD feature, in a hurry, by someone who has not
 * read every existing method", every combination becomes reachable, and some of them are
 * nonsense.
 *
 * <p>Multiply this by three real booleans instead of two, or by a team instead of one person,
 * and "scattered boolean flags" is not a style complaint - it is a specific, nameable source of
 * production bugs: an object that is unreachable-but-only-by-convention, not unreachable by
 * construction the way Day 18's aggregate and Day 27's State pattern both made "unreachable"
 * mean.
 */
public final class LegacyVendingMachine {

    private boolean hasCoins = false;
    private boolean isDispensing = false;
    private long balanceCents = 0;

    public void insertCoin(long amountCents) {
        balanceCents += amountCents;
        hasCoins = true;
    }

    public long selectItem(long priceCents) {
        if (!hasCoins) {
            throw new IllegalStateException("insert coins first");
        }
        if (balanceCents < priceCents) {
            throw new IllegalStateException("insufficient funds");
        }
        isDispensing = true;
        long change = balanceCents - priceCents;
        balanceCents = 0;
        hasCoins = false;
        isDispensing = false;
        return change;
    }
}
