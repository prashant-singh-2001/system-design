package sd.p09.capstone.domain;

import java.time.Instant;

public record ShortLink(String code, String targetUrl, Instant createdAt, long clicks) {

    public ShortLink withClicks(long newClicks) {
        return new ShortLink(code, targetUrl, createdAt, newClicks);
    }
}
