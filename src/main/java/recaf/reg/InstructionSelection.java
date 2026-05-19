package recaf.reg;

import recaf.asm.*;
import recaf.cfg.*;
import recaf.general.*;
import recaf.utils.MagicUtils;
import recaf.utils.NumUtils;

import java.util.Set;
import java.util.stream.Collectors;

// TODO 处理全局变量、passing array to external function 等：每个指令都要先 load、store 所有相关全局变量
// ^^^ definitely
// TODO 肯定要 peephole pass 什么的，如 branch instruction conditional 合并
public class InstructionSelection implements CFGVisitor {

    private final AssemblyBuilder asm;

    public InstructionSelection(AssemblyBuilder asm) {
        this.asm = asm;
    }

    @Override
    public void visit(CFGProgram cfg) {
        for (CFGAddress globalVar : cfg.ctx().getGlobalVars())
            asm.emit(new ASMGlobalVarDecl(asm.ctx(), new ASMLabel(cfg.ctx().getSymbolTable().getVar(globalVar).getName()), asm.sizeof(globalVar)));
        for (String strLiteral : cfg.ctx().getSymbolTable().getAllStrings())
            asm.emit(new ASMStringDecl(asm.ctx(), asm.getStringLabel(strLiteral), strLiteral));

        cfg.getMethods().forEach(method -> method.accept(this));
        asm.wrapUp();

        // spill global variables
        for (ASMMethod method : asm.asm().getMethods()) {
            cfg.ctx().getGlobalVars().forEach(gv -> asm.spill(method, new ASMVirtualRegister(gv)));
        }
    }

    @Override
    public void visit(CFGMethod cfg) {
        asm.newMethod(cfg.getName());

        // prologue
        CFGBasicBlock entry = cfg.getBlocks().peekFirst();
        asm.newBlock(asm.getMethodLabel(cfg.getName()), entry == null ? Set.of() : Set.of(asm.getLabel(entry.address())));
        // asm.emit(ASMOperator.PUSHQ, ASMRegister.RBP);
        // asm.emit(ASMOperator.MOVQ, ASMRegister.RSP, ASMRegister.RBP);
        asm.emit(ASMOperator.SUBQ, asm.getAlignedStackOffset(), ASMRegister.RSP);

        // pre-register
        for (int i = 6; i < cfg.getParams().size(); i++) {
            CFGAddress param = cfg.getParams().get(i);
            asm.manuallyRegisterVariable(param, 8 * i - 40);
            switch (cfg.ctx().getType(param)) {
                case INT:
                    asm.emit(ASMOperator.MOVL, asm.getMemoryLocation(param), new ASMVirtualRegister(param));
                    break;
                case LONG:
                    asm.emit(ASMOperator.MOVQ, asm.getMemoryLocation(param), new ASMVirtualRegister(param));
                    break;
                case BOOL:
                    asm.emit(ASMOperator.MOVB, asm.getMemoryLocation(param), new ASMVirtualRegister(param));
                    break;
                default:
                    throw new AssertionError("This should never happen.");
            }
        }

        cfg.getLocalVars().stream()
                .filter(x -> cfg.ctx().getType(x) == Type.RECORD)
                .forEach(asm::registerVariable);

        for (int i = 0; i < Math.min(6, cfg.getParams().size()); i++) {
            CFGAddress param = cfg.getParams().get(i);
            switch (cfg.ctx().getType(param)) {
                case INT:
                    asm.emit(ASMOperator.MOVL, ASMRegister.getMethodArg(Type.INT, i), new ASMVirtualRegister(param));
                    break;
                case LONG:
                    asm.emit(ASMOperator.MOVQ, ASMRegister.getMethodArg(Type.LONG, i), new ASMVirtualRegister(param));
                    break;
                case BOOL:
                    asm.emit(ASMOperator.MOVB, ASMRegister.getMethodArg(Type.BOOL, i), new ASMVirtualRegister(param));
                    break;
                default:
                    throw new AssertionError("This should never happen.");
            }
        }

        // generate basic blocks
        cfg.getBlocks().forEach(block -> block.accept(this));
    }

