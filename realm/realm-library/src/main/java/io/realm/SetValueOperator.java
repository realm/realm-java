/*
 * Copyright 2021 Realm Inc.
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

import org.bson.types.Decimal128;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import javax.annotation.Nullable;

import io.realm.internal.ObservableSet;
import io.realm.internal.ObserverPairList;
import io.realm.internal.OsSet;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.Row;
import io.realm.internal.core.NativeRealmAny;
import io.realm.internal.core.NativeRealmAnyCollection;

import static io.realm.CollectionUtils.SET_TYPE;


/**
 * Abstraction for different set value types.
 *
 * @param <E> the value type
 */
abstract class SetValueOperator<E> implements ObservableSet {

    protected final BaseRealm baseRealm;
    protected final OsSet osSet;
    protected final Class<E> valueClass;

    protected final ObserverPairList<ObservableSet.SetObserverPair<E>> setObserverPairs = new ObserverPairList<>();

    SetValueOperator(BaseRealm baseRealm, OsSet osSet, Class<E> valueClass) {
        this.baseRealm = baseRealm;
        this.osSet = osSet;
        this.valueClass = valueClass;
    }

    abstract boolean add(@Nullable E value);

    abstract boolean containsInternal(@Nullable Object o);

    abstract boolean removeInternal(@Nullable Object o);

    abstract boolean containsAllInternal(Collection<?> c);

    abstract boolean addAllInternal(Collection<? extends E> c);

    abstract boolean removeAllInternal(Collection<?> c);

    abstract boolean retainAllInternal(Collection<?> c);

    RealmQuery<E> where(){
        throw new UnsupportedOperationException("This feature is available only when the element type is implementing RealmModel.");
    }

    void deleteAll(){
        osSet.deleteAll();
    }

    @Override
    public void notifyChangeListeners(long nativeChangeSetPtr) {
        osSet.notifyChangeListeners(nativeChangeSetPtr, setObserverPairs);
    }

    boolean contains(@Nullable Object o) {
        if (!isObjectSameType(o)) {
            // Throw as per interface contract
            throw new ClassCastException("Set contents and object must be the same type when calling 'contains'.");
        }
        return containsInternal(o);
    }

    boolean remove(@Nullable Object o) {
        if (!isObjectSameType(o)) {
            // Throw as per interface contract
            throw new ClassCastException("Set contents and object must be the same type when calling 'remove'.");
        }
        return removeInternal(o);
    }

    boolean containsAll(Collection<?> c) {
        if (isRealmCollection(c)) {
            OsSet otherOsSet = ((RealmSet<?>) c).getOsSet();
            return funnelCollection(otherOsSet, OsSet.ExternalCollectionOperation.CONTAINS_ALL);
        }
        if (!isCollectionSameType(c)) {
            // Throw as per interface contract
            throw new ClassCastException("Set contents and collection must be the same type when calling 'containsAll'.");
        }

        return containsAllInternal(c);
    }

    boolean addAll(Collection<? extends E> c) {
        if (isRealmCollection(c)) {
            OsSet otherOsSet = ((RealmSet<?>) c).getOsSet();
            return funnelCollection(otherOsSet, OsSet.ExternalCollectionOperation.ADD_ALL);
        }
        if (!isUpperBoundCollectionSameType(c)) {
            // Throw as per interface contract
            throw new ClassCastException("Set contents and collection must be the same type when calling 'addAll'.");
        }

        return addAllInternal(c);
    }

    boolean removeAll(Collection<?> c) {
        if (isRealmCollection(c)) {
            OsSet otherOsSet = ((RealmSet<?>) c).getOsSet();
            return funnelCollection(otherOsSet, OsSet.ExternalCollectionOperation.REMOVE_ALL);
        }
        if (!isCollectionSameType(c)) {
            // Throw as per interface contract
            throw new ClassCastException("Set contents and collection must be the same type when calling 'removeAll'.");
        }

        return removeAllInternal(c);
    }

    boolean retainAll(Collection<?> c) {
        if (isRealmCollection(c)) {
            OsSet otherOsSet = ((RealmSet<?>) c).getOsSet();
            return funnelCollection(otherOsSet, OsSet.ExternalCollectionOperation.RETAIN_ALL);
        }
        if (!isCollectionSameType(c)) {
            // Throw as per interface contract
            throw new ClassCastException("Set contents and collection must be the same type when calling 'retainAll'.");
        }

        return retainAllInternal(c);
    }

    boolean isValid() {
        if (baseRealm.isClosed()) {
            return false;
        }
        return osSet.isValid();
    }

    boolean isFrozen() {
        return baseRealm.isFrozen();
    }

    int size() {
        return Long.valueOf(osSet.size()).intValue();
    }

    boolean isEmpty() {
        return size() == 0;
    }

    Iterator<E> iterator() {
        return iteratorFactory(valueClass, osSet, baseRealm);
    }

    void clear() {
        osSet.clear();
    }

    RealmSet<E> freeze() {
        BaseRealm frozenRealm = baseRealm.freeze();
        OsSet frozenOsSet = osSet.freeze(frozenRealm.sharedRealm);
        return new RealmSet<>(frozenRealm, frozenOsSet, valueClass);
    }

    void addChangeListener(RealmSet<E> realmSet, SetChangeListener<E> listener) {
        CollectionUtils.checkForAddRemoveListener(baseRealm, listener, true);
        if (setObserverPairs.isEmpty()) {
            osSet.startListening(this);
        }
        ObservableSet.SetObserverPair<E> setObserverPair = new ObservableSet.SetObserverPair<>(realmSet, listener);
        setObserverPairs.add(setObserverPair);
    }

