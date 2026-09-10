package sd.p04.day34;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * GIVEN - bills every STARTED hour at the vehicle type's hourly rate. 61 minutes is 2 hours,
 * not 1.0167 hours - parking pricing rounds up, the same "round up, always" lesson from Day 10's
 * capacity estimates, applied to money instead of servers.
 */
public final class HourlyPricingStrategy implements PricingStrategy {

    private final Map<VehicleType, Long> hourlyRateCents;

    public HourlyPricingStrategy(Map<VehicleType, Long> hourlyRateCents) {
        this.hourlyRateCents = Map.copyOf(hourlyRateCents);
    }

    @Override
    public long costCents(Ticket ticket, Instant exitAt) {
        Duration parked = Duration.between(ticket.enteredAt(), exitAt);
        long secondsParked = Math.max(0, parked.getSeconds());
        long wholeHoursStarted = (secondsParked + 3_599) / 3_600;   // ceiling division

        Long rate = hourlyRateCents.get(ticket.vehicleType());
        if (rate == null) {
            throw new IllegalArgumentException("no rate configured for " + ticket.vehicleType());
        }
        return wholeHoursStarted * rate;
    }
}
