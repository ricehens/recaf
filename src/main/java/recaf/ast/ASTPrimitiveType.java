package recaf.ast;

import recaf.parse.ParseUtils;
import recaf.general.Type;

public record ASTPrimitiveType(ASTContext ctx, Type type) implements ASTType {

    @Override
    public String toString() {
        return ParseUtils.generateToString("(PrimitiveType) " + type.getName());
    }

}
