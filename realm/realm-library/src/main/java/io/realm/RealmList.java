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
import java.util.Locale;
import java.util.NoSuchElementException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.realm.internal.InvalidRow;
import io.realm.internal.OsList;
import io.realm.internal.OsObjectStore;
import io.realm.internal.OsResults;
import io.realm.internal.RealmObjectProxy;
import io.realm.rx.CollectionChange;


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

public class RealmList<E> extends AbstractList<E> implements OrderedRealmCollection<E> {

    private static final String ONLY_IN_MANAGED_MODE_MESSAGE = "This method is only available in managed mode.";
    static final String ALLOWED_ONLY_FOR_REALM_MODEL_ELEMENT_MESSAGE = "This feature is available only when the element type is implementing RealmModel.";
    private static final String REMOVE_OUTSIDE_TRANSACTION_ERROR = "Objects can only be removed from inside a write transaction.";

    @Nullable
    protected Class<E> clazz;
    @Nullable
    protected String className;

    // Always null if RealmList is unmanaged, always non-null if managed.
    private final ManagedListOperator<E> osListOperator;
    final protected BaseRealm realm;
    private List<E> unmanagedList;

    /**
     * Creates a RealmList in unmanaged mode, where the elements are not controlled by a Realm.
     * This effectively makes the RealmList function as a {@link java.util.ArrayList} and it is not possible to query
     * the objects in this state.
     * <p>
     * Use {@link io.realm.Realm#copyToRealm(Iterable)} to properly persist its elements in Realm.
     */
    public RealmList() {
        realm = null;
        osListOperator = null;
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
        //noinspection ConstantConditions
        if (objects == null) {
            throw new IllegalArgumentException("The objects argument cannot be null");
        }
        realm = null;
        osListOperator = null;
        unmanagedList = new ArrayList<>(objects.length);
        Collections.addAll(unmanagedList, objects);
    }

    /**
     * Creates a RealmList from a OsList, so its elements are managed by Realm.
     *
     * @param clazz type of elements in the Array.
     * @param osList backing {@link OsList}.
     * @param realm reference to Realm containing the data.
     */
    RealmList(Class<E> clazz, OsList osList, BaseRealm realm) {
        this.clazz = clazz;
        osListOperator = getOperator(realm, osList, clazz, null);
        this.realm = realm;
    }

    RealmList(String className, OsList osList, BaseRealm realm) {
        this.realm = realm;
        this.className = className;
        osListOperator = getOperator(realm, osList, null, className);
    }