    void addChangeListener(RealmSet<E> realmSet, RealmChangeListener<RealmSet<E>> listener) {
        SetChangeListener<E> changeListener = new SetChangeListener<E>() {
            @Override
            public void onChange(RealmSet<E> set, SetChangeSet changes) {
                listener.onChange(set);
            }
        };
        addChangeListener(realmSet, changeListener);
    }

    void removeChangeListener(RealmSet<E> realmSet, RealmChangeListener<RealmSet<E>> listener) {
        removeChangeListener(realmSet, new SetChangeListener<E>() {
            @Override
            public void onChange(RealmSet<E> set, SetChangeSet changes) {
                listener.onChange(set);
            }
        });
    }

    void removeChangeListener(RealmSet<E> realmSet, SetChangeListener<E> listener) {
        setObserverPairs.remove(realmSet, listener);
        if (setObserverPairs.isEmpty()) {
            osSet.stopListening();
        }
    }

    void removeAllChangeListeners() {
        CollectionUtils.checkForAddRemoveListener(baseRealm, null, false);
        setObserverPairs.clear();
        osSet.stopListening();
    }

    boolean hasListeners() {
        return !setObserverPairs.isEmpty();
    }

    OsSet getOsSet() {
        return osSet;
    }

    @SuppressWarnings("unchecked")
    protected boolean isRealmCollection(Collection<?> c) {
        // TODO: add support for RealmList and RealmResults when overloading is exposed by OS/Core
        return c instanceof RealmSet && ((RealmSet<? extends E>) c).isManaged();
    }

    protected boolean funnelCollection(OsSet otherOsSet,
                                       OsSet.ExternalCollectionOperation operation) {
        // Special case if the passed collection is the same native set as this one
        if (osSet.getNativePtr() == otherOsSet.getNativePtr()) {
            switch (operation) {
                case CONTAINS_ALL:
                    // A set always contains itself
                    return true;
                case ADD_ALL:
                    // Nothing changes if we add this set to this very set
                    return false;
                case REMOVE_ALL:
                    // Clear and return true if the passed collection is this very set
                    osSet.clear();
                    return true;
                case RETAIN_ALL:
                    // Nothing changes if this set intersects this very set
                    return false;
                default:
                    throw new IllegalStateException("Unexpected value: " + operation);
            }
        }

        // Otherwise compute set-specific operation
        switch (operation) {
            case CONTAINS_ALL:
                return osSet.containsAll(otherOsSet);
            case ADD_ALL:
                return osSet.union(otherOsSet);
            case REMOVE_ALL:
                return osSet.asymmetricDifference(otherOsSet);
            case RETAIN_ALL:
                return osSet.intersect(otherOsSet);
            default:
                throw new IllegalStateException("Unexpected value: " + operation);
        }
    }

    private boolean isObjectSameType(@Nullable Object object) {
        // Return false when passing something else than the correct type
        if (object != null) {
            return valueClass.isAssignableFrom(object.getClass());
        } else {
            return true;
        }
    }