    @Override
    public void visit(CFGBasicBlock cfg) {
        asm.newBlock(asm.getLabel(cfg.address()),
                cfg.successors().stream().map(CFGBasicBlock::address).map(asm::getLabel).collect(Collectors.toSet()));
        cfg.getAllInstructions().forEach(instruction -> instruction.accept(this));
    }

    @Override
    public void visit(CFGReadInstruction cfg) {
        var dest = new ASMVirtualRegister(cfg.address());
        var indexLoc = new ASMVirtualRegister(cfg.indexAddress());
        ASMLocation arrayLoc;

        asm.emit(ASMOperator.MOVSLQ, indexLoc, ASMRegister.RAX);

        var type = cfg.ctx().getType(cfg.recordAddress());
        if (type == Type.RECORD) {
            arrayLoc = asm.getMemoryLocationArray(cfg.recordAddress(), cfg.width());
        } else {
            asm.emit(ASMOperator.MOVQ, new ASMVirtualRegister(cfg.recordAddress()), ASMRegister.RCX);
            arrayLoc = new ASMStackAddressArray(0, ASMRegister.RCX, ASMRegister.RAX, cfg.width());
        }
        switch (cfg.width()) {
            case 4:
                asm.emit(ASMOperator.MOVL, arrayLoc, dest);
                break;
            case 8:
                asm.emit(ASMOperator.MOVQ, arrayLoc, dest);
                break;
            case 1:
                asm.emit(ASMOperator.MOVZBL, arrayLoc, dest);
                break;
            default:
                throw new AssertionError("unsupported record read width: " + cfg.width());
        }
    }

    @Override
    public void visit(CFGWriteInstruction cfg) {
        var indexLoc = new ASMVirtualRegister(cfg.indexAddress());
        var valueLoc = new ASMVirtualRegister(cfg.valueAddress());
        ASMLocation arrayLoc;

        asm.emit(ASMOperator.MOVSLQ, indexLoc, ASMRegister.RAX);

        var type = cfg.ctx().getType(cfg.recordAddress());
        if (type == Type.RECORD) {
            arrayLoc = asm.getMemoryLocationArray(cfg.recordAddress(), cfg.width());
        } else {
            switch (cfg.width()) {
                case 4 -> asm.emit(ASMOperator.MOVL, valueLoc, ASMRegister.R11D);
                case 8 -> asm.emit(ASMOperator.MOVQ, valueLoc, ASMRegister.R11);
                case 1 -> asm.emit(ASMOperator.MOVZBL, valueLoc, ASMRegister.R11D);
                default -> throw new AssertionError("unsupported record write width: " + cfg.width());
            }
            asm.emit(ASMOperator.MOVQ, new ASMVirtualRegister(cfg.recordAddress()), ASMRegister.RCX);
            arrayLoc = new ASMStackAddressArray(0, ASMRegister.RCX, ASMRegister.RAX, cfg.width());
        }
        switch (cfg.width()) {
            case 4:
                asm.emit(ASMOperator.MOVL,
                        type == Type.LONG ? ASMRegister.R11D : valueLoc,
                        arrayLoc);
                break;
            case 8:
                asm.emit(ASMOperator.MOVQ,
                        type == Type.LONG ? ASMRegister.R11 : valueLoc,
                        arrayLoc);
                break;
            case 1:
                asm.emit(ASMOperator.MOVB,
                        type == Type.LONG ? ASMRegister.R11B : valueLoc,
                        arrayLoc);
                break;
            default:
                throw new AssertionError("unsupported record write width: " + cfg.width());
        }
    }

