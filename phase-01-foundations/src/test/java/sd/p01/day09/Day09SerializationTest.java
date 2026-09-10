package sd.p01.day09;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class Day09SerializationTest {

    static Stream<Codec> codecs() {
        return Stream.of(new JavaSerializationCodec(), new JsonCodec(), new BinaryCodec());
    }

    @ParameterizedTest(name = "{index}: round-trips without loss")
    @MethodSource("codecs")
    void roundTrips(Codec codec) {
        Event original = Event.sample();

        assertThat(codec.decode(codec.encode(original)))
                .as("%s must return exactly what it was given", codec.name())
                .isEqualTo(original);
    }

    @ParameterizedTest(name = "{index}: survives awkward characters")
    @MethodSource("codecs")
    void handlesAwkwardPayloads(Codec codec) {
        Event tricky = new Event(0, "quote\"type", -1,
                "line\break \"quoted\" and unicode: é中");

        assertThat(codec.decode(codec.encode(tricky)))
                .as("%s mangled quotes, backslashes or multi-byte characters", codec.name())
                .isEqualTo(tricky);
    }

    @Test
    @DisplayName("json is recognisably json")
    void jsonLooksLikeJson() {
        String encoded = new String(new JsonCodec().encode(Event.sample()), StandardCharsets.UTF_8);

        assertThat(encoded).startsWith("{").endsWith("}");
        assertThat(encoded).contains("\"id\"", "\"type\"", "\"timestampMillis\"", "\"payload\"");
        assertThat(encoded).contains("user.created");
    }

    @Test
    @DisplayName("binary < json < java serialization")
    void sizeOrdering() {
        Event event = Event.sample();

        int binary = new BinaryCodec().encode(event).length;
        int json = new JsonCodec().encode(event).length;
        int java = new JavaSerializationCodec().encode(event).length;

        System.out.printf("%n  %-22s %5d bytes%n", "binary", binary);
        System.out.printf("  %-22s %5d bytes  (%.1fx binary)%n", "json", json, (double) json / binary);
        System.out.printf("  %-22s %5d bytes  (%.1fx binary)%n%n", "java-serialization", java,
                (double) java / binary);

        assertThat(binary)
                .as("binary drops the field names entirely, so it should be smallest")
                .isLessThan(json);
        assertThat(json)
                .as("java serialization carries a class descriptor, so it should be largest")
                .isLessThan(java);
    }
}
