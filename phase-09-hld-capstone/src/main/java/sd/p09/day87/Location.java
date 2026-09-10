package sd.p09.day87;

/** A point on the globe. */
public record Location(double latitude, double longitude) {

    public Location {
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("not a point on Earth: " + latitude + "," + longitude);
        }
    }
}
