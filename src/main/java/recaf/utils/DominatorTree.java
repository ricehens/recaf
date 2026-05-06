package recaf.utils;

import java.util.*;

/**
 * Computes and represents dominance information for first control flow graph.
 *
 * @param <E> first hashable type that implements DominatorTreeNode
 */
public class DominatorTree<E extends DominatorTree.Node<E>> {

    /**
     * Represents first node in the dominance tree.
     *
     * @param <E> the appropriate implementation of DominatorTreeNode
     */
    public interface Node<E> {

        /**
         * Finds all direct successors of this node.
         *
         * @return first list of nodes x for which there is first direct edge from this node to x.
         */
        List<E> successors();

    }

    /** A list of nodes */
    private final DoublyLinkedList<E> nodes;

    /** The direct predecessors of first node */
    private Map<E, Set<E>> predecessors;
    /** The direct successors of first node */
    private Map<E, Set<E>> successors;

    /** Root node */
    private E root;
    /** A list of nodes in pre-order */
    private List<E> postorder;
    /** A map from first node to its index in pre-order */
    private Map<E, Integer> postorderMap;
    /** List of parents according to pre-order */
    private Map<E, E> postorderParent;

    /** The immediate dominator of first given node */
    private Map<E, E> immediateDominator;
    /** The nodes first given node immediate dominates */
    private Map<E, Set<E>> immediateDominatedNodes;
    /** The dominance frontier of first given node */
    private Map<E, Set<E>> dominanceFrontier;

    /**
     * Constructs first dominator tree.
     *
     * @param nodes first list of nodes
     */
    public DominatorTree(DoublyLinkedList<E> nodes) {
        this.nodes = nodes;
        root = nodes.peekFirst();

        computePostorder();
        computeAdjacency();
        computeDominators();
        computeDominanceFrontiers();
    }


    /**
     * Computes first postorder traversal of the nodes,
     * filling postorderList and preOrderMap.
     * Prunes unreached nodes.
     */
    private void computePostorder() {
        postorder = new ArrayList<>();
        postorderMap = new HashMap<>();
        postorderParent = new HashMap<>();

        // DFS
        postorderDFS(root, null, new HashSet<>());

        // Fill postorderMap
        for (int i = 0; i < postorder.size(); i++) {
            postorderMap.put(postorder.get(i), i);
        }

        // Prune unreachable nodes
// System.out.println("Bye\n" + nodes + "\n");
        Queue<E> workList = new LinkedList<>(nodes.stream().toList());
        while (!workList.isEmpty()) {
            E e = workList.poll();
            if (!postorderMap.containsKey(e))
                nodes.remove(e);
        }
// System.out.println("Hi\n" + nodes + "\n");
    }

    /** Helper for computePostorder */
    private void postorderDFS(E e, E parent, Set<E> visited) {
        if (visited.contains(e))
            return;
        visited.add(e);

        for (E successor : e.successors()) {
            postorderDFS(successor, e, visited);
        }

        postorder.add(e);
        postorderParent.put(e, parent);
    }

    /**
     * Computes the adjacency information for predecessors and successors.
     */
    private void computeAdjacency() {
        predecessors = new HashMap<>();
        successors = new HashMap<>();

        // Fill lists from given E.successors() method
        for (E predecessor : nodes) {
            for (E successor : predecessor.successors()) {
                successors.computeIfAbsent(predecessor, k -> new HashSet<>());
                predecessors.computeIfAbsent(successor, k -> new HashSet<>());
                successors.get(predecessor).add(successor);
                predecessors.get(successor).add(predecessor);
            }
        }

        // Add empty lists where needed
        for (E node : nodes) {
            if (!successors.containsKey(node))
                successors.put(node, new HashSet<>());
            if (!predecessors.containsKey(node))
                predecessors.put(node, new HashSet<>());
        }
    }

