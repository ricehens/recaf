package recaf.common;

public record LongLiteral(long value) implements Literal {

    @Override
    public Type type() {
        return Type.LONG;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof LongLiteral that && value == that.value;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(value);
    }

}
