package sd.p02.day14;

import java.util.List;
import java.util.Optional;

/**
 * GIVEN - the fat interface. The before-picture.
 *
 * <p>Twelve methods serving three completely different audiences: request handlers that only
 * read, the signup flow that writes, and a nightly maintenance job. Every consumer is forced
 * to depend on all twelve.
 *
 * <p>The concrete damage:
 * <ul>
 *   <li>A read-only cache must implement {@code vacuum()}. It has nothing sensible to put
 *       there, so it throws {@code UnsupportedOperationException} - and you have just built
 *       today's Liskov violation as well.</li>
 *   <li>A test double for a read path has to stub twelve methods to exercise one.</li>
 *   <li>Adding an admin method recompiles and re-tests every consumer, including the ones
 *       that will never call it.</li>
 *   <li>You cannot express "this component may read but must not write" in the type system,
 *       so that rule lives only in code review.</li>
 * </ul>
 *
 * <p>Interface Segregation says no client should be forced to depend on methods it does not
 * use. That last bullet is the real prize: after the split, "read-only" becomes a compile-time
 * guarantee instead of a convention.
 */
public interface LegacyUserRepository {

    Optional<User> findById(String id);

    Optional<User> findByEmail(String email);

    List<User> findAllActive();

    long count();

    void save(User user);

    void delete(String id);

    void updateEmail(String id, String email);

    void bulkImport(List<User> users);

    void reindex();

    void purgeDeleted();

    void rebuildStatistics();

    void vacuum();
}
