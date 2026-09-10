package sd.p06.day56;

import java.util.List;

/** GIVEN - the naive scheme, kept so you can measure exactly what consistent hashing saves. */
public final class ModuloSharding {

    private ModuloSharding() {
    }

    public static int nodeFor(String key, int nodeCount) {
        return Math.floorMod(Long.hashCode(ConsistentHashRing.hash(key)), nodeCount);
    }

    public static double keyMovementFraction(int beforeCount, int afterCount, List<String> keys) {
        long moved = keys.stream()
                .filter(key -> nodeFor(key, beforeCount) != nodeFor(key, afterCount))
                .count();
        return (double) moved / keys.size();
    }
}
