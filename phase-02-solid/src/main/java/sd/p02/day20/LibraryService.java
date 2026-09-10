package sd.p02.day20;

import java.time.Clock;
import java.time.LocalDate;

/**
 * TODO(day20): the refactored service. This is the Phase 2 capstone - every principle at once.
 *
 * <p>Constructor: {@code LibraryService(Clock clock, FeePolicy feePolicy)}.
 *
 * <p>Keep the public behaviour identical - same methods, same exceptions, same messages. The
 * characterization tests hold you to it. That constraint is the definition of refactoring:
 * change the structure, never the behaviour. Anything else is a rewrite, and a rewrite needs
 * a different conversation with your team.
 *
 * <p>What each principle buys you here:
 * <ul>
 *   <li><b>DIP</b> - the injected {@link Clock} turns "wait three weeks" into "advance a
 *       variable". Use {@code LocalDate.now(clock)}, never {@code LocalDate.now()}.</li>
 *   <li><b>OCP</b> - {@link FeePolicy} means a new pricing rule is a new class, not an edit.</li>
 *   <li><b>SRP</b> - the fee arithmetic no longer lives in the lending workflow.</li>
 *   <li><b>ISP/LSP</b> - the policy interface is one method, and every implementation of it
 *       obeys the same contract: never negative, zero when not late.</li>
 * </ul>
 *
 * <p>If you have time left, go further and extract inventory into its own type. If you do not,
 * stop - the clock and the fee policy are the two changes that matter most, and finishing two
 * well beats starting four.
 */
public final class LibraryService {

    public LibraryService(Clock clock, FeePolicy feePolicy) {
        throw new UnsupportedOperationException("TODO(day20): inject the clock and the policy");
    }

    public void addStock(String isbn, int copies) {
        throw new UnsupportedOperationException("TODO(day20)");
    }

    public int availableCopies(String isbn) {
        throw new UnsupportedOperationException("TODO(day20)");
    }

    public LocalDate borrow(String memberId, String isbn) {
        throw new UnsupportedOperationException("TODO(day20)");
    }

    public long returnBook(String memberId, String isbn) {
        throw new UnsupportedOperationException("TODO(day20)");
    }
}
