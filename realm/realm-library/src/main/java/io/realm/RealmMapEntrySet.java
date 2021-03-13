/*
 * Copyright 2020 Realm Inc.
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
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

import io.realm.internal.OsMap;
import io.realm.internal.core.NativeMixed;
import io.realm.internal.util.Pair;

/**
 * Wrapper class used to pack key-value pairs when calling {@link RealmMap#entrySet()}.
 *
 * @param <K>
 * @param <V>
 */
class RealmMapEntrySet<K, V> implements Set<Map.Entry<K, V>> {

    public enum IteratorType {
        LONG, BYTE, SHORT, INTEGER, FLOAT, DOUBLE, STRING, BOOLEAN, DATE, DECIMAL128, BINARY,
        OBJECT_ID, UUID, MIXED, OBJECT
    }

    private final BaseRealm baseRealm;
    private final OsMap osMap;
    private final IteratorType iteratorType;
    private final EqualsHelper<K, V> equalsHelper;
    private final TypeSelectorForMap<K, V> typeSelectorForMap;

    public RealmMapEntrySet(BaseRealm baseRealm,
                            OsMap osMap,
                            IteratorType iteratorType,
                            @Nullable TypeSelectorForMap<K, V> typeSelectorForMap) {
        this.baseRealm = baseRealm;
        this.osMap = osMap;
        this.iteratorType = iteratorType;
        this.equalsHelper = new GenericEquals<>();
        this.typeSelectorForMap = typeSelectorForMap;
    }

    public RealmMapEntrySet(BaseRealm baseRealm,
                            OsMap osMap,
                            IteratorType iteratorType,
                            EqualsHelper<K, V> equalsHelper,
                            @Nullable TypeSelectorForMap<K, V> typeSelectorForMap) {
        this.baseRealm = baseRealm;
        this.osMap = osMap;
        this.iteratorType = iteratorType;
        this.equalsHelper = equalsHelper;
        this.typeSelectorForMap = typeSelectorForMap;
    }

    @Override
    public int size() {
        final long actualMap = osMap.size();
        return actualMap < Integer.MAX_VALUE ? (int) actualMap : Integer.MAX_VALUE;
    }

    @Override
    public boolean isEmpty() {
        return osMap.size() == 0;
    }

    @Override
    public boolean contains(@Nullable Object o) {
        for (Map.Entry<K, V> entry : this) {
            if (entry == null && o == null) {
                return true;
            } else if (o instanceof Map.Entry) {
                //noinspection unchecked
                if (entry != null && equalsHelper.equalsHelper(entry, ((Map.Entry<K, V>) o))) {
                    return true;
                }
            } else {
                return false;
            }
        }
        return false;
    }

