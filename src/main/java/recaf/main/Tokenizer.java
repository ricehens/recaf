package recaf.main;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import recaf.antlr.RecafLexer;

import java.io.IOException;
import java.io.InputStream;

/**
 * A tokenizer for the Recaf language.
 *
 * @author Eric Shen
 */
public class Tokenizer {

    private final CharStream cs;

    /**
     * Creates a scanner object to tokenize an input stream.
     *
     * @param is The input stream.
     */
    public Tokenizer(InputStream is) throws IOException {
        cs = CharStreams.fromStream(is);
    }

    /**
     * Tokenizes the input stream.
     *
     * @return The token stream
     */
    public CommonTokenStream tokenize() {
        RecafLexer lexer = new RecafLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        tokens.fill();
        return tokens;
    }

}
