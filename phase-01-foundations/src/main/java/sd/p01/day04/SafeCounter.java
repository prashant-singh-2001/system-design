package sd.p01.day04;

/**
 * TODO(day04): make this correct under concurrent increments.
 *
 * <p>Any of these work, and you should be able to say what each costs:
 * <ul>
 *   <li>{@code synchronized} on both methods - mutual exclusion plus a memory barrier</li>
 *   <li>{@code AtomicLong} - a compare-and-swap loop, no kernel involvement</li>
 *   <li>a {@code ReentrantLock} - same guarantees as synchronized, more control</li>
 * </ul>
 *
 * <p>Whichever you pick, note that making {@code count} merely {@code volatile} is NOT
 * enough. Volatile gives visibility, not atomicity, and {@code count++} needs atomicity.
 * Being able to explain that distinction is most of what this day is for.
 */
public final class SafeCounter implements Counter {

    private long count;

    @Override
    public void increment() {
        throw new UnsupportedOperationException("TODO(day04): make increment() thread-safe");
    }

    @Override
    public long value() {
        throw new UnsupportedOperationException("TODO(day04): make value() see other threads' writes");
    }
}
