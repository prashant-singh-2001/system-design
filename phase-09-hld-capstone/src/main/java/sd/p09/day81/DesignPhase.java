package sd.p09.day81;

import java.time.Duration;

/**
 * The six phases of a 45-minute design round, with the time each deserves.
 *
 * <p>The single most common failure is spending 25 minutes on requirements and never drawing the
 * system. Encoding the budget makes it a plan rather than an intention.
 */
public enum DesignPhase {

    SCOPE(5, "Ask 3-4 sharp questions, then state the scope yourself and get agreement"),
    ESTIMATE(5, "QPS, storage, read:write ratio - and say which numbers will drive design"),
    API_AND_DATA(5, "Signatures and the partition key, with justification"),
    ARCHITECTURE(10, "Draw it, then trace one read and one write through the boxes"),
    DEEP_DIVE(10, "The genuinely hard part. This is most of your score"),
    BOTTLENECKS(10, "What breaks at 10x, what happens when each box dies");

    private final int minutes;
    private final String goal;

    DesignPhase(int minutes, String goal) {
        this.minutes = minutes;
        this.goal = goal;
    }

    public Duration budget() {
        return Duration.ofMinutes(minutes);
    }

    public String goal() {
        return goal;
    }
}
