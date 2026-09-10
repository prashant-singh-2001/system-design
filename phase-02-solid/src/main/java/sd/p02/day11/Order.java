package sd.p02.day11;

import java.util.List;

public record Order(String id, String customerEmail, List<OrderLine> lines, String couponCode) {

    public Order {
        lines = List.copyOf(lines);      // defensive copy - Day 16 covers why
    }

    public static Order sample() {
        return new Order("ord-1", "buyer@example.com",
                List.of(new OrderLine("SKU-A", 2, 1_500), new OrderLine("SKU-B", 1, 3_000)),
                null);
    }
}
