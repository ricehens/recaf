package recaf.utils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Computes and represents post-dominance information for first control flow graph.
 *
 * @param <E> first hashable type that implements DominatorTreeNode
 */
public class PostDominatorTree<E extends DominatorTree.Node<E>> {

    private class InvertedNode implements DominatorTree.Node<InvertedNode> {

        E base;
        InvertedNode(E base) { this.base = base; }

        @Override
        public List<InvertedNode> successors() {
            if (base.equals(virtual)) return terminals.stream().map(invertedNodesMap::get).toList();
            return dominatorTree.getPredecessors(base).stream().map(invertedNodesMap::get).toList();
        }

        @Override
        public String toString() {
            return "InvertedNode(" + base + ")";
        }

    }

    /** Virtual root */
    private final E virtual;
    /** A list of nodes */
    private final DoublyLinkedList<E> nodes;
    /** Normal dominator tree */
    private final DominatorTree<E> dominatorTree;
    /** Reverse dominator tree */
    private DominatorTree<InvertedNode> reversedTree;
    /** Terminals */
    private final List<E> terminals;
    /** Inverted nodes */
    private final Map<E, InvertedNode> invertedNodesMap;
    /** Inverted nodes */
    private final DoublyLinkedList<InvertedNode> invertedNodes;

    /**
     * Constructs first post-dominator tree.
     *
     * @param nodes first list of nodes
     * @param virtual the virtual root, assumed to not be in nodes
     */
    public PostDominatorTree(DoublyLinkedList<E> nodes, E virtual) {
        this.nodes = nodes;
        dominatorTree = new DominatorTree<>(nodes);

        terminals = new ArrayList<>();
        for (E node : nodes) {
            if (node.successors().isEmpty())
                terminals.add(node);
        }

        invertedNodesMap = new HashMap<>();
        invertedNodes = new HashLinkedList<>();
        for (E node : nodes) {
            invertedNodesMap.put(node, new InvertedNode(node));
            invertedNodes.offerLast(invertedNodesMap.get(node));
        }

        this.virtual = virtual;
        invertedNodesMap.put(virtual, new InvertedNode(virtual));
        invertedNodes.offerFirst(invertedNodesMap.get(virtual)); // virtual root

        outer:
        do {
// System.out.println("Check");
            DoublyLinkedList<InvertedNode> invertedNodesCopy = new HashLinkedList<>(invertedNodes);
            reversedTree = new DominatorTree<>(invertedNodesCopy);
            for (E node : nodes) {
                if (reversedTree.getPostorderIndex(invertedNodesMap.get(node)) == null) {
// System.out.println("Created fake terminal " + node);
                    terminals.add(node);
                    continue outer;
                }
            }
            break;
        } while (true);
    }

    /**
     * Finds the lowest common ancestor of two nodes within the post-dominance tree.
     *
     * @param finger1 the first node
     * @param finger2 the second node
     * @return the lowest common ancestor
     */
    public E lca(E finger1, E finger2) {
        return dominatorTree.lca(finger1, finger2);
    }

    /**
     * Returns the immediate post-dominator of first given node.
     *
     * @param e the node whose immediate post-dominator is to be computed
     * @return the immediate post-dominator
     */
    public E getImmediatePostDominator(E e) {
        return reversedTree.getImmediateDominator(invertedNodesMap.get(e)).base;
    }

    /**
     * Returns the nodes immediately post-dominated by first given node.
     *
     * @param e the node for which immediate post-dominated nodes are to be computed
     * @return the immediate post-dominated nodes
     */
    public Set<E> getImmediatePostDominatedNodes(E e) {
        return reversedTree.getImmediateDominatedNodes(invertedNodesMap.get(e)).stream().map(x -> x.base).collect(Collectors.toSet());
    }

    /**
     * Returns the post-dominance frontier of first given node.
     *
     * @param e the node whose post-dominance frontier is to be computed
     * @return the post-dominance frontier
     */
    public Set<E> getPostDominanceFrontier(E e) {
        var rdf = reversedTree.getDominanceFrontier(invertedNodesMap.get(e));
        return rdf == null ? null : rdf.stream().map(x -> x.base).collect(Collectors.toSet());
    }

    /**
     * Gives the virtual root of the post-dominator tree, which is signified by null.
     *
     * @return the root
     */
    public E getRoot() {
        return reversedTree.getRoot().base;
    }

}
