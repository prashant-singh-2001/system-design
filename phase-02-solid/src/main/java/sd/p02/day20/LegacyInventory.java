package sd.p02.day20;

import java.util.HashMap;
import java.util.Map;

/**
 * GIVEN, and deliberately entangled - the "before" picture for today's refactor.
 *
 * <p>Stock levels, reservation bookkeeping and the business rules that connect them all live in
 * one class with two raw maps. It works. It is also the shape that makes a simple question like
 * "what happens if I reserve quantity zero?" require reading the whole class to answer, because
 * there is nowhere smaller to look.
 *
 * <p>Three quirks live in here that a rewrite could easily lose if nobody wrote them down
 * first - which is the entire argument for characterization tests. None of them are documented
 * anywhere except in the code and in {@code InventoryContract}:
 *
 * <ol>
 *   <li>Reserving a non-positive quantity trivially succeeds and touches no stock.</li>
 *   <li>Reserving the same {@code orderId} again - even for a different SKU or quantity -
 *       succeeds immediately without moving stock a second time. A retried request must not be
 *       charged twice against inventory.</li>
 *   <li>Restocking a negative quantity decrements, but never below zero.</li>
 * </ol>
 *
 * <p>Today's job is NOT to fix or improve any of this. It is to pin all of it down with tests
 * BEFORE touching the code, then split it into focused collaborators that reproduce the exact
 * same behaviour. Do not read this class as an example to follow - read it as the reason
 * {@link StockLedger} and {@link ReservationTracker} exist.
 */
public final class LegacyInventory implements Inventory {

    private final Map<String, Integer> stock = new HashMap<>();
    private final Map<String, Reservation> reservations = new HashMap<>();

    @Override
    public void restock(String sku, int quantity) {
        int current = stock.getOrDefault(sku, 0);
        int updated = Math.max(0, current + quantity);
        stock.put(sku, updated);
    }

    @Override
    public boolean reserve(String orderId, String sku, int quantity) {
        if (quantity <= 0) {
            return true;
        }
        if (reservations.containsKey(orderId)) {
            return true;
        }
        int current = stock.getOrDefault(sku, 0);
        if (quantity > current) {
            return false;
        }
        stock.put(sku, current - quantity);
        reservations.put(orderId, new Reservation(sku, quantity));
        return true;
    }

    @Override
    public void release(String orderId) {
        Reservation reservation = reservations.remove(orderId);
        if (reservation == null) {
            return;
        }
        int current = stock.getOrDefault(reservation.sku(), 0);
        stock.put(reservation.sku(), current + reservation.quantity());
    }

    @Override
    public int available(String sku) {
        return stock.getOrDefault(sku, 0);
    }
}
