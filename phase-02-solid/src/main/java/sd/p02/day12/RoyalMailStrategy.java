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
        long base = 350;
        double weight = shipment.weightKg();
        long destination = shipment.destinationCountry().equals("GB") ? 1 : 0;
        long cost = base;
        if (weight > 2) {
            cost += (weight - 2) * 100;
        }
        if (destination != 1) { // Assuming 1 represents GB
            cost += 900;
        }
        return shipment.express() ? cost * 2 : cost;
    }
}
