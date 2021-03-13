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
import java.util.NoSuchElementException;
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
    public boolean containsAll(Collection<?> c) {
        return setValueOperator.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return setValueOperator.addAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return setValueOperator.retainAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
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

    OsSet getOsSet() {
        return setValueOperator.osSet;
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
    protected final Class<E> valueClass;

    public SetValueOperator(BaseRealm baseRealm, OsSet osSet, Class<E> valueClass) {
        this.baseRealm = baseRealm;
        this.osSet = osSet;
        this.valueClass = valueClass;
    }

    public boolean add(@Nullable E e) {
        boolean found = osSet.contains(e);
        osSet.add(e);
        return !found;
    }

    public boolean isValid() {
        if (baseRealm.isClosed()) {
            return false;
        }
        return osSet.isValid();
    }

    public boolean isFrozen() {
        return baseRealm.isFrozen();
    }

    public int size() {
        return Long.valueOf(osSet.size()).intValue();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public boolean contains(@Nullable Object o) {
        return osSet.contains(o);
    }

    public Iterator<E> iterator() {
        return new SetIterator<>(osSet, baseRealm);
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
        // Check if collection is subset in case we receive a RealmSet
        if (c instanceof RealmSet) {
            //noinspection unchecked
            RealmSet<E> setFromCollection = (RealmSet<E>) c;
            OsSet otherOsSet = setFromCollection.getOsSet();
            return otherOsSet.isSubSetOf(osSet.getNativePtr());
        }

        return osSet.containsAll(c, valueClass);
    }

    public boolean addAll(Collection<? extends E> c) {
        // TODO
        return false;
    }

    public boolean retainAll(Collection<?> c) {
        // TODO
        return false;
    }

    public boolean removeAll(Collection<?> c) {
        // TODO
        return false;
    }

    public void clear() {
        osSet.clear();
    }

    public RealmSet<E> freeze() {
        BaseRealm frozenRealm = baseRealm.freeze();
        OsSet frozenOsSet = osSet.freeze(frozenRealm.sharedRealm);
        return new RealmSet<>(frozenRealm, frozenOsSet, valueClass);
    }
}

/**
 * TODO
 *
 * @param <E>
 */
class SetIterator<E> implements Iterator<E> {

    private final OsSet osSet;
    private final BaseRealm baseRealm;

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

    private E getValueAtIndex(int position) {
        //noinspection unchecked
        return (E) osSet.getValueAtIndex(position);
    }
}
