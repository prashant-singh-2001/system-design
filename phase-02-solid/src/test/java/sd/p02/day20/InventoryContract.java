package sd.p02.day20;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * GIVEN - a CHARACTERIZATION TEST. Every method here pins down what {@link LegacyInventory}
 * actually does today, quirks included, written by observing the class rather than by deciding
 * what it should do. That distinction is the whole technique: a characterization test is not
 * allowed an opinion.
 *
 * <p>{@code Day20LegacyCharacterizationTest} runs this contract against {@link LegacyInventory}
 * and is green from the start - that green run IS the safety net. Only once it is green do you
 * touch the production code: split it into {@link StockLedger}, {@link ReservationTracker} and
 * {@link DefaultInventory}, then make {@code Day20RefactoredInventoryTest} extend this same
 * contract and turn it green too, unchanged. If you ever feel the urge to edit a test here to
 * make the refactor easier, stop - that is the exact moment a refactor quietly becomes a rewrite.
 */
abstract class InventoryContract {

    abstract Inventory create();

    @Test
    @DisplayName("contract 1: an unrestocked SKU has zero available, not an error")
    void unknownSkuHasZeroAvailable() {
        assertThat(create().available("SKU-GHOST")).isZero();
    }

    @Test
    @DisplayName("contract 2: restock raises availability by exactly the quantity")
    void restockRaisesAvailability() {
        Inventory inventory = create();
        inventory.restock("SKU-A", 10);

        assertThat(inventory.available("SKU-A")).isEqualTo(10);
    }

    @Test
    @DisplayName("contract 3: restock is cumulative")
    void restockAccumulates() {
        Inventory inventory = create();
        inventory.restock("SKU-A", 10);
        inventory.restock("SKU-A", 5);

        assertThat(inventory.available("SKU-A")).isEqualTo(15);
    }

    @Test
    @DisplayName("contract 4: restocking a negative quantity decrements, but never below zero")
    void restockClampsAtZero() {
        Inventory inventory = create();
        inventory.restock("SKU-A", 3);

        inventory.restock("SKU-A", -10);

        assertThat(inventory.available("SKU-A")).isZero();
    }

    @Test
    @DisplayName("contract 5: reserving within stock succeeds and lowers availability")
    void reserveWithinStockSucceeds() {
        Inventory inventory = create();
        inventory.restock("SKU-A", 10);

        boolean reserved = inventory.reserve("order-1", "SKU-A", 4);

        assertThat(reserved).isTrue();
        assertThat(inventory.available("SKU-A")).isEqualTo(6);
    }

    @Test
    @DisplayName("contract 6: reserving more than available fails and changes nothing")
    void reserveBeyondStockFails() {
        Inventory inventory = create();
        inventory.restock("SKU-A", 3);

        boolean reserved = inventory.reserve("order-1", "SKU-A", 4);

        assertThat(reserved).isFalse();
        assertThat(inventory.available("SKU-A")).isEqualTo(3);
    }

    @Test
    @DisplayName("contract 7: reserving a non-positive quantity trivially succeeds, untouched")
    void reserveNonPositiveIsNoOpSuccess() {
        Inventory inventory = create();
        inventory.restock("SKU-A", 5);

        assertThat(inventory.reserve("order-1", "SKU-A", 0)).isTrue();
        assertThat(inventory.reserve("order-2", "SKU-A", -1)).isTrue();
        assertThat(inventory.available("SKU-A")).isEqualTo(5);
    }

    @Test
    @DisplayName("contract 8: reserving the same orderId again succeeds without moving stock twice")
    void repeatedReservationForSameOrderIsIdempotent() {
        Inventory inventory = create();
        inventory.restock("SKU-A", 10);

        assertThat(inventory.reserve("order-1", "SKU-A", 4)).isTrue();
        assertThat(inventory.reserve("order-1", "SKU-A", 4))
                .as("a retried request for the same order must not be charged twice")
                .isTrue();

        assertThat(inventory.available("SKU-A")).isEqualTo(6);
    }

    @Test
    @DisplayName("contract 9: releasing a reservation credits the stock back")
    void releaseCreditsStockBack() {
        Inventory inventory = create();
        inventory.restock("SKU-A", 10);
        inventory.reserve("order-1", "SKU-A", 4);

        inventory.release("order-1");

        assertThat(inventory.available("SKU-A")).isEqualTo(10);
    }

    @Test
    @DisplayName("contract 10: releasing an unknown orderId is silently ignored")
    void releaseOfUnknownOrderIsNoOp() {
        Inventory inventory = create();
        inventory.restock("SKU-A", 10);

        inventory.release("no-such-order");

        assertThat(inventory.available("SKU-A")).isEqualTo(10);
    }

    @Test
    @DisplayName("contract 11: after release, the same orderId can reserve again")
    void releasedOrderCanReserveAgain() {
        Inventory inventory = create();
        inventory.restock("SKU-A", 10);
        inventory.reserve("order-1", "SKU-A", 4);
        inventory.release("order-1");

        boolean reservedAgain = inventory.reserve("order-1", "SKU-A", 6);

        assertThat(reservedAgain).isTrue();
        assertThat(inventory.available("SKU-A")).isEqualTo(4);
    }

    @Test
    @DisplayName("contract 12: SKUs are tracked independently")
    void skusAreIndependent() {
        Inventory inventory = create();
        inventory.restock("SKU-A", 10);
        inventory.restock("SKU-B", 3);

        inventory.reserve("order-1", "SKU-A", 5);

        assertThat(inventory.available("SKU-A")).isEqualTo(5);
        assertThat(inventory.available("SKU-B")).isEqualTo(3);
    }
}