    OsList getOsList() {
        return osListOperator.getOsList();
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
        return osListOperator != null && osListOperator.isValid();
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
     * @param element the element to add.
     * @throws IllegalStateException if Realm instance has been closed or container object has been removed.
     * @throws IndexOutOfBoundsException if {@code location < 0 || location > size()}.
     */
    @Override
    public void add(int location, @Nullable E element) {
        //noinspection ConstantConditions
        if (isManaged()) {
            checkValidRealm();
            osListOperator.insert(location, element);
        } else {
            unmanagedList.add(location, element);
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
    public boolean add(@Nullable E object) {
        if (isManaged()) {
            checkValidRealm();
            osListOperator.append(object);
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
    public E set(int location, @Nullable E object) {
        E oldObject;
        if (isManaged()) {
            checkValidRealm();
            oldObject = osListOperator.set(location, object);
        } else {
            oldObject = unmanagedList.set(location, object);
        }
        return oldObject;
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
            checkValidRealm();
            osListOperator.move(oldPos, newPos);
        } else {
            final int listSize = unmanagedList.size();
            if (oldPos < 0 || listSize <= oldPos) {
                throw new IndexOutOfBoundsException("Invalid index " + oldPos + ", size is " + listSize);
            }
            if (newPos < 0 || listSize <= newPos) {
                throw new IndexOutOfBoundsException("Invalid index " + newPos + ", size is " + listSize);
            }
            E object = unmanagedList.remove(oldPos);
            unmanagedList.add(newPos, object);
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
            checkValidRealm();
            osListOperator.removeAll();
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
            checkValidRealm();
            removedItem = get(location);
            osListOperator.remove(location);
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
    public boolean remove(@Nullable Object object) {
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
            if (!osListOperator.isEmpty()) {
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
            if (!osListOperator.isEmpty()) {
                osListOperator.deleteLast();
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
    @Nullable
    public E get(int location) {
        if (isManaged()) {
            checkValidRealm();
            return osListOperator.get(location);
        } else {
            return unmanagedList.get(location);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public E first() {
        return firstImpl(true, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public E first(@Nullable E defaultValue) {
        return firstImpl(false, defaultValue);
    }

    @Nullable
    private E firstImpl(boolean shouldThrow, @Nullable E defaultValue) {
        if (isManaged()) {
            checkValidRealm();
            if (!osListOperator.isEmpty()) {
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
    @Nullable
    public E last() {
        return lastImpl(true, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public E last(@Nullable E defaultValue) {
        return lastImpl(false, defaultValue);
    }

    @Nullable
    private E lastImpl(boolean shouldThrow, @Nullable E defaultValue) {
        if (isManaged()) {
            checkValidRealm();
            if (!osListOperator.isEmpty()) {
                return get(osListOperator.size() - 1);
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
            return this.where().sort(fieldName, sortOrder).findAll();
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
            return where().sort(fieldNames, sortOrders).findAll();
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
            checkValidRealm();
            osListOperator.delete(location);
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
            checkValidRealm();
            return osListOperator.size();
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
            checkValidRealm();
            if (!osListOperator.forRealmModel()) {
                throw new UnsupportedOperationException(ALLOWED_ONLY_FOR_REALM_MODEL_ELEMENT_MESSAGE);
            }
            return RealmQuery.createQueryFromList(this);
        } else {
            throw new UnsupportedOperationException(ONLY_IN_MANAGED_MODE_MESSAGE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public Number min(String fieldName) {
        // where() throws if not managed
        return where().min(fieldName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public Number max(String fieldName) {
        // where() throws if not managed
        return this.where().max(fieldName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Number sum(String fieldName) {
        // where() throws if not managed
        return this.where().sum(fieldName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double average(String fieldName) {
        // where() throws if not managed
        return this.where().average(fieldName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public Date maxDate(String fieldName) {
        // where() throws if not managed
        return this.where().maximumDate(fieldName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public Date minDate(String fieldName) {
        // where() throws if not managed
        return this.where().minimumDate(fieldName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteAllFromRealm() {
        if (isManaged()) {
            checkValidRealm();
            if (!osListOperator.isEmpty()) {
                osListOperator.deleteAll();
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
    public boolean contains(@Nullable Object object) {
        if (isManaged()) {
            realm.checkIfValid();

            // Deleted objects can never be part of a RealmList
            if (object instanceof RealmObjectProxy) {
                RealmObjectProxy proxy = (RealmObjectProxy) object;
                if (proxy.realmGet$proxyState().getRow$realm() == InvalidRow.INSTANCE) {
                    return false;
                }
            }

            return super.contains(object);
        } else {
            return unmanagedList.contains(object);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
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
    @Nonnull
    public ListIterator<E> listIterator() {
        return listIterator(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public ListIterator<E> listIterator(int location) {
        if (isManaged()) {
            return new RealmListItr(location);
        } else {
            return super.listIterator(location);
        }
    }

    private void checkValidRealm() {
        realm.checkIfValid();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrderedRealmCollectionSnapshot<E> createSnapshot() {
        if (!isManaged()) {
            throw new UnsupportedOperationException(ONLY_IN_MANAGED_MODE_MESSAGE);
        }
        checkValidRealm();
        if (!osListOperator.forRealmModel()) {
            throw new UnsupportedOperationException(ALLOWED_ONLY_FOR_REALM_MODEL_ELEMENT_MESSAGE);
        }
        if (className != null) {
            return new OrderedRealmCollectionSnapshot<>(
                    realm,
                    OsResults.createFromQuery(realm.sharedRealm, osListOperator.getOsList().getQuery()),
                    className);
        } else {
            // 'clazz' is non-null when 'dynamicClassName' is null.
            //noinspection ConstantConditions
            return new OrderedRealmCollectionSnapshot<>(
                    realm,
                    OsResults.createFromQuery(realm.sharedRealm, osListOperator.getOsList().getQuery()),
                    clazz);
        }
    }

    /**
     * Returns the {@link Realm} instance to which this collection belongs.
     * <p>
     * Calling {@link Realm#close()} on the returned instance is discouraged as it is the same as
     * calling it on the original Realm instance which may cause the Realm to fully close invalidating the
     * list.
     *
     * @return {@link Realm} instance this collection belongs to or {@code null} if the collection is unmanaged.
     * @throws IllegalStateException if the Realm is an instance of {@link DynamicRealm} or the
     * {@link Realm} was already closed.
     */
    public Realm getRealm() {
        if (realm == null) {
            return null;
        }
        realm.checkIfValid();
        if (!(realm instanceof Realm)) {
            throw new IllegalStateException("This method is only available for typed Realms");
        }
        return (Realm) realm;
    }

    @Override
    public String toString() {
        final String separator = ",";
        final StringBuilder sb = new StringBuilder();

        if (!isManaged()) {
            // Build String for unmanaged RealmList

            // Unmanaged RealmList does not know actual element type.
            sb.append("RealmList<?>@[");
            // Print list values
            final int size = size();
            for (int i = 0; i < size; i++) {
                final E value = get(i);
                if (value instanceof RealmModel) {
                    sb.append(System.identityHashCode(value));
                } else {
                    if (value instanceof byte[]) {
                        sb.append("byte[").append(((byte[]) value).length).append("]");
                    } else {
                        sb.append(value);
                    }
                }
                sb.append(separator);
            }
            if (0 < size()) {
                sb.setLength(sb.length() - separator.length());
            }
            sb.append("]");
        } else {
            // Build String for managed RealmList

            // Determines type of List
            sb.append("RealmList<");
            if (className != null) {
                sb.append(className);
            } else {
                // 'clazz' is non-null when 'dynamicClassName' is null.
                //noinspection ConstantConditions,unchecked
                if (isClassForRealmModel(clazz)) {
                    //noinspection ConstantConditions,unchecked
                    sb.append(realm.getSchema().getSchemaForClass((Class<RealmModel>) clazz).getClassName());
                } else {
                    if (clazz == byte[].class) {
                        sb.append(clazz.getSimpleName());
                    } else {
                        sb.append(clazz.getName());
                    }
                }
            }
            sb.append(">@[");

            //Print list values
            if (!isAttached()) {
                sb.append("invalid");
            } else if (isClassForRealmModel(clazz)) {
                for (int i = 0; i < size(); i++) {
                    //noinspection ConstantConditions
                    sb.append(((RealmObjectProxy) get(i)).realmGet$proxyState().getRow$realm().getIndex());
                    sb.append(separator);
                }
                if (0 < size()) {
                    sb.setLength(sb.length() - separator.length());
                }
            } else {
                for (int i = 0; i < size(); i++) {
                    final E value = get(i);
                    if (value instanceof byte[]) {
                        sb.append("byte[").append(((byte[]) value).length).append("]");
                    } else {
                        sb.append(value);
                    }
                    sb.append(separator);
                }
                if (0 < size()) {
                    sb.setLength(sb.length() - separator.length());
                }
            }
            sb.append("]");
        }
        return sb.toString();
    }

    /**
     * Returns an Rx Flowable that monitors changes to this RealmList. It will emit the current RealmList when
     * subscribed to. RealmList will continually be emitted as the RealmList is updated -
     * {@code onComplete} will never be called.
     * <p>
     * If you would like the {@code asFlowable()} to stop emitting items you can instruct RxJava to
     * only emit only the first item by using the {@code first()} operator:
     * <p>
     * <pre>
     * {@code
     * list.asFlowable()
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
    public Flowable<RealmList<E>> asFlowable() {
        if (realm instanceof Realm) {
            return realm.configuration.getRxFactory().from((Realm) realm, this);
        } else if (realm instanceof DynamicRealm) {
            @SuppressWarnings("UnnecessaryLocalVariable")
            Flowable<RealmList<E>> results = realm.configuration.getRxFactory().from((DynamicRealm) realm, this);
            return results;
        } else {
            throw new UnsupportedOperationException(realm.getClass() + " does not support RxJava2.");
        }
    }

    /**
     * Returns an Rx Observable that monitors changes to this RealmList. It will emit the current RealmList when
     * subscribed. For each update to the RealmList a pair consisting of the RealmList and the
     * {@link OrderedCollectionChangeSet} will be sent. The changeset will be {@code null} the first
     * time an RealmList is emitted.
     * <p>
     * RealmList will continually be emitted as the RealmList is updated - {@code onComplete} will never be called.
     * <p>
     * * Note that when the {@link Realm} is accessed from threads other than where it was created,
     * {@link IllegalStateException} will be thrown. Care should be taken when using different schedulers
     * with {@code subscribeOn()} and {@code observeOn()}. Consider using {@code Realm.where().find*Async()}
     * instead.
     *
     * @return RxJava Observable that only calls {@code onNext}. It will never call {@code onComplete} or {@code OnError}.
     * @throws UnsupportedOperationException if the required RxJava framework is not on the classpath or the
     * corresponding Realm instance doesn't support RxJava.
     * @see <a href="https://realm.io/docs/java/latest/#rxjava">RxJava and Realm</a>
     */
    public Observable<CollectionChange<RealmList<E>>> asChangesetObservable() {
        if (realm instanceof Realm) {
            return realm.configuration.getRxFactory().changesetsFrom((Realm) realm, this);
        } else if (realm instanceof DynamicRealm) {
            DynamicRealm dynamicRealm = (DynamicRealm) realm;
            RealmList<DynamicRealmObject> dynamicResults = (RealmList<DynamicRealmObject>) this;
            return (Observable) realm.configuration.getRxFactory().changesetsFrom(dynamicRealm, dynamicResults);
        } else {
            throw new UnsupportedOperationException(realm.getClass() + " does not support RxJava2.");
        }
    }

    private void checkForAddRemoveListener(@Nullable Object listener, boolean checkListener) {
        if (checkListener && listener == null) {
            throw new IllegalArgumentException("Listener should not be null");
        }
        realm.checkIfValid();
        realm.sharedRealm.capabilities.checkCanDeliverNotification(BaseRealm.LISTENER_NOT_ALLOWED_MESSAGE);
    }

    /**
     * Adds a change listener to this {@link RealmList}.
     * <p>
     * Registering a change listener will not prevent the underlying RealmList from being garbage collected.
     * If the RealmList is garbage collected, the change listener will stop being triggered. To avoid this, keep a
     * strong reference for as long as appropriate e.g. in a class variable.
     * <p>
     * <pre>
     * {@code
     * public class MyActivity extends Activity {
     *
     *     private RealmList<Dog> dogs; // Strong reference to keep listeners alive
     *
     *     \@Override
     *     protected void onCreate(Bundle savedInstanceState) {
     *       super.onCreate(savedInstanceState);
     *       dogs = realm.where(Person.class).findFirst().getDogs();
     *       dogs.addChangeListener(new OrderedRealmCollectionChangeListener<RealmList<Dog>>() {
     *           \@Override
     *           public void onChange(RealmList<Dog> dogs, OrderedCollectionChangeSet changeSet) {
     *               // React to change
     *           }
     *       });
     *     }
     * }
     * }
     * </pre>
     *
     * @param listener the change listener to be notified.
     * @throws IllegalArgumentException if the change listener is {@code null}.
     * @throws IllegalStateException if you try to add a listener from a non-Looper or
     * {@link android.app.IntentService} thread.
     */
    public void addChangeListener(OrderedRealmCollectionChangeListener<RealmList<E>> listener) {
        checkForAddRemoveListener(listener, true);
        osListOperator.getOsList().addListener(this, listener);
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
        osListOperator.getOsList().removeListener(this, listener);
    }

    /**
     * Adds a change listener to this {@link RealmList}.
     * <p>
     * Registering a change listener will not prevent the underlying RealmList from being garbage collected.
     * If the RealmList is garbage collected, the change listener will stop being triggered. To avoid this, keep a
     * strong reference for as long as appropriate e.g. in a class variable.
     * <p>
     * <pre>
     * {@code
     * public class MyActivity extends Activity {
     *
     *     private RealmList<Dog> dogs; // Strong reference to keep listeners alive
     *
     *     \@Override
     *     protected void onCreate(Bundle savedInstanceState) {
     *       super.onCreate(savedInstanceState);
     *       dogs = realm.where(Person.class).findFirst().getDogs();
     *       dogs.addChangeListener(new RealmChangeListener<RealmList<Dog>>() {
     *           \@Override
     *           public void onChange(RealmList<Dog> dogs) {
     *               // React to change
     *           }
     *       });
     *     }
     * }
     * }
     * </pre>
     *
     * @param listener the change listener to be notified.
     * @throws IllegalArgumentException if the change listener is {@code null}.
     * @throws IllegalStateException if you try to add a listener from a non-Looper or
     * {@link android.app.IntentService} thread.
     */
    public void addChangeListener(RealmChangeListener<RealmList<E>> listener) {
        checkForAddRemoveListener(listener, true);
        osListOperator.getOsList().addListener(this, listener);
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
        osListOperator.getOsList().removeListener(this, listener);
    }

    /**
     * Removes all user-defined change listeners.
     *
     * @throws IllegalStateException if you try to remove listeners from a non-Looper Thread.
     * @see io.realm.RealmChangeListener
     */
    public void removeAllChangeListeners() {
        checkForAddRemoveListener(null, false);
        osListOperator.getOsList().removeAllListeners();
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
            checkValidRealm();
            checkConcurrentModification();
            return cursor != size();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @Nullable
        public E next() {
            checkValidRealm();
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
            checkValidRealm();
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
        @Nullable
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
        public void set(@Nullable E e) {
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
         * @see #add(Object)
         */
        @Override
        public void add(@Nullable E e) {
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

    private static boolean isClassForRealmModel(Class<?> clazz) {
        return RealmModel.class.isAssignableFrom(clazz);
    }

    private ManagedListOperator<E> getOperator(BaseRealm realm, OsList osList, @Nullable Class<E> clazz, @Nullable String className) {
        if (clazz == null || isClassForRealmModel(clazz)) {
            return new RealmModelListOperator<>(realm, osList, clazz, className);
        }
        if (clazz == String.class) {
            //noinspection unchecked
            return (ManagedListOperator<E>) new StringListOperator(realm, osList, (Class<String>) clazz);
        }
        if (clazz == Long.class || clazz == Integer.class || clazz == Short.class || clazz == Byte.class) {
            return new LongListOperator<>(realm, osList, clazz);
        }
        if (clazz == Boolean.class) {
            //noinspection unchecked
            return (ManagedListOperator<E>) new BooleanListOperator(realm, osList, (Class<Boolean>) clazz);
        }
        if (clazz == byte[].class) {
            //noinspection unchecked
            return (ManagedListOperator<E>) new BinaryListOperator(realm, osList, (Class<byte[]>) clazz);
        }
        if (clazz == Double.class) {
            //noinspection unchecked
            return (ManagedListOperator<E>) new DoubleListOperator(realm, osList, (Class<Double>) clazz);
        }
        if (clazz == Float.class) {
            //noinspection unchecked
            return (ManagedListOperator<E>) new FloatListOperator(realm, osList, (Class<Float>) clazz);
        }
        if (clazz == Date.class) {
            //noinspection unchecked
            return (ManagedListOperator<E>) new DateListOperator(realm, osList, (Class<Date>) clazz);
        }
        throw new IllegalArgumentException("Unexpected value class: " + clazz.getName());
    }
}

/**
 * This class provides facade for against {@link OsList}. {@link OsList} is used for both {@link RealmModel}s
 * and values, but there are some subtle differences in actual operation.
 * <p>
 * This class provides common interface for them.
 * <p>
 * You need to use appropriate sub-class for underlying field type.
 *
 * @param <T> class of element which is returned on read operation.
 */
abstract class ManagedListOperator<T> {
    static final String NULL_OBJECTS_NOT_ALLOWED_MESSAGE = "RealmList does not accept null values.";
    static final String INVALID_OBJECT_TYPE_MESSAGE = "Unacceptable value type. Acceptable: %1$s, actual: %2$s .";

    final BaseRealm realm;
    final OsList osList;
    @Nullable
    final Class<T> clazz;

    ManagedListOperator(BaseRealm realm, OsList osList, @Nullable Class<T> clazz) {
        this.realm = realm;
        this.clazz = clazz;
        this.osList = osList;
    }

    public abstract boolean forRealmModel();

    public final OsList getOsList() {
        return osList;
    }

    public final boolean isValid() {
        return osList.isValid();
    }

    public final int size() {
        final long actualSize = osList.size();
        return actualSize < Integer.MAX_VALUE ? (int) actualSize : Integer.MAX_VALUE;
    }

    public final boolean isEmpty() {
        return osList.isEmpty();
    }

    protected abstract void checkValidValue(@Nullable Object value);

    @Nullable
    public abstract T get(int index);

    public final void append(@Nullable Object value) {
        checkValidValue(value);

        if (value == null) {
            appendNull();
        } else {
            appendValue(value);
        }
    }

    private void appendNull() {
        osList.addNull();
    }

    abstract protected void appendValue(Object value);

    public final void insert(int index, @Nullable Object value) {
        checkValidValue(value);

        if (value == null) {
            insertNull(index);
        } else {
            insertValue(index, value);
        }

    }

    protected void insertNull(int index) {
        osList.insertNull(index);
    }

    protected abstract void insertValue(int index, Object value);

    @Nullable
    public final T set(int index, @Nullable Object value) {
        checkValidValue(value);

        //noinspection unchecked
        final T oldObject = get(index);
        if (value == null) {
            setNull(index);
        } else {
            setValue(index, value);
        }
        return oldObject;
    }

    protected void setNull(int index) {
        osList.setNull(index);
    }

    abstract protected void setValue(int index, Object value);

    final void move(int oldPos, int newPos) {
        osList.move(oldPos, newPos);
    }

    final void remove(int index) {
        osList.remove(index);
    }

    final void removeAll() {
        osList.removeAll();
    }

    final void delete(int index) {
        osList.delete(index);
    }

    final void deleteLast() {
        osList.delete(osList.size() - 1);
    }

    final void deleteAll() {
        osList.deleteAll();
    }

}

/**
 * A subclass of {@link ManagedListOperator} that deal with {@link RealmModel} list field.
 */
final class RealmModelListOperator<T> extends ManagedListOperator<T> {

    @Nullable
    private final String className;

    RealmModelListOperator(BaseRealm realm, OsList osList, @Nullable Class<T> clazz, @Nullable String className) {
        super(realm, osList, clazz);
        this.className = className;
    }

    @Override
    public boolean forRealmModel() {
        return true;
    }

    @Override
    public T get(int index) {
        //noinspection unchecked
        return (T) realm.get((Class<? extends RealmModel>) clazz, className, osList.getUncheckedRow(index));
    }

    @Override
    protected void checkValidValue(@Nullable Object value) {
        if (value == null) {
            throw new IllegalArgumentException(NULL_OBJECTS_NOT_ALLOWED_MESSAGE);
        }
        if (!(value instanceof RealmModel)) {
            throw new IllegalArgumentException(
                    String.format(Locale.ENGLISH, INVALID_OBJECT_TYPE_MESSAGE,
                            "java.lang.String",
                            value.getClass().getName()));
        }
    }

    private void checkInsertIndex(int index) {
        final int size = size();
        if (index < 0 || size < index) {
            throw new IndexOutOfBoundsException("Invalid index " + index + ", size is " + osList.size());
        }
    }

    @Override
    public void appendValue(Object value) {
        final RealmObjectProxy proxy = (RealmObjectProxy) copyToRealmIfNeeded((RealmModel) value);
        osList.addRow(proxy.realmGet$proxyState().getRow$realm().getIndex());
    }

    @Override
    protected void insertNull(int index) {
        throw new RuntimeException("Should not reach here.");
    }

    @Override
    public void insertValue(int index, Object value) {
        // need to check in advance to avoid unnecessary copy of unmanaged object into Realm.
        checkInsertIndex(index);

        RealmObjectProxy proxy = (RealmObjectProxy) copyToRealmIfNeeded((RealmModel) value);
        osList.insertRow(index, proxy.realmGet$proxyState().getRow$realm().getIndex());
    }

    @Override
    protected void setNull(int index) {
        throw new RuntimeException("Should not reach here.");
    }

    @Override
    protected void setValue(int index, Object value) {
        RealmObjectProxy proxy = (RealmObjectProxy) copyToRealmIfNeeded((RealmModel) value);
        osList.setRow(index, proxy.realmGet$proxyState().getRow$realm().getIndex());
    }

    // Transparently copies an unmanaged object or managed object from another Realm to the Realm backing this RealmList.
    private <E extends RealmModel> E copyToRealmIfNeeded(E object) {
        if (object instanceof RealmObjectProxy) {
            RealmObjectProxy proxy = (RealmObjectProxy) object;

            if (proxy instanceof DynamicRealmObject) {
                //noinspection ConstantConditions
                @Nonnull
                String listClassName = className;
                if (proxy.realmGet$proxyState().getRealm$realm() == realm) {
                    String objectClassName = ((DynamicRealmObject) object).getType();
                    if (listClassName.equals(objectClassName)) {
                        // Same Realm instance and same target table
                        return object;
                    } else {
                        // Different target table
                        throw new IllegalArgumentException(String.format(Locale.US,
                                "The object has a different type from list's." +
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
        if (OsObjectStore.getPrimaryKeyForObject(realm.getSharedRealm(),
                realm.getConfiguration().getSchemaMediator().getSimpleClassName(object.getClass())) != null) {
            return realm.copyToRealmOrUpdate(object);
        } else {
            return realm.copyToRealm(object);
        }
    }
}

/**
 * A subclass of {@link ManagedListOperator} that deal with {@link String} list field.
 */
final class StringListOperator extends ManagedListOperator<String> {

    StringListOperator(BaseRealm realm, OsList osList, Class<String> clazz) {
        super(realm, osList, clazz);
    }

    @Override
    public boolean forRealmModel() {
        return false;
    }

    @Nullable
    @Override
    public String get(int index) {
        return (String) osList.getValue(index);
    }

    @Override
    protected void checkValidValue(@Nullable Object value) {
        if (value == null) {
            // null is always valid (but schema may reject null on insertion).
            return;
        }
        if (!(value instanceof String)) {
            throw new IllegalArgumentException(
                    String.format(Locale.ENGLISH, INVALID_OBJECT_TYPE_MESSAGE,
                            "java.lang.String",
                            value.getClass().getName()));
        }
    }

    @Override
    public void appendValue(Object value) {
        osList.addString((String) value);
    }

    @Override
    public void insertValue(int index, Object value) {
        osList.insertString(index, (String) value);
    }

    @Override
    protected void setValue(int index, Object value) {
        osList.setString(index, (String) value);
    }
}

/**
 * A subclass of {@link ManagedListOperator} that deal with {@code long} list field.
 */
final class LongListOperator<T> extends ManagedListOperator<T> {

    LongListOperator(BaseRealm realm, OsList osList, Class<T> clazz) {
        super(realm, osList, clazz);
    }

    @Override
    public boolean forRealmModel() {
        return false;
    }

    @Nullable
    @Override
    public T get(int index) {
        final Long value = (Long) osList.getValue(index);
        if (value == null) {
            return null;
        }
        if (clazz == Long.class) {
            //noinspection unchecked
            return (T) value;
        }
        if (clazz == Integer.class) {
            //noinspection unchecked,UnnecessaryBoxing,ConstantConditions
            return clazz.cast(Integer.valueOf(value.intValue()));
        }
        if (clazz == Short.class) {
            //noinspection unchecked,UnnecessaryBoxing,ConstantConditions
            return clazz.cast(Short.valueOf(value.shortValue()));
        }
        if (clazz == Byte.class) {
            //noinspection unchecked,UnnecessaryBoxing,ConstantConditions
            return clazz.cast(Byte.valueOf(value.byteValue()));
        }
        //noinspection ConstantConditions
        throw new IllegalStateException("Unexpected element type: " + clazz.getName());
    }

    @Override
    protected void checkValidValue(@Nullable Object value) {
        if (value == null) {
            // null is always valid (but schema may reject null on insertion).
            return;
        }
        if (!(value instanceof Number)) {
            throw new IllegalArgumentException(
                    String.format(Locale.ENGLISH, INVALID_OBJECT_TYPE_MESSAGE,
                            "java.lang.Long, java.lang.Integer, java.lang.Short, java.lang.Byte",
                            value.getClass().getName()));
        }
    }

    @Override
    public void appendValue(Object value) {
        osList.addLong(((Number) value).longValue());
    }

    @Override
    public void insertValue(int index, Object value) {
        osList.insertLong(index, ((Number) value).longValue());
    }

    @Override
    protected void setValue(int index, Object value) {
        osList.setLong(index, ((Number) value).longValue());
    }
}

/**
 * A subclass of {@link ManagedListOperator} that deal with {@code boolean} list field.
 */
final class BooleanListOperator extends ManagedListOperator<Boolean> {

    BooleanListOperator(BaseRealm realm, OsList osList, Class<Boolean> clazz) {
        super(realm, osList, clazz);
    }

    @Override
    public boolean forRealmModel() {
        return false;
    }

    @Nullable
    @Override
    public Boolean get(int index) {
        return (Boolean) osList.getValue(index);
    }

    @Override
    protected void checkValidValue(@Nullable Object value) {
        if (value == null) {
            // null is always valid (but schema may reject null on insertion).
            return;
        }
        if (!(value instanceof Boolean)) {
            throw new IllegalArgumentException(
                    String.format(Locale.ENGLISH, INVALID_OBJECT_TYPE_MESSAGE,
                            "java.lang.Boolean",
                            value.getClass().getName()));
        }
    }

    @Override
    public void appendValue(Object value) {
        osList.addBoolean((Boolean) value);
    }

    @Override
    public void insertValue(int index, Object value) {
        osList.insertBoolean(index, (Boolean) value);
    }

    @Override
    protected void setValue(int index, Object value) {
        osList.setBoolean(index, (Boolean) value);
    }
}

/**
 * A subclass of {@link ManagedListOperator} that deal with {@code byte[]} list field.
 */
final class BinaryListOperator extends ManagedListOperator<byte[]> {

    BinaryListOperator(BaseRealm realm, OsList osList, Class<byte[]> clazz) {
        super(realm, osList, clazz);
    }

    @Override
    public boolean forRealmModel() {
        return false;
    }

    @Nullable
    @Override
    public byte[] get(int index) {
        return (byte[]) osList.getValue(index);
    }

    @Override
    protected void checkValidValue(@Nullable Object value) {
        if (value == null) {
            // null is always valid (but schema may reject null on insertion).
            return;
        }
        if (!(value instanceof byte[])) {
            throw new IllegalArgumentException(
                    String.format(Locale.ENGLISH, INVALID_OBJECT_TYPE_MESSAGE,
                            "byte[]",
                            value.getClass().getName()));
        }
    }

    @Override
    public void appendValue(Object value) {
        osList.addBinary((byte[]) value);
    }

    @Override
    public void insertValue(int index, Object value) {
        osList.insertBinary(index, (byte[]) value);
    }

    @Override
    protected void setValue(int index, Object value) {
        osList.setBinary(index, (byte[]) value);
    }
}

/**
 * A subclass of {@link ManagedListOperator} that deal with {@code double} list field.
 */
final class DoubleListOperator extends ManagedListOperator<Double> {

    DoubleListOperator(BaseRealm realm, OsList osList, Class<Double> clazz) {
        super(realm, osList, clazz);
    }

    @Override
    public boolean forRealmModel() {
        return false;
    }

    @Nullable
    @Override
    public Double get(int index) {
        return (Double) osList.getValue(index);
    }

    @Override
    protected void checkValidValue(@Nullable Object value) {
        if (value == null) {
            // null is always valid (but schema may reject null on insertion).
            return;
        }
        if (!(value instanceof Number)) {
            throw new IllegalArgumentException(
                    String.format(Locale.ENGLISH, INVALID_OBJECT_TYPE_MESSAGE,
                            "java.lang.Number",
                            value.getClass().getName()));
        }
    }

    @Override
    public void appendValue(Object value) {
        osList.addDouble(((Number) value).doubleValue());
    }

    @Override
    public void insertValue(int index, Object value) {
        osList.insertDouble(index, ((Number) value).doubleValue());
    }

    @Override
    protected void setValue(int index, Object value) {
        osList.setDouble(index, ((Number) value).doubleValue());
    }
}

/**
 * A subclass of {@link ManagedListOperator} that deal with {@code float} list field.
 */
final class FloatListOperator extends ManagedListOperator<Float> {

    FloatListOperator(BaseRealm realm, OsList osList, Class<Float> clazz) {
        super(realm, osList, clazz);
    }

    @Override
    public boolean forRealmModel() {
        return false;
    }

    @Nullable
    @Override
    public Float get(int index) {
        return (Float) osList.getValue(index);
    }

    @Override
    protected void checkValidValue(@Nullable Object value) {
        if (value == null) {
            // null is always valid (but schema may reject null on insertion).
            return;
        }
        if (!(value instanceof Number)) {
            throw new IllegalArgumentException(
                    String.format(Locale.ENGLISH, INVALID_OBJECT_TYPE_MESSAGE,
                            "java.lang.Number",
                            value.getClass().getName()));
        }
    }

    @Override
    public void appendValue(Object value) {
        osList.addFloat(((Number) value).floatValue());
    }

    @Override
    public void insertValue(int index, Object value) {
        osList.insertFloat(index, ((Number) value).floatValue());
    }

    @Override
    protected void setValue(int index, Object value) {
        osList.setFloat(index, ((Number) value).floatValue());
    }
}

/**
 * A subclass of {@link ManagedListOperator} that deal with {@link Date} list field.
 */
final class DateListOperator extends ManagedListOperator<Date> {

    DateListOperator(BaseRealm realm, OsList osList, Class<Date> clazz) {
        super(realm, osList, clazz);
    }

    @Override
    public boolean forRealmModel() {
        return false;
    }

    @Nullable
    @Override
    public Date get(int index) {
        return (Date) osList.getValue(index);
    }

    @Override
    protected void checkValidValue(@Nullable Object value) {
        if (value == null) {
            // null is always valid (but schema may reject null on insertion).
            return;
        }
        if (!(value instanceof Date)) {
            throw new IllegalArgumentException(
                    String.format(Locale.ENGLISH, INVALID_OBJECT_TYPE_MESSAGE,
                            "java.util.Date",
                            value.getClass().getName()));
        }
    }

    @Override
    public void appendValue(Object value) {
        osList.addDate((Date) value);
    }

    @Override
    public void insertValue(int index, Object value) {
        osList.insertDate(index, (Date) value);
    }

    @Override
    protected void setValue(int index, Object value) {
        osList.setDate(index, (Date) value);
    }
}
