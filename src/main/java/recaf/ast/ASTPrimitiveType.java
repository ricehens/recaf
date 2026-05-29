package recaf.ast;

import recaf.parse.ParseUtils;
import recaf.common.Type;

public record ASTPrimitiveType(ASTContext ctx, Type type) implements ASTType {

    @Override
    public String toString() {
        return ParseUtils.generateToString("(PrimitiveType) " + type.getName());
    }

}
