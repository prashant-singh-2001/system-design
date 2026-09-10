package sd.p07.day70;

import java.util.Map;

/** What one end-to-end run of the pipeline achieved. */
public record PipelineReport(int ordersPlaced, int eventsRelayed, int eventsConsumed,
                             int duplicatesSuppressed, Map<String, Long> revenueByCustomer) {
}
