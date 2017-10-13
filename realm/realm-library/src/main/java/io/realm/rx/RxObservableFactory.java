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

package io.realm.rx;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.OrderedCollectionChangeSet;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Factory interface for creating Rx Observables for Realm classes.
 */
public interface RxObservableFactory {

    /**
     * Creates a Flowable for a {@link Realm}. It should emit the initial state of the Realm when subscribed to and
     * on each subsequent update of the Realm.
     * <p>
     * Realm flowables are hot as Realms are automatically kept up to date.
     *
     * @param realm {@link Realm} to listen to changes for.
     * @return Rx observable that emit all updates to the Realm.
     */
    Flowable<Realm> from(Realm realm);

    /**
     * Creates a Flowable for a {@link DynamicRealm}. It should emit the initial state of the Realm when subscribed
     * to and on each subsequent update of the Realm.
     * <p>
     * DynamicRealm observables are hot as DynamicRealms are automatically kept up to date.
     *
     * @param realm {@link DynamicRealm} to listen to changes for.
     * @return Rx observable that emit all updates to the DynamicRealm.
     */
    Flowable<DynamicRealm> from(DynamicRealm realm);

    /**
     * Creates a Flowable for a {@link RealmResults}. It should emit the initial RealmResult when subscribed to and
     * on each subsequent update of the RealmResults.
     * <p>
     * RealmResults observables are hot as RealmResults are automatically kept up to date.
     *
     * @param results {@link RealmResults} to listen to changes for.
     * @param realm {@link Realm} instance results are coming from.
     * @param <E> type of RealmObject
     * @return Rx observable that emit all updates to the RealmObject.
     */
    <E> Flowable<RealmResults<E>> from(Realm realm, RealmResults<E> results);

    /**
     * Creates an Observable for a {@link RealmResults}. It should emit the initial RealmResult when subscribed to and
     * on each subsequent update of the RealmResults it should emit the RealmResults + the {@link OrderedCollectionChangeSet}
     * that describes the update.
     * <p>
     * Changeset observables do not support backpressure as a changeset depends on the state of the previous
     * changeset. Handling backpressure should therefor be left to users.
     *
     * @param results {@link RealmResults} to listen to changes for.
     * @param realm {@link Realm} instance results are coming from.
     * @param <E> type of RealmObject
     * @return Rx observable that emit all updates + their changeset.
     */
    <E> Observable<CollectionChange<RealmResults<E>>> changesetsFrom(Realm realm, RealmResults<E> results);

    /**
     * Creates a Flowable for a {@link RealmResults}. It should emit the initial RealmResult when subscribed to and
     * on each subsequent update of the RealmResults.
     * <p>
     * Realm observables are hot as RealmResults are automatically kept up to date.
     *
     * @param results {@link RealmResults} to listen to changes for.
     * @param realm {@link DynamicRealm} instance results are coming from.
     * @return Rx observable that emit all updates to the RealmResults.
     */
    <E> Flowable<RealmResults<E>> from(DynamicRealm realm, RealmResults<E> results);

    /**
     * Creates an Observable for a {@link RealmResults}. It should emit the initial RealmResult when subscribed to and
     * on each subsequent update of the RealmResults it should emit the RealmResults + the {@link OrderedCollectionChangeSet}
     * that describes the update.
     * <p>
     * Changeset observables do not support backpressure as a changeset depends on the state of the previous
     * changeset. Handling backpressure should therefor be left to users.
     *
     * @param results {@link RealmResults} to listen to changes for.
     * @param realm {@link Realm} instance results are coming from.
     * @return Rx observable that emit all updates + their changeset.
     */
    <E> Observable<CollectionChange<RealmResults<E>>> changesetsFrom(DynamicRealm realm, RealmResults<E> results);

    /**
     * Creates an Observable for a {@link RealmList}. It should emit the initial list when subscribed to and on each
     * subsequent update of the RealmList.
     * <p>
     * RealmList observables are hot as RealmLists are automatically kept up to date.
     * <p>
     * Note: {@link io.realm.RealmChangeListener} is currently not supported on RealmLists.
     *
     * @param list RealmObject to listen to changes for.
     * @param realm {@link Realm} instance list is coming from.
     * @param <E> type of query target
     */
    <E> Flowable<RealmList<E>> from(Realm realm, RealmList<E> list);

