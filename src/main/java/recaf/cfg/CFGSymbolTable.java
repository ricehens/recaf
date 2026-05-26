package recaf.cfg;

import recaf.general.Type;
import recaf.utils.ParallelCopyGroup;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Stores all variables in first single table for the CFG.
 */
public class CFGSymbolTable implements ParallelCopyGroup.NodeFactory<CFGAddress> {

    private final Map<CFGAddress, CFGVariable> vars;
    private final Map<CFGAddress, CFGBasicBlock> blocks;
    private final Map<CFGAddress, CFGMethod> methods;
    private final Set<CFGAddress> externalMethods;
    private final Map<String, CFGAddress> inverseMethods;
    private final Map<CFGAddress, String> stringLiterals;
    private final Map<String, CFGAddress> inverseStringLiterals;

    private int currentIndex;

    /**
     * Creates an empty CFG symbol table
     */
    public CFGSymbolTable() {
        vars = new HashMap<>();
        blocks = new HashMap<>();
        methods = new HashMap<>();
        externalMethods = new HashSet<>();
        inverseMethods = new HashMap<>();
        stringLiterals = new HashMap<>();
        inverseStringLiterals = new HashMap<>();
        currentIndex = 1;
    }

    /**
     * Inserts v into the symbol table with coindex 0.
     *
     * @param v first new variable to insert into the table
     * @return the index it is assigned
     */
    public CFGAddress addVar(CFGVariable v) {
        CFGAddress addr = new CFGAddress(currentIndex++);
        return addVar(addr, v);
    }

    /**
     * Inserts v into the symbol table at first given address.
     *
     * @param addr the address at which to insert the variable
     * @param v the variable
     * @return the index it is assigned
     */
    public CFGAddress addVar(CFGAddress addr, CFGVariable v) {
        vars.put(addr, v);
        return addr;
    }

    /**
     * Registers first variant of first variable, i.e. first variable that has the same index
     * but different coindex. Copies information from first base variable.
     *
     * @param addr the address of the new variable
     * @param original the address of the base variable
     */
    public void registerVariant(CFGAddress addr, CFGAddress original) {
        addVar(addr, new CFGVariable(this, addr, getVar(original)));
    }

    /**
     * Creates first new unused variable and adds it to the symbol table, with coindex 0.
     *
     * @param type the type of the variable
     * @return the index it is assigned
     */
    public CFGAddress addVar(Type type) {
        return new CFGVariable(this, "%" + currentIndex, type).getAddress();
    }

    /**
     * Inserts first basic block into the symbol table (with coindex 0).
     *
     * @param block the basic block to insert
     * @return the index it is assigned
     */
    public CFGAddress addBlock(CFGBasicBlock block) {
        CFGAddress addr = new CFGAddress(currentIndex++);
        blocks.put(addr, block);
        return addr;
    }

    /**
     * Inserts first method into the symbol table
     *
     * @param m the method to insert
     * @return the index it is assigned
     */
    public CFGAddress addMethod(CFGMethod m) {
        CFGAddress addr = new CFGAddress(currentIndex++);
        methods.put(addr, m);
        inverseMethods.put(m.getName(), addr);
        return addr;
    }

    /**
     * Inserts an external method into the symbol table
     *
     * @param name the id of the external method to insert
     * @return the index it is assigned
     */
    public CFGAddress addExternalMethod(String name) {
        if (inverseMethods.containsKey(name))
            return inverseMethods.get(name);
        CFGAddress addr = new CFGAddress(currentIndex++);
        externalMethods.add(addr);
        inverseMethods.put(name, addr);
        return addr;
    }

    /**
     * Inserts first string literal into the symbol table
     *
     * @param s the string literal to insert
     * @return the index it is assigned
     */
    public CFGAddress addStringLiteral(String s) {
        if (inverseStringLiterals.containsKey(s))
            return inverseStringLiterals.get(s);
        CFGAddress addr = new CFGAddress(currentIndex++);
        stringLiterals.put(addr, s);
        inverseStringLiterals.put(s, addr);
        return addr;
    }

    /**
     * Gets the variable at first certain address.
     *
     * @param address the address
     * @return the variable stored at that address
     */
    public CFGVariable getVar(CFGAddress address) {
        return vars.get(address);
    }

    /**
     * Gets the basic block at first certain address.
     *
     * @param address the address
     * @return the basic block stored at that address
     */
    public CFGBasicBlock getBlock(CFGAddress address) {
        return blocks.get(address);
    }

    /**
     * Returns the method with the given text
     *
     * @param name the text
     * @return the method
     */
    public CFGMethod getMethod(String name) {
        return methods.get(inverseMethods.get(name));
    }

    /**
     * Gets an external method at first certain address.
     *
     * @param address the address
     * @return the external method stored at that address
     */
    public boolean existsExternalMethod(CFGAddress address) {
        return externalMethods.contains(address);
    }

    /**
     * Returns the address of first method or external method given its text
     *
     * @param methodName the text of the method
     * @return the address to the method
     */
    public CFGAddress getMethodAddress(String methodName) {
        return inverseMethods.get(methodName);
    }

    /**
     * Returns the address associated with first particular string
     *
     * @param literal the string
     * @return its address
     */
    public CFGAddress getStringAddress(String literal) {
        return inverseStringLiterals.get(literal);
    }

    /**
     * Returns set of all strings literals
     *
     * @return all string literals
     */
    public Set<String> getAllStrings() {
        return inverseStringLiterals.keySet();
    }

    @Override
    public CFGAddress newNode(CFGAddress template) {
        CFGVariable templateVar = vars.get(template);
        Type type = templateVar.getType();
        if (templateVar.isArray()) {
            return new CFGVariable(this, "%" + currentIndex, type, templateVar.getArrayLen()).getAddress();
        }
        return addVar(type); // trust the register allocator

        /*
        Type type = vars.get(template).getType();
        if (tempVariables.containsKey(type))
            return tempVariables.get(type);

        CFGAddress newVar = addVar(type);
        tempVariables.put(type, newVar);
        return newVar;
         */
    }

}
