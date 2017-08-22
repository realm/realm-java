/*
 * Copyright 2017 Realm Inc.
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

package io.realm.valuelist;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.annotation.Nonnull;


/**
 * {@link RealmValueList} is used to model a list of values those have exactly the same type
 * in a {@link io.realm.RealmObject}.
 * RealmValueList has two modes: A managed and unmanaged mode. In managed mode values all v are persisted inside a Realm,
 * in unmanaged mode it works as a normal ArrayList.
 * <p>
 * Only Realm can create managed RealmValueLists. Managed RealmValueLists will automatically update the content whenever
 * the underlying Realm is updated, and can only be accessed using the getter of a {@link io.realm.RealmObject}.
 * <p>
 * Unmanaged RealmValueLists can be created by the user. This is useful when dealing with JSON deserializers like GSON
 * or other frameworks that inject values into a class.
 * <p>
 * {@link RealmValueList} can contain more elements than {@code Integer.MAX_VALUE}.
 * In that case, you can access only first {@code Integer.MAX_VALUE} elements in it.
 *
 * @param <E> the class of values in list. It must be one of {@link String}, {@link Long}, {@link Integer},
 * {@link Short}, {@link Byte}, {@link Double}, {@link Float}, {@link Boolean} and {@link java.util.Date}.
 */
public abstract class RealmValueList<E> implements OrderedRealmValueCollection<E> {

    /**
     * Creates unmanaged empty {@link RealmValueList}.
     *
     * @param valueType the type of values. It must be one of the supported class described
     * in the class comment of {@link RealmValueList}.
     * @param <E> type of values in the list.
     * @return newly created unmanaged {@link RealmValueList} instance.
     */
    public static <E> RealmValueList<E> of(Class<E> valueType) {
        // TODO implement this
        return null;
    }

    /**
     * Creates unmanaged empty {@link RealmValueList}.
     *
     * @param valueType the type of values. It must be one of the supported class described
     * in the class comment of {@link RealmValueList}.
     * @param values initial values.
     * @param <E> type of values in the list.
     * @return newly created unmanaged {@link RealmValueList} instance.
     */
    @SafeVarargs
    public static <E> RealmValueList<E> copyOf(Class<E> valueType, E... values) {
        // TODO implement this
        return null;
    }

    protected RealmValueList() {
    }

    @Nonnull
    @Override
    public Object[] toArray() {
        // TODO implement this
        return null;
    }

    /*
     * MEMO: This method supports {@code long}, {@code  int}, {@code short}, {@code byte},
     * {@code double}, {@code float} and {@code boolean} as {@code T} in addition to classes
     * mentioned in the class comment of {@link RealmValueList}.
     */
    @Nonnull
    @Override
    public <T> T[] toArray(T[] array) {
        // TODO implement this
        return null;
    }

    @Override
    public boolean add(E e) {
        // TODO implement this
        return false;
    }

    @Override
    public void add(int index, E element) {
        // TODO implement this
    }

    @Override
    public E set(int index, E element) {
        // TODO implement this
        return null;
    }

    @Override
    public void clear() {
        // TODO implement this
    }

    @Override
    public E remove(int index) {
        // TODO implement this
        return null;
    }

    @Override
    public boolean remove(Object o) {
        // TODO implement this
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        // TODO implement this
        return false;
    }

    public void move(int oldIndex, int newIndex) {
        // TODO implement this
    }

    @Override
    public Iterator<E> iterator() {
        // TODO implement this
        return null;
    }

    @Override
    public ListIterator<E> listIterator() {
        // TODO implement this
        return null;
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        // TODO implement this
        return null;
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        // TODO implement this
        return null;
    }

    @Override
    public String toString() {
        // TODO implement this
        return super.toString();
    }
}
