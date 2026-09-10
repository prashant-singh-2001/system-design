package sd.p02.day20;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * GIVEN, and already green. These are CHARACTERIZATION tests: they describe what the legacy
 * code does today, correct or not, so that a refactor cannot silently change it.
 *
 * <p>Each test runs the same scenario against both implementations and asserts they agree.
 * That is the safety net. Write these first when you inherit code you do not understand -
 * you cannot refactor safely without them, and they are usually faster to write than the
 * reading you would otherwise do.
 */
class Day20CharacterizationTest {

    private static final Clock FIXED =
            Clock.fixed(LocalDate.of(2026, 1, 15).atStartOfDay(ZoneOffset.UTC).toInstant(),
                    ZoneOffset.UTC);

    private LibraryService refactored() {
        return new LibraryService(FIXED, new StandardFeePolicy());
    }

    @Test
    @DisplayName("borrowing reduces the available copies")
    void borrowReducesStock() {
        LegacyLibraryService legacy = new LegacyLibraryService();
        legacy.addStock("isbn-1", 2);
        legacy.borrow("member-1", "isbn-1");

        LibraryService modern = refactored();
        modern.addStock("isbn-1", 2);
        modern.borrow("member-1", "isbn-1");

        assertThat(modern.availableCopies("isbn-1"))
                .isEqualTo(legacy.availableCopies("isbn-1"))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("returning restores the copy")
    void returnRestoresStock() {
        LibraryService modern = refactored();
        modern.addStock("isbn-1", 1);
        modern.borrow("member-1", "isbn-1");
        modern.returnBook("member-1", "isbn-1");

        assertThat(modern.availableCopies("isbn-1")).isEqualTo(1);
    }

    @Test
    @DisplayName("an on-time return costs nothing")
    void onTimeReturnIsFree() {
        LibraryService modern = refactored();
        modern.addStock("isbn-1", 1);
        modern.borrow("member-1", "isbn-1");

        assertThat(modern.returnBook("member-1", "isbn-1")).isZero();
    }

    @Test
    @DisplayName("borrowing with no copies left fails the same way")
    void noCopiesAvailable() {
        LibraryService modern = refactored();
        modern.addStock("isbn-1", 1);
        modern.borrow("member-1", "isbn-1");

        assertThatThrownBy(() -> modern.borrow("member-2", "isbn-1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("no copies available: isbn-1");
    }

    @Test
    @DisplayName("the same member cannot borrow the same title twice")
    void noDoubleBorrow() {
        LibraryService modern = refactored();
        modern.addStock("isbn-1", 5);
        modern.borrow("member-1", "isbn-1");

        assertThatThrownBy(() -> modern.borrow("member-1", "isbn-1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("already on loan to this member: isbn-1");
    }

    @Test
    @DisplayName("returning something you never borrowed fails the same way")
    void returnWithoutLoan() {
        assertThatThrownBy(() -> refactored().returnBook("member-1", "isbn-1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("not on loan to this member: isbn-1");
    }

    @Test
    @DisplayName("stock additions must be positive")
    void stockValidation() {
        assertThatThrownBy(() -> refactored().addStock("isbn-1", 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("copies must be positive");
    }
}
