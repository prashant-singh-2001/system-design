package sd.p08.day79;

import java.time.Duration;

/**
 * TODO(day79): the error budget - the idea that turns reliability from an argument into a number.
 *
 * <p>An SLO of 99.9% does not mean "try not to fail". It means <b>you are permitted 0.1% failures,
 * and that permission is a budget you may deliberately spend</b>.
 *
 * <p>That reframing is the whole point, and it dissolves the oldest argument in software:
 * <ul>
 *   <li><b>Budget remaining</b> -> ship. Take risks, deploy on Friday, run the migration. The
 *       budget exists to be used; an unspent budget means the target was set too low and you are
 *       over-investing in reliability instead of features.</li>
 *   <li><b>Budget exhausted</b> -> stop shipping features and fix reliability. Not because someone
 *       lost an argument, but because an agreed number says so.</li>
 * </ul>
 *
 * <p>Product and engineering stop negotiating from opinion. That is why Google's SRE book spends
 * so long on it: it is a management technique wearing an equation.
 *
 * <p>Implement:
 * <ul>
 *   <li>{@code allowedFailures} - {@code totalRequests x (1 - target)}</li>
 *   <li>{@code consumedFraction} - failures divided by the allowance. Above 1.0 means the SLO is
 *       already breached. Zero allowance (no traffic) consumes nothing.</li>
 *   <li>{@code remainingFraction} - {@code max(0, 1 - consumed)}</li>
 *   <li>{@code burnRate} - observed failure rate divided by the sustainable rate. A burn rate of
 *       1.0 exhausts the budget exactly at the end of the window; 10.0 exhausts it in a tenth of
 *       the time. <b>This is the number to alert on</b>, not raw error count - a 1% error rate is
 *       fine at a 99% SLO and an emergency at 99.99%.</li>
 *   <li>{@code timeToExhaustion} - how long the remaining budget lasts at the current burn rate.
 *       A burn rate at or below zero means never: return {@code Duration.ZERO} for an already
 *       exhausted budget and {@code null} for "not burning".</li>
 * </ul>
 */
public final class ErrorBudget {

    private ErrorBudget() {
    }

    public static double allowedFailures(long totalRequests, double target) {
        throw new UnsupportedOperationException("TODO(day79): total x (1 - target)");
    }

    public static double consumedFraction(long totalRequests, long failedRequests, double target) {
        throw new UnsupportedOperationException("TODO(day79): failures / allowance");
    }

    public static double remainingFraction(long totalRequests, long failedRequests, double target) {
        throw new UnsupportedOperationException("TODO(day79): what is left, floored at zero");
    }

    public static double burnRate(long totalRequests, long failedRequests, double target) {
        throw new UnsupportedOperationException("TODO(day79): observed rate / sustainable rate");
    }

    public static Duration timeToExhaustion(double remainingFraction, double burnRate,
                                            Duration window) {
        throw new UnsupportedOperationException("TODO(day79): how long the remainder lasts");
    }
}
