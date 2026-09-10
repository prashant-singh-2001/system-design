package sd.p09.day89;

import sd.p09.capstone.domain.LinkRepository;
import sd.p09.capstone.domain.ShortLink;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/** GIVEN - an in-memory repository that can be switched to failing, for the resilience tests. */
final class FailingLinkRepository implements LinkRepository {

    private final Map<String, ShortLink> byCode = new HashMap<>();
    private final AtomicInteger calls = new AtomicInteger();
    private volatile boolean down;

    @Override
    public void save(ShortLink link) {
        guard();
        byCode.put(link.code(), link);
    }

    @Override
    public Optional<ShortLink> findByCode(String code) {
        guard();
        return Optional.ofNullable(byCode.get(code));
    }

    @Override
    public Optional<ShortLink> findByTargetUrl(String targetUrl) {
        guard();
        return byCode.values().stream()
                .filter(link -> link.targetUrl().equals(targetUrl))
                .findFirst();
    }

    @Override
    public Optional<Long> incrementClicks(String code) {
        guard();
        ShortLink link = byCode.get(code);
        if (link == null) {
            return Optional.empty();
        }
        ShortLink updated = link.withClicks(link.clicks() + 1);
        byCode.put(code, updated);
        return Optional.of(updated.clicks());
    }

    private void guard() {
        calls.incrementAndGet();
        if (down) {
            throw new IllegalStateException("database unavailable");
        }
    }

    void goDown() {
        down = true;
    }

    void recover() {
        down = false;
    }

    int calls() {
        return calls.get();
    }

    void resetCalls() {
        calls.set(0);
    }
}
