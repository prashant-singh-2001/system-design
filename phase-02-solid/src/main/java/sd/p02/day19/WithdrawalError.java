package sd.p02.day19;

/**
 * Given: the three ways a withdrawal can fail, as DATA rather than as three different exception
 * classes. Each variant carries exactly the context a caller needs to react - a UI can show the
 * shortfall, decide whether to offer "unfreeze my account", or just display the reason - without
 * parsing an exception message or maintaining a parallel {@code catch} block per failure kind.
 */
public sealed interface WithdrawalError {

    record InvalidAmount(String reason) implements WithdrawalError {
    }

    record InsufficientFunds(long shortfallCents) implements WithdrawalError {
    }

    record AccountFrozen(String accountId) implements WithdrawalError {
    }
}
