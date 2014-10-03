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

package io.realm;

import java.util.AbstractList;

import io.realm.internal.LinkView;

/**
 * RealmList is used in one-to-many relationships.
 *
 * @param <E> The class of objects in this list
 */

public class RealmList<E extends RealmObject> extends AbstractList<E> {

    private Class<E> clazz;
    private LinkView view;
    private Realm realm;

    public RealmList(Class<E> clazz, LinkView view, Realm realm) {
        this.clazz = clazz;
        this.view = view;
        this.realm = realm;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(int location, E object) {
        view.insert(location, object.realmGetRow().getIndex());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean add(E object) {
        view.add(object.realmGetRow().getIndex());
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E set(int location, E object) {
        view.set(location, object.realmGetRow().getIndex());
        return object;
    }

    /**
     * Moves an object from one position to another
     * @param oldPos
     * @param newPos
     */
    public void move(int oldPos, int newPos) {
        view.move(oldPos, newPos);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        view.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E remove(int location) {
        view.remove(location);
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E get(int i) {
        return realm.get(clazz, view.getTargetRowIndex(i));
    }

    /**
     * Find the first object
     * @return The first object
     */
    public E first() {
        if (!view.isEmpty()) {
            return get(0);
        }
        return null;
    }

    /**
     * Find the last object
     * @return The last object
     */
    public E last() {
        if (!view.isEmpty()) {
            return get(size()-1);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return ((Long)view.size()).intValue();
    }

    /**
     * Initialize a query
     * @return A query object
     * @see io.realm.RealmQuery
     */
    public RealmQuery<E> where() {
        return null;
    }
}
