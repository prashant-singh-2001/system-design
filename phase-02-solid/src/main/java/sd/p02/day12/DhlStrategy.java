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
        long quote = 800;
        quote += 150 * shipment.weightKg();
        if (!shipment.destinationCountry().equals("GB")) {
            quote += 400;
        }
        if (shipment.express()) {
            quote += 1200;
        }
        return quote;
    }
}
