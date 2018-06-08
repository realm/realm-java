package io.realm;

import java.util.AbstractList;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Locale;

import javax.annotation.Nullable;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.realm.internal.OsResults;
import io.realm.internal.InvalidRow;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.SortDescriptor;
import io.realm.internal.Table;
import io.realm.internal.UncheckedRow;


/**
 * General implementation for {@link OrderedRealmCollection} which is based on the {@code Collection}.
 */
abstract class OrderedRealmCollectionImpl<E>
        extends AbstractList<E> implements OrderedRealmCollection<E> {
    private final static String NOT_SUPPORTED_MESSAGE = "This method is not supported by 'RealmResults' or" +
            " 'OrderedRealmCollectionSnapshot'.";

    final BaseRealm realm;
    @Nullable final Class<E> classSpec;   // Return type
    @Nullable final String className;     // Class name used by DynamicRealmObjects
    // FIXME implement this
    @SuppressFBWarnings("SS_SHOULD_BE_STATIC")
    final boolean forValues = false;

    final OsResults osResults;

    OrderedRealmCollectionImpl(BaseRealm realm, OsResults osResults, Class<E> clazz) {
        this(realm, osResults, clazz, null);
    }

    OrderedRealmCollectionImpl(BaseRealm realm, OsResults osResults, String className) {
        this(realm, osResults, null, className);
    }

    private OrderedRealmCollectionImpl(BaseRealm realm, OsResults osResults, @Nullable Class<E> clazz, @Nullable String className) {
        this.realm = realm;
        this.osResults = osResults;
        this.classSpec = clazz;
        this.className = className;
    }

    Table getTable() {
        return osResults.getTable();
    }

    OsResults getOsResults() {
        return osResults;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid() {
        return osResults.isValid();
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
    public boolean contains(@Nullable Object object) {
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
    @Nullable
    public E get(int location) {
        realm.checkIfValid();
        if (forValues) {
            // TODO implement this
            return null;
        }

        //noinspection unchecked
        return (E) realm.get((Class<? extends RealmModel>) classSpec, className, osResults.getUncheckedRow(location));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public E first() {
        return firstImpl(true, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public E first(@Nullable E defaultValue) {
        return firstImpl(false, defaultValue);
    }

    @Nullable
    private E firstImpl(boolean shouldThrow, @Nullable E defaultValue) {
        UncheckedRow row = osResults.firstUncheckedRow();

        if (forValues) {
            // TODO implement this
            return null;
        }

        if (row != null) {
            //noinspection unchecked
            return (E) realm.get((Class<? extends RealmModel>) classSpec, className, row);
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
    @Nullable
    public E last() {
        return lastImpl(true, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public E last(@Nullable E defaultValue) {
        return lastImpl(false, defaultValue);

    }

    @Nullable
    private E lastImpl(boolean shouldThrow, @Nullable E defaultValue) {
        UncheckedRow row = osResults.lastUncheckedRow();

        if (forValues) {
            // TODO implement this
            return null;
        }

        if (row != null) {
            //noinspection unchecked
            return (E) realm.get((Class<? extends RealmModel>) classSpec, className, row);
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
        osResults.delete(location);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteAllFromRealm() {
        realm.checkIfValid();
        if (size() > 0) {
            osResults.clear();
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
        //noinspection ConstantConditions
        if (fieldName == null || fieldName.isEmpty()) {
            throw new IllegalArgumentException("Non-empty field name required.");
        }
        if (fieldName.contains(".")) {
            throw new IllegalArgumentException("Aggregates on child object fields are not supported: " + fieldName);
        }
        long columnIndex = osResults.getTable().getColumnIndex(fieldName);
        if (columnIndex < 0) {
            throw new IllegalArgumentException(String.format(Locale.US, "Field '%s' does not exist.", fieldName));
        }
        return columnIndex;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RealmResults<E> sort(String fieldName) {
        SortDescriptor sortDescriptor =
                SortDescriptor.getInstanceForSort(getSchemaConnector(), osResults.getTable(), fieldName, Sort.ASCENDING);

        OsResults sortedOsResults = osResults.sort(sortDescriptor);
        return createLoadedResults(sortedOsResults);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RealmResults<E> sort(String fieldName, Sort sortOrder) {
        SortDescriptor sortDescriptor =
                SortDescriptor.getInstanceForSort(getSchemaConnector(), osResults.getTable(), fieldName, sortOrder);

        OsResults sortedOsResults = osResults.sort(sortDescriptor);
        return createLoadedResults(sortedOsResults);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RealmResults<E> sort(String fieldNames[], Sort sortOrders[]) {
        SortDescriptor sortDescriptor =
                SortDescriptor.getInstanceForSort(getSchemaConnector(), osResults.getTable(), fieldNames, sortOrders);

        OsResults sortedOsResults = osResults.sort(sortDescriptor);
        return createLoadedResults(sortedOsResults);
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
            long size = osResults.size();
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
        return osResults.aggregateNumber(OsResults.Aggregate.MINIMUM, columnIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date minDate(String fieldName) {
        realm.checkIfValid();
        long columnIndex = getColumnIndexForSort(fieldName);
        return osResults.aggregateDate(OsResults.Aggregate.MINIMUM, columnIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Number max(String fieldName) {
        realm.checkIfValid();
        long columnIndex = getColumnIndexForSort(fieldName);
        return osResults.aggregateNumber(OsResults.Aggregate.MAXIMUM, columnIndex);
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
    @Nullable
    public Date maxDate(String fieldName) {
        realm.checkIfValid();
        long columnIndex = getColumnIndexForSort(fieldName);
        return osResults.aggregateDate(OsResults.Aggregate.MAXIMUM, columnIndex);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Number sum(String fieldName) {
        realm.checkIfValid();
        long columnIndex = getColumnIndexForSort(fieldName);
        return osResults.aggregateNumber(OsResults.Aggregate.SUM, columnIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double average(String fieldName) {
        realm.checkIfValid();
        long columnIndex = getColumnIndexForSort(fieldName);

        Number avg = osResults.aggregateNumber(OsResults.Aggregate.AVERAGE, columnIndex);
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
        return osResults.deleteLast();
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
        return osResults.deleteFirst();
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
    private class RealmCollectionIterator extends OsResults.Iterator<E> {
        RealmCollectionIterator() {
            super(OrderedRealmCollectionImpl.this.osResults);
        }

        @Override
        protected E convertRowToObject(UncheckedRow row) {
            if (forValues) {
                // TODO implement this
                return null;
            }
            //noinspection unchecked
            return (E) realm.get((Class<? extends RealmObject>) classSpec, className, row);
        }
    }

    @Override
    public OrderedRealmCollectionSnapshot<E> createSnapshot() {
        if (className != null) {
            return new OrderedRealmCollectionSnapshot<E>(realm, osResults, className);
        } else {
            // 'classSpec' is non-null when 'className' is null.
            //noinspection ConstantConditions
            return new OrderedRealmCollectionSnapshot<E>(realm, osResults, classSpec);
        }
    }

    /**
     * Returns the {@link Realm} instance to which this collection belongs.
     * <p>
     * Calling {@link Realm#close()} on the returned instance is discouraged as it is the same as
     * calling it on the original Realm instance which may cause the Realm to fully close invalidating the
     * query result.
     *
     * @return {@link Realm} instance this collection belongs to.
     * @throws IllegalStateException if the Realm is an instance of {@link DynamicRealm} or the
     * {@link Realm} was already closed.
     */
    public Realm getRealm() {
        realm.checkIfValid();
        if (!(realm instanceof Realm)) {
            throw new IllegalStateException("This method is only available for typed Realms");
        }
        return (Realm) realm;
    }

    // Custom RealmResults list iterator.
    private class RealmCollectionListIterator extends OsResults.ListIterator<E> {
        RealmCollectionListIterator(int start) {
            super(OrderedRealmCollectionImpl.this.osResults, start);
        }

        @Override
        protected E convertRowToObject(UncheckedRow row) {
            if (forValues) {
                // TODO implement this
                return null;
            }
            //noinspection unchecked
            return (E) realm.get((Class<? extends RealmObject>) classSpec, className, row);
        }
    }

    RealmResults<E> createLoadedResults(OsResults newOsResults) {
        RealmResults<E> results;
        if (className != null) {
            results = new RealmResults<E>(realm, newOsResults, className);
        } else {
            // 'classSpec' is non-null when 'className' is null.
            //noinspection ConstantConditions
            results = new RealmResults<E>(realm, newOsResults, classSpec);
        }
        results.load();
        return results;
    }

    private SchemaConnector getSchemaConnector() {
        return new SchemaConnector(realm.getSchema());
    }
}
