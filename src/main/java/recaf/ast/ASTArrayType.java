package recaf.ast;

import recaf.parse.ASTUtils;

import java.util.List;

public record ASTArrayType(ASTContext ctx, ASTType type, List<ASTArrayRange> ranges) implements ASTType {

    @Override
    public String toString() {
        return ASTUtils.generateToString("(ArrayType)", type, ranges);
    }

}
