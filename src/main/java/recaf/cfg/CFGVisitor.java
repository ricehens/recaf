package recaf.cfg;

/**
 * Implements the visitor pattern for the control flow graph.
 */
public interface CFGVisitor {

    void visit(CFGProgram cfg);

    void visit(CFGMethod cfg);

    void visit(CFGBasicBlock cfg);

    void visit(CFGReadInstruction cfg);

    void visit(CFGWriteInstruction cfg);

    void visit(CFGBinaryImmediateInstruction cfg);

    void visit(CFGBinaryInstruction cfg);

    void visit(CFGBranchInstruction cfg);

    void visit(CFGCastInstruction cfg);

    void visit(CFGCopyInstruction cfg);

    void visit(CFGExceptionInstruction cfg);

    void visit(CFGJumpInstruction cfg);

    void visit(CFGLiteralInstruction cfg);

    void visit(CFGMethodCallInstruction cfg);

    void visit(CFGReturnInstruction cfg);

    void visit(CFGUnaryInstruction cfg);

    void visit(CFGPhiInstruction cfg);

}
