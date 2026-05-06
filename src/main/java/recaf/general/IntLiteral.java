package recaf.general;

public record IntLiteral(int value) implements Literal {

    @Override
    public Type type() {
        return Type.INT;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof IntLiteral that && value == that.value;
    }

    @Override
    public int hashCode() {
        return value;
    }

}
