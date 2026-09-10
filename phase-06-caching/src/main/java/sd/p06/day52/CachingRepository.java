package sd.p06.day52;

import java.util.Optional;

/** The four classic caching strategies share this shape; only their timing differs. */
public interface CachingRepository {

    Optional<String> get(String key);

    void put(String key, String value);

    /** Push any deferred writes to the database. Only write-behind has anything to do. */
    default void flush() {
    }

    String strategy();
}
