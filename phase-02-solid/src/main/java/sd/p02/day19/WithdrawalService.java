package sd.p02.day19;

/**
 * TODO(day19): the same rules as {@link LegacyWithdrawalService}, redesigned around
 * {@link Result} instead of exceptions.
 *
 * <p>Rules, unchanged from the legacy version:
 * <ul>
 *   <li>{@code amountCents <= 0} -&gt; {@code Err(new WithdrawalError.InvalidAmount(...))}</li>
 *   <li>a frozen account -&gt; {@code Err(new WithdrawalError.AccountFrozen(account.id()))}</li>
 *   <li>{@code amountCents > balanceCents} -&gt;
 *       {@code Err(new WithdrawalError.InsufficientFunds(shortfall))}, where
 *       {@code shortfall = amountCents - balanceCents}</li>
 *   <li>otherwise -&gt; {@code Ok} of a NEW {@link Account} with the balance reduced. {@link
 *       Account} is immutable, so this method never mutates its argument.</li>
 * </ul>
 *
 * <p>Behaviour is identical to the legacy version - this is a redesign of the failure channel,
 * not a rule change. What is different is what a caller is now forced to do: a {@code switch}
 * over the returned {@code Result} that omits a case will not compile, where a caller of the
 * legacy method could always forget a {@code catch} block and compile anyway.
 */
public final class WithdrawalService {

    public Result<Account, WithdrawalError> withdraw(Account account, long amountCents) {
        throw new UnsupportedOperationException("TODO(day19): return Ok or the matching Err");
    }
}
