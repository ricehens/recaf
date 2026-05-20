package recaf.opt;

import recaf.cfg.*;
import recaf.general.BinaryOperator;
import recaf.general.Literal;
import recaf.general.Type;
import recaf.general.UnaryOperator;
import recaf.utils.DominatorTree;
import recaf.utils.PostDominatorTree;

import java.util.*;

/**
 * Implements VanDrunen's Global-Value-Numbering Partial Redundancy Elimination algorithm.
 */
public class GVNPRE extends SSATransformation {

    private DominatorTree<CFGBasicBlock> dt;
    private PostDominatorTree<CFGBasicBlock> pdt;

    /**
     * Constructs first new GVNPRE instance
     *
     * @param ctx the CFG context
     * @param method the target method
     */
    public GVNPRE(CFGContext ctx, CFGMethod method) {
        super(ctx, method);
        dt = data.getDominatorTree();
        pdt = new PostDominatorTree<>(method.getBlocks(), new CFGBasicBlock(ctx));
    }

    private final ValueTable vt = new ValueTable();
    private final Value NULL = new Value(Type.UNKNOWN);

    @Override
    public boolean apply() {
        boolean returnValue = false;

        // build sets phase 1
        buildSets1();
// System.out.println("build sets 1 complete");

        // build sets phase 2
        buildSets2();
// System.out.println("build sets 2 complete");

        // insert
        returnValue |= insert();
// System.out.println("insert complete");

        // eliminate
        returnValue |= eliminate();
// System.out.println("eliminate complete");

        return returnValue;
    }

    private Map<CFGBasicBlock, Map<Value, Expression>> expGen = new HashMap<>();
    private Map<CFGBasicBlock, Set<Temporary>> phiGen = new HashMap<>();
    private Map<CFGBasicBlock, Set<Temporary>> tmpGen = new HashMap<>();
    // only hold leaders
    private Map<CFGBasicBlock, Map<Value, Temporary>> availOut = new HashMap<>();
    private Map<CFGBasicBlock, Map<Value, Expression>> anticIn = new HashMap<>();
    private Map<CFGBasicBlock, Set<Temporary>> newSets = new HashMap<>();

    // special logic for immediates
    private Map<Value, Lit> immediates = new HashMap<>();

    /** Constructs gen sets */
    private void buildSets1() {
        for (CFGBasicBlock b : method.getBlocks()) {
            expGen.put(b, new HashMap<>());
            phiGen.put(b, new HashSet<>());
            tmpGen.put(b, new HashSet<>());
        }

        availOut.put(dt.getRoot(), new HashMap<>());
        for (CFGAddress param : method.getParams()) {
            addEntryValue(param);
        }
        for (CFGAddress addr : data.getLocalVars())
            if (data.getDefinition(addr) == null)
                addEntryValue(addr);

        buildSets1DFS(dt.getRoot());
    }

    private void addEntryValue(CFGAddress addr) {
        if (ctx.isGlobalVar(addr))
            return;
        Temporary t = new Temporary(addr);
        if (vt.lookup(t) != null)
            return;
        Value v = new Value(t);
        vt.add(t, v);
        tmpGen.get(dt.getRoot()).add(t);
        availOut.get(dt.getRoot()).put(v, t);
    }

