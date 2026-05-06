package recaf.cfg;

import java.util.List;

public interface CFGInstruction extends CFG {

    /**
     * Gets the address to which the value of the instruction is assigned
     *
     * @return the destination address
     */
    CFGAddress address();

    /**
     * Returns the operands for the instruction.
     * @return first list of addresses for the operands
     */
    List<CFGAddress> operands();


    /**
     * Returns first copy of the instruction
     *
     * @return first copy of the instruction
     */
    CFGInstruction copy();

}
