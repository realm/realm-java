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
import android.os.Looper;

import java.util.AbstractList;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Iterator;
import java.util.ListIterator;

import io.realm.internal.InvalidRow;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.SortDescriptor;
import io.realm.internal.Table;
import io.realm.internal.Collection;
import io.realm.internal.UncheckedRow;
import rx.Observable;

/**
 * This class holds all the matches of a {@link RealmQuery} for a given Realm. The objects are not copied from
 * the Realm to the RealmResults list, but are just referenced from the RealmResult instead. This saves memory and
 * increases speed.
 * <p>
 * RealmResults are live views, which means that if it is on an {@link Looper} thread, it will automatically
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
 * @see Realm#executeTransaction(Realm.Transaction)
 */
public class RealmResults<E extends RealmModel> extends AbstractList<E> implements OrderedRealmCollection<E> {

    private final static String NOT_SUPPORTED_MESSAGE = "This method is not supported by RealmResults.";

    final BaseRealm realm;
    Class<E> classSpec;   // Return type
    String className;     // Class name used by DynamicRealmObjects

    private final Collection collection;

    RealmResults(BaseRealm realm, Collection collection, Class<E> clazz) {
        this.realm = realm;
        this.classSpec = clazz;
        this.collection = collection;
    }

    RealmResults(BaseRealm realm, Collection collection, String className) {
        this.realm = realm;
        this.className = className;
        this.collection = collection;
    }

    Table getTable() {
        return collection.getTable();
    }

