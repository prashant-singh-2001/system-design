package sd.p02.day20;

/** What was set aside for an order: which SKU, and how much. */
public record Reservation(String sku, int quantity) {
}
