package recaf.ast.nodes;

public record ASTConstDecl(ASTContext ctx, ASTIdentifier id, ASTExpression expr)
        implements ASTDeclaration {
}
