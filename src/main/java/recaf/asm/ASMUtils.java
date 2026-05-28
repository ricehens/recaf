package recaf.asm;

/**
 * Some utilities for generating x86_64 AT&amp;T assembly code.
 */
public class ASMUtils {

    /**
     * Adds spaces to the end of a string until the length is a
     * multiple 8.
     *
     * @param str the input string
     * @return str, padded by spaces
     */
    public static String pad(String str) {
        StringBuilder sb = new StringBuilder(str);
        do {
            sb.append(" ");
        } while (sb.length() % 8 != 0);
        return sb.toString();
    }

}
