package sd.p02.day15;

import java.util.List;

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

    public ReportService(SalesDataSource dataSource) {
        throw new UnsupportedOperationException("TODO(day15): store the injected data source");
    }

    public List<RegionTotal> topRegions(int limit) {
        throw new UnsupportedOperationException("TODO(day15): group, sum, sort, limit");
    }
}
