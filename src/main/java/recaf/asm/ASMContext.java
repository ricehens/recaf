package recaf.asm;

import recaf.cfg.CFGContext;

import java.util.HashMap;
import java.util.Map;

public class ASMContext {

    private int instructionCounter;

    private Map<ASMLabel, ASMBasicBlock> blocks;
    private CFGContext cfgCtx;

    public ASMContext(CFGContext cfgCtx) {
        this.cfgCtx = cfgCtx;
        instructionCounter = 0;
        blocks = new HashMap<>();
    }

    public CFGContext getCfgCtx() {
        return cfgCtx;
    }

    public int getInstructionCounter() {
        return instructionCounter++;
    }

    public void addBlock(ASMLabel label, ASMBasicBlock block) {
        blocks.put(label, block);
    }

    public ASMBasicBlock getBlock(ASMLabel label) {
        return blocks.get(label);
    }

}
