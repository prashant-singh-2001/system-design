package sd.p02.day17.adapter;

import sd.p02.day17.domain.CodeGenerator;

/**
 * TODO(day17): base-62 encode an incrementing counter.
 *
 * <p>Alphabet: {@code 0-9A-Za-z}. Start the counter at 0, so the first three codes are
 * {@code "0"}, {@code "1"}, {@code "2"}. Use an {@code AtomicLong} - Day 5 already told you why.
 *
 * <p>Base 62 fits 62^7 (about 3.5 trillion) values into seven characters. Worth noting the
 * trade you are making: sequential codes are compact and collision-free, but they are also
 * guessable and they leak your total volume to anyone who can read one. Day 82 revisits this
 * properly.
 */
public final class SequentialCodeGenerator implements CodeGenerator {

    @Override
    public String nextCode() {
        throw new UnsupportedOperationException("TODO(day17): base-62 encode the next counter value");
    }
}