    @Override
    public void visit(CFGBinaryImmediateInstruction cfg) {
        // TODO
        Type type = cfg.ctx().getType(cfg.left());

        var dest = new ASMVirtualRegister(cfg.address());
        var leftLoc = new ASMVirtualRegister(cfg.left());
        Literal right = cfg.right();

        NumUtils pf = new NumUtils(right);
        // TODO leaq multiplication --- probably relegate to peephole
        if (cfg.operator() == BinaryOperator.TIMES && pf.isPowerOf(2)) {
            asm.emit(type == Type.LONG ? ASMOperator.MOVQ : ASMOperator.MOVL, leftLoc, dest);
            if (!pf.isUnit())
                asm.emit(type == Type.LONG ? ASMOperator.SHLQ : ASMOperator.SHLL, new ASMLiteral(pf.vp(2)), dest);
            if (pf.sign() == -1)
                asm.emit(type == Type.LONG ? ASMOperator.NEGQ : ASMOperator.NEGL, dest);
        } else if (cfg.operator() == BinaryOperator.DIVIDES || cfg.operator() == BinaryOperator.MOD) {
            if (pf.isUnit()) {
                if (cfg.operator() == BinaryOperator.MOD)
                    asm.emit(type == Type.LONG ? ASMOperator.XORQ : ASMOperator.XORL, dest, dest);
                else if (pf.sign() == -1) {
                    asm.emit(type == Type.LONG ? ASMOperator.MOVQ : ASMOperator.MOVL, leftLoc, dest);
                    asm.emit(type == Type.LONG ? ASMOperator.NEGQ : ASMOperator.NEGL, dest);
                }
            } else if (pf.isPowerOf(2)) {
                int exp = pf.vp(2);

                if (cfg.operator() == BinaryOperator.DIVIDES) {
                    var tmp = new ASMVirtualRegister(cfg.ctx().getSymbolTable().addVar(right.type()));
                    asm.emit(type == Type.LONG ? ASMOperator.MOVQ : ASMOperator.MOVL, leftLoc, dest);

                    asm.emit(type == Type.LONG ? ASMOperator.MOVABSQ : ASMOperator.MOVL, new ASMLiteral((1L << exp) - 1), tmp);
                    asm.emit(type == Type.LONG ? ASMOperator.ADDQ : ASMOperator.ADDL, dest, tmp);

                    asm.emit(type == Type.LONG ? ASMOperator.TESTQ : ASMOperator.TESTL, dest, dest);
                    asm.emit(type == Type.LONG ? ASMOperator.CMOVSQ : ASMOperator.CMOVSL, tmp, dest);
                    asm.emit(type == Type.LONG ? ASMOperator.SARQ : ASMOperator.SARL, new ASMLiteral(exp), dest);

                    if (pf.sign() == -1)
                        asm.emit(type == Type.LONG ? ASMOperator.NEGQ : ASMOperator.NEGL, dest);
                } else { // mod
                    var tmp = new ASMVirtualRegister(cfg.ctx().getSymbolTable().addVar(right.type()));
                    var tmp2 = new ASMVirtualRegister(cfg.ctx().getSymbolTable().addVar(right.type()));

                    int numBits = type == Type.LONG ? 64 : 32;
                    asm.emit(type == Type.LONG ? ASMOperator.MOVQ : ASMOperator.MOVL, leftLoc, tmp);
                    asm.emit(type == Type.LONG ? ASMOperator.MOVQ : ASMOperator.MOVL, leftLoc, dest);
                    asm.emit(type == Type.LONG ? ASMOperator.SARQ : ASMOperator.SARL, new ASMLiteral(numBits - 1), tmp);
                    asm.emit(type == Type.LONG ? ASMOperator.SHRQ : ASMOperator.SHRL, new ASMLiteral(numBits - exp), tmp);
                    asm.emit(type == Type.LONG ? ASMOperator.ADDQ : ASMOperator.ADDL, tmp, dest);
                    if (type == Type.INT)
                        asm.emit(ASMOperator.ANDL, new ASMLiteral((1L << exp) - 1), dest);
                    else {
                        asm.emit(ASMOperator.MOVABSQ, new ASMLiteral((1L << exp) - 1), tmp2);
                        asm.emit(ASMOperator.ANDQ, tmp2, dest);
                    }
                    asm.emit(type == Type.LONG ? ASMOperator.SUBQ : ASMOperator.SUBL, tmp, dest);
                }
            } else {
                // TODO 优化 --- IMULO外不要使用固定的寄存器？
                ASMRegister leftOperand = type == Type.LONG ? ASMRegister.RAX
                        : type == Type.BOOL ? ASMRegister.AL : ASMRegister.EAX;
                ASMRegister rightOperand = type == Type.LONG ? ASMRegister.RDX
                        : type == Type.BOOL ? ASMRegister.DL : ASMRegister.EDX;
                ASMRegister tempOperand = type == Type.LONG ? ASMRegister.RCX
                        : type == Type.BOOL ? ASMRegister.CL : ASMRegister.ECX;

                MagicUtils.Magic magic = type == Type.LONG
                        ? MagicUtils.magic64(((LongLiteral) right).value())
                        : MagicUtils.magic32(((IntLiteral) right).value());
                int numBits = type == Type.LONG ? 64 : 32;

                asm.emit(type == Type.LONG ? ASMOperator.MOVQ : ASMOperator.MOVL, leftLoc, leftOperand);
                if (type == Type.LONG) {
                    asm.emit(ASMOperator.MOVABSQ, new ASMLiteral(magic.magic()), rightOperand);
                    asm.emit(ASMOperator.MOVQ, leftOperand, tempOperand);
                    asm.emit(ASMOperator.IMULO, rightOperand);
                    asm.emit(ASMOperator.MOVQ, ASMRegister.RDX, leftOperand);
                    if (magic.magic() < 0)
                        asm.emit(ASMOperator.ADDQ, tempOperand, leftOperand);
                } else {
                    asm.emit(ASMOperator.MOVL, leftOperand, tempOperand);
                    asm.emit(ASMOperator.CLTQ);
                    asm.emit(ASMOperator.IMULQ, new ASMLiteral(magic.magic() >= (1L << 31) ? (magic.magic() - (1L << 32)) : magic.magic()), leftOperand.toType(Type.LONG));
                    asm.emit(ASMOperator.SHRQ, new ASMLiteral(32), leftOperand.toType(Type.LONG));
                    if (magic.magic() >= (1L << 31))
                        asm.emit(ASMOperator.ADDL, tempOperand, leftOperand);
                }
                asm.emit(type == Type.LONG ? ASMOperator.SARQ : ASMOperator.SARL, new ASMLiteral(magic.shift()), leftOperand);
                asm.emit(type == Type.LONG ? ASMOperator.MOVQ : ASMOperator.MOVL, tempOperand, rightOperand);
                asm.emit(type == Type.LONG ? ASMOperator.SARQ : ASMOperator.SARL, new ASMLiteral(numBits - 1), rightOperand);
                asm.emit(type == Type.LONG ? ASMOperator.SUBQ : ASMOperator.SUBL, rightOperand, leftOperand);
                if (magic.neg())
                    asm.emit(type == Type.LONG ? ASMOperator.NEGQ : ASMOperator.NEGL, leftOperand); // TODO just flip sign of subtraction?

                if (cfg.operator() == BinaryOperator.MOD) {
                    if (type == Type.LONG) {
                        asm.emit(ASMOperator.MOVABSQ, new ASMLiteral(right), rightOperand);
                        asm.emit(ASMOperator.IMULQ, rightOperand, leftOperand);
                    } else
                        asm.emit(ASMOperator.IMULL, new ASMLiteral(right), leftOperand);
                    asm.emit(type == Type.LONG ? ASMOperator.SUBQ : ASMOperator.SUBL, leftOperand, tempOperand);
                    asm.emit(type == Type.LONG ? ASMOperator.MOVQ : ASMOperator.MOVL, tempOperand, dest);
                } else asm.emit(type == Type.LONG ? ASMOperator.MOVQ : ASMOperator.MOVL, leftOperand, dest);
            }
        } else {
            ASMLocation rightLoc = new ASMLiteral(right);
            ASMRegister rightOperand = type == Type.LONG ? ASMRegister.RDX
                    : type == Type.BOOL ? ASMRegister.DL : ASMRegister.EDX;
            if (type == Type.LONG) {
                asm.emit(ASMOperator.MOVABSQ, rightLoc, rightOperand);
                rightLoc = rightOperand;
            }
            if (cfg.operator().getType() == BinaryOperator.BinOpType.REL_OP || cfg.operator().getType() == BinaryOperator.BinOpType.EQ_OP) {
                asm.emit(type == Type.LONG
                                ? ASMOperator.CMPQ
                                : type == Type.BOOL ? ASMOperator.CMPB : ASMOperator.CMPL,
                        rightLoc, leftLoc);
                ASMOperator nextOpcode = switch (cfg.operator()) {
                    case LT -> ASMOperator.SETL;
                    case GT -> ASMOperator.SETG;
                    case LEQ -> ASMOperator.SETLE;
                    case GEQ -> ASMOperator.SETGE;
                    case EQ -> ASMOperator.SETE;
                    case NEQ -> ASMOperator.SETNE;
                    default -> throw new AssertionError("This should never happen.");
                };
                asm.emit(nextOpcode, dest);
            } else {
                asm.emit(type == Type.LONG ? ASMOperator.MOVQ : ASMOperator.MOVL, leftLoc, dest);
                ASMOperator opcode2 = switch (cfg.operator()) {
                    case PLUS -> (type == Type.LONG ? ASMOperator.ADDQ : ASMOperator.ADDL);
                    case MINUS -> (type == Type.LONG ? ASMOperator.SUBQ : ASMOperator.SUBL);
                    case TIMES -> (type == Type.LONG ? ASMOperator.IMULQ : ASMOperator.IMULL);
                    default -> throw new AssertionError("This should never happen.");
                };
                asm.emit(opcode2, rightLoc, dest);
            }
        }
    }

