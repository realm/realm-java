/*
 * Copyright 2020 Realm Inc.
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

package io.realm.coroutines;

import javax.annotation.Nonnull;

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.RealmObject;
import io.realm.RealmResults;
import kotlinx.coroutines.flow.Flow;

/**
 * FIXME
 */
public interface CoroutinesFactory {
    /**
     * FIXME
     *
     * @param realm
     * @param results
     * @param <T>
     * @return
     */
    <T extends RealmModel> Flow<RealmResults<T>> from(@Nonnull Realm realm, @Nonnull RealmResults<T> results);

    /**
     * FIXME
     *
     * @param realm
     * @param results
     * @param <T>
     * @return
     */
    <T extends RealmObject> Flow<RealmList<T>> from(@Nonnull Realm realm, @Nonnull RealmList<T> results);

    /**
     * FIXME
     *
     * @param realm
     * @param realmModel
     * @param <T>
     * @return
     */
    <T extends RealmModel> Flow<T> from(@Nonnull Realm realm, @Nonnull T realmModel);

    /**
     * FIXME
     *
     * @param realm
     * @param obj
     * @return
     */
    Flow<DynamicRealmObject> from(@Nonnull DynamicRealm realm, @Nonnull DynamicRealmObject obj);
}