    /**
     * Creates an Observable for a {@link RealmList}. It should emit the initial RealmList when subscribed to and
     * on each subsequent update of the RealmIst it should emit the RealmList + the {@link OrderedCollectionChangeSet}
     * that describes the update.
     * <p>
     * Changeset observables do not support backpressure as a changeset depends on the state of the previous
     * changeset. Handling backpressure should therefor be left to users.
     *
     * @param list {@link RealmList} to listen to changes for.
     * @param realm {@link Realm} instance list is coming from.
     * @param <E> type of RealmObject
     * @return Rx observable that emit all updates + their changeset.
     */
    <E> Observable<CollectionChange<RealmList<E>>> changesetsFrom(Realm realm, RealmList<E> list);

    /**
     * Creates a Flowable for a {@link RealmList}. It should emit the initial list when subscribed to and on each
     * subsequent update of the RealmList.
     * <p>
     * RealmList observables are hot as RealmLists are automatically kept up to date.
     * <p>
     * Note: {@link io.realm.RealmChangeListener} is currently not supported on RealmLists.
     *
     * @param list RealmList to listen to changes for.
     * @param realm {@link DynamicRealm} instance list is coming from.
     */
    <E> Flowable<RealmList<E>> from(DynamicRealm realm, RealmList<E> list);

    /**
     * Creates an Observable for a {@link RealmList}. It should emit the initial RealmList when subscribed to and
     * on each subsequent update of the RealmList it should emit the RealmList + the {@link OrderedCollectionChangeSet}
     * that describes the update.
     * <p>
     * Changeset observables do not support backpressure as a changeset depends on the state of the previous
     * changeset. Handling backpressure should therefor be left to users.
     *
     * @param list {@link RealmList} to listen to changes for.
     * @param realm {@link Realm} instance list is coming from.
     * @return Rx observable that emit all updates + their changeset.
     */
    <E> Observable<CollectionChange<RealmList<E>>> changesetsFrom(DynamicRealm realm, RealmList<E> list);

    /**
     * Creates a Flowable for a {@link RealmObject}. It should emit the initial object when subscribed to and on each
     * subsequent update of the object.
     * <p>
     * RealmObject observables are hot as RealmObjects are automatically kept up to date.
     *
     * @param object RealmObject to listen to changes for.
     * @param realm {@link Realm} instance object is coming from.
     * @param <E> type of query target
     */
    <E extends RealmModel> Flowable<E> from(Realm realm, E object);

    /**
     * Creates an Observable for a {@link RealmObject}. It should emit the initial object when subscribed to and on each
     * subsequent update of the object it should emit the object + the {@link io.realm.ObjectChangeSet} that describes
     * the update.
     * <p>
     * Changeset observables do not support backpressure as a changeset depends on the state of the previous
     * changeset. Handling backpressure should therefore be left to the user.
     *
     * @param object RealmObject to listen to changes for.
     * @param realm {@link Realm} instance object is coming from.
     * @param <E> type of RealmObject
     */
    <E extends RealmModel> Observable<ObjectChange<E>> changesetsFrom(Realm realm, E object);

    /**
     * Creates a Flowable for a {@link DynamicRealmObject}. It should emit the initial object when subscribed to and
     * on each subsequent update of the object.
     * <p>
     * DynamicRealmObject observables are hot as DynamicRealmObjects automatically are kept up to date.
     *
     * @param object DynamicRealmObject to listen to changes for.
     * @param realm {@link DynamicRealm} instance object is coming from.
     */
    Flowable<DynamicRealmObject> from(DynamicRealm realm, DynamicRealmObject object);

    /**
     * Creates an Observable for a {@link RealmObject}. It should emit the initial object when subscribed to and on each
     * subsequent update of the object it should emit the object + the {@link io.realm.ObjectChangeSet} that describes
     * the update.
     * <p>
     * Changeset observables do not support backpressure as a changeset depends on the state of the previous
     * changeset. Handling backpressure should therefore be left to the user.
     *
     * @param object RealmObject to listen to changes for.
     * @param realm {@link Realm} instance object is coming from.
     */
    Observable<ObjectChange<DynamicRealmObject>> changesetsFrom(DynamicRealm realm, DynamicRealmObject object);

    /**
     * Creates a Single from a {@link RealmQuery}. It should emit the query and then complete.
     * <p>
     * A RealmQuery observable is cold.
     *
     * @param query RealmQuery to emit.
     * @param realm {@link Realm} instance query is coming from.
     * @param <E> type of query target
     */
    <E> Single<RealmQuery<E>> from(Realm realm, RealmQuery<E> query);

    /**
     * Creates a Single from a {@link RealmQuery}. It should emit the query and then complete.
     * <p>
     * A RealmQuery observable is cold.
     *
     * @param query RealmObject to listen to changes for.
     * @param realm {@link DynamicRealm} instance query is coming from.
     */
    <E> Single<RealmQuery<E>> from(DynamicRealm realm, RealmQuery<E> query);
}
