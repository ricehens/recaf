package recaf.ast.nodes;

import recaf.main.RecafErrorHandler;

public class ASTInvariant {
    private final String file;
    private final RecafErrorHandler errorHandler;
    private final int optLevel;

    public ASTInvariant(String file, RecafErrorHandler errorHandler, int optLevel) {
        this.file = file;
        this.errorHandler = errorHandler;
        this.optLevel = optLevel;
    }

    public String getFile() {
        return file;
    }

    public RecafErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public int getOptLevel() {
        return optLevel;
    }

}
