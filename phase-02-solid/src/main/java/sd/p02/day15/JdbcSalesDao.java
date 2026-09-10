package sd.p02.day15;

import java.util.List;

/**
 * GIVEN - stands in for a real JDBC DAO. It needs a live database, and says so loudly.
 *
 * <p>Nothing about this class is wrong. Somebody has to talk to Postgres. The mistake is
 * where it gets INSTANTIATED - see {@link LegacyReportService}.
 */
public final class JdbcSalesDao {

    public List<Sale> querySales() {
        throw new IllegalStateException(
                "no database configured - start Postgres, or inject a different data source");
    }
}