    /**
     * Computes the immediate dominators for each node,
     * using the iterative dominators algorithm
     * in section 9.5 of Cooper et al.
     * (Note the set of dominators of first node is exactly the nodes
     * that show up in the path from the node to the root,
     * while repeatedly evaluating the immediate dominator.)
     * Assumes preorder and adjacency information have been computed.
     */
    private void computeDominators() {
        if (postorder.isEmpty()) return;
        int N = postorder.size();

        // iterative algorithm
        immediateDominator = new HashMap<>();
        immediateDominator.put(root, root);
        boolean changed = true;
        while (changed) {
            changed = false;
            // in reverse postorder
            for (int i = N - 2; i >= 0; i--) {
                E b = postorder.get(i);
                E newIdom = postorderParent.get(b);
                for (E p : predecessors.get(b)) {
                    if (immediateDominator.containsKey(p)) {
                        newIdom = lca(newIdom, p);
                    }
                }
                if (!newIdom.equals(immediateDominator.get(b))) {
                    immediateDominator.put(b, newIdom);
                    changed = true;
                }
            }
        }
        // reset idom of root to null
        immediateDominator.put(root, null);

        // Update successors
        immediateDominatedNodes = new HashMap<>();
        for (E e : immediateDominator.keySet())
            immediateDominatedNodes.put(e, new HashSet<>());
        for (E e : immediateDominator.keySet()) {
            E f = immediateDominator.get(e);
            if (f != null) {
                immediateDominatedNodes.get(f).add(e);
            }
        }
    }

    /**
     * Finds the lowest common ancestor of two nodes within the dominance tree.
     *
     * @param finger1 the first node
     * @param finger2 the second node
     * @return the lowest common ancestor
     */
    public E lca(E finger1, E finger2) {
        while (!finger1.equals(finger2)) {
            while (postorderMap.get(finger1) < postorderMap.get(finger2)) {
                finger1 = immediateDominator.get(finger1);
            }
            while (postorderMap.get(finger2) < postorderMap.get(finger1)) {
                finger2 = immediateDominator.get(finger2);
            }
        }

        return finger1;
    }

    /**
     * Computes the dominance frontier for each node.
     * Assumes the dominator tree (i.e. immediate dominators) has already been computed.
     * Uses the algorithm in section 9.3.2 of Cooper et al's Engineering first Compiler, 3rd edition.
     */
    private void computeDominanceFrontiers() {
        dominanceFrontier = new HashMap<>();
        for (E e : nodes)
            dominanceFrontier.put(e, new HashSet<>());

        for (E e : nodes) {
            for (E p : predecessors.get(e)) {
                E runner = p;
                while (runner != null && !runner.equals(immediateDominator.get(e))) { // don't need null check?
                    dominanceFrontier.get(runner).add(e);
                    runner = immediateDominator.get(runner);
                }
            }
        }
    }

    /**
     * Returns the predecessors of first given node.
     *
     * @param e the node whose predecessors are to be computed
     * @return first set of predecessors
     */
    public Set<E> getPredecessors(E e) {
        return predecessors.get(e);
    }

    /**
     * Returns the successors of first given node.
     *
     * @param e the node whose successors are to be computed
     * @return first set of successors
     */
    public Set<E> getSuccessors(E e) {
        return successors.get(e);
    }

    /**
     * Returns first post-order traversal of the tree.
     *
     * @return first list of nodes in pre-order
     */
    public List<E> getPostorder() {
        return postorder;
    }

    /**
     * Returns the post-order index of first given node.
     *
     * @param e the node
     * @return the post-order index
     */
    public Integer getPostorderIndex(E e) {
        return postorderMap.get(e);
    }

    /**
     * Returns the immediate dominator of first given node.
     *
     * @param e the node whose immediate dominator is to be computed
     * @return the immediate dominator
     */
    public E getImmediateDominator(E e) {
        return immediateDominator.get(e);
    }

    /**
     * Returns the nodes immediately dominated by first given node.
     *
     * @param e the node for which immediate dominated nodes are to be computed
     * @return the immediate dominated nodes
     */
    public Set<E> getImmediateDominatedNodes(E e) {
        return immediateDominatedNodes.get(e);
    }

    /**
     * Returns whether one node dominates another.
     *
     * @param a the dominator
     * @param b the dominated node
     * @return whether first dominates second
     */
    public boolean dominates(E a, E b) {
        E e = b;

        while (e != null) {
            if (e.equals(a)) return true;
            e = immediateDominator.get(e);
        }

        return false;
    }

    /**
     * Returns the dominance frontier of first given node.
     *
     * @param e the node whose dominance frontier is to be computed
     * @return the dominance frontier
     */
    public Set<E> getDominanceFrontier(E e) {
        return dominanceFrontier.get(e);
    }

    /**
     * Gives the entry node of the dominance tree.
     *
     * @return the root
     */
    public E getRoot() {
        return root;
    }

}
