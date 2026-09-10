package sd.p02.day19;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Day19ResultTest {

    @Test
    @DisplayName("success and failure carry their payloads")
    void construction() {
        Result<Integer, String> ok = Result.success(42);
        Result<Integer, String> bad = Result.failure("boom");

        assertThat(ok.isSuccess()).isTrue();
        assertThat(bad.isSuccess()).isFalse();
        assertThat(ok).isInstanceOf(Result.Success.class);
        assertThat(bad).isInstanceOf(Result.Failure.class);
    }

    @Test
    @DisplayName("map transforms a success and leaves a failure alone")
    void map() {
        Result<Integer, String> ok = Result.success(21);
        Result<Integer, String> bad = Result.failure("boom");

        assertThat(ok.map(n -> n * 2).orElse(0)).isEqualTo(42);
        assertThat(bad.map(n -> n * 2).orElse(-1)).isEqualTo(-1);
        assertThat(bad.map(n -> n * 2)).isEqualTo(Result.failure("boom"));
    }

    @Test
    @DisplayName("flatMap chains operations that can themselves fail")
    void flatMap() {
        Result<Integer, String> parsed = Result.<String, String>success("21")
                .flatMap(Day19ResultTest::parseNumber)
                .map(n -> n * 2);

        assertThat(parsed.orElse(0)).isEqualTo(42);

        Result<Integer, String> failed = Result.<String, String>success("not-a-number")
                .flatMap(Day19ResultTest::parseNumber)
                .map(n -> n * 2);

        assertThat(failed).isEqualTo(Result.failure("not a number: not-a-number"));
    }

    @Test
    @DisplayName("a failure short-circuits everything downstream")
    void shortCircuits() {
        AtomicInteger callsAfterFailure = new AtomicInteger();

        Result<Integer, String> result = Result.<Integer, String>failure("boom")
                .map(n -> {
                    callsAfterFailure.incrementAndGet();
                    return n * 2;
                })
                .flatMap(n -> {
                    callsAfterFailure.incrementAndGet();
                    return Result.success(n);
                });

        assertThat(result.isSuccess()).isFalse();
        assertThat(callsAfterFailure)
                .as("nothing after the first failure should run")
                .hasValue(0);
    }

    @Test
    @DisplayName("orElseThrow is the boundary where you convert back to an exception")
    void orElseThrow() {
        assertThat(Result.<String, String>success("fine").orElseThrow()).isEqualTo("fine");

        assertThatThrownBy(() -> Result.<String, String>failure("boom").orElseThrow())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("boom");
    }

    @Test
    @DisplayName("a valid signup comes back as a success")
    void validSignup() {
        Signup signup = new Signup("a@example.com", "alice", 30);

        assertThat(new SignupValidator().validate(signup).orElseThrow()).isEqualTo(signup);
    }

    @Test
    @DisplayName("THE point: every problem is reported at once, not just the first")
    void reportsAllFailures() {
        Result<Signup, List<String>> result =
                new SignupValidator().validate(new Signup("no-at-sign", "ab", 12));

        assertThat(result.isSuccess()).isFalse();
        assertThat(((Result.Failure<Signup, List<String>>) result).error())
                .as("an exception would have reported one of these and abandoned the other two")
                .containsExactly("email is invalid", "username is too short", "must be 18 or older");
    }

    @Test
    @DisplayName("one bad field reports exactly one error")
    void reportsSingleFailure() {
        Result<Signup, List<String>> result =
                new SignupValidator().validate(new Signup("a@example.com", "alice", 15));

        assertThat(((Result.Failure<Signup, List<String>>) result).error())
                .containsExactly("must be 18 or older");
    }

    @Test
    @DisplayName("sealed means a switch over Result needs no default branch")
    void exhaustiveSwitch() {
        // If Result were not sealed, this switch would not compile without a default.
        // The compiler knows there are exactly two cases - and would break this code if
        // somebody ever added a third, which is exactly the safety net you want.
        Result<Integer, String> result = Result.success(7);

        String description = switch (result) {
            case Result.Success<Integer, String> s -> "ok: " + s.value();
            case Result.Failure<Integer, String> f -> "err: " + f.error();
        };

        assertThat(description).isEqualTo("ok: 7");
    }

    private static Result<Integer, String> parseNumber(String raw) {
        try {
            return Result.success(Integer.parseInt(raw));
        } catch (NumberFormatException e) {
            return Result.failure("not a number: " + raw);
        }
    }
}