    private boolean isUpperBoundCollectionSameType(Collection<? extends E> c) {
        if (!c.isEmpty()) {
            for (E item : c) {
                if (item != null && !valueClass.isAssignableFrom(item.getClass())) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isCollectionSameType(Collection<?> c) {
        if (!c.isEmpty()) {
            for (Object item : c) {
                if (item != null && !valueClass.isAssignableFrom(item.getClass())) {
                    return false;
                }
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private static <T> SetIterator<T> iteratorFactory(Class<T> valueClass,
                                                      OsSet osSet,
                                                      BaseRealm baseRealm) {
        if (valueClass == Boolean.class) {
            return (SetIterator<T>) new BooleanSetIterator(osSet, baseRealm);
        } else if (valueClass == String.class) {
            return (SetIterator<T>) new StringSetIterator(osSet, baseRealm);
        } else if (valueClass == Integer.class) {
            return (SetIterator<T>) new IntegerSetIterator(osSet, baseRealm);
        } else if (valueClass == Long.class) {
            return (SetIterator<T>) new LongSetIterator(osSet, baseRealm);
        } else if (valueClass == Short.class) {
            return (SetIterator<T>) new ShortSetIterator(osSet, baseRealm);
        } else if (valueClass == Byte.class) {
            return (SetIterator<T>) new ByteSetIterator(osSet, baseRealm);
        } else if (valueClass == Float.class) {
            return (SetIterator<T>) new FloatSetIterator(osSet, baseRealm);
        } else if (valueClass == Double.class) {
            return (SetIterator<T>) new DoubleSetIterator(osSet, baseRealm);
        } else if (valueClass == byte[].class) {
            return (SetIterator<T>) new BinarySetIterator(osSet, baseRealm);
        } else if (valueClass == Date.class) {
            return (SetIterator<T>) new DateSetIterator(osSet, baseRealm);
        } else if (valueClass == Decimal128.class) {
            return (SetIterator<T>) new Decimal128SetIterator(osSet, baseRealm);
        } else if (valueClass == ObjectId.class) {
            return (SetIterator<T>) new ObjectIdSetIterator(osSet, baseRealm);
        } else if (valueClass == UUID.class) {
            return (SetIterator<T>) new UUIDSetIterator(osSet, baseRealm);
        } else if (valueClass == RealmAny.class) {
            return (SetIterator<T>) new RealmAnySetIterator(osSet, baseRealm);
        } else if (CollectionUtils.isClassForRealmModel(valueClass)) {
            return (SetIterator<T>) new RealmModelSetIterator(osSet, baseRealm, valueClass);
        } else {
            throw new IllegalArgumentException("Unknown class for iterator: " + valueClass.getSimpleName());
        }
    }
}

/**
 * {@link SetValueOperator} targeting {@code boolean} values in {@link RealmSet}s.
 */
class BooleanOperator extends SetValueOperator<Boolean> {

    BooleanOperator(BaseRealm baseRealm, OsSet osSet, Class<Boolean> valueClass) {
        super(baseRealm, osSet, valueClass);
    }

    @Override
    boolean add(@Nullable Boolean value) {
        return osSet.add(value);
    }

    @Override
    boolean containsInternal(@Nullable Object o) {
        return osSet.contains((Boolean) o);
    }

    @Override
    boolean removeInternal(@Nullable Object o) {
        return osSet.remove((Boolean) o);
    }

    @Override
    boolean containsAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        Collection<Boolean> booleanCollection = (Collection<Boolean>) c;
        NativeRealmAnyCollection collection = NativeRealmAnyCollection.newBooleanCollection(booleanCollection);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.CONTAINS_ALL);
    }

    @Override
    boolean addAllInternal(Collection<? extends Boolean> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        NativeRealmAnyCollection collection = NativeRealmAnyCollection.newBooleanCollection((Collection<Boolean>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.ADD_ALL);
    }

    @Override
    boolean removeAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        NativeRealmAnyCollection collection = NativeRealmAnyCollection.newBooleanCollection((Collection<Boolean>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.REMOVE_ALL);
    }

    @Override
    boolean retainAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        NativeRealmAnyCollection collection = NativeRealmAnyCollection.newBooleanCollection((Collection<Boolean>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.RETAIN_ALL);
    }
}

/**
 * {@link SetValueOperator} targeting {@link String} values in {@link RealmSet}s.
 */
class StringOperator extends SetValueOperator<String> {

    StringOperator(BaseRealm baseRealm, OsSet osSet, Class<String> valueClass) {
        super(baseRealm, osSet, valueClass);
    }

    @Override
    boolean add(@Nullable String value) {
        return osSet.add(value);
    }

    @Override
    boolean containsInternal(@Nullable Object o) {
        return osSet.contains((String) o);
    }

    @Override
    boolean removeInternal(@Nullable Object o) {
        // Object has been type-checked from caller
        return osSet.remove((String) o);
    }

    @Override
    boolean containsAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        Collection<String> stringCollection = (Collection<String>) c;
        NativeRealmAnyCollection collection = NativeRealmAnyCollection.newStringCollection(stringCollection);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.CONTAINS_ALL);
    }

    @Override
    boolean addAllInternal(Collection<? extends String> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        NativeRealmAnyCollection collection = NativeRealmAnyCollection.newStringCollection((Collection<String>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.ADD_ALL);
    }

    @Override
    boolean removeAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        NativeRealmAnyCollection collection = NativeRealmAnyCollection.newStringCollection((Collection<String>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.REMOVE_ALL);
    }

    @Override
    boolean retainAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        NativeRealmAnyCollection collection = NativeRealmAnyCollection.newStringCollection((Collection<String>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.RETAIN_ALL);
    }
}

/**
 * {@link SetValueOperator} targeting {@code int} values in {@link RealmSet}s.
 */
class IntegerOperator extends SetValueOperator<Integer> {

    IntegerOperator(BaseRealm baseRealm, OsSet osSet, Class<Integer> valueClass) {
        super(baseRealm, osSet, valueClass);
    }

    @Override
    boolean add(@Nullable Integer value) {
        return osSet.add(value);
    }

    @Override
    boolean containsInternal(@Nullable Object o) {
        Long value;
        if (o == null) {
            value = null;
        } else {
            value = ((Integer) o).longValue();
        }
        return osSet.contains(value);
    }

    @Override
    boolean removeInternal(@Nullable Object o) {
        // Object has been type-checked from caller
        return osSet.remove((Integer) o);
    }

    @Override
    boolean containsAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        Collection<Number> numberCollection = (Collection<Number>) c;
        NativeRealmAnyCollection collection = NativeRealmAnyCollection.newIntegerCollection(numberCollection);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.CONTAINS_ALL);
    }

    @Override
    boolean addAllInternal(Collection<? extends Integer> c) {
        // Collection has been type-checked from caller
        NativeRealmAnyCollection collection = NativeRealmAnyCollection.newIntegerCollection(c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.ADD_ALL);
    }

    @Override
    boolean removeAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        NativeRealmAnyCollection collection = NativeRealmAnyCollection.newIntegerCollection((Collection<Integer>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.REMOVE_ALL);
    }

    @Override
    boolean retainAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        NativeRealmAnyCollection collection = NativeRealmAnyCollection.newIntegerCollection((Collection<Integer>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.RETAIN_ALL);
    }
}

/**
 * {@link SetValueOperator} targeting {@code long} values in {@link RealmSet}s.
 */
class LongOperator extends SetValueOperator<Long> {

    LongOperator(BaseRealm baseRealm, OsSet osSet, Class<Long> valueClass) {
        super(baseRealm, osSet, valueClass);
    }

    @Override
    boolean add(@Nullable Long value) {
        return osSet.add(value);
    }

    @Override
    boolean containsInternal(@Nullable Object o) {
        return osSet.contains((Long) o);
    }

    @Override
    boolean removeInternal(@Nullable Object o) {
        // Object has been type-checked from caller
        return osSet.remove((Long) o);
    }

    @Override
    boolean containsAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        NativeRealmAnyCollection collection = NativeRealmAnyCollection.newIntegerCollection((Collection<Number>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.CONTAINS_ALL);
    }

    @Override
    boolean addAllInternal(Collection<? extends Long> c) {
        // Collection has been type-checked from caller
        NativeRealmAnyCollection collection = NativeRealmAnyCollection.newIntegerCollection(c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.ADD_ALL);
    }

    @Override
    boolean removeAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        NativeRealmAnyCollection collection = NativeRealmAnyCollection.newIntegerCollection((Collection<Long>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.REMOVE_ALL);
    }

    @Override
    boolean retainAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        NativeRealmAnyCollection collection = NativeRealmAnyCollection.newIntegerCollection((Collection<Long>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.RETAIN_ALL);
    }
}

/**
 * {@link SetValueOperator} targeting {@code short} values in {@link RealmSet}s.
 */
class ShortOperator extends SetValueOperator<Short> {

    ShortOperator(BaseRealm baseRealm, OsSet osSet, Class<Short> valueClass) {
        super(baseRealm, osSet, valueClass);
    }

    @Override
    boolean add(@Nullable Short value) {
        return osSet.add(value);
    }

    @Override
    boolean containsInternal(@Nullable Object o) {
        Long value;
        if (o == null) {
            value = null;
        } else {
            value = ((Short) o).longValue();
        }
        return osSet.contains(value);
    }

    @Override
    boolean removeInternal(@Nullable Object o) {
        // Object has been type-checked from caller
        return osSet.remove((Short) o);
    }

    @Override
    boolean containsAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        Collection<Number> numberCollection = (Collection<Number>) c;
        NativeRealmAnyCollection collection = NativeRealmAnyCollection.newIntegerCollection(numberCollection);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.CONTAINS_ALL);
    }

