package recaf;

import recaf.utils.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class UtilsTest {

    @Test
    void testIdentityHashLinkedList() {
        DoublyLinkedList<String> list = new HashLinkedList<>();
        list.offerFirst("a");
        assert(list.toString().equals("[a]"));
        list.offerLast("b");
        assert(list.toString().equals("[a, b]"));
        list.offerFirst("c");
        assert(list.toString().equals("[c, a, b]"));
        list.offerLast("d");
        assert(list.toString().equals("[c, a, b, d]"));

        assert(list.contains("d"));
        assert(!list.contains("e"));
        assert(!list.isEmpty());

        assert(list.size() == 4);
        assert(list.stream().map(String::toUpperCase).toList().toString().equals("[C, A, B, D]"));

        assert(list.next("a").equals("b"));
        assert(list.prev("d").equals("b"));

        DoublyLinkedList<String> list2 = new HashLinkedList<>(list);
        list2.insertAfter("a", "e");
        list2.insertBefore("d", "f");
        assert(list2.toString().equals("[c, a, e, b, f, d]"));
        list2.insertAfter("d", "y");
        list2.insertBefore("c", "x");
        assert(list2.toString().equals("[x, c, a, e, b, f, d, y]"));

        list2.clear();
        list2.offerFirst("a");
        list2.offerLast("b");
        list2.offerFirst("c");
        list2.offerLast("d");
        assert(list2.toString().equals("[c, a, b, d]"));
        assert(list2.size() == 4);

        DoublyLinkedList<String> list3 = list2.reverse();
        assert(list3.toString().equals("[d, b, a, c]"));
        assert(list3.size() == 4);
        list3.remove("d");
        assert(list3.toString().equals("[b, a, c]"));
        assert(list3.size() == 3);

        list.remove("b");
        assert(list.toString().equals("[c, a, d]"));
        list.pollFirst();
        assert(list.toString().equals("[a, d]"));
        assert(list.size() == 2);
        list.pollLast();
        assert(list.toString().equals("[a]"));
        list.pollFirst();
        assert(list.toString().equals("[]"));
        assert(list.size() == 0);
        assert(list.isEmpty());
    }

    @Test
    void testDominatorTree() {
        DoublyLinkedList<TestNode> nodes = new HashLinkedList<>();
        TestNode[] nodesArray = new TestNode[9];
        for (int i = 0; i < 9; i++) {
            nodesArray[i] = new TestNode();
            nodes.offerLast(nodesArray[i]);
        }
        nodesArray[0].successors.add(nodesArray[1]);
        nodesArray[1].successors.add(nodesArray[2]);
        nodesArray[2].successors.add(nodesArray[3]);
        nodesArray[3].successors.add(nodesArray[4]);
        nodesArray[1].successors.add(nodesArray[5]);
        nodesArray[5].successors.add(nodesArray[6]);
        nodesArray[5].successors.add(nodesArray[8]);
        nodesArray[6].successors.add(nodesArray[7]);
        nodesArray[8].successors.add(nodesArray[7]);
        nodesArray[7].successors.add(nodesArray[3]);
        nodesArray[3].successors.add(nodesArray[1]);

        // initiate tree
        DominatorTree<TestNode> tree = new DominatorTree<>(nodes);

        // test adjacencies
        assert(tree.getSuccessors(nodesArray[0]).size() == 1);
        assert(tree.getSuccessors(nodesArray[1]).size() == 2);
        assert(tree.getSuccessors(nodesArray[4]).isEmpty());

        assert(tree.getPredecessors(nodesArray[0]).isEmpty());
        assert(tree.getPredecessors(nodesArray[1]).size() == 2);
        assert(tree.getPredecessors(nodesArray[7]).size() == 2);

        assert(tree.getSuccessors(nodesArray[1]).contains(nodesArray[2]));
        assert(tree.getPredecessors(nodesArray[2]).contains(nodesArray[1]));
        assert(tree.getSuccessors(nodesArray[6]).contains(nodesArray[7]));
        assert(tree.getPredecessors(nodesArray[7]).contains(nodesArray[6]));

        // (with knowledge of implementation)
        // assert(tree.getPreOrder().toString().equals("[(0), (1), (5), (8), (7), (3), (4), (6), (2)]"));

        // test immediate dominators
        assert(tree.getImmediateDominator(nodesArray[0]) == null);
        assert(tree.getImmediateDominator(nodesArray[1]).equals(nodesArray[0]));
        assert(tree.getImmediateDominator(nodesArray[2]).equals(nodesArray[1]));
        assert(tree.getImmediateDominator(nodesArray[3]).equals(nodesArray[1]));
        assert(tree.getImmediateDominator(nodesArray[4]).equals(nodesArray[3]));
        assert(tree.getImmediateDominator(nodesArray[5]).equals(nodesArray[1]));
        assert(tree.getImmediateDominator(nodesArray[6]).equals(nodesArray[5]));
        assert(tree.getImmediateDominator(nodesArray[7]).equals(nodesArray[5]));
        assert(tree.getImmediateDominator(nodesArray[8]).equals(nodesArray[5]));

        // test dominance frontier
        assert(tree.getDominanceFrontier(nodesArray[0]).isEmpty());
        for (int i = 1; i < 9; i++) {
            assert(tree.getDominanceFrontier(nodesArray[i]).size() == (i == 4 ? 0 : 1));
        }
        assert(tree.getDominanceFrontier(nodesArray[1]).contains(nodesArray[1]));
        assert(tree.getDominanceFrontier(nodesArray[2]).contains(nodesArray[3]));
        assert(tree.getDominanceFrontier(nodesArray[3]).contains(nodesArray[1]));
        assert(tree.getDominanceFrontier(nodesArray[5]).contains(nodesArray[3]));
        assert(tree.getDominanceFrontier(nodesArray[6]).contains(nodesArray[7]));
        assert(tree.getDominanceFrontier(nodesArray[7]).contains(nodesArray[3]));
        assert(tree.getDominanceFrontier(nodesArray[8]).contains(nodesArray[7]));

        assert(tree.getRoot().equals(nodesArray[0]));
    }

    @Test
    void testDominatorTree2() {
        DoublyLinkedList<TestNode> nodes = new HashLinkedList<>();
        TestNode[] nodesArray = new TestNode[11];
        for (int i = 0; i < 11; i++) {
            nodesArray[i] = new TestNode();
            nodes.offerLast(nodesArray[i]);
        }

        nodesArray[2].successors.add(nodesArray[1]);
        nodesArray[3].successors.add(nodesArray[2]);
        nodesArray[4].successors.add(nodesArray[3]);
        nodesArray[5].successors.add(nodesArray[4]);
        nodesArray[6].successors.add(nodesArray[2]);
        nodesArray[7].successors.add(nodesArray[6]);
        nodesArray[9].successors.add(nodesArray[6]);
        nodesArray[8].successors.add(nodesArray[7]);
        nodesArray[8].successors.add(nodesArray[9]);
        nodesArray[4].successors.add(nodesArray[8]);
        nodesArray[2].successors.add(nodesArray[4]);
        nodesArray[10].successors.add(nodesArray[3]);
        nodesArray[0].successors.add(nodesArray[5]);
        nodesArray[0].successors.add(nodesArray[10]);

        DominatorTree<TestNode> tree = new DominatorTree<>(nodes);
        for (int i = 0; i < 10; i++) {
            System.out.printf("%d : %s%n", i, tree.getImmediateDominator(nodesArray[i]));
        }
        assert(tree.getImmediateDominator(nodesArray[0]) == null);
        assert(tree.getImmediateDominator(nodesArray[1]).equals(nodesArray[2]));
        assert(tree.getImmediateDominator(nodesArray[2]).equals(nodesArray[0]));
        assert(tree.getImmediateDominator(nodesArray[3]).equals(nodesArray[0]));
        assert(tree.getImmediateDominator(nodesArray[4]).equals(nodesArray[0]));
        assert(tree.getImmediateDominator(nodesArray[5]).equals(nodesArray[0]));
        assert(tree.getImmediateDominator(nodesArray[6]).equals(nodesArray[8]));
        assert(tree.getImmediateDominator(nodesArray[7]).equals(nodesArray[8]));
        assert(tree.getImmediateDominator(nodesArray[8]).equals(nodesArray[4]));
        assert(tree.getImmediateDominator(nodesArray[9]).equals(nodesArray[8]));
    }

    @Test
    void testPostDominatorTree() {
        DoublyLinkedList<TestNode> nodes = new HashLinkedList<>();
        TestNode[] nodesArray = new TestNode[10];
        for (int i = 0; i < 10; i++) {
            nodesArray[i] = new TestNode();
            nodes.offerLast(nodesArray[i]);
        }
        nodesArray[0].successors.add(nodesArray[1]);
        nodesArray[1].successors.add(nodesArray[2]);
        nodesArray[2].successors.add(nodesArray[3]);
        nodesArray[3].successors.add(nodesArray[4]);
        nodesArray[1].successors.add(nodesArray[5]);
        nodesArray[5].successors.add(nodesArray[6]);
        nodesArray[5].successors.add(nodesArray[8]);
        nodesArray[6].successors.add(nodesArray[7]);
        nodesArray[8].successors.add(nodesArray[7]);
        nodesArray[7].successors.add(nodesArray[3]);
        nodesArray[3].successors.add(nodesArray[1]);
        nodesArray[2].successors.add(nodesArray[9]);

        // initiate tree
        TestNode virtual = new TestNode();
        PostDominatorTree<TestNode> tree = new PostDominatorTree<>(nodes, virtual);

        // test immediate dominators
        for (int i = 0; i < 10; i++) {
            System.out.printf("%d : %s%n", i, tree.getImmediatePostDominator(nodesArray[i]));
        }
        assert(tree.getImmediatePostDominator(nodesArray[0]).equals(nodesArray[1]));
        assert(tree.getImmediatePostDominator(nodesArray[1]).equals(virtual)) : tree.getImmediatePostDominator(nodesArray[1]);
        assert(tree.getImmediatePostDominator(nodesArray[2]).equals(virtual)) : tree.getImmediatePostDominator(nodesArray[2]);
        assert(tree.getImmediatePostDominator(nodesArray[3]).equals(virtual));
        assert(tree.getImmediatePostDominator(nodesArray[4]).equals(virtual));
        assert(tree.getImmediatePostDominator(nodesArray[5]).equals(nodesArray[7]));
        assert(tree.getImmediatePostDominator(nodesArray[6]).equals(nodesArray[7]));
        assert(tree.getImmediatePostDominator(nodesArray[7]).equals(nodesArray[3]));
        assert(tree.getImmediatePostDominator(nodesArray[8]).equals(nodesArray[7]));
        assert(tree.getImmediatePostDominator(nodesArray[9]).equals(virtual));

        assert(tree.getRoot().equals(virtual));
    }

    private static class TestNode implements DominatorTree.Node<TestNode> {
        private static int cnt = 0;
        private final int index;
        public List<TestNode> successors;
        public TestNode() {
            successors = new ArrayList<>();
            index = cnt++;
        }
        @Override
        public List<TestNode> successors() {
            return successors;
        }
        @Override
        public int hashCode() {
            return index;
        }
        @Override
        public boolean equals(Object obj) {
            return obj instanceof TestNode other && index == other.index;
        }
        @Override
        public String toString() {
            return "(" + index + ")";
        }
    }

    @Test
    void testParallelCopyGroup() {
        {
            IntFactory factory = new IntFactory();
            List<ParallelCopyGroup.DirectedEdge<Integer>> edges = List.of(
                    new PII(4, 0),
                    new PII(0, 1),
                    new PII(1, 2),
                    new PII(2, 0)
            );
            ParallelCopyGroup<Integer> group = new ParallelCopyGroup<>(factory, edges);
            var sequential = group.toSequential();
            for (var edge : sequential) {
                System.out.printf("%d <- %d%n", edge.destination(), edge.source());
            }
            assert (sequential.size() == 4);
        }

        System.out.println("========");

        {
            IntFactory factory = new IntFactory();
            List<ParallelCopyGroup.DirectedEdge<Integer>> edges = List.of(
                    new PII(0, 1),
                    new PII(1, 2),
                    new PII(2, 0)
            );
            ParallelCopyGroup<Integer> group = new ParallelCopyGroup<>(factory, edges);
            var sequential = group.toSequential();
            for (var edge : sequential) {
                System.out.printf("%d <- %d%n", edge.destination(), edge.source());
            }
            assert (sequential.size() == 4);
        }

        System.out.println("========");

        {
            IntFactory factory = new IntFactory();
            List<ParallelCopyGroup.DirectedEdge<Integer>> edges = List.of(
                    new PII(0, 1),
                    new PII(1, 2),
                    new PII(2, 0),
                    new PII(3,  0),
                    new PII(4,  1),
                    new PII(5,  2)
            );
            ParallelCopyGroup<Integer> group = new ParallelCopyGroup<>(factory, edges);
            var sequential = group.toSequential();
            for (var edge : sequential) {
                System.out.printf("%d <- %d%n", edge.destination(), edge.source());
            }
            assert (sequential.size() == 6);
        }


        System.out.println("========");

        {
            IntFactory factory = new IntFactory();
            List<ParallelCopyGroup.DirectedEdge<Integer>> edges = List.of(
                    new PII(0, 1),
                    new PII(1, 2),
                    new PII(2, 3),
                    new PII(3, 0),
                    new PII(4, 5),
                    new PII(5, 6),
                    new PII(6, 4)
            );
            ParallelCopyGroup<Integer> group = new ParallelCopyGroup<>(factory, edges);
            var sequential = group.toSequential();
            for (var edge : sequential) {
                System.out.printf("%d <- %d%n", edge.destination(), edge.source());
            }
            assert (sequential.size() == 9);
        }
    }

    private class IntFactory implements ParallelCopyGroup.NodeFactory<Integer> {
       public Integer newNode(Integer template) {
           return -1;
       }
    }

    private record PII(Integer destination, Integer source) implements ParallelCopyGroup.DirectedEdge<Integer> {}

}
