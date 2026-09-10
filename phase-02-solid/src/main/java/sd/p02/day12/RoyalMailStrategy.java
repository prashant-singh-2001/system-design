package sd.p02.day12;

/**
 * TODO(day12): move the ROYAL_MAIL branch here, unchanged.
 *
 * <p>base 350; plus 100 per kg over 2 kg; plus 900 if the destination is not GB;
 * doubled if express.
 */
public final class RoyalMailStrategy implements ShippingStrategy {

    @Override
    public long quoteCents(Shipment shipment) {
        throw new UnsupportedOperationException("TODO(day12): move the ROYAL_MAIL rules here");
    }
}
