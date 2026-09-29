package sd.p02.day15;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO(day15): the same reporting logic, with the dependency inverted.
 *
 * <p>Take a {@link SalesDataSource} as a constructor parameter and copy the grouping and
 * sorting logic across unchanged. Ties break on region name ascending; totals sort descending.
 *
 * <p>When you are done, this class will not mention JDBC, SQL or Postgres anywhere - and its
 * tests will run in microseconds with no Docker. Two consequences of the same change:
 * testability is not a separate benefit of DIP, it is the same benefit viewed from the test.
 */
public final class ReportService {
    List<Sale> sales;
    public ReportService(SalesDataSource dataSource) {
        this.sales = dataSource.findSales();
    }

    public List<RegionTotal> topRegions(int limit) {
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
