package recaf.asm;

import recaf.cfg.*;
import recaf.general.*;
import recaf.utils.MagicUtils;
import recaf.utils.NumUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Naively converts a CFG to assembly code via plain macro expansion,
 * without any register allocation
 */
public class CFGtoASM implements CFGVisitor {

    private final AssemblyStringBuilder asm;

    public CFGtoASM(AssemblyStringBuilder asm) {
        this.asm = asm;
    }

    @Override
    public void visit(CFGProgram cfg) {
        asm.emit(new ASMDirective(ASMDirectiveOp.DATA));
        for (CFGAddress globalVar : cfg.ctx().getGlobalVars()) {
            asm.emit(new ASMLabelInstruction(new ASMLabel(cfg.ctx().getSymbolTable().getVar(globalVar).getName())));
            asm.emit(new ASMDirective(ASMDirectiveOp.ZERO, asm.sizeof(globalVar)));
            asm.emit(new ASMDirective(ASMDirectiveOp.ALIGN, 16));
        }
        for (String strLiteral : cfg.ctx().getSymbolTable().getAllStrings()) {
            asm.emit(new ASMDirective(ASMDirectiveOp.ALIGN, 16));
            asm.emit(new ASMLabelInstruction(asm.getStringLabel(strLiteral)));
            asm.emit(new ASMDirective(ASMDirectiveOp.STRING, String.format("\"%s\"", strLiteral)));
        }

        asm.emit(new ASMDirective(ASMDirectiveOp.TEXT));
        cfg.getMethods().forEach(method -> method.accept(this));

        asm.emit(new ASMDirective(ASMDirectiveOp.SECTION, ".note.GNU-stack,\"\",@progbits"));
    }

    @Override
    public void visit(CFGMethod cfg) {
        if (cfg.getName().equals("main"))
            asm.emit(new ASMDirective(ASMDirectiveOp.GLOBL, "main"));
        asm.emit(new ASMLabelInstruction(asm.getMethodLabel(cfg.getName())));

        // prologue
        asm.emit(ASMOperator.PUSHQ, ASMRegister.RBP);
        asm.emit(ASMOperator.MOVQ, ASMRegister.RSP, ASMRegister.RBP);

        asm.clearStack();
        // pre-register
        for (int i = 6; i < cfg.getParams().size(); i++) {
            asm.manuallyRegisterVariable(cfg.getParams().get(i), 8 * i - 32);
        }
        extractLocalVars(cfg).forEach(asm::registerVariable);
        int stackShift = asm.getStackOffset();
        // ensure 16-byte alignment
        int padding = (16 - (stackShift % 16)) % 16;
        stackShift += padding;
        asm.emit(ASMOperator.SUBQ, new ASMLiteral(stackShift), ASMRegister.RSP);

        for (int i = 0; i < Math.min(6, cfg.getParams().size()); i++) {
            switch (cfg.ctx().getType(cfg.getParams().get(i))) {
                case INT:
                    asm.emit(ASMOperator.MOVL, ASMRegister.getMethodArg(Type.INT, i), asm.getMemoryLocation(cfg.getParams().get(i)));
                    break;
                case LONG, POINTER:
                    asm.emit(ASMOperator.MOVQ, ASMRegister.getMethodArg(Type.LONG, i), asm.getMemoryLocation(cfg.getParams().get(i)));
                    break;
                case BOOL:
                    asm.emit(ASMOperator.MOVL, ASMRegister.getMethodArg(Type.INT, i), ASMRegister.EAX);
                    asm.emit(ASMOperator.MOVB, ASMRegister.AL, asm.getMemoryLocation(cfg.getParams().get(i)));
                    break;
                default:
                    throw new AssertionError("This should never happen.");
            }
        }

        // generate basic blocks
        cfg.getBlocks().forEach(block -> block.accept(this));

        // leave
        asm.emit(ASMOperator.LEAVE);
        asm.emit(ASMOperator.RET);
    }

