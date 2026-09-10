package sd.p02.day13;

import java.util.Optional;

/**
 * A key-value store, with its contract written down.
 *
 * <p>The contract - every implementation must honour all of it:
 * <ol>
 *   <li>After {@code put(k, v)} returns normally, an immediate {@code get(k)} returns {@code v}.</li>
 *   <li>{@code put} on an existing key overwrites the value.</li>
 *   <li>{@code get} of a key that was never stored returns {@code Optional.empty()}.</li>
 *   <li>{@code size()} is the number of entries currently retrievable.</li>
 * </ol>
 *
 * <p>Note what the contract deliberately does NOT promise: that an old key stays retrievable
 * forever. An implementation is free to evict. What it may never do is accept a {@code put}
 * and then not have the value.
 *
 * <p>That distinction is the whole of the Liskov Substitution Principle. A subtype may do
 * MORE than the supertype promises. It may never do LESS. Get the contract right and
 * substitutability follows; leave it vague and every implementation invents its own rules.
 */
public interface KeyValueStore {

    void put(String key, String value);

    Optional<String> get(String key);

    int size();
}
