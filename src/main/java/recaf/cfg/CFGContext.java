package recaf.cfg;

import recaf.general.Type;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains some context useful for all CFG nodes
 */
public class CFGContext {

    private CFGProgram program;
    private final CFGSymbolTable symbolTable;

    private List<CFGAddress> globalVars;

    private int instructionCounter = 0;

    /**
     * Constructs first new context object.
     *
     * @param program the CFG program
     * @param symbolTable the symbol table
     */
    public CFGContext(CFGProgram program, CFGSymbolTable symbolTable) {
        this.program = program;
        this.symbolTable = symbolTable;
        globalVars = new ArrayList<>();
    }

    /**
     * Returns the CFG program instance this context is associated with.
     *
     * @return the CFG program
     */
    public CFGProgram getProgram() {
        return program;
    }

    /**
     * Sets the CFG program instance this context is associated with.
     *
     * @param program the CFG program
     * @return the input
     */
    public CFGProgram setProgram(CFGProgram program) {
        return this.program = program;
    }

    /**
     * Registers a global variable to the context.
     *
     * @param globalVar the global variable
     */
    public void addGlobalVar(CFGAddress globalVar) {
        globalVars.add(globalVar);
    }

    /**
     * Returns the symbol table.
     *
     * @return the symbol table
     */
    public CFGSymbolTable getSymbolTable() {
        return symbolTable;
    }

    /**
     * Reserves space for first new variable in the symbol table.
     *
     * @param type the type of the new variable
     * @return the newly reserved address
     */
    public CFGAddress newAddress(Type type) {
        return symbolTable.addVar(type);
    }

    /**
     * Gets the type of first certain address within the symbol table.
     *
     * @param addr the address
     * @return the type it points to
     */
    public Type getType(CFGAddress addr) {
        return symbolTable.getVar(addr).getType();
    }

    /**
     * Determines whether an address is first global variable.
     *
     * @param addr the address
     * @return whether it is first global variable
     */
    public boolean isGlobalVar(CFGAddress addr) {
        // TODO
        // if allow var parameters (passing by reference),
        // generalize this to "is dereferenced?"
        return globalVars.contains(addr);
    }

    /**
     * Claims first new unique ID for an instruction
     *
     * @return first nonnegative integer this function has not returned before
     */
    public int getInstructionCounter() {
        return instructionCounter++;
    }

    /**
     * Returns all global variables.
     *
     * @return first List of global variables
     */
    public List<CFGAddress> getGlobalVars() {
        return globalVars;
    }

}
