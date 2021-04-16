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

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import io.realm.internal.Freezable;
import io.realm.internal.ManageableObject;
import io.realm.internal.ObservableMap;
import io.realm.internal.OsSet;

/**
 * TODO
 *
 * @param <E>
 */
public class RealmSet<E> implements Set<E>, ManageableObject, Freezable<RealmSet<E>> {

    protected final SetStrategy<E> setStrategy;

    /**
     * Instantiates a RealmSet in unmanaged mode.
     */
    public RealmSet() {
        this.setStrategy = new UnmanagedSetStrategy<>();
    }

    /**
     * Instantiates a RealmSet in managed mode.
     *
     * @param baseRealm
     * @param osSet
     * @param valueClass
     */
    public RealmSet(BaseRealm baseRealm, OsSet osSet, Class<E> valueClass) {
        this.setStrategy = getStrategy(baseRealm, osSet, valueClass);
    }

    // ------------------------------------------
    // ManageableObject API
    // ------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isManaged() {
        return setStrategy.isManaged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid() {
        return setStrategy.isValid();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFrozen() {
        return setStrategy.isFrozen();
    }

    // ------------------------------------------
    // Set API
    // ------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return setStrategy.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return setStrategy.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(@Nullable Object o) {
        return setStrategy.contains(o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<E> iterator() {
        return setStrategy.iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object[] toArray() {
        return setStrategy.toArray();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T[] toArray(T[] a) {
        return setStrategy.toArray(a);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean add(@Nullable E e) {
        return setStrategy.add(e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean remove(@Nullable Object o) {
        return setStrategy.remove(o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        return setStrategy.containsAll(c);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addAll(Collection<? extends E> c) {
        return setStrategy.addAll(c);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean retainAll(Collection<?> c) {
        return setStrategy.retainAll(c);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        return setStrategy.removeAll(c);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        setStrategy.clear();
    }

    // ------------------------------------------
    // Freezable API
    // ------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public RealmSet<E> freeze() {
        return setStrategy.freeze();
    }

    OsSet getOsSet() {
        return setStrategy.getOsSet();
    }

    // ------------------------------------------
    // RealmSet API
    // ------------------------------------------

    /**
     * TODO
     *
     * @param listener the listener to be notified.
     * @throws IllegalArgumentException if the change listener is {@code null}.
     * @throws IllegalStateException    if you try to add a listener from a non-Looper or
     *                                  {@link android.app.IntentService} thread.
     */
    public void addChangeListener(RealmChangeListener<RealmSet<E>> listener) {
        setStrategy.addChangeListener(this, listener);
    }

    /**
     * TODO
     *
     * @param listener the listener to be notified.
     * @throws IllegalArgumentException if the change listener is {@code null}.
     * @throws IllegalStateException    if you try to add a listener from a non-Looper or
     *                                  {@link android.app.IntentService} thread.
     */
    public void addChangeListener(SetChangeListener<E> listener) {
        setStrategy.addChangeListener(this, listener);
    }

    /**
     * Removes the specified change listener.
     *
     * @param listener the change listener to be removed.
     * @throws IllegalArgumentException if the change listener is {@code null}.
     * @throws IllegalStateException    if you try to remove a listener from a non-Looper Thread.
     */
    public void removeChangeListener(RealmChangeListener<RealmSet<E>> listener) {
        setStrategy.removeChangeListener(this, listener);
    }

    /**
     * Removes the specified change listener.
     *
     * @param listener the change listener to be removed.
     * @throws IllegalArgumentException if the change listener is {@code null}.
     * @throws IllegalStateException    if you try to remove a listener from a non-Looper Thread.
     */
    public void removeChangeListener(SetChangeListener<E> listener) {
        setStrategy.removeChangeListener(this, listener);
    }

    /**
     * Removes all user-defined change listeners.
     *
     * @throws IllegalStateException if you try to remove listeners from a non-Looper Thread.
     * @see io.realm.RealmChangeListener
     */
    public void removeAllChangeListeners() {
        setStrategy.removeAllChangeListeners();
    }

    /**
     * Indicates whether a set has any listeners attached to it.
     *
     * @return {@code true} if any listeners have been added, {@code false} otherwise.
     */
    public boolean hasListeners() {
        return setStrategy.hasListeners();
    }

    // ------------------------------------------
    // Private stuff
    // ------------------------------------------

    @SuppressWarnings("unchecked")
    private static <T> ManagedSetStrategy<T> getStrategy(BaseRealm baseRealm,
                                                         OsSet osSet,
                                                         Class<T> valueClass) {
        if (CollectionUtils.isClassForRealmModel(valueClass)) {
            Class<? extends RealmModel> typeCastClass = (Class<? extends RealmModel>) valueClass;
            return new ManagedSetStrategy<>((SetValueOperator<T>) new RealmModelSetOperator<>(baseRealm, osSet, typeCastClass), valueClass);
        }

        SetValueOperator<T> operator;
        if (valueClass == Boolean.class) {
            operator = (SetValueOperator<T>) new BooleanOperator(baseRealm, osSet, Boolean.class);
        } else if (valueClass == String.class) {
            operator = (SetValueOperator<T>) new StringOperator(baseRealm, osSet, String.class);
        } else if (valueClass == Integer.class) {
            operator = (SetValueOperator<T>) new IntegerOperator(baseRealm, osSet, Integer.class);
        } else if (valueClass == Long.class) {
            operator = (SetValueOperator<T>) new LongOperator(baseRealm, osSet, Long.class);
        } else if (valueClass == Short.class) {
            operator = (SetValueOperator<T>) new ShortOperator(baseRealm, osSet, Short.class);
        } else if (valueClass == Byte.class) {
            operator = (SetValueOperator<T>) new ByteOperator(baseRealm, osSet, Byte.class);
        } else if (valueClass == Float.class) {
            operator = (SetValueOperator<T>) new FloatOperator(baseRealm, osSet, Float.class);
        } else if (valueClass == Double.class) {
            operator = (SetValueOperator<T>) new DoubleOperator(baseRealm, osSet, Double.class);
        } else if (valueClass == byte[].class) {
            operator = (SetValueOperator<T>) new BinaryOperator(baseRealm, osSet, byte[].class);
        } else if (valueClass == Date.class) {
            operator = (SetValueOperator<T>) new DateOperator(baseRealm, osSet, Date.class);
        } else if (valueClass == Decimal128.class) {
            operator = (SetValueOperator<T>) new Decimal128Operator(baseRealm, osSet, Decimal128.class);
        } else if (valueClass == ObjectId.class) {
            operator = (SetValueOperator<T>) new ObjectIdOperator(baseRealm, osSet, ObjectId.class);
        } else if (valueClass == UUID.class) {
            operator = (SetValueOperator<T>) new UUIDOperator(baseRealm, osSet, UUID.class);
        } else if (valueClass == Mixed.class) {
            operator = (SetValueOperator<T>) new MixedSetOperator(baseRealm, osSet, Mixed.class);
        } else {
            throw new UnsupportedOperationException("getStrategy: missing class '" + valueClass.getSimpleName() + "'");
        }

        return new ManagedSetStrategy<>(operator, valueClass);
    }

    /**
     * TODO
     *
     * @param <E>
     */
    private abstract static class SetStrategy<E> implements Set<E>, ManageableObject, Freezable<RealmSet<E>> {
        abstract OsSet getOsSet();

        abstract void addChangeListener(RealmSet<E> set, RealmChangeListener<RealmSet<E>> listener);

        abstract void addChangeListener(RealmSet<E> set, SetChangeListener<E> listener);

        abstract void removeChangeListener(RealmSet<E> set, RealmChangeListener<RealmSet<E>> listener);

        abstract void removeChangeListener(RealmSet<E> set, SetChangeListener<E> listener);

        abstract void removeAllChangeListeners();

        abstract boolean hasListeners();
    }

    /**
     * TODO
     *
     * @param <E>
     */
    private static class ManagedSetStrategy<E> extends SetStrategy<E> {

        private final SetValueOperator<E> setValueOperator;
        private final Class<E> valueClass;

        ManagedSetStrategy(SetValueOperator<E> setValueOperator, Class<E> valueClass) {
            this.setValueOperator = setValueOperator;
            this.valueClass = valueClass;
        }

        // ------------------------------------------
        // ManageableObject API
        // ------------------------------------------

        @Override
        public boolean isManaged() {
            return true;
        }

        @Override
        public boolean isValid() {
            return setValueOperator.isValid();
        }

        @Override
        public boolean isFrozen() {
            return setValueOperator.isFrozen();
        }

        // ------------------------------------------
        // Set API
        // ------------------------------------------

        @Override
        public int size() {
            return setValueOperator.size();
        }

        @Override
        public boolean isEmpty() {
            return setValueOperator.isEmpty();
        }

        @Override
        public boolean contains(@Nullable Object o) {
            return setValueOperator.contains(o);
        }

        @Override
        public Iterator<E> iterator() {
            return setValueOperator.iterator();
        }

        @Override
        public Object[] toArray() {
            Object[] array = new Object[size()];
            int i = 0;
            for (E value : this) {
                array[i] = value;
                i++;
            }
            return array;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T[] toArray(T[] a) {
            checkValidArray(a);

            T[] array;
            long setSize = size();

            // From docs:
            // If the set fits in the specified array, it is returned therein.
            // Otherwise, a new array is allocated with the runtime type of the
            // specified array and the size of this set.
            if (a.length == setSize || a.length > setSize) {
                array = a;
            } else {
                array = (T[]) Array.newInstance(valueClass, (int) setSize);
            }

            int i = 0;
            for (E value : this) {
                if (value == null) {
                    array[i] = null;
                } else {
                    array[i] = (T) value;
                }
                i++;
            }

            // From docs:
            // If this set fits in the specified array with room to spare
            // (i.e., the array has more elements than this set), the element in
            // the array immediately following the end of the set is set to null.
            if (a.length > setSize) {
                array[i] = null;
            }

            return array;
        }

        @Override
        public boolean add(@Nullable E e) {
            return setValueOperator.add(e);
        }

        @Override
        public boolean remove(@Nullable Object o) {
            return setValueOperator.remove(o);
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            checkValidCollection(c);
            return setValueOperator.containsAll(c);
        }

        @Override
        public boolean addAll(Collection<? extends E> c) {
            checkValidCollection(c);
            return setValueOperator.addAll(c);
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            checkValidCollection(c);
            return setValueOperator.retainAll(c);
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            checkValidCollection(c);
            return setValueOperator.removeAll(c);
        }

        @Override
        public void clear() {
            setValueOperator.clear();
        }

        // ------------------------------------------
        // Freezable API
        // ------------------------------------------

        @Override
        public RealmSet<E> freeze() {
            return setValueOperator.freeze();
        }

        @Override
        OsSet getOsSet() {
            return setValueOperator.getOsSet();
        }

        // ------------------------------------------
        // RealmSet API
        // ------------------------------------------

        @Override
        void addChangeListener(RealmSet<E> set, RealmChangeListener<RealmSet<E>> listener) {
            setValueOperator.addChangeListener(set, listener);
        }

        @Override
        void addChangeListener(RealmSet<E> set, SetChangeListener<E> listener) {
            setValueOperator.addChangeListener(set, listener);
        }

        @Override
        void removeChangeListener(RealmSet<E> set, RealmChangeListener<RealmSet<E>> listener) {
            setValueOperator.removeChangeListener(set, listener);
        }

        @Override
        void removeChangeListener(RealmSet<E> set, SetChangeListener<E> listener) {
            setValueOperator.removeChangeListener(set, listener);
        }

        @Override
        void removeAllChangeListeners() {
            setValueOperator.removeAllChangeListeners();
        }

        @Override
        boolean hasListeners() {
            return setValueOperator.hasListeners();
        }

        // ------------------------------------------
        // Private stuff
        // ------------------------------------------

        private <T> void checkValidArray(T[] array) {
            if (array == null) {
                // According to Java Set documentation
                throw new NullPointerException("Cannot pass a null array when calling 'toArray'.");
            }

            String valueClassSimpleName = valueClass.getSimpleName();
            String arrayTypeSimpleName = array.getClass().getComponentType().getSimpleName();

            // According to Java Set documentation
            if (!valueClassSimpleName.equals(arrayTypeSimpleName)) {
                throw new ArrayStoreException("Array type must be of type '" + valueClassSimpleName +
                        "' but it was of type '" + arrayTypeSimpleName + "'.");
            }
        }

        private void checkValidCollection(Collection<?> collection) {
            if (collection == null) {
                throw new NullPointerException("Collection must not be null.");
            }
        }
    }

    /**
     * TODO
     *
     * @param <E>
     */
    private static class UnmanagedSetStrategy<E> extends SetStrategy<E> {

        private final Set<E> unmanagedSet = new HashSet<>();

        // ------------------------------------------
        // ManageableObject API
        // ------------------------------------------

        @Override
        public boolean isManaged() {
            return false;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public boolean isFrozen() {
            return false;
        }

        // ------------------------------------------
        // Set API
        // ------------------------------------------

        @Override
        public int size() {
            return unmanagedSet.size();
        }

        @Override
        public boolean isEmpty() {
            return unmanagedSet.isEmpty();
        }

        @Override
        public boolean contains(@Nullable Object o) {
            return unmanagedSet.contains(o);
        }

        @Override
        public Iterator<E> iterator() {
            return unmanagedSet.iterator();
        }

        @Override
        public Object[] toArray() {
            return unmanagedSet.toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return unmanagedSet.toArray(a);
        }

        @Override
        public boolean add(@Nullable E e) {
            return unmanagedSet.add(e);
        }

        @Override
        public boolean remove(@Nullable Object o) {
            return unmanagedSet.remove(o);
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return unmanagedSet.containsAll(c);
        }

        @Override
        public boolean addAll(Collection<? extends E> c) {
            return unmanagedSet.addAll(c);
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return unmanagedSet.retainAll(c);
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return unmanagedSet.removeAll(c);
        }

        @Override
        public void clear() {
            unmanagedSet.clear();
        }

        // ------------------------------------------
        // Freezable API
        // ------------------------------------------

        @Override
        public RealmSet<E> freeze() {
            throw new UnsupportedOperationException("Unmanaged RealmSets cannot be frozen.");
        }

        @Override
        OsSet getOsSet() {
            throw new UnsupportedOperationException("Unmanaged RealmSets do not have a representation in native code.");
        }

        // ------------------------------------------
        // RealmSet API
        // ------------------------------------------

        @Override
        void addChangeListener(RealmSet<E> set, RealmChangeListener<RealmSet<E>> listener) {
            throw new UnsupportedOperationException("Unmanaged RealmSets do not support change listeners.");
        }

        @Override
        void addChangeListener(RealmSet<E> set, SetChangeListener<E> listener) {
            throw new UnsupportedOperationException("Unmanaged RealmSets do not support change listeners.");
        }

        @Override
        void removeChangeListener(RealmSet<E> set, RealmChangeListener<RealmSet<E>> listener) {
            throw new UnsupportedOperationException("Cannot remove change listener because unmanaged RealmSets do not support change listeners.");
        }

        @Override
        void removeChangeListener(RealmSet<E> set, SetChangeListener<E> listener) {
            throw new UnsupportedOperationException("Cannot remove change listener because unmanaged RealmSets do not support change listeners.");
        }

        @Override
        void removeAllChangeListeners() {
            throw new UnsupportedOperationException("Cannot remove change listeners because unmanaged RealmSets do not support change listeners.");
        }

        @Override
        boolean hasListeners() {
            return false;
        }
    }
}
