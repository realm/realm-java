package io.realm;

import java.util.AbstractList;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Iterator;
import java.util.ListIterator;

import io.realm.internal.Collection;
import io.realm.internal.InvalidRow;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.SortDescriptor;
import io.realm.internal.Table;
import io.realm.internal.UncheckedRow;


/**
 * General implementation for {@link OrderedRealmCollection} which is based on the {@code Collection}.
 */
abstract class OrderedRealmCollectionImpl<E extends RealmModel>
        extends AbstractList<E> implements OrderedRealmCollection<E> {
    private final static String NOT_SUPPORTED_MESSAGE = "This method is not supported by 'RealmResults' or" +
            " 'OrderedRealmCollectionSnapshot'.";

    final BaseRealm realm;
    final Class<E> classSpec;   // Return type
    final String className;     // Class name used by DynamicRealmObjects

    final Collection collection;

    OrderedRealmCollectionImpl(BaseRealm realm, Collection collection, Class<E> clazz) {
        this(realm, collection, clazz, null);
    }

    OrderedRealmCollectionImpl(BaseRealm realm, Collection collection, String className) {
        this(realm, collection, null, className);
    }

    private OrderedRealmCollectionImpl(BaseRealm realm, Collection collection, Class<E> clazz, String className) {
        this.realm = realm;
        this.collection = collection;
        this.classSpec = clazz;
        this.className = className;
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
    @Override
    public boolean isValid() {
        return collection.isValid();
    }

    /**
     * A {@link RealmResults} or a {@link OrderedRealmCollectionSnapshot} is always a managed collection.
     *
     * @return {@code true}.
     * @see RealmCollection#isManaged()
     */
    @Override
    public boolean isManaged() {
        return true;
    }

    /**
     * Searches this {@link OrderedRealmCollection} for the specified object.
     *
     * @param object the object to search for.
     * @return {@code true} if {@code object} is an element of this {@code OrderedRealmCollection},
     * {@code false} otherwise.
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
        // TODO: Implement the delete in OS level and do check there!
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
        return new RealmCollectionIterator();
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
        return new RealmCollectionListIterator(0);
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
        return new RealmCollectionListIterator(location);
    }

    // Sorting

    // aux. method used by sort methods
    private long getColumnIndexForSort(String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) {
            throw new IllegalArgumentException("Non-empty field name required.");
        }
        if (fieldName.contains(".")) {
            throw new IllegalArgumentException("Aggregates on child object fields are not supported: " + fieldName);
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
                SortDescriptor.getInstanceForSort(getSchemaConnector(), collection.getTable(), fieldName, Sort.ASCENDING);

        Collection sortedCollection = collection.sort(sortDescriptor);
        return createLoadedResults(sortedCollection);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RealmResults<E> sort(String fieldName, Sort sortOrder) {
        SortDescriptor sortDescriptor =
                SortDescriptor.getInstanceForSort(getSchemaConnector(), collection.getTable(), fieldName, sortOrder);

        Collection sortedCollection = collection.sort(sortDescriptor);
        return createLoadedResults(sortedCollection);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RealmResults<E> sort(String fieldNames[], Sort sortOrders[]) {
        SortDescriptor sortDescriptor =
                SortDescriptor.getInstanceForSort(getSchemaConnector(), collection.getTable(), fieldNames, sortOrders);

        Collection sortedCollection = collection.sort(sortDescriptor);
        return createLoadedResults(sortedCollection);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RealmResults<E> sort(String fieldName1, Sort sortOrder1, String fieldName2, Sort sortOrder2) {
        return sort(new String[] {fieldName1, fieldName2}, new Sort[] {sortOrder1, sortOrder2});
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
    @Override
    public Number min(String fieldName) {
        realm.checkIfValid();
        long columnIndex = getColumnIndexForSort(fieldName);
        return collection.aggregateNumber(io.realm.internal.Collection.Aggregate.MINIMUM, columnIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date minDate(String fieldName) {
        realm.checkIfValid();
        long columnIndex = getColumnIndexForSort(fieldName);
        return collection.aggregateDate(Collection.Aggregate.MINIMUM, columnIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Number max(String fieldName) {
        realm.checkIfValid();
        long columnIndex = getColumnIndexForSort(fieldName);
        return collection.aggregateNumber(Collection.Aggregate.MAXIMUM, columnIndex);
    }

    /**
     * Finds the maximum date.
     *
     * @param fieldName the field to look for the maximum date. If fieldName is not of Date type, an exception is
     * thrown.
     * @return if no objects exist or they all have {@code null} as the value for the given date field, {@code null}
     * will be returned. Otherwise the maximum date is returned. When determining the maximum date, objects with
     * {@code null} values are ignored.
     * @throws IllegalArgumentException if fieldName is not a Date field.
     */
    @Override
    public Date maxDate(String fieldName) {
        realm.checkIfValid();
        long columnIndex = getColumnIndexForSort(fieldName);
        return collection.aggregateDate(Collection.Aggregate.MAXIMUM, columnIndex);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Number sum(String fieldName) {
        realm.checkIfValid();
        long columnIndex = getColumnIndexForSort(fieldName);
        return collection.aggregateNumber(Collection.Aggregate.SUM, columnIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double average(String fieldName) {
        realm.checkIfValid();
        long columnIndex = getColumnIndexForSort(fieldName);

        Number avg = collection.aggregateNumber(Collection.Aggregate.AVERAGE, columnIndex);
        return avg.doubleValue();
    }

    // Deleting

    /**
     * Not supported by {@link RealmResults} and {@link OrderedRealmCollectionSnapshot}.
     *
     * @throws UnsupportedOperationException
     */
    @Deprecated
    @Override
    public E remove(int index) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_MESSAGE);
    }

    /**
     * Not supported by {@link RealmResults} and {@link OrderedRealmCollectionSnapshot}.
     *
     * @throws UnsupportedOperationException
     */
    @Deprecated
    @Override
    public boolean remove(Object object) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_MESSAGE);
    }

    /**
     * Not supported by {@link RealmResults} and {@link OrderedRealmCollectionSnapshot}.
     *
     * @throws UnsupportedOperationException
     */
    @Deprecated
    @Override
    public boolean removeAll(@SuppressWarnings("NullableProblems") java.util.Collection<?> collection) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_MESSAGE);
    }

    /**
     * Not supported by {@link RealmResults} and {@link OrderedRealmCollectionSnapshot}.
     *
     * @throws UnsupportedOperationException
     */
    @Deprecated
    @Override
    public E set(int location, E object) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_MESSAGE);
    }

    /**
     * Not supported by {@link RealmResults} and {@link OrderedRealmCollectionSnapshot}.
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
     * Not supported by {@link RealmResults} and {@link OrderedRealmCollectionSnapshot}.
     *
     * @throws UnsupportedOperationException always.
     */
    @Override
    @Deprecated
    public void clear() {
        throw new UnsupportedOperationException(NOT_SUPPORTED_MESSAGE);
    }

    /**
     * Not supported by {@link RealmResults} and {@link OrderedRealmCollectionSnapshot}.
     *
     * @throws UnsupportedOperationException always.
     */
    @Override
    @Deprecated
    public boolean add(E element) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_MESSAGE);
    }

    /**
     * Not supported by {@link RealmResults} and {@link OrderedRealmCollectionSnapshot}.
     *
     * @throws UnsupportedOperationException always.
     */
    @Override
    @Deprecated
    public void add(int index, E element) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_MESSAGE);
    }

    /**
     * Not supported by {@link RealmResults} and {@link OrderedRealmCollectionSnapshot}.
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
     * Not supported by {@link RealmResults} and {@link OrderedRealmCollectionSnapshot}.
     *
     * @throws UnsupportedOperationException always.
     */
    @Deprecated
    @Override
    public boolean addAll(@SuppressWarnings("NullableProblems") java.util.Collection<? extends E> collection) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_MESSAGE);
    }

    // Custom RealmResults iterator. It ensures that we only iterate on a Realm that hasn't changed.
    private class RealmCollectionIterator extends Collection.Iterator<E> {
        RealmCollectionIterator() {
            super(OrderedRealmCollectionImpl.this.collection);
        }

        @Override
        protected E convertRowToObject(UncheckedRow row) {
            return realm.get(classSpec, className, row);
        }
    }

    @Override
    public OrderedRealmCollectionSnapshot<E> createSnapshot() {
        if (className != null) {
            return new OrderedRealmCollectionSnapshot<E>(realm, collection, className);
        } else {
            return new OrderedRealmCollectionSnapshot<E>(realm, collection, classSpec);
        }
    }

    // Custom RealmResults list iterator.
    private class RealmCollectionListIterator extends Collection.ListIterator<E> {
        RealmCollectionListIterator(int start) {
            super(OrderedRealmCollectionImpl.this.collection, start);
        }

        @Override
        protected E convertRowToObject(UncheckedRow row) {
            return realm.get(classSpec, className, row);
        }
    }

    RealmResults<E> createLoadedResults(Collection newCollection) {
        RealmResults<E> results;
        if (className != null) {
            results = new RealmResults<E>(realm, newCollection, className);
        } else {
            results = new RealmResults<E>(realm, newCollection, classSpec);
        }
        results.load();
        return results;
    }

    private SchemaConnector getSchemaConnector() {
        return new SchemaConnector(realm.getSchema());
    }
}
