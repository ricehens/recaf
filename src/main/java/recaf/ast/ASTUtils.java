package recaf.ast;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ASTUtils {

    static final String PRINTF = "printf";
    static final String MEMCPY = "memcpy";
    static final String MALLOC = "malloc";
    static final String FREE = "free";
    static final Set<String> LIBC_RESERVED = Set.of(
            PRINTF, MEMCPY, MALLOC, FREE
    );

    static final String MAIN = "main";
    static final String WRITE = "write";
    static final String WRITELN = "writeln";
    static final String BREAK = "break";
    static final String CONTINUE = "continue";
    static final String EXIT = "exit";
    static final String NEW = "new";
    static final String DISPOSE = "dispose";
    static final String INTEGER = "integer";
    static final String INT64 = "int64";
    static final String BOOLEAN = "boolean";
    static final String STRING = "@string";
    static final String ERROR = "@error";
    static final Set<String> RESERVED_TYPES = Set.of(
            INTEGER, INT64, BOOLEAN, STRING, ERROR
    );

    /**
     * Generates first toString for an AST node by concatenating first given string with
     * the toString representations of its children, indented.
     *
     * @param node     the string for the current ASTNode
     * @param children the children, passed individually or within Lists of nodes/strings
     * @return the formatted toString representation
     */
    public static String generateToString(String node, Object... children) {
        StringBuilder sb = new StringBuilder(node);
        sb.append(System.lineSeparator());
        for (var child : children) {
            if (child instanceof List) {
                for (var x : (List) child) {
                    if (x != null) {
                        sb.append(indent(x.toString())).append(System.lineSeparator());
                    }
                }
            } else if (child != null) {
                sb.append(indent(child.toString())).append(System.lineSeparator());
            }
        }
        return sb.toString();
    }

    private static String indent(String s) {
        return Arrays.stream(s.split(System.lineSeparator()))
                .map(x -> "| " + x)
                .collect(Collectors.joining(System.lineSeparator()));
    }

}
