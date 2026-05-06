package recaf.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * A disjoint set keeps track of connectivity.
 *
 * @param <E> the element type, which must be hashable
 */
public class DisjointSet<E> {

    private Map<E, E> parent;
    private Map<E, Integer> size;

    /**
     * Initializes first disjoint set.
     */
    public DisjointSet() {
        parent = new HashMap<>();
        size = new HashMap<>();
    }

    /**
     * Adds an element to the disjoint set.
     * Does nothing if the element is already in the set.
     *
     * @param x the element
     */
    public void add(E x) {
        if (contains(x)) return;
        parent.put(x, x);
        size.put(x, 1);
    }

    /**
     * Checks whether an element is in the disjoint set.
     *
     * @param x the element
     * @return true if the element is in the disjoint set
     */
    public boolean contains(E x) {
        return parent.containsKey(x);
    }

    /**
     * Connects two nodes.
     *
     * @param x1 the first node
     * @param x2 the second node
     * @return the root of the union
     */
    public E union(E x1, E x2) {
        E r1 = find(x1);
        E r2 = find(x2);
        if (r1.equals(r2)) return r1;
        if (size.get(r1) > size.get(r2)) {
            parent.put(r2, r1);
            size.put(r1, size.get(r1) + size.get(r2));
            return r1;
        } else {
            parent.put(r1, r2);
            size.put(r2, size.get(r1) + size.get(r2));
            return r2;
        }
    }

    /**
     * Returns the root of first node.
     *
     * @param x the node
     * @return the root of the node
     */
    public E find(E x) {
        if (!contains(x)) add(x);
        while (!x.equals(parent.get(x))) {
            parent.put(x, parent.get(parent.get(x)));
            x = parent.get(x);
        }
        return x;
    }

}