    @Override
    public void visit(CFGBinaryInstruction cfg) {
        Type type = cfg.ctx().getType(cfg.left());

        var dest = new ASMVirtualRegister(cfg.address());
        var leftLoc = new ASMVirtualRegister(cfg.left());
        var rightLoc = new ASMVirtualRegister(cfg.right());

        if (cfg.ctx().getType(cfg.left()) == Type.RECORD) {
            var leftAddr = new ASMVirtualRegister(cfg.ctx().getSymbolTable().addVar(Type.LONG));
            asm.emit(ASMOperator.LEAQ, asm.getMemoryLocation(cfg.left()), leftAddr);
            leftLoc = leftAddr;
        }

        if (cfg.ctx().getType(cfg.right()) == Type.RECORD) {
            var rightAddr = new ASMVirtualRegister(cfg.ctx().getSymbolTable().addVar(Type.LONG));
            asm.emit(ASMOperator.LEAQ, asm.getMemoryLocation(cfg.right()), rightAddr);
            rightLoc = rightAddr;
        }

        if (cfg.operator() == BinaryOperator.DIVIDES || cfg.operator() == BinaryOperator.MOD) {
            asm.emit(type == Type.LONG ? ASMOperator.MOVQ : ASMOperator.MOVL,
                    leftLoc, type == Type.LONG ? ASMRegister.RAX : ASMRegister.EAX);
            asm.emit(type == Type.LONG ? ASMOperator.CQTO : ASMOperator.CLTD);
            asm.emit(type == Type.LONG ? ASMOperator.IDIVQ : ASMOperator.IDIVL,
                    rightLoc);
            asm.emit(type == Type.LONG ? ASMOperator.MOVQ : ASMOperator.MOVL,
                    cfg.operator() == BinaryOperator.MOD ? (type == Type.LONG ? ASMRegister.RDX : ASMRegister.EDX)
                            : (type == Type.LONG ? ASMRegister.RAX : ASMRegister.EAX),
                    dest);
        } else if (cfg.operator().getType() == BinaryOperator.BinOpType.REL_OP || cfg.operator().getType() == BinaryOperator.BinOpType.EQ_OP) {
            asm.emit(type == Type.LONG
                            ? ASMOperator.CMPQ
                            : type == Type.BOOL ? ASMOperator.CMPB : ASMOperator.CMPL,
                    rightLoc, leftLoc);
            ASMOperator nextOpcode = switch (cfg.operator()) {
                case LT -> ASMOperator.SETL;
                case GT -> ASMOperator.SETG;
                case LEQ -> ASMOperator.SETLE;
                case GEQ -> ASMOperator.SETGE;
                case EQ -> ASMOperator.SETE;
                case NEQ -> ASMOperator.SETNE;
                default -> throw new AssertionError("This should never happen.");
            };
            asm.emit(nextOpcode, dest);
        } else {
            if (rightLoc.equals(dest)) {
                var tmp = new ASMVirtualRegister(cfg.ctx().getSymbolTable().newNode(rightLoc.address()));
                asm.emit(type == Type.LONG ? ASMOperator.MOVQ : ASMOperator.MOVL, rightLoc, tmp);
                rightLoc = tmp;
            }
            asm.emit(type == Type.LONG ? ASMOperator.MOVQ : ASMOperator.MOVL, leftLoc, dest);
            ASMOperator opcode2 = switch (cfg.operator()) {
                case PLUS -> (type == Type.LONG ? ASMOperator.ADDQ : ASMOperator.ADDL);
                case MINUS -> (type == Type.LONG ? ASMOperator.SUBQ : ASMOperator.SUBL);
                case TIMES -> (type == Type.LONG ? ASMOperator.IMULQ : ASMOperator.IMULL);
                default -> throw new AssertionError("This should never happen.");
            };
            asm.emit(opcode2, rightLoc, dest);
        }
    }

