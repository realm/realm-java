package io.realm;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Locale;

import javax.annotation.Nullable;

import io.realm.internal.InvalidRow;
import io.realm.internal.OsResults;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.Table;
import io.realm.internal.UncheckedRow;
import io.realm.internal.core.NativeMixed;

/**
 * General implementation for {@link OrderedRealmCollection} which is based on the {@code Collection}.
 */
abstract class OrderedRealmCollectionImpl<E> extends AbstractList<E> implements OrderedRealmCollection<E> {
    private static final String NOT_SUPPORTED_MESSAGE = "This method is not supported by 'RealmResults' or" +
            " 'OrderedRealmCollectionSnapshot'.";

    /**
     * The {@link BaseRealm} instance in which this collection resides.
     * <p>
     * Warning: This field is only exposed for internal usage, and should not be used.
     */
    public final BaseRealm baseRealm;

    @Nullable
    final Class<E> classSpec;   // Return type
    @Nullable
    final String className;     // Class name used by DynamicRealmObjects
    final OsResults osResults;
    final CollectionOperator<E> operator;

    OrderedRealmCollectionImpl(BaseRealm baseRealm, OsResults osResults, Class<E> clazz) {
        this(baseRealm, osResults, clazz, null, getCollectionOperator(false, baseRealm, osResults, clazz, null));
    }

    OrderedRealmCollectionImpl(BaseRealm baseRealm, OsResults osResults, Class<E> clazz, CollectionOperator<E> operator) {
        this(baseRealm, osResults, clazz, null, operator);
    }

    OrderedRealmCollectionImpl(BaseRealm baseRealm, OsResults osResults, String className) {
        this(baseRealm, osResults, null, className, getCollectionOperator(false, baseRealm, osResults, null, className));
    }

    OrderedRealmCollectionImpl(BaseRealm baseRealm, OsResults osResults, String className, CollectionOperator<E> operator) {
        this(baseRealm, osResults, null, className, operator);
    }

