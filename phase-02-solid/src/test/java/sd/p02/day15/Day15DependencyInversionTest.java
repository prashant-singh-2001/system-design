package sd.p02.day15;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Day15DependencyInversionTest {

    private static final List<Sale> SALES = List.of(
            new Sale("EMEA", 5_000),
            new Sale("EMEA", 3_000),
            new Sale("APAC", 9_000),
            new Sale("AMER", 1_000),
            new Sale("AMER", 1_000));

    @Test
    @DisplayName("the before-picture: the legacy service cannot run without a database")
    void legacyServiceIsUntestable() {
        assertThatThrownBy(() -> new LegacyReportService().topRegions(3))
                .as("the reporting arithmetic is pure, yet you cannot reach it")
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no database configured");
    }

    @Test
    @DisplayName("the port declares only what the domain needs")
    void portIsMinimal() {
        assertThat(Arrays.stream(SalesDataSource.class.getDeclaredMethods()).map(Method::getName))
                .containsExactly("findSales");

        assertThat(SalesDataSource.class.getDeclaredMethods()[0].getReturnType())
                .as("the port speaks in domain types, never in JDBC types")
                .isEqualTo(List.class);
    }

    @Test
    @DisplayName("the same logic, now testable in microseconds with no infrastructure")
    void reportRunsAgainstAFake() {
        // The entire test double. This one line is what dependency inversion bought.
        SalesDataSource fake = () -> SALES;

        assertThat(new ReportService(fake).topRegions(2))
                .containsExactly(
                        new RegionTotal("APAC", 9_000),
                        new RegionTotal("EMEA", 8_000));
    }

    @Test
    @DisplayName("ties break on region name, ascending")
    void deterministicOrdering() {
        SalesDataSource fake = () -> List.of(
                new Sale("ZULU", 1_000),
                new Sale("ALPHA", 1_000));

        assertThat(new ReportService(fake).topRegions(10))
                .extracting(RegionTotal::region)
                .containsExactly("ALPHA", "ZULU");
    }

    @Test
    @DisplayName("limit is respected, and asking for more than exists is fine")
    void limitIsApplied() {
        SalesDataSource fake = () -> SALES;

        assertThat(new ReportService(fake).topRegions(1)).hasSize(1);
        assertThat(new ReportService(fake).topRegions(99)).hasSize(3);
        assertThat(new ReportService(() -> List.of()).topRegions(5)).isEmpty();
    }

    @Test
    @DisplayName("the domain class knows nothing about persistence")
    void domainHasNoInfrastructureDependency() {
        boolean mentionsJdbc = Arrays.stream(ReportService.class.getDeclaredFields())
                .anyMatch(f -> f.getType().getName().toLowerCase().contains("jdbc")
                        || f.getType().getName().startsWith("java.sql"));

        assertThat(mentionsJdbc)
                .as("ReportService should depend on the port, never on a concrete DAO")
                .isFalse();
    }
}
