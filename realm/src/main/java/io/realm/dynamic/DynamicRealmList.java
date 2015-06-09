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
        checkIsValidObject(object);
        linkView.add(object.row.getIndex());
        return true;
    }

    @Override
    public void clear() {
        linkView.clear();
    }

    @Override
    public DynamicRealmObject get(int location) {
        checkValidIndex(location);
        return new DynamicRealmObject(realm, linkView.get(location));
    }

    @Override
    public DynamicRealmObject remove(int location) {
        DynamicRealmObject removedItem = get(location);
        linkView.remove(location);
        return removedItem;
    }

    @Override
    public DynamicRealmObject set(int location, DynamicRealmObject object) {
        checkIsValidObject(object);
        checkValidIndex(location);
        linkView.set(location, object.row.getIndex());
        return object;
    }

    @Override
    public int size() {
        return ((Long)linkView.size()).intValue();
    }

    private void checkIsValidObject(DynamicRealmObject object) {
        if (object == null) {
            throw new IllegalArgumentException("DynamicRealmList does not accept null values");
        }
        if (!realm.getPath().equals(object.realm.getPath())) {
            throw new IllegalArgumentException("Cannot add a object belonging already in another Realm");
        }
        if (!linkView.getTable().hasSameSchema(object.row.getTable())) {
            String expectedClass = linkView.getTable().getName();
            String objectClassName = object.row.getTable().getName();
            throw new IllegalArgumentException("Object is of type " + objectClassName + ". Expected " + expectedClass);
        }
    }

    private void checkValidIndex(int location) {
        long size = linkView.size();
        if (location < 0 || location >= size) {
            throw new IndexOutOfBoundsException(String.format("Invalid index: %s. Valid range is [%s, %s]", location, 0, size - 1));
        }
    }


}