    /** Helper for buildSets1 */
    private void buildSets1DFS(CFGBasicBlock b) {
        CFGBasicBlock parent = data.getDominatorTree().getImmediateDominator(b);
        if (parent != null)
            availOut.put(b, new HashMap<>(availOut.get(parent)));

        for (CFGInstruction i : b.getAllInstructions()) {
            if (isVolatile(i)) {
                if (i.address() != null && !ctx.isGlobalVar(i.address())) {
                    Temporary t = new Temporary(i.address());
                    Value v = new Value(t);
                    vt.add(t, v);
                    tmpGen.get(b).add(t);
                    if (!availOut.get(b).containsKey(v))
                        availOut.get(b).put(v, t);
                }
                continue;
            }
            if (i instanceof CFGPhiInstruction phi) {
                Temporary t = new Temporary(phi.address());
                Value v = new Value(t);
                vt.add(t, v);
                phiGen.get(b).add(t);
                availOut.get(b).put(v, t);
            } else if (i instanceof CFGCopyInstruction copy) {
                Temporary tp = new Temporary(copy.operand());
                Temporary t = new Temporary(copy.address());
                Value v = vt.lookup(tp);
                vt.add(t, v);
                if (!expGen.get(b).containsKey(v))
                    expGen.get(b).put(v, tp);
                tmpGen.get(b).add(t);
                if (!availOut.get(b).containsKey(v))
                    availOut.get(b).put(v, t);
            } else if (i instanceof CFGBinaryInstruction bin) {
                Temporary t = new Temporary(bin.address());
                Temporary t1 = new Temporary(bin.left());
                Temporary t2 = new Temporary(bin.right());
                Value v1 = vt.lookup(t1);
                Value v2 = vt.lookup(t2);
                Expression e = makeBinExpr(switch(bin.operator()) {
                    case PLUS -> Op.ADD;
                    case MINUS -> Op.SUB;
                    case TIMES -> Op.MUL;
                    case DIVIDES -> Op.DIV;
                    case MOD -> Op.MOD;
                    case EQ -> Op.EQ;
                    case NEQ -> Op.NE;
                    case LT -> Op.LT;
                    case GT -> Op.GT;
                    case LEQ -> Op.LE;
                    case GEQ -> Op.GE;
                    case AND -> Op.AND;
                    case OR -> Op.OR;
                }, v1, v2);
                Value v = vt.lookupOrAdd(e);
                vt.add(t, v);
                if (!expGen.get(b).containsKey(v1))
                    expGen.get(b).put(v1, t1);
                if (!expGen.get(b).containsKey(v2))
                    expGen.get(b).put(v2, t2);
                if (!expGen.get(b).containsKey(v))
                    expGen.get(b).put(v, e);
                tmpGen.get(b).add(t);
                if (!availOut.get(b).containsKey(v))
                    availOut.get(b).put(v, t);
            } else if (i instanceof CFGBinaryImmediateInstruction bim) {
                Temporary t = new Temporary(bim.address());
                Temporary t1 = new Temporary(bim.left());
                Lit l2 = new Lit(bim.right());
                Value v1 = vt.lookup(t1);
                Value v2 = vt.lookupOrAdd(l2);
                immediates.put(v2, l2);
                Expression e = makeBinExpr(switch(bim.operator()) {
                    case PLUS -> Op.ADD;
                    case MINUS -> Op.SUB;
                    case TIMES -> Op.MUL;
                    case DIVIDES -> Op.DIV;
                    case MOD -> Op.MOD;
                    case EQ -> Op.EQ;
                    case NEQ -> Op.NE;
                    case LT -> Op.LT;
                    case GT -> Op.GT;
                    case LEQ -> Op.LE;
                    case GEQ -> Op.GE;
                    case AND -> Op.AND;
                    case OR -> Op.OR;
                }, v1, v2);
                Value v = vt.lookupOrAdd(e);
                vt.add(t, v);
                if (!expGen.get(b).containsKey(v1))
                    expGen.get(b).put(v1, t1);
                if (!expGen.get(b).containsKey(v2))
                    expGen.get(b).put(v2, l2);
                if (!expGen.get(b).containsKey(v))
                    expGen.get(b).put(v, e);
                tmpGen.get(b).add(t);
                if (!availOut.get(b).containsKey(v))
                    availOut.get(b).put(v, t);
            } else if (i instanceof CFGUnaryInstruction uni) {
                Temporary t = new Temporary(uni.address());
                Temporary t1 = new Temporary(uni.operand());
                Value v1 = vt.lookup(t1);
                Expression e = makeBinExpr(switch(uni.operator()) {
                    case MINUS -> Op.NEG;
                    case NOT -> Op.NOT;
                }, v1, NULL);
                Value v = vt.lookupOrAdd(e);
                vt.add(t, v);
                if (!expGen.get(b).containsKey(v1))
                    expGen.get(b).put(v1, t1);
                if (!expGen.get(b).containsKey(v))
                    expGen.get(b).put(v, e);
                tmpGen.get(b).add(t);
                if (!availOut.get(b).containsKey(v))
                    availOut.get(b).put(v, t);
            } else if (i instanceof CFGCastInstruction cast) {
                Temporary t = new Temporary(cast.address());
                Temporary t1 = new Temporary(cast.operand());
                Value v1 = vt.lookup(t1);
                Expression e = makeBinExpr(switch(cast.type()) {
                    case INT -> Op.INT;
                    case LONG -> Op.LONG;
                    default -> throw new AssertionError("This should never happen.");
                }, v1, NULL);
                Value v = vt.lookupOrAdd(e);
                vt.add(t, v);
                if (!expGen.get(b).containsKey(v1))
                    expGen.get(b).put(v1, t1);
                if (!expGen.get(b).containsKey(v))
                    expGen.get(b).put(v, e);
                tmpGen.get(b).add(t);
                if (!availOut.get(b).containsKey(v))
                    availOut.get(b).put(v, t);
            } else if (i instanceof CFGLiteralInstruction lit) {
                if (lit.literal().type() == Type.STRING) continue;
                Lit l = new Lit(lit.literal());
                Temporary t = new Temporary(lit.address());
                Value v = vt.lookupOrAdd(l);
                vt.add(t, v);
                immediates.put(v, l);
                if (!expGen.get(b).containsKey(v))
                    expGen.get(b).put(v, l);
                tmpGen.get(b).add(t);
                if (!availOut.get(b).containsKey(v))
                    availOut.get(b).put(v, t);
            } else if (i.address() != null) {
                Temporary t = new Temporary(i.address());
                Value v = new Value(t);
                vt.add(t, v);
                tmpGen.get(b).add(t);
                if (!availOut.get(b).containsKey(v))
                    availOut.get(b).put(v, t);
            }
        }

        dt.getImmediateDominatedNodes(b).forEach(this::buildSets1DFS);
    }

