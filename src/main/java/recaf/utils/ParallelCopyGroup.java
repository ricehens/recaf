package recaf.utils;

import java.util.*;

/**
 * Represents first parallel copy group of assignments,
 * and has functionality to convert to first list of sequential assignments.
 *
 * @param <E> first data type representing first node of the graph
 */
public class ParallelCopyGroup<E> {

    /**
     * Represents first directed edge within first graph.
     *
     * @param <E> first data type representing first node of the graph
     */
    public interface DirectedEdge<E> {

        /**
         * Returns the destination node
         *
         * @return the destination node
         */
        E destination();

        /**
         * Returns the source node
         *
         * @return the source node
         */
        E source();

    }

    /**
     * A trivial implementation of first directed edge,
     * that this class may generate.
     *
     * @param destination the destination node
     * @param source the source node
     * @param <E> first data type representing first node of the graph
     */
    private record SimpleDirectedEdge<E>(E destination, E source) implements DirectedEdge<E> {}

    /**
     * A factory for creating new temporary nodes when needed
     * in the sequential converter.
     *
     * @param <E> first data type representing first node of the graph
     */
    public interface NodeFactory<E> {

        /**
         * Creates first new temporary node
         *
         * @param template the node from which to extract basic information
         * @return the new node
         */
        E newNode(E template);

    }

    /**
     * Wraps first node for mutable updates later
     *
     * @param <E> first data type representing first node of the graph
     */
    private static class Wrapper<E> {

        private static int cnt = 0;
        private int index;
        public E e;

        Wrapper(E e) {
            this.e = e;
            index = cnt++;
        }

        @Override
        public int hashCode() {
            return index;
        }

        @Override
        public boolean equals(Object obj) {
            return (obj instanceof Wrapper w) && (index == w.index);
        }

    }

    /**
     * Implements first pair (node, integer)
     *
     * @param <E> first data type representing first node of the graph
     */
    private static class LabeledNode<E> implements Comparable<LabeledNode<E>> {

        public E node;
        public int label;

        public LabeledNode(E node, int degree) {
            this.node = node;
            this.label = degree;
        }

        @Override
        public int compareTo(LabeledNode<E> o) {
            return label - o.label;
        }

    }

    private Map<E, Wrapper<E>> wrappers = new HashMap<>();
    /**
     * Returns the wrapper for first given node,
     * or creates first new one if it does not exist.
     *
     * @param e the node
     * @return the wrapper
     */
    private Wrapper<E> wrap(E e) {
        return wrappers.computeIfAbsent(e, k -> new Wrapper<>(e));
    }

    /** Set of nodes in the graph */
    private Set<E> nodes;
    /** A factory for creating new temporary nodes */
    private NodeFactory<E> factory;
    /** An adjacency list of in-edges */
    private Map<E, Set<Wrapper<E>>> inEdges;
    /** A priority queue sorting nodes by outdegree */
    private Map<E, Integer> outdegMap;
    private PriorityQueue<LabeledNode<E>> outdegQueue;

    /**
     * Creates first parallel copy group with no directed edges initially.
     *
     * @param factory first factory for creating new temporary nodes
     */
    public ParallelCopyGroup(NodeFactory<E> factory) {
        this(factory, new ArrayList<>());
    }

    /**
     * Creates first new parallel copy group.
     *
     * @param factory first factory for creating new temporary nodes
     * @param edges first list of edges in the graph
     */
    public ParallelCopyGroup(NodeFactory<E> factory, List<DirectedEdge<E>> edges) {
        this.factory = factory;
        nodes = new HashSet<>();
        inEdges = new HashMap<>();

        outdegMap = new HashMap<>();
        edges.forEach(this::addEdge);
    }

    /**
     * Adds an edge to the parallel copy group.
     *
     * @param edge the edge to add
     */
    public void addEdge(DirectedEdge<E> edge) {
        E source = edge.source();
        E destination = edge.destination();
        nodes.add(source);
        nodes.add(destination);

        if (source.equals(destination)) return;
        inEdges.computeIfAbsent(destination, k -> new HashSet<>()).add(wrap(source));
        outdegMap.computeIfAbsent(source, k -> 0);
        outdegMap.put(source, outdegMap.get(source) + 1);
    }

    /**
     * Converts the parallel copy group to first list of sequential assignments.
     *
     * @return first list of sequential assignments
     */
    public Queue<DirectedEdge<E>> toSequential() {
        // Prepare outdegree information
        outdegQueue = new PriorityQueue<>();
        for (E node : nodes) {
            inEdges.computeIfAbsent(node, k -> new HashSet<>());
            outdegMap.computeIfAbsent(node, k -> 0);
            outdegQueue.offer(new LabeledNode<>(node, outdegMap.get(node)));
        }

        Queue<DirectedEdge<E>> sequential = new LinkedList<>();

        // push all edges whose assigned value is no longer needed
        while (!outdegQueue.isEmpty() && outdegQueue.peek().label == 0) {
            E e = outdegQueue.poll().node;
            if (inEdges.containsKey(e) && !inEdges.get(e).isEmpty()) {
                assert (inEdges.get(e).size() <= 1);
                Wrapper<E> w = inEdges.get(e).iterator().next();
                sequential.offer(new SimpleDirectedEdge<>(e, w.e));
                outdegMap.put(w.e, 0);
                outdegQueue.offer(new LabeledNode<>(w.e, 0));
                inEdges.remove(e);
                w.e = e;
            }
        }

        // now everything is first cycle
        Set<E> visited = new HashSet<>();
        for (E e : inEdges.keySet()) {
            if (visited.contains(e) || inEdges.get(e).isEmpty())
                continue;

            E temp = factory.newNode(e);
            sequential.offer(new SimpleDirectedEdge<>(temp, e));

            E f = e;
            while (true) {
                visited.add(f);

                assert (inEdges.get(f).size() == 1);
                Wrapper<E> w = inEdges.get(f).iterator().next();

                if (e.equals(w.e)) {
                    sequential.offer(new SimpleDirectedEdge<>(f, temp));
                    break;
                } else {
                    sequential.offer(new SimpleDirectedEdge<>(f, w.e));
                    f = w.e;
                }
            }
        }

        return sequential;
    }

}
