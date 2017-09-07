package io.realm;

import java.util.AbstractList;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Locale;

import javax.annotation.Nullable;

import io.realm.internal.Collection;
import io.realm.internal.InvalidRow;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.SortDescriptor;
import io.realm.internal.Table;


/**
 * General implementation for {@link OrderedRealmCollection} which is based on the {@code Collection}.
 */
abstract class OrderedRealmCollectionImpl<E>
        extends AbstractList<E> implements OrderedRealmCollection<E> {
    private final static String NOT_SUPPORTED_MESSAGE = "This method is not supported by 'RealmResults' or" +
            " 'OrderedRealmCollectionSnapshot'.";

    final BaseRealm realm;
    @Nullable
    final Class<E> classSpec;   // Return type
    @Nullable
    final String className;     // Class name used by DynamicRealmObjects

    private final CollectionOperator<E> collectionOperator;

    OrderedRealmCollectionImpl(BaseRealm realm, Collection collection, Class<E> clazz) {
        this(realm, collection, clazz, null);
    }

    OrderedRealmCollectionImpl(BaseRealm realm, Collection collection, String className) {
        this(realm, collection, null, className);
    }

    private OrderedRealmCollectionImpl(BaseRealm realm, Collection collection, @Nullable Class<E> clazz, @Nullable String className) {
        this.realm = realm;
        this.classSpec = clazz;
        this.className = className;
        this.collectionOperator = getOperator(realm, collection, clazz, className);
    }

    private static boolean isClassForRealmModel(Class<?> clazz) {
        return RealmModel.class.isAssignableFrom(clazz);
    }

    Table getTable() {
        return collectionOperator.getCollection().getTable();
    }

    Collection getCollection() {
        return collectionOperator.getCollection();
    }

    boolean forValue() {
        return collectionOperator.forValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid() {
        return collectionOperator.isValid();
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

        return collectionOperator.get(location);
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
        if (collectionOperator.isEmpty()) {
            if (shouldThrow) {
                throw new IndexOutOfBoundsException("No results were found.");
            } else {
                return defaultValue;
            }
        }

        return collectionOperator.get(0);
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
        final int size = collectionOperator.size();
        if (size == 0) {
            if (shouldThrow) {
                throw new IndexOutOfBoundsException("No results were found.");
            } else {
                return defaultValue;
            }
        }

        return collectionOperator.get(size - 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteFromRealm(int location) {
        // TODO: Implement the delete in OS level and do check there!
        realm.checkIfValidAndInTransaction();
        collectionOperator.delete(location);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteAllFromRealm() {
        realm.checkIfValid();
        if (!collectionOperator.isEmpty()) {
            collectionOperator.clear();
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

    // aux. method used by aggregation methods
    private long getColumnIndexForAggregation(String fieldName) {
        //noinspection ConstantConditions
        if (fieldName == null || fieldName.isEmpty()) {
            throw new IllegalArgumentException("Non-empty field name required.");
        }
        if (fieldName.contains(".")) {
            throw new IllegalArgumentException("Aggregates on child object fields are not supported: " + fieldName);
        }
        long columnIndex = collectionOperator.getTargetTable().getColumnIndex(fieldName);
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
        return sort(fieldName, Sort.ASCENDING);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RealmResults<E> sort(String fieldName, Sort sortOrder) {
        SortDescriptor sortDescriptor =
                SortDescriptor.getInstanceForSort(getSchemaConnector(), collectionOperator.getTargetTable(), fieldName, sortOrder);

        Collection sortedCollection = collectionOperator.getCollection().sort(sortDescriptor);
        return createLoadedResults(sortedCollection);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RealmResults<E> sort(String fieldNames[], Sort sortOrders[]) {
        SortDescriptor sortDescriptor =
                SortDescriptor.getInstanceForSort(getSchemaConnector(), collectionOperator.getTargetTable(), fieldNames, sortOrders);

        Collection sortedCollection = collectionOperator.getCollection().sort(sortDescriptor);
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
        if (!isLoaded()) {
            return 0;
        }
        return collectionOperator.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Number min(String fieldName) {
        realm.checkIfValid();

        // FIXME primitive list

        long columnIndex = getColumnIndexForAggregation(fieldName);
        return collectionOperator.getCollection().aggregateNumber(io.realm.internal.Collection.Aggregate.MINIMUM, columnIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date minDate(String fieldName) {
        realm.checkIfValid();

        // FIXME primitive list

        long columnIndex = getColumnIndexForAggregation(fieldName);
        return collectionOperator.getCollection().aggregateDate(Collection.Aggregate.MINIMUM, columnIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Number max(String fieldName) {
        realm.checkIfValid();

        // FIXME primitive list

        long columnIndex = getColumnIndexForAggregation(fieldName);
        return collectionOperator.getCollection().aggregateNumber(Collection.Aggregate.MAXIMUM, columnIndex);
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

        // FIXME primitive list

        long columnIndex = getColumnIndexForAggregation(fieldName);
        return collectionOperator.getCollection().aggregateDate(Collection.Aggregate.MAXIMUM, columnIndex);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Number sum(String fieldName) {
        realm.checkIfValid();
        long columnIndex = getColumnIndexForAggregation(fieldName);
        return collectionOperator.getCollection().aggregateNumber(Collection.Aggregate.SUM, columnIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double average(String fieldName) {
        realm.checkIfValid();

        // FIXME primitive list

        long columnIndex = getColumnIndexForAggregation(fieldName);

        Number avg = collectionOperator.getCollection().aggregateNumber(Collection.Aggregate.AVERAGE, columnIndex);
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
        return collectionOperator.deleteLast();
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
        return collectionOperator.deleteFirst();
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
            super(OrderedRealmCollectionImpl.this.collectionOperator.getCollection());
        }

        @Nullable
        @Override
        protected E get(int pos) {
            return collectionOperator.get(pos);
        }
    }

    @Override
    public OrderedRealmCollectionSnapshot<E> createSnapshot() {
        if (className != null) {
            return new OrderedRealmCollectionSnapshot<E>(realm, collectionOperator.getCollection(), className);
        } else {
            // 'classSpec' is non-null when 'className' is null.
            //noinspection ConstantConditions
            return new OrderedRealmCollectionSnapshot<E>(realm, collectionOperator.getCollection(), classSpec);
        }
    }

    // Custom RealmResults list iterator.
    private class RealmCollectionListIterator extends Collection.ListIterator<E> {
        RealmCollectionListIterator(int start) {
            super(OrderedRealmCollectionImpl.this.collectionOperator.getCollection(), start);
        }

        @Nullable
        @Override
        protected E get(int pos) {
            return collectionOperator.get(pos);
        }
    }

    RealmResults<E> createLoadedResults(Collection newCollection) {
        RealmResults<E> results;
        if (className != null) {
            results = new RealmResults<E>(realm, newCollection, className);
        } else {
            // 'classSpec' is non-null when 'className' is null.
            //noinspection ConstantConditions
            results = new RealmResults<E>(realm, newCollection, classSpec);
        }
        results.load();
        return results;
    }

    private SchemaConnector getSchemaConnector() {
        return new SchemaConnector(realm.getSchema());
    }


    private CollectionOperator<E> getOperator(BaseRealm realm, Collection collection, @Nullable Class<E> clazz, @Nullable String className) {
        if (clazz == null || isClassForRealmModel(clazz)) {
            return new RealmModelCollectionOperator<>(realm, collection, clazz, className);
        }
        if (clazz == String.class) {
            //noinspection unchecked
            return (CollectionOperator<E>) new StringCollectionOperator(realm, collection, (Class<String>) clazz);
        }
        if (clazz == Long.class || clazz == Integer.class || clazz == Short.class || clazz == Byte.class) {
            return new LongCollectionOperator<>(realm, collection, clazz);
        }
        if (clazz == Boolean.class) {
            //noinspection unchecked
            return (CollectionOperator<E>) new BooleanCollectionOperator(realm, collection, (Class<Boolean>) clazz);
        }
        if (clazz == byte[].class) {
            //noinspection unchecked
            return (CollectionOperator<E>) new BinaryCollectionOperator(realm, collection, (Class<byte[]>) clazz);
        }
        if (clazz == Double.class) {
            //noinspection unchecked
            return (CollectionOperator<E>) new DoubleCollectionOperator(realm, collection, (Class<Double>) clazz);
        }
        if (clazz == Float.class) {
            //noinspection unchecked
            return (CollectionOperator<E>) new FloatCollectionOperator(realm, collection, (Class<Float>) clazz);
        }
        if (clazz == Date.class) {
            //noinspection unchecked
            return (CollectionOperator<E>) new DateCollectionOperator(realm, collection, (Class<Date>) clazz);
        }
        throw new IllegalArgumentException("Unexpected value class: " + clazz.getName());
    }

    private static abstract class CollectionOperator<T> {
        final BaseRealm realm;
        final Collection collection;
        @Nullable
        final Class<T> clazz;

        CollectionOperator(BaseRealm realm, Collection collection, @Nullable Class<T> clazz) {
            this.realm = realm;
            this.collection = collection;
            this.clazz = clazz;
        }

        final Collection getCollection() {
            return collection;
        }

        abstract boolean forValue();

        final Table getTargetTable() {
            return collection.getTable();
        }

        final boolean isValid() {
            return collection.isValid();
        }

        final int size() {
            final long actualSize = collection.size();
            return actualSize < Integer.MAX_VALUE ? (int) actualSize : Integer.MAX_VALUE;
        }

        final boolean isEmpty() {
            return collection.size() == 0;
        }

        @Nullable
        abstract T get(int index);

        final void delete(int index) {
            collection.delete(index);
        }

        final boolean deleteFirst() {
            return collection.deleteFirst();
        }

        final boolean deleteLast() {
            return collection.deleteLast();
        }

        final void clear() {
            collection.clear();
        }
    }

    private static final class RealmModelCollectionOperator<T> extends CollectionOperator<T> {
        @Nullable
        private String className;

        RealmModelCollectionOperator(BaseRealm realm, Collection collection, @Nullable Class<T> clazz, @Nullable String className) {
            super(realm, collection, clazz);
            this.className = className;
        }

        @Override
        boolean forValue() {
            return true;
        }

        @Nullable
        @Override
        T get(int index) {
            //noinspection unchecked
            return (T) realm.get((Class<? extends RealmModel>) clazz, className, collection.getUncheckedRow(index));
        }
    }

    private static final class StringCollectionOperator extends CollectionOperator<String> {

        StringCollectionOperator(BaseRealm realm, Collection collection, @Nullable Class<String> clazz) {
            super(realm, collection, clazz);
        }

        @Override
        boolean forValue() {
            return false;
        }

        @Nullable
        @Override
        String get(int index) {
            return (String) collection.getValue(index);
        }
    }

    private static final class LongCollectionOperator<T> extends CollectionOperator<T> {

        LongCollectionOperator(BaseRealm realm, Collection collection, @Nullable Class<T> clazz) {
            super(realm, collection, clazz);
        }

        @Override
        boolean forValue() {
            return false;
        }

        @Nullable
        @Override
        T get(int index) {
            final Long value = (Long) collection.getValue(index);
            if (value == null) {
                return null;
            }
            if (clazz == Long.class) {
                //noinspection unchecked
                return (T) value;
            }
            if (clazz == Integer.class) {
                //noinspection unchecked,UnnecessaryBoxing,ConstantConditions
                return clazz.cast(Integer.valueOf(value.intValue()));
            }
            if (clazz == Short.class) {
                //noinspection unchecked,UnnecessaryBoxing,ConstantConditions
                return clazz.cast(Short.valueOf(value.shortValue()));
            }
            if (clazz == Byte.class) {
                //noinspection unchecked,UnnecessaryBoxing,ConstantConditions
                return clazz.cast(Byte.valueOf(value.byteValue()));
            }
            //noinspection ConstantConditions
            throw new IllegalStateException("Unexpected element type: " + clazz.getName());
        }
    }


    private static final class BooleanCollectionOperator extends CollectionOperator<Boolean> {

        BooleanCollectionOperator(BaseRealm realm, Collection collection, @Nullable Class<Boolean> clazz) {
            super(realm, collection, clazz);
        }

        @Override
        boolean forValue() {
            return false;
        }

        @Nullable
        @Override
        Boolean get(int index) {
            return (Boolean) collection.getValue(index);
        }
    }

    private static final class BinaryCollectionOperator extends CollectionOperator<byte[]> {

        BinaryCollectionOperator(BaseRealm realm, Collection collection, @Nullable Class<byte[]> clazz) {
            super(realm, collection, clazz);
        }

        @Override
        boolean forValue() {
            return false;
        }

        @Nullable
        @Override
        byte[] get(int index) {
            return (byte[]) collection.getValue(index);
        }
    }

    private static final class DoubleCollectionOperator extends CollectionOperator<Double> {

        DoubleCollectionOperator(BaseRealm realm, Collection collection, @Nullable Class<Double> clazz) {
            super(realm, collection, clazz);
        }

        @Override
        boolean forValue() {
            return false;
        }

        @Nullable
        @Override
        Double get(int index) {
            return (Double) collection.getValue(index);
        }
    }

    private static final class FloatCollectionOperator extends CollectionOperator<Float> {

        FloatCollectionOperator(BaseRealm realm, Collection collection, @Nullable Class<Float> clazz) {
            super(realm, collection, clazz);
        }

        @Override
        boolean forValue() {
            return false;
        }

        @Nullable
        @Override
        Float get(int index) {
            return (Float) collection.getValue(index);
        }
    }

    private static final class DateCollectionOperator extends CollectionOperator<Date> {

        DateCollectionOperator(BaseRealm realm, Collection collection, @Nullable Class<Date> clazz) {
            super(realm, collection, clazz);
        }

        @Override
        boolean forValue() {
            return false;
        }

        @Nullable
        @Override
        Date get(int index) {
            return (Date) collection.getValue(index);
        }
    }
}
