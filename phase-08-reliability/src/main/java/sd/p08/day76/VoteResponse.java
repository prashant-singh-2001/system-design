package sd.p08.day76;

/**
 * @param term the responder's current term - a candidate that sees a HIGHER term here has been
 *             superseded and must stand down. That single rule is how Raft resolves split brain.
 */
public record VoteResponse(long term, boolean granted) {
}
