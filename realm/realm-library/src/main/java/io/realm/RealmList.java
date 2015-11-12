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
import java.util.Collections;
import java.util.List;

import io.realm.exceptions.RealmException;
import io.realm.internal.LinkView;

/**
 * RealmList is used to model one-to-many relationships in a {@link io.realm.RealmObject}.
 * RealmList has two modes: A managed and non-managed mode. In managed mode all objects are persisted inside a Realm, in
 * non-managed mode it works as a normal ArrayList.
 * <p>
 * Only Realm can create managed RealmLists. Managed RealmLists will automatically update the content whenever the
 * underlying Realm is updated, and can only be accessed using the getter of a {@link io.realm.RealmObject}.
 * <p>
 * Non-managed RealmLists can be created by the user and can contain both managed and non-managed RealmObjects. This is
 * useful when dealing with JSON deserializers like GSON or other frameworks that inject values into a class.
 * Non-managed elements in this list can be added to a Realm using the {@link Realm#copyToRealm(Iterable)} method.
 * <p>
 *
 * @param <E> the class of objects in list.
 */

public class RealmList<E extends RealmObject> extends AbstractList<E> {

    private static final String ONLY_IN_MANAGED_MODE_MESSAGE = "This method is only available in managed mode";
    private static final String NULL_OBJECTS_NOT_ALLOWED_MESSAGE = "RealmList does not accept null values";

    private final boolean managedMode;
    protected Class<E> clazz;
    protected String className;
    protected LinkView view;
    protected BaseRealm realm;
    private List<E> nonManagedList;

    /**
     * Creates a RealmList in non-managed mode, where the elements are not controlled by a Realm.
     * This effectively makes the RealmList function as a {@link java.util.ArrayList} and it is not possible to query
     * the objects in this state.
     * <p>
     * Use {@link io.realm.Realm#copyToRealm(Iterable)} to properly persist it's elements in Realm.
     */
    public RealmList() {
        managedMode = false;
        nonManagedList = new ArrayList<E>();
    }

    /**
     * Creates a RealmList in non-managed mode with an initial list of elements.
     * A RealmList in non-managed mode function as a {@link java.util.ArrayList} and it is not possible to query the
     * objects in this state.
     *
     * Use {@link io.realm.Realm#copyToRealm(Iterable)} to properly persist all non-managed elements in Realm.
     *
     * @param objects initial objects in the list.
     */
    public RealmList(E... objects) {
        if (objects == null) {
            throw new IllegalArgumentException("The objects argument cannot be null");
        }
        managedMode = false;
        nonManagedList = new ArrayList<E>(objects.length);
        Collections.addAll(nonManagedList, objects);
    }

    /**
     * Creates a RealmList from a LinkView, so its elements are managed by Realm.
     *
     * @param clazz type of elements in the Array
     * @param linkView  backing LinkView
     * @param realm reference to Realm containing the data
     */
    RealmList(Class<E> clazz, LinkView linkView, BaseRealm realm) {
        this.managedMode = true;
        this.clazz = clazz;
        this.view = linkView;
        this.realm = realm;
    }

    RealmList(String className, LinkView linkView, BaseRealm realm) {
        this.managedMode = true;
        this.view = linkView;
        this.realm = realm;
        this.className = className;
    }

    /**
     * Checks if {@link io.realm.RealmResults} is still valid to use i.e. the {@link io.realm.Realm} instance hasn't
     * been closed.
     *
     * @return {@code true} if still valid to use, {@code false} otherwise or if it is a standalone object.
     */
    public boolean isValid() {
        //noinspection SimplifiableIfStatement
        if (!managedMode) {
            return false;
        }

        return isAttached();
    }

    private boolean isAttached() {
        return view != null && view.isAttached();
    }

    /**
     * Inserts the specified object into this List at the specified location. The object is inserted before any previous
     * element at the specified location. If the location is equal to the size of this List, the object is added at the
     * end.
     * <ol>
     * <li><b>Un-managed RealmLists:</b> It is possible to add both managed and un-managed objects. If adding managed
     * objects to a un-managed RealmList they will not be copied to the Realm again if using
     * {@link Realm#copyToRealm(RealmObject)} afterwards.</li>
     *
     * <li><b>Managed RealmLists:</b> It is possible to add un-managed objects to a RealmList that is already managed. In
     * that case the object will transparently be copied to Realm using {@link Realm#copyToRealm(RealmObject)}
     * or {@link Realm#copyToRealmOrUpdate(RealmObject)} if it has a primary key.</li>
     * </ol>
     *
     * @param location the index at which to insert.
     * @param object the object to add.
     * @throws IllegalStateException if Realm instance has been closed or container object has been removed.
     * @throws IndexOutOfBoundsException if {@code location < 0 || location > size()}.
     */
    @Override
    public void add(int location, E object) {
        checkValidObject(object);
        if (managedMode) {
            checkValidView();
            object = copyToRealmIfNeeded(object);
            view.insert(location, object.row.getIndex());
        } else {
            nonManagedList.add(location, object);
        }
    }

