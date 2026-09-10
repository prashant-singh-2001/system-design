package sd.p02.day20;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * These are the tests the legacy design made impossible. Each one is a concrete payoff
 * from a specific principle - which is the argument for the whole phase, in executable form.
 */
class Day20RefactorTest {

    private static final LocalDate BORROW_DAY = LocalDate.of(2026, 1, 15);

    private static MutableClock clockAt(LocalDate date) {
        return new MutableClock(date.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC);
    }

    @Test
    @DisplayName("DIP: an injected clock makes the due date exactly assertable")
    void dueDateIsDeterministic() {
        LibraryService library = new LibraryService(clockAt(BORROW_DAY), new StandardFeePolicy());
        library.addStock("isbn-1", 1);

        assertThat(library.borrow("member-1", "isbn-1"))
                .as("14 days after 2026-01-15")
                .isEqualTo(LocalDate.of(2026, 1, 29));
    }

    @Test
    @DisplayName("DIP: a late fee is now a one-line test instead of a three-week wait")
    void lateFeeIsTestable() {
        MutableClock clock = clockAt(BORROW_DAY);
        LibraryService library = new LibraryService(clock, new StandardFeePolicy());
        library.addStock("isbn-1", 1);
        library.borrow("member-1", "isbn-1");          // due 2026-01-29

        clock.advance(Duration.ofDays(20));             // now 2026-02-04, six days late

        assertThat(library.returnBook("member-1", "isbn-1"))
                .as("6 days late x 50 cents")
                .isEqualTo(300);
    }

    @Test
    @DisplayName("returning exactly on the due date is still free")
    void dueDateBoundary() {
        MutableClock clock = clockAt(BORROW_DAY);
        LibraryService library = new LibraryService(clock, new StandardFeePolicy());
        library.addStock("isbn-1", 1);
        library.borrow("member-1", "isbn-1");

        clock.advance(Duration.ofDays(14));             // exactly due

        assertThat(library.returnBook("member-1", "isbn-1")).isZero();
    }

    @Test
    @DisplayName("an early return never produces a negative fee")
    void earlyReturnIsNotACredit() {
        MutableClock clock = clockAt(BORROW_DAY);
        LibraryService library = new LibraryService(clock, new StandardFeePolicy());
        library.addStock("isbn-1", 1);
        library.borrow("member-1", "isbn-1");

        clock.advance(Duration.ofDays(3));

        assertThat(library.returnBook("member-1", "isbn-1")).isZero();
    }

    @Test
    @DisplayName("OCP: a new pricing rule needs no change to LibraryService")
    void feePolicyIsSwappable() {
        MutableClock clock = clockAt(BORROW_DAY);

        // A three-day grace period, then one pound a day. This policy did not exist when
        // LibraryService was written - and LibraryService does not know it exists now.
        FeePolicy generous = (FeePolicy) newGracePolicy();

        LibraryService library = new LibraryService(clock, generous);
        library.addStock("isbn-1", 1);
        library.borrow("member-1", "isbn-1");

        clock.advance(Duration.ofDays(19));             // five days late, two of them chargeable

        assertThat(library.returnBook("member-1", "isbn-1")).isEqualTo(200);
    }

    @Test
    @DisplayName("the standard policy prices exactly as the legacy code did")
    void standardPolicyPreservesBehaviour() {
        StandardFeePolicy policy = new StandardFeePolicy();

        assertThat(feeOf(policy, 0)).isZero();
        assertThat(feeOf(policy, -3)).as("returned early - never a credit").isZero();
        assertThat(feeOf(policy, 1)).isEqualTo(50);
        assertThat(feeOf(policy, 10)).isEqualTo(500);
    }

    // Reflective helpers, so this file compiles before FeePolicy has its method.
    // Once you have declared `long feeCents(long daysLate)`, these resolve to it.

    private static long feeOf(FeePolicy policy, long daysLate) {
        try {
            return (long) FeePolicy.class.getMethod("feeCents", long.class)
                    .invoke(policy, daysLate);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(
                    "FeePolicy must declare: long feeCents(long daysLate)", e);
        }
    }

    private static Object newGracePolicy() {
        try {
            return java.lang.reflect.Proxy.newProxyInstance(
                    FeePolicy.class.getClassLoader(),
                    new Class<?>[]{FeePolicy.class},
                    (proxy, method, args) -> {
                        long daysLate = (long) args[0];
                        long chargeable = daysLate - 3;
                        return chargeable <= 0 ? 0L : chargeable * 100;
                    });
        } catch (IllegalArgumentException e) {
            throw new AssertionError(
                    "FeePolicy must be an interface declaring exactly: long feeCents(long daysLate)", e);
        }
    }
}
