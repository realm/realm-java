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

/**
 * In Realm you define your model classes by sub-classing RealmObject and adding fields to be
 * persisted. You then create your objects within a Realm, and use your custom subclasses instead
 * of using the RealmObject class directly.
 * <p/>
 * An annotation processor will create a proxy class for your RealmObject subclass. The getters and
 * setters should not contain any custom code of logic as they are overridden as part of the annotation
 * process.
 * <p/>
 * A RealmObject is currently limited to the following:
 * <p/>
 * <ul>
 * <li>Private fields.</li>
 * <li>Getter and setters for these fields.</li>
 * <li>Static methods.</li>
 * </ul>
 * <p/>
 * The following field data types are supported (no boxed types):
 * <ul>
 * <li>boolean</li>
 * <li>short</li>
 * <li>int</li>
 * <li>long</li>
 * <li>float</li>
 * <li>double</li>
 * <li>byte[]</li>
 * <li>String</li>
 * <li>Date</li>
 * <li>Any RealmObject subclass</li>
 * <li>RealmList</li>
 * </ul>
 * <p/>
 * The types <code>short</code>, <code>int</code>, and <code>long</code> are mapped to <code>long</code>
 * when storing within a Realm.
 * <p/>
 * Getter and setter names must have the name {@code getXXX} or {@code setXXX} if
 * the field name is {@code XXX}. Getters for fields of type boolean can be called {@code isXXX} as
 * well. Fields with a m-prefix must have getters and setters named setmXXX and getmXXX which is
 * the default behavior when Android Studio automatically generates the getters and setters.
 * <p/>
 * Fields annotated with {@link io.realm.annotations.Ignore} don't have these restrictions and
 * don't require either a getter or setter.
 * <p/>
 * Realm will create indexes for fields annotated with {@link io.realm.annotations.Index}. This
 * will speedup queries but will have a negative impact on inserts and updates.
 * * <p>
 * A RealmObject cannot be passed between different threads.
 *
 * @see Realm#createObject(Class)
 * @see Realm#copyToRealm(RealmObject)
 */

@RealmClass
public abstract class RealmObject {

    protected Row row;
    protected Realm realm;

    private final List<RealmChangeListener> listeners = new CopyOnWriteArrayList<RealmChangeListener>();
    private Future<Long> pendingQuery;
    private Class<? extends RealmObject> clazz;
    private boolean isCompleted = false;

    /**
     * Removes the object from the Realm it is currently associated to.
     * <p/>
     * After this method is called the object will be invalid and any operation (read or write)
     * performed on it will fail with an IllegalStateException
     */
    public void removeFromRealm() {
        if (row == null) {
            throw new IllegalStateException("Object malformed: missing object in Realm. Make sure to instantiate RealmObjects with Realm.createObject()");
        }
        if (realm == null) {
            throw new IllegalStateException("Object malformed: missing Realm. Make sure to instantiate RealmObjects with Realm.createObject()");
        }
        row.getTable().moveLastOver(row.getIndex());
        row = InvalidRow.INSTANCE;
    }

    /**
     * Check if the RealmObject is still valid to use ie. the RealmObject hasn't been deleted nor
     * has the {@link io.realm.Realm} been closed. It will always return false for stand alone
     * objects.
     *
     * @return {@code true} if the object is still accessible, {@code false} otherwise or if it is a
     * standalone object.
     */
    public boolean isValid() {
        return row != null && row.isAttached();
    }

    /**
     * Returns the Realm instance this object belongs to. Internal use only.
     *
     * @return The Realm this object belongs to or {@code null} if it is a standalone object.
     */
    protected static Realm getRealm(RealmObject obj) {
        return obj.realm;
    }

    /**
     * Returns the {@link Row} representing this object. Internal use only.
     *
     * @return The {@link Row} this object belongs to or {@code null} if it is a standalone object.
     */
    protected static Row getRow(RealmObject obj) {
        return obj.row;
    }

    /**
     * Set the Future instance returned by the worker thread, we need this instance
     * to force {@link #load()} an async query, we use it to determine if the current
     * RealmResults is a sync or async one
     *
     * @param pendingQuery pending query
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
     * Set the class type of this RealmObject, we need it to figure out the {@link Table}
     *
     * @param clazz type/table
     */
    void setType(Class<? extends RealmObject> clazz) {
        this.clazz = clazz;
    }

    /**
     * Determine if the current RealmObject is obtained synchronously or asynchronously (from
     * a worker thread). Synchronously RealmObjects are by definition blocking hence this method
     * will always return {@code true} for them.
     *
     * @return {@code true} if the query has completed & the data is available {@code false} if the
     * query is in progress.
     */
    public boolean isLoaded() {
        realm.checkIfValid();
        return pendingQuery == null || isCompleted;
    }

    /**
     * make an asynchronous query blocking.
     *
     * @return {@code true} if it successfully completed the query, {@code false} otherwise.
     */
    public boolean load() {
        // doesn't guarantee to import correctly the result (because the user may have advanced)
        // in this case the Realm#handler will be responsible of retrying
        realm.checkIfValid();
        if (pendingQuery != null && !pendingQuery.isDone() && !isCompleted) {
            return onCompleted();
        }
        return false;
    }

    /**
     * called to import the handover row pointer & notify listeners.
     *
     * @return {@code true} if it successfully completed the query, {@code false} otherwise.
     */
    boolean onCompleted() {
        realm.checkIfValid();
        try {
            Long handoverResult = pendingQuery.get();// make the query blocking
            // this may fail with BadVersionException if the caller and/or the worker thread
            // are not in sync. REALM_COMPLETED_ASYNC_FIND_FIRST will be fired by the worker thread
            // this is should handle more complex use cases like retry, ignore etc
            onCompleted(handoverResult);
        } catch (Exception e) {
            RealmLog.d(e.getMessage());
            return false;
        }
        return true;
    }

    void onCompleted(Long handoverRowPointer) {
        realm.checkIfValid();
        if (!isCompleted) {
            isCompleted = true;
            long nativeRowPointer = TableQuery.nativeImportHandoverRowIntoSharedGroup(handoverRowPointer, realm.sharedGroupManager.getNativePointer());
            Table table = realm.getTable(clazz);
            this.row = table.getUncheckedRowByPointer(nativeRowPointer);
            notifyChangeListeners();
        }// else: already loaded query no need to import again the pointer
    }

    /**
     * add a change listener to this RealmResults
     *
     * @param listener the change listener to be notified
     */
    public void addChangeListener(RealmChangeListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener should not be null");
        }
        realm.checkIfValid();

        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * remove a previously registered listener
     * @param listener the instance to be removed
     */
    public void removeChangeListener(RealmChangeListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener should not be null");
        }
        realm.checkIfValid();

        listeners.remove(listener);
    }

    /**
     * remove all registered listeners
     */
    public void deleteChangeListeners() {
        realm.checkIfValid();
        listeners.clear();
    }

    /**
     * notify all registered listeners
     */
    public void notifyChangeListeners() {
        realm.checkIfValid();
        for (RealmChangeListener listener : listeners) {
            listener.onChange();
        }
    }
}
