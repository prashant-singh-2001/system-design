package sd.p04.day34;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Day34ParkingLotTest {

    private static PricingStrategy flatRate(long centsPerHour) {
        return new HourlyPricingStrategy(Map.of(
                VehicleType.MOTORCYCLE, centsPerHour / 2,
                VehicleType.CAR, centsPerHour,
                VehicleType.BUS, centsPerHour * 3));
    }

    @Test
    @DisplayName("a bus can only be parked in a large spot")
    void busNeedsALargeSpot() {
        ParkingLot lot = new ParkingLot(List.of(
                new ParkingSpot("s1", SpotType.SMALL),
                new ParkingSpot("m1", SpotType.MEDIUM)), flatRate(500));

        assertThat(lot.parkVehicle("bus-1", VehicleType.BUS)).isEmpty();
    }

    @Test
    @DisplayName("a motorcycle prefers the smallest fitting spot, leaving larger ones free")
    void motorcyclePrefersSmallestFittingSpot() {
        ParkingLot lot = new ParkingLot(List.of(
                new ParkingSpot("small-1", SpotType.SMALL),
                new ParkingSpot("large-1", SpotType.LARGE)), flatRate(500));

        Optional<Ticket> ticket = lot.parkVehicle("moto-1", VehicleType.MOTORCYCLE);

        assertThat(ticket).isPresent();
        assertThat(ticket.get().spotId()).isEqualTo("small-1");
        assertThat(lot.availableSpots(SpotType.LARGE))
                .as("the large spot must still be free for something that actually needs it")
                .isEqualTo(1);
    }

    @Test
    @DisplayName("a full lot for a vehicle type returns empty, not an exception")
    void fullLotReturnsEmpty() {
        ParkingLot lot = new ParkingLot(List.of(new ParkingSpot("m1", SpotType.MEDIUM)), flatRate(500));
        lot.parkVehicle("car-1", VehicleType.CAR);

        assertThat(lot.parkVehicle("car-2", VehicleType.CAR)).isEmpty();
    }

    @Test
    @DisplayName("unparking computes cost from the injected pricing strategy")
    void unparkComputesCost() {
        ParkingLot lot = new ParkingLot(List.of(new ParkingSpot("m1", SpotType.MEDIUM)), flatRate(500));
        Instant enteredAt = Instant.parse("2024-01-01T10:00:00Z");
        Ticket ticket = lot.parkVehicle("car-1", VehicleType.CAR).orElseThrow();
        // Force a known enteredAt by re-issuing via a lot whose spot clock we don't control -
        // instead, price directly against the ticket the lot actually issued.
        long cost = lot.unparkVehicle(ticket.id(), ticket.enteredAt().plusSeconds(3_601));

        assertThat(cost).isEqualTo(1_000);   // 2 started hours at 500/hour for a CAR
    }

    @Test
    @DisplayName("unparking frees the spot for the next vehicle")
    void unparkingFreesTheSpot() {
        ParkingLot lot = new ParkingLot(List.of(new ParkingSpot("m1", SpotType.MEDIUM)), flatRate(500));
        Ticket ticket = lot.parkVehicle("car-1", VehicleType.CAR).orElseThrow();

        lot.unparkVehicle(ticket.id(), ticket.enteredAt().plusSeconds(60));

        assertThat(lot.availableSpots(SpotType.MEDIUM)).isEqualTo(1);
        assertThat(lot.parkVehicle("car-2", VehicleType.CAR)).isPresent();
    }

    @Test
    @DisplayName("unparking an unknown ticket fails loudly")
    void unknownTicketThrows() {
        ParkingLot lot = new ParkingLot(List.of(new ParkingSpot("m1", SpotType.MEDIUM)), flatRate(500));

        assertThatThrownBy(() -> lot.unparkVehicle("no-such-ticket", Instant.now()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("concurrent parking never double-assigns a spot")
    void concurrentAllocationNeverDoubleAssigns() throws Exception {
        int spotCount = 5;
        List<ParkingSpot> spots = new ArrayList<>();
        for (int i = 0; i < spotCount; i++) {
            spots.add(new ParkingSpot("m" + i, SpotType.MEDIUM));
        }
        ParkingLot lot = new ParkingLot(spots, flatRate(500));

        int attemptCount = 50;
        ExecutorService pool = Executors.newFixedThreadPool(16);
        List<java.util.concurrent.Future<Optional<Ticket>>> futures = new ArrayList<>();
        for (int i = 0; i < attemptCount; i++) {
            int carId = i;
            futures.add(pool.submit(() -> lot.parkVehicle("car-" + carId, VehicleType.CAR)));
        }
        pool.shutdown();
        assertThat(pool.awaitTermination(10, TimeUnit.SECONDS)).isTrue();

        List<Ticket> successes = new ArrayList<>();
        for (var future : futures) {
            future.get().ifPresent(successes::add);
        }

        assertThat(successes).as("exactly as many successes as spots exist").hasSize(spotCount);
        Set<String> distinctSpots = successes.stream().map(Ticket::spotId).collect(Collectors.toSet());
        assertThat(distinctSpots)
                .as("no two successful tickets may reference the same spot")
                .hasSize(spotCount);
    }
}
