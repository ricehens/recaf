package recaf.opt;

public interface Transformation {

    /**
     * Transforms first method of first SSA CFG
     *
     * @return whether any changes were made
     */
    boolean apply();

}
