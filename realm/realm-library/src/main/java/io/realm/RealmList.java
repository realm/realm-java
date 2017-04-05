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
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import io.realm.internal.InvalidRow;
import io.realm.internal.LinkView;
import io.realm.internal.RealmObjectProxy;
import rx.Observable;


/**
 * RealmList is used to model one-to-many relationships in a {@link io.realm.RealmObject}.
 * RealmList has two modes: A managed and unmanaged mode. In managed mode all objects are persisted inside a Realm, in
 * unmanaged mode it works as a normal ArrayList.
 * <p>
 * Only Realm can create managed RealmLists. Managed RealmLists will automatically update the content whenever the
 * underlying Realm is updated, and can only be accessed using the getter of a {@link io.realm.RealmObject}.
 * <p>
 * Unmanaged RealmLists can be created by the user and can contain both managed and unmanaged RealmObjects. This is
 * useful when dealing with JSON deserializers like GSON or other frameworks that inject values into a class.
 * Unmanaged elements in this list can be added to a Realm using the {@link Realm#copyToRealm(Iterable)} method.
 * <p>
 * {@link RealmList} can contain more elements than {@code Integer.MAX_VALUE}.
 * In that case, you can access only first {@code Integer.MAX_VALUE} elements in it.
 *
 * @param <E> the class of objects in list.
 */

public class RealmList<E extends RealmModel> extends AbstractList<E> implements OrderedRealmCollection<E> {

    private static final String ONLY_IN_MANAGED_MODE_MESSAGE = "This method is only available in managed mode";
    private static final String NULL_OBJECTS_NOT_ALLOWED_MESSAGE = "RealmList does not accept null values";
    public static final String REMOVE_OUTSIDE_TRANSACTION_ERROR = "Objects can only be removed from inside a write transaction";

    private final io.realm.internal.Collection collection;
    protected Class<E> clazz;
    protected String className;
    final LinkView view;
    protected BaseRealm realm;
    private List<E> unmanagedList;

    /**
     * Creates a RealmList in unmanaged mode, where the elements are not controlled by a Realm.
     * This effectively makes the RealmList function as a {@link java.util.ArrayList} and it is not possible to query
     * the objects in this state.
     * <p>
     * Use {@link io.realm.Realm#copyToRealm(Iterable)} to properly persist its elements in Realm.
     */
    public RealmList() {
        collection = null;
        view = null;
        unmanagedList = new ArrayList<>();
    }

    /**
     * Creates a RealmList in unmanaged mode with an initial list of elements.
     * A RealmList in unmanaged mode function as a {@link java.util.ArrayList} and it is not possible to query the
     * objects in this state.
     * <p>
     * Use {@link io.realm.Realm#copyToRealm(Iterable)} to properly persist all unmanaged elements in Realm.
     *
     * @param objects initial objects in the list.
     */
    public RealmList(E... objects) {
        if (objects == null) {
            throw new IllegalArgumentException("The objects argument cannot be null");
        }
        collection = null;
        view = null;
        unmanagedList = new ArrayList<>(objects.length);
        Collections.addAll(unmanagedList, objects);
    }

    /**
     * Creates a RealmList from a LinkView, so its elements are managed by Realm.
     *
     * @param clazz type of elements in the Array.
     * @param linkView backing LinkView.
     * @param realm reference to Realm containing the data.
     */
    RealmList(Class<E> clazz, LinkView linkView, BaseRealm realm) {
        this.collection = new io.realm.internal.Collection(realm.sharedRealm, linkView, null);
        this.clazz = clazz;
        this.view = linkView;
        this.realm = realm;
    }

