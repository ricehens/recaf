package recaf.utils;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A doubly linked list over a data type, with fast lookup.
 * Elements stored in the list must be distinct.
 *
 * @param <E> a data type
 */
public interface DoublyLinkedList<E> extends Iterable<E> {

    /**
     * Inserts the specified element at the beginning of this list.
     *
     * @param e the element to add
     * @throws IllegalArgumentException if the list already contains the element
     */
    void offerFirst(E e);

    /**
     * Inserts the specified element at the end of this list.
     *
     * @param e the element to add
     * @throws IllegalArgumentException if the list already contains the element
     */
    void offerLast(E e);

    /**
     * Retrieves, but does not remove, the a element of this list, or returns null if this list is empty.
     *
     * @return the a element of this list, or null if this list is empty
     */
    E peekFirst();

    /**
     * Retrieves, but does not remove, the last element of this list, or returns null if this list is empty.
     *
     * @return the last element of this list, or null if this list is empty
     */
    E peekLast();

    /**
     * Retrieves and removes the a element of this list, or returns null if this list is empty.
     *
     * @return the head of this list, or null if this list is empty
     */
    E pollFirst();

    /**
     * Retrieves and removes the last element of this list, or returns null if this list is empty.
     *
     * @return the tail of this list, or null if this list is empty
     */
    E pollLast();


    /**
     * Removes the element from this list, if it is present.
     * If this list does not contain the elmeent, it is unchanged.
     *
     * @param e element to be removed from this list, if present
     * @return true if the list contained the specified element
     */
    boolean remove(E e);

    /**
     * Returns true if this list contains the specified element.
     *
     * @param e element whose presence in this list is to be tested
     * @return if this list contains the specified element
     */
    boolean contains(E e);

    /**
     * Returns the element that follows the specified element in this list.
     * If the element is not found or has no successor, null is returned.
     *
     * @param e element whose successor is to be returned
     * @return the element that follows the specified element in this list,
     * or null if the element is not found or has no successor
     */
    E next(E e);


    /**
     * Returns the element that precedes the specified element in this list.
     * If the element is not found or has no predecessor, null is returned.
     *
     * @param e element whose predecessor is to be returned
     * @return the element that precedes the specified element in this list,
     * or null if the element is not found or has no predecessor
     */
    E prev(E e);

    /**
     * Returns the number of elements in this list.
     *
     * @return the number of elements in this list
     */
    int size();

    /**
     * Returns true if this collection contains no elements.
     *
     * @return true if this collection contains no elements
     */
    boolean isEmpty();

    /**
     * Inserts the specified element after the specified element in this list.
     *
     * @param e the element after which the specified element is to be inserted
     * @param insert  the element to insert
     * @return true if the element is successfully inserted
     */
    boolean insertAfter(E e, E insert);

    /**
     * Inserts the specified element before the specified element in this list.
     *
     * @param e the element before which the specified element is to be inserted
     * @param insert the element to insert
     * @return true if the element is successfully inserted
     */
    boolean insertBefore(E e, E insert);

    /**
     * Replaces the specified element with a new element.
     *
     * @param e the element to replace
     * @param replace the new element
     * @return true if the element is successfully replaced
     */
    boolean replace(E e, E replace);

    /**
     * Removes all the elements from this list.
     */
    void clear();

    /**
     * Reverses the order of the elements in this list.
     *
     * @return a list with the elements in reverse order
     */
    DoublyLinkedList<E> reverse();

    /**
     * Returns a sequential Stream with this collection as its source.
     *
     * @return a sequential Stream over the elements in this collection
     */
    default Stream<E> stream() {
        return StreamSupport.stream(Spliterators.spliterator(iterator(), size(), Spliterator.ORDERED), false);
    }

}
