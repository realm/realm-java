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
import io.realm.internal.UncheckedRow;


/**
 * This implements {@code RealmObjectProxy} interface, to eliminate copying logic between
 * {@link RealmObject} and {@link DynamicRealmObject}.
 */
public final class ProxyState<E extends RealmModel> implements PendingRow.FrontEnd {
    private E model;

    // true only while executing the constructor of the enclosing proxy object
    private boolean underConstruction = true;

    private Row row;
    private BaseRealm realm;
    private boolean acceptDefaultValue;
    private List<String> excludeFields;

    private final List<RealmChangeListener<E>> listeners = new CopyOnWriteArrayList<RealmChangeListener<E>>();
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
    private void notifyChangeListeners() {
        if (!listeners.isEmpty()) {
            for (RealmChangeListener<E> listener : listeners) {
                if (realm.sharedRealm == null || realm.sharedRealm.isClosed()) {
                    return;
                }
                listener.onChange(model);
            }
        }
    }

    public void addChangeListener(RealmChangeListener<E> listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
        // this might be called after query returns. So it is still necessary to register.
        if (row instanceof UncheckedRow) {
            registerToRealmNotifier();
        }
    }

    public void removeChangeListener(RealmChangeListener<E> listener) {
        listeners.remove(listener);
        if (listeners.isEmpty() && row instanceof UncheckedRow) {
            realm.sharedRealm.realmNotifier.removeChangeListeners(this);
        }
    }

    public void removeAllChangeListeners() {
        listeners.clear();
        if (row instanceof UncheckedRow) {
            realm.sharedRealm.realmNotifier.removeChangeListeners(this);
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
        if (realm.sharedRealm == null || realm.sharedRealm.isClosed() || !row.isAttached()) {
            return;
        }

        realm.sharedRealm.realmNotifier.addChangeListener(this, new RealmChangeListener<ProxyState<E>>() {
            @Override
            public void onChange(ProxyState<E> element) {
                long tableVersion = -1;
                if (row.isAttached()) {
                    // If the Row gets detached, table version will be -1 and it is different from current value.
                    tableVersion = row.getTable().getVersion();
                }
                if (currentTableVersion != tableVersion) {
                    currentTableVersion = tableVersion;
                    notifyChangeListeners();
                }
            }
        });
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
            notifyChangeListeners();
        }
    }

    @Override
    public void onQueryFinished(Row row) {
        this.row = row;
        notifyChangeListeners();
        if (row.isAttached()) {
            // getTable should return a non-null table since the row should always be valid here.
            currentTableVersion = row.getTable().getVersion();
            registerToRealmNotifier();
        }
    }
}
