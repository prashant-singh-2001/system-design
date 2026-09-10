package sd.p02.day14;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * GIVEN - a component that can ONLY read, and says so in its type.
 *
 * <p>Before the split this class had to implement {@code vacuum()}, {@code reindex()} and
 * nine other methods it has no business having. Look at how much smaller and more honest it
 * is once it only claims the role it fills.
 *
 * <p>It declares {@code implements UserReader}, so it will not compile until you have given
 * {@link UserReader} its four methods - which is exactly the feedback loop you want.
 */
public final class ReadOnlyUserCache implements UserReader {

    private final Map<String, User> snapshot;

    public ReadOnlyUserCache(List<User> users) {
        this.snapshot = users.stream()
                .collect(java.util.stream.Collectors.toUnmodifiableMap(User::id, u -> u));
    }

    public Optional<User> findById(String id) {
        return Optional.ofNullable(snapshot.get(id));
    }

    public Optional<User> findByEmail(String email) {
        return snapshot.values().stream()
                .filter(u -> u.email().equals(email))
                .findFirst();
    }

    public List<User> findAllActive() {
        return snapshot.values().stream().filter(User::active).toList();
    }

    public long count() {
        return snapshot.size();
    }
}
