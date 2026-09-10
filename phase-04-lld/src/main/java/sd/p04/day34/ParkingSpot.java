package sd.p04.day34;

/**
 * A spot is immutable data - just an id and a size. Whether it is CURRENTLY occupied is tracked
 * separately, by {@link ParkingLot}, precisely so this class has no mutable state to race on.
 */
public record ParkingSpot(String id, SpotType type) {
}