    /**
     * Find first listing of all local variables used within first method, for which space should be allocated.
     * @param cfg the method
     * @return first list of all local variables
     */
    private List<CFGAddress> extractLocalVars(CFGMethod cfg) {
        List<CFGAddress> localVars = new ArrayList<>(cfg.getParams());
        for (CFGBasicBlock block : cfg.getBlocks()) {
            for (CFGInstruction instruction : block.getInstructions()) {
                CFGAddress addr = instruction.address();
                if (!cfg.ctx().isGlobalVar(addr) && cfg.ctx().getSymbolTable().getVar(addr) != null)
                    localVars.add(addr);
            }
        }
        return localVars;
    }

    @Override
    public void visit(CFGBasicBlock cfg) {
        asm.emit(new ASMLabelInstruction(asm.getLabel(cfg.address())));
        cfg.getAllInstructions().forEach(instruction -> instruction.accept(this));
    }

    @Override
    public void visit(CFGReadInstruction cfg) {
        asm.emitLL(cfg);

        var dest = asm.getMemoryLocation(cfg.address());
        var indexLoc = asm.getMemoryLocation(cfg.indexAddress());
        ASMLocation arrayLoc;

        asm.emit(ASMOperator.MOVL, indexLoc, ASMRegister.EAX);
        asm.emit(ASMOperator.CLTQ);
        if (cfg.ctx().getType(cfg.recordAddress()) == Type.POINTER) {
            asm.emit(ASMOperator.MOVQ, asm.getMemoryLocation(cfg.recordAddress()), ASMRegister.RCX);
            arrayLoc = new ASMStackAddressArray(0, ASMRegister.RCX, ASMRegister.RAX, cfg.width());
        } else {
            arrayLoc = asm.getMemoryLocationArray(cfg.recordAddress(), cfg.width());
        }
        switch (cfg.width()) {
            case 4:
                asm.emit(ASMOperator.MOVL, arrayLoc, ASMRegister.EAX);
                asm.emit(ASMOperator.MOVL, ASMRegister.EAX, dest);
                break;
            case 8:
                asm.emit(ASMOperator.MOVQ, arrayLoc, ASMRegister.RAX);
                asm.emit(ASMOperator.MOVQ, ASMRegister.RAX, dest);
                break;
            case 1:
                asm.emit(ASMOperator.MOVZBL, arrayLoc, ASMRegister.EAX);
                asm.emit(ASMOperator.MOVB, ASMRegister.AL, dest);
                break;
            default:
                throw new AssertionError("unsupported record read width: " + cfg.width());
        }
    }

    @Override
    public void visit(CFGWriteInstruction cfg) {
        asm.emitLL(cfg);

        var indexLoc = asm.getMemoryLocation(cfg.indexAddress());
        var valueLoc = asm.getMemoryLocation(cfg.valueAddress());
        ASMLocation arrayLoc;

        asm.emit(ASMOperator.MOVL, indexLoc, ASMRegister.EAX);
        asm.emit(ASMOperator.CLTQ);
        if (cfg.ctx().getType(cfg.recordAddress()) == Type.POINTER) {
            asm.emit(ASMOperator.MOVQ, asm.getMemoryLocation(cfg.recordAddress()), ASMRegister.RCX);
            arrayLoc = new ASMStackAddressArray(0, ASMRegister.RCX, ASMRegister.RAX, cfg.width());
        } else {
            arrayLoc = asm.getMemoryLocationArray(cfg.recordAddress(), cfg.width());
        }
        switch (cfg.width()) {
            case 4:
                if (cfg.ctx().getType(cfg.recordAddress()) == Type.POINTER) {
                    asm.emit(ASMOperator.MOVL, valueLoc, ASMRegister.R11D);
                    asm.emit(ASMOperator.MOVL, ASMRegister.R11D, arrayLoc);
                } else {
                    asm.emit(ASMOperator.MOVL, valueLoc, ASMRegister.EDX);
                    asm.emit(ASMOperator.MOVL, ASMRegister.EDX, arrayLoc);
                }
                break;
            case 8:
                if (cfg.ctx().getType(cfg.recordAddress()) == Type.POINTER) {
                    asm.emit(ASMOperator.MOVQ, valueLoc, ASMRegister.R11);
                    asm.emit(ASMOperator.MOVQ, ASMRegister.R11, arrayLoc);
                } else {
                    asm.emit(ASMOperator.MOVQ, valueLoc, ASMRegister.RDX);
                    asm.emit(ASMOperator.MOVQ, ASMRegister.RDX, arrayLoc);
                }
                break;
            case 1:
                if (cfg.ctx().getType(cfg.recordAddress()) == Type.POINTER) {
                    asm.emit(ASMOperator.MOVZBL, valueLoc, ASMRegister.R11D);
                    asm.emit(ASMOperator.MOVB, ASMRegister.R11B, arrayLoc);
                } else {
                    asm.emit(ASMOperator.MOVZBL, valueLoc, ASMRegister.EDX);
                    asm.emit(ASMOperator.MOVB, ASMRegister.DL, arrayLoc);
                }
                break;
            default:
                throw new AssertionError("unsupported record write width: " + cfg.width());
        }
    }

