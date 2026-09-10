package sd.p03.day21;

import java.nio.charset.StandardCharsets;

/**
 * TODO(day21): FNV-1a, a real, widely-used non-cryptographic hash - the same family used inside
 * several production sharding schemes. Simple enough to implement from the spec in ten lines,
 * which is exactly why it is worth doing once by hand.
 *
 * <p>The 32-bit algorithm, precisely:
 * <ol>
 *   <li>{@code hash = 0x811c9dc5} (the FNV offset basis)</li>
 *   <li>for each BYTE of the key's UTF-8 encoding, in order:
 *     <ol>
 *       <li>{@code hash = hash XOR (byte value, treated as unsigned 0-255)}</li>
 *       <li>{@code hash = (hash * 0x01000193) mod 2^32} (the FNV prime, then truncate to 32 bits)</li>
 *     </ol>
 *   </li>
 *   <li>return the final 32-bit value as an unsigned {@code long} (0 to 2^32 - 1)</li>
 * </ol>
 *
 * <p>Two details that are easy to get subtly wrong and will silently break every value:
 * <ul>
 *   <li>XOR happens BEFORE the multiply, not after - that is the "a" in FNV-1a. Plain FNV-1
 *       multiplies first, and produces different numbers for the same input.</li>
 *   <li>A Java {@code byte} is signed. {@code b & 0xffL} converts it to its unsigned value
 *       before the XOR; skipping that mangles every byte with the high bit set.</li>
 * </ul>
 *
 * <p>Known vectors to check yourself against: {@code hash("")} is {@code 2166136261}
 * ({@code 0x811c9dc5}, the offset basis untouched); {@code hash("a")} is {@code 3826002220}.
 */
public final class Fnv1aHashFunction implements HashFunction {

    private static final long OFFSET_BASIS = 0x811c9dc5L;
    private static final long PRIME = 0x01000193L;
    private static final long MASK_32 = 0xFFFFFFFFL;

    @Override
    public long hash(String key) {
        throw new UnsupportedOperationException(
                "TODO(day21): fold each UTF-8 byte through XOR-then-multiply, masked to 32 bits");
    }
}
