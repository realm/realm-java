/*
 * Copyright 2014 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.realm;


import java.util.AbstractList;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import io.realm.exceptions.RealmException;
import io.realm.internal.TableOrView;
import io.realm.internal.TableView;

/**
 * This class holds all the matches of a {@link io.realm.RealmQuery} for a given Realm. The objects
 * are not copied from the Realm to the RealmResults list, but are just referenced from the
 * RealmResult instead. This saves memory and increases speed.
 * <p>
 * RealmResults are live views, which means that if it is on an {@link android.os.Looper} thread,
 * it will automatically update its query results after a transaction has been committed. If on a
 * non-looper thread, {@link Realm#refresh()} must be called to update the results.
 * <p>
 * Updates to RealmObjects from a RealmResults list must be done from within a transaction and the
 * modified objects are persisted to the Realm file during the commit of the transaction.
 * <p>
 * A RealmResults object cannot be passed between different threads.
 * <p>
 * Notice that a RealmResults is never {@code null} not even in the case where it contains no objects. You
 * should always use the size() method to check if a RealmResults is empty or not.
 *
 * @param <E> The class of objects in this list
 * @see RealmQuery#findAll()
 * @see Realm#allObjects(Class)
 * @see io.realm.Realm#beginTransaction()
 */
public class RealmResults<E extends RealmObject> extends AbstractList<E> {

    BaseRealm realm;
    Class<E> classSpec;   // Return type
    String className;     // Class name for DynamicRealmObjects -> TODO This is not set properly
    private TableOrView table = null;

    private static final String TYPE_MISMATCH = "Field '%s': type mismatch - %s expected.";
    private long currentTableViewVersion = -1;


    static <E extends RealmObject> RealmResults<E> createFromClass(BaseRealm realm, Class<E> clazz) {
        return new RealmResults<E>(realm, clazz);
    }

    static <E extends RealmObject> RealmResults<E> createFromQuery(BaseRealm realm, TableOrView table, Class<E> clazz) {
        return new RealmResults<E>(realm, table, clazz);
    }

    static RealmResults<DynamicRealmObject> createFromDynamicClass(BaseRealm realm, String className) {
        return new RealmResults<DynamicRealmObject>(realm, className);
    }

    static RealmResults<DynamicRealmObject> createFromDynamicQuery(BaseRealm realm, TableOrView table, String className) {
        return new RealmResults<DynamicRealmObject>(realm, table, className);
    }

    private RealmResults(BaseRealm realm, Class<E> classSpec) {
        this.realm = realm;
        this.classSpec = classSpec;
    }

    private RealmResults(BaseRealm realm, TableOrView table, Class<E> classSpec) {
        this(realm, classSpec);
        this.table = table;
    }

    private RealmResults(BaseRealm realm, String className) {
        this.realm = realm;
        this.className = className;
    }

    private RealmResults(BaseRealm realm, TableOrView table, String className) {
        this(realm, className);
        this.table = table;
    }

    TableOrView getTable() {
        if (table == null) {
            return realm.getTable(classSpec);
        } else {
            return table;
        }
    }

    /**
     * Returns a typed {@link io.realm.RealmQuery}, which can be used to query for specific
     * objects of this type.
     *
     * @return A typed RealmQuery
     * @see io.realm.RealmQuery
     */
    public RealmQuery<E> where() {
        realm.checkIfValid();
        return RealmQuery.createQueryFromResult(this);
    }

    /**
     * Returns the element at the specified location in this list.
     *
     * @param location the index of the element to return.
     * @return the element at the specified index.
     * @throws IndexOutOfBoundsException if {@code location < 0 || location >= size()}
     */
    @Override
    public E get(int location) {
        E obj;
        realm.checkIfValid();
        TableOrView table = getTable();
        if (table instanceof TableView) {
            obj = realm.get(classSpec, className, ((TableView)table).getSourceRowIndex(location));
        } else {
            obj = realm.get(classSpec, className, location);
        }

        return obj;
    }

    /**
     * This method is not supported.
     *
     * @throws NoSuchMethodError always.
     */
    @Override
    public int indexOf(Object o) {
        throw new NoSuchMethodError("indexOf is not supported on RealmResults");
    }

