package sd.p02.day20;

/**
 * TODO(day20): ONE of {@link LegacyInventory}'s three responsibilities, on its own - raw stock
 * numbers, and nothing about reservations, orders or the rules connecting them.
 *
 * <p>Rules, unchanged from the legacy class:
 * <ul>
 *   <li>a SKU that has never been restocked has {@code available == 0}</li>
 *   <li>{@code restock} adds {@code quantity} to the current level, but the result never goes
 *       below zero - a negative {@code quantity} decrements, clamped at the floor</li>
 *   <li>{@code decrease} lowers a SKU's level by {@code quantity} (the caller has already
 *       checked there is enough; this class does not re-check)</li>
 *   <li>{@code increase} raises it back (used when a reservation is released)</li>
 * </ul>
 */
public final class StockLedger {

    public void restock(String sku, int quantity) {
        throw new UnsupportedOperationException("TODO(day20): add, clamped at zero");
    }

    public void decrease(String sku, int quantity) {
        throw new UnsupportedOperationException("TODO(day20): subtract quantity from the level");
    }

    public void increase(String sku, int quantity) {
        throw new UnsupportedOperationException("TODO(day20): add quantity back to the level");
    }

    public int available(String sku) {
        throw new UnsupportedOperationException("TODO(day20): unknown SKU -> 0");
    }
}
