package sd.p02.day17;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sd.p02.day17.adapter.InMemoryLinkRepository;
import sd.p02.day17.adapter.SequentialCodeGenerator;
import sd.p02.day17.domain.ShortLink;
import sd.p02.day17.domain.UrlShortener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Day17HexagonalTest {

    private static final Instant FIXED = Instant.parse("2026-01-15T10:00:00Z");

    private UrlShortener newShortener() {
        return new UrlShortener(
                new InMemoryLinkRepository(),
                new SequentialCodeGenerator(),
                Clock.fixed(FIXED, ZoneOffset.UTC));
    }

    @Test
    @DisplayName("shorten then resolve")
    void roundTrip() {
        UrlShortener shortener = newShortener();

        ShortLink link = shortener.shorten("https://example.com/a/very/long/path");

        assertThat(link.code()).isNotBlank();
        assertThat(shortener.resolve(link.code())).contains("https://example.com/a/very/long/path");
    }

    @Test
    @DisplayName("the injected clock makes time testable - no Instant.now() in the domain")
    void usesTheInjectedClock() {
        assertThat(newShortener().shorten("https://example.com").createdAt())
                .as("if this is 'now' rather than the fixed instant, the clock is not being used")
                .isEqualTo(FIXED);
    }

    @Test
    @DisplayName("shortening the same URL twice returns the same code")
    void idempotent() {
        UrlShortener shortener = newShortener();

        ShortLink first = shortener.shorten("https://example.com/same");
        ShortLink second = shortener.shorten("https://example.com/same");

        assertThat(second.code()).isEqualTo(first.code());
    }

    @Test
    @DisplayName("distinct URLs get distinct codes")
    void distinctUrls() {
        UrlShortener shortener = newShortener();

        assertThat(shortener.shorten("https://example.com/one").code())
                .isNotEqualTo(shortener.shorten("https://example.com/two").code());
    }

    @Test
    @DisplayName("an unknown code resolves to empty")
    void unknownCode() {
        assertThat(newShortener().resolve("nope")).isEmpty();
    }

    @Test
    @DisplayName("the URL rule is a domain rule, enforced in the domain")
    void rejectsNonHttpUrls() {
        UrlShortener shortener = newShortener();

        assertThatThrownBy(() -> shortener.shorten("ftp://example.com"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> shortener.shorten("javascript:alert(1)"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> shortener.shorten(""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("base-62 codes start at 0, 1, 2")
    void codeGeneration() {
        SequentialCodeGenerator generator = new SequentialCodeGenerator();

        assertThat(generator.nextCode()).isEqualTo("0");
        assertThat(generator.nextCode()).isEqualTo("1");
        assertThat(generator.nextCode()).isEqualTo("2");
    }

    // ------------------------------------------------------------------ fitness function

    @Test
    @DisplayName("ARCHITECTURE: nothing in the domain may reference the adapter layer")
    void domainDoesNotDependOnAdapters() throws IOException {
        Path domain = Path.of("src/main/java/sd/p02/day17/domain");

        try (Stream<Path> sources = Files.walk(domain)) {
            List<String> offenders = sources
                    .filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> {
                        try {
                            return Files.readString(p).contains("sd.p02.day17.adapter");
                        } catch (IOException e) {
                            throw new IllegalStateException("could not read " + p, e);
                        }
                    })
                    .map(p -> p.getFileName().toString())
                    .toList();

            assertThat(offenders)
                    .as("""
                            Every arrow points inward. The domain declares ports; adapters implement
                            them. The moment a domain class imports an adapter, you can no longer
                            swap infrastructure without touching business logic - which is the one
                            thing this architecture exists to prevent.""")
                    .isEmpty();
        }
    }
}
