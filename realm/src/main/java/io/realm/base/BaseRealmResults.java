package io.realm.base;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import io.realm.RealmObject;
import io.realm.exceptions.RealmException;
import io.realm.internal.ColumnType;
import io.realm.internal.TableOrView;
import io.realm.internal.TableView;

public abstract class BaseRealmResults<O extends RealmObject, Q extends BaseRealmQuery> extends AbstractList<O> {

    public static final boolean SORT_ORDER_ASCENDING = true;
    public static final boolean SORT_ORDER_DESCENDING = false;

    private static final String TYPE_MISMATCH = "Field '%s': type mismatch - %s expected.";

    private long currentTableViewVersion = -1;
    protected TableOrView table = null;

    protected BaseRealmResults(TableOrView queryResult) {
        table = queryResult;
    }

    protected abstract void checkIsRealmValid();
    protected abstract TableOrView getTable();
    protected abstract Q getSubQuery();
    protected abstract O getObject(long sourceRowIndex);

    /**
     * Returns a typed {@link io.realm.RealmQuery}, which can be used to query for specific
     * objects of this type.
     *
     * @return A typed RealmQuery
     * @see io.realm.RealmQuery
     */
    public Q where() {
        checkIsRealmValid();
        return getSubQuery();
    }

    /**
     * Returns the element at the specified location in this list.
     *
     * @param location the index of the element to return.
     * @return the element at the specified index.
     * @throws IndexOutOfBoundsException if {@code location < 0 || location >= size()}
     */
    @Override
    public O get(int location) {
        O obj;
        checkIsRealmValid();
        TableOrView table = getTable();
        if (table instanceof TableView) {
            obj = getObject(((TableView) table).getSourceRowIndex(location));
        } else {
            obj = getObject(location);
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
    public O first() {
        return get(0);
    }

    /**
     * Get the last object from the list.
     * @return The last object.
     * @throws ArrayIndexOutOfBoundsException if RealmResults is empty.
     */
    public O last() {
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
    public Iterator<O> iterator() {
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
    public ListIterator<O> listIterator() {
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
    public ListIterator<O> listIterator(int location) {
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
        this.sort(fieldName, SORT_ORDER_ASCENDING);
    }

    /**
     * Sort existing {@link io.realm.RealmResults}.
     *
     * @param fieldName      The field name to sort by. Only fields of type boolean, short, int,
     *                       long, float, double, Date, and String are supported.
     * @param sortAscending  The direction to sort by; if true ascending, otherwise descending
     *                       You can use the constants SORT_ORDER_ASCENDING and SORT_ORDER_DESCENDING
     *                       for readability.
     * @throws java.lang.IllegalArgumentException if field name does not exist.
     */
    public void sort(String fieldName, boolean sortAscending) {
        if (fieldName == null) {
            throw new IllegalArgumentException("fieldName must be provided");
        }
        checkIsRealmValid();
        TableOrView table = getTable();

        if (table instanceof TableView) {
            long columnIndex = getColumnIndex(fieldName);
            TableView.Order TVOrder = sortAscending ? TableView.Order.ascending : TableView.Order.descending;
            ((TableView) table).sort(columnIndex, TVOrder);
        } else {
            throw new IllegalArgumentException("Only RealmResults can be sorted - please use allObject() to create a RealmResults.");
        }
    }

    /**
     * Sort existing {@link io.realm.RealmResults}.
     *
     * @param fieldNames an array of field names to sort by. Only fields of type boolean, short, int,
     *                       long, float, double, Date, and String are supported.
     * @param sortAscending The directions to sort by; if true ascending, otherwise descending
     *                       You can use the constants SORT_ORDER_ASCENDING and SORT_ORDER_DESCENDING
     *                       for readability.
     * @throws java.lang.IllegalArgumentException if a field name does not exist.
     */
    public void sort(String fieldNames[], boolean sortAscending[]) {
        if (fieldNames == null) {
            throw new IllegalArgumentException("fieldNames must be provided.");
        } else if (sortAscending == null) {
            throw new IllegalArgumentException("sortAscending must be provided.");
        }

        if (fieldNames.length == 1 && sortAscending.length == 1) {
            sort(fieldNames[0], sortAscending[0]);
        } else {
            checkIsRealmValid();
            TableOrView table = getTable();
            if (table instanceof TableView) {
                List<TableView.Order> TVOrder = new ArrayList<TableView.Order>();
                List<Long> columnIndices = new ArrayList<Long>();
                for (int i = 0; i < fieldNames.length; i++) {
                    String fieldName = fieldNames[i];
                    long columnIndex = getColumnIndex(fieldName);
                    columnIndices.add(columnIndex);
                }
                for (int i = 0; i < sortAscending.length; i++) {
                    TVOrder.add(sortAscending[i] ? TableView.Order.ascending : TableView.Order.descending);
                }
                ((TableView) table).sort(columnIndices, TVOrder);
            }
        }
    }

    /**
     * Sort existing {@link io.realm.RealmResults} using two fields.
     *
     * @param fieldName1 first field name.
     * @param sortAscending1 sort order for first field.
     * @param fieldName2 second field name.
     * @param sortAscending2 sort order for second field.
     * @throws java.lang.IllegalArgumentException if a field name does not exist.
     */
    public void sort(String fieldName1, boolean sortAscending1, String fieldName2, boolean sortAscending2) {
        sort(new String[] {fieldName1, fieldName2}, new boolean[] {sortAscending1, sortAscending2});
    }

    /**
     * Sort existing {@link io.realm.RealmResults} using three fields.
     *
     * @param fieldName1 first field name.
     * @param sortAscending1 sort order for first field.
     * @param fieldName2 second field name.
     * @param sortAscending2 sort order for second field.
     * @param fieldName3 third field name.
     * @param sortAscending3 sort order for third field.
     * @throws java.lang.IllegalArgumentException if a field name does not exist.
     */
    public void sort(String fieldName1, boolean sortAscending1, String fieldName2, boolean sortAscending2, String fieldName3, boolean sortAscending3) {
        sort(new String[] {fieldName1, fieldName2, fieldName3}, new boolean[] {sortAscending1, sortAscending2, sortAscending3});
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
     * @param fieldName   The field to look for a minimum on. Only int, float, and double
     *                    are supported.
     * @return            The minimum value.
     * @throws            java.lang.IllegalArgumentException if field is not int, float or double.
     */
    public Number min(String fieldName) {
        checkIsRealmValid();
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
        checkIsRealmValid();
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
        checkIsRealmValid();
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
        checkIsRealmValid();
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
        checkIsRealmValid();
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
        checkIsRealmValid();
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
     * @return           Always return null.
     */
    @Override
    public O remove(int index) {
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
    public boolean add(O element) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public void add(int index, O element) {
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
    private class RealmResultsIterator implements Iterator<O> {

        int pos = -1;

        RealmResultsIterator() {
            currentTableViewVersion = table.sync();
        }

        public boolean hasNext() {
            assertRealmIsStable();
            return pos + 1 < size();
        }

        public O next() {
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
    private class RealmResultsListIterator extends RealmResultsIterator implements ListIterator<O> {

        RealmResultsListIterator(int start) {
            if (start >= 0 && start <= size()) {
                pos = start - 1;
            } else {
                throw new IndexOutOfBoundsException("Starting location must be a valid index: [0, " + (size() - 1) + "]. Yours was " + start);
            }
        }

        @Override
        public void add(O object) {
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
        public O previous() {
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
        public void set(O object) {
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
