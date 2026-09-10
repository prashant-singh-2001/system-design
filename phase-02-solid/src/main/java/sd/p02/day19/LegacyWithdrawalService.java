package sd.p02.day19;

/**
 * GIVEN, and the "before" picture. Same rules as {@link WithdrawalService}, expressed the way
 * most codebases actually express them: three different exception types, thrown from deep
 * inside a call stack, with no trace of them anywhere in the method signature.
 *
 * <p>Read {@code withdraw}'s signature alone. Nothing about it says failure is even possible,
 * let alone which of three shapes it might take. A caller finds out by reading the
 * implementation, by reading the docs if they exist, or by finding out in production.
 */
public final class LegacyWithdrawalService {

    public Account withdraw(Account account, long amountCents) {
        if (amountCents <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        if (account.frozen()) {
            throw new IllegalStateException("account " + account.id() + " is frozen");
        }
        if (amountCents > account.balanceCents()) {
            long shortfall = amountCents - account.balanceCents();
            throw new IllegalStateException("insufficient funds, short by " + shortfall);
        }
        return new Account(account.id(), account.balanceCents() - amountCents, account.frozen());
    }
}
