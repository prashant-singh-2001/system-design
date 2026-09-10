package sd.p02.day20;

import java.util.Optional;

/**
 * TODO(day20): the SECOND of {@link LegacyInventory}'s three responsibilities - which orderId
 * has claimed what, and nothing about stock levels.
 *
 * <p>Implement:
 * <ul>
 *   <li>{@code has(orderId)} - true once {@code record} has been called for it and it has not
 *       yet been {@code remove}d</li>
 *   <li>{@code record(orderId, sku, quantity)} - remember the reservation</li>
 *   <li>{@code remove(orderId)} - forget it, returning what was stored so the caller can restore
 *       stock; {@code Optional.empty()} if this {@code orderId} had no active reservation</li>
 * </ul>
 */
public final class ReservationTracker {

    public boolean has(String orderId) {
        throw new UnsupportedOperationException("TODO(day20): implement has");
    }

    public void record(String orderId, String sku, int quantity) {
        throw new UnsupportedOperationException("TODO(day20): implement record");
    }

    public Optional<Reservation> remove(String orderId) {
        throw new UnsupportedOperationException("TODO(day20): implement remove");
    }
}
