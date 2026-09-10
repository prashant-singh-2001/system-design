package sd.p01.day02;

/**
 * The inputs to a back-of-the-envelope estimate. Everything an interviewer gives you in
 * the first five minutes, in one place.
 *
 * @param dailyActiveUsers  DAU, not total registered users - the difference is often 10x
 * @param writesPerUserPerDay how many times an average user creates something
 * @param readsPerUserPerDay  how many times an average user consumes something
 * @param bytesPerWrite     size of one stored record, including its keys and metadata
 * @param retentionYears    how long you keep the data
 * @param replicationFactor copies of the data (3 is the usual default)
 */
public record SystemProfile(
        long dailyActiveUsers,
        double writesPerUserPerDay,
        double readsPerUserPerDay,
        int bytesPerWrite,
        int retentionYears,
        int replicationFactor) {

    public SystemProfile {
        if (dailyActiveUsers < 0 || bytesPerWrite < 0 || retentionYears < 0 || replicationFactor < 1) {
            throw new IllegalArgumentException("nonsensical profile: " + dailyActiveUsers
                    + "/" + bytesPerWrite + "/" + retentionYears + "/" + replicationFactor);
        }
    }

    /** The classic interview system: read-heavy, small records, huge fanout. */
    public static SystemProfile twitterLike() {
        return new SystemProfile(200_000_000L, 2, 100, 300, 5, 3);
    }
}
