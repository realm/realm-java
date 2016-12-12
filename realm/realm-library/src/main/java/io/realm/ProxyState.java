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

import io.realm.internal.PendingRow;
import io.realm.internal.Row;
import io.realm.internal.RowNotifier;
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
        if (row instanceof PendingRow) {
            row = ((PendingRow) row).executeQuery();
            registerToRowNotifier();
        }
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

    public List<RealmChangeListener<E>> getListeners$realm() {
        return listeners;
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
            registerToRowNotifier();
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
        // only used while construction.
        excludeFields = null;
    }

    private void registerToRowNotifier() {
        if (realm.sharedRealm == null || realm.sharedRealm.isClosed()) {
            return;
        }

        RowNotifier rowNotifier = realm.sharedRealm.rowNotifier;
        if (row.isAttached()) {
            rowNotifier.registerListener((UncheckedRow) row, this, new RealmChangeListener<ProxyState<E>>() {
                @Override
                public void onChange(ProxyState<E> proxyState) {
                    proxyState.notifyChangeListeners();
                }
            });
        }
    }

    @Override
    public void onQueryFinished(Row row) {
        this.row = row;
        notifyChangeListeners();
        registerToRowNotifier();
    }
}
