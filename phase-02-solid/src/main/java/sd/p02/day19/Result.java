package sd.p02.day19;

import java.util.function.Function;

/**
 * TODO(day19): errors as VALUES rather than as control flow.
 *
 * <p>Exceptions are excellent for the genuinely exceptional - a disk failing, a bug, a
 * violated invariant. They are a poor fit for outcomes you fully expect: a malformed email,
 * a card declined, a name too long. Three reasons:
 *
 * <ul>
 *   <li><b>They are invisible in the signature.</b> {@code User parse(String)} does not tell
 *       you it can fail. A {@code Result<User, String>} does, and the compiler makes you deal
 *       with it.</li>
 *   <li><b>They stop at the first problem.</b> A form with four bad fields should report four
 *       errors, not throw on the first one. Values accumulate; throws do not.</li>
 *   <li><b>They are expensive and they jump.</b> Filling in a stack trace costs real time on
 *       a hot path, and a throw transfers control somewhere you cannot see from here.</li>
 * </ul>
 *
 * <p>Implement this as a SEALED interface with two records - {@code Success} and
 * {@code Failure}. Sealed means the compiler knows the complete set of cases, so a switch
 * over a Result needs no default branch and will fail to compile if a third case is ever
 * added. That is the type system doing the work exhaustiveness checks used to require
 * discipline for.
 *
 * <p>Implement:
 * <ul>
 *   <li>{@code success(value)} / {@code failure(error)} factories</li>
 *   <li>{@code isSuccess()}</li>
 *   <li>{@code map(fn)} - transform the value; a failure passes through untouched</li>
 *   <li>{@code flatMap(fn)} - chain another operation that can itself fail. This is what
 *       gives you short-circuiting: the first failure in a chain skips everything after it</li>
 *   <li>{@code orElse(fallback)}</li>
 *   <li>{@code orElseThrow()} - for the boundary where you finally do want an exception;
 *       throw {@code IllegalStateException} whose message is the error's {@code toString()}</li>
 * </ul>
 *
 * <p>The cost, stated honestly: Result is viral. Once a method returns one, its callers must
 * handle it, and the style spreads outward. That is a real design commitment, not a free win -
 * which is why the usual advice is to use Result for expected domain outcomes and keep
 * exceptions for genuine faults.
 */
public sealed interface Result<T, E> permits Result.Success, Result.Failure {

    record Success<T, E>(T value) implements Result<T, E> {
    }

    record Failure<T, E>(E error) implements Result<T, E> {
    }

    static <T, E> Result<T, E> success(T value) {
        throw new UnsupportedOperationException("TODO(day19): implement success");
    }

    static <T, E> Result<T, E> failure(E error) {
        throw new UnsupportedOperationException("TODO(day19): implement failure");
    }

    default boolean isSuccess() {
        throw new UnsupportedOperationException("TODO(day19): implement isSuccess");
    }

    default <R> Result<R, E> map(Function<T, R> mapper) {
        throw new UnsupportedOperationException("TODO(day19): implement map");
    }

    default <R> Result<R, E> flatMap(Function<T, Result<R, E>> mapper) {
        throw new UnsupportedOperationException("TODO(day19): implement flatMap");
    }

    default T orElse(T fallback) {
        throw new UnsupportedOperationException("TODO(day19): implement orElse");
    }

    default T orElseThrow() {
        throw new UnsupportedOperationException("TODO(day19): implement orElseThrow");
    }
}
