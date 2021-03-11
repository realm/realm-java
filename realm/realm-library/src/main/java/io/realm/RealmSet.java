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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import io.realm.internal.Freezable;
import io.realm.internal.ManageableObject;

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

    // TODO: missing constructor for managed mode

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

    @Override
    public int size() {
        return setStrategy.size();
    }

    @Override
    public boolean isEmpty() {
        return setStrategy.isEmpty();
    }

    @Override
    public boolean contains(@Nullable Object o) {
        return setStrategy.contains(o);
    }

    @NotNull
    @Override
    public Iterator<E> iterator() {
        return setStrategy.iterator();
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return setStrategy.toArray();
    }

    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] a) {
        return setStrategy.toArray(a);
    }

    @Override
    public boolean add(@Nullable E e) {
        return setStrategy.add(e);
    }

    @Override
    public boolean remove(@Nullable Object o) {
        return setStrategy.remove(o);
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return setStrategy.containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends E> c) {
        return setStrategy.addAll(c);
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return setStrategy.retainAll(c);
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        return setStrategy.removeAll(c);
    }

    @Override
    public void clear() {
        setStrategy.clear();
    }

    // ------------------------------------------
    // Freezable API
    // ------------------------------------------

    @Override
    public RealmSet<E> freeze() {
        return setStrategy.freeze();
    }

    /**
     * TODO
     *
     * @param <E>
     */
    private abstract static class SetStrategy<E> implements Set<E>, ManageableObject, Freezable<RealmSet<E>> {
        // TODO
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
            return false;
        }

        @Override
        public boolean isValid() {
            return false;
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
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean contains(@Nullable Object o) {
            return false;
        }

        @NotNull
        @Override
        public Iterator<E> iterator() {
            return null;
        }

        @NotNull
        @Override
        public Object[] toArray() {
            return new Object[0];
        }

        @NotNull
        @Override
        public <T> T[] toArray(@NotNull T[] a) {
            return null;
        }

        @Override
        public boolean add(@Nullable E e) {
            return false;
        }

        @Override
        public boolean remove(@Nullable Object o) {
            return false;
        }

        @Override
        public boolean containsAll(@NotNull Collection<?> c) {
            return false;
        }

        @Override
        public boolean addAll(@NotNull Collection<? extends E> c) {
            return false;
        }

        @Override
        public boolean retainAll(@NotNull Collection<?> c) {
            return false;
        }

        @Override
        public boolean removeAll(@NotNull Collection<?> c) {
            return false;
        }

        @Override
        public void clear() {

        }

        // ------------------------------------------
        // Freezable API
        // ------------------------------------------

        @Override
        public RealmSet<E> freeze() {
            return null;
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
    }
}
