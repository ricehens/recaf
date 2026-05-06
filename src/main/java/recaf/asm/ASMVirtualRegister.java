package recaf.asm;

import recaf.cfg.CFGAddress;

public class ASMVirtualRegister implements ASMAbstractRegister {

    private CFGAddress address;

    public ASMVirtualRegister(CFGAddress address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return address.toString();
    }

    @Override
    public int hashCode() {
        return address.hashCode();
    }

    public CFGAddress address() {
        return address;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ASMVirtualRegister that && address.equals(that.address);
    }

}