    @Override
    boolean addAllInternal(Collection<? extends Short> c) {
        // Collection has been type-checked from caller
        NativeRealmAnyCollection collection = NativeRealmAnyCollection.newIntegerCollection(c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.ADD_ALL);
    }

    @Override
    boolean removeAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        NativeRealmAnyCollection collection = NativeRealmAnyCollection.newIntegerCollection((Collection<Short>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.REMOVE_ALL);
    }

    @Override
    boolean retainAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        NativeRealmAnyCollection collection = NativeRealmAnyCollection.newIntegerCollection((Collection<Short>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.RETAIN_ALL);
    }
}

/**
 * {@link SetValueOperator} targeting {@code byte} values in {@link RealmSet}s.
 */
class ByteOperator extends SetValueOperator<Byte> {

    ByteOperator(BaseRealm baseRealm, OsSet osSet, Class<Byte> valueClass) {
        super(baseRealm, osSet, valueClass);
    }

    @Override
    boolean add(@Nullable Byte value) {
        return osSet.add(value);
    }

    @Override
    boolean containsInternal(@Nullable Object o) {
        Long value;
        if (o == null) {
            value = null;
        } else {
            value = ((Byte) o).longValue();
        }
        return osSet.contains(value);
    }

    @Override
    boolean removeInternal(@Nullable Object o) {
        // Object has been type-checked from caller
        return osSet.remove((Byte) o);
    }

    @Override
    boolean containsAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        Collection<Number> numberCollection = (Collection<Number>) c;
        NativeRealmAnyCollection collection = NativeRealmAnyCollection.newIntegerCollection(numberCollection);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.CONTAINS_ALL);
    }

    @Override
    boolean addAllInternal(Collection<? extends Byte> c) {
        // Collection has been type-checked from caller
        NativeRealmAnyCollection collection = NativeRealmAnyCollection.newIntegerCollection(c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.ADD_ALL);
    }

    @Override
    boolean removeAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        NativeRealmAnyCollection collection = NativeRealmAnyCollection.newIntegerCollection((Collection<Byte>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.REMOVE_ALL);
    }

    @Override
    boolean retainAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        NativeRealmAnyCollection collection = NativeRealmAnyCollection.newIntegerCollection((Collection<Byte>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.RETAIN_ALL);
    }
}

/**
 * {@link SetValueOperator} targeting {@code float} values in {@link RealmSet}s.
 */
class FloatOperator extends SetValueOperator<Float> {

    FloatOperator(BaseRealm baseRealm, OsSet osSet, Class<Float> valueClass) {
        super(baseRealm, osSet, valueClass);
    }

    @Override
    boolean add(@Nullable Float value) {
        return osSet.add(value);
    }

    @Override
    boolean containsInternal(@Nullable Object o) {
        Float value;
        if (o == null) {
            value = null;
        } else {
            value = (Float) o;
        }
        return osSet.contains(value);
    }

    @Override
    boolean removeInternal(@Nullable Object o) {
        return osSet.remove((Float) o);
    }

    @Override
    boolean containsAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        Collection<Float> floatCollection = (Collection<Float>) c;
        NativeRealmAnyCollection collection = NativeRealmAnyCollection.newFloatCollection(floatCollection);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.CONTAINS_ALL);
    }

    @Override
    boolean addAllInternal(Collection<? extends Float> c) {
        // Collection has been type-checked from caller
        NativeRealmAnyCollection collection = NativeRealmAnyCollection.newFloatCollection(c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.ADD_ALL);
    }

    @Override
    boolean removeAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        NativeRealmAnyCollection collection = NativeRealmAnyCollection.newFloatCollection((Collection<Float>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.REMOVE_ALL);
    }

    @Override
    boolean retainAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        NativeRealmAnyCollection collection = NativeRealmAnyCollection.newFloatCollection((Collection<Float>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.RETAIN_ALL);
    }
}

/**
 * {@link SetValueOperator} targeting {@code double} values in {@link RealmSet}s.
 */
class DoubleOperator extends SetValueOperator<Double> {

    DoubleOperator(BaseRealm baseRealm, OsSet osSet, Class<Double> valueClass) {
        super(baseRealm, osSet, valueClass);
    }

    @Override
    boolean add(@Nullable Double value) {
        return osSet.add(value);
    }

    @Override
    boolean containsInternal(@Nullable Object o) {
        Double value;
        if (o == null) {
            value = null;
        } else {
            value = (Double) o;
        }
        return osSet.contains(value);
    }

    @Override
    boolean removeInternal(@Nullable Object o) {
        return osSet.remove((Double) o);
    }

