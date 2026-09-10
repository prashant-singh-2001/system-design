package sd.p02.day12;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Day12OpenClosedTest {

    static Stream<Shipment> shipments() {
        return Stream.of(
                new Shipment("ROYAL_MAIL", 1.0, "GB", false),
                new Shipment("ROYAL_MAIL", 5.0, "GB", false),
                new Shipment("ROYAL_MAIL", 5.0, "FR", true),
                new Shipment("DHL", 1.0, "GB", false),
                new Shipment("DHL", 3.5, "US", true),
                new Shipment("PICKUP", 10.0, "GB", true));
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("shipments")
    @DisplayName("the refactored calculator quotes exactly what the legacy one did")
    void behaviourIsPreserved(Shipment shipment) {
        long legacy = new LegacyShippingCalculator().quoteCents(shipment);
        long refactored = ShippingCalculator.withDefaults().quoteCents(shipment);

        assertThat(refactored)
                .as("moving code into strategies must not change a single quote")
                .isEqualTo(legacy);
    }

    @Test
    @DisplayName("an unknown carrier still fails the same way")
    void unknownCarrier() {
        assertThatThrownBy(() -> ShippingCalculator.withDefaults()
                .quoteCents(new Shipment("SPACE_X", 1, "GB", false)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("unknown carrier: SPACE_X");
    }

    @Test
    @DisplayName("THE test: a brand-new carrier works without editing a single existing file")
    void openForExtension() {
        // This lambda is a carrier that did not exist when ShippingCalculator was written,
        // compiled, reviewed and deployed. If this passes, the class is genuinely closed
        // to modification.
        ShippingCalculator calculator = ShippingCalculator.withDefaults()
                .register("DRONE", shipment -> 2_500 + (long) (shipment.weightKg() * 500));

        assertThat(calculator.quoteCents(new Shipment("DRONE", 2.0, "GB", false)))
                .isEqualTo(3_500);

        // ... and the existing carriers are untouched.
        assertThat(calculator.quoteCents(new Shipment("PICKUP", 1, "GB", false)))
                .isEqualTo(0);
    }

    @Test
    @DisplayName("each carrier is now independently testable")
    void strategiesStandAlone() {
        assertThat(new PickupStrategy().quoteCents(new Shipment("PICKUP", 99, "JP", true)))
                .isEqualTo(0);
        assertThat(new RoyalMailStrategy().quoteCents(new Shipment("ROYAL_MAIL", 1, "GB", false)))
                .isEqualTo(350);
    }
}
