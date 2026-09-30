package sd.p02.day16;

import java.util.List;

/**
 * TODO(day16): immutability at the boundary, which is where it actually gets broken.
 *
 * <p>{@link Money} is a record, so it is immutable. But a class that HOLDS a collection of
 * them is only immutable if it guards both doors:
 *
 * <ul>
 *   <li><b>The way in.</b> Copy the incoming list in the constructor. Otherwise the caller
 *       keeps a reference to your internal state and can mutate your basket from the outside,
 *       long after construction, with no method call you could ever have logged.</li>
 *   <li><b>The way out.</b> Return an unmodifiable view (or a copy) from {@link #items()}.
 *       Otherwise you handed your internals to whoever asked.</li>
 * </ul>
 *
 * <p>Miss either one and you have a mutable object that merely looks immutable - which is
 * worse than an obviously mutable one, because everyone will reason about it as if it were safe.
 *
 * <p>Implement the constructor, {@link #items()}, and {@link #total()} (sum in the given
 * currency; an empty basket totals zero).
 */
public final class Basket {

    private final String currency;
    private final List<Money> items;

    public Basket(String currency, List<Money> items) {
        this.currency = currency;
        this.items = List.copyOf(items);
    }

    public List<Money> items() {
        return List.copyOf(this.items);
    }

    public Money total() {
        long totalMinorUnits = items.stream()
                .filter(item -> item.currency().equals(this.currency))
                .mapToLong(Money::minorUnits)
                .sum();
        return Money.of(this.currency, totalMinorUnits);
    }
}