    @Override
    public void visit(CFGBranchInstruction cfg) {
        var condLoc = new ASMVirtualRegister(cfg.boolAddr());
        asm.emit(ASMOperator.CMPB, new ASMLiteral(0), condLoc);
        asm.emit(ASMOperator.JNE, asm.getLabel(cfg.thenAddr()));
        asm.emit(ASMOperator.JMP, asm.getLabel(cfg.elseAddr()));
        // TODO peephole this away?
    }

    @Override
    public void visit(CFGCastInstruction cfg) {
        var dest = new ASMVirtualRegister(cfg.address());
        var operandLoc = new ASMVirtualRegister(cfg.operand());

        Type startingType = cfg.ctx().getType(cfg.operand());
        if (startingType == Type.RECORD) {
            asm.emit(ASMOperator.LEAQ, asm.getMemoryLocation(cfg.operand()), dest);
            return;
        }
        ASMOperator op = startingType == Type.LONG
                ? (cfg.type() == Type.LONG ? ASMOperator.MOVQ : ASMOperator.MOVL)
                : (cfg.type() == Type.LONG ? ASMOperator.MOVSLQ : ASMOperator.MOVL);

        asm.emit(op, operandLoc, dest);
    }

    @Override
    public void visit(CFGCopyInstruction cfg) {
        var dest = new ASMVirtualRegister(cfg.address());
        var srcLoc = new ASMVirtualRegister(cfg.operand());

        switch (cfg.ctx().getType(cfg.address())) {
            case INT:
                asm.emit(ASMOperator.MOVL, srcLoc, dest);
                break;
            case LONG:
                asm.emit(ASMOperator.MOVQ, srcLoc, dest);
                break;
            case BOOL:
                asm.emit(ASMOperator.MOVB, srcLoc, dest);
                break;
            default:
                throw new AssertionError("This should never happen.");
        }
    }

