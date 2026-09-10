package sd.p02.day18;

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

    public static Order draft(String id, String customerId) {
        throw new UnsupportedOperationException("TODO(day18): create an empty DRAFT order");
    }

    public String id() {
        throw new UnsupportedOperationException("TODO(day18)");
    }

    public String customerId() {
        throw new UnsupportedOperationException("TODO(day18)");
    }

    public OrderStatus status() {
        throw new UnsupportedOperationException("TODO(day18)");
    }

    public List<OrderLine> lines() {
        throw new UnsupportedOperationException("TODO(day18): return an unmodifiable view");
    }

    public long totalCents() {
        throw new UnsupportedOperationException("TODO(day18): sum the line subtotals");
    }

    public void addLine(String sku, int quantity, long unitPriceCents) {
        throw new UnsupportedOperationException("TODO(day18): guard state, validate, merge");
    }

    public void removeLine(String sku) {
        throw new UnsupportedOperationException("TODO(day18): DRAFT only");
    }

    public void submit() {
        throw new UnsupportedOperationException("TODO(day18): DRAFT with lines -> SUBMITTED");
    }

    public void pay() {
        throw new UnsupportedOperationException("TODO(day18): SUBMITTED -> PAID");
    }

    public void cancel() {
        throw new UnsupportedOperationException("TODO(day18): DRAFT or SUBMITTED -> CANCELLED");
    }
}
