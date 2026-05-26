package recaf.ast;

import recaf.parse.ParseUtils;

import java.util.List;

public record ASTEnumType(ASTContext ctx, List<ASTIdentifier> members) implements ASTType {

    /*
    public ASTEnumType {
        Thread.dumpStack();
    }
    */

    @Override
    public String toString() {
        return ParseUtils.generateToString("(EnumType)", members);
    }
}
