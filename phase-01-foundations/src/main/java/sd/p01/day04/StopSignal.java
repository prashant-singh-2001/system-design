package sd.p01.day04;

/**
 * TODO(day04): the visibility half of the memory model.
 *
 * <p>One thread spins reading {@link #shouldStop()}. Another calls {@link #stop()}. With a
 * plain field, the JIT is entitled to hoist the read out of the loop - it can prove that
 * nothing INSIDE the loop writes the field, and without a happens-before edge it is not
 * required to consider other threads at all. The loop then never exits.
 *
 * <p>This is not theoretical. It is one of the classic ways a shutdown hangs in production.
 *
 * <p>Fix it with one keyword.
 */
public final class StopSignal {

    private boolean stop;

    public void stop() {
        this.stop = true;
    }

    public boolean shouldStop() {
        return stop;
    }
}
