package sd.p08.day71;

/**
 * The choice CAP actually forces on you, and the only choice it forces.
 *
 * <p>CAP is widely misquoted as "pick two of three". It says something much narrower:
 * <b>during a network partition</b> you must choose between availability and consistency.
 * When there is no partition you get both, which is almost all of the time. Describing a database
 * as "AP" or "CP" as a general property is a misuse of the theorem.
 */
public enum PartitionStrategy {

    /**
     * Consistency over availability. The minority side refuses writes rather than risk diverging.
     * The system stays correct and becomes partly unavailable.
     *
     * <p>The right choice when a wrong answer costs more than no answer: balances, inventory
     * counts, distributed locks, leader election.
     */
    CP,

    /**
     * Availability over consistency. Both sides accept writes and diverge, and you reconcile
     * afterwards. The system stays up and becomes temporarily wrong.
     *
     * <p>The right choice when no answer costs more than a slightly wrong one: shopping carts,
     * social feeds, view counts, session state.
     */
    AP
}