    /** Constructs antic sets */
    private void buildSets2() {
        method.getBlocks().forEach(x -> anticIn.put(x, new HashMap<>()));
        boolean changed = true;
        int cnt = 0;
        while (changed && cnt++ < 100) {
            changed = buildSets2DFS(pdt.getRoot());
        }
    }

    /** Helper for buildSets2 */
    private boolean buildSets2DFS(CFGBasicBlock b) {
        boolean changed = false;
// System.out.println("Building sets for " + second.address());
        if (b != pdt.getRoot()) {
            Map<Value, Expression> old = new HashMap<>(anticIn.get(b));

            Map<Value, Expression> anticOut = new HashMap<>();
            if (dt.getSuccessors(b).size() == 1) {
// System.out.println("if");
                CFGBasicBlock succ = dt.getSuccessors(b).iterator().next();
                anticOut = new HashMap<>(anticIn.get(succ));
                phiTranslate(anticOut, b, succ);
            } else if (dt.getSuccessors(b).size() > 1) {
// System.out.println("else if");
                Queue<CFGBasicBlock> workList = new LinkedList<>(dt.getSuccessors(b));
                CFGBasicBlock first = workList.poll();
                anticOut = new HashMap<>(anticIn.getOrDefault(first, new HashMap<>()));
                while (!workList.isEmpty()) {
                    CFGBasicBlock bp = workList.poll();
// System.out.println("Working on " + bp.address());
                    HashMap<Value, Expression> anticOut2 = new HashMap<>(anticOut);
                    for (Value v : anticOut.keySet()) {
                        if (!anticIn.get(bp).containsKey(v))
                            anticOut2.remove(v);
                    }
                    anticOut = anticOut2;
                }
            }
// System.out.println("Hello!");

            Map<Value, Expression> s = new HashMap<>(anticOut); // copy for debugging purposes
            for (Temporary t : tmpGen.get(b)) {
                Value v = vt.lookup(t);
                if (t.equals(s.get(v)))
                    s.remove(v);
            }
// System.out.println("Hi!");

            anticIn.put(b, new HashMap<>(expGen.get(b)));
            for (Temporary t : tmpGen.get(b)) {
                Value v = vt.lookup(t);
                if (t.equals(anticIn.get(b).get(v)))
                    anticIn.get(b).remove(v);
            }
// System.out.println("Greetings!");
            for (Value v : s.keySet()) {
                Expression e = s.get(v);
                if (!anticIn.get(b).containsKey(v))
                    anticIn.get(b).put(v, e);
            }

            clean(anticIn.get(b));

            if (!old.equals(anticIn.get(b))) {
                /*
var list1 = new ArrayList<>(old.keySet());
list1.sort(Comparator.comparingInt(x -> x.index));
var list2 = new ArrayList<>(anticIn.get(second).keySet());
list2.sort(Comparator.comparingInt(x -> x.index));
System.out.printf("%s 化为 %s%n", list1, list2);
System.out.println(vt);
                 */
                changed = true;
            }
        }

        return pdt.getImmediatePostDominatedNodes(b).stream().map(this::buildSets2DFS).reduce(changed, Boolean::logicalOr);
    }

