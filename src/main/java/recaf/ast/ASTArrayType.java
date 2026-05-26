package recaf.ast;

import recaf.parse.ParseUtils;

import java.util.List;

public record ASTArrayType(ASTContext ctx, ASTType type, List<ASTArrayRange> ranges) implements ASTType {

    @Override
    public String toString() {
        return ParseUtils.generateToString("(ArrayType)", type, ranges);
    }

}
