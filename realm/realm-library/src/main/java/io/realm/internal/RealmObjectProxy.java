/*
 * Copyright 2015 Realm Inc.
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

package io.realm.internal;

import java.util.List;
import java.util.concurrent.Future;

import io.realm.BaseRealm;
import io.realm.RealmChangeListener;
import io.realm.RealmModel;

/**
 * Interface making it easy to determine if an object is the generated RealmProxy class or the original class.
 *
 * Ideally all the static methods was also present here, but that is not supported before Java 8.
 */
 interface RealmObjectProxy extends RealmModel {

    BaseRealm getRealm$realm();
    void setRealm$realm(BaseRealm realm);
    Row getRow$realm();
    void setRow$realm(Row row);
    Object getPendingQuery$realm();
    boolean isCompleted$realm();
    boolean onCompleted$realm();
    void onCompleted$realm(long rowPointer);
    List<RealmChangeListener> getListeners$realm();
    void setTableVersion$realm();
    void notifyChangeListeners$realm();
    void setPendingQuery$realm(Future<Long> pendingQuery);

    /**
     * Tuple class for saving meta data about a cached RealmObject.
     */
    class CacheData<E extends RealmModel> {
        public int minDepth;
        public final E object;

        public CacheData(int minDepth, E object) {
            this.minDepth = minDepth;
            this.object = object;
        }
    }
}