    /** Inserts instructions for PRE */
    private boolean insert() {
        boolean returnValue = false;

        boolean newStuff = true;
        int cnt = 0;
        while (newStuff && cnt++ < 50 ) {
// System.out.println("==== Inserting ====");
            newStuff = insertDFS(dt.getRoot());
            returnValue |= newStuff;
        }

        return returnValue;
    }

    /** Helper for insert */
    private boolean insertDFS(CFGBasicBlock b) {
// System.out.println("Inserting block " + second.address());
        boolean newStuff = false;
        newSets.put(b, new HashSet<>());

        CFGBasicBlock dom = dt.getImmediateDominator(b);
        if (dom != null) {
            for (Temporary e : newSets.get(dom)) {
                newSets.get(b).add(e);
                availOut.get(b).put(vt.lookup(e), e);
            }
        }

        if (dt.getPredecessors(b).size() > 1) {
            Deque<Value> workList = new LinkedList<>(anticIn.get(b).keySet());
            Set<Value> processed = new HashSet<>();
            outer:
            while (!workList.isEmpty()) {
                Value key = workList.poll();
                Expression e = anticIn.get(b).get(key);
// System.out.printf("Processing %s: %s in block %s%n", key, e, second.address());

                if (!(e instanceof BinExpr bin))
                    processed.add(key);
                else {
                    if (!processed.contains(bin.left()) || !processed.contains(bin.right())) {
// System.out.printf("Trying to process %s: %s, need to first process %s and/or %s%n", vt.lookup(bin), bin, processed.contains(bin.left()) ? "" : bin.left(), processed.contains(bin.right()) ? "" : bin.right());
                        // ensure topological sort
                        workList.offerFirst(key);
                        if (!processed.contains(bin.right()))
                            workList.offerFirst(bin.right());
                        if (!processed.contains(bin.left()))
                            workList.offerFirst(bin.left());
                        continue;
                    }
                    processed.add(key);

// System.out.printf("Looking for %s: %s in %s from %s%n", vt.lookup(bin), bin, dom.address(), second.address());
                    // if (availOut.get(dom).containsKey(vt.lookup(bin)))
                    if (availOut.get(dom).containsKey(vt.lookup(bin)))
                        continue;

                    Map<CFGBasicBlock, Expression> avail = new HashMap<>();
                    boolean bySome = false;
                    boolean allSame = true;
                    Expression firstS = null;

                    for (CFGBasicBlock bp : dt.getPredecessors(b)) {
                        Map<Value, Expression> phiTranslations = getPhiTranslation(anticIn.get(b), bp, b);
                        Expression ep = phiTranslations.getOrDefault(key, e);
                        Value vp = vt.lookup(ep);
                        Expression epp = availOut.get(bp).get(vp);
                        if (epp == null) {
// System.out.printf("Could not find %s in %s from %s%n", vp, bp.address(), second.address());
                            avail.put(bp, ep);
                            allSame = false;
                        } else {
// System.out.printf("Found %s in %s from %s%n", vp, bp.address(), second.address());
                            avail.put(bp, epp);
                            bySome = true;
                            if (firstS == null) {
                                firstS = epp;
                            } else if (!firstS.equals(epp)) {
// System.out.printf("But %s != %s%n", firstS, epp);
                                allSame = false;
                            }
                        }
                    }

                    if (!allSame && bySome) {
                        Map<CFGBasicBlock, Temporary> availTemps = new HashMap<>();
                        for (CFGBasicBlock bp : dt.getPredecessors(b)) {
                            Expression ep = avail.get(bp);
                            if (ep instanceof Temporary t) {
                                availTemps.put(bp, t);
                            } else if (ep instanceof BinExpr binp) {
                                Temporary t = new Temporary(ctx.getSymbolTable().addVar(getType(binp)));
                                if (immediates.containsKey(binp.left())) {
                                    Lit s1 = immediates.get(binp.left());
                                    if (immediates.containsKey(binp.right())) {
                                        Lit s2 = immediates.get(binp.right());
                                        bp.offer(assign(t, binp.op, s1, s2));
                                    } else {
                                        Temporary s2 = availOut.get(bp).get(binp.right());
                                        Temporary s1t = new Temporary(ctx.getSymbolTable().addVar(s1.literal.type()));
                                        bp.offer(new CFGLiteralInstruction(ctx, s1t.address(), s1.literal));
                                        bp.offer(assign(t, binp.op, s1t, s2));
                                    }
                                } else {
                                    Temporary s1 = availOut.get(bp).get(binp.left());
                                    if (immediates.containsKey(binp.right())) {
                                        Lit s2 = immediates.get(binp.right());
                                        bp.offer(assign(t, binp.op, s1, s2));
                                    } else {
                                        Temporary s2 = availOut.get(bp).get(binp.right());
                                        bp.offer(assign(t, binp.op, s1, s2));
                                    }
                                }
                                Value v = vt.lookupOrAdd(binp);
                                vt.add(t, v);
                                availOut.get(bp).put(v, t);
                                availTemps.put(bp, t);
                            } else throw new AssertionError("This should never happen.");
                        }

                        CFGPhiInstruction phi = new CFGPhiInstruction(ctx, new CFGAddress());
                        for (CFGBasicBlock bi : dt.getPredecessors(b)) {
                            phi.add(bi, availTemps.get(bi).address());
                        }

                        // check phi instruction doesn't exist already
                        // not in VanDrunen paper (why?)
                        for (CFGPhiInstruction existingPhi : b.getPhiInstructions()) {
                            if (equalPhi(existingPhi, phi))
                                continue outer;
                        }

                        Temporary t = new Temporary(ctx.getSymbolTable().addVar(getType(e)));
                        phi.address().set(t.address());
// System.out.printf("Adding %s for %s%n", t, e);
                        vt.add(t, vt.lookup(e));
                        availOut.get(b).put(vt.lookup(e), t);

                        b.getPhiInstructions().offerLast(phi);
                        newStuff = true;
                        newSets.get(b).add(t);
                    }
                }
            }
        }

// System.out.println("newStuff at block " + second.address() + ": " + newStuff);

        return dt.getImmediateDominatedNodes(b).stream().map(this::insertDFS).reduce(newStuff, Boolean::logicalOr);
    }

