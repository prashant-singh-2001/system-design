package sd.p09.day81;

import java.time.Duration;
import java.util.Optional;

/**
 * TODO(day81): the 45-minute framework, as something you can actually rehearse against.
 *
 * <p>Interviewers are not assessing whether you know what a load balancer is. They are assessing
 * whether you can take an ambiguous problem, impose structure on it, make decisions under time
 * pressure, and defend them. Structure is most of the score, and it is the part you can practise.
 *
 * <p>Implement:
 * <ul>
 *   <li>{@code totalBudget} - the phases must add up to exactly 45 minutes. If they do not, the
 *       plan is a wish.</li>
 *   <li>{@code phaseAt} - which phase you should be in at a given elapsed time. Anything at or
 *       past 45 minutes is finished, so return empty.</li>
 *   <li>{@code startOf} - when each phase begins.</li>
 *   <li>{@code isBehind} - true when you are still in an EARLIER phase than the clock says you
 *       should be. This is the one that matters: the failure mode is not running out of ideas, it
 *       is running out of time in section one.</li>
 * </ul>
 *
 * <p>Say the clock out loud in a real round. "I have used ten minutes, let me move to the
 * architecture" is a strong signal on its own - it tells the interviewer you are managing the
 * session rather than being carried by it.
 */
public final class DesignClock {

    private DesignClock() {
    }

    public static Duration totalBudget() {
        throw new UnsupportedOperationException("TODO(day81): sum the phase budgets");
    }

    public static Optional<DesignPhase> phaseAt(Duration elapsed) {
        throw new UnsupportedOperationException("TODO(day81): which phase should you be in?");
    }

    public static Duration startOf(DesignPhase phase) {
        throw new UnsupportedOperationException("TODO(day81): cumulative budget before this phase");
    }

    public static boolean isBehind(DesignPhase currentPhase, Duration elapsed) {
        throw new UnsupportedOperationException("TODO(day81): are you lagging the clock?");
    }
}
