package sd.p02.day17.domain;

import java.util.Optional;

/**
 * A driven port: something the domain NEEDS.
 *
 * <p>It is declared here, in the domain, and implemented out in the adapter layer. That
 * direction is the whole idea - the domain writes the job description and infrastructure
 * applies for the job.
 */
public interface LinkRepository {

    void save(ShortLink link);

    Optional<ShortLink> findByCode(String code);

    Optional<ShortLink> findByTargetUrl(String targetUrl);
}
