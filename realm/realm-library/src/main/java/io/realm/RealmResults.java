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


import android.app.IntentService;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;

import io.realm.internal.InvalidRow;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.Table;
import io.realm.internal.TableOrView;
import io.realm.internal.TableQuery;
import io.realm.internal.TableView;
import io.realm.internal.async.BadVersionException;
import io.realm.internal.log.RealmLog;
import rx.Observable;

/**
 * This class holds all the matches of a {@link io.realm.RealmQuery} for a given Realm. The objects are not copied from
 * the Realm to the RealmResults list, but are just referenced from the RealmResult instead. This saves memory and
 * increases speed.
 * <p>
 * RealmResults are live views, which means that if it is on an {@link android.os.Looper} thread, it will automatically
 * update its query results after a transaction has been committed. If on a non-looper thread, {@link Realm#waitForChange()}
 * must be called to update the results.
 * <p>
 * Updates to RealmObjects from a RealmResults list must be done from within a transaction and the modified objects are
 * persisted to the Realm file during the commit of the transaction.
 * <p>
 * A RealmResults object cannot be passed between different threads.
 * <p>
 * Notice that a RealmResults is never {@code null} not even in the case where it contains no objects. You should always
 * use the {@link RealmResults#size()} method to check if a RealmResults is empty or not.
 * <p>
 * If a RealmResults is built on RealmList through {@link RealmList#where()}, it will become empty when the source
 * RealmList gets deleted.
 * <p>
 * {@link RealmResults} can contain more elements than {@code Integer.MAX_VALUE}.
 * In that case, you can access only first {@code Integer.MAX_VALUE} elements in it.
 *
 * @param <E> The class of objects in this list.
 * @see RealmQuery#findAll()
 * @see io.realm.Realm#executeTransaction(Realm.Transaction)
 */
public final class RealmResults<E extends RealmModel> extends AbstractList<E> implements OrderedRealmCollection<E> {

    private final static String NOT_SUPPORTED_MESSAGE = "This method is not supported by RealmResults.";

    final BaseRealm realm;
    Class<E> classSpec;   // Return type
    String className;     // Class name used by DynamicRealmObjects
    private TableOrView table = null;

    private static final String TYPE_MISMATCH = "Field '%s': type mismatch - %s expected.";
    private static final long TABLE_VIEW_VERSION_NONE = -1;

    private long currentTableViewVersion = TABLE_VIEW_VERSION_NONE;
    private final TableQuery query;
    private final List<RealmChangeListener<RealmResults<E>>> listeners = new CopyOnWriteArrayList<RealmChangeListener<RealmResults<E>>>();
    private Future<Long> pendingQuery;
    private boolean asyncQueryCompleted = false;
    // Keep track of changes to the RealmResult. Is updated after a call to `syncIfNeeded()`. Calling notifyListeners will
    // clear it.
    private boolean viewUpdated = false;


    static <E extends RealmModel> RealmResults<E> createFromTableQuery(BaseRealm realm, TableQuery query, Class<E> clazz) {
        return new RealmResults<E>(realm, query, clazz);
    }

    static <E extends RealmModel> RealmResults<E> createFromTableOrView(BaseRealm realm, TableOrView table, Class<E> clazz) {
        RealmResults<E> realmResults = new RealmResults<E>(realm, table, clazz);
        realm.handlerController.addToRealmResults(realmResults);
        return realmResults;
    }

    static RealmResults<DynamicRealmObject> createFromDynamicClass(BaseRealm realm, TableQuery query, String className) {
        return new RealmResults<DynamicRealmObject>(realm, query, className);
    }

    static RealmResults<DynamicRealmObject> createFromDynamicTableOrView(BaseRealm realm, TableOrView table, String className) {
        RealmResults<DynamicRealmObject> realmResults = new RealmResults<DynamicRealmObject>(realm, table, className);
        realm.handlerController.addToRealmResults(realmResults);
        return realmResults;
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
        this.currentTableViewVersion = table.syncIfNeeded();
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
        this.currentTableViewVersion = table.syncIfNeeded();
    }

