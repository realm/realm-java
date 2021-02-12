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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import io.realm.internal.ClassContainer;
import io.realm.internal.OsMap;
import io.realm.internal.core.NativeMixed;
import io.realm.internal.util.Pair;

/**
 * Wrapper class used to pack key-value pairs when calling {@link RealmMap#entrySet()}.
 *
 * @param <K>
 * @param <V>
 */
public class RealmMapEntrySet<K, V> implements Set<Map.Entry<K, V>> {

    public enum IteratorType {
        PRIMITIVE, MIXED, OBJECT
    }

    private final BaseRealm baseRealm;
    private final OsMap osMap;
    private final IteratorType iteratorType;
    private final ClassContainer classContainer;

    public RealmMapEntrySet(BaseRealm baseRealm,
                            OsMap osMap,
                            IteratorType iteratorType,
                            @Nullable ClassContainer classContainer) {
        this.baseRealm = baseRealm;
        this.osMap = osMap;
        this.iteratorType = iteratorType;
        this.classContainer = classContainer;
    }

    @Override
    public int size() {
        return (int) osMap.size();
    }

    @Override
    public boolean isEmpty() {
        return osMap.size() == 0;
    }

    @Override
    public boolean contains(@Nullable Object o) {
        for (Map.Entry<K, V> entry : this) {
            if (entry != null && entry.equals(o)) {
                return true;
            } else if (entry == null && o == null) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        return iteratorFactory(iteratorType, osMap, baseRealm, classContainer);
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
        //noinspection unchecked
        T[] array = (T[]) Array.newInstance(Map.Entry.class, (int) osMap.size());

        int i = 0;
        for (Map.Entry<K, V> entry : this) {
            //noinspection unchecked
            array[i] = (T) entry;
            i++;
        }

        return array;
    }

    @Override
    public boolean add(Map.Entry<K, V> kvEntry) {
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

    private static <K, V> Iterator<Map.Entry<K, V>> iteratorFactory(IteratorType iteratorType,
                                                                    OsMap osMap,
                                                                    BaseRealm baseRealm,
                                                                    @Nullable ClassContainer classContainer) {
        switch (iteratorType) {
            case PRIMITIVE:
                return new PrimitiveValueIterator<>(osMap, baseRealm);
            case MIXED:
                //noinspection unchecked
                return (EntrySetIterator<K, V>) new MixedValueIterator<K>(osMap, baseRealm);
            case OBJECT:
                if (classContainer == null) {
                    throw new IllegalArgumentException("Missing class container when creating RealmModelValueIterator.");
                }
                return new RealmMapEntrySet.RealmModelValueIterator<>(osMap, baseRealm, classContainer);
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

    private static class PrimitiveValueIterator<K, V> extends EntrySetIterator<K, V> {

        public PrimitiveValueIterator(OsMap osMap, BaseRealm baseRealm) {
            super(osMap, baseRealm);
        }

        @Override
        protected Map.Entry<K, V> getEntryInternal(int position) {
            Pair<K, NativeMixed> pair = osMap.getKeyMixedPair(position);
            K key = pair.first;
            NativeMixed value = pair.second;
            MixedType type = value.getType();

            V returnValue;
            switch (type) {
                case INTEGER:
                    //noinspection unchecked
                    returnValue = (V) Long.valueOf(value.asLong());
                    break;
                case BOOLEAN:
                    //noinspection unchecked
                    returnValue = (V) Boolean.valueOf(value.asBoolean());
                    break;
                case STRING:
                    //noinspection unchecked
                    returnValue = (V) String.valueOf(value.asString());
                    break;
                case BINARY:
                    returnValue = (V) value.asBinary();
                    break;
                case DATE:
                    //noinspection unchecked
                    returnValue = (V) value.asDate();
                    break;
                case FLOAT:
                    //noinspection unchecked
                    returnValue = (V) Float.valueOf(value.asFloat());
                    break;
                case DOUBLE:
                    //noinspection unchecked
                    returnValue = (V) Double.valueOf(value.asDouble());
                    break;
                case DECIMAL128:
                    //noinspection unchecked
                    returnValue = (V) value.asDecimal128();
                    break;
                case OBJECT_ID:
                    //noinspection unchecked
                    returnValue = (V) value.asObjectId();
                    break;
                case UUID:
                    //noinspection unchecked
                    returnValue = (V) value.asUUID();
                    break;
                case NULL:
                    returnValue = null;
                    break;
                default:
                    throw new IllegalStateException("Wrong Mixed type for PrimitiveValueIterator.getValue: " + type.toString());
            }

            return new AbstractMap.SimpleImmutableEntry<>(key, returnValue);
        }
    }

    private static class RealmModelValueIterator<K, V> extends EntrySetIterator<K, V> {

        private final ClassContainer classContainer;

        public RealmModelValueIterator(OsMap osMap, BaseRealm baseRealm, ClassContainer classContainer) {
            super(osMap, baseRealm);
            this.classContainer = classContainer;
        }

        @Override
        protected Map.Entry<K, V> getEntryInternal(int position) {
            Pair<K, Long> pair = osMap.getKeyObjRowPair(position);
            K key = pair.first;
            long objRow = pair.second;

            if (objRow == OsMap.NOT_FOUND) {
                return new AbstractMap.SimpleImmutableEntry<>(key, null);
            }

            //noinspection unchecked
            Class<? extends RealmModel> clazz = (Class<? extends RealmModel>) classContainer.getClazz();
            String className = classContainer.getClassName();

            //noinspection unchecked
            V realmModel = (V) baseRealm.get(clazz, className, objRow);

            return new AbstractMap.SimpleImmutableEntry<>(key, realmModel);
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