    /**
     * Get the first object from the list.
     * @return The first object.
     * @throws ArrayIndexOutOfBoundsException if RealmResults is empty.
     */
    public E first() {
        return get(0);
    }

    /**
     * Get the last object from the list.
     * @return The last object.
     * @throws ArrayIndexOutOfBoundsException if RealmResults is empty.
     */
    public E last() {
        return get(size()-1);
    }

    /**
     * Returns an iterator for the results of a query. Any change to Realm while iterating will
     * cause this iterator to throw a {@link java.util.ConcurrentModificationException} if accessed.
     *
     * @return  an iterator on the elements of this list.
     * @see     Iterator
     */
    @Override
    public Iterator<E> iterator() {
        return new RealmResultsIterator();
    }

    /**
     * Returns a list iterator for the results of a query. Any change to Realm while iterating will
     * cause the iterator to throw a {@link java.util.ConcurrentModificationException} if accessed.
     *
     * @return  a ListIterator on the elements of this list.
     * @see     ListIterator
     */
    @Override
    public ListIterator<E> listIterator() {
        return new RealmResultsListIterator(0);
    }

    /**
     * Returns a list iterator on the results of a query. Any change to Realm while iterating will
     * cause the iterator to throw a {@link java.util.ConcurrentModificationException} if accessed.
     *
     * @param location  the index at which to start the iteration.
     * @return          a ListIterator on the elements of this list.
     * @throws          IndexOutOfBoundsException if {@code location < 0 || location > size()}
     * @see             ListIterator
     */
    @Override
    public ListIterator<E> listIterator(int location) {
        return new RealmResultsListIterator(location);
    }

    // Sorting

    // aux. method used by sort methods
    private long getColumnIndex(String fieldName) {
        if (fieldName.contains(".")) {
            throw new IllegalArgumentException("Sorting using child object properties is not supported: " + fieldName);
        }
        long columnIndex = table.getColumnIndex(fieldName);
        if (columnIndex < 0) {
            throw new IllegalArgumentException(String.format("Field '%s' does not exist.", fieldName));
        }
        return columnIndex;
    }

    /**
     * Sort (ascending) an existing {@link io.realm.RealmResults}.
     *
     * @param fieldName  The field name to sort by. Only fields of type boolean, short, int, long,
     *                   float, double, Date, and String are supported.
     * @throws java.lang.IllegalArgumentException if field name does not exist.
     */
    public void sort(String fieldName) {
        this.sort(fieldName, Sort.ASCENDING);
    }

    /**
     * Sort existing {@link io.realm.RealmResults}.
     *
     * @param fieldName      The field name to sort by. Only fields of type boolean, short, int,
     *                       long, float, double, Date, and String are supported.
     * @param sortOrder  The direction to sort by; if true ascending, otherwise descending
     *                       You can use the constants SORT_ORDER_ASCENDING and SORT_ORDER_DESCENDING
     *                       for readability.
     * @throws java.lang.IllegalArgumentException if field name does not exist.
     */
    public void sort(String fieldName, Sort sortOrder) {
        if (fieldName == null) {
            throw new IllegalArgumentException("fieldName must be provided");
        }
        realm.checkIfValid();
        TableOrView table = getTable();

        if (table instanceof TableView) {
            long columnIndex = getColumnIndex(fieldName);
            ((TableView) table).sort(columnIndex, sortOrder);
        } else {
            throw new IllegalArgumentException("Only RealmResults can be sorted - please use allObject() to create a RealmResults.");
        }
    }

