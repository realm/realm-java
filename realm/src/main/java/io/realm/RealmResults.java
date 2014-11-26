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
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Iterator;
import java.util.ListIterator;

import io.realm.exceptions.RealmException;
import io.realm.internal.ColumnType;
import io.realm.internal.Table;
import io.realm.internal.TableOrView;
import io.realm.internal.TableView;

/**
 * A RealmResults list contains a list of objects of a given type that matches the query.
 * The objects are not copied from the Realm to the RealmResults list, but just references the original objects.
 * This preserves memory and increase speed.
 * It also implies that any modification to any object in a RealmResults is reflected in the objects in the 
 * Realm that was queried.
 * Updates to objects must be done within a transaction and the modified object is persisted to the backing
 * Realm file during the commit of the transaction.
 *
 * @param <E> The class of objects in this list
 * @see RealmQuery#findAll()
 * @see Realm#allObjects(Class)
 */
public class RealmResults<E extends RealmObject> extends AbstractList<E> {

    private Class<E> classSpec;
    private Realm realm;
    private TableOrView table = null;

    public static final boolean SORT_ORDER_ASCENDING = true;
    public static final boolean SORT_ORDER_DECENDING = false;

    private static final String TYPE_MISMATCH = "Field '%s': type mismatch - %s expected.";
    private long currentTableViewVersion = -1;

    RealmResults(Realm realm, Class<E> classSpec) {
        this.realm = realm;
        this.classSpec = classSpec;
    }

    RealmResults(Realm realm, TableOrView table, Class<E> classSpec) {
        this(realm, classSpec);
        this.table = table;
    }

    Realm getRealm() {
        return realm;
    }

    TableOrView getTable() {
        if (table == null) {
            return realm.getTable(classSpec);
        } else {
            return table;
        }
    }

    /**
     * Returns a typed @{link io.realm.RealmQuery}, which can be used to query for specific
     * objects of this type.
     *
     * @return A typed RealmQuery
     * @see io.realm.RealmQuery
     */
    public RealmQuery<E> where() {
        realm.assertThread();
        return new RealmQuery<E>(this, classSpec);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E get(int rowIndex) {
        E obj;
        realm.assertThread();
        TableOrView table = getTable();
        if (table instanceof TableView) {
            obj = realm.get(classSpec, ((TableView)table).getSourceRowIndex(rowIndex));
        } else {
            obj = realm.get(classSpec, rowIndex);
        }

        return obj;
    }

    /**
     * Get the first object from the list.
     * @return The first object.
     */
    public E first() {
        return get(0);
    }

    /**
     * Get the last object from the list.
     * @return The last object.
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

    /**
     * Get a sorted (ascending) RealmList from an existing @{link io.realm.RealmList}.
     * Only fields of type boolean, short, int, long, float, double, Date, and String are supported.
     * 
     * @param fieldName  The field name to sort by.
     * @return           A sorted RealmResults list
     */
    public RealmResults<E> sort(String fieldName) {
        return sort(fieldName, SORT_ORDER_ASCENDING);
    }

    /**
     * Get a sorted RealmList from an existing @{link io.realm.RealmList}.
     * Only fields of type boolean, short, int, long, float, double, Date, and String are supported.
     *
     * @param fieldName      The field name to sort by.
     * @param sortAscending  The direction to sort by; if true ascending, otherwise descending
     *                       You can use the constants SORT_ORDER_ASCENDING and SORT_ORDER_DECENDING
     *                       for readability.
     * @return               A sorted RealmResults list.
     */
    public RealmResults<E> sort(String fieldName, boolean sortAscending) {
        TableView sorted;

        realm.assertThread();
        TableOrView table = getTable();
        long columnIndex = table.getColumnIndex(fieldName);
        TableView.Order TVOrder = sortAscending ? TableView.Order.ascending : TableView.Order.descending;

        if (table instanceof TableView) {
            TableView v = (TableView)table;
            sorted = v.where().findAll();
            sorted.sort(columnIndex, TVOrder);
        }
        else {
            Table t = (Table)table;
            sorted = t.getSortedView(columnIndex, TVOrder);
        }

        return new RealmResults<E>(realm, sorted, classSpec);
    }


    // Aggregates

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return ((Long)getTable().size()).intValue();
    }

