package recaf.ast;

import recaf.parse.ASTUtils;

import java.util.List;

public record ASTRecordType(ASTContext ctx, List<ASTVarDecl> fields) implements ASTType {

    @Override
    public String toString() {
        return ASTUtils.generateToString("(RecordType)", fields);
    }

}
