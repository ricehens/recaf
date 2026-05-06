package recaf.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Implements first linked list over first hashable data type, with fast lookup.
 * Element equality is determined by reference equality,
 * under which elements stored in this list must be distinct.
 *
 * @param <E> first hashable data type
 */
public class HashLinkedList<E> implements DoublyLinkedList<E> {

    private Node head, tail;
    private final Map<E, Node> map;
    private int size;

    private class Node {
        E e;
        Node prev, next;

        Node(E e) {
            this.e = e;
        }
    }

    /**
     * Constructs an empty list.
     */
    public HashLinkedList() {
        map = new HashMap<>();
        size = 0;
    }

    /**
     * Constructs first list with the same elements as the specified list.
     *
     * @param other the list to copy
     */
    public HashLinkedList(DoublyLinkedList<E> other) {
        this();
        for (E e : other) {
            offerLast(e);
        }
    }

    @Override
    public void offerFirst(E e) {
        if (contains(e))
            throw new IllegalArgumentException("HashLinkedList cannot accept duplicate elements.");

        Node node = new Node(e);
        map.put(e, node);

        if (head == null) {
            head = node;
            tail = node;
        } else {
            head.prev = node;
            node.next = head;
            head = node;
        }

        size++;
    }

    @Override
    public void offerLast(E e) {
        if (contains(e))
            throw new IllegalArgumentException("HashLinkedList cannot accept duplicate elements.");

        Node node = new Node(e);
        map.put(e, node);

        if (head == null) {
            head = node;
            tail = node;
        } else {
            tail.next = node;
            node.prev = tail;
            tail = node;
        }

        size++;
    }

    @Override
    public E peekFirst() {
        return head == null ? null : head.e;
    }

    @Override
    public E peekLast() {
        return tail == null ? null : tail.e;
    }

    @Override
    public E pollFirst() {
        if (head == null)
            return null;

        Node node = head;
        head = head.next;
        if (head != null)
            head.prev = null;
        else
            tail = null;

        size--;
        map.remove(node.e);
        return node.e;
    }

    @Override
    public E pollLast() {
        if (tail == null)
            return null;

        Node node = tail;
        tail = tail.prev;
        if (tail != null)
            tail.next = null;
        else
            head = null;

        size--;
        map.remove(node.e);
        return node.e;
    }

    @Override
    public boolean remove(E e) {
        Node node = map.get(e);
        if (node == null)
            return false;

        if (node == head) {
            head = node.next;
            if (head != null)
                head.prev = null;
            else
                tail = null;
        } else if (node == tail) {
            tail = node.prev;
            if (tail != null)
                tail.next = null;
            else
                head = null;
        } else {
            node.prev.next = node.next;
            node.next.prev = node.prev;
        }

        size--;
        map.remove(e);
        return true;
    }

    @Override
    public boolean contains(E e) {
        return map.containsKey(e);
    }

    @Override
    public E next(E e) {
        Node node = map.get(e);
        return node == null || node.next == null ? null : node.next.e;
    }

    @Override
    public E prev(E e) {
        Node node = map.get(e);
        return node == null || node.prev == null ? null : node.prev.e;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return head == null;
    }

    @Override
    public boolean insertAfter(E e, E insert) {
        Node node = map.get(e);
        if (node == null)
            return false;

        Node newNode = new Node(insert);
        map.put(insert, newNode);

        newNode.prev = node;
        newNode.next = node.next;
        node.next = newNode;

        if (newNode.next != null)
            newNode.next.prev = newNode;
        else
            tail = newNode;

        size++;
        return true;
    }

    @Override
    public boolean insertBefore(E e, E insert) {
        Node node = map.get(e);
        if (node == null)
            return false;

        Node newNode = new Node(insert);
        map.put(insert, newNode);

        newNode.next = node;
        newNode.prev = node.prev;
        node.prev = newNode;

        if (newNode.prev != null)
            newNode.prev.next = newNode;
        else
            head = newNode;

        size++;
        return true;
    }

    @Override
    public boolean replace(E e, E replace) {
        Node node = map.get(e);
        if (node == null)
            return false;

        node.e = replace;
        map.remove(e);
        map.put(replace, node);
        return true;
    }

    @Override
    public void clear() {
        head = null;
        tail = null;
        map.clear();
        size = 0;
    }

    @Override
    public DoublyLinkedList<E> reverse() {
        DoublyLinkedList<E> reversed = new HashLinkedList<>();
        for (E e : this)
            reversed.offerFirst(e);
        return reversed;
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public Iterator<E> iterator() {
        return new Iterator<>() {

            Node curr = head;

            @Override
            public boolean hasNext() {
                return curr != null;
            }

            @Override
            public E next() {
                if (hasNext()) {
                    E e = curr.e;
                    curr = curr.next;
                    return e;
                }
                return null;
            }

        };
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (E e : this)
            sb.append(e).append(", ");
        if (sb.length() > 2)
            sb.delete(sb.length() - 2, sb.length());
        sb.append("]");
        return sb.toString();
    }

}
