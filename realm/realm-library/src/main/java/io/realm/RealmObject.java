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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;

import io.realm.annotations.RealmClass;
import io.realm.internal.InvalidRow;
import io.realm.internal.Row;
import io.realm.internal.Table;
import io.realm.internal.TableQuery;
import io.realm.internal.log.RealmLog;
import rx.Observable;

/**
 * In Realm you define your RealmObject classes by sub-classing RealmObject and adding fields to be persisted. You then 
 * create your objects within a Realm, and use your custom subclasses instead of using the RealmObject class directly.
 * <p>
 * An annotation processor will create a proxy class for your RealmObject subclass. The getters and setters should not
 * contain any custom code of logic as they are overridden as part of the annotation process.
 * <p>
 * A RealmObject is currently limited to the following:
 *
 * <ul>
 *   <li>Private fields.</li>
 *   <li>Getter and setters for these fields.</li>
 *   <li>Static methods.</li>
 * </ul>
 * <p>
 * The following field data types are supported:
 * <ul>
 *   <li>boolean/Boolean</li>
 *   <li>short/Short</li>
 *   <li>int/Integer</li>
 *   <li>long/Long</li>
 *   <li>float/Float</li>
 *   <li>double/Double</li>
 *   <li>byte[]</li>
 *   <li>String</li>
 *   <li>Date</li>
 *   <li>Any RealmObject subclass</li>
 *   <li>RealmList</li>
 * </ul>
 * <p>
 * The types <code>short</code>, <code>int</code>, and <code>long</code> are mapped to <code>long</code> when storing
 * within a Realm.
 * <p>
 * Getter and setter names must have the name {@code getXXX} or {@code setXXX} if the field name is {@code XXX}. Getters
 * for fields of type boolean can be called {@code isXXX} as well. Fields with a m-prefix must have getters and setters
 * named setmXXX and getmXXX which is the default behavior when Android Studio automatically generates the getters and
 * setters.
 * <p>
 * Fields annotated with {@link io.realm.annotations.Ignore} don't have these restrictions and don't require either a
 * getter or setter.
 * <p>
 * Realm will create indexes for fields annotated with {@link io.realm.annotations.Index}. This will speedup queries but
 * will have a negative impact on inserts and updates.
 * <p>
 * A RealmObject cannot be passed between different threads.
 *
 * @see Realm#createObject(Class)
 * @see Realm#copyToRealm(RealmObject)
 */

@RealmClass
public abstract class RealmObject {

    protected Row row;
    protected BaseRealm realm;

    private final List<RealmChangeListener> listeners = new CopyOnWriteArrayList<RealmChangeListener>();
    private Future<Long> pendingQuery;
    private boolean isCompleted = false;
    protected long currentTableVersion = -1;

    /**
     * Removes the object from the Realm it is currently associated to.
     * <p>
     * After this method is called the object will be invalid and any operation (read or write) performed on it will
     * fail with an IllegalStateException
     *
     * @throws IllegalStateException if the corresponding Realm is closed or in an incorrect thread.
     */
    public void removeFromRealm() {
        if (row == null) {
            throw new IllegalStateException("Object malformed: missing object in Realm. Make sure to instantiate RealmObjects with Realm.createObject()");
        }
        if (realm == null) {
            throw new IllegalStateException("Object malformed: missing Realm. Make sure to instantiate RealmObjects with Realm.createObject()");
        }
        realm.checkIfValid();

        row.getTable().moveLastOver(row.getIndex());
        row = InvalidRow.INSTANCE;
    }

    /**
     * Checks if the RealmObject is still valid to use i.e. the RealmObject hasn't been deleted nor has the
     * {@link io.realm.Realm} been closed. It will always return false for stand alone objects.
     *
     * @return {@code true} if the object is still accessible, {@code false} otherwise or if it is a standalone object.
     */
    public final boolean isValid() {
        return row != null && row.isAttached();
    }

    protected Table getTable () {
        return realm.schema.getTable(getClass());
    }

    /**
     * Sets the Future instance returned by the worker thread, we need this instance to force {@link #load()} an async
     * query, we use it to determine if the current RealmResults is a sync or async one.
     *
     * @param pendingQuery pending query.
     */
    void setPendingQuery(Future<Long> pendingQuery) {
        this.pendingQuery = pendingQuery;
        if (isLoaded()) {
            // the query completed before RealmQuery
            // had a chance to call setPendingQuery to register the pendingQuery (used btw
            // to determine isLoaded behaviour)
            onCompleted();

        } // else, it will be handled by the Realm#handler
    }

    /**
     * Determines if the current RealmObject is obtained synchronously or asynchronously (from a worker thread).
     * Synchronous RealmObjects are by definition blocking hence this method will always return {@code true} for them.
     * This will return {@code true} if called for a standalone object (created outside of Realm).
     *
     * @return {@code true} if the query has completed and the data is available {@code false} if the query is in
     * progress.
     */
    public final boolean isLoaded() {
        if (realm == null) {
            return true;
        }
        realm.checkIfValid();
        return pendingQuery == null || isCompleted;
    }

