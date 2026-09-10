package sd.p02.day11;

/**
 * TODO(day11): extract the pricing rules from {@link LegacyOrderProcessor}.
 *
 * <p>Exactly the same arithmetic, in a class that knows nothing about databases, email or
 * validation. When you are done you will be able to test tax calculation in a microsecond
 * with no infrastructure at all - which is the entire practical payoff of SRP.
 *
 * <p>The rules, unchanged:
 * <ul>
 *   <li>subtotal = sum of quantity x unit price</li>
 *   <li>coupon {@code SAVE10} takes 10% off the subtotal (integer division)</li>
 *   <li>tax is 20% of the discounted subtotal (integer division)</li>
 *   <li>shipping is free at 5,000 cents or more, otherwise 499</li>
 *   <li>total = discounted + tax + shipping</li>
 * </ul>
 */
public final class PricingService {

    public long subtotalCents(Order order) {
        throw new UnsupportedOperationException("TODO(day11): implement subtotalCents");
    }

    public long discountedSubtotalCents(Order order) {
        throw new UnsupportedOperationException("TODO(day11): implement discountedSubtotalCents");
    }

    public long totalCents(Order order) {
        throw new UnsupportedOperationException("TODO(day11): implement totalCents");
    }
}
