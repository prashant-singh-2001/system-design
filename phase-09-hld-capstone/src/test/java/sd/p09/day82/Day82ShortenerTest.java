package sd.p09.day82;

import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Day82ShortenerTest {

    @Test
    @DisplayName("base-62 capacity: seven characters is the industry answer, and here is why")
    void capacity() {
        assertThat(ShortCodeMath.capacity(6))
                .as("about 57 billion")
                .isCloseTo(5.68e10, Percentage.withPercentage(1));
        assertThat(ShortCodeMath.capacity(7))
                .as("about 3.5 trillion")
                .isCloseTo(3.52e12, Percentage.withPercentage(1));
        assertThat(ShortCodeMath.capacity(8))
                .as("about 218 trillion")
                .isCloseTo(2.18e14, Percentage.withPercentage(1));
    }

    @Test
    @DisplayName("the birthday paradox: collisions arrive far sooner than the key space suggests")
    void collisionProbability() {
        // 1 million random 6-character codes into a 57-billion space.
        double atOneMillion = ShortCodeMath.collisionProbability(1_000_000, 6);

        System.out.printf("  1M random 6-char codes -> collision probability %.4f%n", atOneMillion);

        assertThat(atOneMillion)
                .as("""
                        A million codes in a 57-billion space sounds utterly safe, and a collision
                        is close to certain. The intuition is wrong by orders of magnitude, which
                        is exactly why you compute it instead of estimating it.""")
                .isGreaterThan(0.99);

        assertThat(ShortCodeMath.collisionProbability(1_000, 8))
                .as("a thousand codes in a 218-trillion space really is safe")
                .isLessThan(1e-6);
    }

    @Test
    @DisplayName("no codes, no collisions")
    void emptyCase() {
        assertThat(ShortCodeMath.collisionProbability(0, 7)).isZero();
        assertThat(ShortCodeMath.collisionProbability(1, 7)).isLessThan(1e-9);
    }

    @Test
    @DisplayName("the design question: how many characters do I actually need?")
    void lengthFor() {
        int forABillion = ShortCodeMath.lengthFor(1_000_000_000L, 0.01);

        System.out.printf("  1 billion URLs at 1%% acceptable collision -> %d characters%n",
                forABillion);

        assertThat(forABillion).isBetween(9, 12);
        assertThat(ShortCodeMath.lengthFor(1_000, 0.01))
                .as("a thousand URLs needs far fewer")
                .isLessThan(forABillion);
    }

    @Test
    @DisplayName("longer is always at least as safe")
    void lengthIsMonotonic() {
        for (int length = 1; length < 10; length++) {
            assertThat(ShortCodeMath.collisionProbability(1_000_000, length + 1))
                    .isLessThanOrEqualTo(ShortCodeMath.collisionProbability(1_000_000, length));
        }
    }

    @Test
    @DisplayName("base-62 encoding of a counter")
    void base62() {
        assertThat(ShortCodeMath.encodeBase62(0)).isEqualTo("0");
        assertThat(ShortCodeMath.encodeBase62(1)).isEqualTo("1");
        assertThat(ShortCodeMath.encodeBase62(61)).isEqualTo("z");
        assertThat(ShortCodeMath.encodeBase62(62)).isEqualTo("10");
        assertThat(ShortCodeMath.encodeBase62(3_844)).isEqualTo("100");
    }

    @Test
    @DisplayName("counter codes are compact - and sequential, which leaks your volume")
    void counterCodesAreShortAndGuessable() {
        String billion = ShortCodeMath.encodeBase62(1_000_000_000L);

        System.out.printf("  the billionth URL gets the code %s%n", billion);

        assertThat(billion.length())
                .as("six characters for a billion URLs - nothing beats a counter for density")
                .isLessThanOrEqualTo(6);

        assertThat(ShortCodeMath.encodeBase62(1_000_001L))
                .as("""
                        Adjacent counter values give adjacent codes. Anyone who reads one code can
                        enumerate your links and watch your total climb. That is the price of the
                        density, and it is a product decision, not just a technical one.""")
                .isNotEqualTo(ShortCodeMath.encodeBase62(1_000_000L));
    }

    @Test
    @DisplayName("every strategy is named and distinct - the choice is explicit")
    void strategiesAreEnumerated() {
        assertThat(KeyGenerationStrategy.values()).hasSize(3);
    }
}
