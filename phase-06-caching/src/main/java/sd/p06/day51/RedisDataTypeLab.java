package sd.p06.day51;

import io.lettuce.core.api.sync.RedisCommands;

import java.util.List;
import java.util.Map;

/**
 * Day 51 - picking the right Redis data type.
 *
 * <p>Redis is not "a place to put strings". It is a set of server-side data structures, and
 * choosing the right one is the difference between one round trip and a thousand. The rule of
 * thumb: <b>push the work to where the data already is.</b> Fetching a list to Java, sorting it,
 * and taking the top ten is three orders of magnitude more expensive than asking Redis for the
 * top ten.
 *
 * <p>Each method below is a job that has one obviously correct type.
 */
public final class RedisDataTypeLab {

    private final RedisCommands<String, String> redis;

    public RedisDataTypeLab(RedisCommands<String, String> redis) {
        this.redis = redis;
    }

    /**
     * TODO(day51): store a user profile so that ONE field can be read or updated without
     * fetching and rewriting the whole object.
     *
     * <p>Use a HASH ({@code hset} / {@code hgetall}). Serialising the profile to a JSON string
     * would work, but then updating one field is read-modify-write over the network - and two
     * concurrent updates lose one of them. That is Day 4's lost update, at a distance.
     *
     * <p>Key: {@code user:<id>}.
     */
    public void saveProfile(String userId, Map<String, String> fields) {
        throw new UnsupportedOperationException("TODO(day51): use a HASH");
    }

    /** TODO(day51): read the whole profile back as a map. */
    public Map<String, String> loadProfile(String userId) {
        throw new UnsupportedOperationException("TODO(day51): hgetall");
    }

    /**
     * TODO(day51): update exactly one profile field, touching nothing else.
     *
     * <p>This is the payoff of choosing a HASH. One field, one round trip, no read first.
     */
    public void updateProfileField(String userId, String field, String value) {
        throw new UnsupportedOperationException("TODO(day51): hset a single field");
    }

    /**
     * TODO(day51): record a score and return the top N players, highest first.
     *
     * <p>Use a SORTED SET ({@code zadd} / {@code zrevrange}). Redis keeps it ordered on write,
     * so the read is O(log N + M) rather than "fetch everything and sort in Java".
     *
     * <p>Key: {@code leaderboard:<game>}.
     */
    public void recordScore(String game, String player, double score) {
        throw new UnsupportedOperationException("TODO(day51): zadd");
    }

    /** TODO(day51): top N players, highest score first. */
    public List<String> topPlayers(String game, int limit) {
        throw new UnsupportedOperationException("TODO(day51): zrevrange, 0 to limit-1");
    }

    /**
     * TODO(day51): count unique visitors for a day in fixed, tiny memory.
     *
     * <p>Use a HYPERLOGLOG ({@code pfadd} / {@code pfcount}). A SET would be exact but grows
     * with cardinality - a million visitors is a million members. HyperLogLog answers in ~12 KB
     * regardless, with about 0.81% error.
     *
     * <p>That trade is the whole lesson: for a dashboard number, "12,438,201 give or take 0.8%"
     * is worth an enormous amount of memory. For billing, it is not.
     *
     * <p>Key: {@code visitors:<day>}.
     */
    public void recordVisitor(String day, String visitorId) {
        throw new UnsupportedOperationException("TODO(day51): pfadd");
    }

    /** TODO(day51): approximate unique visitor count for that day. */
    public long uniqueVisitors(String day) {
        throw new UnsupportedOperationException("TODO(day51): pfcount");
    }

    /**
     * TODO(day51): a fixed-window request counter that expires by itself.
     *
     * <p>{@code incr} returns the new value, and it is atomic - no read-modify-write race. Set
     * the TTL only when the counter is first created (the returned value is 1), or you push the
     * window's expiry forward on every request and it never resets.
     *
     * <p>That subtlety is a real production bug. Getting it right here is the point.
     *
     * <p>Key: {@code ratelimit:<clientId>}.
     */
    public long incrementRequestCount(String clientId, long windowSeconds) {
        throw new UnsupportedOperationException("TODO(day51): incr, then expire only when it is 1");
    }
}