    @Override
    public void visit(CFGBinaryImmediateInstruction cfg) {
        asm.emitLL(cfg);

        Type type = cfg.ctx().getType(cfg.left());

        var dest = asm.getMemoryLocation(cfg.address());
        var leftLoc = asm.getMemoryLocation(cfg.left());
        var right = cfg.right();

        // Load operands
        ASMRegister leftOperand = type == Type.LONG ? ASMRegister.RAX
                : type == Type.BOOL ? ASMRegister.AL : ASMRegister.EAX;
        ASMRegister rightOperand = type == Type.LONG ? ASMRegister.RDX
                : type == Type.BOOL ? ASMRegister.DL : ASMRegister.EDX;
        /*
        ASMRegister rightOperand = type == Type.LONG ? ASMRegister.RSI
                : type == Type.BOOL ? ASMRegister.SIL : ASMRegister.ESI;
         */

        ASMRegister tempOperand = type == Type.LONG ? ASMRegister.RCX
                : type == Type.BOOL ? ASMRegister.CL : ASMRegister.ECX;
        ASMOperator opcode1 = switch (cfg.ctx().getType(cfg.left())) {
            case INT -> ASMOperator.MOVL;
            case LONG -> ASMOperator.MOVQ;
            case BOOL -> ASMOperator.MOVB;
            default -> throw new AssertionError("This should never happen.");
        };
        asm.emit(opcode1, leftLoc, leftOperand);

        // Perform operation
        ASMRegister destLoc = switch (cfg.operator()) {
            case PLUS, MINUS, TIMES, DIVIDES -> leftOperand;
            // case MOD -> (type == Type.LONG ? ASMRegister.RDX : ASMRegister.EDX);
            case MOD -> rightOperand;
            case LT, GT, LEQ, GEQ, EQ, NEQ -> ASMRegister.AL;
            default -> throw new AssertionError("This should never happen.");
        };
        ASMOperator opcode2 = switch (cfg.operator()) {
            case PLUS -> (type == Type.LONG ? ASMOperator.ADDQ : ASMOperator.ADDL);
            case MINUS -> (type == Type.LONG ? ASMOperator.SUBQ : ASMOperator.SUBL);
            case TIMES -> (type == Type.LONG ? ASMOperator.IMULQ : ASMOperator.IMULL);
            case DIVIDES, MOD -> (type == Type.LONG ? ASMOperator.IDIVQ : ASMOperator.IDIVL);
            case LT, GT, LEQ, GEQ, EQ, NEQ -> (type == Type.LONG ? ASMOperator.CMPQ : type == Type.BOOL ? ASMOperator.CMPB : ASMOperator.CMPL);
            default -> throw new AssertionError("This should never happen.");
        };
        NumUtils pf = new NumUtils(right);
        if (cfg.operator() == BinaryOperator.TIMES && Set.of(2L, 3L, 4L, 5L, 8L, 9L).contains(pf.asLong())) {
            opcode2 = type == Type.LONG ? ASMOperator.LEAQ : ASMOperator.LEAL;
            ASMRegister leftLong = leftOperand.toType(Type.LONG);
            asm.emit(opcode2,
                    switch ((int) pf.asLong()) {
                        case 2 -> new ASMStackAddressArray(0, leftLong, leftLong, 1);
                        case 3 -> new ASMStackAddressArray(0, leftLong, leftLong, 2);
                        case 4 -> new ASMStackAddressArray(0, null, leftLong, 4);
                        case 5 -> new ASMStackAddressArray(0, leftLong, leftLong, 4);
                        case 8 -> new ASMStackAddressArray(0, null, leftLong, 8);
                        case 9 -> new ASMStackAddressArray(0, leftLong, leftLong, 8);
                        default -> throw new AssertionError("This should never happen.");
                    },
                    leftOperand
            );
            if (pf.sign() == -1)
                asm.emit(type == Type.LONG ? ASMOperator.NEGQ : ASMOperator.NEGL, leftOperand);
        } else if (cfg.operator() == BinaryOperator.TIMES && pf.isPowerOf(2)) {
            opcode2 = type == Type.LONG ? ASMOperator.SHLQ : ASMOperator.SHLL;
            if (!pf.isUnit())
                asm.emit(opcode2, new ASMLiteral(pf.vp(2)), leftOperand);
            if (pf.sign() == -1)
                asm.emit(type == Type.LONG ? ASMOperator.NEGQ : ASMOperator.NEGL, leftOperand);
        } else if (cfg.operator() == BinaryOperator.DIVIDES || cfg.operator() == BinaryOperator.MOD) {
            if (pf.isUnit()) {
                if (cfg.operator() == BinaryOperator.MOD)
                    asm.emit(type == Type.LONG ? ASMOperator.XORQ : ASMOperator.XORL, destLoc, destLoc);
                else if (pf.sign() == -1)
                    asm.emit(type == Type.LONG ? ASMOperator.NEGQ : ASMOperator.NEGL, leftOperand);
            } else if (pf.isPowerOf(2)) {
                int exp = pf.vp(2);
                if (cfg.operator() == BinaryOperator.DIVIDES) {
                    if (type == Type.INT)
                        asm.emit(ASMOperator.LEAL, new ASMStackAddress((1 << exp) - 1, leftOperand), rightOperand);
                    else {
                        asm.emit(ASMOperator.MOVABSQ, new ASMLiteral((1L << exp) - 1), rightOperand);
                        asm.emit(ASMOperator.ADDQ, leftOperand, rightOperand);
                    }

                    asm.emit(type == Type.LONG ? ASMOperator.TESTQ : ASMOperator.TESTL, leftOperand, leftOperand);
                    asm.emit(type == Type.LONG ? ASMOperator.CMOVSQ : ASMOperator.CMOVSL, rightOperand, leftOperand);
                    asm.emit(type == Type.LONG ? ASMOperator.SARQ : ASMOperator.SARL, new ASMLiteral(exp), leftOperand);

                    if (pf.sign() == -1)
                        asm.emit(type == Type.LONG ? ASMOperator.NEGQ : ASMOperator.NEGL, leftOperand);
                } else { // mod
                    int numBits = type == Type.LONG ? 64 : 32;
                    asm.emit(type == Type.LONG ? ASMOperator.MOVQ : ASMOperator.MOVL, leftOperand, destLoc);
                    asm.emit(type == Type.LONG ? ASMOperator.SARQ : ASMOperator.SARL, new ASMLiteral(numBits - 1), leftOperand);
                    asm.emit(type == Type.LONG ? ASMOperator.SHRQ : ASMOperator.SHRL, new ASMLiteral(numBits - exp), leftOperand);
                    asm.emit(type == Type.LONG ? ASMOperator.ADDQ : ASMOperator.ADDL, leftOperand, destLoc);
                    if (type == Type.INT)
                        asm.emit(ASMOperator.ANDL, new ASMLiteral((1L << exp) - 1), destLoc);
                    else {
                        asm.emit(ASMOperator.MOVABSQ, new ASMLiteral((1L << exp) - 1), tempOperand);
                        asm.emit(ASMOperator.ANDQ, tempOperand, destLoc);
                    }
                    asm.emit(type == Type.LONG ? ASMOperator.SUBQ : ASMOperator.SUBL, leftOperand, destLoc);
                }
            } else {
                MagicUtils.Magic magic = type == Type.LONG
                        ? MagicUtils.magic64(((LongLiteral) right).value())
                        : MagicUtils.magic32(((IntLiteral) right).value());
                int numBits = type == Type.LONG ? 64 : 32;

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
                    asm.emit(type == Type.LONG ? ASMOperator.MOVQ : ASMOperator.MOVL, tempOperand, destLoc);
                }
            }
        } else {
            if (cfg.ctx().getType(cfg.left()) == Type.LONG) {
                asm.emit(ASMOperator.MOVABSQ, new ASMLiteral(right), rightOperand);
                asm.emit(opcode2, rightOperand, leftOperand);
            } else {
                asm.emit(opcode2, new ASMLiteral(right), leftOperand);
            }
        }

