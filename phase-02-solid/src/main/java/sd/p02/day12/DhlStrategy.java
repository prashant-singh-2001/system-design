package sd.p02.day12;

/**
 * TODO(day12): move the DHL branch here, unchanged.
 *
 * <p>base 800; plus 150 per kg; plus 400 if the destination is not GB;
 * plus a flat 1,200 if express.
 */
public final class DhlStrategy implements ShippingStrategy {

    @Override
    public long quoteCents(Shipment shipment) {
        throw new UnsupportedOperationException("TODO(day12): move the DHL rules here");
    }
}
