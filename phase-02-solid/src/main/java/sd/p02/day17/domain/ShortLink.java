package sd.p02.day17.domain;

import java.time.Instant;

/** A domain type. Note the imports: java.time only. Nothing about HTTP, SQL or Redis. */
public record ShortLink(String code, String targetUrl, Instant createdAt) {
}
