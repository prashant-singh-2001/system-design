package sd.p02.day11;

/**
 * TODO(day11): extract the validation rules, unchanged.
 *
 * <p>Throw {@code IllegalArgumentException} with the same messages the legacy class used, so
 * behaviour is genuinely preserved. Refactoring means changing structure without changing
 * behaviour - if the messages drift, you have rewritten rather than refactored.
 */
public final class OrderValidator {

    public void validate(Order order) {
        if (order.id() == null || order.id().isBlank()) {
            throw new IllegalArgumentException("order id is required");
        }
        if (order.customerEmail() == null || !order.customerEmail().contains("@")) {
            throw new IllegalArgumentException("valid customer email is required");
        }
        if (order.lines().isEmpty()) {
            throw new IllegalArgumentException("order must have at least one line");
        }
        for (OrderLine line : order.lines()) {
            if (line.quantity() <= 0) {
                throw new IllegalArgumentException("quantity must be positive: " + line.sku());
            }
            if (line.unitPriceCents() < 0) {
                throw new IllegalArgumentException("price cannot be negative: " + line.sku());
            }
        }
    }
}
