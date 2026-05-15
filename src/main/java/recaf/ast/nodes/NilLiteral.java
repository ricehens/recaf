package recaf.ast.nodes;

import recaf.general.Literal;
import recaf.general.Type;

public class NilLiteral implements Literal {

    @Override
    public Type type() {
        return Type.POINTER;
    }

    @Override
    public String toString() {
        return "nil";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof NilLiteral;
    }

    @Override
    public int hashCode() {
        return 0;
    }

}
