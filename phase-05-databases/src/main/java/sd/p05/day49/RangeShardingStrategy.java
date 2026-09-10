package sd.p05.day49;

import java.util.List;

/**
 * TODO(day49): the OTHER classic scheme - shards own contiguous, ordered ranges of the key
 * space instead of a hash-scattered assignment. {@code boundaries} is a SORTED list of
 * {@code shardCount - 1} keys marking where one shard's range ends and the next begins; a key
 * belongs to the first shard whose boundary is greater than or equal to it, or the LAST shard
 * if it is past every boundary.
 *
 * <p>Concretely, with boundaries {@code ["mz"]} (2 shards): any key that sorts at or before
 * {@code "mz"} lexicographically is shard 0; everything after is shard 1. Boundaries are full
 * cut-point VALUES, not single letters on purpose - a boundary of bare {@code "m"} would sort as
 * LESS than the longer word {@code "mango"} (a shorter string that is a prefix of a longer one
 * always sorts first), which would put "mango" in the wrong shard entirely.
 *
 * <p>Range sharding's whole appeal is that it keeps a QUERY over a range of keys - "everything
 * between A and C" - on as few shards as possible, which hash sharding can never do (a hash
 * scatters a contiguous range across every shard equally). The cost: a workload skewed toward
 * one part of the key space (today's date, an alphabetically common prefix) hits one shard hard,
 * for structural reasons hashing does not have.
 */
public final class RangeShardingStrategy implements ShardingStrategy {

    private final List<String> boundaries;

    public RangeShardingStrategy(List<String> boundaries) {
        this.boundaries = List.copyOf(boundaries);
    }

    @Override
    public int shardFor(String key, int shardCount) {
        throw new UnsupportedOperationException(
                "TODO(day49): find the first boundary >= key, or the last shard if none");
    }
}
