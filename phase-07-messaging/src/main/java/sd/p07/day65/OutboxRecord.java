package sd.p07.day65;

public record OutboxRecord(long id, String aggregateId, String eventType, String payload,
                           boolean published) {
}