    /** Determines if first phi expr is the same as an existing phi expr */
    private boolean equalPhi(CFGPhiInstruction existing, CFGPhiInstruction proposed) {
        for (CFGBasicBlock b : proposed.getSources().keySet())
            if (!existing.getSources().containsKey(b)) return false;
        for (CFGBasicBlock b : existing.getSources().keySet()) {
            if (!proposed.getSources().containsKey(b)) return false;
            CFGAddress src1 = existing.getSources().get(b);
            CFGAddress src2 = proposed.getSources().get(b);
            if (!src1.equals(src2) && !src2.equals(existing.address()))
                return false;
        }
        return true;
    }

    /** Replaces redundant instructions with copies */
    private boolean eliminate() {
        boolean changed = false;

        for (CFGBasicBlock b : method.getBlocks()) {
            for (CFGInstruction i : b.getInstructions()) {
                Temporary t = new Temporary(i.address());
                if (!isVolatile(i) &&
                        (i instanceof CFGBinaryInstruction || i instanceof CFGBinaryImmediateInstruction
                                || i instanceof CFGUnaryInstruction || i instanceof CFGCastInstruction))
                {
                    Temporary sp = availOut.get(b).get(vt.lookup(t));
                    if (!t.equals(sp)) {
                        b.getInstructions().replace(i, new CFGCopyInstruction(ctx, t.address(), sp.address()));
                        changed = true;
                    }
                }
            }
        }

        return changed;
    }

