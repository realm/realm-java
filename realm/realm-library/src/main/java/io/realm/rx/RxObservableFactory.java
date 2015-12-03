package io.realm.rx;

import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;
import rx.Observable;

/**
 * Factory interface for creating Rx Observables from Realm classes.
 */
public interface RxObservableFactory {

    /**
     * Creates an Observable for a {@link Realm}. It should emit the initial state of the Realm when subscribed to and
     * on each update to the Realm.
     *
     * Realm observables are effectively hot observables as Realms auto-update.
     *
     * @param realm {@link Realm} to listen to changes for.
     */
    Observable<Realm> from(Realm realm);

    /**
     * Creates an Observable for a {@link DynamicRealm}. It should emit the initial state of the Realm when subscribed
     * to and on each update to the Realm.
     *
     * Realm observables are effectively hot observables as Realms auto-update.
     *
     * @param realm {@link DynamicRealm} to listen to changes for.
     */
    Observable<DynamicRealm> from(DynamicRealm realm);

    /**
     * Creates an Observable for a {@link RealmResults}. It should emit the initial results when subscribed to and on
     * each update to the results.
     *
     * RealmResults observables are effectively hot observables as RealmObjects auto-update.
     *
     * @param results {@link RealmResults} to listen to changes for.
     * @param <E> type of RealmObject
     */
    <E extends RealmObject> Observable<RealmResults<E>> from(RealmResults<E> results);

    /**
     * Creates an Observable for a {@link RealmList}. It should emit the initial list when subscribed to and on each
     * update to the list.
     *
     * RealmList observables are effectively hot observables as RealmLists auto-update.
     *
     * Note: {@link io.realm.RealmChangeListener} is currently not supported on RealmLists.
     *
     * @param list RealmObject to listen to changes for.
     * @param <E> type of RealmObject
     */
    <E extends RealmObject> Observable<RealmList<E>> from(RealmList<E> list);

    /**
     * Creates an Observable for a {@link RealmObject}. It should emit the initial object when subscribed to and on each update
     * to the object.
     *
     * RealmObject observables are effectively hot observables as RealmObjects auto-update.
     *
     * @param object RealmObject to listen to changes for.
     * @param <E> type of RealmObject
     */
    <E extends RealmObject> Observable<E> from(E object);
}
