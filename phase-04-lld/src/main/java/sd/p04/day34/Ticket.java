package sd.p04.day34;

import java.time.Instant;

/** Issued the moment a vehicle is successfully parked; consumed exactly once, on exit. */
public record Ticket(String id, String spotId, VehicleType vehicleType, Instant enteredAt) {
}
