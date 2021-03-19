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

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import javax.annotation.Nullable;

import io.realm.internal.Freezable;
import io.realm.internal.ManageableObject;
import io.realm.internal.OsSet;

/**
 * TODO
 *
 * @param <E>
 */
public class ManagedSetManager<E> implements Set<E>, ManageableObject, Freezable<RealmSet<E>> {

    private final SetValueOperator<E> setValueOperator;

    public ManagedSetManager(SetValueOperator<E> setValueOperator) {
        this.setValueOperator = setValueOperator;
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

    @NotNull
    @Override
    public Iterator<E> iterator() {
        return setValueOperator.iterator();
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return setValueOperator.toArray();
    }

    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] a) {
        return setValueOperator.toArray(a);
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
    public boolean containsAll(@NotNull Collection<?> c) {
        return setValueOperator.containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends E> c) {
        return setValueOperator.addAll(c);
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return setValueOperator.reatainAll(c);
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
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
        return null;
    }
}

/**
 * TODO
 *
 * @param <E>
 */
class SetValueOperator<E> {

    protected final BaseRealm baseRealm;
    protected final OsSet osSet;

    public SetValueOperator(BaseRealm baseRealm, OsSet osSet) {
        this.baseRealm = baseRealm;
        this.osSet = osSet;
    }

    public boolean add(@Nullable E e) {
        boolean alreadyExisted = osSet.contains(e);
        osSet.add(e);
        return !alreadyExisted;
    }

    public boolean isValid() {
        // TODO
        return false;
    }

    public boolean isFrozen() {
        // TODO
        return false;
    }

    public int size() {
        return Long.valueOf(osSet.size()).intValue();
    }

    public boolean isEmpty() {
        // TODO
        return false;
    }

    public boolean contains(@Nullable Object o) {
        return osSet.contains(o);
    }

    public Iterator<E> iterator() {
        // TODO
        return null;
    }

    public Object[] toArray() {
        // TODO
        return new Object[0];
    }

    public <T> T[] toArray(T[] a) {
        // TODO
        return null;
    }

    public boolean remove(@Nullable Object o) {
        return osSet.remove(o);
    }

    public boolean containsAll(Collection<?> c) {
        // TODO
        return false;
    }

    public boolean addAll(Collection<? extends E> c) {
        // TODO
        return false;
    }

    public boolean reatainAll(Collection<?> c) {
        // TODO
        return false;
    }

    public boolean removeAll(Collection<?> c) {
        // TODO
        return false;
    }

    public void clear() {
        // TODO
    }
}