    TableOrView getTable() {
        if (table == null) {
            return realm.schema.getTable(classSpec);
        } else {
            return table;
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isValid() {
        return !realm.isClosed();
    }

    /**
     * A {@link RealmResults} is always a managed collection.
     *
     * @return {@code true}.
     * @see RealmCollection#isManaged()
     */
    public boolean isManaged() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RealmQuery<E> where() {
        realm.checkIfValid();

        return RealmQuery.createQueryFromResult(this);
    }

    /**
     * Searches this {@link RealmResults} for the specified object.
     *
     * @param object the object to search for.
     * @return {@code true} if {@code object} is an element of this {@code RealmResults},
     *         {@code false} otherwise
     */
    @Override
    public boolean contains(Object object) {
        boolean contains = false;
        if (isLoaded() && object instanceof RealmObjectProxy) {
            RealmObjectProxy proxy = (RealmObjectProxy) object;
            if (realm.getPath().equals(proxy.realmGet$proxyState().getRealm$realm().getPath()) && proxy.realmGet$proxyState().getRow$realm() != InvalidRow.INSTANCE) {
                contains = (table.sourceRowIndex(proxy.realmGet$proxyState().getRow$realm().getIndex()) != TableOrView.NO_MATCH);
            }
        }
        return contains;
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
     * {@inheritDoc}
     */
    @Override
    public E first() {
        if (size() > 0) {
            return get(0);
        } else {
            throw new IndexOutOfBoundsException("No results were found.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E last() {
        int size = size();
        if (size > 0) {
            return get(size - 1);
        } else {
            throw new IndexOutOfBoundsException("No results were found.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteFromRealm(int location) {
        realm.checkIfValid();
        TableOrView table = getTable();
        table.remove(location);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteAllFromRealm() {
        realm.checkIfValid();
        if (size() > 0) {
            TableOrView table = getTable();
            table.clear();
            return true;
        } else {
            return false;
        }
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
    private long getColumnIndexForSort(String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) {
            throw new IllegalArgumentException("Non-empty field name required.");
        }
        if (fieldName.contains(".")) {
            throw new IllegalArgumentException("Sorting using child object fields is not supported: " + fieldName);
        }
        long columnIndex = table.getColumnIndex(fieldName);
        if (columnIndex < 0) {
            throw new IllegalArgumentException(String.format("Field '%s' does not exist.", fieldName));
        }
        return columnIndex;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RealmResults<E> sort(String fieldName) {
        return this.sort(fieldName, Sort.ASCENDING);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RealmResults<E> sort(String fieldName, Sort sortOrder) {
        return where().findAllSorted(fieldName, sortOrder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RealmResults<E> sort(String fieldNames[], Sort sortOrders[]) {
        return where().findAllSorted(fieldNames, sortOrders);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RealmResults<E> sort(String fieldName1, Sort sortOrder1, String fieldName2, Sort sortOrder2) {
        return sort(new String[]{fieldName1, fieldName2}, new Sort[]{sortOrder1, sortOrder2});
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
            long size = getTable().size();
            return (size > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) size;
        }
    }

    /**
     * {@inheritDoc}
     */
    public Number min(String fieldName) {
        realm.checkIfValid();
        long columnIndex = getColumnIndexForSort(fieldName);
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
     * {@inheritDoc}
     */
    public Date minDate(String fieldName) {
        realm.checkIfValid();
        long columnIndex = getColumnIndexForSort(fieldName);
        if (table.getColumnType(columnIndex) == RealmFieldType.DATE) {
            return table.minimumDate(columnIndex);
        }
        else {
            throw new IllegalArgumentException(String.format(TYPE_MISMATCH, fieldName, "Date"));
        }
    }

    /**
     * {@inheritDoc}
     */
    public Number max(String fieldName) {
        realm.checkIfValid();
        long columnIndex = getColumnIndexForSort(fieldName);
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
        long columnIndex = getColumnIndexForSort(fieldName);
        if (table.getColumnType(columnIndex) == RealmFieldType.DATE) {
            return table.maximumDate(columnIndex);
        }
        else {
            throw new IllegalArgumentException(String.format(TYPE_MISMATCH, fieldName, "Date"));
        }
    }


    /**
     * {@inheritDoc}
     */
    public Number sum(String fieldName) {
        realm.checkIfValid();
        long columnIndex = getColumnIndexForSort(fieldName);
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
     * {@inheritDoc}
     */
    public double average(String fieldName) {
        realm.checkIfValid();
        long columnIndex = getColumnIndexForSort(fieldName);
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

    /**
     * Returns a distinct set of objects of a specific class. If the result is sorted, the first
     * object will be returned in case of multiple occurrences, otherwise it is undefined which
     * object is returned.
     *
     * @param fieldName the field name.
     * @return a non-null {@link RealmResults} containing the distinct objects.
     * @throws IllegalArgumentException if a field is null, does not exist, is an unsupported type,
     * is not indexed, or points to linked fields.
     */
    public RealmResults<E> distinct(String fieldName) {
        realm.checkIfValid();
        long columnIndex = RealmQuery.getAndValidateDistinctColumnIndex(fieldName, this.table.getTable());

        TableOrView tableOrView = getTable();
        if (tableOrView instanceof Table) {
            this.table = ((Table) tableOrView).getDistinctView(columnIndex);
        } else {
            ((TableView) tableOrView).distinct(columnIndex);
        }
        return this;
    }

    /**
     * Asynchronously returns a distinct set of objects of a specific class. If the result is
     * sorted, the first object will be returned in case of multiple occurrences, otherwise it is
     * undefined which object is returned.
     *
     * @param fieldName the field name.
     * @return immediately a {@link RealmResults}. Users need to register a listener
     * {@link io.realm.RealmResults#addChangeListener(RealmChangeListener)} to be notified when the
     * query completes.
     * @throws IllegalArgumentException if a field is null, does not exist, is an unsupported type,
     * is not indexed, or points to linked fields.
     */
    public RealmResults<E> distinctAsync(String fieldName) {
        return where().distinctAsync(fieldName);
    }

    /**
     * Returns a distinct set of objects from a specific class. When multiple distinct fields are
     * given, all unique combinations of values in the fields will be returned. In case of multiple
     * matches, it is undefined which object is returned. Unless the result is sorted, then the
     * first object will be returned.
     *
     * @param firstFieldName first field name to use when finding distinct objects.
     * @param remainingFieldNames remaining field names when determining all unique combinations of field values.
     * @return a non-null {@link RealmResults} containing the distinct objects.
     * @throws IllegalArgumentException if field names is empty or {@code null}, does not exist,
     * is an unsupported type, or points to a linked field.
     */
    public RealmResults<E> distinct(String firstFieldName, String... remainingFieldNames) {
        return where().distinct(firstFieldName, remainingFieldNames);
    }

    // Deleting

    /**
     * Not supported by RealmResults.
     *
     * @throws UnsupportedOperationException
     */
    @Deprecated
    @Override
    public E remove(int index) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_MESSAGE);
    }

    /**
     * Not supported by RealmResults.
     *
     * @throws UnsupportedOperationException
     */
    @Deprecated
    @Override
    public boolean remove(Object object) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_MESSAGE);
    }

    /**
     * Not supported by RealmResults.
     *
     * @throws UnsupportedOperationException
     */
    @Deprecated
    @Override
    public boolean removeAll(Collection<?> collection) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_MESSAGE);
    }

    /**
     * Not supported by RealmResults.
     *
     * @throws UnsupportedOperationException
     */
    @Deprecated
    @Override
    public E set(int location, E object) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_MESSAGE);
    }



    /**
     * Not supported by RealmResults.
     *
     * @throws UnsupportedOperationException
     */
    @Deprecated
    @Override
    public boolean retainAll(Collection<?> collection) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_MESSAGE);
    }

    /**
     * Removes the last object in the list. This also deletes the object from the underlying Realm.
     *
     * @throws IllegalStateException if the corresponding Realm is closed or in an incorrect thread.
     */
    @Override
    public boolean deleteLastFromRealm() {
        realm.checkIfValid();
        if (size() > 0) {
            TableOrView table = getTable();
            table.removeLast();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Syncs this RealmResults, so it is up to date after `advance_read` has been called.
     * Not doing so can leave detached accessors in the table view.
     *
     * By design, we should only call this on looper events.
     *
     * NOTE: Calling this is a prerequisite to calling {@link #notifyChangeListeners(boolean)}.
     */
    void syncIfNeeded() {
        long newVersion = table.syncIfNeeded();
        viewUpdated = newVersion != currentTableViewVersion;
        currentTableViewVersion = newVersion;
    }

    /**
     * Removes the first object in the list. This also deletes the object from the underlying Realm.
     *
     * @throws IllegalStateException if the corresponding Realm is closed or in an incorrect thread.
     */
    @Override
    public boolean deleteFirstFromRealm() {
        if (size() > 0) {
            TableOrView table = getTable();
            table.removeFirst();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Not supported by RealmResults.
     *
     * @throws UnsupportedOperationException always.
     */
    @Override
    @Deprecated
    public void clear() {
        throw new UnsupportedOperationException(NOT_SUPPORTED_MESSAGE);
    }

    /**
     * Not supported by RealmResults.
     *
     * @throws UnsupportedOperationException always.
     */
    @Override
    @Deprecated
    public boolean add(E element) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_MESSAGE);
    }

    /**
     * Not supported by RealmResults.
     *
     * @throws UnsupportedOperationException always.
     */
    @Override
    @Deprecated
    public void add(int index, E element) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_MESSAGE);
    }

    /**
     * Not supported by RealmResults.
     *
     * @throws UnsupportedOperationException always.
     */
    @Override
    @Deprecated
    public boolean addAll(int location, Collection<? extends E> collection) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_MESSAGE);
    }

    /**
     * Not supported by RealmResults.
     *
     * @throws UnsupportedOperationException always.
     */
    @Deprecated
    @Override
    public boolean addAll(Collection<? extends E> collection) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_MESSAGE);
    }

    // Custom RealmResults iterator. It ensures that we only iterate on a Realm that hasn't changed.
    private class RealmResultsIterator implements Iterator<E> {
        long tableViewVersion = 0;
        int pos = -1;

        RealmResultsIterator() {
            tableViewVersion = currentTableViewVersion;
        }

        /**
         * {@inheritDoc}
         */
        public boolean hasNext() {
            return pos + 1 < size();
        }

        /**
         * {@inheritDoc}
         */
        public E next() {
            realm.checkIfValid();
            checkRealmIsStable();
            pos++;
            if (pos >= size()) {
                throw new NoSuchElementException("Cannot access index " + pos + " when size is " + size() +  ". Remember to check hasNext() before using next().");
            }
            return get(pos);
        }

        /**
         * Not supported by RealmResults iterators.
         *
         * @throws UnsupportedOperationException
         */
        @Deprecated
        public void remove() {
            throw new UnsupportedOperationException("remove() is not supported by RealmResults iterators.");
        }

        protected void checkRealmIsStable() {
            long version = table.getVersion();
            // Any change within a write transaction will immediately update the table version. This means that we
            // cannot depend on the tableVersion heuristic in that case.
            // You could argue that in that case it is not really a "ConcurrentModification", but this interpretation
            // is still more lax than what the standard Java Collection API gives.
            // TODO: Try to come up with a better scheme
            if (!realm.isInTransaction() && tableViewVersion > -1 && version != tableViewVersion) {
                throw new ConcurrentModificationException("No outside changes to a Realm is allowed while iterating a RealmResults. Don't call Realm.refresh() while iterating.");
            }
            tableViewVersion = version;
        }
    }

    // Custom RealmResults list iterator.
    private class RealmResultsListIterator extends RealmResultsIterator implements ListIterator<E> {

        RealmResultsListIterator(int start) {
            if (start >= 0 && start <= size()) {
                pos = start - 1;
            } else {
                throw new IndexOutOfBoundsException("Starting location must be a valid index: [0, " + (size() - 1) + "]. Yours was " + start);
            }
        }

        /**
         * Unsupported by RealmResults iterators.
         *
         * @throws UnsupportedOperationException
         */
        @Override
        @Deprecated
        public void add(E object) {
            throw new UnsupportedOperationException("Adding an element is not supported. Use Realm.createObject() instead.");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasPrevious() {
            return pos >= 0;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int nextIndex() {
            return pos + 1;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public E previous() {
            realm.checkIfValid();
            checkRealmIsStable();
            try {
                E obj = get(pos);
                pos--;
                return obj;
            } catch (IndexOutOfBoundsException e) {
                throw new NoSuchElementException("Cannot access index less than zero. This was " + pos + ". Remember to check hasPrevious() before using previous().");
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int previousIndex() {
            return pos;
        }

        /**
         * Unsupported by RealmResults iterators.
         *
         * @throws UnsupportedOperationException
         */
        @Override
        @Deprecated
        public void set(E object) {
            throw new UnsupportedOperationException("Replacing and element is not supported.");
        }
    }

    /**
     * Swaps the table_view pointer used by this RealmResults mostly called when updating the RealmResults from a worker
     * thread.
     *
     * @param handoverTableViewPointer handover pointer to the new table_view.
     * @throws IllegalStateException if caller and worker are not at the same version.
     */
    void swapTableViewPointer(long handoverTableViewPointer) {
        try {
            table = query.importHandoverTableView(handoverTableViewPointer, realm.sharedRealm);
            asyncQueryCompleted = true;
        } catch (BadVersionException e) {
            throw new IllegalStateException("Caller and Worker Realm should have been at the same version");
        }
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
            // had a chance to call setPendingQuery to register the pendingQuery (used
            // to determine isLoaded behaviour)
            onAsyncQueryCompleted();
        } // else, it will be handled by the {@link BaseRealm#handlerController#handleMessage}
    }

    /**
     * Returns {@code false} if the results are not yet loaded, {@code true} if they are loaded. Synchronous
     * query methods like findAll() will always return {@code true}, while asynchronous query methods like
     * findAllAsync() will return {@code false} until the results are available.
     *
     * @return {@code true} if the query has completed and the data is available, {@code false} if the query is still
     * running.
     */
    public boolean isLoaded() {
        realm.checkIfValid();
        return pendingQuery == null || asyncQueryCompleted;
    }

    /**
     * Makes an asynchronous query blocking. This will also trigger any registered {@link RealmChangeListener} when
     * the query completes.
     *
     * @return {@code true} if it successfully completed the query, {@code false} otherwise. {@code true} will always
     *         be returned for unmanaged objects.
     */
    public boolean load() {
        //noinspection SimplifiableIfStatement
        if (isLoaded()) {
            return true;
        } else {
            // doesn't guarantee to correctly import the result (because the user may have advanced)
            // in this case the Realm#handler will be responsible of retrying
            return onAsyncQueryCompleted();
        }
    }

    /**
     * Called to import the handover table_view pointer & notify listeners.
     * This should be invoked once the {@link #pendingQuery} finish, unless the user force {@link #load()}.
     *
     * @return {@code true} if it successfully completed the query, {@code false} otherwise.
     */
    private boolean onAsyncQueryCompleted() {
        try {
            long tvHandover = pendingQuery.get();// make the query blocking
            // this may fail with BadVersionException if the caller and/or the worker thread
            // are not in sync. COMPLETED_ASYNC_REALM_RESULTS will be fired by the worker thread
            // this should handle more complex use cases like retry, ignore etc
            table = query.importHandoverTableView(tvHandover, realm.sharedRealm);
            asyncQueryCompleted = true;
            notifyChangeListeners(true);
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
     * @throws IllegalArgumentException if the change listener is {@code null}.
     * @throws IllegalStateException if you try to add a listener from a non-Looper or {@link IntentService} thread.
     */
    public void addChangeListener(RealmChangeListener<RealmResults<E>> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener should not be null");
        }
        realm.checkIfValid();
        if (!realm.handlerController.isAutoRefreshEnabled()) {
            throw new IllegalStateException("You can't register a listener from a non-Looper thread or IntentService thread. ");
        }
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Removes a previously registered listener.
     *
     * @param listener the instance to be removed.
     * @throws IllegalArgumentException if the change listener is {@code null}.
     * @throws IllegalStateException if you try to remove a listener from a non-Looper Thread.
     */
    public void removeChangeListener(RealmChangeListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener should not be null");
        }
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
     * subscribed to. RealmResults will continually be emitted as the RealmResults are updated -
     * {@code onComplete} will never be called.
     *
     * If you would like the {@code asObservable()} to stop emitting items you can instruct RxJava to
     * only emit only the first item by using the {@code first()} operator:
     *
     *<pre>
     * {@code
     * realm.where(Foo.class).findAllAsync().asObservable()
     *      .filter(results -> results.isLoaded())
     *      .first()
     *      .subscribe( ... ) // You only get the results once
     * }
     * </pre>
     *
     * <p>Note that when the {@link Realm} is accessed from threads other than where it was created,
     * {@link IllegalStateException} will be thrown. Care should be taken when using different schedulers
     * with {@code subscribeOn()} and {@code observeOn()}. Consider using {@code Realm.where().find*Async()}
     * instead.
     *
     * @return RxJava Observable that only calls {@code onNext}. It will never call {@code onComplete} or {@code OnError}.
     * @throws UnsupportedOperationException if the required RxJava framework is not on the classpath or the
     * corresponding Realm instance doesn't support RxJava.
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
            throw new UnsupportedOperationException(realm.getClass() + " does not support RxJava.");
        }
    }

    /**
     * Notifies all registered listeners.
     *
     * NOTE: Remember to call `syncIfNeeded` before calling this method.
     */
    void notifyChangeListeners(boolean forceNotify) {
        if (!listeners.isEmpty()) {
            // table might be null (if the async query didn't complete
            // but we have already registered listeners for it)
            if (pendingQuery != null && !asyncQueryCompleted) return;
            if (!viewUpdated && !forceNotify) return;
            viewUpdated = false;
            for (RealmChangeListener listener : listeners) {
                listener.onChange(this);
            }
        }
    }
}