    /**
     * Adds the specified object at the end of this List.
     * <ol>
     * <li><b>Un-managed RealmLists:</b> It is possible to add both managed and un-managed objects. If adding managed
     * objects to a un-managed RealmList they will not be copied to the Realm again if using
     * {@link Realm#copyToRealm(RealmObject)} afterwards.</li>
     *
     * <li><b>Managed RealmLists:</b> It is possible to add un-managed objects to a RealmList that is already managed. In
     * that case the object will transparently be copied to Realm using {@link Realm#copyToRealm(RealmObject)}
     * or {@link Realm#copyToRealmOrUpdate(RealmObject)} if it has a primary key.</li>
     * </ol>
     *
     * @param object the object to add.
     * @return always true
     * @throws IllegalStateException if Realm instance has been closed or parent object has been removed.
     */
    @Override
    public boolean add(E object) {
        checkValidObject(object);
        if (managedMode) {
            checkValidView();
            object = copyToRealmIfNeeded(object);
            view.add(object.row.getIndex());
        } else {
            nonManagedList.add(object);
        }
        return true;
    }

    /**
     * Replaces the element at the specified location in this list with the specified object.
     * <ol>
     * <li><b>Un-managed RealmLists:</b> It is possible to add both managed and un-managed objects. If adding managed
     * objects to a un-managed RealmList they will not be copied to the Realm again if using
     * {@link Realm#copyToRealm(RealmObject)} afterwards.</li>
     *
     * <li><b>Managed RealmLists:</b> It is possible to add un-managed objects to a RealmList that is already managed.
     * In that case the object will transparently be copied to Realm using {@link Realm#copyToRealm(RealmObject)} or
     * {@link Realm#copyToRealmOrUpdate(RealmObject)} if it has a primary key.</li>
     * </ol>
     * @param location the index at which to put the specified object.
     * @param object the object to add.
     * @return the previous element at the index.
     * @throws IllegalStateException if Realm instance has been closed or parent object has been removed.
     * @throws IndexOutOfBoundsException if {@code location < 0 || location >= size()}.
     */
    @Override
    public E set(int location, E object) {
        checkValidObject(object);
        if (managedMode) {
            checkValidView();
            object = copyToRealmIfNeeded(object);
            view.set(location, object.row.getIndex());
        } else {
            nonManagedList.set(location, object);
        }
        return object;
    }

    // Transparently copies a standalone object or managed object from another Realm to the Realm backing this RealmList.
    private E copyToRealmIfNeeded(E object) {
        // Object is already in this realm
        if (object.row != null && object.realm.getPath().equals(realm.getPath())) {
            return object;
        }

        // We don't support moving DynamicRealmObjects across Realms automatically. The overhead is too big as you
        // have to run a full schema validation for each object.
        if (object instanceof DynamicRealmObject) {
            throw new IllegalArgumentException("Automatically copying DynamicRealmObjects from other Realms are not supported");
        }

        // At this point the object can only be a typed object, so the backing Realm cannot be a DynamicRealm.
        Realm realm = (Realm) this.realm;
        if (realm.getTable(object.getClass()).hasPrimaryKey()) {
            return realm.copyToRealmOrUpdate(object);
        } else {
            return realm.copyToRealm(object);
        }
    }

    /**
     * Moves an object from one position to another, while maintaining a fixed sized list.
     * RealmObjects will be shifted so no null values are introduced.
     *
     * @param oldPos index of RealmObject to move.
     * @param newPos target position. If newPos &lt; oldPos the object at the location will be shifted to the right. If
     *               oldPos &lt; newPos, indexes &gt; oldPos will be shifted once to the left.
     * @throws IllegalStateException if Realm instance has been closed or parent object has been removed.
     * @throws java.lang.IndexOutOfBoundsException if any position is outside [0, size()].
     */
    public void move(int oldPos, int newPos) {
        if (managedMode) {
            checkValidView();
            view.move(oldPos, newPos);
        } else {
            checkIndex(oldPos);
            checkIndex(newPos);
            E object = nonManagedList.remove(oldPos);
            if (newPos > oldPos) {
                nonManagedList.add(newPos - 1, object);
            } else {
                nonManagedList.add(newPos, object);
            }
        }
    }

