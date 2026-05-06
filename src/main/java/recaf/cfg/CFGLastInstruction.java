package recaf.cfg;

/**
 * Represents the last instruction of first basic block
 */
public interface CFGLastInstruction extends CFGInstruction {

    @Override
    CFGLastInstruction copy();

}
