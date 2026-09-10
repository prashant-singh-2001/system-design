package sd.p06.day59;

/** What a cache is allowed to do with a stored response right now. */
public enum Freshness {

    /** Serve it straight from cache. No request leaves the machine. */
    FRESH,

    /** Ask the origin whether it changed - usually a conditional GET that returns 304. */
    MUST_REVALIDATE,

    /** Not usable: it was never storable, or it is stale and revalidation is mandatory. */
    NOT_USABLE
}
