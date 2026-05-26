package recaf.ast;

import recaf.parse.ParseUtils;

import java.util.List;

public record ASTRecordType(ASTContext ctx, List<ASTVarDecl> fields) implements ASTType {

    @Override
    public String toString() {
        return ParseUtils.generateToString("(RecordType)", fields);
    }

}
