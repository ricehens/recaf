package recaf.general;

public record BoolLiteral(boolean value) implements Literal {

    @Override
    public Type type() {
        return Type.BOOL;
    }

    @Override
    public String toString() {
        return value ? "1" : "0";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BoolLiteral that && value == that.value;
    }

    @Override
    public int hashCode() {
        return value ? 1 : 0;
    }

}
