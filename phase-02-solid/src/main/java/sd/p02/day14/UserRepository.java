package sd.p02.day14;

/**
 * TODO(day14): the full repository - extend all three roles and declare nothing of its own.
 *
 * <pre>
 *   public interface UserRepository extends UserReader, UserWriter, UserAdmin { }
 * </pre>
 *
 * <p>Implementations that genuinely do everything still implement one interface, so nothing
 * gets harder. But every CONSUMER can now depend on the narrowest role it needs, and a
 * component that takes a {@code UserReader} is provably incapable of writing.
 */
public interface UserRepository {
}