    RealmList(String className, LinkView linkView, BaseRealm realm) {
        this.collection = new io.realm.internal.Collection(realm.sharedRealm, linkView, null);
        this.view = linkView;
        this.realm = realm;
        this.className = className;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid() {
        if (realm == null) {
            return true;
        }
        //noinspection SimplifiableIfStatement
        if (realm.isClosed()) {
            return false;
        }
        return isAttached();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isManaged() {
        return realm != null;
    }

    private boolean isAttached() {
        return view != null && view.isAttached();
    }

    /**
     * Inserts the specified object into this List at the specified location. The object is inserted before any previous
     * element at the specified location. If the location is equal to the size of this List, the object is added at the
     * end.
     * <ol>
     * <li><b>Unmanaged RealmLists</b>: It is possible to add both managed and unmanaged objects. If adding managed
     * objects to an unmanaged RealmList they will not be copied to the Realm again if using
     * {@link Realm#copyToRealm(RealmModel)} afterwards.</li>
     * <li><b>Managed RealmLists</b>: It is possible to add unmanaged objects to a RealmList that is already managed. In
     * that case the object will transparently be copied to Realm using {@link Realm#copyToRealm(RealmModel)}
     * or {@link Realm#copyToRealmOrUpdate(RealmModel)} if it has a primary key.</li>
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
        if (isManaged()) {
            checkValidView();
            if (location < 0 || location > size()) {
                throw new IndexOutOfBoundsException("Invalid index " + location + ", size is " + size());
            }
            RealmObjectProxy proxy = (RealmObjectProxy) copyToRealmIfNeeded(object);
            view.insert(location, proxy.realmGet$proxyState().getRow$realm().getIndex());
        } else {
            unmanagedList.add(location, object);
        }
        modCount++;
    }

    /**
     * Adds the specified object at the end of this List.
     * <ol>
     * <li><b>Unmanaged RealmLists</b>: It is possible to add both managed and unmanaged objects. If adding managed
     * objects to an unmanaged RealmList they will not be copied to the Realm again if using
     * {@link Realm#copyToRealm(RealmModel)} afterwards.</li>
     * <li><b>Managed RealmLists</b>: It is possible to add unmanaged objects to a RealmList that is already managed. In
     * that case the object will transparently be copied to Realm using {@link Realm#copyToRealm(RealmModel)}
     * or {@link Realm#copyToRealmOrUpdate(RealmModel)} if it has a primary key.</li>
     * </ol>
     *
     * @param object the object to add.
     * @return always {@code true}.
     * @throws IllegalStateException if Realm instance has been closed or parent object has been removed.
     */
    @Override
    public boolean add(E object) {
        checkValidObject(object);
        if (isManaged()) {
            checkValidView();
            RealmObjectProxy proxy = (RealmObjectProxy) copyToRealmIfNeeded(object);
            view.add(proxy.realmGet$proxyState().getRow$realm().getIndex());
        } else {
            unmanagedList.add(object);
        }
        modCount++;
        return true;
    }

    /**
     * Replaces the element at the specified location in this list with the specified object.
     * <ol>
     * <li><b>Unmanaged RealmLists</b>: It is possible to add both managed and unmanaged objects. If adding managed
     * objects to an unmanaged RealmList they will not be copied to the Realm again if using
     * {@link Realm#copyToRealm(RealmModel)} afterwards.</li>
     * <li><b>Managed RealmLists</b>: It is possible to add unmanaged objects to a RealmList that is already managed.
     * In that case the object will transparently be copied to Realm using {@link Realm#copyToRealm(RealmModel)} or
     * {@link Realm#copyToRealmOrUpdate(RealmModel)} if it has a primary key.</li>
     * </ol>
     *
     * @param location the index at which to put the specified object.
     * @param object the object to add.
     * @return the previous element at the index.
     * @throws IllegalStateException if Realm instance has been closed or parent object has been removed.
     * @throws IndexOutOfBoundsException if {@code location < 0 || location >= size()}.
     */
    @Override
    public E set(int location, E object) {
        checkValidObject(object);
        E oldObject;
        if (isManaged()) {
            checkValidView();
            RealmObjectProxy proxy = (RealmObjectProxy) copyToRealmIfNeeded(object);
            oldObject = get(location);
            view.set(location, proxy.realmGet$proxyState().getRow$realm().getIndex());
            return oldObject;
        } else {
            oldObject = unmanagedList.set(location, object);
        }
        return oldObject;
    }

    // Transparently copies an unmanaged object or managed object from another Realm to the Realm backing this RealmList.
    private E copyToRealmIfNeeded(E object) {
        if (object instanceof RealmObjectProxy) {
            RealmObjectProxy proxy = (RealmObjectProxy) object;

            if (proxy instanceof DynamicRealmObject) {
                String listClassName = StandardRealmSchema.getSchemaForTable(view.getTargetTable());
                if (proxy.realmGet$proxyState().getRealm$realm() == realm) {
                    String objectClassName = ((DynamicRealmObject) object).getType();
                    if (listClassName.equals(objectClassName)) {
                        // Same Realm instance and same target table
                        return object;
                    } else {
                        // Different target table
                        throw new IllegalArgumentException(String.format("The object has a different type from list's." +
                                " Type of the list is '%s', type of object is '%s'.", listClassName, objectClassName));
                    }
                } else if (realm.threadId == proxy.realmGet$proxyState().getRealm$realm().threadId) {
                    // We don't support moving DynamicRealmObjects across Realms automatically. The overhead is too big as
                    // you have to run a full schema validation for each object.
                    // And copying from another Realm instance pointed to the same Realm file is not supported as well.
                    throw new IllegalArgumentException("Cannot copy DynamicRealmObject between Realm instances.");
                } else {
                    throw new IllegalStateException("Cannot copy an object to a Realm instance created in another thread.");
                }
            } else {
                // Object is already in this realm
                if (proxy.realmGet$proxyState().getRow$realm() != null && proxy.realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                    if (realm != proxy.realmGet$proxyState().getRealm$realm()) {
                        throw new IllegalArgumentException("Cannot copy an object from another Realm instance.");
                    }
                    return object;
                }
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
     * RealmObjects will be shifted so no {@code null} values are introduced.
     *
     * @param oldPos index of RealmObject to move.
     * @param newPos target position. If newPos &lt; oldPos the object at the location will be shifted to the right. If
     * oldPos &lt; newPos, indexes &gt; oldPos will be shifted once to the left.
     * @throws IllegalStateException if Realm instance has been closed or parent object has been removed.
     * @throws java.lang.IndexOutOfBoundsException if any position is outside [0, size()].
     */
    public void move(int oldPos, int newPos) {
        if (isManaged()) {
            checkValidView();
            view.move(oldPos, newPos);
        } else {
            checkIndex(oldPos);
            checkIndex(newPos);
            E object = unmanagedList.remove(oldPos);
            if (newPos > oldPos) {
                unmanagedList.add(newPos - 1, object);
            } else {
                unmanagedList.add(newPos, object);
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
        if (isManaged()) {
            checkValidView();
            view.clear();
        } else {
            unmanagedList.clear();
        }
        modCount++;
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
        E removedItem;
        if (isManaged()) {
            checkValidView();
            removedItem = get(location);
            view.remove(location);
        } else {
            removedItem = unmanagedList.remove(location);
        }
        modCount++;
        return removedItem;
    }

    /**
     * Removes one instance of the specified object from this {@code Collection} if one
     * is contained. This implementation iterates over this
     * {@code Collection} and tests each element {@code e} returned by the iterator,
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
     * @throws NullPointerException if {@code object} is {@code null}.
     */
    @Override
    public boolean remove(Object object) {
        if (isManaged() && !realm.isInTransaction()) {
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
     * This implementation iterates over the {@code Collection} and tests each
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
        if (isManaged() && !realm.isInTransaction()) {
            throw new IllegalStateException(REMOVE_OUTSIDE_TRANSACTION_ERROR);
        }
        return super.removeAll(collection);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteFirstFromRealm() {
        if (isManaged()) {
            if (size() > 0) {
                deleteFromRealm(0);
                modCount++;
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
        if (isManaged()) {
            if (size() > 0) {
                deleteFromRealm(size() - 1);
                modCount++;
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
        if (isManaged()) {
            checkValidView();
            long rowIndex = view.getTargetRowIndex(location);
            return realm.get(clazz, className, rowIndex);
        } else {
            return unmanagedList.get(location);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E first() {
        return firstImpl(true, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E first(E defaultValue) {
        return firstImpl(false, defaultValue);
    }

    private E firstImpl(boolean shouldThrow, E defaultValue) {
        if (isManaged()) {
            checkValidView();
            if (!view.isEmpty()) {
                return get(0);
            }
        } else if (unmanagedList != null && !unmanagedList.isEmpty()) {
            return unmanagedList.get(0);
        }

        if (shouldThrow) {
            throw new IndexOutOfBoundsException("The list is empty.");
        } else {
            return defaultValue;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E last() {
        return lastImpl(true, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E last(E defaultValue) {
        return lastImpl(false, defaultValue);
    }

    private E lastImpl(boolean shouldThrow, E defaultValue) {
        if (isManaged()) {
            checkValidView();
            if (!view.isEmpty()) {
                return get((int) view.size() - 1);
            }
        } else if (unmanagedList != null && !unmanagedList.isEmpty()) {
            return unmanagedList.get(unmanagedList.size() - 1);
        }

        if (shouldThrow) {
            throw new IndexOutOfBoundsException("The list is empty.");
        } else {
            return defaultValue;
        }
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
        if (isManaged()) {
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
        return sort(new String[] {fieldName1, fieldName2}, new Sort[] {sortOrder1, sortOrder2});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RealmResults<E> sort(String[] fieldNames, Sort[] sortOrders) {
        if (isManaged()) {
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
        if (isManaged()) {
            checkValidView();
            view.removeTargetRow(location);
            modCount++;
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
        if (isManaged()) {
            checkValidView();
            long size = view.size();
            return size < Integer.MAX_VALUE ? (int) size : Integer.MAX_VALUE;
        } else {
            return unmanagedList.size();
        }
    }

    /**
     * Returns a RealmQuery, which can be used to query for specific objects of this class.
     *
     * @return a RealmQuery object.
     * @throws IllegalStateException if Realm instance has been closed or parent object has been removed.
     * @see io.realm.RealmQuery
     */
    @Override
    public RealmQuery<E> where() {
        if (isManaged()) {
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
        if (isManaged()) {
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
        if (isManaged()) {
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
        if (isManaged()) {
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
        if (isManaged()) {
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
        if (isManaged()) {
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
        if (isManaged()) {
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
        if (isManaged()) {
            checkValidView();
            if (size() > 0) {
                view.removeAllTargetRows();
                modCount++;
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
        return true; // Managed RealmLists are always loaded, Unmanaged RealmLists return true pr. the contract.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean load() {
        return true; // Managed RealmLists are always loaded, Unmanaged RealmLists return true pr. the contract.
    }

    /**
     * Returns {@code true} if the list contains the specified element when attached to a Realm. This
     * method will query the native Realm underlying storage engine to quickly find the specified element.
     * <p>
     * If the list is not attached to a Realm, the default {@link List#contains(Object)}
     * implementation will occur.
     *
     * @param object the element whose presence in this list is to be tested.
     * @return {@code true} if this list contains the specified element otherwise {@code false}.
     */
    @Override
    public boolean contains(Object object) {
        if (isManaged()) {
            realm.checkIfValid();

            // Deleted objects can never be part of a RealmList
            if (object instanceof RealmObjectProxy) {
                RealmObjectProxy proxy = (RealmObjectProxy) object;
                if (proxy.realmGet$proxyState().getRow$realm() == InvalidRow.INSTANCE) {
                    return false;
                }
            }

            for (E e : this) {
                if (e.equals(object)) {
                    return true;
                }
            }
            return false;
        } else {
            return unmanagedList.contains(object);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<E> iterator() {
        if (isManaged()) {
            return new RealmItr();
        } else {
            return super.iterator();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ListIterator<E> listIterator() {
        return listIterator(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ListIterator<E> listIterator(int location) {
        if (isManaged()) {
            return new RealmListItr(location);
        } else {
            return super.listIterator(location);
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
            throw new IllegalStateException("Realm instance has been closed or this object or its parent has been deleted.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrderedRealmCollectionSnapshot<E> createSnapshot() {
        if (!isManaged()) {
            throw new UnsupportedOperationException(ONLY_IN_MANAGED_MODE_MESSAGE);
        }
        checkValidView();
        if (className != null) {
            return new OrderedRealmCollectionSnapshot<>(
                    realm,
                    new io.realm.internal.Collection(realm.sharedRealm, view, null),
                    className);
        } else {
            return new OrderedRealmCollectionSnapshot<>(
                    realm,
                    new io.realm.internal.Collection(realm.sharedRealm, view, null),
                    clazz);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(isManaged() ? clazz.getSimpleName() : getClass().getSimpleName());
        sb.append("@[");
        if (isManaged() && !isAttached()) {
            sb.append("invalid");
        } else {
            for (int i = 0; i < size(); i++) {
                if (isManaged()) {
                    sb.append(((RealmObjectProxy) get(i)).realmGet$proxyState().getRow$realm().getIndex());
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


    /**
     * Returns an Rx Observable that monitors changes to this RealmList. It will emit the current RealmList when
     * subscribed to. RealmList will continually be emitted as the RealmList is updated -
     * {@code onComplete} will never be called.
     * <p>
     * If you would like the {@code asObservable()} to stop emitting items you can instruct RxJava to
     * only emit only the first item by using the {@code first()} operator:
     * <p>
     * <pre>
     * {@code
     * list.asObservable()
     *      .first()
     *      .subscribe( ... ) // You only get the results once
     * }
     * </pre>
     * <p>
     * <p>Note that when the {@link Realm} is accessed from threads other than where it was created,
     * {@link IllegalStateException} will be thrown. Care should be taken when using different schedulers
     * with {@code subscribeOn()} and {@code observeOn()}.
     *
     * @return RxJava Observable that only calls {@code onNext}. It will never call {@code onComplete} or {@code OnError}.
     * @throws UnsupportedOperationException if the required RxJava framework is not on the classpath or the
     * corresponding Realm instance doesn't support RxJava.
     * @see <a href="https://realm.io/docs/java/latest/#rxjava">RxJava and Realm</a>
     */
    @SuppressWarnings("unchecked")
    public Observable<RealmList<E>> asObservable() {
        if (realm instanceof Realm) {
            return realm.configuration.getRxFactory().from((Realm) realm, this);
        } else if (realm instanceof DynamicRealm) {
            DynamicRealm dynamicRealm = (DynamicRealm) realm;
            RealmList<DynamicRealmObject> dynamicList = (RealmList<DynamicRealmObject>) this;
            @SuppressWarnings("UnnecessaryLocalVariable")
            Observable results = realm.configuration.getRxFactory().from(dynamicRealm, dynamicList);
            return results;
        } else {
            throw new UnsupportedOperationException(realm.getClass() + " does not support RxJava.");
        }
    }

    private void checkForAddRemoveListener(Object listener, boolean checkListener) {
        if (checkListener && listener == null) {
            throw new IllegalArgumentException("Listener should not be null");
        }
        realm.checkIfValid();
        realm.sharedRealm.capabilities.checkCanDeliverNotification(BaseRealm.LISTENER_NOT_ALLOWED_MESSAGE);
    }

    /**
     * Adds a change listener to this {@link RealmList}.
     *
     * @param listener the change listener to be notified.
     * @throws IllegalArgumentException if the change listener is {@code null}.
     * @throws IllegalStateException if you try to add a listener from a non-Looper or
     * {@link android.app.IntentService} thread.
     */
    public void addChangeListener(OrderedRealmCollectionChangeListener<RealmList<E>> listener) {
        checkForAddRemoveListener(listener, true);
        collection.addListener(this, listener);
    }

    /**
     * Removes the specified change listener.
     *
     * @param listener the change listener to be removed.
     * @throws IllegalArgumentException if the change listener is {@code null}.
     * @throws IllegalStateException if you try to remove a listener from a non-Looper Thread.
     * @see io.realm.RealmChangeListener
     */
    public void removeChangeListener(OrderedRealmCollectionChangeListener<RealmList<E>> listener) {
        checkForAddRemoveListener(listener, true);
        collection.removeListener(this, listener);
    }

    /**
     * Adds a change listener to this {@link RealmList}.
     *
     * @param listener the change listener to be notified.
     * @throws IllegalArgumentException if the change listener is {@code null}.
     * @throws IllegalStateException if you try to add a listener from a non-Looper or
     * {@link android.app.IntentService} thread.
     */
    public void addChangeListener(RealmChangeListener<RealmList<E>> listener) {
        checkForAddRemoveListener(listener, true);
        collection.addListener(this, listener);
    }

    /**
     * Removes the specified change listener.
     *
     * @param listener the change listener to be removed.
     * @throws IllegalArgumentException if the change listener is {@code null}.
     * @throws IllegalStateException if you try to remove a listener from a non-Looper Thread.
     * @see io.realm.RealmChangeListener
     */
    public void removeChangeListener(RealmChangeListener<RealmList<E>> listener) {
        checkForAddRemoveListener(listener, true);
        collection.removeListener(this, listener);
    }

    /**
     * Removes all user-defined change listeners.
     *
     * @throws IllegalStateException if you try to remove listeners from a non-Looper Thread.
     * @see io.realm.RealmChangeListener
     */
    public void removeAllChangeListeners() {
        checkForAddRemoveListener(null, false);
        collection.removeAllListeners();
    }

    // Custom RealmList iterator.
    private class RealmItr implements Iterator<E> {
        /**
         * Index of element to be returned by subsequent call to next.
         */
        int cursor = 0;

        /**
         * Index of element returned by most recent call to next or
         * previous. Resets to -1 if this element is deleted by a call
         * to remove.
         */
        int lastRet = -1;

        /**
         * The modCount value that the iterator believes that the backing
         * List should have. If this expectation is violated, the iterator
         * has detected concurrent modification.
         */
        int expectedModCount = modCount;

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasNext() {
            realm.checkIfValid();
            checkConcurrentModification();
            return cursor != size();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public E next() {
            realm.checkIfValid();
            checkConcurrentModification();
            int i = cursor;
            try {
                E next = get(i);
                lastRet = i;
                cursor = i + 1;
                return next;
            } catch (IndexOutOfBoundsException e) {
                checkConcurrentModification();
                throw new NoSuchElementException("Cannot access index " + i + " when size is " + size() + ". Remember to check hasNext() before using next().");
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void remove() {
            realm.checkIfValid();
            if (lastRet < 0) {
                throw new IllegalStateException("Cannot call remove() twice. Must call next() in between.");
            }
            checkConcurrentModification();

            try {
                RealmList.this.remove(lastRet);
                if (lastRet < cursor) {
                    cursor--;
                }
                lastRet = -1;
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException e) {
                throw new ConcurrentModificationException();
            }
        }

        final void checkConcurrentModification() {
            // A Realm ListView is backed by the original Table and not a TableView, this means
            // that all changes are reflected immediately. It is therefore not possible to use
            // the same version pinning trick we use for RealmResults (avoiding calling sync_if_needed)
            // Fortunately a LinkView does not change unless manually altered (unlike RealmResults)
            // So therefore it should be acceptable to use the same heuristic as a normal AbstractList
            // when detecting concurrent modifications.
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
        }
    }

    private class RealmListItr extends RealmItr implements ListIterator<E> {

        RealmListItr(int index) {
            if (index >= 0 && index <= size()) {
                cursor = index;
            } else {
                throw new IndexOutOfBoundsException("Starting location must be a valid index: [0, " + (size() - 1) + "]. Index was " + index);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasPrevious() {
            return cursor != 0;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public E previous() {
            checkConcurrentModification();
            int i = cursor - 1;
            try {
                E previous = get(i);
                lastRet = cursor = i;
                return previous;
            } catch (IndexOutOfBoundsException e) {
                checkConcurrentModification();
                throw new NoSuchElementException("Cannot access index less than zero. This was " + i + ". Remember to check hasPrevious() before using previous().");
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int nextIndex() {
            return cursor;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int previousIndex() {
            return cursor - 1;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void set(E e) {
            realm.checkIfValid();
            if (lastRet < 0) {
                throw new IllegalStateException();
            }
            checkConcurrentModification();

            try {
                RealmList.this.set(lastRet, e);
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }

        /**
         * Adding a new object to the RealmList. If the object is not already manage by Realm it will be transparently
         * copied using {@link Realm#copyToRealmOrUpdate(RealmModel)}
         *
         * @see #add(RealmModel)
         */
        @Override
        public void add(E e) {
            realm.checkIfValid();
            checkConcurrentModification();
            try {
                int i = cursor;
                RealmList.this.add(i, e);
                lastRet = -1;
                cursor = i + 1;
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }
    }
}