    /**
     * Find the minimum value of a field.
     *
     * @param fieldName   The field to look for a minimum on. Only int, float, and double
     *                    are supported.
     * @return            The minimum value.
     * @throws            java.lang.IllegalArgumentException if field is not int, float or double.
     */
    public Number min(String fieldName) {
        realm.assertThread();
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
     * @return           The minimum date.
     * @throws           java.lang.IllegalArgumentException if fieldName is not a Date field.
     */
    public Date minDate(String fieldName) {
        realm.assertThread();
        long columnIndex = table.getColumnIndex(fieldName);
        if (table.getColumnType(columnIndex) == ColumnType.DATE) {
            return table.minimumDate(columnIndex);
        }
        else {
            throw new IllegalArgumentException(String.format(TYPE_MISMATCH, fieldName, "Date"));
        }
    }

    /**
     * Find the maximum value of a field.
     *
     * @param fieldName   The field to look for a maximum on. Only int, float, and double are supported.
     * @return            The maximum value.
     * @throws            java.lang.IllegalArgumentException if field is not int, float or double.
     */
    public Number max(String fieldName) {
        realm.assertThread();
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
     * @param fieldName  The field to look for the maximum date. If fieldName is not of Date type,
     *                   an exception is thrown.
     * @return           The maximum date.
     * @throws           java.lang.IllegalArgumentException if fieldName is not a Date field.
     */
    public Date maxDate(String fieldName) {
        realm.assertThread();
        long columnIndex = table.getColumnIndex(fieldName);
        if (table.getColumnType(columnIndex) == ColumnType.DATE) {
            return table.maximumDate(columnIndex);
        }
        else {
            throw new IllegalArgumentException(String.format(TYPE_MISMATCH, fieldName, "Date"));
        }
    }


    /**
     * Calculate the sum of a given field.
     *
     * @param fieldName   The field to sum. Only int, float, and double are supported.
     * @return            The sum.
     * @throws            java.lang.IllegalArgumentException if field is not int, float or double.
     */

    public Number sum(String fieldName) {
        realm.assertThread();
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
     * @param fieldName  The field to calculate average on. Only properties of type int,
     *                   float and double are supported.
     * @return           The average for the given field amongst objects in an RealmList. This
     *                   will be of type double for both float and double field.
     * @throws           java.lang.IllegalArgumentException if field is not int, float or double.
     */
    public double average(String fieldName) {
        realm.assertThread();
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
     * Removes an object at a given index.
     *
     * @param index      The array index identifying the object to be removed.
     * @return           Always return null.
     */
    @Override
    public E remove(int index) {
        TableOrView table = getTable();
        table.remove(index);
        return null;
    }

    /**
     * Removes the last object in the list.
     *
     */
    public void removeLast() {
        TableOrView table = getTable();
        table.removeLast();
    }

    /**
     * Removes all objects from the list.
     *
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
        boolean removeUsed = false;

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
            removeUsed = false;
            if (pos >= size()) {
                throw new IndexOutOfBoundsException("Cannot access index " + pos + " when size is " + size() +  ". Remember to check hasNext() before using next().");
            }

            return get(pos);
        }

        public void remove() {
            assertRealmIsStable();
            if (pos == -1) throw new IllegalStateException("Must call next() before calling remove()");
            if (removeUsed) throw new IllegalStateException("Cannot call remove() twice. Must call next() in between");

            RealmResults.this.remove(pos);
            pos--;
            removeUsed = true;
            currentTableViewVersion = getTable().sync();
        }
    }

    // Custom RealmResults list iterator. It ensures that we only iterate on a Realm that hasn't changed.
    private class RealmResultsListIterator extends RealmResultsIterator implements ListIterator<E> {

        RealmResultsListIterator(int location) {
            if (location < 0 || location >= size()) {
                throw new IndexOutOfBoundsException("Starting location must be a valid index: [0, " + (size() - 1) + "]. Yours was " + pos);
            } else {
                pos = location - 1;
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
            return pos - 1;
        }

        @Override
        public void set(E object) {
            throw new RealmException("Replacing elements not supported.");
        }
    }
}