    /** Translate an expr through phi nodes between two basic blocks */
    private void phiTranslate(Map<Value, Expression> set, CFGBasicBlock pre, CFGBasicBlock post) {
        Map<Value, Expression> replace = getPhiTranslation(set, pre, post);

        for (Value v : replace.keySet()) {
            set.remove(v);
            set.put(vt.lookup(replace.get(v)), replace.get(v));
        }
    }

    /** Helper for phiTranslate */
    private void phiTranslate(Map<Value, Expression> set, Map<Value, Expression> replace, Value v, Map<Temporary, Temporary> subs, Set<Value> visited) {
        if (v == NULL || visited.contains(v)) return;
// System.out.println("Phi translating " + v);
        visited.add(v);
        Expression e = set.get(v);

        if (e instanceof Temporary t) {
            if (subs.containsKey(t)) {
                replace.put(v, subs.get(t));
            }
        } else if (e instanceof BinExpr bin) {
            phiTranslate(set, replace, bin.left(), subs, visited);
            phiTranslate(set, replace, bin.right(), subs, visited);
            if (replace.containsKey(bin.left()) || replace.containsKey(bin.right())) {
                Expression newExpr = makeBinExpr(bin.op(),
                        replace.containsKey(bin.left()) ? vt.lookup(replace.get(bin.left())) : bin.left(),
                        replace.containsKey(bin.right()) ? vt.lookup(replace.get(bin.right())) : bin.right());
                vt.lookupOrAdd(newExpr);
                replace.put(v, newExpr);
            }
        }
    }

    /** Helper for phiTranslate */
    private Map<Value, Expression> getPhiTranslation(Map<Value, Expression> set, CFGBasicBlock pre, CFGBasicBlock post) {
// System.out.println("get phi translation");
        Map<Temporary, Temporary> subs = new HashMap<>();
        for (CFGPhiInstruction phi : post.getPhiInstructions()) {
            CFGAddress addr = phi.address();
            if (phi.getSources().containsKey(pre)) {
                subs.put(new Temporary(addr), new Temporary(phi.getSources().get(pre)));
            }
        }

        Map<Value, Expression> replace = new HashMap<>();
        Set<Value> visited = new HashSet<>();
        for (Value v : set.keySet()) {
            phiTranslate(set, replace, v, subs, visited);
        }

        return replace;
    }

    /** Cleans first set of expressions involving values not in the set */
    private void clean(Map<Value, Expression> set) {
        Set<Value> visited = new HashSet<>();
        Set<Value> kill = new HashSet<>();
        for (Value v : set.keySet()) {
            clean(set, visited, kill, v);
        }
        for (Value v : kill) {
            set.remove(v);
        }
    }

    /** Helper for clean */
    private void clean(Map<Value, Expression> set, Set<Value> visited, Set<Value> kill, Value v) {
        if (visited.contains(v)) return;
        visited.add(v);
        if (set.get(v) instanceof BinExpr bin) {
            clean(set, visited, kill, bin.left());
            clean(set, visited, kill, bin.right());
            if (!immediates.containsKey(bin.left())
                    && (!set.containsKey(bin.left())
                    || kill.contains(bin.left())))
                kill.add(v);
            else if (!immediates.containsKey(bin.right())
                    && (!set.containsKey(bin.right())
                    || kill.contains(bin.right())))
                kill.add(v);
        }
    }

    /** Determines if an instruction has properties that GVNPRE cannot handle */
    private boolean isVolatile(CFGInstruction inst) {
        if (inst.address() != null && ctx.isGlobalVar(inst.address())) return true;
        for (CFGAddress addr : inst.operands()) {
            if (ctx.isGlobalVar(addr)) return true;
        }
        return false;
    }

    /** Represents an expr */
    private interface Expression {}
    /** Represents first literal */
    private record Lit(Literal literal) implements Expression {}
    /** Represents first temporary */
    private record Temporary(CFGAddress address) implements Expression {}
    /** Represents the operator of first "binary expr" */
    private enum Op { ADD, SUB, MUL, DIV, MOD, EQ, NE, LT, GT, LE, GE, AND, OR, NEG, NOT, INT, LONG }
    /** Represents first binary expr; right is null when no second argument is needed */
    private record BinExpr(Op op, Value left, Value right) implements Expression {
        @Override
        public String toString() { return op.toString() + "(" + left + ", " + right + ")"; }
    }

