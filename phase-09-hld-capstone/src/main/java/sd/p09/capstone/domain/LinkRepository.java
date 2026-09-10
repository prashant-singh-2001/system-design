package sd.p09.capstone.domain;

import java.util.Optional;

/** The driven port for storage. Day 17's hexagonal architecture, at capstone scale. */
public interface LinkRepository {

    void save(ShortLink link);

    Optional<ShortLink> findByCode(String code);

    Optional<ShortLink> findByTargetUrl(String targetUrl);

    /** Records a click. Returns the new total, or empty if the code is unknown. */
    Optional<Long> incrementClicks(String code);
}
