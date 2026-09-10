package sd.p03.day21;

/**
 * TODO(day21): given a {@link HashFunction} and a fixed number of partitions, decide which
 * partition a key belongs to.
 *
 * <p>{@code partitionFor(key) = Math.floorMod(hashFunction.hash(key), partitionCount)}.
 * {@link Math#floorMod} matters over the raw {@code %} operator here - {@code %} can return a
 * negative result for a negative dividend in Java, which would hand you a partition INDEX that
 * does not exist.
 *
 * <p>The constructor should reject {@code partitionCount < 1} - a store with zero partitions is
 * not a store.
 *
 * <p>Once this is green, try resizing from 4 partitions to 5 across the same set of keys (the
 * test does this for you) and look at how many keys land in a different partition. That number
 * is the entire motivation for Day 56.
 */
public final class Partitioner {

    public Partitioner(HashFunction hashFunction, int partitionCount) {
        throw new UnsupportedOperationException("TODO(day21): validate and store both");
    }

    public int partitionFor(String key) {
        throw new UnsupportedOperationException("TODO(day21): floorMod the hash by the count");
    }
}
