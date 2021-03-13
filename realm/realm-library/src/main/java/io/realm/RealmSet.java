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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import io.realm.internal.Freezable;
import io.realm.internal.ManageableObject;
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
    @NotNull
    @Override
    public Iterator<E> iterator() {
        return setStrategy.iterator();
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public Object[] toArray() {
        return setStrategy.toArray();
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] a) {
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
    public boolean containsAll(@NotNull Collection<?> c) {
        return setStrategy.containsAll(c);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addAll(@NotNull Collection<? extends E> c) {
        return setStrategy.addAll(c);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return setStrategy.retainAll(c);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
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
    // Private stuff
    // ------------------------------------------

    @SuppressWarnings("unchecked")
    private static <T> ManagedSetStrategy<T> getStrategy(BaseRealm baseRealm,
                                                         OsSet osSet,
                                                         Class<T> valueClass) {
        if (CollectionUtils.isClassForRealmModel(valueClass)) {
            // TODO
            return null;
        }

        ManagedSetManager<T> manager;
        if (valueClass == String.class) {
            manager = new ManagedSetManager<>((SetValueOperator<T>) new SetValueOperator<>(baseRealm, osSet, String.class));
        } else {
            throw new UnsupportedOperationException("getStrategy: missing class '" + valueClass.getSimpleName() + "'");
        }

        return new ManagedSetStrategy<>(manager);
    }

    /**
     * TODO
     *
     * @param <E>
     */
    private abstract static class SetStrategy<E> implements Set<E>, ManageableObject, Freezable<RealmSet<E>> {
        abstract OsSet getOsSet();
    }

    /**
     * TODO
     *
     * @param <E>
     */
    private static class ManagedSetStrategy<E> extends SetStrategy<E> {

        private final ManagedSetManager<E> managedSetManager;

        private ManagedSetStrategy(ManagedSetManager<E> managedSetManager) {
            this.managedSetManager = managedSetManager;
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
            return managedSetManager.isValid();
        }

        @Override
        public boolean isFrozen() {
            return managedSetManager.isFrozen();
        }

        // ------------------------------------------
        // Set API
        // ------------------------------------------

        @Override
        public int size() {
            return managedSetManager.size();
        }

        @Override
        public boolean isEmpty() {
            return managedSetManager.isEmpty();
        }

        @Override
        public boolean contains(@Nullable Object o) {
            return managedSetManager.contains(o);
        }

        @NotNull
        @Override
        public Iterator<E> iterator() {
            return managedSetManager.iterator();
        }

        @NotNull
        @Override
        public Object[] toArray() {
            // TODO
            return new Object[0];
        }

        @NotNull
        @Override
        public <T> T[] toArray(T[] a) {
            checkValidArray(a);
            return managedSetManager.toArray(a);
        }

        @Override
        public boolean add(@Nullable E e) {
            return managedSetManager.add(e);
        }

        @Override
        public boolean remove(@Nullable Object o) {
            return managedSetManager.remove(o);
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            checkValidCollection(c);
            return managedSetManager.containsAll(c);
        }

        @Override
        public boolean addAll(Collection<? extends E> c) {
            checkValidCollection(c);
            // TODO
            return false;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            checkValidCollection(c);
            // TODO
            return false;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            checkValidCollection(c);
            // TODO
            return false;
        }

        @Override
        public void clear() {
            managedSetManager.clear();
        }

        // ------------------------------------------
        // Freezable API
        // ------------------------------------------

        @Override
        public RealmSet<E> freeze() {
            return managedSetManager.freeze();
        }

        @Override
        OsSet getOsSet() {
            return managedSetManager.getOsSet();
        }

        // ------------------------------------------
        // Private stuff
        // ------------------------------------------

        private <T> void checkValidArray(@Nullable T[] array) {
            if (array == null) {
                // According to Java Set documentation
                throw new NullPointerException("Cannot specify a null collection in containsAll.");
            }
        }

        private void checkValidCollection(@Nullable Collection<?> collection) {
            if (collection == null) {
                // According to Java Set documentation
                throw new NullPointerException("Cannot specify a null collection in containsAll.");
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

        @NotNull
        @Override
        public Iterator<E> iterator() {
            return unmanagedSet.iterator();
        }

        @NotNull
        @Override
        public Object[] toArray() {
            return unmanagedSet.toArray();
        }

        @NotNull
        @Override
        public <T> T[] toArray(@NotNull T[] a) {
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
        public boolean containsAll(@NotNull Collection<?> c) {
            return unmanagedSet.containsAll(c);
        }

        @Override
        public boolean addAll(@NotNull Collection<? extends E> c) {
            return unmanagedSet.addAll(c);
        }

        @Override
        public boolean retainAll(@NotNull Collection<?> c) {
            return unmanagedSet.retainAll(c);
        }

        @Override
        public boolean removeAll(@NotNull Collection<?> c) {
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
    }
}
