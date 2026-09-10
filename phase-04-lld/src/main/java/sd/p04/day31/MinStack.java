package sd.p04.day31;

/**
 * TODO(day31): today's warm-up for the LLD method - not because the problem is hard, but
 * because running the FULL {@code docs/templates/lld-template.md} process on something small
 * is how you learn to run it fast. See the brief for the timed exercise; this class is just the
 * "Build" step's output.
 *
 * <p>A stack that supports {@code push}, {@code pop}, {@code top} and {@code getMin} - the
 * current minimum - ALL in O(1). The trick: keep a SECOND, parallel stack that tracks what the
 * minimum was at each point in the first stack's history.
 *
 * <ul>
 *   <li>{@code push(x)}: push {@code x} onto the value stack. Push onto the min stack too -
 *       {@code x} if the min stack is empty or {@code x <= currentMin}, otherwise the CURRENT
 *       minimum again (a duplicate). Pushing a duplicate keeps both stacks the same height,
 *       which is what makes {@code pop} symmetric and trivial.</li>
 *   <li>{@code pop()}: pop both stacks; return what came off the value stack.</li>
 *   <li>{@code top()}: peek the value stack.</li>
 *   <li>{@code getMin()}: peek the min stack - it is always the minimum of everything CURRENTLY
 *       in the value stack, because every pop removes exactly the min-stack entry that was
 *       pushed alongside it.</li>
 * </ul>
 *
 * <p>Every operation on an empty stack throws {@code IllegalStateException}.
 */
public final class MinStack {

    public void push(int value) {
        throw new UnsupportedOperationException("TODO(day31): push onto both stacks");
    }

    public int pop() {
        throw new UnsupportedOperationException("TODO(day31): pop both stacks, return the value");
    }

    public int top() {
        throw new UnsupportedOperationException("TODO(day31): peek the value stack");
    }

    public int getMin() {
        throw new UnsupportedOperationException("TODO(day31): peek the min stack");
    }

    public boolean isEmpty() {
        throw new UnsupportedOperationException("TODO(day31): implement isEmpty");
    }
}
