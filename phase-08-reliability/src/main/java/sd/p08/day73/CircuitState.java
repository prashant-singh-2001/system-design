package sd.p08.day73;

/** The three states, and what each one is for. */
public enum CircuitState {

    /** Normal. Calls pass through and failures are counted. */
    CLOSED,

    /**
     * Tripped. Calls fail immediately without touching the dependency.
     *
     * <p>This is the whole point: when something is down, the kindest thing you can do is stop
     * calling it. Every request you send to a struggling service is one more it has to reject,
     * and one more thread of yours parked on a timeout.
     */
    OPEN,

    /**
     * Probing. A limited number of calls are allowed through to test recovery.
     *
     * <p>Without this state a breaker either stays open forever or slams the full load back onto
     * a service that has just come up - and knocks it over again.
     */
    HALF_OPEN
}
