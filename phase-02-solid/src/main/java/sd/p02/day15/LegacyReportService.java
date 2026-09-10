package sd.p02.day15;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GIVEN, and untestable. The before-picture.
 *
 * <p>One word is the problem: {@code new}. The business logic below - grouping and summing -
 * is pure arithmetic with no need for a database. But because the service constructs its own
 * dependency, you cannot exercise that arithmetic without a live Postgres.
 *
 * <p>The dependency points the wrong way. A high-level policy (how we report on sales) depends
 * on a low-level detail (that sales live in a relational database, reached over JDBC). Change
 * the storage and this class changes, even though the reporting rules did not.
 *
 * <p>Dependency Inversion: high-level modules should not depend on low-level modules; both
 * should depend on abstractions. Crucially, the abstraction is owned by the HIGH-level module -
 * the domain declares what it needs, and the infrastructure conforms. That ownership direction
 * is the part people miss, and it is what makes it "inversion" rather than merely "indirection".
 */
public final class LegacyReportService {

    private final JdbcSalesDao dao = new JdbcSalesDao();     // <-- the whole problem

    public List<RegionTotal> topRegions(int limit) {
        List<Sale> sales = dao.querySales();

        Map<String, Long> byRegion = new HashMap<>();
        for (Sale sale : sales) {
            byRegion.merge(sale.region(), sale.amountCents(), Long::sum);
        }

        List<RegionTotal> totals = new ArrayList<>();
        byRegion.forEach((region, total) -> totals.add(new RegionTotal(region, total)));
        totals.sort(Comparator.comparingLong(RegionTotal::totalCents).reversed()
                .thenComparing(RegionTotal::region));

        return totals.size() <= limit ? totals : new ArrayList<>(totals.subList(0, limit));
    }
}
