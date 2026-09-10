package sd.p09.day87;

import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Day87GeoTest {

    private static final Location LONDON = new Location(51.5074, -0.1278);
    private static final Location PARIS = new Location(48.8566, 2.3522);
    private static final Location NEW_YORK = new Location(40.7128, -74.0060);

    @Test
    @DisplayName("geohash encodes to the documented value")
    void encoding() {
        assertThat(Geohash.encode(LONDON, 6))
                .as("central London is gcpvj...")
                .startsWith("gcpv");
        assertThat(Geohash.encode(new Location(0, 0), 5)).startsWith("s000");
    }

    @Test
    @DisplayName("precision is a length, and each character narrows the cell")
    void precision() {
        String coarse = Geohash.encode(LONDON, 4);
        String fine = Geohash.encode(LONDON, 8);

        assertThat(coarse).hasSize(4);
        assertThat(fine).hasSize(8);
        assertThat(fine).startsWith(coarse);
    }

    @Test
    @DisplayName("nearby points share a prefix - which is what makes this indexable")
    void nearbyPointsSharePrefixes() {
        Location nearLondon = new Location(51.5080, -0.1270);

        assertThat(Geohash.sharePrefix(
                Geohash.encode(LONDON, 8), Geohash.encode(nearLondon, 8), 5))
                .as("""
                        'Near me' becomes a prefix match, which any B-tree or sorted set answers
                        instantly. The alternative - computing a distance for every driver - is a
                        full scan per request.""")
                .isTrue();
    }

    @Test
    @DisplayName("distant points do not")
    void distantPointsDiffer() {
        assertThat(Geohash.sharePrefix(
                Geohash.encode(LONDON, 6), Geohash.encode(NEW_YORK, 6), 2)).isFalse();
    }

    @Test
    @DisplayName("haversine gives real distances")
    void distances() {
        assertThat(Geohash.haversineKm(LONDON, PARIS))
                .as("London to Paris is about 344 km")
                .isCloseTo(344, Percentage.withPercentage(2));
        assertThat(Geohash.haversineKm(LONDON, NEW_YORK))
                .as("London to New York is about 5,570 km")
                .isCloseTo(5_570, Percentage.withPercentage(2));
        assertThat(Geohash.haversineKm(LONDON, LONDON)).isZero();
    }

    @Test
    @DisplayName("THE flaw: two points metres apart can have completely different geohashes")
    void boundaryProblem() {
        // Either side of a cell boundary. Physically adjacent, lexically unrelated.
        Location justWest = new Location(51.5, -0.0001);
        Location justEast = new Location(51.5, 0.0001);

        double metresApart = Geohash.haversineKm(justWest, justEast) * 1_000;
        String westHash = Geohash.encode(justWest, 6);
        String eastHash = Geohash.encode(justEast, 6);

        System.out.printf("  %.0f metres apart, geohashes %s and %s%n",
                metresApart, westHash, eastHash);

        assertThat(metresApart).isLessThan(50);
        assertThat(westHash.charAt(0))
                .as("""
                        A prefix search alone silently misses the driver on the other side of the
                        line. The fix is to search the cell AND its eight neighbours, then filter
                        by true distance - which every production geospatial search does.""")
                .isNotEqualTo(eastHash.charAt(0));
    }

    @Test
    @DisplayName("prefix-then-filter: the two-step search that actually works")
    void prefixThenFilter() {
        Location rider = LONDON;
        String riderCell = Geohash.encode(rider, 5);

        java.util.List<Location> drivers = java.util.List.of(
                new Location(51.5080, -0.1270),     // very close
                new Location(51.5200, -0.1000),     // a couple of km
                PARIS,
                NEW_YORK);

        java.util.List<Location> candidates = drivers.stream()
                .filter(d -> Geohash.sharePrefix(Geohash.encode(d, 5), riderCell, 4))
                .filter(d -> Geohash.haversineKm(rider, d) <= 5)
                .toList();

        assertThat(candidates)
                .as("the prefix narrows the set cheaply; the haversine makes it correct")
                .hasSize(2);
    }

    @Test
    @DisplayName("a point off the Earth is rejected")
    void validation() {
        assertThatThrownBy(() -> new Location(91, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Location(0, 181)).isInstanceOf(IllegalArgumentException.class);
    }
}
