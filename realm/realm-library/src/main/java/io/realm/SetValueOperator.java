package io.realm;

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

    abstract boolean removeInternal(@Nullable Object o);

    abstract boolean containsAllInternal(Collection<?> c);

    abstract boolean addAllInternal(Collection<? extends E> c);

    abstract boolean removeAllInternal(Collection<?> c);

    abstract boolean retainAllInternal(Collection<?> c);

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

    private boolean isObjectSameType(@Nullable Object o) {
        // Return false when passing something else than the correct type
        if (o != null) {
            return o.getClass() == valueClass;
        } else {
            return true;
        }
    }

    private boolean isUpperBoundCollectionSameType(Collection<? extends E> c) {
        if (!c.isEmpty()) {
            for (E item : c) {
                if (item != null && item.getClass() != valueClass) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isCollectionSameType(Collection<?> c) {
        if (!c.isEmpty()) {
            for (Object item : c) {
                if (item != null && item.getClass() != valueClass) {
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
        if (valueClass == String.class) {
            return (SetIterator<T>) new StringSetIterator(osSet, baseRealm);
        } else if (valueClass == Integer.class) {
            return (SetIterator<T>) new IntegerSetIterator(osSet, baseRealm);
        } else if (valueClass == Long.class) {
            return (SetIterator<T>) new LongSetIterator(osSet, baseRealm);
        } else if (valueClass == Short.class) {
            return (SetIterator<T>) new ShortSetIterator(osSet, baseRealm);
        } else if (valueClass == Byte.class) {
            return (SetIterator<T>) new ByteSetIterator(osSet, baseRealm);
        } else if (valueClass == byte[].class) {
            return (SetIterator<T>) new BinarySetIterator(osSet, baseRealm);
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
    boolean removeInternal(@Nullable Object o) {
        // Object has been type-checked from caller
        return osSet.remove((String) o);
    }

    @Override
    boolean containsAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        Collection<String> stringCollection = (Collection<String>) c;
        NativeMixedCollection collection = NativeMixedCollection.newStringCollection(stringCollection);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.CONTAINS_ALL);
    }

    @Override
    boolean addAllInternal(Collection<? extends String> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        NativeMixedCollection collection = NativeMixedCollection.newStringCollection((Collection<String>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.ADD_ALL);
    }

    @Override
    boolean removeAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        NativeMixedCollection collection = NativeMixedCollection.newStringCollection((Collection<String>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.REMOVE_ALL);
    }

    @Override
    boolean retainAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
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
    boolean removeInternal(@Nullable Object o) {
        // Object has been type-checked from caller
        return osSet.remove((Integer) o);
    }

    @Override
    boolean containsAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        Collection<Number> numberCollection = (Collection<Number>) c;
        NativeMixedCollection collection = NativeMixedCollection.newIntegerCollection(numberCollection);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.CONTAINS_ALL);
    }

    @Override
    boolean addAllInternal(Collection<? extends Integer> c) {
        // Collection has been type-checked from caller
        NativeMixedCollection collection = NativeMixedCollection.newIntegerCollection(c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.ADD_ALL);
    }

    @Override
    boolean removeAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        NativeMixedCollection collection = NativeMixedCollection.newIntegerCollection((Collection<Integer>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.REMOVE_ALL);
    }

    @Override
    boolean retainAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
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
    boolean removeInternal(@Nullable Object o) {
        // Object has been type-checked from caller
        return osSet.remove((Long) o);
    }

    @Override
    boolean containsAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        NativeMixedCollection collection = NativeMixedCollection.newIntegerCollection((Collection<Number>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.CONTAINS_ALL);
    }

    @Override
    boolean addAllInternal(Collection<? extends Long> c) {
        // Collection has been type-checked from caller
        NativeMixedCollection collection = NativeMixedCollection.newIntegerCollection(c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.ADD_ALL);
    }

    @Override
    boolean removeAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        NativeMixedCollection collection = NativeMixedCollection.newIntegerCollection((Collection<Long>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.REMOVE_ALL);
    }

    @Override
    boolean retainAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
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
    boolean removeInternal(@Nullable Object o) {
        // Object has been type-checked from caller
        return osSet.remove((Short) o);
    }

    @Override
    boolean containsAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        Collection<Number> numberCollection = (Collection<Number>) c;
        NativeMixedCollection collection = NativeMixedCollection.newIntegerCollection(numberCollection);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.CONTAINS_ALL);
    }

    @Override
    boolean addAllInternal(Collection<? extends Short> c) {
        // Collection has been type-checked from caller
        NativeMixedCollection collection = NativeMixedCollection.newIntegerCollection(c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.ADD_ALL);
    }

    @Override
    boolean removeAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        NativeMixedCollection collection = NativeMixedCollection.newIntegerCollection((Collection<Short>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.REMOVE_ALL);
    }

    @Override
    boolean retainAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
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
    boolean removeInternal(@Nullable Object o) {
        // Object has been type-checked from caller
        return osSet.remove((Byte) o);
    }

    @Override
    boolean containsAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        Collection<Number> numberCollection = (Collection<Number>) c;
        NativeMixedCollection collection = NativeMixedCollection.newIntegerCollection(numberCollection);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.CONTAINS_ALL);
    }

    @Override
    boolean addAllInternal(Collection<? extends Byte> c) {
        // Collection has been type-checked from caller
        NativeMixedCollection collection = NativeMixedCollection.newIntegerCollection(c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.ADD_ALL);
    }

    @Override
    boolean removeAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        NativeMixedCollection collection = NativeMixedCollection.newIntegerCollection((Collection<Byte>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.REMOVE_ALL);
    }

    @Override
    boolean retainAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        NativeMixedCollection collection = NativeMixedCollection.newIntegerCollection((Collection<Byte>) c);
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
    boolean removeInternal(@Nullable Object o) {
        return osSet.remove((byte[]) o);
    }

    @Override
    boolean containsAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        Collection<byte[]> binaryCollection = (Collection<byte[]>) c;
        NativeMixedCollection collection = NativeMixedCollection.newBinaryCollection(binaryCollection);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.CONTAINS_ALL);
    }

    @Override
    boolean addAllInternal(Collection<? extends byte[]> c) {
        // Collection has been type-checked from caller
        NativeMixedCollection collection = NativeMixedCollection.newBinaryCollection(c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.ADD_ALL);
    }

    @Override
    boolean removeAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        NativeMixedCollection collection = NativeMixedCollection.newBinaryCollection((Collection<byte[]>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.REMOVE_ALL);
    }

    @Override
    boolean retainAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        NativeMixedCollection collection = NativeMixedCollection.newBinaryCollection((Collection<byte[]>) c);
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
    boolean removeInternal(@Nullable Object o) {
        // Object has been type-checked from caller
        return osSet.remove((UUID) o);
    }

    @Override
    boolean containsAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        Collection<UUID> uuidCollection = (Collection<UUID>) c;
        NativeMixedCollection collection = NativeMixedCollection.newUUIDCollection(uuidCollection);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.CONTAINS_ALL);
    }

    @Override
    boolean addAllInternal(Collection<? extends UUID> c) {
        // Collection has been type-checked from caller
        NativeMixedCollection collection = NativeMixedCollection.newUUIDCollection(c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.ADD_ALL);
    }

    @Override
    boolean removeAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
        //noinspection unchecked
        NativeMixedCollection collection = NativeMixedCollection.newUUIDCollection((Collection<UUID>) c);
        return osSet.collectionFunnel(collection, OsSet.ExternalCollectionOperation.REMOVE_ALL);
    }

    @Override
    boolean retainAllInternal(Collection<?> c) {
        // Collection has been type-checked from caller
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
class UUIDSetIterator extends SetIterator<UUID> {
    public UUIDSetIterator(OsSet osSet, BaseRealm baseRealm) {
        super(osSet, baseRealm);
    }
}
