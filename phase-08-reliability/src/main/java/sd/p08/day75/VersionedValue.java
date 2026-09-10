package sd.p08.day75;

/** A value plus the version that lets a reader tell which copy is newest. */
public record VersionedValue(String value, long version) {
}
