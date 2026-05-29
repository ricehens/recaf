package recaf.utils;

import recaf.common.IntLiteral;
import recaf.common.Literal;
import recaf.common.LongLiteral;

public class NumUtils {

    private long n;

    public NumUtils(long n) {
        this.n = n;
    }

   public NumUtils(Literal lit) {
        this(lit instanceof IntLiteral ilit ? ilit.value() : lit instanceof LongLiteral llit ? llit.value() : 0);
    }

    public int vp(long p) {
        if (n == 0) return -1;

        int count = 0;
        long n = this.n;
        while (n % p == 0) {
            n /= p;
            count++;
        }
        return count;
    }

    public boolean isPowerOf(long p) {
        if (n == 0) return false;

        long n = this.n;
        while (n % p == 0) {
            n /= p;
        }
        return n == 1 || n == -1;
    }

    public boolean isUnit() {
        return n == 1 || n == -1;
    }

    public boolean isZero() {
        return n == 0;
    }

    public int sign() {
        return n > 0 ? 1 : n < 0 ? -1 : 0;
    }

    public long asLong() {
        return n;
    }

}
