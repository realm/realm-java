package io.realm;

import org.bson.types.ObjectId;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.UUID;

import javax.annotation.Nullable;

import io.realm.internal.OsSet;
import io.realm.internal.core.NativeMixedCollection;

/**
 * TODO
 *
 * @param <E>
 */
abstract class SetValueOperator<E> {

    protected final BaseRealm baseRealm;
    protected final OsSet osSet;
    protected final Class<E> valueClass;

    public SetValueOperator(BaseRealm baseRealm, OsSet osSet, Class<E> valueClass) {
        this.baseRealm = baseRealm;
        this.osSet = osSet;
        this.valueClass = valueClass;
    }

    abstract boolean add(@Nullable E value);

    abstract boolean containsInternal(@Nullable Object o);

    abstract boolean remove(@Nullable Object o);

    abstract boolean containsAllInternal(Collection<?> c);

    abstract boolean addAllInternal(Collection<? extends E> c);

    abstract boolean removeAllInternal(Collection<?> c);

    abstract boolean retainAllInternal(Collection<?> c);

    boolean contains(@Nullable Object o) {
        // Return false when passing something else than the correct type
        if (o != null && o.getClass() != valueClass) {
            return false;
        }
        return containsInternal(o);
    }

    boolean containsAll(Collection<?> c) {
        if (isRealmCollection(c)) {
            OsSet otherOsSet = ((RealmSet<?>) c).getOsSet();
            return funnelCollection(otherOsSet, OsSet.ExternalCollectionOperation.CONTAINS_ALL);
        }
        return containsAllInternal(c);
    }

    boolean addAll(Collection<? extends E> c) {
        if (isRealmCollection(c)) {
            OsSet otherOsSet = ((RealmSet<?>) c).getOsSet();
            return funnelCollection(otherOsSet, OsSet.ExternalCollectionOperation.ADD_ALL);
        }
        return addAllInternal(c);
    }

    boolean removeAll(Collection<?> c) {
        if (isRealmCollection(c)) {
            OsSet otherOsSet = ((RealmSet<?>) c).getOsSet();
            return funnelCollection(otherOsSet, OsSet.ExternalCollectionOperation.REMOVE_ALL);
        }
        return removeAllInternal(c);
    }

    boolean retainAll(Collection<?> c) {
        if (isRealmCollection(c)) {
            OsSet otherOsSet = ((RealmSet<?>) c).getOsSet();
            return funnelCollection(otherOsSet, OsSet.ExternalCollectionOperation.RETAIN_ALL);
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

    public void clear() {
        osSet.clear();
    }

    public RealmSet<E> freeze() {
        BaseRealm frozenRealm = baseRealm.freeze();
        OsSet frozenOsSet = osSet.freeze(frozenRealm.sharedRealm);
        return new RealmSet<>(frozenRealm, frozenOsSet, valueClass);
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
        } else if (valueClass == ObjectId.class) {
            return (SetIterator<T>) new ObjectIdSetIterator(osSet, baseRealm);
        } else if (valueClass == UUID.class) {
            return (SetIterator<T>) new UUIDSetIterator(osSet, baseRealm);
        } else {
            throw new IllegalArgumentException("Unknown class for iterator: " + valueClass.getSimpleName());
        }
    }
}

/**
 * TODO
 */
class BooleanOperator extends SetValueOperator<Boolean> {

    public BooleanOperator(BaseRealm baseRealm, OsSet osSet, Class<Boolean> valueClass) {
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
    boolean remove(@Nullable Object o) {
        return osSet.remove((Boolean) o);
    }

    @Override
    boolean containsAllInternal(Collection<?> c) {
        for (Object value : c) {
            if (value != null && !(value instanceof Boolean)) {
                return false;
            }
        }

        //noinspection unchecked
        Collection<Boolean> booleanCollection = (Collection<Boolean>) c;
        NativeMixedCollection collection = NativeMixedCollection.newBooleanCollection(booleanCollection);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.CONTAINS_ALL);
    }