    @Override
    boolean containsAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        Collection<Double> doubleCollection = (Collection<Double>) c;
        NativeRealmAnyCollection collection = NativeRealmAnyCollection.newDoubleCollection(doubleCollection);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.CONTAINS_ALL);
    }

    @Override
    boolean addAllInternal(Collection<? extends Double> c) {
        // Collection has been type-checked from caller
        NativeRealmAnyCollection collection = NativeRealmAnyCollection.newDoubleCollection(c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.ADD_ALL);
    }

    @Override
    boolean removeAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        NativeRealmAnyCollection collection = NativeRealmAnyCollection.newDoubleCollection((Collection<Double>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.REMOVE_ALL);
    }

    @Override
    boolean retainAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        NativeRealmAnyCollection collection = NativeRealmAnyCollection.newDoubleCollection((Collection<Double>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.RETAIN_ALL);
    }
}

/**
 * {@link SetValueOperator} targeting {@code byte[]} values in {@link RealmSet}s.
 */
class BinaryOperator extends SetValueOperator<byte[]> {

    BinaryOperator(BaseRealm baseRealm, OsSet osSet, Class<byte[]> valueClass) {
        super(baseRealm, osSet, valueClass);
    }

    @Override
    boolean add(@Nullable byte[] value) {
        return osSet.add(value);
    }

    @Override
    boolean containsInternal(@Nullable Object o) {
        byte[] value;
        if (o == null) {
            value = null;
        } else {
            value = (byte[]) o;
        }
        return osSet.contains(value);
    }

    @Override
    boolean removeInternal(@Nullable Object o) {
        return osSet.remove((byte[]) o);
    }

    @Override
    boolean containsAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        Collection<byte[]> binaryCollection = (Collection<byte[]>) c;
        NativeRealmAnyCollection collection = NativeRealmAnyCollection.newBinaryCollection(binaryCollection);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.CONTAINS_ALL);
    }

    @Override
    boolean addAllInternal(Collection<? extends byte[]> c) {
        // Collection has been type-checked from caller
        NativeRealmAnyCollection collection = NativeRealmAnyCollection.newBinaryCollection(c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.ADD_ALL);
    }

    @Override
    boolean removeAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        NativeRealmAnyCollection collection = NativeRealmAnyCollection.newBinaryCollection((Collection<byte[]>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.REMOVE_ALL);
    }

    @Override
    boolean retainAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        NativeRealmAnyCollection collection = NativeRealmAnyCollection.newBinaryCollection((Collection<byte[]>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.RETAIN_ALL);
    }
}

/**
 * {@link SetValueOperator} targeting {@link Date} values in {@link RealmSet}s.
 */
class DateOperator extends SetValueOperator<Date> {

    DateOperator(BaseRealm baseRealm, OsSet osSet, Class<Date> valueClass) {
        super(baseRealm, osSet, valueClass);
    }

    @Override
    boolean add(@Nullable Date value) {
        return osSet.add(value);
    }

    @Override
    boolean containsInternal(@Nullable Object o) {
        Date value;
        if (o == null) {
            value = null;
        } else {
            value = (Date) o;
        }
        return osSet.contains(value);
    }

    @Override
    boolean removeInternal(@Nullable Object o) {
        return osSet.remove((Date) o);
    }

    @Override
    boolean containsAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        Collection<Date> dateCollection = (Collection<Date>) c;
        NativeRealmAnyCollection collection = NativeRealmAnyCollection.newDateCollection(dateCollection);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.CONTAINS_ALL);
    }

    @Override
    boolean addAllInternal(Collection<? extends Date> c) {
        // Collection has been type-checked from caller
        NativeRealmAnyCollection collection = NativeRealmAnyCollection.newDateCollection(c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.ADD_ALL);
    }

    @Override
    boolean removeAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        NativeRealmAnyCollection collection = NativeRealmAnyCollection.newDateCollection((Collection<Date>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.REMOVE_ALL);
    }

    @Override
    boolean retainAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        NativeRealmAnyCollection collection = NativeRealmAnyCollection.newDateCollection((Collection<Date>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.RETAIN_ALL);
    }
}

/**
 * {@link SetValueOperator} targeting {@link Decimal128} values in {@link RealmSet}s.
 */
class Decimal128Operator extends SetValueOperator<Decimal128> {

    Decimal128Operator(BaseRealm baseRealm, OsSet osSet, Class<Decimal128> valueClass) {
        super(baseRealm, osSet, valueClass);
    }

    @Override
    boolean add(@Nullable Decimal128 value) {
        return osSet.add(value);
    }

    @Override
    boolean containsInternal(@Nullable Object o) {
        Decimal128 value;
        if (o == null) {
            value = null;
        } else {
            value = (Decimal128) o;
        }
        return osSet.contains(value);
    }

    @Override
    boolean removeInternal(@Nullable Object o) {
        return osSet.remove((Decimal128) o);
    }

    @Override
    boolean containsAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        Collection<Decimal128> decimal128Collection = (Collection<Decimal128>) c;
        NativeRealmAnyCollection collection = NativeRealmAnyCollection.newDecimal128Collection(decimal128Collection);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.CONTAINS_ALL);
    }

    @Override
    boolean addAllInternal(Collection<? extends Decimal128> c) {
        // Collection has been type-checked from caller
        NativeRealmAnyCollection collection = NativeRealmAnyCollection.newDecimal128Collection(c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.ADD_ALL);
    }

    @Override
    boolean removeAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        NativeRealmAnyCollection collection = NativeRealmAnyCollection.newDecimal128Collection((Collection<Decimal128>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.REMOVE_ALL);
    }

    @Override
    boolean retainAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        NativeRealmAnyCollection collection = NativeRealmAnyCollection.newDecimal128Collection((Collection<Decimal128>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.RETAIN_ALL);
    }
}

/**
 * {@link SetValueOperator} targeting {@link ObjectId} values in {@link RealmSet}s.
 */
class ObjectIdOperator extends SetValueOperator<ObjectId> {

