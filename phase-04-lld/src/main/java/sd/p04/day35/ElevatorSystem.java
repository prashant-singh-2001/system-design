package sd.p04.day35;

import java.util.List;

/**
 * TODO(day35): wires a fleet of {@link Elevator}s to a pluggable {@link DispatchStrategy} -
 * exactly the Strategy-selected-by-a-coordinator shape from Day 12 and Day 21, applied to
 * scheduling instead of pricing or hashing.
 *
 * <p>{@code requestPickup(floor, direction)}: ask the strategy which elevator should answer,
 * then call {@code requestFloor} on THAT elevator.
 *
 * <p>{@code stepAll()}: advance every elevator by exactly one {@code step()}.
 */
public final class ElevatorSystem {

    public ElevatorSystem(List<Elevator> elevators, DispatchStrategy dispatchStrategy) {
        throw new UnsupportedOperationException("TODO(day35): store the fleet and the strategy");
    }

    public void requestPickup(int floor, Direction direction) {
        throw new UnsupportedOperationException(
                "TODO(day35): select an elevator via the strategy, then request the floor on it");
    }

    public void stepAll() {
        throw new UnsupportedOperationException("TODO(day35): step every elevator once");
    }

    public List<Elevator> elevators() {
        throw new UnsupportedOperationException("TODO(day35): return the fleet");
    }
}
