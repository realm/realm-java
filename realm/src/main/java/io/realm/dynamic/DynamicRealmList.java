/*
 * Copyright 2014 Realm Inc.
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

package io.realm.dynamic;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;

import io.realm.Realm;
import io.realm.internal.LinkView;

/**
 * Dynamic RealmLObject for interacting with a RealmObject using dynamic names.
 *
 * @see io.realm.RealmMigration
 */
public class DynamicRealmList extends AbstractList<DynamicRealmObject> {

    private final LinkView linkView;
    private final Realm realm;

    DynamicRealmList(LinkView linkView, Realm realm) {
        this.linkView = linkView;
        this.realm = realm;
    }

    @Override
    public boolean add(DynamicRealmObject object) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public boolean addAll(Collection<? extends DynamicRealmObject> collection) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void clear() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public boolean contains(Object object) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public DynamicRealmObject get(int location) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public int indexOf(Object object) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public boolean isEmpty() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public Iterator<DynamicRealmObject> iterator() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public int lastIndexOf(Object object) {
        return 0;
    }

    @Override
    public ListIterator<DynamicRealmObject> listIterator() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public ListIterator<DynamicRealmObject> listIterator(int location) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public DynamicRealmObject remove(int location) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public boolean remove(Object object) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public DynamicRealmObject set(int location, DynamicRealmObject object) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public int size() {
        throw new RuntimeException("Not implemented");
    }
}
