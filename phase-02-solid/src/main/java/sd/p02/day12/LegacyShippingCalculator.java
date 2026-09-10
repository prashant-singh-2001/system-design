package sd.p02.day12;

/**
 * GIVEN, and deliberately closed to extension. The before-picture.
 *
 * <p>Every new carrier means editing this method. That means re-reviewing it, re-testing every
 * existing carrier, and re-deploying the module that owns it. A partner integration - which
 * ought to be additive - becomes a change to shared, already-working code.
 *
 * <p>Worse, this file becomes a merge-conflict magnet: three teams adding three carriers all
 * edit the same twenty lines.
 *
 * <p>The Open/Closed Principle says a module should be open to EXTENSION but closed to
 * MODIFICATION. The test for it is concrete and unsentimental: can you add a carrier without
 * editing this file?
 */
public final class LegacyShippingCalculator {

    public long quoteCents(Shipment shipment) {
        if ("ROYAL_MAIL".equals(shipment.carrier())) {
            long base = 350;
            if (shipment.weightKg() > 2) {
                base += (long) ((shipment.weightKg() - 2) * 100);
            }
            if (!"GB".equals(shipment.destinationCountry())) {
                base += 900;
            }
            return shipment.express() ? base * 2 : base;

        } else if ("DHL".equals(shipment.carrier())) {
            long base = 800;
            base += (long) (shipment.weightKg() * 150);
            if (!"GB".equals(shipment.destinationCountry())) {
                base += 400;
            }
            return shipment.express() ? base + 1_200 : base;

        } else if ("PICKUP".equals(shipment.carrier())) {
            return 0;
        }
        throw new IllegalArgumentException("unknown carrier: " + shipment.carrier());
    }
}
