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
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import rx.Observable;
import rx.Subscriber;
import rx.exceptions.Exceptions;
import rx.functions.Action0;
import rx.functions.Func0;
import rx.subscriptions.Subscriptions;

/**
 * Factory class for creating Observables for RxJava (&lt;=1.1.*).
 *
 * @see Realm#asObservable()
 * @see RealmObject#asObservable()
 * @see RealmResults#asObservable()
 * @see DynamicRealm#asObservable()
 * @see DynamicRealmObject#asObservable()
 */
public class RealmObservableFactory implements RxObservableFactory {

    private boolean rxJavaAvailable;

    public RealmObservableFactory() {
        try {
            Class.forName("rx.Observable");
            rxJavaAvailable = true;
        } catch (ClassNotFoundException ignore) {
            rxJavaAvailable = false;
        }
    }

    @Override
    public Observable<Realm> from(final Realm realm) {
        checkRxJavaAvailable();
        return Observable.create(new Observable.OnSubscribe<Realm>() {
            @Override
            public void call(final Subscriber<? super Realm> subscriber) {
                final RealmChangeListener listener = new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(realm);
                        }
                    }
                };
                realm.addChangeListener(listener);
                subscriber.add(Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        realm.removeChangeListener(listener);
                    }
                }));
                subscriber.onNext(realm);
            }
        });
    }

    @Override
    public Observable<DynamicRealm> from(final DynamicRealm realm) {
        checkRxJavaAvailable();
        return Observable.create(new Observable.OnSubscribe<DynamicRealm>() {
            @Override
            public void call(final Subscriber<? super DynamicRealm> subscriber) {
                final RealmChangeListener listener = new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(realm);
                        }
                    }
                };
                realm.addChangeListener(listener);
                subscriber.add(Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        realm.removeChangeListener(listener);
                    }
                }));

                // Immediately call onNext with the current value, as due to Realms auto-update, it will be the latest
                // value.
                subscriber.onNext(realm);
            }
        });
    }

    @Override
    public <E extends RealmObject> Observable<RealmResults<E>> from(Realm realm, RealmResults<E> results) {
        checkRxJavaAvailable();
        return getRealmResultsObservable(results);
    }

    @Override
    public Observable<RealmResults<DynamicRealmObject>> from(DynamicRealm realm, RealmResults<DynamicRealmObject> results) {
        checkRxJavaAvailable();
        return getRealmResultsObservable(results);
    }

    private <E extends RealmObject> Observable<RealmResults<E>> getRealmResultsObservable(final RealmResults<E> results) {
        return Observable.create(new Observable.OnSubscribe<RealmResults<E>>() {
            @Override
            public void call(final Subscriber<? super RealmResults<E>> subscriber) {
                final RealmChangeListener listener = new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(results);
                        }
                    }
                };
                results.addChangeListener(listener);
                subscriber.add(Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        results.removeChangeListener(listener);
                    }
                }));

                // Immediately call onNext with the current value, as due to Realms auto-update, it will be the latest
                // value.
                subscriber.onNext(results);
            }
        });
    }

    @Override
    public <E extends RealmObject> Observable<RealmList<E>> from(Realm realm, RealmList<E> list) {
        checkRxJavaAvailable();
        return getRealmListObservable();
    }

    @Override
    public Observable<RealmList<DynamicRealmObject>> from(DynamicRealm realm, RealmList<DynamicRealmObject> list) {
        checkRxJavaAvailable();
        return getRealmListObservable();
    }

    private <E extends RealmObject> Observable<RealmList<E>> getRealmListObservable() {
        throw new RuntimeException("RealmList does not support change listeners yet, so cannot create an Observable");
    }

    @Override
    public <E extends RealmObject> Observable<E> from(Realm realm, final E object) {
        checkRxJavaAvailable();
        return getObjectObservable(object);
    }

    @Override
    public Observable<DynamicRealmObject> from(DynamicRealm realm, DynamicRealmObject object) {
        checkRxJavaAvailable();
        return getObjectObservable(object);
    }

    private <E extends RealmObject> Observable<E> getObjectObservable(final E object) {
        return Observable.create(new Observable.OnSubscribe<E>() {
            @Override
            public void call(final Subscriber<? super E> subscriber) {
                final RealmChangeListener listener = new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(object);
                        }
                    }
                };
                object.addChangeListener(listener);
                subscriber.add(Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        object.removeChangeListener(listener);
                    }
                }));

                // Immediately call onNext with the current value, as due to Realms auto-update, it will be the latest
                // value.
                subscriber.onNext(object);
            }
        });
    }

    @Override
    public <E extends RealmObject> Observable<RealmQuery<E>> from(final Realm realm, RealmQuery<E> query) {
        checkRxJavaAvailable();
        // Create a copy of the RealmQuery to make sure it doesn't change.
        final RealmQuery<E> queryCopy = query.clone();

        return Observable.create(new Observable.OnSubscribe<RealmQuery<E>>() {
            @Override
            public void call(final Subscriber<? super RealmQuery<E>> subscriber) {
                // Create an Realm instance that is open for as long as the subscription is alive.
                Realm subscriberRealm = null;
                try {
                    subscriberRealm = Realm.getInstance(realm.getConfiguration());
                    RealmQuery<E> queryClone = subscriberRealm.threadLocalVersion(queryCopy);
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onNext(queryClone);
                    }
                    subscriberRealm.close(); // Close all Realm resources before completing.
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onCompleted();
                    }
                } catch (Throwable t) {
                    if (subscriberRealm != null && !subscriberRealm.isClosed()) {
                        subscriberRealm.close();
                    }
                    Exceptions.throwIfFatal(t);
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onError(t);
                    }
                }
            }
        });
    }

    @Override
    public Observable<RealmQuery<DynamicRealmObject>> from(final DynamicRealm realm, RealmQuery<DynamicRealmObject> query) {
        checkRxJavaAvailable();
        final RealmQuery<DynamicRealmObject> queryCopy = query.clone(); // Create a copy of the RealmQuery to make sure it doesn't change.

        return Observable.create(new Observable.OnSubscribe<RealmQuery<DynamicRealmObject>>() {
            @Override
            public void call(Subscriber<? super RealmQuery<DynamicRealmObject>> subscriber) {
                // Create an Realm instance that is open for as long as the subscription is alive.
                DynamicRealm subscriberRealm = null;
                try {
                    subscriberRealm = DynamicRealm.getInstance(realm.getConfiguration());
                    RealmQuery<DynamicRealmObject> queryClone = subscriberRealm.threadLocalVersion(queryCopy);
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onNext(queryClone);
                    }
                    subscriberRealm.close();  // Close all Realm resources before completing.
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onCompleted();
                    }
                } catch (Throwable t) {
                    if (subscriberRealm != null && !subscriberRealm.isClosed()) {
                        subscriberRealm.close();
                    }
                    Exceptions.throwIfFatal(t);
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onError(t);
                    }
                }
            }
        });
    }

    private void checkRxJavaAvailable() {
        if (!rxJavaAvailable) {
            throw new IllegalStateException("RxJava seems to be missing from the classpath. " +
                    "Remember to add it as a compile dependency. See https://realm.io/docs/java/latest/#rxjava for more details.");
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof RealmObservableFactory;
    }
}
