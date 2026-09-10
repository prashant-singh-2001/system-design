package sd.p02.day15;

import java.util.List;

/**
 * GIVEN - the port. Exactly one method, and note where it LIVES: beside the domain logic that
 * needs it, not beside the JDBC code that implements it. The domain states its requirement;
 * infrastructure conforms.
 *
 * <p>Note also what is absent from the signature: no {@code Connection}, no {@code SQLException},
 * no result set, no pagination cursor. If a persistence concept leaks into this interface, the
 * inversion is incomplete and you will feel it the first time you try to back it with an HTTP
 * API instead.
 *
 * <p>Because it has exactly one abstract method, any lambda returning {@code List<Sale>} is a
 * complete implementation - that is what makes the fake in the test a one-liner.
 */
public interface SalesDataSource {

    List<Sale> findSales();
}
