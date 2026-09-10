package sd.p03.day22;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Day22BuilderTest {

    @Test
    @DisplayName("every field is named at the call site, and defaults are sane")
    void buildsWithDefaults() {
        ServiceConfig config = ServiceConfig.builder()
                .host("payments.internal")
                .port(8080)
                .build();

        assertThat(config.host()).isEqualTo("payments.internal");
        assertThat(config.port()).isEqualTo(8080);
        assertThat(config.retries()).isZero();
        assertThat(config.tlsEnabled()).isFalse();
        assertThat(config.tags()).isEmpty();
    }

    @Test
    @DisplayName("a missing host is only detectable once the whole object is assembled")
    void missingHostFailsAtBuild() {
        assertThatThrownBy(() -> ServiceConfig.builder().port(8080).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("host");
    }

    @Test
    @DisplayName("a bad port is rejected the moment it is set, not at build()")
    void invalidPortFailsImmediately() {
        ServiceConfig.Builder builder = ServiceConfig.builder().host("svc");

        assertThatThrownBy(() -> builder.port(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> builder.port(70_000)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("timeout and connection count reject non-positive values")
    void invalidNumericFieldsAreRejected() {
        ServiceConfig.Builder builder = ServiceConfig.builder().host("svc").port(80);

        assertThatThrownBy(() -> builder.timeoutMs(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> builder.maxConnections(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> builder.retries(-1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("tls is off unless explicitly turned on")
    void tlsIsOptedIn() {
        ServiceConfig withTls = ServiceConfig.builder()
                .host("svc").port(443).tls("/etc/certs/svc.pem").build();

        assertThat(withTls.tlsEnabled()).isTrue();
        assertThat(withTls.certPath()).isEqualTo("/etc/certs/svc.pem");
    }

    @Test
    @DisplayName("tags accumulate, and the built config's map cannot be mutated from outside")
    void tagsAreCollectedAndImmutable() {
        ServiceConfig config = ServiceConfig.builder()
                .host("svc").port(80)
                .tag("team", "payments")
                .tag("tier", "critical")
                .build();

        assertThat(config.tags()).isEqualTo(Map.of("team", "payments", "tier", "critical"));
        assertThatThrownBy(() -> config.tags().put("new", "value"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("building twice from the same builder yields two independent configs")
    void builderIsReusableAfterBuild() {
        ServiceConfig.Builder builder = ServiceConfig.builder().host("svc").port(80).retries(1);

        ServiceConfig first = builder.build();
        ServiceConfig second = builder.retries(9).build();

        assertThat(first.retries())
                .as("mutating the builder after build() must not reach back into the first config")
                .isEqualTo(1);
        assertThat(second.retries()).isEqualTo(9);
    }

    @Test
    @DisplayName("PROTOTYPE: toBuilder() clones an existing config for a targeted change")
    void toBuilderClonesAndOverrides() {
        ServiceConfig prod = ServiceConfig.builder()
                .host("payments.internal")
                .port(8080)
                .timeoutMs(3_000)
                .maxConnections(10)
                .retries(1)
                .tag("env", "prod")
                .build();

        ServiceConfig staging = prod.toBuilder()
                .host("payments.staging")
                .retries(5)
                .build();

        assertThat(staging.host()).isEqualTo("payments.staging");
        assertThat(staging.retries()).isEqualTo(5);
        assertThat(staging.port())
                .as("every field NOT overridden must survive the clone unchanged")
                .isEqualTo(8080);
        assertThat(staging.timeoutMs()).isEqualTo(3_000);
        assertThat(staging.maxConnections()).isEqualTo(10);
        assertThat(staging.tags()).isEqualTo(Map.of("env", "prod"));

        assertThat(prod.host())
                .as("cloning must never mutate the original")
                .isEqualTo("payments.internal");
        assertThat(prod.retries()).isEqualTo(1);
    }
}
