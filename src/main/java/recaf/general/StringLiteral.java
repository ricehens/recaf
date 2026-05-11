package recaf.general;

public record StringLiteral(String value) implements Literal {

    @Override
    public Type type() {
        return Type.STRING;
    }

    @Override
    public String toString() {
        return String.format("\"%s\"", escape());
    }

    public String escape() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case '"' -> sb.append("\\\"");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                case '\0' -> sb.append("\\0");
                default -> {
                    if (c < 32 || c >= 127)
                        sb.append(String.format("\\x%02x", (int) c));
                    else sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof StringLiteral that && value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
