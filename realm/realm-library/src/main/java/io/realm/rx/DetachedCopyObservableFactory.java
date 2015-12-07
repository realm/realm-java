//package io.realm.rx;
//
//import io.realm.DynamicRealm;
//import io.realm.DynamicRealmObject;
//import io.realm.Realm;
//import io.realm.RealmChangeListener;
//import io.realm.RealmList;
//import io.realm.RealmObject;
//import io.realm.RealmQuery;
//import io.realm.RealmResults;
//import rx.Observable;
//import rx.Subscriber;
//import rx.functions.Action0;
//import rx.subscriptions.Subscriptions;
//
///**
// * Specialized factory class for creating RxJava Observables that create detached deep copies of all Realm objects
// * before emitting them. This is effectively a snapshot of the Realm data at a given point in time.
// *
// * This has the following implications:
// *
// * - Increased memory usage, as all fields including references are copied to the Java heap.
// * - The Realm object is now considered a "standalone" object. Future changes to it are not persisted in Realm and the
// *   object will no longer automatically update either.
// * - All guarantees of staying consistent with other data from persisted Realm objects are gone.
// * - The object is no longer thread-confined and can be parsed between threads.
// *
// * @see {@link io.realm.Realm#copyFromRealm(RealmObject)}
// */
//public class DetachedCopyObservableFactory implements RxObservableFactory {
//
//    private final int maxDepth;
//    private boolean rxJavaAvailble;
//
//    /**
//     * Creates an instance of this factory class. Detached objects also copy all referenced objects.
//     */
//    public DetachedCopyObservableFactory() {
//        this(Integer.MAX_VALUE);
//    }
//
//    /**
//     * Creates an instance of this factory class. Detached copies only contain references up to {@code maxDepth} away.
//     *
//     * @param maxDepth maximum distance from root object that objects are copied. References above this limit will be
//     *                 {@code null}.
//     * @see io.realm.Realm#copyFromRealm(RealmObject, int)
//     */
//    public DetachedCopyObservableFactory(int maxDepth) {
//        this.maxDepth = maxDepth;
//        try {
//            Class.forName("rx.Observable");
//            rxJavaAvailble = true;
//        } catch (ClassNotFoundException ignore) {
//            rxJavaAvailble = false;
//        }
//    }
//
//    @Override
//    public Observable<Realm> from(final Realm realm) {
//        checkRxJavaAvailable();
//        return Observable.create(new Observable.OnSubscribe<Realm>() {
//            @Override
//            public void call(final Subscriber<? super Realm> subscriber) {
//                final RealmChangeListener listener = new RealmChangeListener() {
//                    @Override
//                    public void onChange() {
//                        subscriber.onNext(realm);
//                    }
//                };
//                realm.addChangeListener(listener);
//                subscriber.add(Subscriptions.create(new Action0() {
//                    @Override
//                    public void call() {
//                        realm.removeChangeListener(listener);
//                    }
//                }));
//
//                // Immediately call onNext with the current value as due to Realms auto-update it will be the latest
//                // value.
//                subscriber.onNext(realm);
//            }
//        });
//    }
//
//    @Override
//    public Observable<DynamicRealm> from(final DynamicRealm realm) {
//        checkRxJavaAvailable();
//        return Observable.create(new Observable.OnSubscribe<DynamicRealm>() {
//            @Override
//            public void call(final Subscriber<? super DynamicRealm> subscriber) {
//                final RealmChangeListener listener = new RealmChangeListener() {
//                    @Override
//                    public void onChange() {
//                        subscriber.onNext(realm);
//                    }
//                };
//                realm.addChangeListener(listener);
//                subscriber.add(Subscriptions.create(new Action0() {
//                    @Override
//                    public void call() {
//                        realm.removeChangeListener(listener);
//                    }
//                }));
//
//                // Immediately call onNext with the current value as due to Realms auto-update it will be the latest
//                // value.
//                subscriber.onNext(realm);
//            }
//        });
//    }
//
//    @Override
//    public <E extends RealmObject> Observable<RealmResults<E>> from(Realm realm, RealmResults<E> results) {
//        return null;
//    }
//
//    @Override
//    public Observable<RealmResults<DynamicRealmObject>> from(DynamicRealm realm, RealmResults<DynamicRealmObject> results) {
//        return null;
//    }
//
//    @Override
//    public <E extends RealmObject> Observable<RealmList<E>> from(Realm realm, RealmList<E> list) {
//        return null;
//    }
//
//    @Override
//    public Observable<RealmList<DynamicRealmObject>> from(DynamicRealm realm, RealmList<DynamicRealmObject> list) {
//        return null;
//    }
//
//    @Override
//    public <E extends RealmObject> Observable<E> from(Realm realm, E object) {
//        return null;
//    }
//
//    @Override
//    public Observable<DynamicRealmObject> from(DynamicRealm realm, DynamicRealmObject object) {
//        return null;
//    }
//
//    @Override
//    public <E extends RealmObject> Observable<RealmQuery<E>> from(Realm realm, RealmQuery<E> query) {
//        return null;
//    }
//
//    @Override
//    public Observable<RealmQuery<DynamicRealmObject>> from(DynamicRealm realm, RealmQuery<DynamicRealmObject> query) {
//        return null;
//    }
//
//    @Override
//    public <E extends RealmObject> Observable<RealmResults<E>> from(final RealmResults<E> results) {
//        checkRxJavaAvailable();
//        return Observable.create(new Observable.OnSubscribe<RealmResults<E>>() {
//            @Override
//            public void call(final Subscriber<? super RealmResults<E>> subscriber) {
//                final RealmChangeListener listener = new RealmChangeListener() {
//                    @Override
//                    public void onChange() {
//                        subscriber.onNext(results);
//                    }
//                };
//                results.addChangeListener(listener);
//                subscriber.add(Subscriptions.create(new Action0() {
//                    @Override
//                    public void call() {
//                        results.removeChangeListener(listener);
//                    }
//                }));
//
//                // Immediately call onNext with the current value as due to Realms auto-update it will be the latest
//                // value.
//                subscriber.onNext(results);
//            }
//        });
//    }
//
//    @Override
//    public <E extends RealmObject> Observable<RealmList<E>> from(RealmList<E> list) {
//        checkRxJavaAvailable();
//        throw new RuntimeException("RealmList does not support change listeners yet, so cannot create an Observable");
//    }
//
//    @Override
//    public <E extends RealmObject> Observable<E> from(final E object) {
//        checkRxJavaAvailable();
//        return Observable.create(new Observable.OnSubscribe<E>() {
//            @Override
//            public void call(final Subscriber<? super E> subscriber) {
//                final RealmChangeListener listener = new RealmChangeListener() {
//                    @Override
//                    public void onChange() {
//                        subscriber.onNext(object);
//                    }
//                };
//                object.addChangeListener(listener);
//                subscriber.add(Subscriptions.create(new Action0() {
//                    @Override
//                    public void call() {
//                        object.removeChangeListener(listener);
//                    }
//                }));
//
//                // Immediately call onNext with the current value as due to Realms auto-update it will be the latest
//                // value.
//                subscriber.onNext(object);
//            }
//        });
//    }
//
//    private void checkRxJavaAvailable() {
//        if (!rxJavaAvailble) {
//            throw new IllegalStateException("RxJava seems to be missing from the classpath. " +
//                    "Remember to add it as a compile dependency. See https://realm.io/docs/java/latest/#rxjava for more details.");
//        }
//    }
//}
