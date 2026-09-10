package sd.p04.day35;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class Day35ElevatorTest {

    @Test
    @DisplayName("an elevator moves one floor per step toward a single requested floor")
    void movesOneFloorPerStep() {
        Elevator elevator = new Elevator("e1", 0);
        elevator.requestFloor(3);

        elevator.step();
        assertThat(elevator.currentFloor()).isEqualTo(1);
        assertThat(elevator.direction()).isEqualTo(Direction.UP);

        elevator.step();
        assertThat(elevator.currentFloor()).isEqualTo(2);

        elevator.step();
        assertThat(elevator.currentFloor()).isEqualTo(3);

        elevator.step();   // arrival step: removes the request, does not move
        assertThat(elevator.currentFloor()).isEqualTo(3);
        assertThat(elevator.hasPendingRequests()).isFalse();
        assertThat(elevator.direction()).isEqualTo(Direction.IDLE);
    }

    @Test
    @DisplayName("multiple pending floors are serviced nearest-first")
    void servicesNearestFloorFirst() {
        Elevator elevator = new Elevator("e1", 0);
        elevator.requestFloor(5);
        elevator.requestFloor(2);

        for (int i = 0; i < 2; i++) {
            elevator.step();
        }
        elevator.step();   // arrival at floor 2

        assertThat(elevator.currentFloor())
                .as("floor 2 is nearer than floor 5 from a starting floor of 0")
                .isEqualTo(2);
        assertThat(elevator.hasPendingRequests())
                .as("floor 5 is still pending")
                .isTrue();
    }

    @Test
    @DisplayName("an elevator with no requests is idle")
    void idleWithNoRequests() {
        Elevator elevator = new Elevator("e1", 4);

        elevator.step();

        assertThat(elevator.currentFloor()).isEqualTo(4);
        assertThat(elevator.direction()).isEqualTo(Direction.IDLE);
        assertThat(elevator.hasPendingRequests()).isFalse();
    }

    @Test
    @DisplayName("the nearest-elevator strategy picks the fleet member closest to the call")
    void nearestStrategyPicksClosestElevator() {
        Elevator far = new Elevator("far", 10);
        Elevator near = new Elevator("near", 2);

        Elevator chosen = new NearestElevatorDispatchStrategy()
                .selectElevator(List.of(far, near), 3, Direction.UP);

        assertThat(chosen).isSameAs(near);
    }

    @Test
    @DisplayName("a tie in distance is broken by list order")
    void tieIsBrokenByListOrder() {
        Elevator first = new Elevator("first", 0);
        Elevator second = new Elevator("second", 10);

        Elevator chosen = new NearestElevatorDispatchStrategy()
                .selectElevator(List.of(first, second), 5, Direction.UP);

        assertThat(chosen).isSameAs(first);
    }

    @Test
    @DisplayName("the system delegates a pickup request to the strategy-chosen elevator")
    void systemDelegatesToChosenElevator() {
        Elevator far = new Elevator("far", 10);
        Elevator near = new Elevator("near", 1);
        ElevatorSystem system = new ElevatorSystem(
                List.of(far, near), new NearestElevatorDispatchStrategy());

        system.requestPickup(2, Direction.UP);

        assertThat(near.hasPendingRequests()).isTrue();
        assertThat(far.hasPendingRequests()).isFalse();
    }

    @Test
    @DisplayName("stepAll advances every elevator in the fleet by one step")
    void stepAllAdvancesEveryElevator() {
        Elevator a = new Elevator("a", 0);
        Elevator b = new Elevator("b", 10);
        a.requestFloor(3);
        b.requestFloor(7);
        ElevatorSystem system = new ElevatorSystem(List.of(a, b), new NearestElevatorDispatchStrategy());

        system.stepAll();

        assertThat(a.currentFloor()).isEqualTo(1);
        assertThat(b.currentFloor()).isEqualTo(9);
        assertThat(system.elevators()).containsExactly(a, b);
    }
}
