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
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;

import io.realm.exceptions.RealmException;
import io.realm.internal.TableOrView;
import io.realm.internal.TableQuery;
import io.realm.internal.TableView;
import io.realm.internal.log.RealmLog;
import rx.Observable;

/**
 * This class holds all the matches of a {@link io.realm.RealmQuery} for a given Realm. The objects are not copied from
 * the Realm to the RealmResults list, but are just referenced from the RealmResult instead. This saves memory and
 * increases speed.
 * <p>
 * RealmResults are live views, which means that if it is on an {@link android.os.Looper} thread, it will automatically
 * update its query results after a transaction has been committed. If on a non-looper thread, {@link Realm#refresh()}
 * must be called to update the results.
 * <p>
 * Updates to RealmObjects from a RealmResults list must be done from within a transaction and the modified objects are
 * persisted to the Realm file during the commit of the transaction.
 * <p>
 * A RealmResults object cannot be passed between different threads.
 * <p>
 * Notice that a RealmResults is never {@code null} not even in the case where it contains no objects. You should always
 * use the size() method to check if a RealmResults is empty or not.
 *
 * @param <E> The class of objects in this list.
 * @see RealmQuery#findAll()
 * @see Realm#allObjects(Class)
 * @see io.realm.Realm#beginTransaction()
 */
public final class RealmResults<E extends RealmObject> extends AbstractList<E> {

    BaseRealm realm;
    Class<E> classSpec;   // Return type
    String className;     // Class name used by DynamicRealmObjects
    private TableOrView table = null;

    private static final String TYPE_MISMATCH = "Field '%s': type mismatch - %s expected.";
    private long currentTableViewVersion = -1;

    private final TableQuery query;
    private final List<RealmChangeListener> listeners = new CopyOnWriteArrayList<RealmChangeListener>();
    private Future<Long> pendingQuery;
    private boolean isCompleted = false;

    static <E extends RealmObject> RealmResults<E> createFromTableQuery(BaseRealm realm, TableQuery query, Class<E> clazz) {
        return new RealmResults<E>(realm, query, clazz);
    }

    static <E extends RealmObject> RealmResults<E> createFromTableOrView(BaseRealm realm, TableOrView table, Class<E> clazz) {
        return new RealmResults<E>(realm, table, clazz);
    }

    static RealmResults<DynamicRealmObject> createFromDynamicClass(BaseRealm realm, TableQuery query, String className) {
        return new RealmResults<DynamicRealmObject>(realm, query, className);
    }

    static RealmResults<DynamicRealmObject> createFromDynamicTableOrView(BaseRealm realm, TableOrView table, String className) {
        return new RealmResults<DynamicRealmObject>(realm, table, className);
    }

    private RealmResults(BaseRealm realm, TableQuery query, Class<E> clazz) {
        this.realm = realm;
        this.classSpec = clazz;
        this.query = query;
    }

    private RealmResults(BaseRealm realm, TableQuery query, String className) {
        this.realm = realm;
        this.query = query;
        this.className = className;
    }

    private RealmResults(BaseRealm realm, TableOrView table, Class<E> classSpec) {
        this.realm = realm;
        this.classSpec = classSpec;
        this.table = table;

        this.pendingQuery = null;
        this.query = null;
        this.currentTableViewVersion = table.sync();
    }

    private RealmResults(BaseRealm realm, String className) {
        this.realm = realm;
        this.className = className;

        pendingQuery = null;
        query = null;
    }

    private RealmResults(BaseRealm realm, TableOrView table, String className) {
        this(realm, className);
        this.table = table;
    }

    TableOrView getTable() {
        if (table == null) {
            return realm.schema.getTable(classSpec);
        } else {
            return table;
        }
    }

    /**
     * Checks if {@link io.realm.RealmResults} is still valid to use i.e. the {@link io.realm.Realm} instance hasn't
     * been closed.
     *
     * @return {@code true} if still valid to use, {@code false} otherwise.
     */
    public boolean isValid() {
        return realm != null && !realm.isClosed();
    }

