package sd.p08.day72;

/**
 * TODO(day72): the safety valve that backoff alone does not give you.
 *
 * <p>Backoff spaces retries out. It does not bound how many there are. If a dependency is failing
 * for every caller, backoff still lets every caller retry - just politely - and the aggregate load
 * can still be several times normal at exactly the wrong moment.
 *
 * <p>A <b>retry budget</b> caps retries as a fraction of successful traffic. Allow, say, 10%: at
 * 1,000 successes you may spend 100 retries. When the dependency is healthy that is plenty. When
 * it is failing there are no successes, so the budget empties and retries stop almost entirely -
 * exactly when stopping is what helps.
 *
 * <p>That inversion is the whole idea, and it is the opposite of what naive retry logic does:
 * <b>the budget is most generous when you least need it and most restrictive when retrying would
 * hurt.</b> gRPC and Finagle both work this way.
 *
 * <p>Implement:
 * <ul>
 *   <li>{@code recordSuccess} - adds {@code ratio} to the available budget, capped at
 *       {@code maxTokens} so a long healthy period cannot bank an unlimited retry allowance.</li>
 *   <li>{@code tryConsume} - if at least one token is available, spend one and return true;
 *       otherwise return false and record a rejection.</li>
 * </ul>
 */
public final class RetryBudget {

    private final double ratio;
    private final double maxTokens;
    private double tokens;
    private long rejected;

    public RetryBudget(double ratio, double maxTokens) {
        if (ratio <= 0 || ratio > 1) {
            throw new IllegalArgumentException("ratio must be in (0, 1]");
        }
        if (maxTokens < 1) {
            throw new IllegalArgumentException("maxTokens must be at least 1");
        }
        this.ratio = ratio;
        this.maxTokens = maxTokens;
    }

    public void recordSuccess() {
        throw new UnsupportedOperationException("TODO(day72): earn budget from successes");
    }

    public boolean tryConsume() {
        throw new UnsupportedOperationException("TODO(day72): spend a token, or refuse");
    }

    public double availableTokens() {
        return tokens;
    }

    public long rejected() {
        return rejected;
    }
}
