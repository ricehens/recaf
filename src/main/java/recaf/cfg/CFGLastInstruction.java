package recaf.cfg;

/**
 * Represents the last instruction of a basic block
 */
public interface CFGLastInstruction extends CFGInstruction {

    @Override
    CFGLastInstruction copy();

}
