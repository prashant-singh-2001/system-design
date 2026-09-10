package sd.p09.capstone.domain;

import java.time.Clock;
import java.util.Optional;

/**
 * TODO(day88): the capstone domain service - the hexagon.
 *
 * <p>This is Day 17's URL shortener again, and that is deliberate: you now know what every port
 * behind it will really be. The repository will be Postgres wrapped in a cache wrapped in a
 * circuit breaker. The code generator's strategy is a decision you can now defend with Day 82's
 * arithmetic. And this class will not know about any of it.
 *
 * <p>Implement:
 * <ul>
 *   <li>{@code shorten} - reject anything not {@code http://} or {@code https://}. If this URL was
 *       already shortened, return the existing link (idempotency as a business rule, Day 74's
 *       idea expressed in the domain). Otherwise generate, save and return.</li>
 *   <li>{@code resolve} - the target URL for a code, or empty.</li>
 *   <li>{@code recordClick} - increment and return the new total, or empty for an unknown code.</li>
 * </ul>
 *
 * <p>The constraint, enforced by a test: this package may import nothing from
 * {@code sd.p09.capstone.adapter}. Every arrow points inward.
 */
public final class LinkService {

    private final LinkRepository repository;
    private final CodeGenerator codeGenerator;
    private final Clock clock;

    public LinkService(LinkRepository repository, CodeGenerator codeGenerator, Clock clock) {
        this.repository = repository;
        this.codeGenerator = codeGenerator;
        this.clock = clock;
    }

    public ShortLink shorten(String targetUrl) {
        throw new UnsupportedOperationException("TODO(day88): validate, dedupe, generate, save");
    }

    public Optional<String> resolve(String code) {
        throw new UnsupportedOperationException("TODO(day88): look up the target");
    }

    public Optional<Long> recordClick(String code) {
        throw new UnsupportedOperationException("TODO(day88): increment and report the total");
    }
}
