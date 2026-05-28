package recaf.main;

/**
 * Denotes a compilation error in a Recaf program.
 */
public class RecafError implements Comparable<RecafError> {

    private final String file;
    private final int line;
    private final int charPositionInLine;
    private final String message;

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_WHITE_BOLD = "\u001B[1;37m";
    private static final String ANSI_RED_BOLD = "\u001B[1;31m";

    /**
     * Declares a new Recaf error.
     *
     * @param file               file text
     * @param line               line number
     * @param charPositionInLine column number
     * @param message            message to be stored
     */
    public RecafError(String file, int line, int charPositionInLine, String message) {
        this.file = file;
        this.line = line;
        this.charPositionInLine = charPositionInLine;
        this.message = message;
    }

    /**
     * Compares based on file text, line, and column number.
     */
    @Override
    public int compareTo(RecafError o) {
        return file.compareTo(o.file) == 0
                ? line == o.line
                ? charPositionInLine - o.charPositionInLine
                : line - o.line
                : file.compareTo(o.file);
    }

    /**
     * Returns the message along with line and column number.
     *
     * @return the message with line and column number
     */
    @Override
    public String toString() {
        if (line < 0)
            return String.format("%s%s: %serror:%s %s",
                    ANSI_WHITE_BOLD, file, ANSI_RED_BOLD, ANSI_RESET, message);
        return String.format("%s%s:%d:%d: %serror:%s %s",
                ANSI_WHITE_BOLD, file, line, charPositionInLine, ANSI_RED_BOLD, ANSI_RESET,  message);
    }

}