    /**
     * Sort existing {@link io.realm.RealmResults}.
     *
     * @param fieldNames an array of field names to sort by. Only fields of type boolean, short, int,
     *                       long, float, double, Date, and String are supported.
     * @param sortOrders The directions to sort by.
     * @throws java.lang.IllegalArgumentException if a field name does not exist.
     */
    public void sort(String fieldNames[], Sort sortOrders[]) {
        if (fieldNames == null) {
            throw new IllegalArgumentException("fieldNames must be provided.");
        } else if (sortOrders == null) {
            throw new IllegalArgumentException("sortOrder must be provided.");
        }

        if (fieldNames.length == 1 && sortOrders.length == 1) {
            sort(fieldNames[0], sortOrders[0]);
        } else {
            realm.checkIfValid();
            TableOrView table = getTable();
            if (table instanceof TableView) {
                List<Long> columnIndices = new ArrayList<Long>();
                for (int i = 0; i < fieldNames.length; i++) {
                    String fieldName = fieldNames[i];
                    long columnIndex = getColumnIndex(fieldName);
                    columnIndices.add(columnIndex);
                }
                ((TableView) table).sort(columnIndices, sortOrders);
            }
        }
    }

    /**
     * Sort existing {@link io.realm.RealmResults} using two fields.
     *
     * @param fieldName1 first field name.
     * @param sortOrder1 sort order for first field.
     * @param fieldName2 second field name.
     * @param sortOrder2 sort order for second field.
     * @throws java.lang.IllegalArgumentException if a field name does not exist.
     */
    public void sort(String fieldName1, Sort sortOrder1, String fieldName2, Sort sortOrder2) {
        sort(new String[] {fieldName1, fieldName2}, new Sort[] {sortOrder1, sortOrder2});
    }

    /**
     * Sort existing {@link io.realm.RealmResults} using three fields.
     *
     * @param fieldName1 first field name.
     * @param sortOrder1 sort order for first field.
     * @param fieldName2 second field name.
     * @param sortOrder2 sort order for second field.
     * @param fieldName3 third field name.
     * @param sortOrder3 sort order for third field.
     * @throws java.lang.IllegalArgumentException if a field name does not exist.
     */
    public void sort(String fieldName1, Sort sortOrder1, String fieldName2, Sort sortOrder2, String fieldName3, Sort sortOrder3) {
        sort(new String[] {fieldName1, fieldName2, fieldName3}, new Sort[] {sortOrder1, sortOrder2, sortOrder3});
    }

    // Aggregates

    /**
     * Returns the number of elements in this query result.
     *
     * @return the number of elements in this query result.
     */
    @Override
    public int size() {
        return ((Long)getTable().size()).intValue();
    }

    /**
     * Find the minimum value of a field.
     *
     * @param fieldName   the field to look for a minimum on. Only number fields are supported.
     * @return            if no objects exist or they all have {@code null} as the value for the given
     *                    field, {@code null} will be returned. Otherwise the minimum value is returned.
     *                    When determining the minimum value, objects with {@code null} values are ignored.
     * @throws            java.lang.IllegalArgumentException if the field is not a number type.
     */
    public Number min(String fieldName) {
        realm.checkIfValid();
        long columnIndex = table.getColumnIndex(fieldName);
        switch (table.getColumnType(columnIndex)) {
            case INTEGER:
                return table.minimumLong(columnIndex);
            case FLOAT:
                return table.minimumFloat(columnIndex);
            case DOUBLE:
                return table.minimumDouble(columnIndex);
            default:
                throw new IllegalArgumentException(String.format(TYPE_MISMATCH, fieldName, "int, float or double"));
        }
    }

    /**
     * Find the minimum date.
     *
     * @param fieldName  The field to look for the minimum date. If fieldName is not of Date type,
     *                   an exception is thrown.
     * @return           If no objects exist or they all have {@code null} as the value for the given
     *                   date field, {@code null} will be returned. Otherwise the minimum date is returned.
     *                   When determining the minimum date, objects with {@code null} values are ignored.
     * @throws           java.lang.IllegalArgumentException if fieldName is not a Date field.
     */
    public Date minDate(String fieldName) {
        realm.checkIfValid();
        long columnIndex = table.getColumnIndex(fieldName);
        if (table.getColumnType(columnIndex) == RealmFieldType.DATE) {
            return table.minimumDate(columnIndex);
        }
        else {
            throw new IllegalArgumentException(String.format(TYPE_MISMATCH, fieldName, "Date"));
        }
    }

