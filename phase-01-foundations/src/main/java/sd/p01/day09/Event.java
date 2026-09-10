package sd.p01.day09;

import java.io.Serial;
import java.io.Serializable;

/** The record we will push through three different wire formats. */
public record Event(long id, String type, long timestampMillis, String payload)
        implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static Event sample() {
        return new Event(9_007_199_254_740_991L, "user.created", 1_735_689_600_000L,
                "the quick brown fox jumps over the lazy dog");
    }
}
