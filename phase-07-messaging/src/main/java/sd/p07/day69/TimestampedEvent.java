package sd.p07.day69;

import java.time.Instant;

/**
 * A stream record with two timestamps' worth of meaning.
 *
 * @param eventTime when it actually HAPPENED (on the phone, at the sensor, at the till)
 */
public record TimestampedEvent(String key, long value, Instant eventTime) {
}
