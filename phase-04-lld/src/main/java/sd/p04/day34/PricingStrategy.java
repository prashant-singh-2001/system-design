package sd.p04.day34;

import java.time.Instant;

/** The STRATEGY (Phase 3, Day 21) for turning a stay into a cost - pluggable on purpose. */
@FunctionalInterface
public interface PricingStrategy {

    long costCents(Ticket ticket, Instant exitAt);
}
