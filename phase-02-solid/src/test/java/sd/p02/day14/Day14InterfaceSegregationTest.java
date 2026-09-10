package sd.p02.day14;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * These assertions are reflective on purpose. ISP is a STRUCTURAL principle, so the test is a
 * structural one - an architecture fitness function, the same idea you will use again on Day 17.
 */
class Day14InterfaceSegregationTest {

    private static Set<String> methodNames(Class<?> type) {
        return Arrays.stream(type.getDeclaredMethods())
                .map(Method::getName)
                .collect(Collectors.toSet());
    }

    @Test
    @DisplayName("UserReader declares exactly the four read operations")
    void readerRole() {
        assertThat(methodNames(UserReader.class))
                .containsExactlyInAnyOrder("findById", "findByEmail", "findAllActive", "count");
    }

    @Test
    @DisplayName("UserWriter declares exactly the three write operations")
    void writerRole() {
        assertThat(methodNames(UserWriter.class))
                .containsExactlyInAnyOrder("save", "delete", "updateEmail");
    }

    @Test
    @DisplayName("UserAdmin declares exactly the five maintenance operations")
    void adminRole() {
        assertThat(methodNames(UserAdmin.class))
                .containsExactlyInAnyOrder("bulkImport", "reindex", "purgeDeleted",
                        "rebuildStatistics", "vacuum");
    }

    @Test
    @DisplayName("UserRepository composes the roles and adds nothing of its own")
    void repositoryComposesRoles() {
        assertThat(UserReader.class.isAssignableFrom(UserRepository.class)).isTrue();
        assertThat(UserWriter.class.isAssignableFrom(UserRepository.class)).isTrue();
        assertThat(UserAdmin.class.isAssignableFrom(UserRepository.class)).isTrue();

        assertThat(methodNames(UserRepository.class))
                .as("the full repository is a composition, not a new set of methods")
                .isEmpty();
    }

    @Test
    @DisplayName("the payoff: 'read-only' becomes a compile-time guarantee")
    void readOnlyIsEnforcedByTheTypeSystem() {
        assertThat(UserReader.class.isAssignableFrom(ReadOnlyUserCache.class))
                .as("the cache fills the reader role")
                .isTrue();
        assertThat(UserWriter.class.isAssignableFrom(ReadOnlyUserCache.class))
                .as("and is structurally incapable of writing - not by convention, by type")
                .isFalse();
        assertThat(UserAdmin.class.isAssignableFrom(ReadOnlyUserCache.class)).isFalse();
    }

    @Test
    @DisplayName("and the cache still works, with a fraction of the surface area")
    void cacheStillWorks() {
        ReadOnlyUserCache cache = new ReadOnlyUserCache(List.of(
                new User("1", "a@example.com", true),
                new User("2", "b@example.com", false)));

        assertThat(cache.findById("1")).map(User::email).contains("a@example.com");
        assertThat(cache.findByEmail("b@example.com")).map(User::id).contains("2");
        assertThat(cache.findAllActive()).hasSize(1);
        assertThat(cache.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("for contrast: the fat interface forces twelve methods on every consumer")
    void theBeforePicture() {
        assertThat(methodNames(LegacyUserRepository.class)).hasSize(12);
    }
}
