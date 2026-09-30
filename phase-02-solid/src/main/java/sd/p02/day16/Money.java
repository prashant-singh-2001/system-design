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
        if(currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("Currency must not be null or blank");
        }
        if(minorUnits < 0) {
            throw new IllegalArgumentException("Minor units must be a non-negative number");
        }
        return new Money(currency, minorUnits);
    }

    public Money plus(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Currency mismatch: " + this.currency + " vs " + other.currency);
        }
        return new Money(this.currency, this.minorUnits + other.minorUnits);
    }

    public Money minus(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Currency mismatch: " + this.currency + " vs " + other.currency);
        }
        return new Money(this.currency, this.minorUnits - other.minorUnits);
    }

    public Money times(int factor) {
        if (factor < 0) {
            throw new IllegalArgumentException("Factor must be a non-negative number");
        }
        return new Money(this.currency, this.minorUnits * factor);
    }

    public boolean isNegative() {
        return this.minorUnits < 0;
    }

    public List<Money> allocate(int parts) {
        // if (parts <= 0) {
        //     throw new IllegalArgumentException("Parts must be a positive number");
        // }
        // long baseAmount = this.minorUnits / parts;
        // long remainder = this.minorUnits % parts;

        return parts <= 0 ? List.of() : java.util.stream.IntStream.range(0, parts)
                .mapToObj(i -> new Money(this.currency, this.minorUnits / parts + (i < this.minorUnits % parts ? 1 : 0)))
                .toList();
    }
}
