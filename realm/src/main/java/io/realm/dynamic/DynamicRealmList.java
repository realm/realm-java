/*
 * Copyright 2015 Realm Inc.
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
import java.util.List;

import io.realm.Realm;
import io.realm.internal.LinkView;

/**
 * {@link io.realm.RealmList} exposed using a dynamic API. All objects in the list must have the same schema even though
 * they are accessed dynamically. Null values are not allowed in this list.
 */
public class DynamicRealmList extends AbstractList<DynamicRealmObject> {

    private final LinkView linkView;
    private final Realm realm;

    DynamicRealmList(LinkView linkView, Realm realm) {
        this.linkView = linkView;
        this.realm = realm;
    }

    /**
     * Adds the specified object at the end of this List.
     *
     * @param object the object to add.
     * @return true
     * @throws IllegalArgumentException if object is either {@code null} or has the wrong type.
     */
    @Override
    public boolean add(DynamicRealmObject object) {
        checkIsValidObject(object);
        linkView.add(object.row.getIndex());
        return true;
    }

    /**
     * Removes all elements from this list, leaving it empty.
     *
     * @see List#isEmpty
     * @see List#size
     */
    @Override
    public void clear() {
        linkView.clear();
    }

    /**
     * Returns the element at the specified location in this list.
     *
     * @param location the index of the element to return.
     * @return the element at the specified index.
     * @throws IndexOutOfBoundsException if {@code location < 0 || location >= size()}
     */
    @Override
    public DynamicRealmObject get(int location) {
        checkValidIndex(location);
        return new DynamicRealmObject(realm, linkView.getCheckedRow(location));
    }

    /**
     * Removes the object at the specified location from this list.
     *
     * @param location the index of the object to remove.
     * @return the removed object.
     * @throws IndexOutOfBoundsException if {@code location < 0 || location >= size()}
     */
    @Override
    public DynamicRealmObject remove(int location) {
        DynamicRealmObject removedItem = get(location);
        linkView.remove(location);
        return removedItem;
    }

    /**
     * Replaces the element at the specified location in this list with the
     * specified object.
     *
     * @param location the index at which to put the specified object.
     * @param object the object to add.
     * @return the previous element at the index.
     * @throws IllegalArgumentException if object is either {@code null} or has the wrong type.
     * @throws IndexOutOfBoundsException if {@code location < 0 || location >= size()}
     */
    @Override
    public DynamicRealmObject set(int location, DynamicRealmObject object) {
        checkIsValidObject(object);
        checkValidIndex(location);
        linkView.set(location, object.row.getIndex());
        return object;
    }

    /**
     * Returns the number of elements in this list.
     *
     * @return the number of elements in this list.
     */
    @Override
    public int size() {
        long size = linkView.size();
        return size < Integer.MAX_VALUE ? (int) size : Integer.MAX_VALUE;
    }

    private void checkIsValidObject(DynamicRealmObject object) {
        if (object == null) {
            throw new IllegalArgumentException("DynamicRealmList does not accept null values");
        }
        if (!realm.getPath().equals(object.realm.getPath())) {
            throw new IllegalArgumentException("Cannot add an object already belonging to another Realm");
        }
        if (!linkView.getTable().hasSameSchema(object.row.getTable())) {
            String expectedClass = linkView.getTable().getName();
            String objectClassName = object.row.getTable().getName();
            throw new IllegalArgumentException("Object is of type " + objectClassName + ". Expected " + expectedClass);
        }
    }

    private void checkValidIndex(int index) {
        long size = linkView.size();
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException(String.format("Invalid index: %s. Valid range is [%s, %s]", index, 0, size - 1));
        }
    }
}
