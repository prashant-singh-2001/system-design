package sd.p04.day34;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * TODO(day34): the coordinator. Two things make this genuinely a "physical resource allocation"
 * problem rather than a data-structure exercise:
 *
 * <ul>
 *   <li><b>Fit, not just availability.</b> A spot is a candidate for a vehicle only if
 *       {@code spot.type().fits(vehicleType)}. Among the FITTING, free spots, prefer the
 *       SMALLEST one ({@code SMALL} before {@code MEDIUM} before {@code LARGE}) - parking a
 *       motorcycle in your one remaining {@code LARGE} spot is how a bus shows up five minutes
     *       later and finds nowhere to go.</li>
 *   <li><b>Concurrent allocation must never double-assign a spot.</b> Two threads calling
 *       {@code parkVehicle} at the same instant, competing for the last compatible spot, must
 *       never both walk away with a {@link Ticket} for it. A {@code ConcurrentHashMap} keyed by
 *       spot id, claimed with {@code putIfAbsent} (which atomically fails if another thread just
 *       won the same spot), is enough - you do not need a global lock over the whole lot.</li>
 * </ul>
 *
 * <p>{@code parkVehicle(vehicleId, type)}: find the smallest fitting, currently-unclaimed spot;
 * atomically claim it; return a new {@link Ticket}. If no compatible spot is free, return
 * {@code Optional.empty()} - a full lot is a normal outcome, not an error.
 *
 * <p>{@code unparkVehicle(ticketId, exitAt)}: look up the active ticket (throw
 * {@code IllegalArgumentException} if unknown or already closed), compute the cost via the
 * injected {@link PricingStrategy}, free the spot so a future vehicle can claim it, and return
 * the cost in cents.
 *
 * <p>{@code availableSpots(type)}: how many currently-unoccupied spots of that size exist.
 */
public final class ParkingLot {

    public ParkingLot(List<ParkingSpot> spots, PricingStrategy pricingStrategy) {
        throw new UnsupportedOperationException(
                "TODO(day34): index the spots, remember the pricing strategy");
    }

    public Optional<Ticket> parkVehicle(String vehicleId, VehicleType vehicleType) {
        throw new UnsupportedOperationException(
                "TODO(day34): find the smallest fitting free spot, claim it atomically, issue a ticket");
    }

    public long unparkVehicle(String ticketId, Instant exitAt) {
        throw new UnsupportedOperationException(
                "TODO(day34): validate the ticket, price the stay, free the spot, return the cost");
    }

    public int availableSpots(SpotType type) {
        throw new UnsupportedOperationException("TODO(day34): count unoccupied spots of this type");
    }

    // A helper you will likely want - not part of the graded contract, feel free to change it.
    private static String newTicketId() {
        return UUID.randomUUID().toString();
    }
}
