/*
 * Copyright 2016 Realm Inc.
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

import io.realm.internal.InvalidRow;
import io.realm.internal.Row;
import io.realm.internal.Table;
import io.realm.internal.TableQuery;
import io.realm.internal.log.RealmLog;

/**
 * This implements {@code RealmObjectProxy} interface, to eliminate copying logic between
 * {@link RealmObject} and {@link DynamicRealmObject}.
 */
public final class ProxyState<E extends RealmModel> {
    private E model;
    private String className;
    private Class<? extends RealmModel> clazzName;

    private Row row;
    private BaseRealm realm;

    private final List<RealmChangeListener<E>> listeners = new CopyOnWriteArrayList<RealmChangeListener<E>>();
    private Future<Long> pendingQuery;
    private boolean isCompleted = false;
    protected long currentTableVersion = -1;

    public ProxyState() {}

    public ProxyState(E model) {
        this.model = model;
    }

    public ProxyState(Class<? extends RealmModel> clazzName, E model) {
        this.clazzName = clazzName;
        this.model = model;
    }

    /**
     * Sets the Future instance returned by the worker thread, we need this instance to force {@link RealmObject#load()} an async
     * query, we use it to determine if the current RealmResults is a sync or async one.
     *
     * @param pendingQuery pending query.
     */
    public void setPendingQuery$realm(Future<Long> pendingQuery) {
        this.pendingQuery = pendingQuery;
        if (isLoaded()) {
            // the query completed before RealmQuery
            // had a chance to call setPendingQuery to register the pendingQuery (used btw
            // to determine isLoaded behaviour)
            onCompleted$realm();

        } // else, it will be handled by the Realm#handler
    }

    public BaseRealm getRealm$realm() {
        return realm;
    }

    public void setRealm$realm(BaseRealm realm) {
        this.realm = realm;
    }

    public Row getRow$realm() {
        return row;
    }

    public void setRow$realm(Row row) {
        this.row = row;
    }

    public Object getPendingQuery$realm() {
        return pendingQuery;
    }

    public boolean isCompleted$realm() {
        return isCompleted;
    }

    /**
     * Called to import the handover row pointer and notify listeners.
     *
     * @return {@code true} if it successfully completed the query, {@code false} otherwise.
     */
    public boolean onCompleted$realm() {
        try {
            Long handoverResult = pendingQuery.get();// make the query blocking
            if (handoverResult != 0) {
                // this may fail with BadVersionException if the caller and/or the worker thread
                // are not in sync (same shared_group version).
                // COMPLETED_ASYNC_REALM_OBJECT will be fired by the worker thread
                // this should handle more complex use cases like retry, ignore etc
                onCompleted$realm(handoverResult);
                notifyChangeListeners$realm();
            } else {
                isCompleted = true;
            }
        } catch (Exception e) {
            RealmLog.d(e.getMessage());
            return false;
        }
        return true;
    }

    public List<RealmChangeListener<E>> getListeners$realm() {
        return listeners;
    }

    public void onCompleted$realm(long handoverRowPointer) {
        if (handoverRowPointer == 0) {
            // we'll retry later to update the row pointer, but we consider
            // the query done
            isCompleted = true;

        } else if (!isCompleted || row == Row.EMPTY_ROW) {
            isCompleted = true;
            long nativeRowPointer = TableQuery.importHandoverRow(handoverRowPointer, realm.sharedRealm);
            Table table = getTable();
            this.row = table.getUncheckedRowByPointer(nativeRowPointer);
        }// else: already loaded query no need to import again the pointer
    }

    /**
     * Notifies all registered listeners.
     */
    void notifyChangeListeners$realm() {
        if (!listeners.isEmpty()) {
            boolean notify = false;

            Table table = row.getTable();
            if (table == null) {
                // Completed async queries might result in `table == null`, `isCompleted == true` and `row == Row.EMPTY_ROW`
                // We still want to trigger change notifications for these cases.
                // isLoaded / isValid should be considered properties on RealmObjects as well so any change to these
                // should trigger a RealmChangeListener.
                notify = true;
            } else {
                long version = table.getVersion();
                if (currentTableVersion != version) {
                    currentTableVersion = version;
                    notify = true;
                }
            }

            if (notify) {
                for (RealmChangeListener listener : listeners) {
                    listener.onChange(model);
                }
            }
        }
    }

    public void setTableVersion$realm() {
        if (row.getTable() != null) {
            currentTableVersion = row.getTable().getVersion();
        }
    }

    public void setClassName(String className) {
        this.className = className;
    }

    private Table getTable () {
        if (className != null) {
            return getRealm$realm().schema.getTable(className);
        }
        return getRealm$realm().schema.getTable(clazzName);
    }

    private boolean isLoaded() {
        realm.checkIfValid();
        return getPendingQuery$realm() == null || isCompleted$realm();
    }
}
