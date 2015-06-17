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

import io.realm.Realm;
import io.realm.internal.LinkView;

/**
 * RealmList exposed using a dynamic API. All objects in the list must have the same schema even though they are accessed
 * dynamically.
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
        checkIsValidObject(object);
        linkView.add(object.row.getIndex());
        return true;
    }

    @Override
    public void clear() {
        linkView.clear();
    }

    @Override
    public DynamicRealmObject get(int index) {
        checkValidIndex(index);
        return new DynamicRealmObject(realm, linkView.getCheckedRow(index));
    }

    @Override
    public DynamicRealmObject remove(int index) {
        DynamicRealmObject removedItem = get(index);
        linkView.remove(index);
        return removedItem;
    }

    @Override
    public DynamicRealmObject set(int index, DynamicRealmObject object) {
        checkIsValidObject(object);
        checkValidIndex(index);
        linkView.set(index, object.row.getIndex());
        return object;
    }

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
