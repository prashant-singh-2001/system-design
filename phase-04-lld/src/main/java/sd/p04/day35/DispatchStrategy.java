package sd.p04.day35;

import java.util.List;

/** The STRATEGY (Phase 3, Day 21): given a hall call, which elevator should answer it? */
@FunctionalInterface
public interface DispatchStrategy {

    Elevator selectElevator(List<Elevator> elevators, int requestedFloor, Direction requestedDirection);
}