    @Override
    public void visit(CFGExceptionInstruction cfg) {
        asm.emit(ASMOperator.LEAQ, asm.getStringLiteral(cfg.msg()), ASMRegister.RSI);
        asm.emit(ASMOperator.MOVQ, new ASMLiteral(getFalloffString(cfg.msg()).length()), ASMRegister.RDX);
        asm.emit(ASMOperator.MOVQ, new ASMLiteral(2), ASMRegister.RDI);  // stderr
        asm.emit(ASMOperator.MOVQ, new ASMLiteral(1), ASMRegister.RAX);  // sys_write (syscall 1)
        asm.emit(ASMOperator.SYSCALL);

        asm.emit(ASMOperator.MOVQ, new ASMLiteral(60), ASMRegister.RAX); // sys_exit (syscall 60)
        asm.emit(ASMOperator.MOVQ, new ASMLiteral(-1), ASMRegister.RDI); // exit code -1
        asm.emit(ASMOperator.SYSCALL);
    }

    @Override
    public void visit(CFGJumpInstruction cfg) {
        CFGAddress dest = cfg.jumpAddr();
        // CFGBasicBlock destBlock = cfg.ctx().getSymbolTable().getBlock(dest);
        // CFGBasicBlock prevBlock = destBlock.getMethod().getBlocks().prev(destBlock);
        // if (prevBlock == null || !cfg.equals(prevBlock.getLastInstruction()))
        asm.emit(ASMOperator.JMP, asm.getLabel(cfg.jumpAddr()));
    }

