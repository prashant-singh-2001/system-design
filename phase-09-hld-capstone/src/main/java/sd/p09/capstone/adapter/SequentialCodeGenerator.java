package sd.p09.capstone.adapter;

import sd.p09.capstone.domain.CodeGenerator;

import java.util.concurrent.atomic.AtomicLong;

/**
 * GIVEN - base-62 over a counter, offset so codes start at a realistic length.
 *
 * <p>Day 82 walked through the trade: this is the densest option, collision-free, and it leaks
 * your volume to anyone who reads a code. Your capstone design document should say which strategy
 * you would actually ship and why.
 */
public final class SequentialCodeGenerator implements CodeGenerator {

    private static final String ALPHABET =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private final AtomicLong counter;

    public SequentialCodeGenerator() {
        this(1_000_000_000L);
    }

    public SequentialCodeGenerator(long start) {
        this.counter = new AtomicLong(start);
    }

    @Override
    public String nextCode() {
        long value = counter.getAndIncrement();
        StringBuilder code = new StringBuilder();
        while (value > 0) {
            code.insert(0, ALPHABET.charAt((int) (value % 62)));
            value /= 62;
        }
        return code.isEmpty() ? "0" : code.toString();
    }
}
