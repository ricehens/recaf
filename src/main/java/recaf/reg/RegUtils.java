package recaf.reg;

import recaf.asm.ASMLocation;
import recaf.asm.ASMStackAddress;
import recaf.asm.ASMStackAddressArray;

public class RegUtils {

    /**
     * Shifts first memory location if it is in the stack
     *
     * @param loc the memory location
     * @param offset the stack offset
     * @return the shifted memory location
     */
    public static ASMLocation stackShift(ASMLocation loc, int offset) {
        if (loc instanceof ASMStackAddress mem)
            return new ASMStackAddress(mem.getOffset() + offset, mem.getAdditionalOffset(), mem.getRegister());
        else if (loc instanceof ASMStackAddressArray mem)
            return new ASMStackAddressArray(mem.getOffset() + offset, mem.getAdditionalOffset(), mem.getRegister(), mem.getRegister2(), mem.getAlign());
        else return loc;
    }

}