    @Override
    public void visit(CFGLiteralInstruction cfg) {
        var dest = new ASMVirtualRegister(cfg.address());
        switch (cfg.literal().type()) {
            case INT:
                if (((IntLiteral) cfg.literal()).value() == 0)
                    asm.emit(ASMOperator.XORL, dest, dest);
                else
                    asm.emit(ASMOperator.MOVL, new ASMLiteral(cfg.literal()), dest);
                break;
            case LONG:
                if (((LongLiteral) cfg.literal()).value() == 0L)
                    asm.emit(ASMOperator.XORQ, dest, dest);
                else
                    asm.emit(ASMOperator.MOVABSQ, new ASMLiteral(cfg.literal()), dest);
                break;
            case BOOL:
                if (!((BoolLiteral) cfg.literal()).value())
                    asm.emit(ASMOperator.XORB, dest, dest);
                else
                    asm.emit(ASMOperator.MOVB, new ASMLiteral(cfg.literal()), dest);
                break;
            case STRING:
                asm.emit(ASMOperator.LEAQ, asm.getStringLiteral(((StringLiteral) cfg.literal()).escape()), dest);
                break;
            default:
                throw new AssertionError("This should never happen.");
        }
    }

    @Override
    public void visit(CFGMethodCallInstruction cfg) {
        for (int i = 0; i < Math.min(6, cfg.args().size()); i++) {
            var param = cfg.args().get(i);
            var address = new ASMVirtualRegister(param);

            if (cfg.ctx().getType(param) == Type.RECORD) {
                asm.emit(ASMOperator.LEAQ, asm.getMemoryLocation(param), ASMRegister.getMethodArg(Type.LONG, i));
                continue;
            }

            switch (cfg.ctx().getType(param)) {
                case INT:
                    asm.emit(ASMOperator.MOVL, address, ASMRegister.getMethodArg(Type.INT, i));
                    // asm.emit(ASMOperator.MOVSLQ, address, ASMRegister.getMethodArg(Type.LONG, i));
                    break;
                case BOOL:
                    asm.emit(ASMOperator.MOVZBL, address, ASMRegister.getMethodArg(Type.INT, i));
                    // asm.emit(ASMOperator.MOVZBQ, address, ASMRegister.getMethodArg(Type.LONG, i));
                    break;
                default:
                    asm.emit(ASMOperator.MOVQ, address, ASMRegister.getMethodArg(Type.LONG, i));
            }
        }

        int distancePushed = 8 * Math.max(0, cfg.args().size() - 6);
        // ensure 16-byte aligned
        int extraPush = (16 - (distancePushed % 16)) % 16;
        asm.emit(ASMOperator.SUBQ, new ASMLiteral(extraPush), ASMRegister.RSP);
        int pushed = extraPush;

        // push extra arguments onto stack in reverse order
        for (int i = cfg.args().size() - 1; i >= 6; i--) {
            var param = cfg.args().get(i);
            switch (cfg.ctx().getType(param)) {
                case BOOL:
                    asm.emit(ASMOperator.MOVZBL, new ASMVirtualRegister(param), ASMRegister.EAX, new ASMInstructionContext(pushed));
                    asm.emit(ASMOperator.PUSHQ, ASMRegister.RAX, new ASMInstructionContext(pushed));
                    pushed += 8;
                    break;
                case INT:
                case LONG:
                case STRING:
                    asm.emit(ASMOperator.PUSHQ, new ASMVirtualRegister(param), new ASMInstructionContext(pushed));
                    pushed += 8;
                    break;
                default:
                    asm.emit(ASMOperator.PUSHQ, asm.getMemoryLocation(param), new ASMInstructionContext(pushed));
                    pushed += 8;
            }
        }

        // set rax to 0 for external functions
        // (only needed if variable number of inputs, but hard to detect)
        if (cfg.ctx().getSymbolTable().existsExternalMethod(cfg.ctx().getSymbolTable().getMethodAddress(cfg.methodName()))
                && (cfg.methodName().equals("printf") || cfg.methodName().equals("scanf"))) // TODO special case for operandRegisters?
            asm.emit(ASMOperator.XORQ, ASMRegister.RAX, ASMRegister.RAX, new ASMInstructionContext(pushed));

        asm.emit(ASMOperator.CALL, asm.getMethodLabel(cfg.methodName()),
                new ASMInstructionContext(cfg.args().size(), extraPush, distancePushed + extraPush, pushed));
        asm.emit(ASMOperator.ADDQ, new ASMLiteral(distancePushed + extraPush), ASMRegister.RSP);
        if (cfg.address() != null) {
            switch (cfg.ctx().getType(cfg.address())) {
                case INT:
                    asm.emit(ASMOperator.MOVL, ASMRegister.EAX, new ASMVirtualRegister(cfg.address()));
                    break;
                case LONG:
                    asm.emit(ASMOperator.MOVQ, ASMRegister.RAX, new ASMVirtualRegister(cfg.address()));
                    break;
                case BOOL:
                    asm.emit(ASMOperator.MOVB, ASMRegister.AL, new ASMVirtualRegister(cfg.address()));
                    break;
            }
        }
    }

