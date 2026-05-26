package recaf.parse;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ASTUtils {

    static final String PRINTF = "printf";
    static final String SCANF = "scanf";
    static final String MEMCPY = "memcpy";
    static final String INTRINSIC_WRITESTR = "recaf_writestr";
    static final String INTRINSIC_READLN = "recaf_readln";
    static final String INTRINSIC_READSTR = "recaf_readstr";
    static final String INTRINSIC_ALLOC = "recaf_alloc";
    static final String INTRINSIC_FREE = "recaf_free";
    static final Set<String> LIB_RESERVED = Set.of(
            PRINTF, SCANF, MEMCPY, 
            INTRINSIC_WRITESTR,
            INTRINSIC_READLN, INTRINSIC_READSTR,
            INTRINSIC_ALLOC, INTRINSIC_FREE
    );

    static final String MAIN = "main";
    static final String WRITE = "write";
    static final String WRITELN = "writeln";
    static final String READ = "read";
    static final String READLN = "readln";
    static final String BREAK = "break";
    static final String CONTINUE = "continue";
    static final String EXIT = "exit";
    static final String NEW = "new";
    static final String DISPOSE = "dispose";
    static final String INTEGER = "integer";
    static final String INT64 = "int64";
    static final String BOOLEAN = "boolean";
    static final String CHAR = "char";
    static final String STRING = "@string";
    static final String ERROR = "@error";
    static final String TRUE = "true";
    static final String FALSE = "false";
    static final String NIL = "nil";
    static final String NIL_TYPE = "@nil";

    static final String SYSTEM = "system";
    static final String FLOAT64 = "float64";

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
