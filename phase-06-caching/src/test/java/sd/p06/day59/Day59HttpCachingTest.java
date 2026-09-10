package sd.p06.day59;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class Day59HttpCachingTest {

    private static final boolean SHARED = true;
    private static final boolean PRIVATE = false;

    @Nested
    @DisplayName("parsing")
    class Parsing {

        @Test
        void directives() {
            CacheControl control = CacheControl.parse("public, max-age=3600, s-maxage=86400");

            assertThat(control.isPublic()).isTrue();
            assertThat(control.maxAge()).contains(Duration.ofHours(1));
            assertThat(control.sharedMaxAge()).contains(Duration.ofDays(1));
            assertThat(control.noStore()).isFalse();
        }

        @Test
        @DisplayName("directive names are case-insensitive and whitespace is tolerated")
        void lenient() {
            CacheControl control = CacheControl.parse("  No-Cache ,  MAX-AGE=60 ");

            assertThat(control.noCache()).isTrue();
            assertThat(control.maxAge()).contains(Duration.ofSeconds(60));
        }

        @Test
        @DisplayName("unknown directives are ignored, not fatal - that is how HTTP evolves")
        void unknownDirectives() {
            CacheControl control = CacheControl.parse("max-age=60, immutable, surrogate-key=abc");

            assertThat(control.maxAge()).contains(Duration.ofSeconds(60));
        }

        @Test
        @DisplayName("a malformed max-age is ignored rather than throwing")
        void malformedValue() {
            assertThat(CacheControl.parse("max-age=abc").maxAge()).isEmpty();
        }

        @Test
        void emptyHeader() {
            CacheControl control = CacheControl.parse("");

            assertThat(control.maxAge()).isEmpty();
            assertThat(control.noStore()).isFalse();
        }

        @Test
        @DisplayName("formats in a fixed order")
        void formatting() {
            assertThat(CacheControl.publicFor(Duration.ofHours(1)).format())
                    .isEqualTo("public, max-age=3600");
            assertThat(CacheControl.neverStore().format()).isEqualTo("no-store");
            assertThat(new CacheControl(false, true, true, false,
                    Optional.of(Duration.ofSeconds(60)), Optional.empty(), true).format())
                    .isEqualTo("no-cache, private, max-age=60, must-revalidate");
        }
    }

    @Nested
    @DisplayName("storability")
    class Storability {

        @Test
        @DisplayName("no-store means nobody stores it")
        void noStore() {
            assertThat(HttpCachePolicy.isStorable(CacheControl.neverStore(), SHARED)).isFalse();
            assertThat(HttpCachePolicy.isStorable(CacheControl.neverStore(), PRIVATE)).isFalse();
        }

        @Test
        @DisplayName("private: the browser may keep it, a CDN may not")
        void privateResponses() {
            CacheControl control = CacheControl.parse("private, max-age=600");

            assertThat(HttpCachePolicy.isStorable(control, PRIVATE))
                    .as("the user's own browser may keep the user's own page")
                    .isTrue();
            assertThat(HttpCachePolicy.isStorable(control, SHARED))
                    .as("""
                            This directive stands between a per-user response and a CDN serving it
                            to everybody. Getting it wrong is a data leak, not a slow page.""")
                    .isFalse();
        }

        @Test
        void publicResponses() {
            CacheControl control = CacheControl.publicFor(Duration.ofHours(1));

            assertThat(HttpCachePolicy.isStorable(control, SHARED)).isTrue();
            assertThat(HttpCachePolicy.isStorable(control, PRIVATE)).isTrue();
        }
    }

    @Nested
    @DisplayName("freshness")
    class FreshnessRules {

        private final CacheControl oneHour = CacheControl.publicFor(Duration.ofHours(1));

        @Test
        void freshWhileYoung() {
            assertThat(HttpCachePolicy.evaluate(oneHour, Duration.ofMinutes(30), SHARED))
                    .isEqualTo(Freshness.FRESH);
        }

        @Test
        @DisplayName("at exactly max-age it is no longer fresh")
        void boundary() {
            assertThat(HttpCachePolicy.evaluate(oneHour, Duration.ofHours(1), SHARED))
                    .isEqualTo(Freshness.MUST_REVALIDATE);
        }

        @Test
        void staleAfterMaxAge() {
            assertThat(HttpCachePolicy.evaluate(oneHour, Duration.ofHours(2), SHARED))
                    .isEqualTo(Freshness.MUST_REVALIDATE);
        }

        @Test
        @DisplayName("no-cache revalidates however young the entry is")
        void noCacheAlwaysRevalidates() {
            CacheControl control = CacheControl.parse("no-cache, max-age=3600");

            assertThat(HttpCachePolicy.evaluate(control, Duration.ofSeconds(1), SHARED))
                    .as("no-cache means 'check first', not 'do not store'")
                    .isEqualTo(Freshness.MUST_REVALIDATE);
        }

        @Test
        @DisplayName("s-maxage overrides max-age, but only for shared caches")
        void sharedMaxAgeWins() {
            CacheControl control = CacheControl.parse("public, max-age=60, s-maxage=86400");
            Duration tenMinutes = Duration.ofMinutes(10);

            assertThat(HttpCachePolicy.evaluate(control, tenMinutes, SHARED))
                    .as("the CDN gets a day")
                    .isEqualTo(Freshness.FRESH);
            assertThat(HttpCachePolicy.evaluate(control, tenMinutes, PRIVATE))
                    .as("the browser only gets a minute")
                    .isEqualTo(Freshness.MUST_REVALIDATE);
        }

        @Test
        @DisplayName("no freshness information means revalidate - absence is not permission")
        void noLifetime() {
            assertThat(HttpCachePolicy.evaluate(CacheControl.parse("public"), Duration.ZERO, SHARED))
                    .isEqualTo(Freshness.MUST_REVALIDATE);
        }

        @Test
        @DisplayName("must-revalidate turns stale into unusable")
        void mustRevalidate() {
            CacheControl control = CacheControl.parse("public, max-age=60, must-revalidate");

            assertThat(HttpCachePolicy.evaluate(control, Duration.ofSeconds(30), SHARED))
                    .isEqualTo(Freshness.FRESH);
            assertThat(HttpCachePolicy.evaluate(control, Duration.ofMinutes(5), SHARED))
                    .isEqualTo(Freshness.NOT_USABLE);
        }

        @Test
        void unstorableIsNeverUsable() {
            assertThat(HttpCachePolicy.evaluate(CacheControl.neverStore(), Duration.ZERO, SHARED))
                    .isEqualTo(Freshness.NOT_USABLE);
        }
    }

    @Nested
    @DisplayName("revalidation")
    class Revalidation {

        @Test
        @DisplayName("a matching ETag means 304 - a round trip, but no body")
        void matchingEtag() {
            assertThat(HttpCachePolicy.isNotModified("\"abc123\"", "\"abc123\"")).isTrue();
        }

        @Test
        void changedEtag() {
            assertThat(HttpCachePolicy.isNotModified("\"abc123\"", "\"def456\"")).isFalse();
        }

        @Test
        @DisplayName("no If-None-Match means the client has nothing to compare")
        void missingHeader() {
            assertThat(HttpCachePolicy.isNotModified(null, "\"abc123\"")).isFalse();
        }
    }

    @Test
    @DisplayName("age is measured from when the response was stored")
    void ageCalculation() {
        Instant stored = Instant.parse("2026-03-01T12:00:00Z");
        Instant now = Instant.parse("2026-03-01T12:45:00Z");

        assertThat(HttpCachePolicy.age(stored, now)).isEqualTo(Duration.ofMinutes(45));
    }
}
