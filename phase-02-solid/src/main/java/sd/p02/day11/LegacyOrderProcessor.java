package sd.p02.day11;

import java.util.ArrayList;
import java.util.List;

/**
 * GIVEN, and deliberately awful. Read it, then leave it alone as a before-picture.
 *
 * <p>Count the reasons this class might have to change:
 * <ol>
 *   <li>the validation rules change</li>
 *   <li>the pricing or tax rules change</li>
 *   <li>the storage technology changes</li>
 *   <li>the notification channel changes</li>
 *   <li>the audit log format changes</li>
 * </ol>
 *
 * <p>Five reasons, five different teams, one file. That is what "single responsibility"
 * is actually about - not method count, not line count, but the number of independent
 * forces that can demand a change here.
 *
 * <p>The practical damage: you cannot test the tax calculation without a database and an
 * email server. That is the tell. Whenever testing one rule requires standing up unrelated
 * infrastructure, responsibilities are tangled.
 */
public final class LegacyOrderProcessor {

    private final List<String> auditLog = new ArrayList<>();
    private final List<String> savedOrders = new ArrayList<>();
    private final List<String> sentEmails = new ArrayList<>();

    public long process(Order order) {
        // --- validation ---
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

        // --- pricing ---
        long subtotal = 0;
        for (OrderLine line : order.lines()) {
            subtotal += line.quantity() * line.unitPriceCents();
        }
        long discounted = subtotal;
        if ("SAVE10".equals(order.couponCode())) {
            discounted = subtotal - (subtotal / 10);
        }
        long tax = discounted / 5;                       // 20%
        long shipping = discounted >= 5_000 ? 0 : 499;
        long total = discounted + tax + shipping;

        // --- persistence ---
        savedOrders.add(order.id() + ":" + total);

        // --- notification ---
        sentEmails.add("To: " + order.customerEmail() + " / order " + order.id()
                + " total " + total);

        // --- audit ---
        auditLog.add("processed " + order.id() + " total=" + total);

        return total;
    }

    public List<String> savedOrders() {
        return List.copyOf(savedOrders);
    }

    public List<String> sentEmails() {
        return List.copyOf(sentEmails);
    }

    public List<String> auditLog() {
        return List.copyOf(auditLog);
    }
}
