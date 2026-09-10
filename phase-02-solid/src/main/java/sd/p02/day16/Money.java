package sd.p02.day16;

import java.util.List;

/**
 * TODO(day16): a value object for money.
 *
 * <p>Three rules that this type exists to enforce, and that a bare {@code long} cannot:
 *
 * <ol>
 *   <li><b>Never use floating point for money.</b> {@code 0.1 + 0.2 != 0.3} in binary floating
 *       point. Store MINOR UNITS as a whole number - cents, pence, satoshi - and the problem
 *       disappears. This is not pedantry; it is a real and recurring class of financial bug.</li>
 *   <li><b>Currency is part of the value.</b> Adding 100 USD to 100 EUR is not 200 of anything.
 *       A {@code long} lets you do it silently; this type must refuse.</li>
 *   <li><b>Immutable.</b> Every operation returns a NEW instance. Two references to the same
 *       Money can never surprise each other, which means it is free to share across threads and
 *       safe to use as a map key.</li>
 * </ol>
 *
 * <p>Implement:
 * <ul>
 *   <li>{@code of(currency, minorUnits)} - reject a null or blank currency</li>
 *   <li>{@code plus} / {@code minus} - throw {@code IllegalArgumentException} on a currency
 *       mismatch, with a message naming both currencies</li>
 *   <li>{@code times(int factor)}</li>
 *   <li>{@code isNegative()}</li>
 *   <li>{@code allocate(int parts)} - see below, it is the interesting one</li>
 * </ul>
 *
 * <p><b>allocate</b> splits an amount into {@code parts} shares that sum EXACTLY back to the
 * original. 100 cents into 3 is not 33.33 each - it is 34, 33, 33. Divide, then hand the
 * remainder out one minor unit at a time to the earliest shares. Getting this wrong is how
 * invoices end up off by a penny, and "where did the penny go" is a genuinely expensive bug
 * to chase in a ledger.
 */
public record Money(String currency, long minorUnits) {

    public static Money of(String currency, long minorUnits) {
        throw new UnsupportedOperationException("TODO(day16): validate and construct");
    }

    public Money plus(Money other) {
        throw new UnsupportedOperationException("TODO(day16): implement plus");
    }

    public Money minus(Money other) {
        throw new UnsupportedOperationException("TODO(day16): implement minus");
    }

    public Money times(int factor) {
        throw new UnsupportedOperationException("TODO(day16): implement times");
    }

    public boolean isNegative() {
        throw new UnsupportedOperationException("TODO(day16): implement isNegative");
    }

    public List<Money> allocate(int parts) {
        throw new UnsupportedOperationException("TODO(day16): implement allocate");
    }
}
