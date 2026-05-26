package recaf.ast;

import recaf.parse.ASTUtils;
import recaf.general.Type;

public record ASTPrimitiveType(ASTContext ctx, Type type) implements ASTType {

    @Override
    public String toString() {
        return ASTUtils.generateToString("(PrimitiveType) " + type.getName());
    }

}
