package sd.p02.day20;

/**
 * TODO(day20): the same twelve contracts, now run against your refactored {@link
 * DefaultInventory}. Wire it to a fresh {@link StockLedger} and {@link ReservationTracker} per
 * test - once this class is green, the refactor is done and behaviour-preserving, by
 * construction rather than by inspection.
 */
class Day20RefactoredInventoryTest extends InventoryContract {

    @Override
    Inventory create() {
        throw new UnsupportedOperationException(
                "TODO(day20): return new DefaultInventory(new StockLedger(), new ReservationTracker())");
    }
}
