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
     * Realm observables are hot observables as Realms automatically are kept up to date.
     *
     * @param realm {@link Realm} to listen to changes for.
     * @return Rx observable that emit all updates to the Realm.
     */
    Observable<Realm> from(Realm realm);

    /**
     * Creates an Observable for a {@link DynamicRealm}. It should emit the initial state of the Realm when subscribed
     * to and on each subsequent update of the Realm.
     *
     * Realm observables are hot observables as Realms automatically are kept up to date.
     *
     * @param realm {@link DynamicRealm} to listen to changes for.
     * @return Rx observable that emit all updates to the DynamicRealm.
     */
    Observable<DynamicRealm> from(DynamicRealm realm);

    /**
     * Creates an Observable for a {@link RealmResults}. It should emit the initial RealmResult when subscribed to and
     * on each subsequent update of the RealmResults.
     *
     * Realm observables are hot observables as RealmResults are automatically kept up to date.
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
     * Realm observables are hot observables as Realms automatically are kept up to date.
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
     * Realm observables are hot observables as Realms automatically are kept up to date.
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
     * Realm observables are hot observables as Realms automatically are kept up to date.
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
     * Realm observables are hot observables as Realms automatically are kept up to date.
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
