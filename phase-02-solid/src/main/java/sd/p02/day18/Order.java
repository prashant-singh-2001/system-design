package sd.p02.day18;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO(day18): an AGGREGATE ROOT.
 *
 * <p>An aggregate is a cluster of objects treated as one unit for consistency. The root is
 * the only way in: nothing outside may hold a reference to an internal line and change it.
 * That is what makes invariants enforceable, because every path that could break them goes
 * through methods you control.
 *
 * <p>Two ideas worth separating, because they get conflated:
 * <ul>
 *   <li>A <b>value object</b> ({@link sd.p02.day16.Money}) has no identity - two instances with
 *       equal fields are the same thing, and it should be immutable.</li>
 *   <li>An <b>entity</b> has identity and a lifecycle. An order with id {@code ord-1} is the
 *       same order today and tomorrow, even after its contents change. It may be mutable -
 *       but every mutation must leave it valid.</li>
 * </ul>
 *
 * <p>The invariants to enforce. Illegal STATE transitions throw {@code IllegalStateException};
 * illegal DATA throws {@code IllegalArgumentException}:
 * <ol>
 *   <li>A new order starts {@code DRAFT} with no lines.</li>
 *   <li>Lines may only be added or removed while {@code DRAFT}.</li>
 *   <li>Quantity must be positive; unit price must not be negative.</li>
 *   <li>Adding a SKU that is already present MERGES the quantities rather than duplicating.
 *       The unit price on the merged line is the one from THIS call - a re-add is treated as
 *       "add more, at today's price", not as a second independent line.</li>
 *   <li>{@code removeLine} on a SKU that is not present is a no-op, not an error - removing
 *       something that is already gone should never be the caller's problem.</li>
 *   <li>{@code submit()} requires at least one line, and moves DRAFT to SUBMITTED.</li>
 *   <li>{@code pay()} moves SUBMITTED to PAID. Nothing else may be paid.</li>
 *   <li>{@code cancel()} works from DRAFT or SUBMITTED. A PAID order may NOT be cancelled -
 *       that is a refund, which is a different process with different rules.</li>
 *   <li>{@code lines()} returns an unmodifiable view. Day 16 explains why.</li>
 * </ol>
 *
 * <p>The payoff: an invalid order cannot exist, even briefly. No caller has to remember to
 * check the status before adding a line, because there is no way to get it wrong. Compare
 * that with scattering {@code if (order.getStatus() == DRAFT)} across five services and
 * hoping nobody forgets - which is how most codebases actually do it.
 */
public final class Order {

    private List<OrderLine> lines;
    private OrderStatus status;
    private String id;
    private String customerId;

    public static Order draft(String id, String customerId) {
        Order order = new Order();
        order.lines = new ArrayList<>();
        order.status = OrderStatus.DRAFT;
        order.id = id;
        order.customerId = customerId;
        return order;
    }

    public String id() {
        return id;
    }

    public String customerId() {
        return customerId;
    }

    public OrderStatus status() {
        return status;
    }

    public List<OrderLine> lines() {
        return List.copyOf(lines);
    }

    public long totalCents() {
        return lines.stream().mapToLong(line -> line.unitPriceCents() * line.quantity()).sum();
    }

    public void addLine(String sku, int quantity, long unitPriceCents) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        if (unitPriceCents < 0) {
            throw new IllegalArgumentException("unit price must not be negative");
        }
        if(status != OrderStatus.DRAFT) {
            throw new IllegalStateException("can only add lines to DRAFT orders");
        }
        if(lines.stream().anyMatch(line -> line.sku().equals(sku))) {
            // merge with existing line
            lines.stream()
                    .filter(line -> line.sku().equals(sku))
                    .findFirst()
                    .ifPresent(line -> {
                        int newQuantity = line.quantity() + quantity;
                        lines.remove(line);
                        lines.add(new OrderLine(sku, newQuantity, unitPriceCents));
                    });
            return;
        }
        lines.add(new OrderLine(sku, quantity, unitPriceCents));
    }

    public void removeLine(String sku) {
        if(status != OrderStatus.DRAFT) {
            throw new IllegalStateException("can only remove lines from DRAFT orders");
        }
        lines.removeIf(line -> line.sku().equals(sku));
    }

    public void submit() {
        if(status != OrderStatus.DRAFT) {
            throw new IllegalStateException("can only submit DRAFT orders");
        }
        if(lines.isEmpty()) {
            throw new IllegalStateException("can only submit orders with lines");
        }
        status = OrderStatus.SUBMITTED;
    }

    public void pay() {
        if(status != OrderStatus.SUBMITTED) {
            throw new IllegalStateException("can only pay SUBMITTED orders");
        }
        status = OrderStatus.PAID;
    }

    public void cancel() {
        if(status != OrderStatus.DRAFT && status != OrderStatus.SUBMITTED) {
            throw new IllegalStateException("can only cancel DRAFT or SUBMITTED orders");
        }
        status = OrderStatus.CANCELLED;
    }
}
