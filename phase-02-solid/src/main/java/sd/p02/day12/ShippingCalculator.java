package sd.p02.day12;

/**
 * TODO(day12): the calculator, now closed to modification.
 *
 * <p>Hold a {@code Map<String, ShippingStrategy>}. {@link #register} adds a carrier;
 * {@link #quoteCents} looks one up and delegates. Unknown carrier still throws
 * {@code IllegalArgumentException} with the message {@code "unknown carrier: X"}.
 *
 * <p>Provide a static {@link #withDefaults()} that pre-registers the three carriers we ship
 * with, so callers get a working calculator without wiring it themselves.
 *
 * <p>Once this exists, adding a carrier is: write a class, register it. This file never
 * changes again. That is what "closed to modification" buys you - and note that the
 * ENUMERATION of carriers moved from a hard-coded chain into runtime configuration, which
 * is also what makes per-tenant and per-region carrier sets possible.
 *
 * <p>Be honest about the cost too: the logic is now spread across five files instead of one,
 * and you cannot see every carrier's rules on one screen. OCP is worth paying for at the
 * axis of change you actually expect - and is over-engineering everywhere else.
 */
public final class ShippingCalculator {

    public static ShippingCalculator withDefaults() {
        throw new UnsupportedOperationException("TODO(day12): register the three built-in carriers");
    }

    public ShippingCalculator register(String carrier, ShippingStrategy strategy) {
        throw new UnsupportedOperationException("TODO(day12): add the strategy to the registry");
    }

    public long quoteCents(Shipment shipment) {
        throw new UnsupportedOperationException("TODO(day12): look up the strategy and delegate");
    }
}
