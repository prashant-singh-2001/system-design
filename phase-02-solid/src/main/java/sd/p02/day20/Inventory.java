package sd.p02.day20;

/**
 * The behaviour contract for today's refactor - implemented once badly (as {@link
 * LegacyInventory}), and once again cleanly. Both must satisfy {@code InventoryContract}
 * exactly, quirks included: a characterization test does not ask whether behaviour is sensible,
 * only whether it is preserved.
 */
public interface Inventory {

    void restock(String sku, int quantity);

    boolean reserve(String orderId, String sku, int quantity);

    void release(String orderId);

    int available(String sku);
}
