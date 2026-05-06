package recaf;

import recaf.general.BinaryOperator;
import recaf.general.Type;
import recaf.cfg.*;
import recaf.opt.GVNPRE;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class OptTest {

    @Disabled
    @Test
    void testGVNPRE() throws ReflectiveOperationException {
        CFGContext fakeCtx = new CFGContext(null, new CFGSymbolTable());

        CFGMethod f = new CFGMethod(fakeCtx, Type.INT, "f", List.of(), new CFGBuilder(null));
        CFGMethod g = new CFGMethod(fakeCtx, Type.BOOL, "g", List.of(), new CFGBuilder(null));
        CFGMethod method = new CFGMethod(fakeCtx, Type.VOID, "main", List.of(), new CFGBuilder(null));
        fakeCtx.getSymbolTable().addMethod(f);
        fakeCtx.getSymbolTable().addMethod(g);
        fakeCtx.getSymbolTable().addMethod(method);
        CFGAddress[] t =  new CFGAddress[15];
        for (int i = 0; i <= 13; i++)
            t[i] = fakeCtx.getSymbolTable().addVar(Type.INT);
        t[14] = fakeCtx.getSymbolTable().addVar(Type.BOOL);

        CFGBasicBlock[] b = new CFGBasicBlock[8];
        for (int i = 1; i <= 7; i++) {
            b[i] = new CFGBasicBlock(fakeCtx);
            fakeCtx.getSymbolTable().addBlock(b[i]);
            b[i].setMethod(method);
        }

        b[1].offer(new CFGLiteralInstruction(fakeCtx, t[0], 1));
        b[1].offer(new CFGMethodCallInstruction(fakeCtx, t[14], "g", List.of()));
        b[1].offer(new CFGMethodCallInstruction(fakeCtx, t[1], "f", List.of()));
        b[1].setLastInstruction(new CFGJumpInstruction(fakeCtx, b[2].address()));

        CFGPhiInstruction phi2 = new CFGPhiInstruction(fakeCtx, t[2]);
        phi2.add(b[1], t[1]);
        phi2.add(b[6], t[3]);
        b[2].getPhiInstructions().offerLast(phi2);
        b[2].offer(new CFGBinaryInstruction(fakeCtx, t[3], BinaryOperator.PLUS, t[2], t[0]));
        b[2].setLastInstruction(new CFGBranchInstruction(fakeCtx, t[14], b[3].address(), b[7].address()));

        b[3].setLastInstruction(new CFGBranchInstruction(fakeCtx, t[14], b[4].address(), b[5].address()));

        b[4].offer(new CFGBinaryInstruction(fakeCtx, t[4], BinaryOperator.PLUS, t[2], t[3]));
        b[4].offer(new CFGCopyInstruction(fakeCtx, t[5], t[4]));
        b[4].offer(new CFGBinaryInstruction(fakeCtx, t[6], BinaryOperator.PLUS, t[1], t[5]));
        b[4].setLastInstruction(new CFGJumpInstruction(fakeCtx, b[6].address()));

        b[5].offer(new CFGBinaryInstruction(fakeCtx, t[7], BinaryOperator.PLUS, t[3], t[0]));
        b[5].setLastInstruction(new CFGJumpInstruction(fakeCtx, b[6].address()));

        CFGPhiInstruction phi8 = new CFGPhiInstruction(fakeCtx, t[8]);
        phi8.add(b[4], t[1]);
        phi8.add(b[5], t[7]);
        b[6].getPhiInstructions().offerLast(phi8);
        b[6].offer(new CFGBinaryInstruction(fakeCtx, t[9], BinaryOperator.PLUS, t[2], t[3]));
        b[6].offer(new CFGBinaryInstruction(fakeCtx, t[10], BinaryOperator.PLUS, t[9], t[8]));
        b[6].offer(new CFGMethodCallInstruction(fakeCtx, t[11], "f", List.of()));
        b[6].offer(new CFGBinaryInstruction(fakeCtx, t[12], BinaryOperator.PLUS, t[9], t[11]));
        b[6].offer(new CFGBinaryInstruction(fakeCtx, t[13], BinaryOperator.PLUS, t[12], t[3]));
        b[6].setLastInstruction(new CFGJumpInstruction(fakeCtx, b[2].address()));

        for (int i = 1; i <= 7; i++)
            method.getBlocks().offerLast(b[i]);

        System.out.println(printHelper(method.toString(), b, t));
        System.out.println("========");

        // Reflection
        GVNPRE opt = new GVNPRE(fakeCtx, method);

        Method buildSets1 = GVNPRE.class.getDeclaredMethod("buildSets1"); buildSets1.setAccessible(true);
        Method buildSets2 = GVNPRE.class.getDeclaredMethod("buildSets2"); buildSets2.setAccessible(true);
        Method insert = GVNPRE.class.getDeclaredMethod("insert"); insert.setAccessible(true);
        Method eliminate = GVNPRE.class.getDeclaredMethod("eliminate"); eliminate.setAccessible(true);

        Field vt = GVNPRE.class.getDeclaredField("vt"); vt.setAccessible(true);
        Field dt = GVNPRE.class.getDeclaredField("dt"); dt.setAccessible(true);
        Field pdt = GVNPRE.class.getDeclaredField("pdt"); pdt.setAccessible(true);
        Field phiGen = GVNPRE.class.getDeclaredField("phiGen"); phiGen.setAccessible(true);
        Field tmpGen = GVNPRE.class.getDeclaredField("tmpGen"); tmpGen.setAccessible(true);
        Field expGen = GVNPRE.class.getDeclaredField("expGen"); expGen.setAccessible(true);
        Field availOut = GVNPRE.class.getDeclaredField("availOut"); availOut.setAccessible(true);
        Field anticIn = GVNPRE.class.getDeclaredField("anticIn"); anticIn.setAccessible(true);
        Field newSets = GVNPRE.class.getDeclaredField("newSets"); newSets.setAccessible(true);

        // BuildSets phase 1
        buildSets1.invoke(opt);

        // Test BuildSets phase 1
        System.out.println("VT: " + printHelper(vt.get(opt).toString(), b, t));
        for (int i = 1; i <= 7; i++) {
            System.out.printf("PHI_GEN[%d]: %s%n", i, printHelper(((Map) phiGen.get(opt)).get(b[i]).toString(), b, t));
            System.out.printf("TMP_GEN[%d]: %s%n", i, printHelper(((Map) tmpGen.get(opt)).get(b[i]).toString(), b, t));
            System.out.printf("EXP_GEN[%d]: %s%n", i, printHelper(((Map) expGen.get(opt)).get(b[i]).toString(), b, t));
            System.out.printf("AVAIL_OUT[%d]: %s%n", i, printHelper(((Map) availOut.get(opt)).get(b[i]).toString(), b, t));
        }
        System.out.println("========");

        // BuildSets phase 2
        buildSets2.invoke(opt);

        // Test BuildSets phase 2
        for (int i = 1; i <= 7; i++) {
            System.out.printf("ANTIC_IN[%d]: %s%n", i, printHelper(((Map) anticIn.get(opt)).get(b[i]).toString(), b, t));
        }
        System.out.println("========");

        // Insert
        insert.invoke(opt);

        // Test insert
        System.out.println(printHelper(method.toString(), b, t));
        for (int i = 1; i <= 7; i++) {
            System.out.printf("AVAIL_OUT[%d]: %s%n", i, printHelper(((Map) availOut.get(opt)).get(b[i]).toString(), b, t));
            System.out.printf("new_sets[%d]: %s%n", i, printHelper(((Map) newSets.get(opt)).get(b[i]).toString(), b, t));
        }

        // Eliminate
        eliminate.invoke(opt);
        System.out.println(printHelper(method.toString(), b, t));
    }

    private String printHelper(String ll, CFGBasicBlock[] b, CFGAddress[] t) {
        for (int i = 1; i <= 6; i++) {
            String gotoLabel = b[i].address().toString();
            String label = gotoLabel.substring(1) + ":";
            ll = ll.replaceAll(label, String.format("B%d (%s):", i, gotoLabel));
            ll = ll.replaceAll(gotoLabel + ",", "B" + i + ",");
            ll = ll.replaceAll(gotoLabel + "\n", "B" + i + "\n");
        }
        for (int i = 1; i <= 13; i++) {
            String label = t[i].toString();
            ll = ll.replaceAll(label + " ", String.format("t%d (%s) ", i, label));
            ll = ll.replaceAll(label + ",", "t" + i + ",");
            ll = ll.replaceAll(label + "\n", "t" + i + "\n");
            ll = ll.replaceAll(label + "]", "t" + i + "]");
        }

        String litLabel = t[0].toString();
        ll = ll.replaceAll(litLabel + " ", String.format("\\$1 (%s) ", litLabel));
        ll = ll.replaceAll(litLabel + ",", "\\$1" + ",");
        ll = ll.replaceAll(litLabel + "\n", "\\$1" + "\n");
        ll = ll.replaceAll(litLabel + "]", "\\$1" + "]");

        String condLabel = t[14].toString();
        ll = ll.replaceAll(condLabel + " ", String.format("bool (%s) ", condLabel));
        ll = ll.replaceAll(condLabel + ",", "bool" + ",");
        ll = ll.replaceAll(condLabel + "\n", "bool" + "\n");
        ll = ll.replaceAll(condLabel + "]", "bool" + "]");

        return ll;
    }


}
