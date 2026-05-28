package recaf.general;

/**
 * Represents a CFG type, e.g. int, long[].
 */
public enum Type {

    INT("int"),
    LONG("long"),
    BOOL("bool"),
    VOID("void"),
    STRING("string"),
    UNKNOWN("unknown"),
    RECORD("record");

    private final String name;

    Type(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "(Type) " + name;
    }

    /**
     * Returns a string representation of the text of the type, e.g. "int"
     *
     * @return a string representation of the type
     */
    public String getName() {
        return name;
    }

    /**
     * Returns a string representation of the type suitable for low-level IR
     *
     * @return a low-level representation of type text
     */
    public String toCFGString() {
        return switch (this) {
            case INT -> "i32";
            case LONG -> "i64";
            case BOOL -> "i1";
            case VOID -> "void";
            case STRING -> "str";
            case RECORD -> "rec";
            default -> "unk";
        };
    }

}
