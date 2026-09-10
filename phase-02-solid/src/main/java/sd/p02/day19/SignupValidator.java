package sd.p02.day19;

import java.util.List;

/**
 * TODO(day19): validation that reports EVERY problem, not just the first.
 *
 * <p>Rules:
 * <ul>
 *   <li>email must be non-null and contain {@code @} -&gt; {@code "email is invalid"}</li>
 *   <li>username must be at least 3 characters -&gt; {@code "username is too short"}</li>
 *   <li>age must be at least 18 -&gt; {@code "must be 18 or older"}</li>
 * </ul>
 *
 * <p>Collect the failures in the order listed above and return
 * {@code Result.failure(errors)} if there are any, otherwise {@code Result.success(signup)}.
 *
 * <p>This is the case exceptions genuinely cannot serve. A throw reports one problem and
 * abandons the rest, so a user with three bad fields fixes one, resubmits, and discovers the
 * next - three round trips for one form. Errors as values fix that, and the fix is structural
 * rather than a workaround.
 */
public final class SignupValidator {

    public Result<Signup, List<String>> validate(Signup signup) {
        throw new UnsupportedOperationException("TODO(day19): accumulate every failure");
    }
}