    ObjectIdOperator(BaseRealm baseRealm, OsSet osSet, Class<ObjectId> valueClass) {
        super(baseRealm, osSet, valueClass);
    }

    @Override
    boolean add(@Nullable ObjectId value) {
        return osSet.add(value);
    }

    @Override
    boolean containsInternal(@Nullable Object o) {
        ObjectId value;
        if (o == null) {
            value = null;
        } else {
            value = (ObjectId) o;
        }
        return osSet.contains(value);
    }

    @Override
    boolean removeInternal(@Nullable Object o) {
        return osSet.remove((ObjectId) o);
    }

    @Override
    boolean containsAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        Collection<ObjectId> objectIdCollection = (Collection<ObjectId>) c;
        NativeRealmAnyCollection collection = NativeRealmAnyCollection.newObjectIdCollection(objectIdCollection);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.CONTAINS_ALL);
    }

    @Override
    boolean addAllInternal(Collection<? extends ObjectId> c) {
        // Collection has been type-checked from caller
        NativeRealmAnyCollection collection = NativeRealmAnyCollection.newObjectIdCollection(c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.ADD_ALL);
    }

    @Override
    boolean removeAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        NativeRealmAnyCollection collection = NativeRealmAnyCollection.newObjectIdCollection((Collection<ObjectId>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.REMOVE_ALL);
    }

    @Override
    boolean retainAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        NativeRealmAnyCollection collection = NativeRealmAnyCollection.newObjectIdCollection((Collection<ObjectId>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.RETAIN_ALL);
    }
}

/**
 * {@link SetValueOperator} targeting {@link UUID} values in {@link RealmSet}s.
 */
class UUIDOperator extends SetValueOperator<UUID> {

    UUIDOperator(BaseRealm baseRealm, OsSet osSet, Class<UUID> valueClass) {
        super(baseRealm, osSet, valueClass);
    }

    @Override
    boolean add(@Nullable UUID value) {
        return osSet.add(value);
    }

    @Override
    boolean containsInternal(@Nullable Object o) {
        UUID value;
        if (o == null) {
            value = null;
        } else {
            value = (UUID) o;
        }
        return osSet.contains(value);
    }

    @Override
    boolean removeInternal(@Nullable Object o) {
        // Object has been type-checked from caller
        return osSet.remove((UUID) o);
    }

    @Override
    boolean containsAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        Collection<UUID> uuidCollection = (Collection<UUID>) c;
        NativeRealmAnyCollection collection = NativeRealmAnyCollection.newUUIDCollection(uuidCollection);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.CONTAINS_ALL);
    }

    @Override
    boolean addAllInternal(Collection<? extends UUID> c) {
        // Collection has been type-checked from caller
        NativeRealmAnyCollection collection = NativeRealmAnyCollection.newUUIDCollection(c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.ADD_ALL);
    }

    @Override
    boolean removeAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        NativeRealmAnyCollection collection = NativeRealmAnyCollection.newUUIDCollection((Collection<UUID>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.REMOVE_ALL);
    }

    @Override
    boolean retainAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        NativeRealmAnyCollection collection = NativeRealmAnyCollection.newUUIDCollection((Collection<UUID>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.RETAIN_ALL);
    }
}

/**
 * {@link SetValueOperator} targeting {@link RealmModel} values in {@link RealmSet}s.
 */
class RealmModelSetOperator<T extends RealmModel> extends SetValueOperator<T> {

    RealmModelSetOperator(BaseRealm baseRealm, OsSet osSet, Class<T> valueClass) {
        super(baseRealm, osSet, valueClass);
    }

    @Override
    boolean add(T value) {
        // Realm model sets cannot contain null values
        RealmObjectProxy proxy = (RealmObjectProxy) getManagedObject(value);
        Row row$realm = proxy.realmGet$proxyState().getRow$realm();
        return osSet.addRow(row$realm.getObjectKey());
    }

    private T getManagedObject(T value) {
        if (value == null) {
            throw new NullPointerException("This set does not permit null values.");
        }
        // Check we can add this object into the Realm
        boolean copyObject = CollectionUtils.checkCanObjectBeCopied(baseRealm, value, valueClass.getName(), SET_TYPE);

        // Add value into set
        //noinspection unchecked
        return (T) ((copyObject) ? CollectionUtils.copyToRealm(baseRealm, (RealmModel) value) : value);
    }

    /**
     * Check that object is a valid and managed object by the set's Realm.
     *
     * @param value model object
     */
    private void checkValidObject(RealmModel value) {
        // Realm model sets cannot contain null values
        if (value == null) {
            throw new NullPointerException("This set does not permit null values.");
        }
        if (!RealmObject.isValid(value) || !RealmObject.isManaged(value)) {
            throw new IllegalArgumentException("'value' is not a valid managed object.");
        }
        if (((RealmObjectProxy) value).realmGet$proxyState().getRealm$realm() != baseRealm) {
            throw new IllegalArgumentException("'value' belongs to a different Realm.");
        }
    }

    @Override
    boolean containsInternal(Object value) {
        checkValidObject((RealmModel) value);
        Row row$realm = ((RealmObjectProxy) value).realmGet$proxyState().getRow$realm();
        return osSet.containsRow(row$realm.getObjectKey());
    }

    @Override
    boolean removeInternal(Object value) {
        checkValidObject((RealmModel) value);
        Row row$realm = ((RealmObjectProxy) value).realmGet$proxyState().getRow$realm();
        return osSet.removeRow(row$realm.getObjectKey());
    }