    /**
     * Find the maximum value of a field.
     *
     * @param fieldName   the field to look for a maximum on. Only number fields are supported.
     * @return            if no objects exist or they all have {@code null} as the value for the given
     *                    field, {@code null} will be returned. Otherwise the maximum value is returned.
     *                    When determining the maximum value, objects with {@code null} values are ignored.
     * @throws            java.lang.IllegalArgumentException if the field is not a number type.
     */
    public Number max(String fieldName) {
        realm.checkIfValid();
        long columnIndex = table.getColumnIndex(fieldName);
        switch (table.getColumnType(columnIndex)) {
            case INTEGER:
                return table.maximumLong(columnIndex);
            case FLOAT:
                return table.maximumFloat(columnIndex);
            case DOUBLE:
                return table.maximumDouble(columnIndex);
            default:
                throw new IllegalArgumentException(String.format(TYPE_MISMATCH, fieldName, "int, float or double"));
        }
    }

    /**
     * Find the maximum date.
     *
     * @param fieldName  the field to look for the maximum date. If fieldName is not of Date type,
     *                   an exception is thrown.
     * @return           if no objects exist or they all have {@code null} as the value for the given
     *                   date field, {@code null} will be returned. Otherwise the maximum date is returned.
     *                   When determining the maximum date, objects with {@code null} values are ignored.
     * @throws           java.lang.IllegalArgumentException if fieldName is not a Date field.
     */
    public Date maxDate(String fieldName) {
        realm.checkIfValid();
        long columnIndex = table.getColumnIndex(fieldName);
        if (table.getColumnType(columnIndex) == RealmFieldType.DATE) {
            return table.maximumDate(columnIndex);
        }
        else {
            throw new IllegalArgumentException(String.format(TYPE_MISMATCH, fieldName, "Date"));
        }
    }


    /**
     * Calculate the sum of a given field.
     *
     * @param fieldName   the field to sum. Only number fields are supported.
     * @return            the sum. If no objects exist or they all have {@code null} as the value for the given field,
     *                    {@code 0} will be returned. When computing the sum, objects with {@code null} values are ignored.
     * @throws            java.lang.IllegalArgumentException if the field is not a number type.
     */
    public Number sum(String fieldName) {
        realm.checkIfValid();
        long columnIndex = table.getColumnIndex(fieldName);
        switch (table.getColumnType(columnIndex)) {
            case INTEGER:
                return table.sumLong(columnIndex);
            case FLOAT:
                return table.sumFloat(columnIndex);
            case DOUBLE:
                return table.sumDouble(columnIndex);
            default:
                throw new IllegalArgumentException(String.format(TYPE_MISMATCH, fieldName, "int, float or double"));
        }
    }


    /**
     * Returns the average of a given field.
     *
     * @param fieldName  the field to calculate average on. Only number fields are supported.
     * @return           The average for the given field amongst objects in query results. This
     *                   will be of type double for all types of number fields. If no objects exist or
     *                   they all have {@code null} as the value for the given field, {@code 0} will be returned.
     *                   When computing the average, objects with {@code null} values are ignored.
     * @throws           java.lang.IllegalArgumentException if the field is not a number type.
     */
    public double average(String fieldName) {
        realm.checkIfValid();
        long columnIndex = table.getColumnIndex(fieldName);
        switch (table.getColumnType(columnIndex)) {
            case INTEGER:
                return table.averageLong(columnIndex);
            case DOUBLE:
                return table.averageDouble(columnIndex);
            case FLOAT:
                return table.averageFloat(columnIndex);
            default:
                throw new IllegalArgumentException(String.format(TYPE_MISMATCH, fieldName, "int, float or double"));
        }
    }


    // Deleting

    /**
     * Removes an object at a given index. This also deletes the object from the underlying Realm.
     *
     * Using this method while iterating the list can result in a undefined behavior. Use
     * {@link io.realm.RealmResults.RealmResultsIterator#remove()} instead.
     *
     * @param index      The array index identifying the object to be removed.
     * @return           Always return {@code null}.
     */
    @Override
    public E remove(int index) {
        TableOrView table = getTable();
        table.remove(index);
        return null; // Returning the object doesn't make sense, since it could no longer access any data.
    }

