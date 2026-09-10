package sd.p04.day35;

import java.util.TreeSet;

/**
 * TODO(day35): a single elevator, simulated one FLOOR-STEP at a time rather than with real
 * threads and timers - deterministic, and fast to test, which matters more today than realism.
 *
 * <p>Hold pending requests in a {@code TreeSet<Integer>} so "which pending floor is nearest" is
 * always a cheap lookup rather than a scan.
 *
 * <p>{@code step()}, precisely, in this order:
 * <ol>
 *   <li>if {@code currentFloor} is itself a pending request, that request has just been
 *       reached - remove it, set {@code direction = IDLE}, and return. Arriving is its own
     *       step; it does not also move the elevator.</li>
 *   <li>if there are no pending requests at all, set {@code direction = IDLE} and return.</li>
 *   <li>otherwise, find whichever pending floor is NEAREST to {@code currentFloor} (by absolute
 *       distance), and move exactly one floor toward it - increment {@code currentFloor} and
 *       set {@code direction = UP} if the target is above, or the mirror image if below.</li>
 * </ol>
 *
 * <p>{@code requestFloor(floor)} just adds to the pending set - {@code TreeSet} already refuses
 * a duplicate for you.
 */
public final class Elevator {

    private final String id;
    private int currentFloor;
    private Direction direction = Direction.IDLE;
    private final TreeSet<Integer> pendingFloors = new TreeSet<>();

    public Elevator(String id, int startingFloor) {
        this.id = id;
        this.currentFloor = startingFloor;
    }

    public String id() {
        return id;
    }

    public int currentFloor() {
        return currentFloor;
    }

    public Direction direction() {
        return direction;
    }

    public void requestFloor(int floor) {
        throw new UnsupportedOperationException("TODO(day35): add to pendingFloors");
    }

    public boolean hasPendingRequests() {
        throw new UnsupportedOperationException("TODO(day35): implement hasPendingRequests");
    }

    public void step() {
        throw new UnsupportedOperationException(
                "TODO(day35): arrive if already at a pending floor; else move one floor toward the nearest");
    }
}
