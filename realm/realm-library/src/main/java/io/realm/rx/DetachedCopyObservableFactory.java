package io.realm.rx;

import io.realm.BaseRealm;
import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;
import rx.Observable;

/**
 * Specialized factory class for creating RxJava Observables that create detached deep copies of all Realm objects before
 * emitting them. This is effectively a snapshot of the Realm data at a given point in time.
 *
 * This has the following implications:
 *
 * - Increased memory usage, as all fields including references are copied to the Java heap.
 * - The Realm object is now considered a "standalone" object. Future changes to it are not persisted in Realm, but
 *   the object will no longer automatically update either.
 * - All guarantees of staying consistent with other data from persisted Realm objects are gone.
 * - The object is no longer thread-confined and can be parsed between threads.
 *
 * @see {@link io.realm.Realm#copyFromRealm(RealmObject)}
 */
public class DetachedCopyObservableFactory implements RxObservableFactory {

    private final int maxDepth;

    /**
     * Creates an instance of this factory class. Detached objects also copy all referenced objects.
     */
    public DetachedCopyObservableFactory() {
        this(Integer.MAX_VALUE);
    }

    /**
     * Creates an instance of this factory class. Detached copies only contain references up to {@code maxDepth} away.
     *
     * @param maxDepth maximum distance from root object that objects are copied. References above this limit will be
     *                 {@code null}.
     * @see io.realm.Realm#copyFromRealm(RealmObject, int)
     */
    public DetachedCopyObservableFactory(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    @Override
    public Observable<Realm> from(Realm realm) {
        return null;
    }

    @Override
    public Observable<DynamicRealm> from(DynamicRealm realm) {
        return null;
    }

    @Override
    public <E extends RealmObject> Observable<RealmResults<E>> from(RealmResults<E> results) {
        return null;
    }

    @Override
    public <E extends RealmObject> Observable<RealmList<E>> from(RealmList<E> list) {
        return null;
    }

    @Override
    public <E extends RealmObject> Observable<E> from(E object) {
        return null;
    }
}