    @NotNull
    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        return iteratorFactory(iteratorType, osMap, baseRealm, typeSelectorForMap);
    }

    @NotNull
    @Override
    public Object[] toArray() {
        Object[] array = new Object[(int) osMap.size()];

        int i = 0;
        for (Map.Entry<K, V> entry : this) {
            array[i] = entry;
            i++;
        }

        return array;
    }

    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] a) {
        T[] array;
        long mapSize = osMap.size();

        // From docs:
        // If the set fits in the specified array, it is returned therein.
        // Otherwise, a new array is allocated with the runtime type of the
        // specified array and the size of this set.
        if (a.length == mapSize || a.length > mapSize) {
            array = a;
        } else {
            //noinspection unchecked
            array = (T[]) Array.newInstance(Map.Entry.class, (int) mapSize);
        }

        int i = 0;
        for (Map.Entry<K, V> entry : this) {
            //noinspection unchecked
            array[i] = (T) entry;
            i++;
        }

        // From docs:
        // If this set fits in the specified array with room to spare
        // (i.e., the array has more elements than this set), the element in
        // the array immediately following the end of the set is set to null.
        if (a.length > mapSize) {
            array[i] = null;
        }

        return array;
    }

    @Override
    public boolean add(Map.Entry<K, V> entry) {
        throw new UnsupportedOperationException("This set is immutable and cannot be modified.");
    }

    @Override
    public boolean remove(@Nullable Object o) {
        throw new UnsupportedOperationException("This set is immutable and cannot be modified.");
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        if (c.isEmpty()) {
            return this.isEmpty();
        }

        for (Object elem : c) {
            if (elem instanceof Map.Entry) {
                //noinspection unchecked
                Map.Entry<K, V> entry = (Map.Entry<K, V>) elem;
                if (!this.contains(entry)) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends Map.Entry<K, V>> c) {
        throw new UnsupportedOperationException("This set is immutable and cannot be modified.");
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException("This set is immutable and cannot be modified.");
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException("This set is immutable and cannot be modified.");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("This set is immutable and cannot be modified.");
    }

    private static <K, V> EntrySetIterator<K, V> iteratorFactory(IteratorType iteratorType,
                                                                 OsMap osMap,
                                                                 BaseRealm baseRealm,
                                                                 @Nullable TypeSelectorForMap typeSelectorForMap) {
        switch (iteratorType) {
            case LONG:
                //noinspection unchecked
                return (EntrySetIterator<K, V>) new LongValueIterator<>(osMap, baseRealm);
            case BYTE:
                //noinspection unchecked
                return (EntrySetIterator<K, V>) new ByteValueIterator<>(osMap, baseRealm);
            case SHORT:
                //noinspection unchecked
                return (EntrySetIterator<K, V>) new ShortValueIterator<>(osMap, baseRealm);
            case INTEGER:
                //noinspection unchecked
                return (EntrySetIterator<K, V>) new IntegerValueIterator<>(osMap, baseRealm);
            case FLOAT:
                //noinspection unchecked
                return (EntrySetIterator<K, V>) new FloatValueIterator<>(osMap, baseRealm);
            case DOUBLE:
                //noinspection unchecked
                return (EntrySetIterator<K, V>) new DoubleValueIterator<>(osMap, baseRealm);
            case STRING:
                //noinspection unchecked
                return (EntrySetIterator<K, V>) new StringValueIterator<>(osMap, baseRealm);
            case BOOLEAN:
                //noinspection unchecked
                return (EntrySetIterator<K, V>) new BooleanValueIterator<>(osMap, baseRealm);
            case DATE:
                //noinspection unchecked
                return (EntrySetIterator<K, V>) new DateValueIterator<>(osMap, baseRealm);
            case DECIMAL128:
                //noinspection unchecked
                return (EntrySetIterator<K, V>) new Decimal128ValueIterator<>(osMap, baseRealm);
            case BINARY:
                //noinspection unchecked
                return (EntrySetIterator<K, V>) new BinaryValueIterator<>(osMap, baseRealm);
            case OBJECT_ID:
                //noinspection unchecked
                return (EntrySetIterator<K, V>) new ObjectIdValueIterator<>(osMap, baseRealm);
            case UUID:
                //noinspection unchecked
                return (EntrySetIterator<K, V>) new UUIDValueIterator<>(osMap, baseRealm);
            case MIXED:
                //noinspection unchecked
                return (EntrySetIterator<K, V>) new MixedValueIterator<K>(osMap, baseRealm);
            case OBJECT:
                if (typeSelectorForMap == null) {
                    throw new IllegalArgumentException("Missing class container when creating RealmModelValueIterator.");
                }
                return new RealmModelValueIterator<>(osMap, baseRealm, typeSelectorForMap);
            default:
                throw new IllegalArgumentException("Invalid iterator type.");
        }
    }

    private abstract static class EntrySetIterator<K, V> implements Iterator<Map.Entry<K, V>> {

        protected final OsMap osMap;
        protected final BaseRealm baseRealm;

        private int pos = -1;

        public EntrySetIterator(OsMap osMap, BaseRealm baseRealm) {
            this.osMap = osMap;
            this.baseRealm = baseRealm;
        }

        protected abstract Map.Entry<K, V> getEntryInternal(int position);

        @Override
        public boolean hasNext() {
            return pos + 1 < osMap.size();
        }

        @Override
        public Map.Entry<K, V> next() {
            pos++;
            long size = osMap.size();
            if (pos >= size) {
                throw new NoSuchElementException("Cannot access index " + pos + " when size is " + size +
                        ". Remember to check hasNext() before using next().");
            }

            return getEntryInternal(pos);
        }
    }

    private static class LongValueIterator<K> extends EntrySetIterator<K, Long> {

        public LongValueIterator(OsMap osMap, BaseRealm baseRealm) {
            super(osMap, baseRealm);
        }

        @Override
        protected Map.Entry<K, Long> getEntryInternal(int position) {
            Pair<K, Object> pair = osMap.getEntryForPrimitive(position);
            if (pair.second == null) {
                return new AbstractMap.SimpleImmutableEntry<>(pair.first, null);
            }

            Long longValue = (Long) pair.second;

            return new AbstractMap.SimpleImmutableEntry<>(pair.first, longValue);
        }
    }

    private static class ByteValueIterator<K> extends EntrySetIterator<K, Byte> {

        public ByteValueIterator(OsMap osMap, BaseRealm baseRealm) {
            super(osMap, baseRealm);
        }

        @Override
        protected Map.Entry<K, Byte> getEntryInternal(int position) {
            Pair<K, Object> pair = osMap.getEntryForPrimitive(position);
            if (pair.second == null) {
                return new AbstractMap.SimpleImmutableEntry<>(pair.first, null);
            }

            Long longValue = (Long) pair.second;

            return new AbstractMap.SimpleImmutableEntry<>(pair.first, longValue.byteValue());
        }
    }

    private static class ShortValueIterator<K> extends EntrySetIterator<K, Short> {

        public ShortValueIterator(OsMap osMap, BaseRealm baseRealm) {
            super(osMap, baseRealm);
        }

        @Override
        protected Map.Entry<K, Short> getEntryInternal(int position) {
            Pair<K, Object> pair = osMap.getEntryForPrimitive(position);
            if (pair.second == null) {
                return new AbstractMap.SimpleImmutableEntry<>(pair.first, null);
            }

            Long longValue = (Long) pair.second;

            return new AbstractMap.SimpleImmutableEntry<>(pair.first, longValue.shortValue());
        }
    }

    private static class IntegerValueIterator<K> extends EntrySetIterator<K, Integer> {

        public IntegerValueIterator(OsMap osMap, BaseRealm baseRealm) {
            super(osMap, baseRealm);
        }

        @Override
        protected Map.Entry<K, Integer> getEntryInternal(int position) {
            Pair<K, Object> pair = osMap.getEntryForPrimitive(position);
            if (pair.second == null) {
                return new AbstractMap.SimpleImmutableEntry<>(pair.first, null);
            }

            Long longValue = (Long) pair.second;

            return new AbstractMap.SimpleImmutableEntry<>(pair.first, longValue.intValue());
        }
    }

    private static class FloatValueIterator<K> extends EntrySetIterator<K, Float> {

        public FloatValueIterator(OsMap osMap, BaseRealm baseRealm) {
            super(osMap, baseRealm);
        }

        @Override
        protected Map.Entry<K, Float> getEntryInternal(int position) {
            Pair<K, Object> pair = osMap.getEntryForPrimitive(position);
            if (pair.second == null) {
                return new AbstractMap.SimpleImmutableEntry<>(pair.first, null);
            }

            return new AbstractMap.SimpleImmutableEntry<>(pair.first, (Float) pair.second);
        }
    }

    private static class DoubleValueIterator<K> extends EntrySetIterator<K, Double> {

        public DoubleValueIterator(OsMap osMap, BaseRealm baseRealm) {
            super(osMap, baseRealm);
        }

        @Override
        protected Map.Entry<K, Double> getEntryInternal(int position) {
            Pair<K, Object> pair = osMap.getEntryForPrimitive(position);
            if (pair.second == null) {
                return new AbstractMap.SimpleImmutableEntry<>(pair.first, null);
            }

            return new AbstractMap.SimpleImmutableEntry<>(pair.first, (Double) pair.second);
        }
    }

    private static class StringValueIterator<K> extends EntrySetIterator<K, String> {

        public StringValueIterator(OsMap osMap, BaseRealm baseRealm) {
            super(osMap, baseRealm);
        }

        @Override
        protected Map.Entry<K, String> getEntryInternal(int position) {
            Pair<K, Object> pair = osMap.getEntryForPrimitive(position);
            if (pair.second == null) {
                return new AbstractMap.SimpleImmutableEntry<>(pair.first, null);
            }

            return new AbstractMap.SimpleImmutableEntry<>(pair.first, (String) pair.second);
        }
    }

    private static class BooleanValueIterator<K> extends EntrySetIterator<K, Boolean> {

        public BooleanValueIterator(OsMap osMap, BaseRealm baseRealm) {
            super(osMap, baseRealm);
        }

        @Override
        protected Map.Entry<K, Boolean> getEntryInternal(int position) {
            Pair<K, Object> pair = osMap.getEntryForPrimitive(position);
            if (pair.second == null) {
                return new AbstractMap.SimpleImmutableEntry<>(pair.first, null);
            }

            return new AbstractMap.SimpleImmutableEntry<>(pair.first, (Boolean) pair.second);
        }
    }

    private static class DateValueIterator<K> extends EntrySetIterator<K, Date> {

        public DateValueIterator(OsMap osMap, BaseRealm baseRealm) {
            super(osMap, baseRealm);
        }

        @Override
        protected Map.Entry<K, Date> getEntryInternal(int position) {
            Pair<K, Object> pair = osMap.getEntryForPrimitive(position);
            if (pair.second == null) {
                return new AbstractMap.SimpleImmutableEntry<>(pair.first, null);
            }

            return new AbstractMap.SimpleImmutableEntry<>(pair.first, (Date) pair.second);
        }
    }

    private static class Decimal128ValueIterator<K> extends EntrySetIterator<K, Decimal128> {

        public Decimal128ValueIterator(OsMap osMap, BaseRealm baseRealm) {
            super(osMap, baseRealm);
        }

        @Override
        protected Map.Entry<K, Decimal128> getEntryInternal(int position) {
            Pair<K, Object> pair = osMap.getEntryForPrimitive(position);
            if (pair.second == null) {
                return new AbstractMap.SimpleImmutableEntry<>(pair.first, null);
            }

            return new AbstractMap.SimpleImmutableEntry<>(pair.first, (Decimal128) pair.second);
        }
    }

    private static class BinaryValueIterator<K> extends EntrySetIterator<K, byte[]> {

        public BinaryValueIterator(OsMap osMap, BaseRealm baseRealm) {
            super(osMap, baseRealm);
        }

        @Override
        protected Map.Entry<K, byte[]> getEntryInternal(int position) {
            Pair<K, Object> pair = osMap.getEntryForPrimitive(position);
            if (pair.second == null) {
                return new AbstractMap.SimpleImmutableEntry<>(pair.first, null);
            }

            return new AbstractMap.SimpleImmutableEntry<>(pair.first, (byte[]) pair.second);
        }
    }

    private static class ObjectIdValueIterator<K> extends EntrySetIterator<K, ObjectId> {

        public ObjectIdValueIterator(OsMap osMap, BaseRealm baseRealm) {
            super(osMap, baseRealm);
        }

        @Override
        protected Map.Entry<K, ObjectId> getEntryInternal(int position) {
            Pair<K, Object> pair = osMap.getEntryForPrimitive(position);
            if (pair.second == null) {
                return new AbstractMap.SimpleImmutableEntry<>(pair.first, null);
            }

            return new AbstractMap.SimpleImmutableEntry<>(pair.first, (ObjectId) pair.second);
        }
    }

    private static class UUIDValueIterator<K> extends EntrySetIterator<K, UUID> {

        public UUIDValueIterator(OsMap osMap, BaseRealm baseRealm) {
            super(osMap, baseRealm);
        }

        @Override
        protected Map.Entry<K, UUID> getEntryInternal(int position) {
            Pair<K, Object> pair = osMap.getEntryForPrimitive(position);
            if (pair.second == null) {
                return new AbstractMap.SimpleImmutableEntry<>(pair.first, null);
            }

            return new AbstractMap.SimpleImmutableEntry<>(pair.first, (UUID) pair.second);
        }
    }

    private static class RealmModelValueIterator<K, V> extends EntrySetIterator<K, V> {

        private final TypeSelectorForMap<K, V> typeSelectorForMap;

        public RealmModelValueIterator(OsMap osMap,
                                       BaseRealm baseRealm,
                                       TypeSelectorForMap<K, V> typeSelectorForMap) {
            super(osMap, baseRealm);
            this.typeSelectorForMap = typeSelectorForMap;
        }

        @Override
        protected Map.Entry<K, V> getEntryInternal(int position) {
            Pair<K, Long> pair = osMap.getKeyObjRowPair(position);
            K key = pair.first;
            long objRow = pair.second;

            if (objRow == OsMap.NOT_FOUND) {
                return new AbstractMap.SimpleImmutableEntry<>(key, null);
            }

            return typeSelectorForMap.getModelEntry(baseRealm, objRow, key);
        }
    }

    private static class MixedValueIterator<K> extends EntrySetIterator<K, Mixed> {

        public MixedValueIterator(OsMap osMap, BaseRealm baseRealm) {
            super(osMap, baseRealm);
        }

        @Override
        protected Map.Entry<K, Mixed> getEntryInternal(int position) {
            Pair<K, NativeMixed> pair = osMap.getKeyMixedPair(position);
            K key = pair.first;
            NativeMixed nativeMixed = pair.second;
            Mixed value = new Mixed(MixedOperator.fromNativeMixed(baseRealm, nativeMixed));
            return new AbstractMap.SimpleImmutableEntry<>(key, value);
        }
    }
}

abstract class EqualsHelper<K, V> {

    boolean equalsHelper(Map.Entry<K, V> entry, Map.Entry<K, V> other) {
        K otherKey = other.getKey();
        K key = entry.getKey();
        if (key.equals(otherKey)) {
            return compareInternal(entry.getValue(), other.getValue());
        }
        return false;
    }

    abstract protected boolean compareInternal(@Nullable V value, @Nullable V otherValue);
}

class GenericEquals<K, V> extends EqualsHelper<K, V> {
    @Override
    protected boolean compareInternal(@Nullable V value, @Nullable V otherValue) {
        if (value == null) {
            return otherValue == null;
        } else {
            return value.equals(otherValue);
        }
    }
}

class BinaryEquals<K> extends EqualsHelper<K, byte[]> {
    @Override
    protected boolean compareInternal(@Nullable byte[] value, @Nullable byte[] otherValue) {
        return Arrays.equals(value, otherValue);
    }
}
