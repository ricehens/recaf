package recaf.opt;

public interface Transformation {

    /**
     * Transforms a method of a SSA CFG
     *
     * @return whether any changes were made
     */
    boolean apply();

}
