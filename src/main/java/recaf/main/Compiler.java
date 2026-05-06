package recaf.main;

import org.antlr.v4.runtime.CommonTokenStream;
import recaf.antlr.RecafParser;
import recaf.ast.nodes.ASTProgram;
import recaf.cfg.CFGProgram;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Compiler {

    private final String infile;
    private final String outfile;
    private final CompilerTarget target;
    private final boolean printErrors;
    private final int optLevel;
    private final List<String> libraries;

    private RecafErrorHandler errorHandler;
    private PrintStream out;

    public enum CompilerTarget {
        AST, LL, SSA, ASSEMBLY, EXE
    }

    private File temp; // temporary file for EXE target

    public Compiler(
            String infile,
            String outfile,
            CompilerTarget target,
            boolean printErrors,
            int optLevel,
            List<String> libraries
    ) {
        this.infile = infile;
        this.outfile = outfile;
        this.target = target;
        this.printErrors = printErrors;
        this.optLevel = optLevel;
        this.libraries = libraries;
    }

    public void run() {
        errorHandler = new RecafErrorHandler();
        openOutputStream();

        var cs = tokenize(); bye();
        var cst = parse(cs); bye();
        var ast = generate(cst); bye();
        if (target == CompilerTarget.AST) {
            out.println(ast);
            return;
        }

        var cfg = linearize(ast);

        CodeGenerator cg = new CodeGenerator(cfg, optLevel);
        switch (target) {
            case LL -> cg.printLL(out);
            case SSA -> cg.printSSA(out);
            case ASSEMBLY, EXE -> cg.printASM(out, false);
        }

        if (target == CompilerTarget.EXE)
            makeExecutable();
    }

    private void bye() {
        if (errorHandler.hasErrors()) {
            errorHandler.printErrors();
            System.exit(1);
        }
    }

    private void openOutputStream() {
        if (target == CompilerTarget.EXE) {
            try {
                temp = File.createTempFile("recaf", ".s");
                out = new PrintStream(new FileOutputStream(temp));
            } catch (IOException e) {
                errorHandler.error("recafc", -1, -1, "temporary file could not be created");
            }

            return;
        }

        if (outfile == null) {
            out = System.out;
            return;
        }

        try {
            out = new PrintStream(Files.newOutputStream(Path.of(outfile))); ;
        } catch (IOException e) {
            errorHandler.error("recafc", -1, -1, "output file " + outfile + " could not be opened");
            bye();
        }
    }

    private CommonTokenStream tokenize() {
        if (!infile.endsWith(".pas")) {
            errorHandler.error("recafc", -1, -1, "file format for " + infile + " not recognized");
        }

        try (InputStream is = Files.newInputStream(Path.of(infile))) {
            return new Tokenizer(is).tokenize();
        } catch (IOException e) {
            errorHandler.error("recafc", -1, -1, "file " + infile + " could not be opened");
            return null;
        }
    }

    private RecafParser.ProgramContext parse(CommonTokenStream cs) {
        return new Parser(infile, cs, errorHandler).parse();
    }

    private ASTProgram generate(RecafParser.ProgramContext cst) {
        ASTGenerator gen = new ASTGenerator(errorHandler, infile, optLevel);
        return gen.generate(cst);
    }

    private CFGProgram linearize(ASTProgram ast) {
        CFGGenerator gen = new CFGGenerator();
        return gen.linearize(ast);
    }

    private void makeExecutable() {
        if (!System.getProperty("os.arch").equals("x86_64") && !System.getProperty("os.arch").equals("amd64")) {
            errorHandler.error("recafc", -1, -1, "cannot make executable on non-x86_64 architecture");
            bye();
        }
        try {
            List<String> cmd = new ArrayList<>(List.of(
                    "gcc", "-O0", "-no-pie", "-s", temp.getPath(),
                    "-o", outfile == null ? "a.out" : outfile
            ));
            for (String lib : libraries) {
                File file = new File(lib);
                if (!file.exists())
                    errorHandler.error("recafc", -1, -1, "library " + lib + " does not exist");
                if (file.getParent() != null)
                    cmd.add("-L" + file.getParent());
                String name = file.getName();
                if (!name.startsWith("lib") || !name.endsWith(".a"))
                    errorHandler.error("recafc", -1, -1, "library " + lib + " is not a static library");
                cmd.add("-l" + name.substring(3, name.length() - 2));
            }
            cmd.add("-lm");
            bye();
            ProcessBuilder pb = new ProcessBuilder(cmd.toArray(new String[0]));
            pb.inheritIO();
            Process p = pb.start();
            int exitCode = p.waitFor();
            if (exitCode != 0) {
                errorHandler.error("recafc", -1, -1, "gcc failed with exit code " + exitCode);
            }
        } catch (IOException e) {
            errorHandler.error("recafc", -1, -1, "error making executable");
            bye();
        } catch (InterruptedException e) {
            errorHandler.error("recafc", -1, -1, "error linking");
            bye();
        }
    }

}
