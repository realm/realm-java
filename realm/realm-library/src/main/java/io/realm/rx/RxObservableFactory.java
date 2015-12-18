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

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import rx.Observable;

/**
 * Factory interface for creating Rx Observables for Realm classes.
 */
public interface RxObservableFactory {

    /**
     * Creates an Observable for a {@link Realm}. It should emit the initial state of the Realm when subscribed to and
     * on each subsequent update of the Realm.
     *
     * Realm observables are hot observables as Realms are automatically kept up to date.
     *
     * @param realm {@link Realm} to listen to changes for.
     * @return Rx observable that emit all updates to the Realm.
     */
    Observable<Realm> from(Realm realm);

    /**
     * Creates an Observable for a {@link DynamicRealm}. It should emit the initial state of the Realm when subscribed
     * to and on each subsequent update of the Realm.
     *
     * DynamicRealm observables are hot observables as DynamicRealms are automatically kept up to date.
     *
     * @param realm {@link DynamicRealm} to listen to changes for.
     * @return Rx observable that emit all updates to the DynamicRealm.
     */
    Observable<DynamicRealm> from(DynamicRealm realm);

    /**
     * Creates an Observable for a {@link RealmResults}. It should emit the initial RealmResult when subscribed to and
     * on each subsequent update of the RealmResults.
     *
     * RealmResults observables are hot observables as RealmResults are automatically kept up to date.
     *
     * @param results {@link RealmResults} to listen to changes for.
     * @param realm {@link Realm} instance results are coming from.
     * @param <E> type of RealmObject
     * @return Rx observable that emit all updates to the RealmObject.
     */
    <E extends RealmObject> Observable<RealmResults<E>> from(Realm realm, RealmResults<E> results);

    /**
     * Creates an Observable for a {@link RealmResults}. It should emit the initial RealmResult when subscribed to and
     * on each subsequent update of the RealmResults.
     *
     * Realm observables are hot observables as RealmResults are automatically kept up to date.
     *
     * @param results {@link RealmResults} to listen to changes for.
     * @param realm {@link DynamicRealm} instance results are coming from.
     * @return Rx observable that emit all updates to the RealmResults.
     */
    Observable<RealmResults<DynamicRealmObject>> from(DynamicRealm realm, RealmResults<DynamicRealmObject> results);

    /**
     * Creates an Observable for a {@link RealmList}. It should emit the initial list when subscribed to and on each
     * subsequent update of the RealmList.
     *
     * RealmList observables are hot observables as RealmLists are automatically kept up to date.
     *
     * Note: {@link io.realm.RealmChangeListener} is currently not supported on RealmLists.
     *
     * @param list RealmObject to listen to changes for.
     * @param realm {@link Realm} instance list is coming from.
     * @param <E> type of RealmObject
     */
    <E extends RealmObject> Observable<RealmList<E>> from(Realm realm, RealmList<E> list);

    /**
     * Creates an Observable for a {@link RealmList}. It should emit the initial list when subscribed to and on each
     * subsequent update of the RealmList.
     *
     * RealmList observables are hot observables as RealmLists are automatically kept up to date.
     *
     * Note: {@link io.realm.RealmChangeListener} is currently not supported on RealmLists.
     *
     * @param list RealmList to listen to changes for.
     * @param realm {@link DynamicRealm} instance list is coming from.
     */
    Observable<RealmList<DynamicRealmObject>> from(DynamicRealm realm, RealmList<DynamicRealmObject> list);

    /**
     * Creates an Observable for a {@link RealmObject}. It should emit the initial object when subscribed to and on each
     * subsequent update of the object.
     *
     * RealmObject observables are hot observables as RealmObjects are automatically kept up to date.
     *
     * @param object RealmObject to listen to changes for.
     * @param realm {@link Realm} instance object is coming from.
     * @param <E> type of RealmObject
     */
    <E extends RealmObject> Observable<E> from(Realm realm, E object);

    /**
     * Creates an Observable for a {@link DynamicRealmObject}. It should emit the initial object when subscribed to and
     * on each subsequent update of the object.
     *
     * DynamicRealmObject observables are hot observables as DynamicRealmObjects automatically are kept up to date.
     *
     * @param object DynamicRealmObject to listen to changes for.
     * @param realm {@link DynamicRealm} instance object is coming from.
     */
    Observable<DynamicRealmObject> from(DynamicRealm realm, DynamicRealmObject object);

    /**
     * Creates an Observable from a {@link RealmQuery}. It should emit the query and then complete.
     *
     * A RealmQuery observable is cold.
     *
     * @param query RealmQuery to emit.
     * @param realm {@link Realm} instance query is coming from.
     * @param <E> type of RealmObject
     */
    <E extends RealmObject> Observable<RealmQuery<E>> from(Realm realm, RealmQuery<E> query);

    /**
     * Creates an Observable from a {@link RealmQuery}. It should emit the query and then complete.
     *
     * A RealmQuery observable is cold.
     *
     * @param query RealmObject to listen to changes for.
     * @param realm {@link DynamicRealm} instance query is coming from.
     */
    Observable<RealmQuery<DynamicRealmObject>> from(DynamicRealm realm, RealmQuery<DynamicRealmObject> query);
}
