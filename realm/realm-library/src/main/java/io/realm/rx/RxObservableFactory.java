package io.realm.rx;

import io.realm.BaseRealm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;
import rx.Observable;

/**
 * Factory interface for creating Rx Observables from Realm classes.
 */
public interface RxObservableFactory {

    /**
     * Creates an Observable for a Realm. It should emit the initial state of the Realm when subscribed to and on each
     * update to the Realm.
     *
     * Realm observables are effectively hot observables as Realms auto-update.
     *
     * @param realm RealmResults to listen to changes for.
     */
    <E extends BaseRealm> Observable<E> from(E realm);

    /**
     * Creates an Observable for a {@link RealmResults}. It should emit the initial results when subscribed to and on
     * each update to the results.
     *
     * RealmResults observables are effectively hot observables as RealmObjects auto-update.
     *
     * @param results RealmResults to listen to changes for.
     * @param <E> Type of RealmObject
     */
    <E extends RealmObject> Observable<RealmResults<E>> from(RealmResults<E> results);

    /**
     * Creates an Observable for a RealmList. It should emit the initial list when subscribed to and on each
     * update to the list.
     *
     * RealmList observables are effectively hot observables as RealmLists auto-update.
     *
     * @param list RealmObject to listen to changes for.
     * @param <E> Type of RealmObject
     */
    <E extends RealmObject> Observable<RealmList<E>> from(RealmList<E> list);


    /**
     * Creates an Observable for a RealmObject. It should emit the initial object when subscribed to and on each update
     * to the object.
     *
     * RealmObject observables are effectively hot observables as RealmObjects auto-update.
     *
     * @param object RealmObject to listen to changes for.
     * @param <E> Type of RealmObject
     */
    <E extends RealmObject> Observable<E> from(E object);
}
