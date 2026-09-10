package sd.p02.day11;

/**
 * A port. The domain says what it needs; it does not say how.
 *
 * <p>This one interface is what lets the tests run with no database at all. You will meet the
 * idea properly on Day 15 (DIP) and again on Day 17 (hexagonal architecture).
 */
public interface OrderRepository {

    void save(Order order, long totalCents);
}
