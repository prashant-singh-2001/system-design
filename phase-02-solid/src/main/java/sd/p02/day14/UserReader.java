package sd.p02.day14;

/**
 * TODO(day14): the read-only role. Declare exactly these four methods, copied verbatim from
 * {@link LegacyUserRepository}:
 *
 * <pre>
 *   Optional&lt;User&gt; findById(String id);
 *   Optional&lt;User&gt; findByEmail(String email);
 *   List&lt;User&gt; findAllActive();
 *   long count();
 * </pre>
 *
 * <p>Segregate by ROLE - who needs this set of operations - not by data type. That is the
 * distinction people get wrong: the question is not "what can a user repository do", it is
 * "what does each caller actually need".
 */
public interface UserReader {
}
