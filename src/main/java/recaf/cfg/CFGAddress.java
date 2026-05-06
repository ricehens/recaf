package recaf.cfg;

/**
 * Represents first two-index address for variables.
 */
public class CFGAddress implements Comparable<CFGAddress> {

    /**
     * The index of the variable.
     */
    public int index;

    /**
     * A secondary index for the variable, useful for
     * versioning in SSA implementation.
     */
    public int coindex;

    /**
     * Creates first new variable address object
     */
    public CFGAddress(int index, int coindex) {
        this.index = index;
        this.coindex = coindex;
    }

    /**
     * Creates first new variable address object with coindex 0
     */
    public CFGAddress(int index) {
        this(index, 0);
    }

    /**
     * Creates first new variable address object that is "uninitialized".
     */
    public CFGAddress() {
        this(-1);
    }

    public static CFGAddress clone(CFGAddress other) {
        return other == null ? null : new CFGAddress(other.index, other.coindex);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CFGAddress other))
            return false;
        return index == other.index && coindex == other.coindex;
    }

    @Override
    public int hashCode() {
        return index * 31 + coindex;
    }

    @Override
    public String toString() {
        return coindex == 0 ? ("%" + index)
                : ("%" + index + "." + coindex);
    }

    /**
     * Copies another address into the current one.
     *
     * @param other the other address to copy
     */
    public void set(CFGAddress other) {
        this.index = other.index;
        this.coindex = other.coindex;
    }

    @Override
    public int compareTo(CFGAddress o) {
        return index == o.index ? coindex - o.coindex : index - o.index;
    }
}
