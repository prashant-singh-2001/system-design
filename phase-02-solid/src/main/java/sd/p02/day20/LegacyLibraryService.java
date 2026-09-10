package sd.p02.day20;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * GIVEN - the legacy service. Every problem from days 11-19, in one file.
 *
 * <p>Find them before you read on:
 * <ul>
 *   <li><b>SRP</b> - inventory, lending, and fee calculation all live here.</li>
 *   <li><b>OCP</b> - the fee rate is hard-coded, so a new pricing rule means editing this.</li>
 *   <li><b>DIP</b> - it constructs its own storage, and reads the system clock directly.</li>
 *   <li><b>Testability</b> - because of {@code LocalDate.now()}, you cannot test a late fee
 *       without literally waiting three weeks. Time is a dependency like any other, and this
 *       class refuses to admit it.</li>
 * </ul>
 *
 * <p>Leave this class exactly as it is. It is the before-picture and the safety net: the
 * characterization tests pin its behaviour, and your refactored version has to match it.
 */
public final class LegacyLibraryService {

    private static final int LOAN_DAYS = 14;
    private static final long FEE_CENTS_PER_DAY = 50;

    private final Map<String, Integer> stock = new HashMap<>();
    private final Map<String, LocalDate> loans = new HashMap<>();

    public void addStock(String isbn, int copies) {
        if (copies <= 0) {
            throw new IllegalArgumentException("copies must be positive");
        }
        stock.merge(isbn, copies, Integer::sum);
    }

    public int availableCopies(String isbn) {
        return stock.getOrDefault(isbn, 0);
    }

    public LocalDate borrow(String memberId, String isbn) {
        if (availableCopies(isbn) <= 0) {
            throw new IllegalStateException("no copies available: " + isbn);
        }
        String key = memberId + "|" + isbn;
        if (loans.containsKey(key)) {
            throw new IllegalStateException("already on loan to this member: " + isbn);
        }
        stock.merge(isbn, -1, Integer::sum);
        LocalDate dueDate = LocalDate.now().plusDays(LOAN_DAYS);
        loans.put(key, dueDate);
        return dueDate;
    }

    public long returnBook(String memberId, String isbn) {
        String key = memberId + "|" + isbn;
        LocalDate dueDate = loans.remove(key);
        if (dueDate == null) {
            throw new IllegalStateException("not on loan to this member: " + isbn);
        }
        stock.merge(isbn, 1, Integer::sum);

        long daysLate = ChronoUnit.DAYS.between(dueDate, LocalDate.now());
        return daysLate <= 0 ? 0 : daysLate * FEE_CENTS_PER_DAY;
    }
}
