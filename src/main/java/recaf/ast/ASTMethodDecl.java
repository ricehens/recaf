package recaf.ast;

import recaf.parse.ASTUtils;

import java.util.List;
import java.util.Optional;

public record ASTMethodDecl(
        ASTContext ctx,
        Optional<ASTType> returnType, // null if void
        ASTIdentifier id,
        Optional<List<ASTVarDecl>> params, // null if variadic
        List<ASTDeclaration> decls,
        Optional<ASTBlock> block, // null if external, forward, or native
        boolean forward,
        boolean external,
        boolean internal
) implements ASTDeclaration {

    @Override
    public String toString() {
        return ASTUtils.generateToString("(MethodDecl) " + id.text(),
                params.isEmpty() ? "variadic" : params.get(),
                returnType.isEmpty() ? "void" : returnType.get(),
                decls,
                external ? "external" : forward ? "forward" : internal ? "native" : block.orElseThrow());
    }

}
