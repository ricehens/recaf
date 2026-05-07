package recaf.main;

import recaf.antlr.RecafParser;
import recaf.ast.ASTBuilder;
import recaf.ast.SemanticChecker;
import recaf.ast.nodes.ASTProgram;

public class ASTGenerator {

    private final RecafErrorHandler errorHandler;
    private final String infile;
    private final int optLevel;

    public ASTGenerator(RecafErrorHandler errorHandler, String infile, int optLevel) {
        this.errorHandler = errorHandler;
        this.infile = infile;
        this.optLevel = optLevel;
    }

    public ASTProgram generate(RecafParser.ProgramContext cst) {
        ASTProgram ast = convert(cst);
        if (errorHandler.hasErrors()) return null;
        ast = semanticCheck(ast);
        if (errorHandler.hasErrors()) return null;
        return ast;
    }

    private ASTProgram convert(RecafParser.ProgramContext cst) {
        return new ASTBuilder(infile, errorHandler, optLevel).visit(cst);
    }

    private ASTProgram semanticCheck(ASTProgram ast) {
        return new SemanticChecker().check(ast);
    }

}