    private OrderedRealmCollectionImpl(BaseRealm baseRealm, OsResults osResults, @Nullable Class<E> clazz, @Nullable String className, CollectionOperator<E> operator) {
        this.baseRealm = baseRealm;
        this.osResults = osResults;
        this.classSpec = clazz;
        this.className = className;
        this.operator = operator;
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
                if (e instanceof byte[] && object instanceof byte[]) {
                    if (Arrays.equals((byte[]) e, (byte[]) object)) {
                        return true;
                    }
                } else {
                    if (e != null && e.equals(object)) {
                        return true;
                    } else if (e == null && object == null) {
                        return true;
                    }
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
        baseRealm.checkIfValid();
        return operator.get(location);
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
        return operator.firstImpl(shouldThrow, defaultValue);
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
        return operator.lastImpl(shouldThrow, defaultValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteFromRealm(int location) {
        // TODO: Implement the delete in OS level and do check there!
        baseRealm.checkIfValidAndInTransaction();
        osResults.delete(location);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteAllFromRealm() {
        baseRealm.checkIfValid();
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
    private long getColumnKeyForSort(String fieldName) {
        //noinspection ConstantConditions
        if (fieldName == null || fieldName.isEmpty()) {
            throw new IllegalArgumentException("Non-empty field name required.");
        }
        if (fieldName.contains(".")) {
            throw new IllegalArgumentException("Aggregates on child object fields are not supported: " + fieldName);
        }
        long columnKey = osResults.getTable().getColumnKey(fieldName);
        if (columnKey < 0) {
            throw new IllegalArgumentException(String.format(Locale.US, "Field '%s' does not exist.", fieldName));
        }
        return columnKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RealmResults<E> sort(String fieldName) {
        OsResults sortedOsResults = osResults.sort(fieldName, Sort.ASCENDING);
        return createLoadedResults(sortedOsResults);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RealmResults<E> sort(String fieldName, Sort sortOrder) {
        OsResults sortedOsResults = osResults.sort(fieldName, sortOrder);
        return createLoadedResults(sortedOsResults);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RealmResults<E> sort(String[] fieldNames, Sort[] sortOrders) {
        OsResults sortedOsResults = osResults.sort(fieldNames, sortOrders);
        return createLoadedResults(sortedOsResults);
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
        baseRealm.checkIfValid();
        long columnKey = getColumnKeyForSort(fieldName);
        return osResults.aggregateNumber(OsResults.Aggregate.MINIMUM, columnKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date minDate(String fieldName) {
        baseRealm.checkIfValid();
        long columnIndex = getColumnKeyForSort(fieldName);
        return osResults.aggregateDate(OsResults.Aggregate.MINIMUM, columnIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Number max(String fieldName) {
        baseRealm.checkIfValid();
        long columnIndex = getColumnKeyForSort(fieldName);
        return osResults.aggregateNumber(OsResults.Aggregate.MAXIMUM, columnIndex);
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
    @Override
    @Nullable
    public Date maxDate(String fieldName) {
        baseRealm.checkIfValid();
        long columnIndex = getColumnKeyForSort(fieldName);
        return osResults.aggregateDate(OsResults.Aggregate.MAXIMUM, columnIndex);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Number sum(String fieldName) {
        baseRealm.checkIfValid();
        long columnIndex = getColumnKeyForSort(fieldName);
        return osResults.aggregateNumber(OsResults.Aggregate.SUM, columnIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double average(String fieldName) {
        baseRealm.checkIfValid();
        long columnIndex = getColumnKeyForSort(fieldName);

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
        baseRealm.checkIfValidAndInTransaction();
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
        baseRealm.checkIfValidAndInTransaction();
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
            return operator.convertRowToObject(row);
        }

        @Override
        protected E getInternal(int pos, OsResults iteratorOsResults) {
            return operator.getFromResults(pos, iteratorOsResults);
        }
    }

    @Override
    public OrderedRealmCollectionSnapshot<E> createSnapshot() {
        if (className != null) {
            return new OrderedRealmCollectionSnapshot<E>(baseRealm, osResults, className);
        } else {
            // 'classSpec' is non-null when 'className' is null.
            //noinspection ConstantConditions
            return new OrderedRealmCollectionSnapshot<E>(baseRealm, osResults, classSpec);
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
     *                               {@link Realm} was already closed.
     */
    public Realm getRealm() {
        baseRealm.checkIfValid();
        if (!(baseRealm instanceof Realm)) {
            throw new IllegalStateException("This method is only available for typed Realms");
        }
        return (Realm) baseRealm;
    }

    // Custom RealmResults list iterator.
    private class RealmCollectionListIterator extends OsResults.ListIterator<E> {
        RealmCollectionListIterator(int start) {
            super(OrderedRealmCollectionImpl.this.osResults, start);
        }

        @Override
        protected E convertRowToObject(UncheckedRow row) {
            return operator.convertRowToObject(row);
        }

        @Override
        protected E getInternal(int pos, OsResults iteratorOsResults) {
            return operator.getFromResults(pos, iteratorOsResults);
        }
    }

    RealmResults<E> createLoadedResults(OsResults newOsResults) {
        RealmResults<E> results;
        if (className != null) {
            results = new RealmResults<E>(baseRealm, newOsResults, className);
        } else {
            // 'classSpec' is non-null when 'className' is null.
            //noinspection ConstantConditions
            results = new RealmResults<E>(baseRealm, newOsResults, classSpec);
        }
        results.load();
        return results;
    }

    protected static <T> CollectionOperator<T> getCollectionOperator(boolean forPrimitives,
                                                                     BaseRealm baseRealm,
                                                                     OsResults osResults,
                                                                     @Nullable Class<T> clazz,
                                                                     @Nullable String className) {
        if (forPrimitives) {
            if (clazz == Integer.class) {
                //noinspection unchecked
                return (CollectionOperator<T>) new IntegerValueOperator(baseRealm, osResults, Integer.class, className);
            } else if (clazz == Short.class) {
                //noinspection unchecked
                return (CollectionOperator<T>) new ShortValueOperator(baseRealm, osResults, Short.class, className);
            } else if (clazz == Byte.class) {
                //noinspection unchecked
                return (CollectionOperator<T>) new ByteValueOperator(baseRealm, osResults, Byte.class, className);
            } else if (clazz == Mixed.class) {
                //noinspection unchecked
                return (CollectionOperator<T>) new MixedValueOperator(baseRealm, osResults, Mixed.class, className);
            } else {
                return new PrimitiveValueOperator<>(baseRealm, osResults, clazz, className);
            }
        } else {
            return new ModelCollectionOperator<>(baseRealm, osResults, clazz, className);
        }
    }

    /**
     * Used to abstract operations from the collection itself depending on whether it is a primitive
     * value collection or a model collection.
     *
     * @param <T> the type of the collection.
     */
    static abstract class CollectionOperator<T> {

        protected final BaseRealm baseRealm;
        protected final OsResults osResults;
        @Nullable
        protected final Class<T> classSpec;
        @Nullable
        protected final String className;

        public CollectionOperator(BaseRealm baseRealm,
                                  OsResults osResults,
                                  @Nullable Class<T> classSpec,
                                  @Nullable String className) {
            this.baseRealm = baseRealm;
            this.osResults = osResults;
            this.classSpec = classSpec;
            this.className = className;
        }

        public abstract T get(int location);

        @Nullable
        public abstract T firstImpl(boolean shouldThrow, @Nullable T defaultValue);

        @Nullable
        public abstract T lastImpl(boolean shouldThrow, @Nullable T defaultValue);

        public abstract T convertRowToObject(UncheckedRow row);

        public abstract T getFromResults(int pos, OsResults iteratorOsResults);

        protected T convertToObject(@Nullable UncheckedRow row,
                                    boolean shouldThrow,
                                    @Nullable T defaultValue) {
            if (row != null) {
                //noinspection unchecked
                return (T) baseRealm.get((Class<? extends RealmModel>) classSpec, className, row);
            } else {
                if (shouldThrow) {
                    throw new IndexOutOfBoundsException("No results were found.");
                } else {
                    return defaultValue;
                }
            }
        }
    }

    /**
     * Operator for models.
     *
     * @param <T> the type of the collection, must extend {@link RealmModel}.
     */
    static class ModelCollectionOperator<T> extends CollectionOperator<T> {

        public ModelCollectionOperator(BaseRealm baseRealm,
                                       OsResults osResults,
                                       @Nullable Class<T> clazz,
                                       @Nullable String className) {
            super(baseRealm, osResults, clazz, className);
        }

        @Override
        public T get(int location) {
            //noinspection unchecked
            return (T) baseRealm.get((Class<? extends RealmModel>) classSpec, className, osResults.getUncheckedRow(location));
        }

        @Nullable
        @Override
        public T firstImpl(boolean shouldThrow, @Nullable T defaultValue) {
            return convertToObject(osResults.firstUncheckedRow(), shouldThrow, defaultValue);
        }

        @Nullable
        @Override
        public T lastImpl(boolean shouldThrow, @Nullable T defaultValue) {
            return convertToObject(osResults.lastUncheckedRow(), shouldThrow, defaultValue);
        }

        @Override
        public T convertRowToObject(UncheckedRow row) {
            //noinspection unchecked
            return (T) baseRealm.get((Class<? extends RealmObject>) classSpec, className, row);
        }

        @Override
        public T getFromResults(int pos, OsResults iteratorOsResults) {
            return convertRowToObject(iteratorOsResults.getUncheckedRow(pos));
        }
    }

    /**
     * Operator for Realm primitive types.
     *
     * @param <T> the type of the collection.
     */
    static class PrimitiveValueOperator<T> extends CollectionOperator<T> {

        public PrimitiveValueOperator(BaseRealm baseRealm,
                                      OsResults osResults,
                                      @Nullable Class<T> classSpec,
                                      @Nullable String className) {
            super(baseRealm, osResults, classSpec, className);
        }

        @Override
        public T get(int location) {
            Object value = osResults.getValue(location);

            //noinspection unchecked
            return (T) value;
        }

        @Nullable
        @Override
        public T firstImpl(boolean shouldThrow, @Nullable T defaultValue) {
            if (osResults.size() != 0) {
                //noinspection unchecked
                return (T) osResults.getValue(0);
            }
            return defaultValue;
        }

        @Nullable
        @Override
        public T lastImpl(boolean shouldThrow, @Nullable T defaultValue) {
            int size = (int) osResults.size();
            if (size != 0) {
                //noinspection unchecked
                return (T) osResults.getValue(size - 1);
            }
            return defaultValue;
        }

        @Override
        public T convertRowToObject(UncheckedRow row) {
            throw new UnsupportedOperationException("Method 'convertRowToObject' cannot be used on primitive Realm collections.");
        }

        @Override
        public T getFromResults(int pos, OsResults iteratorOsResults) {
            //noinspection unchecked
            return (T) iteratorOsResults.getValue(pos);
        }
    }

    static class IntegerValueOperator extends PrimitiveValueOperator<Integer> {

        public IntegerValueOperator(BaseRealm baseRealm,
                                    OsResults osResults,
                                    @Nullable Class<Integer> classSpec,
                                    @Nullable String className) {
            super(baseRealm, osResults, classSpec, className);
        }

        @Override
        public Integer get(int location) {
            Object value = osResults.getValue(location);
            Long longValue = (Long) value;
            return longValue.intValue();
        }

        @Override
        public Integer getFromResults(int pos, OsResults iteratorOsResults) {
            Long longValue = (Long) iteratorOsResults.getValue(pos);
            if (longValue == null) {
                return null;
            }
            return longValue.intValue();
        }
    }

    static class ShortValueOperator extends PrimitiveValueOperator<Short> {

        public ShortValueOperator(BaseRealm baseRealm,
                                  OsResults osResults,
                                  @Nullable Class<Short> classSpec,
                                  @Nullable String className) {
            super(baseRealm, osResults, classSpec, className);
        }

        @Override
        public Short get(int location) {
            Object value = osResults.getValue(location);
            Long longValue = (Long) value;
            return longValue.shortValue();
        }

        @Override
        public Short getFromResults(int pos, OsResults iteratorOsResults) {
            Long longValue = (Long) iteratorOsResults.getValue(pos);
            if (longValue == null) {
                return null;
            }
            return longValue.shortValue();
        }
    }

    static class ByteValueOperator extends PrimitiveValueOperator<Byte> {

        public ByteValueOperator(BaseRealm baseRealm,
                                 OsResults osResults,
                                 @Nullable Class<Byte> classSpec,
                                 @Nullable String className) {
            super(baseRealm, osResults, classSpec, className);
        }

        @Override
        public Byte get(int location) {
            Object value = osResults.getValue(location);
            Long longValue = (Long) value;
            return longValue.byteValue();
        }

        @Override
        public Byte getFromResults(int pos, OsResults iteratorOsResults) {
            Long longValue = (Long) iteratorOsResults.getValue(pos);
            if (longValue == null) {
                return null;
            }
            return longValue.byteValue();
        }
    }

    static class MixedValueOperator extends PrimitiveValueOperator<Mixed> {

        public MixedValueOperator(BaseRealm baseRealm,
                                  OsResults osResults,
                                  @Nullable Class<Mixed> classSpec,
                                  @Nullable String className) {
            super(baseRealm, osResults, classSpec, className);
        }

        @Override
        public Mixed get(int location) {
            Object value = osResults.getValue(location);
            NativeMixed nativeMixed = (NativeMixed) value;
            return new Mixed(MixedOperator.fromNativeMixed(baseRealm, nativeMixed));
        }

        @Override
        public Mixed getFromResults(int pos, OsResults iteratorOsResults) {
            NativeMixed nativeMixed = (NativeMixed) iteratorOsResults.getValue(pos);
            return new Mixed(MixedOperator.fromNativeMixed(baseRealm, nativeMixed));
        }
    }
}