    /**
     * Makes an asynchronous query blocking. This will also trigger any registered listeners.
     * Note: This will return {@code true} if called for a standalone object (created outside of Realm).
     *
     * @return {@code true} if it successfully completed the query, {@code false} otherwise.
     */
    public final boolean load() {
        if (isLoaded()) {
            return true;
        } else {
            // doesn't guarantee to import correctly the result (because the user may have advanced)
            // in this case the Realm#handler will be responsible of retrying
            return onCompleted();
        }
    }

    /**
     * Called to import the handover row pointer & notify listeners.
     *
     * @return {@code true} if it successfully completed the query, {@code false} otherwise.
     */
    boolean onCompleted() {
        try {
            Long handoverResult = pendingQuery.get();// make the query blocking
            if (handoverResult != 0) {
                // this may fail with BadVersionException if the caller and/or the worker thread
                // are not in sync (same shared_group version).
                // COMPLETED_ASYNC_REALM_OBJECT will be fired by the worker thread
                // this should handle more complex use cases like retry, ignore etc
                onCompleted(handoverResult);
                notifyChangeListeners();
            } else {
                isCompleted = true;
            }
        } catch (Exception e) {
            RealmLog.d(e.getMessage());
            return false;
        }
        return true;
    }

    void onCompleted(Long handoverRowPointer) {
        if (handoverRowPointer == 0) {
            // we'll retry later to update the row pointer, but we consider
            // the query done
            isCompleted = true;

        } else if (!isCompleted || row == Row.EMPTY_ROW) {
            isCompleted = true;
            long nativeRowPointer = TableQuery.nativeImportHandoverRowIntoSharedGroup(handoverRowPointer, realm.sharedGroupManager.getNativePointer());
            Table table = getTable();
            this.row = table.getUncheckedRowByPointer(nativeRowPointer);
        }// else: already loaded query no need to import again the pointer
    }

    /**
     * Adds a change listener to this RealmObject.
     *
     * @param listener the change listener to be notified.
     */
    public final void addChangeListener(RealmChangeListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener should not be null");
        }
        if (realm != null) {
            realm.checkIfValid();
        } else {
            throw new IllegalArgumentException("Cannot add listener from this unmanaged RealmObject (created outside of Realm)");
        }
        if (realm.handler == null) {
            throw new IllegalStateException("You can't register a listener from a non-Looper thread ");
        }
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Removes a previously registered listener.
     *
     * @param listener the instance to be removed.
     */
    public final void removeChangeListener(RealmChangeListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener should not be null");
        }
        if (realm != null) {
            realm.checkIfValid();
        } else {
            throw new IllegalArgumentException("Cannot remove listener from this unmanaged RealmObject (created outside of Realm)");
        }
        listeners.remove(listener);
    }

    /**
     * Removes all registered listeners.
     */
    public final void removeChangeListeners() {
        if (realm != null) {
            realm.checkIfValid();
        } else {
            throw new IllegalArgumentException("Cannot remove listeners from this unmanaged RealmObject (created outside of Realm)");
        }
        listeners.clear();
    }

    /**
     * Returns an Rx Observable that monitors changes to this RealmObject. It will emit the current object when
     * subscribed to.
     *
     * If chaining a RealmObject observable use {@code obj.<MyRealmObjectClass>asObservable()} to pass on
     * type information, otherwise the type of the following observables will be {@code RealmObject}.
     *
     * @param <E> RealmObject class that is being observed. Must be this class or its super types.
     * @return RxJava Observable.
     * @throws UnsupportedOperationException if the required RxJava framework is not on the classpath.
     * @see <a href="https://realm.io/docs/java/latest/#rxjava">RxJava and Realm</a>
     */
    public <E extends RealmObject> Observable<E> asObservable() {
        if (realm instanceof Realm) {
            @SuppressWarnings("unchecked")
            E obj = (E) this;
            return realm.configuration.getRxFactory().from((Realm) realm, obj);
        } else if (realm instanceof DynamicRealm) {
            DynamicRealm dynamicRealm = (DynamicRealm) realm;
            DynamicRealmObject dynamicObject = (DynamicRealmObject) this;
            @SuppressWarnings("unchecked")
            Observable<E> observable = (Observable<E>) realm.configuration.getRxFactory().from(dynamicRealm, dynamicObject);
            return observable;
        } else {
            throw new UnsupportedOperationException(realm.getClass() + " not supported");
        }
    }

    /**
     * Notifies all registered listeners.
     */
    void notifyChangeListeners() {
        if (listeners != null && !listeners.isEmpty()) {
            if (row.getTable() == null) return;

            long version = row.getTable().version();
            if (currentTableVersion != version) {
                currentTableVersion = version;
                for (RealmChangeListener listener : listeners) {
                    listener.onChange();
                }
            }
        }
    }

    void setTableVersion() {
        if (row.getTable() != null) {
            currentTableVersion = row.getTable().version();
        }
    }
}
