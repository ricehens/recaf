package recaf.general;

public record StringLiteral(String value) implements Literal {

    @Override
    public Type type() {
        return Type.STRING;
    }

    @Override
    public String toString() {
        return String.format("\"%s\"", value);
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