    Collection getCollection() {
        return collection;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isValid() {
        return collection.isValid();
    }

    /**
     * A {@link RealmResults} is always a managed iteratorCollection.
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
        if (isLoaded()) {
            // Deleted objects can never be part of a RealmResults
            if (object instanceof RealmObjectProxy) {
                RealmObjectProxy proxy = (RealmObjectProxy) object;
                if (proxy.realmGet$proxyState().getRow$realm() == InvalidRow.INSTANCE) {
                    return false;
                }
            }

            for (E e : this) {
                if (e.equals(object)) {
                    return true;
                }
            }
        }
        return false;
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
        realm.checkIfValid();
        return realm.get(classSpec, className, collection.getUncheckedRow(location));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E first() {
        return firstImpl(true, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E first(E defaultValue) {
        return firstImpl(false, defaultValue);
    }

    private E firstImpl(boolean shouldThrow, E defaultValue) {
        UncheckedRow row = collection.firstUncheckedRow();

        if (row != null) {
            return realm.get(classSpec, className, row);
        } else {
            if (shouldThrow) {
                throw new IndexOutOfBoundsException("No results were found.");
            } else {
                return defaultValue;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E last() {
        return lastImpl(true, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E last(E defaultValue) {
        return lastImpl(false, defaultValue);

    }

    private E lastImpl(boolean shouldThrow, E defaultValue) {
        UncheckedRow row = collection.lastUncheckedRow();

        if (row != null) {
            return realm.get(classSpec, className, row);
        } else {
            if (shouldThrow) {
                throw new IndexOutOfBoundsException("No results were found.");
            } else {
                return defaultValue;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteFromRealm(int location) {
        // TODO: Implement the deleteLast in OS level and do check there!
        realm.checkIfValidAndInTransaction();
        collection.delete(location);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteAllFromRealm() {
        realm.checkIfValid();
        if (size() > 0) {
            collection.clear();
            return true;
        }
        return false;
    }

    /**
     * Returns an iterator for the results of a query. Any change to Realm while iterating will cause this iterator to
     * throw a {@link ConcurrentModificationException} if accessed.
     *
     * @return an iterator on the elements of this list.
     * @see Iterator
     */
    @SuppressWarnings("NullableProblems")
    @Override
    public Iterator<E> iterator() {
        return new RealmResultsIterator();
    }

    /**
     * Returns a list iterator for the results of a query. Any change to Realm while iterating will cause the iterator
     * to throw a {@link ConcurrentModificationException} if accessed.
     *
     * @return a ListIterator on the elements of this list.
     * @see ListIterator
     */
    @Override
    public ListIterator<E> listIterator() {
        return new RealmResultsListIterator(0);
    }

    /**
     * Returns a list iterator on the results of a query. Any change to Realm while iterating will cause the iterator to
     * throw a {@link ConcurrentModificationException} if accessed.
     *
     * @param location the index at which to start the iteration.
     * @return a ListIterator on the elements of this list.
     * @throws IndexOutOfBoundsException if {@code location < 0 || location > size()}.
     * @see ListIterator
     */
    @SuppressWarnings("NullableProblems")
    @Override
    public ListIterator<E> listIterator(int location) {
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
        long columnIndex = collection.getTable().getColumnIndex(fieldName);
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
        SortDescriptor sortDescriptor =
                SortDescriptor.getInstanceForSort(collection.getTable(), fieldName, Sort.ASCENDING);

        Collection sortedCollection = collection.sort(sortDescriptor);
        return createLoadedResults(sortedCollection);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RealmResults<E> sort(String fieldName, Sort sortOrder) {
        SortDescriptor sortDescriptor =
                SortDescriptor.getInstanceForSort(collection.getTable(), fieldName, sortOrder);

        Collection sortedCollection = collection.sort(sortDescriptor);
        return createLoadedResults(sortedCollection);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RealmResults<E> sort(String fieldNames[], Sort sortOrders[]) {
        SortDescriptor sortDescriptor =
                SortDescriptor.getInstanceForSort(collection.getTable(), fieldNames, sortOrders);

        Collection sortedCollection = collection.sort(sortDescriptor);
        return createLoadedResults(sortedCollection);
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
        if (isLoaded()) {
            long size = collection.size();
            return (size > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) size;
        }
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public Number min(String fieldName) {
        realm.checkIfValid();
        long columnIndex = getColumnIndexForSort(fieldName);
        return collection.aggregateNumber(io.realm.internal.Collection.Aggregate.MINIMUM, columnIndex);
    }

    /**
     * {@inheritDoc}
     */
    public Date minDate(String fieldName) {
        realm.checkIfValid();
        long columnIndex = getColumnIndexForSort(fieldName);
        return collection.aggregateDate(Collection.Aggregate.MINIMUM, columnIndex);
    }

    /**
     * {@inheritDoc}
     */
    public Number max(String fieldName) {
        realm.checkIfValid();
        long columnIndex = getColumnIndexForSort(fieldName);
        return collection.aggregateNumber(Collection.Aggregate.MAXIMUM, columnIndex);
    }

    /**
     * Finds the maximum date.
     *
     * @param fieldName the field to look for the maximum date. If fieldName is not of Date type, an exception is
     *                  thrown.
     * @return if no objects exist or they all have {@code null} as the value for the given date field, {@code null}
     * will be returned. Otherwise the maximum date is returned. When determining the maximum date, objects with
     * {@code null} values are ignored.
     * @throws IllegalArgumentException if fieldName is not a Date field.
     */
    public Date maxDate(String fieldName) {
        realm.checkIfValid();
        long columnIndex = getColumnIndexForSort(fieldName);
        return collection.aggregateDate(Collection.Aggregate.MAXIMUM, columnIndex);
    }


    /**
     * {@inheritDoc}
     */
    public Number sum(String fieldName) {
        realm.checkIfValid();
        long columnIndex = getColumnIndexForSort(fieldName);
        return collection.aggregateNumber(Collection.Aggregate.SUM, columnIndex);
    }

    /**
     * {@inheritDoc}
     */
    public double average(String fieldName) {
        realm.checkIfValid();
        long columnIndex = getColumnIndexForSort(fieldName);

        Number avg = collection.aggregateNumber(Collection.Aggregate.AVERAGE, columnIndex);
        return avg.doubleValue();
    }

    /**
     * Returns a distinct set of objects of a specific class. If the result is sorted, the first
     * object will be returned in case of multiple occurrences, otherwise it is undefined which
     * object is returned.
     *
     * @param fieldName the field name.
     * @return a new non-null {@link RealmResults} containing the distinct objects.
     * @throws IllegalArgumentException if a field is null, does not exist, is an unsupported type,
     * is not indexed, or points to linked fields.
     */
    public RealmResults<E> distinct(String fieldName) {
        SortDescriptor distinctDescriptor = SortDescriptor.getInstanceForDistinct(collection.getTable(), fieldName);
        Collection distinctCollection = collection.distinct(distinctDescriptor);
        return createLoadedResults(distinctCollection);
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
        realm.sharedRealm.capabilities.checkCanDeliverNotification(RealmQuery.ASYNC_QUERY_WRONG_THREAD_MESSAGE);
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
    public boolean removeAll(@SuppressWarnings("NullableProblems") java.util.Collection<?> collection) {
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
    public boolean retainAll(@SuppressWarnings("NullableProblems") java.util.Collection<?> collection) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_MESSAGE);
    }

    /**
     * Removes the last object in the list. This also deletes the object from the underlying Realm.
     *
     * @throws IllegalStateException if the corresponding Realm is closed or in an incorrect thread.
     */
    @Override
    public boolean deleteLastFromRealm() {
        // TODO: Implement the deleteLast in OS level and do check there!
        realm.checkIfValidAndInTransaction();
        return collection.deleteLast();
    }

    /**
     * Removes the first object in the list. This also deletes the object from the underlying Realm.
     *
     * @throws IllegalStateException if the corresponding Realm is closed or in an incorrect thread.
     */
    @Override
    public boolean deleteFirstFromRealm() {
        // TODO: Implement the deleteLast in OS level and do check there!
        realm.checkIfValidAndInTransaction();
        return collection.deleteFirst();
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
    public boolean addAll(int location,
                          @SuppressWarnings("NullableProblems") java.util.Collection<? extends E> collection) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_MESSAGE);
    }

    /**
     * Not supported by RealmResults.
     *
     * @throws UnsupportedOperationException always.
     */
    @Deprecated
    @Override
    public boolean addAll(@SuppressWarnings("NullableProblems") java.util.Collection<? extends E> collection) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_MESSAGE);
    }

    // Custom RealmResults iterator. It ensures that we only iterate on a Realm that hasn't changed.
    private class RealmResultsIterator extends Collection.Iterator<E> {
        RealmResultsIterator() {
            super(RealmResults.this.collection);
        }

        @Override
        protected E convertRowToObject(UncheckedRow row) {
            return realm.get(classSpec, className, row);
        }
    }

    // Custom RealmResults list iterator.
    private class RealmResultsListIterator extends Collection.ListIterator<E> {
        RealmResultsListIterator(int start) {
            super(RealmResults.this.collection, start);
        }

        @Override
        protected E convertRowToObject(UncheckedRow row) {
            return realm.get(classSpec, className, row);
        }
    }

    /**
     * Returns {@code false} if the results are not yet loaded, {@code true} if they are loaded.
     *
     * @return {@code true} if the query has completed and the data is available, {@code false} if the query is still
     * running in the background.
     */
    public boolean isLoaded() {
        realm.checkIfValid();
        return collection.isLoaded();
    }

    /**
     * Makes an asynchronous query blocking. This will also trigger any registered {@link RealmChangeListener} when
     * the query completes.
     *
     * @return {@code true} if it successfully completed the query, {@code false} otherwise.
     */
    public boolean load() {
        // The Collection doesn't have to be loaded before accessing it if the query has not returned.
        // Instead, accessing the Collection will just trigger the execution of query if needed. We add this flag is
        // only to keep the original behavior of those APIs. eg.: For a async RealmResults, before query returns, the
        // size() call should return 0 instead of running the query get the real size.
        realm.checkIfValid();
        collection.load();
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
        realm.sharedRealm.capabilities.checkCanDeliverNotification(BaseRealm.LISTENER_NOT_ALLOWED_MESSAGE);
        collection.addListener(this, listener);
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
        realm.sharedRealm.capabilities.checkCanDeliverNotification(BaseRealm.LISTENER_NOT_ALLOWED_MESSAGE);
        collection.removeListener(this, listener);
    }

    /**
     * Removes all registered listeners.
     */
    public void removeChangeListeners() {
        realm.checkIfValid();
        realm.sharedRealm.capabilities.checkCanDeliverNotification(BaseRealm.LISTENER_NOT_ALLOWED_MESSAGE);
        collection.removeAllListeners();
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

    private RealmResults<E> createLoadedResults(Collection newCollection) {
        RealmResults<E> results;
        if (className != null) {
            results = new RealmResults<E>(realm, newCollection, className);
        } else {
            results = new RealmResults<E>(realm, newCollection, classSpec);
        }
        results.load();
        return results;
    }
}