    /** Makes first binary expr, applying deterministic equivalence reductions */
    private BinExpr makeBinExpr(Op op, Value left, Value right) {
        if (immediates.containsKey(right)) {
            return new BinExpr(op, left, right);
        }

        if (op == Op.LE)
            return new BinExpr(Op.GT, right, left);
        if (op == Op.GE)
            return new BinExpr(Op.LT, right, left);
        if (op == Op.ADD || op == Op.MUL || op == Op.EQ || op == Op.NE)
            if (left.compareTo(right) > 0)
                return new BinExpr(op, right, left);

        return new BinExpr(op, left, right);
    }

    /** Gets the type of an expr */
    private Type getType(Expression e) {
        if (e instanceof Temporary t)
            return ctx.getType(t.address());
        else if (e instanceof Lit lit)
            return lit.literal().type();
        else if (e instanceof BinExpr bin) {
            return switch (bin.op) {
                case INT -> Type.INT;
                case LONG -> Type.LONG;
                case EQ -> Type.BOOL;
                case NE -> Type.BOOL;
                case LT -> Type.BOOL;
                case GT -> Type.BOOL;
                case LE -> Type.BOOL;
                case GE -> Type.BOOL;
                default -> bin.left().type;
            };
        } else throw new AssertionError("This should never happen.");
    }

    /** Represents first value */
    private class Value implements Comparable<Value> {
        private static int cnt = 0;
        public final int index;
        Type type;
        Value(Type type) { index = cnt++; this.type = type; }
        Value(Expression e) { this(getType(e)); }
        @Override public boolean equals(Object obj) { return obj instanceof Value v && index == v.index; }
        @Override public int hashCode() { return index; }
        @Override public int compareTo(Value o) { return index - o.index; }
        @Override public String toString() { return "" + index; }
    }

    /** A table mapping expressions to their values */
    private class ValueTable {
        HashMap<Expression, Value> map;
        ValueTable() { map = new HashMap<>(); }
        void add(Expression e, Value v) { map.put(e, v); }
        Value lookup(Expression e) { return map.getOrDefault(e, null); } // TODO could be smarter
        Value lookupOrAdd(Expression e) { return map.computeIfAbsent(e, k -> new Value(e)); }
        @Override public String toString() { return map.toString(); }
    }

    /** Constructs first CFG assignment instruction from internal representations */
    private CFGInstruction assign(Temporary dest, Op op, Temporary left, Temporary right) {
        return switch (op) {
            case ADD -> new CFGBinaryInstruction(ctx, dest.address(), BinaryOperator.PLUS, left.address(), right.address());
            case SUB -> new CFGBinaryInstruction(ctx, dest.address(), BinaryOperator.MINUS, left.address(), right.address());
            case MUL -> new CFGBinaryInstruction(ctx, dest.address(), BinaryOperator.TIMES, left.address(), right.address());
            case DIV -> new CFGBinaryInstruction(ctx, dest.address(), BinaryOperator.DIVIDES, left.address(), right.address());
            case MOD -> new CFGBinaryInstruction(ctx, dest.address(), BinaryOperator.MOD, left.address(), right.address());
            case EQ -> new CFGBinaryInstruction(ctx, dest.address(), BinaryOperator.EQ, left.address(), right.address());
            case NE -> new CFGBinaryInstruction(ctx, dest.address(), BinaryOperator.NEQ, left.address(), right.address());
            case LT -> new CFGBinaryInstruction(ctx, dest.address(), BinaryOperator.LT, left.address(), right.address());
            case GT -> new CFGBinaryInstruction(ctx, dest.address(), BinaryOperator.GT, left.address(), right.address());
            case LE -> new CFGBinaryInstruction(ctx, dest.address(), BinaryOperator.LEQ, left.address(), right.address());
            case GE -> new CFGBinaryInstruction(ctx, dest.address(), BinaryOperator.GEQ, left.address(), right.address());
            case AND -> new CFGBinaryInstruction(ctx, dest.address(), BinaryOperator.AND, left.address(), right.address());
            case OR -> new CFGBinaryInstruction(ctx, dest.address(), BinaryOperator.OR, left.address(), right.address());
            case NEG -> new CFGUnaryInstruction(ctx, dest.address(), UnaryOperator.MINUS, left.address());
            case NOT -> new CFGUnaryInstruction(ctx, dest.address(), UnaryOperator.NOT, left.address());
            case INT -> new CFGCastInstruction(ctx, dest.address(), Type.INT, left.address());
            case LONG -> new CFGCastInstruction(ctx, dest.address(), Type.LONG, left.address());
        };
    }

