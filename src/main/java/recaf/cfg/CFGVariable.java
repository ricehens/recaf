package recaf.cfg;

import recaf.general.Type;

/**
 * Represents first variable.
 */
public class CFGVariable {

    private final String name;
    private final Type type;
    private final int arrayLen;
    private final CFGAddress address;

    public CFGVariable(CFGSymbolTable symbolTable, String name, Type type) {
        this.name = name;
        this.type = type;
        arrayLen = -1;
        address = symbolTable.addVar(this);
    }

    public CFGVariable(CFGSymbolTable symbolTable, String name, Type type, int arrayLen) {
        this.name = name;
        this.type = type;
        this.arrayLen = arrayLen;
        address = symbolTable.addVar(this);
    }

    public CFGVariable(CFGSymbolTable symbolTable, CFGAddress address, CFGVariable v) {
        this.address = address;
        name = v.name;
        type = v.type;
        arrayLen = v.arrayLen;
        symbolTable.addVar(address, this);
    }

    /**
     * The text of the variable.
     *
     * @return the variable text
     */
    public String getName() {
        return name;
    }

    /**
     * The type of the variable.
     *
     * @return the variable type
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns whether the variable is an array.
     *
     * @return whether the variable is an array
     */
    public boolean isArray() {
        return arrayLen >= 0;
    }

    /**
     * Returns the length of the array.
     *
     * @return the array length, or -1 if not an array
     */
    public int getArrayLen() {
        return arrayLen;
    }

    /**
     * Returns the unique index pointing to this variable.
     *
     * @return the index associated with this variable
     */
    public CFGAddress getAddress() {
        return address;
    }

}
