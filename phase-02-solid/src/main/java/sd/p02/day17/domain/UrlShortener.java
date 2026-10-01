package sd.p02.day17.domain;

import java.time.Clock;
import java.util.Optional;

/**
 * TODO(day17): the domain service - the hexagon itself.
 *
 * <p>Constructor takes {@link LinkRepository}, {@link CodeGenerator} and a
 * {@link java.time.Clock}. All three are injected. The clock matters as much as the other
 * two: {@code Instant.now()} inside a domain object makes time untestable, and time is a
 * dependency exactly like a database is.
 *
 * <p>Implement:
 * <ul>
 *   <li>{@code shorten(targetUrl)} - reject anything not starting {@code http://} or
 *       {@code https://} with {@code IllegalArgumentException}. If this URL was already
 *       shortened, return the EXISTING link rather than minting a second code. Otherwise
 *       generate a code, save, and return the new link.</li>
 *   <li>{@code resolve(code)} - the target URL, or empty.</li>
 * </ul>
 *
 * <p>That idempotency rule is a business decision, and notice where it lives: here, in the
 * domain, not in a repository and not in a controller. Business rules belong inside the
 * hexagon. Everything outside it is a detail about how the world reaches them.
 *
 * <p>The constraint for today: this file, and every file in this package, may import NOTHING
 * from the adapter package. The test enforces it.
 */
public final class UrlShortener {

    LinkRepository repository;
    CodeGenerator codeGenerator;
    Clock clock;


    public UrlShortener(LinkRepository repository, CodeGenerator codeGenerator, Clock clock) {
        this.repository = repository;
        this.codeGenerator = codeGenerator;
        this.clock = clock;
    }

    public ShortLink shorten(String targetUrl) {
        if (!targetUrl.startsWith("http://") && !targetUrl.startsWith("https://")) {
            throw new IllegalArgumentException("Invalid URL: " + targetUrl);
        }
        Optional<ShortLink> existingLink = repository.findByTargetUrl(targetUrl);
        if (existingLink.isPresent()) {
            return existingLink.get();
        } else {
            String code = codeGenerator.nextCode();
            ShortLink newLink = new ShortLink(code, targetUrl, clock.instant());
            repository.save(newLink);
            return newLink; 
        }
    }

    public Optional<String> resolve(String code) {
        Optional<ShortLink> link = repository.findByCode(code);
        return link.map(ShortLink::targetUrl);
    }
}
