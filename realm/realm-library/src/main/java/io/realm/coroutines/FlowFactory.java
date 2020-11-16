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

import io.reactivex.Observable;
import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.Beta;
import io.realm.rx.CollectionChange;
import io.realm.rx.ObjectChange;
import kotlinx.coroutines.flow.Flow;

/**
 * Factory interface for creating Kotlin {@link Flow}s for Realm classes.
 */
@Beta
public interface FlowFactory {

    /**
     * Creates a {@link Flow} for a {@link Realm}. It should emit the initial state of the Realm when subscribed to and
     * on each subsequent update of the Realm.
     *
     * @param realm {@link Realm} instance being observed for changes to be emitted by the flow.
     * @return Flow that emits all updates to the Realm.
     */
    @Beta
    Flow<Realm> from(@Nonnull Realm realm);

    /**
     * Creates a {@link Flow} for a {@link DynamicRealm}. It should emit the initial state of the Realm when subscribed to and
     * on each subsequent update of the Realm.
     *
     * @param dynamicRealm {@link DynamicRealm} instance being observed for changes to be emitted by the flow.
     * @return Flow that emits all updates to the Realm.
     */
    @Beta
    Flow<DynamicRealm> from(@Nonnull DynamicRealm dynamicRealm);

    /**
     * Creates a {@link Flow} for a {@link RealmResults}. It should emit the initial RealmResult when subscribed to and
     * on each subsequent update of the RealmResults.
     *
     * @param results {@link RealmResults} instance being observed for changes to be emitted by the flow.
     * @param realm   {@link Realm} instance from where the results are coming.
     * @param <T>     type of RealmObject.
     * @return {@link Flow} that emits all updates to the RealmObject.
     */
    @Beta
    <T> Flow<RealmResults<T>> from(@Nonnull Realm realm, @Nonnull RealmResults<T> results);

    /**
     * Creates a {@link Flow} for a {@link RealmResults} instance. It should emit the initial results when subscribed to and on each
     * subsequent update of the results it should emit the results plus the {@link io.realm.rx.CollectionChange} that describes
     * the update.
     * <p>
     * Changeset observables do not support backpressure as a changeset depends on the state of the previous
     * changeset. Handling backpressure should therefore be left to the user.
     *
     * @param realm   {@link Realm} instance from where the object is coming.
     * @param results {@link RealmResults} instance being observed for changes to be emitted by the flow.
     * @return {@link Flow} that emits all updates to the RealmResults.
     */
    @Beta
    <T> Flow<CollectionChange<RealmResults<T>>> changesetFrom(@Nonnull Realm realm, @Nonnull RealmResults<T> results);

    /**
     * Creates a {@link Flow} for a {@link RealmResults}. It should emit the initial RealmResult when subscribed to and
     * on each subsequent update of the RealmResults.
     *
     * @param results      {@link RealmResults} instance being observed for changes to be emitted by the flow.
     * @param dynamicRealm {@link DynamicRealm} instance from where the results are coming.
     * @param <T>          type of RealmObject.
     * @return {@link Flow} that emits all updates to the RealmObject.
     */
    @Beta
    <T> Flow<RealmResults<T>> from(@Nonnull DynamicRealm dynamicRealm, @Nonnull RealmResults<T> results);

    /**
     * Creates a {@link Flow} for a {@link RealmResults} instance. It should emit the initial results when subscribed to and on each
     * subsequent update of the results it should emit the results plus the {@link io.realm.rx.CollectionChange} that describes
     * the update.
     * <p>
     * Changeset observables do not support backpressure as a changeset depends on the state of the previous
     * changeset. Handling backpressure should therefore be left to the user.
     *
     * @param dynamicRealm {@link DynamicRealm} instance from where the object is coming.
     * @param results      {@link RealmResults} instance being observed for changes to be emitted by the flow.
     * @return {@link Flow} that emits all updates to the RealmResults.
     */
    @Beta
    <T> Flow<CollectionChange<RealmResults<T>>> changesetFrom(@Nonnull DynamicRealm dynamicRealm, @Nonnull RealmResults<T> results);

    /**
     * Creates a {@link Flow} for a {@link RealmList}. It should emit the initial RealmResult when subscribed to and
     * on each subsequent update of the RealmList.
     * <p>
     * Note: {@link io.realm.RealmChangeListener} is currently not supported on RealmLists.
     *
     * @param realmList {@link RealmList} instance being observed for changes to be emitted by the flow.
     * @param realm     {@link Realm} instance from where the results are coming.
     * @param <T>       type of RealmObject
     * @return {@link Flow} that emit all updates to the RealmList.
     */
    @Beta
    <T> Flow<RealmList<T>> from(@Nonnull Realm realm, @Nonnull RealmList<T> realmList);

