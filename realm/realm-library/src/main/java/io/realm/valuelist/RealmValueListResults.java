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

import java.util.AbstractList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.realm.Sort;


public class RealmValueListResults<E> extends AbstractList<E>
        implements OrderedRealmValueCollection<E> {
    private RealmValueListResults() {
    }

    @Override
    public int size() {
        // TODO implement this
        return 0;
    }

    @Override
    public E get(int i) {
        return null;
    }

    @Override
    public boolean isNull(int index) {
        // TODO implement this
        return false;
    }

    @Nonnull
    @Override
    public RealmValueListResults<E> sort() {
        // TODO implement this
        return null;
    }

    @Nonnull
    @Override
    public RealmValueListResults<E> sort(Sort sortOrder) {
        // TODO implement this
        return null;
    }

    @Nonnull
    @Override
    public RealmValueListSnapshot<E> createSnapshot() {
        // TODO implement this
        return null;
    }

    @Nonnull
    @Override
    public RealmValueListQuery<E> where() {
        // TODO implement this
        return null;
    }

    @Nullable
    @Override
    public Number min() {
        // TODO implement this
        return null;
    }

    @Nullable
    @Override
    public Number max() {
        // TODO implement this
        return null;
    }

    @Nonnull
    @Override
    public Number sum() {
        // TODO implement this
        return null;
    }

    @Override
    public double average() {
        // TODO implement this
        return 0;
    }

    @Nullable
    @Override
    public Date maxDate() {
        // TODO implement this
        return null;
    }

    @Nullable
    @Override
    public Date minDate() {
        // TODO implement this
        return null;
    }

    @Override
    public boolean isValid() {
        // TODO implement this
        return false;
    }

    @Override
    public boolean isManaged() {
        // TODO implement this
        return false;
    }

    @Nonnull
    @Override
    public Object[] toArray() {
        // TODO implement this
        return null;
    }

    @Nonnull
    @Override
    public <T> T[] toArray(@Nonnull T[] a) {
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
