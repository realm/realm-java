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

import javax.annotation.Nullable;

import io.realm.internal.ObserverPairList;
import io.realm.internal.PendingRow;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.Row;
import io.realm.internal.OsObject;
import io.realm.internal.UncheckedRow;


/**
 * This implements {@code RealmObjectProxy} interface, to eliminate copying logic between
 * {@link RealmObject} and {@link DynamicRealmObject}.
 */
public final class ProxyState<E extends RealmModel> implements PendingRow.FrontEnd {

    static class RealmChangeListenerWrapper<T extends RealmModel> implements RealmObjectChangeListener<T> {
        private final RealmChangeListener<T> listener;

        RealmChangeListenerWrapper(RealmChangeListener<T> listener) {
            //noinspection ConstantConditions
            if (listener == null) {
                throw new IllegalArgumentException("Listener should not be null");
            }
            this.listener = listener;
        }

        @Override
        public void onChange(T object, @Nullable ObjectChangeSet changes) {
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

    private static class QueryCallback implements ObserverPairList.Callback<OsObject.ObjectObserverPair> {

        @Override
        public void onCalled(OsObject.ObjectObserverPair pair, Object observer) {
            //noinspection unchecked
            pair.onChange((RealmModel) observer, null);
        }
    }

    private E model;

    // true only while executing the constructor of the enclosing proxy object
    private boolean underConstruction = true;

    private Row row;
    private OsObject osObject;
    private BaseRealm realm;
    private boolean acceptDefaultValue;
    private List<String> excludeFields;

    private ObserverPairList<OsObject.ObjectObserverPair> observerPairs =
            new ObserverPairList<OsObject.ObjectObserverPair>();
    private static QueryCallback queryCallback = new QueryCallback();

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

    @SuppressWarnings("unused")
    public List<String> getExcludeFields$realm() {
        return excludeFields;
    }

    public void setExcludeFields$realm(List<String> excludeFields) {
        this.excludeFields = excludeFields;
    }

    /**
     * Notifies all registered listeners.
     */
    private void notifyQueryFinished() {
        observerPairs.foreach(queryCallback);
    }

    public void addChangeListener(RealmObjectChangeListener<E> listener) {
        if (row instanceof PendingRow) {
            observerPairs.add(new OsObject.ObjectObserverPair<E>(model, listener));
        } else if (row instanceof UncheckedRow) {
            registerToObjectNotifier();
            if (osObject != null) {
                osObject.addListener(model, listener);
            }
        }
    }

    public void removeChangeListener(RealmObjectChangeListener<E> listener) {
        if (osObject != null) {
            osObject.removeListener(model, listener);
        } else {
            observerPairs.remove(model, listener);
        }
    }

    public void removeAllChangeListeners() {
        if (osObject != null) {
            osObject.removeListener(model);
        } else {
            observerPairs.clear();
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

    private void registerToObjectNotifier() {
        if (realm.sharedRealm == null || realm.sharedRealm.isClosed() || !row.isAttached()) {
            return;
        }

        if (osObject == null) {
            osObject = new OsObject(realm.sharedRealm, (UncheckedRow) row);
            osObject.setObserverPairs(observerPairs);
            // We should never need observerPairs after pending row returns.
            observerPairs = null;
        }
    }

    public boolean isLoaded() {
        return !(row instanceof PendingRow);
    }

    public void load() {
        if (row instanceof PendingRow) {
            ((PendingRow) row).executeQuery();
        }
    }

    @Override
    public void onQueryFinished(Row row) {
        this.row = row;
        // getTable should return a non-null table since the row should always be valid here.
        notifyQueryFinished();
        if (row.isAttached()) {
            registerToObjectNotifier();
        }
    }

    /**
     * Check that object is a valid and managed object by this Realm.
     * Used by proxy classes to verify input.
     *
     * @param value model object
     */
    public void checkValidObject(RealmModel value) {
        if (!RealmObject.isValid(value) || !RealmObject.isManaged(value)) {
            throw new IllegalArgumentException("'value' is not a valid managed object.");
        }
        if (((RealmObjectProxy) value).realmGet$proxyState().getRealm$realm() != getRealm$realm()) {
            throw new IllegalArgumentException("'value' belongs to a different Realm.");
        }
    }
}
