package sd.p02.day12;

/**
 * TODO(day12): the extension point.
 *
 * <p>One method: quote a shipment in cents. Each carrier becomes its own small, separately
 * testable class implementing this interface.
 *
 * <p>Keep it minimal. An interface with one method can be implemented by a lambda, which
 * is what makes the test able to register a brand-new carrier in a single line.
 */
public interface ShippingStrategy {

    long quoteCents(Shipment shipment);
}
