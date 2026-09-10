package sd.p03.day21;

/**
 * TODO(day21): the strategy that reaches for what is already on the object - {@link
 * String#hashCode()}.
 *
 * <p>Return it widened to a {@code long}. Do not mask off the sign bit - {@link
 * Partitioner#partitionFor} uses {@link Math#floorMod}, which already handles a negative
 * dividend correctly, and masking here would just be extra code that changes nothing observable.
 *
 * <p>The trade this strategy is making: it is free - no new code to maintain - but {@code
 * String.hashCode()}'s algorithm is a JLS-specified implementation detail of the JDK, not a
 * general-purpose hash designed for uniform distribution under adversarial input. Good enough
 * for a HashMap bucket; worth being suspicious of for sharding a hot key space.
 */
public final class JavaHashFunction implements HashFunction {

    @Override
    public long hash(String key) {
        throw new UnsupportedOperationException("TODO(day21): widen key.hashCode() to long");
    }
}
