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
import java.util.ArrayList;
import java.util.List;

import io.realm.exceptions.RealmException;
import io.realm.internal.LinkView;
import io.realm.internal.TableOrView;

/**
 * RealmList is used to model one-to-many relationships in a {@link io.realm.RealmObject}.
 * RealmList has two modes: A managed and non-managed mode. In managed mode all objects are persisted
 * inside a Realm, in non-managed mode it works as a normal ArrayList.
 *
 * Only Realm can create managed RealmLists. Managed RealmLists will automatically update their
 * content whenever the underlying Realm is updated, and can only be accessed using the getter
 * from a {@link io.realm.RealmObject}.
 *
 * Non-managed RealmLists can be created by the user and can contain both managed and non-managed
 * RealmObjects. This is useful eg. when dealing with JSON deserializers like GSON or other
 * frameworks that inject values into a class. Non-managed elements in this list can be added to a
 * Realm using the {@link Realm#copyToRealm(Iterable)} method.
 *
 * @param <E> The class of objects in list.
 */

public class RealmList<E extends RealmObject> extends AbstractList<E> {

    private static final String ONLY_IN_MANAGED_MODE_MESSAGE = "This method is only available in managed mode";
    private static final String NULL_OBJECTS_NOT_ALLOWED_MESSAGE = "RealmList does not accept null values";
    public static final String MANAGED_OBJECTS_NOT_ALLOWED_MESSAGE = "RealmObjects already managed by Realm cannot be added to RealmList in non-managed mode.";

    private final boolean managedMode;
    private Class<E> clazz;
    private LinkView view;
    private Realm realm;
    private List<E> nonManagedList;

    /**
     * Create a RealmList in non-managed mode, where the elements are not controlled by a Realm.
     * This effectively makes the RealmList function as a {@link java.util.ArrayList} and it is not possible
     * to query the objects in this state.
     *
     * Use {@link io.realm.Realm#copyToRealm(Iterable)}  to properly persist it's elements in
     * Realm.
     */
    public RealmList() {
        managedMode = false;
        nonManagedList = new ArrayList<E>();
    }

    /**
     * Creates a RealmList from a LinkView, so its elements are managed by Realm.
     *
     * @param clazz Type of elements in the Array
     * @param view  Backing LinkView
     * @param realm Reference to Realm containing the data
     */
    RealmList(Class<E> clazz, LinkView view, Realm realm) {
        this.managedMode = true;
        this.clazz = clazz;
        this.view = view;
        this.realm = realm;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(int location, E object) {
        assertValidObject(object);
        if (managedMode) {
            view.insert(location, object.row.getIndex());
        } else {
            nonManagedList.add(location, object);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean add(E object) {
        assertValidObject(object);
        if (managedMode) {
            view.add(object.row.getIndex());
        } else {
            nonManagedList.add(object);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E set(int location, E object) {
        assertValidObject(object);
        if (managedMode) {
            assertIndex(location);
            view.set(location, object.row.getIndex());
        } else {
            nonManagedList.set(location, object);
        }
        return object;
    }

    /**
     * Moves an object from one position to another, while maintaining a fixed sized list.
     * RealmObjects will be shifted so no null values are introduced.
     *
     * @param oldPos Index of RealmObject to move.
     * @param newPos Target position. If newPos < oldPos the object at the location will be shifted
     *               to the right. If oldPos < newPos, indexes > oldPos will be shifted once to the
     *               left.
     *
     * @throws java.lang.IndexOutOfBoundsException if any position is outside [0, size()[.
     */
    public void move(int oldPos, int newPos) {
        assertIndex(oldPos);
        assertIndex(newPos);
        if (managedMode) {
            view.move(oldPos, newPos);
        } else {
            E object = nonManagedList.remove(oldPos);
            if (newPos > oldPos) {
                nonManagedList.add(newPos - 1, object);
            } else {
                nonManagedList.add(newPos, object);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        if (managedMode) {
            view.clear();
        } else {
            nonManagedList.clear();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E remove(int location) {
        if (managedMode) {
            assertIndex(location);
            E removedItem = get(location);
            view.remove(location);
            return removedItem;
        } else {
            return nonManagedList.remove(location);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E get(int location) {
        if (managedMode) {
            assertIndex(location);
            return realm.get(clazz, view.getTargetRowIndex(location));
        } else {
            return nonManagedList.get(location);
        }
    }

    /**
     * Find the first object.
     *
     * @return The first object or {@code null} if the list is empty.
     */
    public E first() {
        if (managedMode && !view.isEmpty()) {
            return get(0);
        } else if (nonManagedList != null && nonManagedList.size() > 0) {
            return nonManagedList.get(0);
        }
        return null;
    }

    /**
     * Find the last object.
     *
     * @return The last object or {@code null} if the list is empty.
     */
    public E last() {
        if (managedMode && !view.isEmpty()) {
            return get((int) view.size() - 1);
        } else if (nonManagedList != null && nonManagedList.size() > 0) {
            return nonManagedList.get(nonManagedList.size() - 1);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        if (managedMode) {
            return ((Long)view.size()).intValue();
        } else {
            return nonManagedList.size();
        }
    }

    /**
     * Returns a RealmQuery, which can be used to query for specific objects of this class
     *
     * @return A RealmQuery object
     * @see io.realm.RealmQuery
     */
    public RealmQuery<E> where() {
        if (managedMode) {
            TableOrView table = this.view.where().findAll();
            return new RealmQuery<E>(this.realm, table, clazz);
        } else {
            throw new RealmException(ONLY_IN_MANAGED_MODE_MESSAGE);
        }
    }

    private void assertValidObject(E object) {
        if (object == null) {
            throw new IllegalArgumentException(NULL_OBJECTS_NOT_ALLOWED_MESSAGE);
        }
    }

    private void assertIndex(int location) {
        int size = size();
        if (location < 0 || location >= size) {
            throw new IndexOutOfBoundsException("Invalid index " + location + ", size is " + size);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(managedMode ? clazz.getSimpleName() : getClass().getSimpleName());
        sb.append("@[");
        for (int i = 0; i < size(); i++) {
            if (managedMode) {
                sb.append(get(i).row.getIndex());
            } else {
                sb.append(System.identityHashCode(get(i)));
            }
            if (i < size() - 1) {
                sb.append(',');
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
