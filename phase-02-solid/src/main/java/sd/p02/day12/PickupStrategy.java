package sd.p02.day12;

/** TODO(day12): customer collection is free. The simplest strategy in the set. */
public final class PickupStrategy implements ShippingStrategy {

    @Override
    public long quoteCents(Shipment shipment) {
        throw new UnsupportedOperationException("TODO(day12): pickup costs nothing");
    }
}
