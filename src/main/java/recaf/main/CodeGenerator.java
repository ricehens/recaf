package recaf.main;

import recaf.asm.AssemblyBuilder;
import recaf.asm.AssemblyStringBuilder;
import recaf.asm.CFGtoASM;
import recaf.cfg.CFGProgram;
import recaf.opt.Deglobalization;
import recaf.opt.InlineExpansion;
import recaf.opt.MethodTransformer;
import recaf.opt.MethodTransformer.TransformType;
import recaf.reg.AssemblyOptimizer;
import recaf.reg.InstructionSelection;
import recaf.reg.PseudoassemblyOptimizer;
import recaf.reg.RegisterAllocation;

import java.io.PrintStream;

/**
 * Generates code from a control-flow graph.
 */
public class CodeGenerator {

    private CFGProgram cfg;
    private int optLevel;

    /**
     * Constructs a new CodeGenerator instance.
     *
     * @param cfg the control flow graph
     * @param optLevel the optimization level
     */
    public CodeGenerator(CFGProgram cfg, int optLevel) {
        this.cfg = cfg;
        this.optLevel = optLevel;
    }

    /**
     * Prints the low-level intermediate representation of the program.
     *
     * @param out the output destination
     */
    public void printLL(PrintStream out) {
        runOpts(true);
        out.print(cfg);
    }

    /**
     * Prints the SSA low-level intermediate representation of the program.
     *
     * @param out the output destination
     */
    public void printSSA(PrintStream out) {
        runOpts(false);
        out.print(cfg);
    }

    /**
     * Prints the compiled assembly code.
     *
     * @param out   the output destination
     * @param debug whether to print LL in comments
     */
    public void printASM(PrintStream out, boolean debug) {
        runOpts(true);
        if (optLevel >= 1) {
            // instruction selection
            AssemblyBuilder ab = new AssemblyBuilder(cfg.ctx());
            InstructionSelection sel = new InstructionSelection(ab);
            sel.visit(cfg);

            // pseudoassembly peephole
            new PseudoassemblyOptimizer(ab).apply();

            // register allocation
            RegisterAllocation reg = new RegisterAllocation(ab);
            reg.apply();

            // assembly peephole
            new AssemblyOptimizer(ab).apply();

            ab.printCode(out);
        } else {
            AssemblyStringBuilder ab = new AssemblyStringBuilder(cfg.ctx());
            ab.DEBUG = debug;
            CFGtoASM visitor = new CFGtoASM(ab);
            cfg.accept(visitor);
            ab.printCode(out);
        }
    }

    /**
     * Returns the CFGProgram object.
     *
     * @return the CFGProgram object
     */
    public CFGProgram getCfg() {
        return cfg;
    }

    private void runOpts(boolean dessa) {
        if (optLevel >= 1) {
            new Deglobalization(cfg).apply();

            MethodTransformer df = new MethodTransformer(cfg);
            df.apply(TransformType.TO_SSA);

            int iterations = switch (optLevel) {
                case 1 -> 3;
                case 2 -> 5;
                case 3 -> 10;
                default -> throw new RuntimeException("Unrecognized option level: " + optLevel);
            };

            for (int i = 0; i < 2 * iterations; i++) {
                df.apply(TransformType.SSCP);
                df.apply(TransformType.CP);

                if (optLevel >= 2) // GVNPRE
                    df.apply(TransformType.GVNPRE);

                df.apply(TransformType.ALG_SIMP);
                df.apply(TransformType.DCE);
                df.apply(TransformType.CLEAN);

                if (optLevel >= 2) {
                    df.apply(TransformType.TAILCALL);
                    new InlineExpansion(cfg).apply();
                }

                if (i == iterations && optLevel == 3) {
                    df.apply(TransformType.UNROLL);
                }

                if (optLevel >= 2)
                    df.apply(TransformType.LOAD_ELIM);

                df.apply(TransformType.DCE);
                df.apply(TransformType.CLEAN);

                if (optLevel >= 2 && i < iterations)
                    df.apply(TransformType.OSR_LFTR);

                if (optLevel >= 2) // GVNPRE
                    df.apply(TransformType.GVNPRE);

                df.apply(TransformType.DCE);
                df.apply(TransformType.CLEAN);
            }

            // cleanup
            df.apply(TransformType.SSCP);
            df.apply(TransformType.CP);
            df.apply(TransformType.DCE);
            df.apply(TransformType.CLEAN);

            // df.apply(enabled("regalloc") ? TransformType.COOPER_SSA : TransformType.DE_SSA);
            if (dessa) {
                df.apply(TransformType.DE_SSA);
                df.apply(TransformType.CLEAN);
            }
        }
    }

}
