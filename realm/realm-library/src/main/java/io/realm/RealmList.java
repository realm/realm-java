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
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import io.realm.internal.InvalidRow;
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
 * {@link RealmList} can contain more elements than {@code Integer.MAX_VALUE}.
 * In that case, you can access only first {@code Integer.MAX_VALUE} elements in it.
 *
 * @param <E> the class of objects in list.
 */

public class RealmList<E extends RealmObject> extends AbstractList<E> implements OrderedRealmCollection<E> {

    private static final String ONLY_IN_MANAGED_MODE_MESSAGE = "This method is only available in managed mode";
    private static final String NULL_OBJECTS_NOT_ALLOWED_MESSAGE = "RealmList does not accept null values";
    public static final String REMOVE_OUTSIDE_TRANSACTION_ERROR = "Objects can only be removed from inside a write transaction";

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
     * Checks if the {@link RealmList} is managed by Realm and contains valid data i.e. the {@link io.realm.Realm}
     * instance hasn't been closed.
     *
     * @return {@code true} if still valid to use, {@code false} otherwise or if it's an un-managed list.
     */
    public boolean isValid() {
        //noinspection SimplifiableIfStatement
        if (realm == null || realm.isClosed()) {
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
            if (location < 0 || location > size()) {
                throw new IndexOutOfBoundsException("Invalid index " + location + ", size is " + size());
            }
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
            E oldObject = get(location);
            view.set(location, object.row.getIndex());
            return oldObject;
        } else {
            return nonManagedList.set(location, object);
        }
    }

