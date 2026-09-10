package sd.p02.day20;

/**
 * TODO(day20): the THIRD responsibility, finally isolated - the workflow that connects a
 * {@link StockLedger} and a {@link ReservationTracker}. This class owns the business rules;
 * it owns no state of its own.
 *
 * <p>{@code reserve(orderId, sku, quantity)}, in order:
 * <ol>
 *   <li>{@code quantity <= 0} -&gt; return {@code true}, touch nothing</li>
 *   <li>{@code tracker.has(orderId)} -&gt; return {@code true}, touch nothing (a retried request
 *       must not be charged twice)</li>
 *   <li>{@code quantity > ledger.available(sku)} -&gt; return {@code false}, touch nothing</li>
 *   <li>otherwise: {@code ledger.decrease}, {@code tracker.record}, return {@code true}</li>
 * </ol>
 *
 * <p>{@code release(orderId)}: if the tracker has no reservation for it, do nothing; otherwise
 * remove it and {@code ledger.increase} the SKU by the reserved quantity.
 *
 * <p>{@code restock} and {@code available} simply delegate to the ledger.
 *
 * <p>Run {@code Day20RefactoredInventoryTest} - it extends the exact same {@code
 * InventoryContract} that already passes against {@link LegacyInventory}. Every quirk from the
 * legacy class must still hold, because the contract was written against its OBSERVED
 * behaviour, not against what the behaviour ought to have been. That is what makes this a
 * refactor and not a rewrite.
 */
public final class DefaultInventory implements Inventory {

    public DefaultInventory(StockLedger ledger, ReservationTracker tracker) {
        throw new UnsupportedOperationException("TODO(day20): store the collaborators");
    }

    @Override
    public void restock(String sku, int quantity) {
        throw new UnsupportedOperationException("TODO(day20): delegate to the ledger");
    }

    @Override
    public boolean reserve(String orderId, String sku, int quantity) {
        throw new UnsupportedOperationException("TODO(day20): the four-step workflow above");
    }

    @Override
    public void release(String orderId) {
        throw new UnsupportedOperationException("TODO(day20): remove, then credit the ledger");
    }

    @Override
    public int available(String sku) {
        throw new UnsupportedOperationException("TODO(day20): delegate to the ledger");
    }
}
