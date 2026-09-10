package sd.p04.day35;

import java.util.List;

/**
 * TODO(day35): the simplest defensible dispatch rule - the elevator with the smallest
 * {@code abs(currentFloor - requestedFloor)}. Break ties by list order (the first elevator in
 * the list wins a tie).
 *
 * <p>Deliberately ignoring each elevator's current DIRECTION is a simplification, named as such
 * on purpose - a real dispatcher would also prefer an elevator already travelling toward the
 * request in the right direction over one that would have to reverse. See the brief's Stretch
 * section.
 */
public final class NearestElevatorDispatchStrategy implements DispatchStrategy {

    @Override
    public Elevator selectElevator(List<Elevator> elevators, int requestedFloor,
                                    Direction requestedDirection) {
        throw new UnsupportedOperationException(
                "TODO(day35): return the elevator with minimum distance to requestedFloor");
    }
}
