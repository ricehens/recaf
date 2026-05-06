package recaf.ast.nodes;

import recaf.general.Type;

public record ASTPrimitiveType(ASTContext ctx, Type type) implements ASTType {
}
