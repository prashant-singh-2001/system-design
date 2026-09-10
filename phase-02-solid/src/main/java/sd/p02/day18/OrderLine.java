package sd.p02.day18;

public record OrderLine(String sku, int quantity, long unitPriceCents) {

    public long subtotalCents() {
        return quantity * unitPriceCents;
    }
}
