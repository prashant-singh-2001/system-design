package sd.p02.day17.adapter;

import sd.p02.day17.domain.LinkRepository;
import sd.p02.day17.domain.ShortLink;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * TODO(day17): an adapter. It knows about the domain; the domain knows nothing about it.
 *
 * <p>Back it with a {@code Map<String, ShortLink>} keyed by code. {@code findByTargetUrl} may
 * scan - correctness first, and the inefficiency is a useful thing to notice: in Phase 5 you
 * will replace this with a real index and see exactly which access pattern forced it.
 *
 * <p>The dependency arrow points INWARD (adapter to domain) and never outward. Every arrow in
 * a hexagonal architecture points at the domain. That single rule is what lets you swap
 * Postgres for DynamoDB without the domain noticing.
 */
public final class InMemoryLinkRepository implements LinkRepository {

    Map<String, ShortLink> linksByCode = new HashMap<>();

    @Override
    public void save(ShortLink link) {
        linksByCode.put(link.code(), link);
    }

    @Override
    public Optional<ShortLink> findByCode(String code) {
        return Optional.ofNullable(linksByCode.get(code));
    }

    @Override
    public Optional<ShortLink> findByTargetUrl(String targetUrl) {
        for (ShortLink link : linksByCode.values()) {
            if (link.targetUrl().equals(targetUrl)) {
                return Optional.of(link);
            }
        }
        return Optional.empty();
    }
}
