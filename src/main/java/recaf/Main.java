package recaf;

import recaf.main.Compiler;
import recaf.main.RecafError;

import java.util.ArrayList;
import java.util.List;

/**
 * Main frontend for the Recaf compiler.
 *
 * @author Eric Shen
 */
public class Main {

    private static final String usage = """
            Options:
              -t <stage>              Compile to the given stage. <stage> is one of
              --target <stage>        "ast", "ll", "ssa", "assembly", "exe" (default).
                                      Compilation will proceed to the specified stage
                                      and print the relevant representation.

              -o <outfile>            Output will be written to <outfile>. The default
              --output <outfile>      output location is a file with the .pas extension
                                      removed if stage is "exe", and stdout otherwise.

              -O0                     Perform no optimizations (default).
              -O1                     Perform simple optimizations, such as
                                      dead code elimination, constant/copy propagation,
                                      and register allocation.
              -O2                     Perform most optimizations, including subexpression
                                      elimination and strength reduction.
              -O3                     Perform all optimizations, including loop unrolling.

              -l <library>            Link in the given static library. Provide the path
              --library <library>     to the library file, e.g. dir/libname.a.

              --and                   If multiple compilation tasks are needed,
                                      divide their arguments with --and
                                      (e.g. recaf a.rcf -o a --and b.rcf -o b).

              -h, --help              Display this information.
            """;

    public static void main(String... args) {
        if (args.length == 0) {
            System.err.println(new RecafError("recaf", -1, -1, "no input files"));
            System.exit(1);
        }

        Compiler.CompilerTarget target = Compiler.CompilerTarget.EXE;
        int optLevel = 0;
        String infile = null;
        String outfile = null;
        List<String> libraries = new ArrayList<>();

        int i;
        outer:
        for (i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-h":
                case "--help":
                    System.out.println(usage);
                    System.exit(0);
                    break;

                case "-t":
                case "--target":
                    if (i + 1 >= args.length) {
                        System.err.println(new RecafError("recaf", -1, -1, "missing target after " + args[i]));
                        System.exit(1);
                    }
                    switch (args[++i]) {
                        case "ast":
                            target = Compiler.CompilerTarget.AST;
                            break;
                        case "ll":
                            target = Compiler.CompilerTarget.LL;
                            break;
                        case "ssa":
                            target = Compiler.CompilerTarget.SSA;
                            break;
                        case "assembly":
                            target = Compiler.CompilerTarget.ASSEMBLY;
                            break;
                        case "exe":
                            target = Compiler.CompilerTarget.EXE;
                            break;
                        default:
                            System.err.println(new RecafError("recaf", -1, -1, "unknown target " + args[i]));
                            System.exit(1);
                    }
                    break;

                case "-O0":
                    optLevel = 0;
                    break;

                case "-O1":
                    optLevel = 1;
                    break;

                case "-O2":
                    optLevel = 2;
                    break;

                case "-O3":
                    optLevel = 3;
                    break;

                case "-o":
                case "--output":
                    if (i + 1 >= args.length) {
                        System.err.println(new RecafError("recaf", -1, -1, "missing filename after " + args[i]));
                        System.exit(1);
                    }
                    outfile = args[++i];
                    break;

                case "-l":
                case "--library":
                    if (i + 1 >= args.length) {
                        System.err.println(new RecafError("recaf", -1, -1, "missing library after " + args[i]));
                        System.exit(1);
                    }
                    libraries.add(args[++i]);
                    break;

                case "--and":
                    break outer;

                default:
                    if (args[i].startsWith("-")) {
                        System.err.println(new RecafError("recaf", -1, -1, "unknown option " + args[i]));
                        System.exit(1);
                    }
                    if (infile != null)
                        System.err.println(new RecafError("recaf", -1, -1, "expected only one input file"));
                    infile = args[i];
            }
        }

        if (infile == null) {
            System.err.println(new RecafError("recaf", -1, -1, "no input files"));
            System.exit(1);
        }

        try {
            Compiler compiler = new Compiler(infile, outfile, target, true, optLevel, libraries);
            compiler.run();
        } catch (Throwable t) {
            System.err.println(new RecafError("recaf", -1, -1, "fatal error compiling " + infile + "; exiting now"));
            throw t;
        }

        if (i + 1 < args.length) {
            String[] newArgs = new String[args.length - i - 1];
            System.arraycopy(args, i + 1, newArgs, 0, args.length - i - 1);
            Main.main(newArgs);
        }
    }

}