    // Transparently copies a standalone object or managed object from another Realm to the Realm backing this RealmList.
    private E copyToRealmIfNeeded(E object) {
        if (object instanceof DynamicRealmObject) {
            String listClassName = RealmSchema.getSchemaForTable(view.getTargetTable());
            String objectClassName = ((DynamicRealmObject) object).getType();
            if (object.realm == realm) {
                if (listClassName.equals(objectClassName)) {
                    // Same Realm instance and same target table
                    return object;
                } else {
                    // Different target table
                    throw new IllegalArgumentException(String.format("The object has a different type from list's." +
                            " Type of the list is '%s', type of object is '%s'.", listClassName, objectClassName));
                }
            } else if (realm.threadId == object.realm.threadId) {
                // We don't support moving DynamicRealmObjects across Realms automatically. The overhead is too big as
                // you have to run a full schema validation for each object.
                // And copying from another Realm instance pointed to the same Realm file is not supported as well.
                throw new IllegalArgumentException("Cannot copy DynamicRealmObject between Realm instances.");
            } else {
                throw new IllegalStateException("Cannot copy an object to a Realm instance created in another thread.");
            }
        } else {
            // Object is already in this realm
            if (object.row != null && object.realm.getPath().equals(realm.getPath())) {
                if (realm != object.realm) {
                    throw new IllegalArgumentException("Cannot copy an object from another Realm instance.");
                }
                return object;
            }
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
     * Removes all elements from this list, leaving it empty. This method doesn't remove the objects from the Realm.
     *
     * @throws IllegalStateException if Realm instance has been closed or parent object has been removed.
     * @see List#isEmpty
     * @see List#size
     * @see #deleteAllFromRealm()
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
     * Removes one instance of the specified object from this {@code Collection} if one
     * is contained . This implementation iterates over this
     * {@code Collection} and tests for each element {@code e} returned by the iterator,
     * whether {@code e} is equal to the given object. If {@code object != null}
     * then this test is performed using {@code object.equals(e)}, otherwise
     * using {@code object == null}. If an element equal to the given object is
     * found, then the {@code remove} method is called on the iterator and
     * {@code true} is returned, {@code false} otherwise. If the iterator does
     * not support removing elements, an {@code UnsupportedOperationException}
     * is thrown.
     *
     * @param object the object to remove.
     * @return {@code true} if this {@code Collection} is modified, {@code false} otherwise.
     * @throws ClassCastException if the object passed is not of the correct type.
     * @throws NullPointerException  if {@code object} is {@code null}.
     */
    @Override
    public boolean remove(Object object) {
        if (managedMode && !realm.isInTransaction()) {
            throw new IllegalStateException(REMOVE_OUTSIDE_TRANSACTION_ERROR);
        }
        return super.remove(object);
    }

    /**
     * Removes all occurrences in this {@code Collection} of each object in the
     * specified {@code Collection}. After this method returns none of the
     * elements in the passed {@code Collection} can be found in this {@code Collection}
     * anymore.
     * <p>
     * This implementation iterates over this {@code Collection} and tests for each
     * element {@code e} returned by the iterator, whether it is contained in
     * the specified {@code Collection}. If this test is positive, then the {@code
     * remove} method is called on the iterator.
     *
     * @param collection the collection of objects to remove.
     * @return {@code true} if this {@code Collection} is modified, {@code false} otherwise.
     * @throws ClassCastException if one or more elements of {@code collection} isn't of the correct type.
     * @throws NullPointerException if {@code collection} is {@code null}.
     */
    @Override
    public boolean removeAll(Collection<?> collection) {
        if (managedMode && !realm.isInTransaction()) {
            throw new IllegalStateException(REMOVE_OUTSIDE_TRANSACTION_ERROR);
        }
        return super.removeAll(collection);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteFirstFromRealm() {
        if (managedMode) {
            if (size() > 0) {
                deleteFromRealm(0);
                return true;
            } else {
                return false;
            }
        } else {
            throw new UnsupportedOperationException(ONLY_IN_MANAGED_MODE_MESSAGE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteLastFromRealm() {
        if (managedMode) {
            if (size() > 0) {
                deleteFromRealm(size() - 1);
                return true;
            } else {
                return false;
            }
        } else {
            throw new UnsupportedOperationException(ONLY_IN_MANAGED_MODE_MESSAGE);
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
            if (!view.isEmpty()) {
                return get(0);
            }
        } else if (nonManagedList != null && nonManagedList.size() > 0) {
            return nonManagedList.get(0);
        }
        throw new IndexOutOfBoundsException("The list is empty.");
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
            if (!view.isEmpty()) {
                return get((int) view.size() - 1);
            }
        } else if (nonManagedList != null && nonManagedList.size() > 0) {
            return nonManagedList.get(nonManagedList.size() - 1);
        }
        throw new IndexOutOfBoundsException("The list is empty.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RealmResults<E> sort(String fieldName) {
        return this.sort(fieldName, Sort.ASCENDING);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RealmResults<E> sort(String fieldName, Sort sortOrder) {
        if (managedMode) {
            return this.where().findAllSorted(fieldName, sortOrder);
        } else {
            throw new UnsupportedOperationException(ONLY_IN_MANAGED_MODE_MESSAGE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RealmResults<E> sort(String fieldName1, Sort sortOrder1, String fieldName2, Sort sortOrder2) {
        return sort(new String[]{fieldName1, fieldName2}, new Sort[]{sortOrder1, sortOrder2});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RealmResults<E> sort(String[] fieldNames, Sort[] sortOrders) {
        if (managedMode) {
            return where().findAllSorted(fieldNames, sortOrders);
        } else {
            throw new UnsupportedOperationException(ONLY_IN_MANAGED_MODE_MESSAGE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteFromRealm(int location) {
        if (managedMode) {
            checkValidView();
            view.removeTargetRow(location);
        } else {
            throw new UnsupportedOperationException(ONLY_IN_MANAGED_MODE_MESSAGE);
        }
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
            throw new UnsupportedOperationException(ONLY_IN_MANAGED_MODE_MESSAGE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Number min(String fieldName) {
        if (managedMode) {
            return this.where().min(fieldName);
        } else {
            throw new UnsupportedOperationException(ONLY_IN_MANAGED_MODE_MESSAGE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Number max(String fieldName) {
        if (managedMode) {
            return this.where().max(fieldName);
        } else {
            throw new UnsupportedOperationException(ONLY_IN_MANAGED_MODE_MESSAGE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Number sum(String fieldName) {
        if (managedMode) {
            return this.where().sum(fieldName);
        } else {
            throw new UnsupportedOperationException(ONLY_IN_MANAGED_MODE_MESSAGE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double average(String fieldName) {
        if (managedMode) {
            return this.where().average(fieldName);
        } else {
            throw new UnsupportedOperationException(ONLY_IN_MANAGED_MODE_MESSAGE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date maxDate(String fieldName) {
        if (managedMode) {
            return this.where().maximumDate(fieldName);
        } else {
            throw new UnsupportedOperationException(ONLY_IN_MANAGED_MODE_MESSAGE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date minDate(String fieldName) {
        if (managedMode) {
            return this.where().minimumDate(fieldName);
        } else {
            throw new UnsupportedOperationException(ONLY_IN_MANAGED_MODE_MESSAGE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteAllFromRealm() {
        if (managedMode) {
            checkValidView();
            if (size() > 0) {
                view.removeAllTargetRows();
                return true;
            } else {
                return false;
            }
        } else {
            throw new UnsupportedOperationException(ONLY_IN_MANAGED_MODE_MESSAGE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLoaded() {
        return true; // Managed RealmLists are always loaded, Un-managed RealmLists return true pr. the contract.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean load() {
        return true; // Managed RealmLists are always loaded, Un-managed RealmLists return true pr. the contract.
    }

    /**
     * Returns true if the list contains the specified element when attached to a Realm. This
     * method will query the native Realm underlying storage engine to quickly find the specified element.
     *
     * If this list is not attached to a Realm the default {@link List#contains(Object)}
     * implementation will occur.
     *
     * @param object the element whose presence in this list is to be tested.
     * @return {@code true} if this list contains the specified element otherwise {@code false}.
     */
    @Override
    public boolean contains(Object object) {
        boolean contains = false;
        if (managedMode) {
            realm.checkIfValid();
            if (object instanceof RealmObject) {
                RealmObject realmObject = (RealmObject) object;
                if (realmObject.row != null && realm.getPath().equals(realmObject.realm.getPath()) && realmObject.row != InvalidRow.INSTANCE) {
                    contains = view.contains(realmObject.row.getIndex());
                }
            }
        } else {
            contains = nonManagedList.contains(object);
        }
        return contains;
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
            throw new IllegalStateException("Realm instance has been closed or this object or its parent has been deleted.");
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
