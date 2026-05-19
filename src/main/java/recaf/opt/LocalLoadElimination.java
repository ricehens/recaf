package recaf.opt;

import recaf.cfg.*;
import recaf.general.Type;

import java.util.LinkedList;
import java.util.Queue;

public class LocalLoadElimination extends SSATransformation {

    public LocalLoadElimination(CFGContext ctx, CFGMethod method) {
        super(ctx, method);
    }

    @Override
    public boolean apply() {
        boolean changed = false;
        for (CFGBasicBlock block : method.getBlocks()) {
            Queue<CFGInstruction> workList = new LinkedList<>(block.getInstructions().stream().toList());
            while (!workList.isEmpty()) {
                CFGInstruction inst = workList.poll();
                if (inst instanceof CFGReadInstruction read) {
                    boolean pointerBase = ctx.getType(read.recordAddress()) != Type.RECORD;
                    CFGInstruction prev = block.getInstructions().prev(inst);
                    while (prev != null) {
                        if (prev instanceof CFGReadInstruction read2 && read2.recordAddress().equals(read.recordAddress())) {
                            if (read.width() == read2.width() && read.indexAddress().equals(read2.indexAddress())) {
                                block.getInstructions().replace(inst, new CFGCopyInstruction(ctx, read.address(), read2.address()));
                                changed = true;
                                break;
                            }
                        } else if (prev instanceof CFGWriteInstruction write && write.recordAddress().equals(read.recordAddress())) {
                            if (read.width() == write.width() && write.indexAddress().equals(read.indexAddress())) {
                                block.getInstructions().replace(inst, new CFGCopyInstruction(ctx, read.address(), write.valueAddress()));
                                changed = true;
                                break;
                            }

                            CFGInstruction def1 = data.getDefinition(read.indexAddress());
                            CFGInstruction def2 = data.getDefinition(write.indexAddress());
                            if (!(def1 instanceof CFGLiteralInstruction lit1) || !(def2 instanceof CFGLiteralInstruction lit2)
                            || !lit1.literal().equals(lit2.literal()))
                                break;
                        } else if (pointerBase && prev.address() != null && prev.address().equals(read.recordAddress())) {
                            break;
                        } else if (prev instanceof CFGMethodCallInstruction)
                            break;

                        prev = block.getInstructions().prev(prev);
                    }
                }
            }
        }
        return changed;
    }

}
