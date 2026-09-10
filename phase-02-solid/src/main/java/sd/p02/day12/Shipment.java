package sd.p02.day12;

/** What we are quoting for. */
public record Shipment(String carrier, double weightKg, String destinationCountry, boolean express) {
}