    @Override
    public void visit(CFGReturnInstruction cfg) {
        if (cfg.returnAddress() != null) {
            var retLoc = new ASMVirtualRegister(cfg.returnAddress());
            switch (cfg.ctx().getType(cfg.returnAddress())) {
                case INT:
                    asm.emit(ASMOperator.MOVL, retLoc, ASMRegister.EAX);
                    break;
                case LONG:
                    asm.emit(ASMOperator.MOVQ, retLoc, ASMRegister.RAX);
                    break;
                case BOOL:
                    asm.emit(ASMOperator.MOVB, retLoc, ASMRegister.AL);
                    break;
            }
        }

        // in register allocation, this will be replaced with an appropriate addq instruction to move the stack pointer
        // as we do not use %rbp; this primarily serves as first marker for function end
        asm.emit(ASMOperator.LEAVE);
        asm.emit(ASMOperator.RET);
    }

    @Override
    public void visit(CFGUnaryInstruction cfg) {
        Type type = cfg.ctx().getType(cfg.address());
        var dest = new ASMVirtualRegister(cfg.address());
        var operandLoc = new ASMVirtualRegister(cfg.operand());

        if (type == Type.INT) {
            asm.emit(ASMOperator.MOVL, operandLoc, dest);
            asm.emit(ASMOperator.NEGL, dest);
        } else if (type == Type.LONG) {
            asm.emit(ASMOperator.MOVQ, operandLoc, dest);
            asm.emit(ASMOperator.NEGQ, dest);
        } else if (type == Type.BOOL) {
            asm.emit(ASMOperator.MOVB, operandLoc, dest);
            asm.emit(ASMOperator.XORB, new ASMLiteral(1), dest);
        }
    }

    @Override
    public void visit(CFGPhiInstruction cfg) {
        throw new AssertionError("Phi instruction not eliminated before assembly generation.");
    }

    private static String getFalloffString(String m) {
        return String.format("Runtime error: non-void method %s fell off the end without returning first value.\\n", m);
    }

}
