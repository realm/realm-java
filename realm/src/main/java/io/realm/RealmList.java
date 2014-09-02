package io.realm;

import java.lang.reflect.Array;
import java.util.List;

public interface RealmList<E extends RealmObject> extends List<E> {

    // private Realm realm;
    // private boolean readOnly;

    void move(int oldPos, int newPos);

    /**
     *
     * @param rowIndex      The objects index in the list
     * @return              An object of type E, which is backed by Realm
     */
    @Override
    E get(int rowIndex);

    /**
     * Gets the first object in this list
     * @return              An object of type E, which is backed by Realm
     */
    E first();

    /**
     * Gets the last object in this list
     * @return              An object of type E, which is backed by Realm
     */
    E last();

    /**
     *
     * @return              The number of elements in this RealmList
     */
    @Override
    int size();

    /**
     * Returns a RealmQuery, used to filter this RealmList
     *
     * @return              A RealmQuery to filter the list
     */
    RealmQuery<E> where();

    /**
     * Not implemented.
     * Suggested rename: add
     *
     * @param element
     */
    void addObject(E element) throws NoSuchMethodException;

    /**
     * Not implemented.
     * Suggested rename: add
     *
     * @param array
     */
    void addObjectFromArray(Array array) throws NoSuchMethodException;


    /**
     * Not implemented.
     * Suggested rename: sort
     *
     * @param property
     * @return
     */
    RealmList<E> arraySortForProperty(String property) throws NoSuchMethodException;

    /**
     * Not implemented.
     * Suggested rename: average
     *
     * @param propertyName
     * @return
     */
    double averageOfProperty(String propertyName) throws NoSuchMethodException;

    /**
     * Not implemented.
     * Suggested rename: sum
     *
     * @param propertyName
     * @return
     */
    double sumOfProperty(String propertyName) throws NoSuchMethodException;

    /**
     * Not implemented.
     * Suggested rename: min
     *
     * @param propertyName
     * @return
     */
    int minOfProperty(String propertyName) throws NoSuchMethodException;

    /**
     * Not implemented.
     * Suggested rename: max
     *
     * @param propertyName
     * @return
     */
    int maxOfProperty(String propertyName) throws NoSuchMethodException;

    /**
     * Not implemented.
     *
     * @param element
     * @return
     */
    int indexOfObject(E element) throws NoSuchMethodException;

    /**
     * Not implemented.
     * Suggested rename: where (and return RealmQuery
     *
     * @return
     */
    int indexOfObjectWhere() throws NoSuchMethodException;

    /**
     * Not implemented.
     * Suggested rename: insert
     *
     * @param element
     * @param index
     */
    void insertObjectAtIndex(E element, int index) throws NoSuchMethodException;

    /**
     * Not implemented.
     * Suggested rename: removeLast
     */
    void removeLastObject() throws NoSuchMethodException;

    /**
     * Not element.
     * Suggested rename: replace
     *
     * @param index
     * @param newElement
     */
    void replaceObjectAtIndexWithObject(int index, E newElement) throws NoSuchMethodException;

    /**
     * Not implemented.
     *
     * @return
     */
    String JSONString() throws NoSuchMethodException;
}
