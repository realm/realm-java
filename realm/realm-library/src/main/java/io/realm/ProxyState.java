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

import io.realm.internal.InvalidRow;
import io.realm.internal.PendingRow;
import io.realm.internal.Row;
import io.realm.internal.RowObject;
import io.realm.internal.UncheckedRow;

/**
 * This implements {@code RealmObjectProxy} interface, to eliminate copying logic between
 * {@link RealmObject} and {@link DynamicRealmObject}.
 */
public final class ProxyState<E extends RealmModel> implements PendingRow.FrontEnd {

    static class RealmChangeListenerWrapper<T> implements RealmObjectChangeListener<T> {
        private final RealmChangeListener<T> listener;

        RealmChangeListenerWrapper(RealmChangeListener<T> listener) {
            this.listener = listener;
        }

        @Override
        public void onChange(T object, ObjectChangeSet changes) {
            listener.onChange(object);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof RealmChangeListenerWrapper &&
                    listener == ((RealmChangeListenerWrapper) obj).listener;
        }

        @Override
        public int hashCode() {
            return listener.hashCode();
        }
    }

    private E model;

    // true only while executing the constructor of the enclosing proxy object
    private boolean underConstruction = true;

    private Row row;
    private RowObject rowObject;
    private BaseRealm realm;
    private boolean acceptDefaultValue;
    private List<String> excludeFields;

    private final List<RealmObjectChangeListener<E>> listeners =
            new CopyOnWriteArrayList<RealmObjectChangeListener<E>>();
    protected long currentTableVersion = -1;

    public ProxyState() {}

    public ProxyState(E model) {
        this.model = model;
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

    public boolean getAcceptDefaultValue$realm() {
        return acceptDefaultValue;
    }

    public void setAcceptDefaultValue$realm(boolean acceptDefaultValue) {
        this.acceptDefaultValue = acceptDefaultValue;
    }

    public List<String> getExcludeFields$realm() {
        return excludeFields;
    }

    public void setExcludeFields$realm(List<String> excludeFields) {
        this.excludeFields = excludeFields;
    }

    /**
     * Notifies all registered listeners.
     */
    private void notifyChangeListeners(ObjectChangeSet changeSet) {
        if (!listeners.isEmpty()) {
            for (RealmObjectChangeListener<E> listener : listeners) {
                if (realm.sharedRealm == null || realm.sharedRealm.isClosed()) {
                    return;
                }
                listener.onChange(model, changeSet);
            }
        }
    }

    public void addChangeListener(RealmObjectChangeListener<E> listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
        // this might be called after query returns. So it is still necessary to register.
        if (row instanceof UncheckedRow) {
            registerToRealmNotifier();
        }
    }

    public void removeChangeListener(RealmObjectChangeListener<E> listener) {
        listeners.remove(listener);
        if (listeners.isEmpty() && rowObject != null) {
            rowObject.removeListener(this);
            rowObject = null;
        }
    }

    public void removeAllChangeListeners() {
        listeners.clear();
        if (rowObject != null) {
            rowObject.removeListener(this);
            rowObject = null;
        }
    }

    public void setTableVersion$realm() {
        if (row.getTable() != null) {
            currentTableVersion = row.getTable().getVersion();
        }
    }

    public boolean isUnderConstruction() {
        return underConstruction;
    }

    public void setConstructionFinished() {
        underConstruction = false;
        // Only used while construction.
        excludeFields = null;
    }

    private void registerToRealmNotifier() {
        if (realm.sharedRealm == null || realm.sharedRealm.isClosed()) {
            return;
        }

        if (rowObject == null) {
            rowObject = new RowObject(realm.sharedRealm, (UncheckedRow) row);
            rowObject.addListener(this, new RealmObjectChangeListener<ProxyState<E>>() {
                @Override
                public void onChange(ProxyState<E> object, ObjectChangeSet changeSet) {
                    notifyChangeListeners(changeSet);
                }
            });
        }
    }

    public boolean isLoaded() {
        return !(row instanceof PendingRow);
    }

    public void load() {
        if (row instanceof PendingRow) {
            row = ((PendingRow) row).executeQuery();
            if (!(row instanceof InvalidRow)) {
                registerToRealmNotifier();
            }
            notifyChangeListeners(null);
        }
    }

    @Override
    public void onQueryFinished(Row row) {
        this.row = row;
        // getTable should return a non-null table since the row should always be valid here.
        currentTableVersion = row.getTable().getVersion();
        notifyChangeListeners(null);
        registerToRealmNotifier();
    }
}