    @Override
    boolean containsAllInternal(Collection<?> collection) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        Collection<T> realmModelCollection = (Collection<T>) collection;
        // All models must be managed and from the same set's Realm
        checkValidCollection(realmModelCollection);
        NativeRealmAnyCollection realmAnyCollection = NativeRealmAnyCollection.newRealmModelCollection(realmModelCollection);
        return osSet.collectionFunnel(realmAnyCollection, OsSet.ExternalCollectionOperation.CONTAINS_ALL);
    }

    @Override
    boolean addAllInternal(Collection<? extends T> collection) {
        // Collection has been type-checked from caller
        // Use add method as it contains all the necessary checks
        List<T> managedRealmObjectCollection = new ArrayList<>(collection.size());
        for (T item : collection) {
            managedRealmObjectCollection.add(getManagedObject(item));
        }
        NativeRealmAnyCollection realmAnyCollection = NativeRealmAnyCollection.newRealmModelCollection(managedRealmObjectCollection);
        return osSet.collectionFunnel(realmAnyCollection, OsSet.ExternalCollectionOperation.ADD_ALL);
    }

    @Override
    boolean removeAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        Collection<T> realmModelCollection = (Collection<T>) c;
        checkValidCollection(realmModelCollection);
        NativeRealmAnyCollection collection = NativeRealmAnyCollection.newRealmModelCollection(realmModelCollection);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.REMOVE_ALL);
    }

    @Override
    boolean retainAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        Collection<T> realmModelCollection = (Collection<T>) c;
        checkValidCollection(realmModelCollection);
        NativeRealmAnyCollection collection = NativeRealmAnyCollection.newRealmModelCollection(realmModelCollection);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.RETAIN_ALL);
    }

    private void checkValidCollection(Collection<? extends T> collection) {
        for (T object : collection) {
            checkValidObject(object);
        }
    }

    @Override
    RealmQuery<T> where() {
        return new RealmQuery<>(baseRealm, osSet, valueClass);
    }
}

/**
 * {@link SetValueOperator} targeting {@link RealmAny} values in {@link RealmSet}s.
 */
class RealmAnySetOperator extends SetValueOperator<RealmAny> {

    RealmAnySetOperator(BaseRealm baseRealm, OsSet osSet, Class<RealmAny> valueClass) {
        super(baseRealm, osSet, valueClass);
    }

    @Override
    boolean add(@Nullable RealmAny value) {
        value = getManagedRealmAny(value);
        return osSet.addRealmAny(value.getNativePtr());
    }

    @NotNull
    private RealmAny getManagedRealmAny(@Nullable RealmAny value) {
        if (value == null) {
            value = RealmAny.nullValue();
        } else if (value.getType() == RealmAnyType.OBJECT) {
            RealmModel realmModel = value.asRealmModel(RealmModel.class);
            boolean copyObject = CollectionUtils.checkCanObjectBeCopied(baseRealm, realmModel, valueClass.getName(), SET_TYPE);
            RealmObjectProxy proxy = (RealmObjectProxy) ((copyObject) ? CollectionUtils.copyToRealm(baseRealm, realmModel) : realmModel);
            value = RealmAny.valueOf(proxy);
        }
        return value;
    }

    @Override
    boolean containsInternal(@Nullable Object o) {
        RealmAny value;
        if (o == null) {
            value = RealmAny.nullValue();
        } else {
            value = (RealmAny) o;
        }
        checkValidObject(value);
        return osSet.containsRealmAny(value.getNativePtr());
    }

    @Override
    boolean removeInternal(@Nullable Object o) {
        // Object has been type-checked from caller
        RealmAny value;
        if (o == null) {
            value = RealmAny.nullValue();
        } else {
            value = (RealmAny) o;
        }
        checkValidObject(value);
        return osSet.removeRealmAny(value.getNativePtr());
    }

    @NotNull
    private NativeRealmAnyCollection getNativeRealmAnyCollection(Collection<? extends RealmAny> realmAnyCollection) {
        long[] realmAnyPtrs = new long[realmAnyCollection.size()];
        boolean[] notNull = new boolean[realmAnyCollection.size()];

        int i = 0;
        for (RealmAny realmAny : realmAnyCollection) {
            if (realmAny != null) {
                checkValidObject(realmAny);
                realmAnyPtrs[i] = realmAny.getNativePtr();
                notNull[i] = true;
            }
            i++;
        }

        return NativeRealmAnyCollection.newRealmAnyCollection(realmAnyPtrs, notNull);
    }

    @Override
    boolean containsAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        NativeRealmAnyCollection collection = getNativeRealmAnyCollection((Collection<RealmAny>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.CONTAINS_ALL);
    }

    @Override
    boolean addAllInternal(Collection<? extends RealmAny> collection) {
        // Collection has been type-checked from caller
        List<RealmAny> managedRealmAnyCollection = new ArrayList<>(collection.size());
        for (RealmAny realmAny : collection) {
            managedRealmAnyCollection.add(getManagedRealmAny(realmAny));
        }
        NativeRealmAnyCollection nativeRealmAnyCollection = getNativeRealmAnyCollection(managedRealmAnyCollection);
        return osSet.collectionFunnel(nativeRealmAnyCollection, OsSet.ExternalCollectionOperation.ADD_ALL);
    }

    @Override
    boolean removeAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        NativeRealmAnyCollection collection = getNativeRealmAnyCollection((Collection<RealmAny>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.REMOVE_ALL);
    }

    @Override
    boolean retainAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        NativeRealmAnyCollection collection = getNativeRealmAnyCollection((Collection<RealmAny>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.RETAIN_ALL);
    }

    private void checkValidObject(RealmAny realmAny) {
        try {
            realmAny.checkValidObject(baseRealm);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("RealmAny collection contains unmanaged objects.", exception);
        }
    }
}

/**
 * Base iterator for {@link RealmSet}s.
 *
 * @param <E> the value type.
 */
abstract class SetIterator<E> implements Iterator<E> {

    protected final OsSet osSet;
    protected final BaseRealm baseRealm;

    private int pos = -1;

    SetIterator(OsSet osSet, BaseRealm baseRealm) {
        this.osSet = osSet;
        this.baseRealm = baseRealm;
    }