    /**
     * Creates a {@link Flow} for a {@link RealmList}. It should emit the initial list when subscribed to and on each
     * subsequent update of the list it should emit the list plus the {@link io.realm.rx.CollectionChange} that describes
     * the update.
     * <p>
     * Changeset observables do not support backpressure as a changeset depends on the state of the previous
     * changeset. Handling backpressure should therefore be left to the user.
     *
     * @param realm {@link Realm} instance from where the object is coming.
     * @param list  {@link RealmList} instance being observed for changes to be emitted by the flow.
     * @return {@link Flow} that emits all updates to the RealmList.
     */
    @Beta
    <T> Flow<CollectionChange<RealmList<T>>> changesetFrom(@Nonnull Realm realm, @Nonnull RealmList<T> list);

    /**
     * Creates a {@link Flow} for a {@link RealmList}. It should emit the initial RealmResult when subscribed to and
     * on each subsequent update of the RealmList.
     * <p>
     * Note: {@link io.realm.RealmChangeListener} is currently not supported on RealmLists.
     *
     * @param realmList    {@link RealmList} instance being observed for changes to be emitted by the flow.
     * @param dynamicRealm {@link DynamicRealm} instance from where the results are coming.
     * @param <T>          type of RealmObject
     * @return {@link Flow} that emit all updates to the RealmList.
     */
    @Beta
    <T> Flow<RealmList<T>> from(@Nonnull DynamicRealm dynamicRealm, @Nonnull RealmList<T> realmList);

    /**
     * Creates a {@link Flow} for a {@link RealmList}. It should emit the initial list when subscribed to and on each
     * subsequent update of the list it should emit the list plus the {@link io.realm.rx.CollectionChange} that describes
     * the update.
     * <p>
     * Changeset observables do not support backpressure as a changeset depends on the state of the previous
     * changeset. Handling backpressure should therefore be left to the user.
     *
     * @param dynamicRealm {@link DynamicRealm} instance from where the object is coming.
     * @param list         {@link RealmList} instance being observed for changes to be emitted by the flow.
     * @return {@link Flow} that emits all updates to the RealmList.
     */
    @Beta
    <T> Flow<CollectionChange<RealmList<T>>> changesetFrom(@Nonnull DynamicRealm dynamicRealm, @Nonnull RealmList<T> list);

    /**
     * Creates a {@link Flow} for a {@link RealmObject}. It should emit the initial object when subscribed to and on each
     * subsequent update of the object.
     *
     * @param realmObject {@link RealmObject} instance being observed for changes to be emitted by the flow.
     * @param realm       {@link Realm} instance from where the object is coming.
     * @param <T>         type of query target
     * @return {@link Flow} that emits all updates to the DynamicRealmObject.
     */
    @Beta
    <T extends RealmModel> Flow<T> from(@Nonnull Realm realm, @Nonnull T realmObject);

    /**
     * Creates a {@link Flow} for a {@link RealmObject}. It should emit the initial object when subscribed to and on each
     * subsequent update of the object it should emit the object plus the {@link io.realm.ObjectChangeSet} that describes
     * the update.
     * <p>
     * Changeset observables do not support backpressure as a changeset depends on the state of the previous
     * changeset. Handling backpressure should therefore be left to the user.
     *
     * @param realm       {@link Realm} instance from where the object is coming.
     * @param realmObject {@link RealmObject} instance being observed for changes to be emitted by the flow.
     * @return {@link Flow} that emits all updates to the DynamicRealmObject.
     */
    @Beta
    <T extends RealmModel> Flow<ObjectChange<T>> changesetFrom(@Nonnull Realm realm, @Nonnull T realmObject);

    /**
     * Creates a {@link Flow} for a {@link DynamicRealmObject}. It should emit the initial object when subscribed to and
     * on each subsequent update of the object.
     *
     * @param dynamicRealm       {@link DynamicRealm} instance from where the object is coming.
     * @param dynamicRealmObject {@link DynamicRealmObject} instance being observed for changes to be emitted by the flow.
     * @return {@link Flow} that emits all updates to the DynamicRealmObject.
     */
    @Beta
    Flow<DynamicRealmObject> from(@Nonnull DynamicRealm dynamicRealm, @Nonnull DynamicRealmObject dynamicRealmObject);

    /**
     * Creates a {@link Flow} for a {@link DynamicRealmObject}. It should emit the initial object when subscribed to and on each
     * subsequent update of the object it should emit the object plus the {@link io.realm.ObjectChangeSet} that describes
     * the update.
     * <p>
     * Changeset observables do not support backpressure as a changeset depends on the state of the previous
     * changeset. Handling backpressure should therefore be left to the user.
     *
     * @param dynamicRealm       {@link DynamicRealm} instance from where the object is coming.
     * @param dynamicRealmObject {@link DynamicRealmObject} instance being observed for changes to be emitted by the flow.
     * @return {@link Flow} that emits all updates to the DynamicRealmObject.
     */
    @Beta
    Flow<ObjectChange<DynamicRealmObject>> changesetFrom(@Nonnull DynamicRealm dynamicRealm, @Nonnull DynamicRealmObject dynamicRealmObject);
}
