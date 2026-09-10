package sd.p08.day71;

/**
 * TODO(day71): turn requirements into a consistency choice, deterministically.
 *
 * <p>Writing this as code forces the reasoning to be explicit. In a design discussion "we need
 * strong consistency" is usually an unexamined default; these four questions are what actually
 * decide it.
 *
 * <p>Apply the rules in this order - the strongest applicable requirement wins:
 * <ol>
 *   <li>{@code correctnessDependsOnLatestValue} - a decision is made from the read, and a stale
 *       answer produces a wrong outcome (a balance check, an inventory decrement, a lock).
 *       -> {@code LINEARIZABLE}</li>
 *   <li>{@code operationsAreCausallyRelated} - a reply must not appear before the message it
 *       answers. -> {@code CAUSAL}</li>
 *   <li>{@code userMustSeeOwnWrites} - the classic "I edited my profile and it reverted".
 *       -> {@code READ_YOUR_WRITES}</li>
 *   <li>{@code mustNotGoBackwards} - a feed that must not un-scroll. -> {@code MONOTONIC_READS}</li>
 *   <li>otherwise -> {@code EVENTUAL}</li>
 * </ol>
 *
 * <p>Notice how few systems genuinely need the top of that list. Most "we need strong consistency"
 * requirements turn out to be read-your-writes, which is enormously cheaper - a routing decision
 * rather than a consensus round trip.
 */
public final class ConsistencyChooser {

    private ConsistencyChooser() {
    }

    public record Requirements(boolean correctnessDependsOnLatestValue,
                               boolean operationsAreCausallyRelated,
                               boolean userMustSeeOwnWrites,
                               boolean mustNotGoBackwards) {
    }

    public static ConsistencyModel choose(Requirements requirements) {
        throw new UnsupportedOperationException("TODO(day71): strongest applicable rule wins");
    }

    /**
     * TODO(day71): PACELC, the more useful framing.
     *
     * <p>"If <b>P</b>artition, choose <b>A</b> or <b>C</b>; <b>E</b>lse, choose <b>L</b>atency or
     * <b>C</b>onsistency."
     *
     * <p>The "else" half is where you live 99.9% of the time, and it is the half CAP ignores
     * entirely. Even on a perfectly healthy network, waiting for replicas costs latency - so the
     * trade never goes away, it just stops being about availability.
     *
     * <p>Return exactly {@code "PC/EC"}, {@code "PA/EL"}, {@code "PC/EL"} or {@code "PA/EC"}.
     */
    public static String pacelc(PartitionStrategy duringPartition, boolean prefersLatencyOtherwise) {
        throw new UnsupportedOperationException("TODO(day71): classify the system");
    }
}