    @Override
    public boolean hasNext() {
        return pos + 1 < osSet.size();
    }

    @Override
    public E next() {
        pos++;
        long size = osSet.size();
        if (pos >= size) {
            throw new NoSuchElementException("Cannot access index " + pos + " when size is " + size +
                    ". Remember to check hasNext() before using next().");
        }

        return getValueAtIndex(pos);
    }

    // Some types might want to override this to convert/typecast the value correctly
    protected E getValueAtIndex(int position) {
        //noinspection unchecked
        return (E) osSet.getValueAtIndex(position);
    }
}

/**
 * Set iterator for {@code boolean} values.
 */
class BooleanSetIterator extends SetIterator<Boolean> {
    BooleanSetIterator(OsSet osSet, BaseRealm baseRealm) {
        super(osSet, baseRealm);
    }
}

/**
 * Set iterator for {@link String} values.
 */
class StringSetIterator extends SetIterator<String> {
    StringSetIterator(OsSet osSet, BaseRealm baseRealm) {
        super(osSet, baseRealm);
    }
}

/**
 * Set iterator for {@code int} values.
 */
class IntegerSetIterator extends SetIterator<Integer> {
    IntegerSetIterator(OsSet osSet, BaseRealm baseRealm) {
        super(osSet, baseRealm);
    }

    @Override
    protected Integer getValueAtIndex(int position) {
        Object value = osSet.getValueAtIndex(position);
        if (value == null) {
            return null;
        }

        Long valueAtIndex = (Long) value;
        return valueAtIndex.intValue();
    }
}

/**
 * Set iterator for {@code long} values.
 */
class LongSetIterator extends SetIterator<Long> {
    LongSetIterator(OsSet osSet, BaseRealm baseRealm) {
        super(osSet, baseRealm);
    }
}

/**
 * Set iterator for {@code short} values.
 */
class ShortSetIterator extends SetIterator<Short> {
    ShortSetIterator(OsSet osSet, BaseRealm baseRealm) {
        super(osSet, baseRealm);
    }

    @Override
    protected Short getValueAtIndex(int position) {
        Object value = osSet.getValueAtIndex(position);
        if (value == null) {
            return null;
        }

        Long longValue = (Long) value;
        return longValue.shortValue();
    }
}

/**
 * Set iterator for {@code byte} values.
 */
class ByteSetIterator extends SetIterator<Byte> {
    ByteSetIterator(OsSet osSet, BaseRealm baseRealm) {
        super(osSet, baseRealm);
    }

    @Override
    protected Byte getValueAtIndex(int position) {
        Object value = osSet.getValueAtIndex(position);
        if (value == null) {
            return null;
        }

        Long longValue = (Long) value;
        return longValue.byteValue();
    }
}

/**
 * Set iterator for {@code float} values.
 */
class FloatSetIterator extends SetIterator<Float> {
    FloatSetIterator(OsSet osSet, BaseRealm baseRealm) {
        super(osSet, baseRealm);
    }
}

/**
 * Set iterator for {@code double} values.
 */
class DoubleSetIterator extends SetIterator<Double> {
    DoubleSetIterator(OsSet osSet, BaseRealm baseRealm) {
        super(osSet, baseRealm);
    }
}

/**
 * Set iterator for {@code byte[]} values.
 */
class BinarySetIterator extends SetIterator<byte[]> {
    BinarySetIterator(OsSet osSet, BaseRealm baseRealm) {
        super(osSet, baseRealm);
    }

    @Override
    protected byte[] getValueAtIndex(int position) {
        Object value = osSet.getValueAtIndex(position);
        if (value == null) {
            return null;
        }

        return (byte[]) value;
    }
}

/**
 * Set iterator for {@link Date} values.
 */
class DateSetIterator extends SetIterator<Date> {
    DateSetIterator(OsSet osSet, BaseRealm baseRealm) {
        super(osSet, baseRealm);
    }
}

/**
 * Set iterator for {@link Decimal128} values.
 */
class Decimal128SetIterator extends SetIterator<Decimal128> {
    Decimal128SetIterator(OsSet osSet, BaseRealm baseRealm) {
        super(osSet, baseRealm);
    }
}

/**
 * Set iterator for {@link ObjectId} values.
 */
class ObjectIdSetIterator extends SetIterator<ObjectId> {
    ObjectIdSetIterator(OsSet osSet, BaseRealm baseRealm) {
        super(osSet, baseRealm);
    }
}

/**
 * Set iterator for {@link UUID} values.
 */
class UUIDSetIterator extends SetIterator<UUID> {
    UUIDSetIterator(OsSet osSet, BaseRealm baseRealm) {
        super(osSet, baseRealm);
    }
}

/**
 * Set iterator for {@link RealmAny} values.
 */
class RealmAnySetIterator extends SetIterator<RealmAny> {
    RealmAnySetIterator(OsSet osSet, BaseRealm baseRealm) {
        super(osSet, baseRealm);
    }

    @Override
    protected RealmAny getValueAtIndex(int position) {
        NativeRealmAny nativeRealmAny = new NativeRealmAny(osSet.getRealmAny(position));
        return new RealmAny(RealmAnyOperator.fromNativeRealmAny(baseRealm, nativeRealmAny));
    }
}

/**
 * Set iterator for {@link RealmModel} values.
 */
class RealmModelSetIterator<T extends RealmModel> extends SetIterator<T> {

    private final Class<T> valueClass;

    RealmModelSetIterator(OsSet osSet, BaseRealm baseRealm, Class<T> valueClass) {
        super(osSet, baseRealm);
        this.valueClass = valueClass;
    }

    @Override
    protected T getValueAtIndex(int position) {
        long rowPtr = osSet.getRow(position);
        return baseRealm.get(valueClass, rowPtr, false, new ArrayList<>());
    }
}