    @Override
    boolean addAllInternal(Collection<? extends Boolean> c) {
        for (Object value : c) {
            if (value != null && !(value instanceof Boolean)) {
                throw new IllegalArgumentException("Invalid collection type. Set and collection must contain the same type of elements.");
            }
        }

        //noinspection unchecked
        NativeMixedCollection collection = NativeMixedCollection.newBooleanCollection((Collection<Boolean>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.ADD_ALL);
    }

    @Override
    boolean removeAllInternal(Collection<?> c) {
        //noinspection unchecked
        NativeMixedCollection collection = NativeMixedCollection.newBooleanCollection((Collection<Boolean>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.REMOVE_ALL);
    }

    @Override
    boolean retainAllInternal(Collection<?> c) {
        //noinspection unchecked
        NativeMixedCollection collection = NativeMixedCollection.newBooleanCollection((Collection<Boolean>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.RETAIN_ALL);
    }
}

/**
 * TODO
 */
class StringOperator extends SetValueOperator<String> {

    public StringOperator(BaseRealm baseRealm, OsSet osSet, Class<String> valueClass) {
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
    boolean remove(@Nullable Object o) {
        return osSet.remove((String) o);
    }

    @Override
    boolean containsAllInternal(Collection<?> c) {
        for (Object value : c) {
            if (value != null && !(value instanceof String)) {
                return false;
            }
        }

        //noinspection unchecked
        Collection<String> stringCollection = (Collection<String>) c;
        NativeMixedCollection collection = NativeMixedCollection.newStringCollection(stringCollection);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.CONTAINS_ALL);
    }

    @Override
    boolean addAllInternal(Collection<? extends String> c) {
        for (Object value : c) {
            if (value != null && !(value instanceof String)) {
                throw new IllegalArgumentException("Invalid collection type. Set and collection must contain the same type of elements.");
            }
        }

        //noinspection unchecked
        NativeMixedCollection collection = NativeMixedCollection.newStringCollection((Collection<String>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.ADD_ALL);
    }

    @Override
    boolean removeAllInternal(Collection<?> c) {
        //noinspection unchecked
        NativeMixedCollection collection = NativeMixedCollection.newStringCollection((Collection<String>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.REMOVE_ALL);
    }

    @Override
    boolean retainAllInternal(Collection<?> c) {
        //noinspection unchecked
        NativeMixedCollection collection = NativeMixedCollection.newStringCollection((Collection<String>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.RETAIN_ALL);
    }
}

/**
 * TODO
 */
class IntegerOperator extends SetValueOperator<Integer> {

    public IntegerOperator(BaseRealm baseRealm, OsSet osSet, Class<Integer> valueClass) {
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
    boolean remove(@Nullable Object o) {
        return osSet.remove((Integer) o);
    }

    @Override
    boolean containsAllInternal(Collection<?> c) {
        for (Object value : c) {
            if (value != null && !(value instanceof Number)) {
                return false;
            }
        }

        //noinspection unchecked
        Collection<Number> numberCollection = (Collection<Number>) c;
        NativeMixedCollection collection = NativeMixedCollection.newIntegerCollection(numberCollection);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.CONTAINS_ALL);
    }

    @Override
    boolean addAllInternal(Collection<? extends Integer> c) {
        for (Object value : c) {
            if (value != null && !(value instanceof Integer)) {
                throw new IllegalArgumentException("Invalid collection type. Set and collection must contain the same type of elements.");
            }
        }

        NativeMixedCollection collection = NativeMixedCollection.newIntegerCollection(c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.ADD_ALL);
    }

    @Override
    boolean removeAllInternal(Collection<?> c) {
        //noinspection unchecked
        NativeMixedCollection collection = NativeMixedCollection.newIntegerCollection((Collection<Integer>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.REMOVE_ALL);
    }

    @Override
    boolean retainAllInternal(Collection<?> c) {
        //noinspection unchecked
        NativeMixedCollection collection = NativeMixedCollection.newIntegerCollection((Collection<Integer>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.RETAIN_ALL);
    }
}

/**
 * TODO
 */
class LongOperator extends SetValueOperator<Long> {

    public LongOperator(BaseRealm baseRealm, OsSet osSet, Class<Long> valueClass) {
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
    boolean remove(@Nullable Object o) {
        return osSet.remove((Long) o);
    }

    @Override
    boolean containsAllInternal(Collection<?> c) {
        for (Object value : c) {
            if (value != null && !(value instanceof Number)) {
                return false;
            }
        }

        //noinspection unchecked
        NativeMixedCollection collection = NativeMixedCollection.newIntegerCollection((Collection<Number>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.CONTAINS_ALL);
    }

    @Override
    boolean addAllInternal(Collection<? extends Long> c) {
        for (Object value : c) {
            if (value != null && !(value instanceof Long)) {
                throw new IllegalArgumentException("Invalid collection type. Set and collection must contain the same type of elements.");
            }
        }

        NativeMixedCollection collection = NativeMixedCollection.newIntegerCollection(c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.ADD_ALL);
    }

    @Override
    boolean removeAllInternal(Collection<?> c) {
        //noinspection unchecked
        NativeMixedCollection collection = NativeMixedCollection.newIntegerCollection((Collection<Long>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.REMOVE_ALL);
    }

    @Override
    boolean retainAllInternal(Collection<?> c) {
        //noinspection unchecked
        NativeMixedCollection collection = NativeMixedCollection.newIntegerCollection((Collection<Long>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.RETAIN_ALL);
    }
}

/**
 * TODO
 */
class ShortOperator extends SetValueOperator<Short> {

    public ShortOperator(BaseRealm baseRealm, OsSet osSet, Class<Short> valueClass) {
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
    boolean remove(@Nullable Object o) {
        return osSet.remove((Short) o);
    }

    @Override
    boolean containsAllInternal(Collection<?> c) {
        for (Object value : c) {
            if (value != null && !(value instanceof Number)) {
                return false;
            }
        }

        //noinspection unchecked
        Collection<Number> numberCollection = (Collection<Number>) c;
        NativeMixedCollection collection = NativeMixedCollection.newIntegerCollection(numberCollection);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.CONTAINS_ALL);
    }

    @Override
    boolean addAllInternal(Collection<? extends Short> c) {
        for (Object value : c) {
            if (value != null && !(value instanceof Short)) {
                throw new IllegalArgumentException("Invalid collection type. Set and collection must contain the same type of elements.");
            }
        }

        NativeMixedCollection collection = NativeMixedCollection.newIntegerCollection(c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.ADD_ALL);
    }

    @Override
    boolean removeAllInternal(Collection<?> c) {
        //noinspection unchecked
        NativeMixedCollection collection = NativeMixedCollection.newIntegerCollection((Collection<Short>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.REMOVE_ALL);
    }

    @Override
    boolean retainAllInternal(Collection<?> c) {
        //noinspection unchecked
        NativeMixedCollection collection = NativeMixedCollection.newIntegerCollection((Collection<Short>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.RETAIN_ALL);
    }
}

/**
 * TODO
 */
class ByteOperator extends SetValueOperator<Byte> {

    public ByteOperator(BaseRealm baseRealm, OsSet osSet, Class<Byte> valueClass) {
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
    boolean remove(@Nullable Object o) {
        return osSet.remove((Byte) o);
    }

    @Override
    boolean containsAllInternal(Collection<?> c) {
        for (Object value : c) {
            if (value != null && !(value instanceof Number)) {
                return false;
            }
        }

        //noinspection unchecked
        Collection<Number> numberCollection = (Collection<Number>) c;
        NativeMixedCollection collection = NativeMixedCollection.newIntegerCollection(numberCollection);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.CONTAINS_ALL);
    }

    @Override
    boolean addAllInternal(Collection<? extends Byte> c) {
        for (Object value : c) {
            if (value != null && !(value instanceof Byte)) {
                throw new IllegalArgumentException("Invalid collection type. Set and collection must contain the same type of elements.");
            }
        }

        NativeMixedCollection collection = NativeMixedCollection.newIntegerCollection(c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.ADD_ALL);
    }

    @Override
    boolean removeAllInternal(Collection<?> c) {
        //noinspection unchecked
        NativeMixedCollection collection = NativeMixedCollection.newIntegerCollection((Collection<Byte>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.REMOVE_ALL);
    }

    @Override
    boolean retainAllInternal(Collection<?> c) {
        //noinspection unchecked
        NativeMixedCollection collection = NativeMixedCollection.newIntegerCollection((Collection<Byte>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.RETAIN_ALL);
    }
}

/**
 * TODO
 */
class FloatOperator extends SetValueOperator<Float> {

    public FloatOperator(BaseRealm baseRealm, OsSet osSet, Class<Float> valueClass) {
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
    boolean remove(@Nullable Object o) {
        return osSet.remove((Float) o);
    }

    @Override
    boolean containsAllInternal(Collection<?> c) {
        for (Object value : c) {
            if (value != null && !(value instanceof Float)) {
                return false;
            }
        }

        //noinspection unchecked
        Collection<Float> floatCollection = (Collection<Float>) c;
        NativeMixedCollection collection = NativeMixedCollection.newFloatCollection(floatCollection);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.CONTAINS_ALL);
    }

    @Override
    boolean addAllInternal(Collection<? extends Float> c) {
        for (Object value : c) {
            if (value != null && !(value instanceof Float)) {
                throw new IllegalArgumentException("Invalid collection type. Set and collection must contain the same type of elements.");
            }
        }

        NativeMixedCollection collection = NativeMixedCollection.newFloatCollection(c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.ADD_ALL);
    }

    @Override
    boolean removeAllInternal(Collection<?> c) {
        //noinspection unchecked
        NativeMixedCollection collection = NativeMixedCollection.newFloatCollection((Collection<Float>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.REMOVE_ALL);
    }

    @Override
    boolean retainAllInternal(Collection<?> c) {
        //noinspection unchecked
        NativeMixedCollection collection = NativeMixedCollection.newFloatCollection((Collection<Float>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.RETAIN_ALL);
    }
}

/**
 * TODO
 */
class DoubleOperator extends SetValueOperator<Double> {

    public DoubleOperator(BaseRealm baseRealm, OsSet osSet, Class<Double> valueClass) {
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
    boolean remove(@Nullable Object o) {
        return osSet.remove((Double) o);
    }

    @Override
    boolean containsAllInternal(Collection<?> c) {
        for (Object value : c) {
            if (value != null && !(value instanceof Double)) {
                return false;
            }
        }

        //noinspection unchecked
        Collection<Double> doubleCollection = (Collection<Double>) c;
        NativeMixedCollection collection = NativeMixedCollection.newDoubleCollection(doubleCollection);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.CONTAINS_ALL);
    }

    @Override
    boolean addAllInternal(Collection<? extends Double> c) {
        for (Object value : c) {
            if (value != null && !(value instanceof Double)) {
                throw new IllegalArgumentException("Invalid collection type. Set and collection must contain the same type of elements.");
            }
        }

        NativeMixedCollection collection = NativeMixedCollection.newDoubleCollection(c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.ADD_ALL);
    }

    @Override
    boolean removeAllInternal(Collection<?> c) {
        //noinspection unchecked
        NativeMixedCollection collection = NativeMixedCollection.newDoubleCollection((Collection<Double>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.REMOVE_ALL);
    }

    @Override
    boolean retainAllInternal(Collection<?> c) {
        //noinspection unchecked
        NativeMixedCollection collection = NativeMixedCollection.newDoubleCollection((Collection<Double>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.RETAIN_ALL);
    }
}

/**
 * TODO
 */
class BinaryOperator extends SetValueOperator<byte[]> {

    public BinaryOperator(BaseRealm baseRealm, OsSet osSet, Class<byte[]> valueClass) {
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
    boolean remove(@Nullable Object o) {
        return osSet.remove((byte[]) o);
    }

    @Override
    boolean containsAllInternal(Collection<?> c) {
        for (Object value : c) {
            if (value != null && !(value instanceof byte[])) {
                return false;
            }
        }

        //noinspection unchecked
        Collection<byte[]> binaryCollection = (Collection<byte[]>) c;
        NativeMixedCollection collection = NativeMixedCollection.newBinaryCollection(binaryCollection);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.CONTAINS_ALL);
    }

    @Override
    boolean addAllInternal(Collection<? extends byte[]> c) {
        for (Object value : c) {
            if (value != null && !(value instanceof byte[])) {
                throw new IllegalArgumentException("Invalid collection type. Set and collection must contain the same type of elements.");
            }
        }

        NativeMixedCollection collection = NativeMixedCollection.newBinaryCollection(c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.ADD_ALL);
    }

    @Override
    boolean removeAllInternal(Collection<?> c) {
        //noinspection unchecked
        NativeMixedCollection collection = NativeMixedCollection.newBinaryCollection((Collection<byte[]>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.REMOVE_ALL);
    }

    @Override
    boolean retainAllInternal(Collection<?> c) {
        //noinspection unchecked
        NativeMixedCollection collection = NativeMixedCollection.newBinaryCollection((Collection<byte[]>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.RETAIN_ALL);
    }
}

/**
 * TODO
 */
class ObjectIdOperator extends SetValueOperator<ObjectId> {

    public ObjectIdOperator(BaseRealm baseRealm, OsSet osSet, Class<ObjectId> valueClass) {
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
    boolean remove(@Nullable Object o) {
        return osSet.remove((ObjectId) o);
    }

    @Override
    boolean containsAllInternal(Collection<?> c) {
        for (Object value : c) {
            if (value != null && !(value instanceof ObjectId)) {
                return false;
            }
        }

        //noinspection unchecked
        Collection<ObjectId> objectIdCollection = (Collection<ObjectId>) c;
        NativeMixedCollection collection = NativeMixedCollection.newObjectIdCollection(objectIdCollection);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.CONTAINS_ALL);
    }

    @Override
    boolean addAllInternal(Collection<? extends ObjectId> c) {
        for (Object value : c) {
            if (value != null && !(value instanceof ObjectId)) {
                throw new IllegalArgumentException("Invalid collection type. Set and collection must contain the same type of elements.");
            }
        }

        NativeMixedCollection collection = NativeMixedCollection.newObjectIdCollection(c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.ADD_ALL);
    }

    @Override
    boolean removeAllInternal(Collection<?> c) {
        //noinspection unchecked
        NativeMixedCollection collection = NativeMixedCollection.newObjectIdCollection((Collection<ObjectId>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.REMOVE_ALL);
    }

    @Override
    boolean retainAllInternal(Collection<?> c) {
        //noinspection unchecked
        NativeMixedCollection collection = NativeMixedCollection.newObjectIdCollection((Collection<ObjectId>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.RETAIN_ALL);
    }
}

/**
 * TODO
 */
class UUIDOperator extends SetValueOperator<UUID> {

    public UUIDOperator(BaseRealm baseRealm, OsSet osSet, Class<UUID> valueClass) {
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
    boolean remove(@Nullable Object o) {
        return osSet.remove((UUID) o);
    }

    @Override
    boolean containsAllInternal(Collection<?> c) {
        for (Object value : c) {
            if (value != null && !(value instanceof UUID)) {
                return false;
            }
        }

        //noinspection unchecked
        Collection<UUID> uuidCollection = (Collection<UUID>) c;
        NativeMixedCollection collection = NativeMixedCollection.newUUIDCollection(uuidCollection);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.CONTAINS_ALL);
    }

    @Override
    boolean addAllInternal(Collection<? extends UUID> c) {
        for (Object value : c) {
            if (value != null && !(value instanceof UUID)) {
                throw new IllegalArgumentException("Invalid collection type. Set and collection must contain the same type of elements.");
            }
        }

        NativeMixedCollection collection = NativeMixedCollection.newUUIDCollection(c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.ADD_ALL);
    }

    @Override
    boolean removeAllInternal(Collection<?> c) {
        //noinspection unchecked
        NativeMixedCollection collection = NativeMixedCollection.newUUIDCollection((Collection<UUID>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.REMOVE_ALL);
    }

    @Override
    boolean retainAllInternal(Collection<?> c) {
        //noinspection unchecked
        NativeMixedCollection collection = NativeMixedCollection.newUUIDCollection((Collection<UUID>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.RETAIN_ALL);
    }
}

/**
 * TODO
 *
 * @param <E>
 */
abstract class SetIterator<E> implements Iterator<E> {

    protected final OsSet osSet;
    protected final BaseRealm baseRealm;    // TODO: needed for models, will be abstracted later

    private int pos = -1;

    public SetIterator(OsSet osSet, BaseRealm baseRealm) {
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
 * TODO
 */
class BooleanSetIterator extends SetIterator<Boolean> {
    public BooleanSetIterator(OsSet osSet, BaseRealm baseRealm) {
        super(osSet, baseRealm);
    }
}

/**
 * TODO
 */
class StringSetIterator extends SetIterator<String> {
    public StringSetIterator(OsSet osSet, BaseRealm baseRealm) {
        super(osSet, baseRealm);
    }
}

/**
 * TODO
 */
class IntegerSetIterator extends SetIterator<Integer> {
    public IntegerSetIterator(OsSet osSet, BaseRealm baseRealm) {
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
 * TODO
 */
class LongSetIterator extends SetIterator<Long> {
    public LongSetIterator(OsSet osSet, BaseRealm baseRealm) {
        super(osSet, baseRealm);
    }
}

/**
 * TODO
 */
class ShortSetIterator extends SetIterator<Short> {
    public ShortSetIterator(OsSet osSet, BaseRealm baseRealm) {
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
 * TODO
 */
class ByteSetIterator extends SetIterator<Byte> {
    public ByteSetIterator(OsSet osSet, BaseRealm baseRealm) {
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
 * TODO
 */
class FloatSetIterator extends SetIterator<Float> {
    public FloatSetIterator(OsSet osSet, BaseRealm baseRealm) {
        super(osSet, baseRealm);
    }
}

/**
 * TODO
 */
class DoubleSetIterator extends SetIterator<Double> {
    public DoubleSetIterator(OsSet osSet, BaseRealm baseRealm) {
        super(osSet, baseRealm);
    }
}

/**
 * TODO
 */
class BinarySetIterator extends SetIterator<byte[]> {
    public BinarySetIterator(OsSet osSet, BaseRealm baseRealm) {
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
 * TODO
 */

class ObjectIdSetIterator extends SetIterator<ObjectId> {
    public ObjectIdSetIterator(OsSet osSet, BaseRealm baseRealm) {
        super(osSet, baseRealm);
    }
}

/**
 * TODO
 */
class UUIDSetIterator extends SetIterator<UUID> {
    public UUIDSetIterator(OsSet osSet, BaseRealm baseRealm) {
        super(osSet, baseRealm);
    }
}
