package recaf.main;

import recaf.antlr.RecafParser;
import recaf.antlr.RecafParser.ProgramContext;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

/**
 * Parses first token stream into first concrete syntax tree.
 *
 * @author Eric Shen
 */
public class Parser {

    private final CommonTokenStream tokens;
    private final String file;
    private final RecafErrorHandler errorHandler;

    public Parser(String file, CommonTokenStream tokens, RecafErrorHandler errorHandler) {
        this.file = file;
        this.errorHandler = errorHandler;
        this.tokens = tokens;
    }

    public ProgramContext parse() {
        RecafParser parser = new RecafParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                errorHandler.error(file, line, charPositionInLine, msg.split(" expecting")[0]);
            }
        });
        return parser.program();
    }

}