        // If boolean operation, extract comparison
        if (cfg.operator().getType() == BinaryOperator.BinOpType.REL_OP
                || cfg.operator().getType() == BinaryOperator.BinOpType.EQ_OP) {
            ASMOperator nextOpcode = switch (cfg.operator()) {
                case LT -> ASMOperator.SETL;
                case GT -> ASMOperator.SETG;
                case LEQ -> ASMOperator.SETLE;
                case GEQ -> ASMOperator.SETGE;
                case EQ -> ASMOperator.SETE;
                case NEQ -> ASMOperator.SETNE;
                default -> throw new AssertionError("This should never happen.");
            };
            asm.emit(nextOpcode, destLoc);
        }

        // Store result in destination
        ASMOperator opcode3 = switch (cfg.ctx().getType(cfg.address())) {
            case INT -> ASMOperator.MOVL;
            case LONG -> ASMOperator.MOVQ;
            case BOOL -> ASMOperator.MOVB;
            default -> throw new AssertionError("This should never happen.");
        };
        asm.emit(opcode3, destLoc, dest);
    }

    @Override
    public void visit(CFGBinaryInstruction cfg) {
        asm.emitLL(cfg);

        Type type = cfg.ctx().getType(cfg.left());

        var dest = asm.getMemoryLocation(cfg.address());
        var leftLoc = asm.getMemoryLocation(cfg.left());
        var rightLoc = asm.getMemoryLocation(cfg.right());

        // Load operands
        ASMRegister leftOperand = type == Type.LONG || type == Type.POINTER ? ASMRegister.RAX
                : type == Type.BOOL ? ASMRegister.AL : ASMRegister.EAX;
        ASMOperator opcode1 = switch (cfg.ctx().getType(cfg.left())) {
            case INT -> ASMOperator.MOVL;
            case LONG, POINTER -> ASMOperator.MOVQ;
            case BOOL -> ASMOperator.MOVB;
            default -> throw new AssertionError("This should never happen.");
        };
        asm.emit(opcode1, leftLoc, leftOperand);

        // Perform operation
        ASMRegister destLoc = switch (cfg.operator()) {
            case PLUS, MINUS, TIMES, DIVIDES -> (type == Type.LONG ? ASMRegister.RAX : ASMRegister.EAX);
            case MOD -> (type == Type.LONG ? ASMRegister.RDX : ASMRegister.EDX);
            case LT, GT, LEQ, GEQ, EQ, NEQ -> ASMRegister.AL;
            default -> throw new AssertionError("This should never happen.");
        };
        ASMOperator opcode2 = switch (cfg.operator()) {
            case PLUS -> (type == Type.LONG ? ASMOperator.ADDQ : ASMOperator.ADDL);
            case MINUS -> (type == Type.LONG ? ASMOperator.SUBQ : ASMOperator.SUBL);
            case TIMES -> (type == Type.LONG ? ASMOperator.IMULQ : ASMOperator.IMULL);
            case DIVIDES, MOD -> (type == Type.LONG ? ASMOperator.IDIVQ : ASMOperator.IDIVL);
            case LT, GT, LEQ, GEQ, EQ, NEQ -> (type == Type.LONG || type == Type.POINTER ? ASMOperator.CMPQ
                    : type == Type.BOOL ? ASMOperator.CMPB : ASMOperator.CMPL);
            default -> throw new AssertionError("This should never happen.");
        };
        if (cfg.operator() == BinaryOperator.DIVIDES || cfg.operator() == BinaryOperator.MOD) {
            asm.emit(type == Type.LONG ? ASMOperator.CQTO : ASMOperator.CLTD);
            asm.emit(opcode2, rightLoc);
        } else {
            asm.emit(opcode2, rightLoc, leftOperand);
        }

        // If boolean operation, extract comparison
        if (cfg.operator().getType() == BinaryOperator.BinOpType.REL_OP
                || cfg.operator().getType() == BinaryOperator.BinOpType.EQ_OP) {
            ASMOperator nextOpcode = switch (cfg.operator()) {
                case LT -> ASMOperator.SETL;
                case GT -> ASMOperator.SETG;
                case LEQ -> ASMOperator.SETLE;
                case GEQ -> ASMOperator.SETGE;
                case EQ -> ASMOperator.SETE;
                case NEQ -> ASMOperator.SETNE;
                default -> throw new AssertionError("This should never happen.");
            };
            asm.emit(nextOpcode, destLoc);
        }

        // Store result in destination
        ASMOperator opcode3 = switch (cfg.ctx().getType(cfg.address())) {
            case INT -> ASMOperator.MOVL;
            case LONG -> ASMOperator.MOVQ;
            case BOOL -> ASMOperator.MOVB;
            default -> throw new AssertionError("This should never happen.");
        };
        asm.emit(opcode3, destLoc, dest);
    }

    @Override
    public void visit(CFGBranchInstruction cfg) {
        asm.emitLL(cfg);

        var condLoc = asm.getMemoryLocation(cfg.boolAddr());
        asm.emit(ASMOperator.MOVZBL, condLoc, ASMRegister.EAX);
        asm.emit(ASMOperator.CMPB, new ASMLiteral(0), ASMRegister.AL);
        asm.emit(ASMOperator.JNE, asm.getLabel(cfg.thenAddr()));
        asm.emit(ASMOperator.JMP, asm.getLabel(cfg.elseAddr()));
    }

    @Override
    public void visit(CFGCastInstruction cfg) {
        asm.emitLL(cfg);

        var dest = asm.getMemoryLocation(cfg.address());
        var operandLoc = asm.getMemoryLocation(cfg.operand());

        Type startingType = cfg.ctx().getType(cfg.operand());
        asm.emit(startingType == Type.LONG ? ASMOperator.MOVQ : ASMOperator.MOVSLQ,
                operandLoc, ASMRegister.RAX);

        asm.emit(cfg.type() == Type.LONG ? ASMOperator.MOVQ : ASMOperator.MOVL,
                cfg.type() == Type.LONG ? ASMRegister.RAX : ASMRegister.EAX,
                dest);
    }

    @Override
    public void visit(CFGCopyInstruction cfg) {
        asm.emitLL(cfg);

        var dest = asm.getMemoryLocation(cfg.address());
        var srcLoc = asm.getMemoryLocation(cfg.operand());

        switch (cfg.ctx().getType(cfg.address())) {
            case INT:
                asm.emit(ASMOperator.MOVL, srcLoc, ASMRegister.EAX);
                asm.emit(ASMOperator.MOVL, ASMRegister.EAX, dest);
                break;
            case LONG, POINTER:
                asm.emit(ASMOperator.MOVQ, srcLoc, ASMRegister.RAX);
                asm.emit(ASMOperator.MOVQ, ASMRegister.RAX, dest);
                break;
            case BOOL:
                asm.emit(ASMOperator.MOVZBL, srcLoc, ASMRegister.EAX);
                asm.emit(ASMOperator.MOVB, ASMRegister.AL, dest);
                break;
            default:
                throw new AssertionError("This should never happen.");
        }
    }

    @Override
    public void visit(CFGExceptionInstruction cfg) {
        asm.emitLL(cfg);

        asm.emit(new ASMComment("Exception"));

        asm.emit(ASMOperator.LEAQ, asm.getStringLiteral(cfg.msg()), ASMRegister.RSI);
        asm.emit(ASMOperator.MOVQ, new ASMLiteral(cfg.msg().length()), ASMRegister.RDX);
        asm.emit(ASMOperator.MOVQ, new ASMLiteral(2), ASMRegister.RDI);  // stderr
        asm.emit(ASMOperator.MOVQ, new ASMLiteral(1), ASMRegister.RAX);  // sys_write (syscall 1)
        asm.emit(ASMOperator.SYSCALL);

        asm.emit(ASMOperator.MOVQ, new ASMLiteral(60), ASMRegister.RAX); // sys_exit (syscall 60)
        asm.emit(ASMOperator.MOVQ, new ASMLiteral(-1), ASMRegister.RDI); // exit code -1
        asm.emit(ASMOperator.SYSCALL);
    }

    @Override
    public void visit(CFGJumpInstruction cfg) {
        asm.emitLL(cfg);
        CFGAddress dest = cfg.jumpAddr();
        CFGBasicBlock destBlock = cfg.ctx().getSymbolTable().getBlock(dest);
        CFGBasicBlock prevBlock = destBlock.getMethod().getBlocks().prev(destBlock);
        if (prevBlock == null || !cfg.equals(prevBlock.getLastInstruction()))
            asm.emit(ASMOperator.JMP, asm.getLabel(cfg.jumpAddr()));
    }

    @Override
    public void visit(CFGLiteralInstruction cfg) {
        asm.emitLL(cfg);

        var dest = asm.getMemoryLocation(cfg.address());
        switch (cfg.literal().type()) {
            case INT:
                asm.emit(ASMOperator.MOVL, new ASMLiteral(cfg.literal()), dest);
                break;
            case LONG:
                if (((LongLiteral) cfg.literal()).value() == 0L)
                    asm.emit(ASMOperator.XORQ, ASMRegister.RAX, ASMRegister.RAX);
                else
                    asm.emit(ASMOperator.MOVABSQ, new ASMLiteral(cfg.literal()), ASMRegister.RAX);
                asm.emit(ASMOperator.MOVQ, ASMRegister.RAX, dest);
                break;
            case BOOL:
                asm.emit(ASMOperator.MOVB, new ASMLiteral(cfg.literal()), dest);
                break;
            case STRING:
                asm.emit(ASMOperator.LEAQ, asm.getStringLiteral(((StringLiteral) cfg.literal()).value()), ASMRegister.RAX);
                asm.emit(ASMOperator.MOVQ, ASMRegister.RAX, dest);
                break;
            default:
                throw new AssertionError("This should never happen.");
        }
    }

    @Override
    public void visit(CFGMethodCallInstruction cfg) {
        asm.emitLL(cfg);

        for (int i = 0; i < Math.min(6, cfg.args().size()); i++) {
            var address = asm.getMemoryLocation(cfg.args().get(i));

            if (cfg.ctx().getType(cfg.args().get(i)) == Type.RECORD) {
                asm.emit(ASMOperator.LEAQ, address, ASMRegister.getMethodArg(Type.LONG, i));
                continue;
            }

            switch (cfg.ctx().getType(cfg.args().get(i))) {
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

        // push extra arguments onto stack in reverse order
        for (int i = cfg.args().size() - 1; i >= 6; i--) {
            switch (cfg.ctx().getType(cfg.args().get(i))) {
                case BOOL:
                    asm.emit(ASMOperator.MOVZBL, asm.getMemoryLocation(cfg.args().get(i)), ASMRegister.EAX);
                    // asm.emit(ASMOperator.MOVZBQ, asm.getMemoryLocation(cfg.args().get(i)), ASMRegister.RAX);
                    asm.emit(ASMOperator.PUSHQ, ASMRegister.RAX);
                    break;
                case INT:
                    asm.emit(ASMOperator.MOVL, asm.getMemoryLocation(cfg.args().get(i)), ASMRegister.EAX);
                    // asm.emit(ASMOperator.MOVSLQ, asm.getMemoryLocation(cfg.args().get(i)), ASMRegister.RAX);
                    asm.emit(ASMOperator.PUSHQ, ASMRegister.RAX);
                    break;
                default:
                    asm.emit(ASMOperator.PUSHQ, asm.getMemoryLocation(cfg.args().get(i)));
            }
        }

        // set rax to 0 for external functions
        // (only needed if variable number of inputs, but hard to detect)
        if (cfg.ctx().getSymbolTable().existsExternalMethod(cfg.ctx().getSymbolTable().getMethodAddress(cfg.methodName())))
            asm.emit(ASMOperator.MOVQ, new ASMLiteral(0), ASMRegister.RAX);

        asm.emit(ASMOperator.CALL, asm.getMethodLabel(cfg.methodName()));
        asm.emit(ASMOperator.ADDQ, new ASMLiteral(distancePushed + extraPush), ASMRegister.RSP);
        if (cfg.address() != null) {
            switch (cfg.ctx().getType(cfg.address())) {
                case INT:
                    asm.emit(ASMOperator.MOVL, ASMRegister.EAX, asm.getMemoryLocation(cfg.address()));
                    break;
                case LONG, POINTER:
                    asm.emit(ASMOperator.MOVQ, ASMRegister.RAX, asm.getMemoryLocation(cfg.address()));
                    break;
                case BOOL:
                    asm.emit(ASMOperator.MOVB, ASMRegister.AL, asm.getMemoryLocation(cfg.address()));
                    break;
            }
        }
    }

    @Override
    public void visit(CFGReturnInstruction cfg) {
        asm.emitLL(cfg);

        if (cfg.returnAddress() != null) {
            var retLoc = asm.getMemoryLocation(cfg.returnAddress());
            switch (cfg.ctx().getType(cfg.returnAddress())) {
                case INT:
                    asm.emit(ASMOperator.MOVL, retLoc, ASMRegister.EAX);
                    break;
                case LONG, POINTER:
                    asm.emit(ASMOperator.MOVQ, retLoc, ASMRegister.RAX);
                    break;
                case BOOL:
                    asm.emit(ASMOperator.MOVZBL, retLoc, ASMRegister.EAX);
                    break;
            }
        }

        asm.emit(ASMOperator.LEAVE);
        asm.emit(ASMOperator.RET);
    }

    @Override
    public void visit(CFGUnaryInstruction cfg) {
        asm.emitLL(cfg);

        Type type = cfg.ctx().getType(cfg.address());
        var dest = asm.getMemoryLocation(cfg.address());
        var operandLoc = asm.getMemoryLocation(cfg.operand());

        if (type == Type.INT) {
            asm.emit(ASMOperator.MOVL, operandLoc, ASMRegister.EAX);
            asm.emit(ASMOperator.NEGL, ASMRegister.EAX);
            asm.emit(ASMOperator.MOVL, ASMRegister.EAX, dest);
        } else if (type == Type.LONG) {
            asm.emit(ASMOperator.MOVQ, operandLoc, ASMRegister.RAX);
            asm.emit(ASMOperator.NEGQ, ASMRegister.RAX);
            asm.emit(ASMOperator.MOVQ, ASMRegister.RAX, dest);
        } else if (type == Type.BOOL) {
            asm.emit(ASMOperator.MOVB, operandLoc, ASMRegister.AL);
            asm.emit(ASMOperator.XORB, new ASMLiteral(1), ASMRegister.AL);
            asm.emit(ASMOperator.MOVB, ASMRegister.AL, dest);
        }
    }

    @Override
    public void visit(CFGPhiInstruction cfg) {
        throw new AssertionError("Phi instruction not eliminated before assembly generation.");
    }

}