    /**
     * Removes and returns the last object in the list. This also deletes the object from the
     * underlying Realm.
     *
     * Using this method while iterating the list can result in a undefined behavior. Use
     * {@link io.realm.RealmResults.RealmResultsListIterator#removeLast()} instead.
     */
    public void removeLast() {
        TableOrView table = getTable();
        table.removeLast();
    }

    /**
     * Removes all objects from the list. This also deletes the objects from the
     * underlying Realm.
     */
    public void clear() {
        TableOrView table = getTable();
        table.clear();
    }

    // Adding objects

    @Override
    @Deprecated
    public boolean add(E element) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public void add(int index, E element) {
        throw new UnsupportedOperationException();
    }
//
//    /**
//     * Replaces an object at the given index with a new object.
//     *
//     * @param index       The array index of the object to be replaced.
//     * @param element     An object.
//     */
//    public void replace(int index, E element) {
//        throw new NoSuchMethodError();
//    }

    private void assertRealmIsStable() {
        long version = table.sync();
        if (currentTableViewVersion > -1 && version != currentTableViewVersion) {
            throw new ConcurrentModificationException("No outside changes to a Realm is allowed while iterating a RealmResults. Use iterators methods instead.");
        }

        currentTableViewVersion = version;
    }

    // Custom RealmResults iterator. It ensures that we only iterate on a Realm that hasn't changed.
    private class RealmResultsIterator implements Iterator<E> {

        int pos = -1;

        RealmResultsIterator() {
            currentTableViewVersion = table.sync();
        }

        public boolean hasNext() {
            assertRealmIsStable();
            return pos + 1 < size();
        }

        public E next() {
            assertRealmIsStable();
            pos++;
            if (pos >= size()) {
                throw new IndexOutOfBoundsException("Cannot access index " + pos + " when size is " + size() +  ". Remember to check hasNext() before using next().");
            }
            return get(pos);
        }

        /**
         * Removes the RealmObject at the current position from both the list and the underlying Realm.
         *
         * WARNING: This method is currently disabled and will always throw an
         * {@link io.realm.exceptions.RealmException}
         */
        public void remove() {
            throw new RealmException("Removing is not supported.");
    /*        assertRealmIsStable();
            if (pos == -1) {
                throw new IllegalStateException("Must call next() before calling remove()");
            }
            if (removeUsed) {
                throw new IllegalStateException("Cannot call remove() twice. Must call next() in between");
            }

            RealmResults.this.remove(pos);
            pos--;
            removeUsed = true;
            currentTableViewVersion = getTable().sync();
     */   }
    }

    // Custom RealmResults list iterator. It ensures that we only iterate on a Realm that hasn't changed.
    private class RealmResultsListIterator extends RealmResultsIterator implements ListIterator<E> {

        RealmResultsListIterator(int start) {
            if (start >= 0 && start <= size()) {
                pos = start - 1;
            } else {
                throw new IndexOutOfBoundsException("Starting location must be a valid index: [0, " + (size() - 1) + "]. Yours was " + start);
            }
        }

        @Override
        public void add(E object) {
            throw new RealmException("Adding elements not supported. Use Realm.createObject() instead.");
        }

        @Override
        public boolean hasPrevious() {
            assertRealmIsStable();
            return pos > 0;
        }

        @Override
        public int nextIndex() {
            assertRealmIsStable();
            return pos + 1;
        }

        @Override
        public E previous() {
            assertRealmIsStable();
            pos--;
            if (pos < 0) {
                throw new IndexOutOfBoundsException("Cannot access index less than zero. This was " + pos + ". Remember to check hasPrevious() before using previous().");
            }
            return get(pos);
        }

        @Override
        public int previousIndex() {
            assertRealmIsStable();
            return pos;
        }

        @Override
        public void set(E object) {
            throw new RealmException("Replacing elements not supported.");
        }


        /**
         * Removes the RealmObject at the current position from both the list and the underlying Realm.
         *
         * WARNING: This method is currently disabled and will always throw an
         * {@link io.realm.exceptions.RealmException}
         */
        @Override
        public void remove() { throw new RealmException("Removing elements not supported."); }
    }
}
