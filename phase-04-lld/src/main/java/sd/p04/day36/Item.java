package sd.p04.day36;

/** A slot's contents: what it is, what it costs, and how many are left. */
public record Item(String code, String name, long priceCents, int stock) {
}
