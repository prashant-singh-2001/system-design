package sd.p05.day42;

/** What kind of scan Postgres chose for a query, read straight off its EXPLAIN output. */
public enum PlanKind {
    SEQ_SCAN, INDEX_SCAN, INDEX_ONLY_SCAN, OTHER
}