    /**
     * Removes all elements from this list, leaving it empty.
     *
     * @throws IllegalStateException if Realm instance has been closed or parent object has been removed.
     * @see List#isEmpty
     * @see List#size
     */
    @Override
    public void clear() {
        if (managedMode) {
            checkValidView();
            view.clear();
        } else {
            nonManagedList.clear();
        }
    }

    /**
     * Removes the object at the specified location from this list.
     *
     * @param location the index of the object to remove.
     * @return the removed object.
     * @throws IllegalStateException if Realm instance has been closed or parent object has been removed.
     * @throws IndexOutOfBoundsException if {@code location < 0 || location >= size()}.
     */
    @Override
    public E remove(int location) {
        if (managedMode) {
            checkValidView();
            E removedItem = get(location);
            view.remove(location);
            return removedItem;
        } else {
            return nonManagedList.remove(location);
        }
    }

    /**
     * Returns the element at the specified location in this list.
     *
     * @param location the index of the element to return.
     * @return the element at the specified index.
     * @throws IllegalStateException if Realm instance has been closed or parent object has been removed.
     * @throws IndexOutOfBoundsException if {@code location < 0 || location >= size()}.
     */
    @Override
    public E get(int location) {
        if (managedMode) {
            checkValidView();
            long rowIndex = view.getTargetRowIndex(location);
            return realm.get(clazz, className, rowIndex);
        } else {
            return nonManagedList.get(location);
        }
    }

    /**
     * Finds the first object.
     *
     * @return the first object or {@code null} if the list is empty.
     * @throws IllegalStateException if Realm instance has been closed or parent object has been removed.
     */
    public E first() {
        if (managedMode) {
            checkValidView();
            return view.isEmpty() ? null : get(0);
        } else if (nonManagedList != null && nonManagedList.size() > 0) {
            return nonManagedList.get(0);
        }
        return null;
    }

    /**
     * Finds the last object.
     *
     * @return the last object or {@code null} if the list is empty.
     * @throws IllegalStateException if Realm instance has been closed or parent object has been removed.
     */
    public E last() {
        if (managedMode) {
            checkValidView();
            return view.isEmpty() ? null : get((int) view.size() - 1);
        } else if (nonManagedList != null && nonManagedList.size() > 0) {
            return nonManagedList.get(nonManagedList.size() - 1);
        }
        return null;
    }

    /**
     * Returns the number of elements in this {@code List}.
     *
     * @return the number of elements in this {@code List}.
     * @throws IllegalStateException if Realm instance has been closed or parent object has been removed.
     */
    @Override
    public int size() {
        if (managedMode) {
            checkValidView();
            long size = view.size();
            return size < Integer.MAX_VALUE ? (int) size : Integer.MAX_VALUE;
        } else {
            return nonManagedList.size();
        }
    }

    /**
     * Returns a RealmQuery, which can be used to query for specific objects of this class.
     *
     * @return a RealmQuery object.
     * @throws IllegalStateException if Realm instance has been closed or parent object has been removed.
     * @see io.realm.RealmQuery
     */
    public RealmQuery<E> where() {
        if (managedMode) {
            checkValidView();
            return RealmQuery.createQueryFromList(this);
        } else {
            throw new RealmException(ONLY_IN_MANAGED_MODE_MESSAGE);
        }
    }

    private void checkValidObject(E object) {
        if (object == null) {
            throw new IllegalArgumentException(NULL_OBJECTS_NOT_ALLOWED_MESSAGE);
        }
    }

    private void checkIndex(int location) {
        int size = size();
        if (location < 0 || location >= size) {
            throw new IndexOutOfBoundsException("Invalid index " + location + ", size is " + size);
        }
    }

    private void checkValidView() {
        realm.checkIfValid();
        if (view == null || !view.isAttached()) {
            throw new IllegalStateException("Realm instance has been closed or parent object has been removed.");
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(managedMode ? clazz.getSimpleName() : getClass().getSimpleName());
        sb.append("@[");
        if (managedMode && !isAttached()) {
            sb.append("invalid");
        } else {
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
        }
        sb.append("]");
        return sb.toString();
    }
}
