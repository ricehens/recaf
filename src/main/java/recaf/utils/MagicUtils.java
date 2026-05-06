package recaf.utils;

import java.math.BigInteger;

/**
 * TRUNC(x/d) = (m*x)>>(64+exp) - (x >> 63);
 * replace 63 with 31 for 32-bit ints
 * note x >> 63 is most significant bit.
 */
public class MagicUtils {

    public record Magic(long magic, int shift, boolean neg) {}

    public static Magic magic32(int d) {
        long absd = Math.abs(d);
        for (int exp = 32;; exp++) {
            long pow2 = 1L << exp;
            // m = ceil(2^exp / |d|)
            long m = (pow2 - 1) / absd + 1;

            // check if m * |d| <= 2^exp = 2^(exp - 31)
            if (m * absd <= (1L << exp) + (1L << (exp - 31)))
                return new Magic(m, exp - 32, d < 0);
        }
    }

    public static Magic magic64(long d) {
        BigInteger absd = BigInteger.valueOf(d).abs();
        for (int exp = 64;; exp++) {
            BigInteger pow2 = BigInteger.ONE.shiftLeft(exp);
            // m = ceil(2^exp / |d|)
            BigInteger m = pow2.subtract(BigInteger.ONE).divide(absd).add(BigInteger.ONE);

            // check if m * |d| <= 2^exp = 2^(exp - 63)
            if (m.multiply(absd).compareTo(BigInteger.ONE.shiftLeft(exp).add(BigInteger.ONE.shiftLeft(exp - 63))) <= 0)
                return new Magic(m.longValue(), exp - 64, d < 0);
        }
    }

}
