package sd.p09.day82;

/** The three ways to mint a short code, and what each leaks or costs. */
public enum KeyGenerationStrategy {

    /**
     * Base-62 encode a monotonic counter. Shortest possible codes, zero collisions, and it leaks
     * your total volume: anyone can decode a code and watch the number climb. Also needs a
     * distributed counter, which is a coordination point.
     */
    COUNTER,

    /**
     * Hash the URL and truncate. Idempotent for free - the same URL always gets the same code -
     * but collisions are real and must be handled, and it still leaks nothing about volume.
     */
    HASH,

    /**
     * Random codes, checked for collision. Unguessable and uncorrelated, at the cost of a
     * uniqueness check per generation and longer codes to keep collisions rare.
     */
    RANDOM
}