    /** Constructs first CFG assignment instruction (with an immediate) from internal representations */
    private CFGInstruction assign(Temporary dest, Op op, Temporary left, Lit right) {
// System.out.printf("assign(%s, %s, %s, %s)%n", dest, op, left, right);
        return switch (op) {
            case ADD -> new CFGBinaryImmediateInstruction(ctx, dest.address(), BinaryOperator.PLUS, left.address(), right.literal());
            case SUB -> new CFGBinaryImmediateInstruction(ctx, dest.address(), BinaryOperator.MINUS, left.address(), right.literal());
            case MUL -> new CFGBinaryImmediateInstruction(ctx, dest.address(), BinaryOperator.TIMES, left.address(), right.literal());
            case DIV -> new CFGBinaryImmediateInstruction(ctx, dest.address(), BinaryOperator.DIVIDES, left.address(), right.literal());
            case MOD -> new CFGBinaryImmediateInstruction(ctx, dest.address(), BinaryOperator.MOD, left.address(), right.literal());
            case EQ -> new CFGBinaryImmediateInstruction(ctx, dest.address(), BinaryOperator.EQ, left.address(), right.literal());
            case NE -> new CFGBinaryImmediateInstruction(ctx, dest.address(), BinaryOperator.NEQ, left.address(), right.literal());
            case LT -> new CFGBinaryImmediateInstruction(ctx, dest.address(), BinaryOperator.LT, left.address(), right.literal());
            case GT -> new CFGBinaryImmediateInstruction(ctx, dest.address(), BinaryOperator.GT, left.address(), right.literal());
            case LE -> new CFGBinaryImmediateInstruction(ctx, dest.address(), BinaryOperator.LEQ, left.address(), right.literal());
            case GE -> new CFGBinaryImmediateInstruction(ctx, dest.address(), BinaryOperator.GEQ, left.address(), right.literal());
            case AND -> new CFGBinaryImmediateInstruction(ctx, dest.address(), BinaryOperator.AND, left.address(), right.literal());
            case OR -> new CFGBinaryImmediateInstruction(ctx, dest.address(), BinaryOperator.OR, left.address(), right.literal());
            default -> throw new IllegalArgumentException();
        };
    }

    private CFGInstruction assign(Temporary dest, Op op, Lit left, Lit right) {
        return new CFGLiteralInstruction(ctx, dest.address(), compute(op, left, right));
    }

    private Literal compute(Op op, Lit left, Lit right) {
        return switch (op) {
            case ADD -> OptUtils.compute(BinaryOperator.PLUS, left.literal(), right.literal());
            case SUB -> OptUtils.compute(BinaryOperator.MINUS, left.literal(), right.literal());
            case MUL -> OptUtils.compute(BinaryOperator.TIMES, left.literal(), right.literal());
            case DIV -> OptUtils.compute(BinaryOperator.DIVIDES, left.literal(), right.literal());
            case MOD -> OptUtils.compute(BinaryOperator.MOD, left.literal(), right.literal());
            case EQ -> OptUtils.compute(BinaryOperator.EQ, left.literal(), right.literal());
            case NE -> OptUtils.compute(BinaryOperator.NEQ, left.literal(), right.literal());
            case LT -> OptUtils.compute(BinaryOperator.LT, left.literal(), right.literal());
            case GT -> OptUtils.compute(BinaryOperator.GT, left.literal(), right.literal());
            case LE -> OptUtils.compute(BinaryOperator.LEQ, left.literal(), right.literal());
            case GE -> OptUtils.compute(BinaryOperator.GEQ, left.literal(), right.literal());
            case AND -> OptUtils.compute(BinaryOperator.AND, left.literal(), right.literal());
            case OR -> OptUtils.compute(BinaryOperator.OR, left.literal(), right.literal());
            default -> throw new IllegalArgumentException();
        };
    }

  }