    /**
     * Returns a typed {@link io.realm.RealmQuery}, which can be used to query for specific objects of this type.
     *
     * @return a typed RealmQuery.
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
     * @throws IndexOutOfBoundsException if {@code location < 0 || location >= size()}.
     */
    @Override
    public E get(int location) {
        E obj;
        realm.checkIfValid();
        TableOrView table = getTable();
        if (table instanceof TableView) {
            obj = realm.get(classSpec, className, ((TableView) table).getSourceRowIndex(location));
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
     * Gets the first object from the list.
     *
     * @return the first object.
     * @throws ArrayIndexOutOfBoundsException if RealmResults is empty.
     */
    public E first() {
        return get(0);
    }

    /**
     * Gets the last object from the list.
     *
     * @return the last object.
     * @throws ArrayIndexOutOfBoundsException if RealmResults is empty.
     */
    public E last() {
        return get(size()-1);
    }

    /**
     * Returns an iterator for the results of a query. Any change to Realm while iterating will cause this iterator to
     * throw a {@link java.util.ConcurrentModificationException} if accessed.
     *
     * @return an iterator on the elements of this list.
     * @see Iterator
     */
    @Override
    public Iterator<E> iterator() {
        if (!isLoaded()) {
            // Collections.emptyIterator(); is only available since API 19
            return Collections.<E>emptyList().iterator();
        }
        return new RealmResultsIterator();
    }

    /**
     * Returns a list iterator for the results of a query. Any change to Realm while iterating will cause the iterator
     * to throw a {@link java.util.ConcurrentModificationException} if accessed.
     *
     * @return a ListIterator on the elements of this list.
     * @see ListIterator
     */
    @Override
    public ListIterator<E> listIterator() {
        if (!isLoaded()) {
            // Collections.emptyListIterator() is only available since API 19
            return Collections.<E>emptyList().listIterator();
        }
        return new RealmResultsListIterator(0);
    }

    /**
     * Returns a list iterator on the results of a query. Any change to Realm while iterating will cause the iterator to
     * throw a {@link java.util.ConcurrentModificationException} if accessed.
     *
     * @param location the index at which to start the iteration.
     * @return a ListIterator on the elements of this list.
     * @throws IndexOutOfBoundsException if {@code location < 0 || location > size()}.
     * @see ListIterator
     */
    @Override
    public ListIterator<E> listIterator(int location) {
        if (!isLoaded()) {
            // Collections.emptyListIterator() is only available since API 19
            return Collections.<E>emptyList().listIterator(location);
        }
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
     * Sorts (ascending) an existing {@link io.realm.RealmResults}.
     *
     * @param fieldName the field name to sort by. Only fields of type boolean, short, int, long, float, double, Date,
     *                  and String are supported.
     * @throws java.lang.IllegalArgumentException if field name does not exist.
     */
    public void sort(String fieldName) {
        this.sort(fieldName, Sort.ASCENDING);
    }

    /**
     * Sorts existing {@link io.realm.RealmResults}.
     *
     * @param fieldName the field name to sort by. Only fields of type boolean, short, int, long, float, double, Date,
     *                  and String are supported.
     * @param sortOrder the direction to sort by.
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
     * Sorts existing {@link io.realm.RealmResults}.
     *
     * @param fieldNames an array of field names to sort by. Only fields of type boolean, short, int, long, float,
     *                   double, Date, and String are supported.
     * @param sortOrders the directions to sort by.
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
     * Sorts existing {@link io.realm.RealmResults} using two fields.
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
     * Sorts existing {@link io.realm.RealmResults} using three fields.
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
        if (!isLoaded()) {
            return 0;
        } else {
            return ((Long)getTable().size()).intValue();
        }
    }

    /**
     * Finds the minimum value of a field.
     *
     * @param fieldName the field to look for a minimum on. Only number fields are supported.
     * @return if no objects exist or they all have {@code null} as the value for the given field, {@code null} will be
     * returned. Otherwise the minimum value is returned. When determining the minimum value, objects with {@code null}
     * values are ignored.
     * @throws java.lang.IllegalArgumentException if the field is not a number type.
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
     * Finds the minimum date.
     *
     * @param fieldName the field to look for the minimum date. If fieldName is not of Date type, an exception is
     *                  thrown.
     * @return if no objects exist or they all have {@code null} as the value for the given date field, {@code null}
     * will be returned. Otherwise the minimum date is returned. When determining the minimum date, objects with
     * {@code null} values are ignored.
     * @throws java.lang.IllegalArgumentException if fieldName is not a Date field.
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
     * Finds the maximum value of a field.
     *
     * @param fieldName the field to look for a maximum on. Only number fields are supported.
     * @return if no objects exist or they all have {@code null} as the value for the given field, {@code null} will be
     * returned. Otherwise the maximum value is returned. When determining the maximum value, objects with {@code null}
     * values are ignored.
     * @throws java.lang.IllegalArgumentException if the field is not a number type.
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
     * Finds the maximum date.
     *
     * @param fieldName the field to look for the maximum date. If fieldName is not of Date type, an exception is
     *                  thrown.
     * @return if no objects exist or they all have {@code null} as the value for the given date field, {@code null}
     * will be returned. Otherwise the maximum date is returned. When determining the maximum date, objects with
     * {@code null} values are ignored.
     * @throws java.lang.IllegalArgumentException if fieldName is not a Date field.
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
     * Calculates the sum of a given field.
     *
     * @param fieldName the field to sum. Only number fields are supported.
     * @return the sum. If no objects exist or they all have {@code null} as the value for the given field, {@code 0}
     * will be returned. When computing the sum, objects with {@code null} values are ignored.
     * @throws java.lang.IllegalArgumentException if the field is not a number type.
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
     * @param fieldName the field to calculate average on. Only number fields are supported.
     * @return the average for the given field amongst objects in query results. This will be of type double for all
     * types of number fields. If no objects exist or they all have {@code null} as the value for the given field,
     * {@code 0} will be returned. When computing the average, objects with {@code null} values are ignored.
     * @throws java.lang.IllegalArgumentException if the field is not a number type.
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
     * @param index the array index identifying the object to be removed.
     * @return always return {@code null}.
     * @throws IllegalStateException if the corresponding Realm is closed or in an incorrect thread.
     */
    @Override
    public E remove(int index) {
        realm.checkIfValid();
        TableOrView table = getTable();
        table.remove(index);
        return null; // Returning the object doesn't make sense, since it could no longer access any data.
    }

    /**
     * Removes and returns the last object in the list. This also deletes the object from the underlying Realm.
     *
     * Using this method while iterating the list can result in a undefined behavior. Use
     * {@link io.realm.RealmResults.RealmResultsListIterator#removeLast()} instead.
     *
     * @throws IllegalStateException if the corresponding Realm is closed or in an incorrect thread.
     */
    public void removeLast() {
        realm.checkIfValid();
        TableOrView table = getTable();
        table.removeLast();
    }

    /**
     * Removes all objects from the list. This also deletes the objects from the underlying Realm.
     *
     * @throws IllegalStateException if the corresponding Realm is closed or in an incorrect thread.
     */
    public void clear() {
        realm.checkIfValid();
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
//     * @param index the array index of the object to be replaced.
//     * @param element an object.
//     */
//    public void replace(int index, E element) {
//        throw new NoSuchMethodError();
//    }



    // Custom RealmResults iterator. It ensures that we only iterate on a Realm that hasn't changed.
    private class RealmResultsIterator implements Iterator<E> {
        long tableViewVersion = 0;
        int pos = -1;

        RealmResultsIterator() {
            tableViewVersion = table.sync();
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

        protected void assertRealmIsStable() {
            long version = table.sync();
            if (tableViewVersion > -1 && version != tableViewVersion) {
                throw new ConcurrentModificationException("No outside changes to a Realm is allowed while iterating a RealmResults. Use iterators methods instead.");
            }
            tableViewVersion = version;
        }
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

    /**
     * Swaps the table_view pointer used by this RealmResults mostly called when updating the RealmResults from a worker
     * thread.
     *
     * @param handoverTableViewPointer handover pointer to the new table_view.
     */
    void swapTableViewPointer(long handoverTableViewPointer) {
        table = query.importHandoverTableView(handoverTableViewPointer, realm.sharedGroupManager.getNativePointer());
        isCompleted = true;
    }

    /**
     * Sets the Future instance returned by the worker thread, we need this instance to force {@link #load()} an async
     * query, we use it to determine if the current RealmResults is a sync or async one.
     *
     * @param pendingQuery pending query.
     */
    void setPendingQuery(Future<Long> pendingQuery) {
        this.pendingQuery = pendingQuery;
        if (isLoaded()) {
            // the query completed before RealmQuery
            // had a chance to call setPendingQuery to register the pendingQuery (used btw
            // to determine isLoaded behaviour)
            onCompleted();
        } // else, it will be handled by the Realm#handler
    }

    /**
     * Returns {@code true} if the results are not yet loaded, {@code false} if they are still loading. Synchronous
     * query methods like findAll() will always return {@code true}, while asynchronous query methods like
     * findAllAsync() will return {@code false} until the results are available.
     * This will return {@code true} if called for a standalone object (created outside of Realm).
     *
     * @return {@code true} if the query has completed and the data is available {@code false} if the query is still
     * running.
     */
    public boolean isLoaded() {
        realm.checkIfValid();
        return pendingQuery == null || isCompleted;
    }

    /**
     * Makes an asynchronous query blocking. This will also trigger any registered listeners.
     * This will return {@code true} for standalone object (created outside of Realm). {@link RealmChangeListener} when
     * the query completes.
     *
     * @return {@code true} if it successfully completed the query, {@code false} otherwise.
     */
    public boolean load() {
        if (isLoaded()) {
            return true;
        } else {
        // doesn't guarantee to import correctly the result (because the user may have advanced)
        // in this case the Realm#handler will be responsible of retrying
            return onCompleted();
        }
    }

    /**
     * Called to import the handover table_view pointer & notify listeners.
     * This should be invoked once the {@link #pendingQuery} finish, unless the user force {@link #load()}.
     *
     * @return {@code true} if it successfully completed the query, {@code false} otherwise.
     */
    private boolean onCompleted() {
        try {
            long tvHandover = pendingQuery.get();// make the query blocking
            // this may fail with BadVersionException if the caller and/or the worker thread
            // are not in sync. COMPLETED_ASYNC_REALM_RESULTS will be fired by the worker thread
            // this should handle more complex use cases like retry, ignore etc
            table = query.importHandoverTableView(tvHandover, realm.sharedGroupManager.getNativePointer());
            isCompleted = true;
            notifyChangeListeners();
        } catch (Exception e) {
            RealmLog.d(e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Adds a change listener to this RealmResults.
     *
     * @param listener the change listener to be notified.
     */
    public void addChangeListener(RealmChangeListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener should not be null");
        }
        realm.checkIfValid();
        if (realm.handler == null) {
            throw new IllegalStateException("You can't register a listener from a non-Looper thread ");
        }
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Removes a previously registered listener.
     *
     * @param listener the instance to be removed.
     */
    public void removeChangeListener(RealmChangeListener listener) {
        if (listener == null)
            throw new IllegalArgumentException("Listener should not be null");

        realm.checkIfValid();
        listeners.remove(listener);
    }

    /**
     * Removes all registered listeners.
     */
    public void removeChangeListeners() {
        realm.checkIfValid();
        listeners.clear();
    }

    /**
     * Returns an Rx Observable that monitors changes to this RealmResults. It will emit the current RealmResults when
     * subscribed to.
     *
     * @return RxJava Observable
     * @throws UnsupportedOperationException if the required RxJava framework is not on the classpath.
     * @see <a href="https://realm.io/docs/java/latest/#rxjava">RxJava and Realm</a>
     */
    @SuppressWarnings("unchecked")
    public Observable<RealmResults<E>> asObservable() {
        if (realm instanceof Realm) {
            return realm.configuration.getRxFactory().from((Realm) realm, this);
        } else if (realm instanceof DynamicRealm) {
            DynamicRealm dynamicRealm = (DynamicRealm) realm;
            RealmResults<DynamicRealmObject> dynamicResults = (RealmResults<DynamicRealmObject>) this;
            @SuppressWarnings("UnnecessaryLocalVariable")
            Observable results = realm.configuration.getRxFactory().from(dynamicRealm, dynamicResults);
            return results;
        } else {
            throw new UnsupportedOperationException(realm.getClass() + " not supported");
        }
    }

    /**
     * Notifies all registered listeners.
     */
    void notifyChangeListeners() {
        if (listeners != null && !listeners.isEmpty()) {
            // table might be null (if the async query didn't complete
            // but we have already registered listeners for it)
            if (pendingQuery != null && !isCompleted) return;

            //FIXME: still waiting for Core to provide a fix
            //       for crash when calling _sync_if_needed on a cleared View.
            //       https://github.com/realm/realm-core/pull/1390
            long version = table.sync();
            if (currentTableViewVersion != version) {
                currentTableViewVersion = version;
                for (RealmChangeListener listener : listeners) {
                    listener.onChange();
                }
            }
        }
    }
}
