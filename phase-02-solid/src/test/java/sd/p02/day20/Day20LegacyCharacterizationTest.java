package sd.p02.day20;

/**
 * GIVEN. Green from the start - this is step one of today's work, not a check on it. Run this
 * FIRST, confirm all twelve contracts hold against the class as it exists today, and only then
 * start splitting it apart.
 */
class Day20LegacyCharacterizationTest extends InventoryContract {

    @Override
    Inventory create() {
        return new LegacyInventory();
    }
}
