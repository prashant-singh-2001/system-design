package sd.p09.day84;

public enum FanoutStrategy {

    /** Copy the post into every follower's inbox at write time. Fast reads, write amplification. */
    ON_WRITE,

    /** Leave the post in place and merge at read time. Cheap writes, expensive reads. */
    ON_READ
}
