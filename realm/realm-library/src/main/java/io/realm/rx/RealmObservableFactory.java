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

import java.util.IdentityHashMap;
import java.util.Map;

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
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

    // Maps for storing strong references to RealmResults while they are subscribed to.
    // This is needed if users create Observables without manually maintaining a reference to them.
    // In that case RealmObjects/RealmResults might be GC'ed too early.
    ThreadLocal<StrongReferenceCounter<RealmResults>> resultsRefs = new ThreadLocal<StrongReferenceCounter<RealmResults>>() {
        @Override
        protected StrongReferenceCounter<RealmResults> initialValue() {
            return new StrongReferenceCounter<RealmResults>();
        }
    };
    ThreadLocal<StrongReferenceCounter<RealmModel>> objectRefs = new ThreadLocal<StrongReferenceCounter<RealmModel>>() {
        @Override
        protected StrongReferenceCounter<RealmModel> initialValue() {
            return new StrongReferenceCounter<RealmModel>();
        }
    };

    @Override
    public Observable<Realm> from(Realm realm) {
        final RealmConfiguration realmConfig = realm.getConfiguration();
        return Observable.create(new Observable.OnSubscribe<Realm>() {
            @Override
            public void call(final Subscriber<? super Realm> subscriber) {
                // Get instance to make sure that the Realm is open for as long as the
                // Observable is subscribed to it.
                final Realm observableRealm = Realm.getInstance(realmConfig);
                final RealmChangeListener<Realm> listener = new RealmChangeListener<Realm>() {
                    @Override
                    public void onChange(Realm realm) {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(observableRealm);
                        }
                    }
                };
                observableRealm.addChangeListener(listener);
                subscriber.add(Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        observableRealm.removeChangeListener(listener);
                        observableRealm.close();
                    }
                }));
                subscriber.onNext(observableRealm);
            }
        });
    }

    @Override
    public Observable<DynamicRealm> from(DynamicRealm realm) {
        final RealmConfiguration realmConfig = realm.getConfiguration();
        return Observable.create(new Observable.OnSubscribe<DynamicRealm>() {
            @Override
            public void call(final Subscriber<? super DynamicRealm> subscriber) {
                // Get instance to make sure that the Realm is open for as long as the
                // Observable is subscribed to it.
                final DynamicRealm observableRealm = DynamicRealm.getInstance(realmConfig);
                final RealmChangeListener<DynamicRealm> listener = new RealmChangeListener<DynamicRealm>() {
                    @Override
                    public void onChange(DynamicRealm realm) {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(observableRealm);
                        }
                    }
                };
                observableRealm.addChangeListener(listener);
                subscriber.add(Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        observableRealm.removeChangeListener(listener);
                        observableRealm.close();
                    }
                }));

                    // Immediately call onNext with the current value, as due to Realm's auto-update, it will be the latest
                // value.
                subscriber.onNext(observableRealm);
            }
        });
    }

    @Override
    public <E extends RealmModel> Observable<RealmResults<E>> from(final Realm realm, final RealmResults<E> results) {
        final RealmConfiguration realmConfig = realm.getConfiguration();

        return Observable.create(new Observable.OnSubscribe<RealmResults<E>>() {
            @Override
            public void call(final Subscriber<? super RealmResults<E>> subscriber) {
                // Get instance to make sure that the Realm is open for as long as the
                // Observable is subscribed to it.
                final Realm observableRealm = Realm.getInstance(realmConfig);
                resultsRefs.get().acquireReference(results);

                final RealmChangeListener<RealmResults<E>> listener = new RealmChangeListener<RealmResults<E>>() {
                    @Override
                    public void onChange(RealmResults<E> result) {
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
                        observableRealm.close();
                        resultsRefs.get().releaseReference(results);
                    }
                }));

                // Immediately call onNext with the current value, as due to Realm's auto-update, it will be the latest
                // value.
                subscriber.onNext(results);
            }
        });
    }

    @Override
    public Observable<RealmResults<DynamicRealmObject>> from(DynamicRealm realm, final RealmResults<DynamicRealmObject> results) {
        final RealmConfiguration realmConfig = realm.getConfiguration();
        return Observable.create(new Observable.OnSubscribe<RealmResults<DynamicRealmObject>>() {
            @Override
            public void call(final Subscriber<? super RealmResults<DynamicRealmObject>> subscriber) {
                // Get instance to make sure that the Realm is open for as long as the
                // Observable is subscribed to it.
                final DynamicRealm observableRealm = DynamicRealm.getInstance(realmConfig);
                resultsRefs.get().acquireReference(results);

                final RealmChangeListener<RealmResults<DynamicRealmObject>> listener = new RealmChangeListener<RealmResults<DynamicRealmObject>>() {
                    @Override
                    public void onChange(RealmResults<DynamicRealmObject> result) {
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
                        observableRealm.close();
                        resultsRefs.get().releaseReference(results);
                    }
                }));

                // Immediately call onNext with the current value, as due to Realm's auto-update, it will be the latest
                // value.
                subscriber.onNext(results);
            }
        });
    }

    @Override
    public <E extends RealmModel> Observable<RealmList<E>> from(Realm realm, RealmList<E> list) {
        return getRealmListObservable();
    }

    @Override
    public Observable<RealmList<DynamicRealmObject>> from(DynamicRealm realm, RealmList<DynamicRealmObject> list) {
        return getRealmListObservable();
    }

    private <E extends RealmModel> Observable<RealmList<E>> getRealmListObservable() {
        throw new RuntimeException("RealmList does not support change listeners yet, so cannot create an Observable");
    }

    @Override
    public <E extends RealmModel> Observable<E> from(final Realm realm, final E object) {
        final RealmConfiguration realmConfig = realm.getConfiguration();
        return Observable.create(new Observable.OnSubscribe<E>() {
            @Override
            public void call(final Subscriber<? super E> subscriber) {
                // Get instance to make sure that the Realm is open for as long as the
                // Observable is subscribed to it.
                final Realm observableRealm = Realm.getInstance(realmConfig);
                objectRefs.get().acquireReference(object);

                final RealmChangeListener<E> listener = new RealmChangeListener<E>() {
                    @Override
                    public void onChange(E object) {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(object);
                        }
                    }
                };
                RealmObject.addChangeListener(object, listener);
                subscriber.add(Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        RealmObject.removeChangeListener(object, listener);
                        observableRealm.close();
                        objectRefs.get().releaseReference(object);
                    }
                }));

                // Immediately call onNext with the current value, as due to Realm's auto-update, it will be the latest
                // value.
                subscriber.onNext(object);
            }
        });
    }

    @Override
    public Observable<DynamicRealmObject> from(DynamicRealm realm, final DynamicRealmObject object) {
        final RealmConfiguration realmConfig = realm.getConfiguration();
        return Observable.create(new Observable.OnSubscribe<DynamicRealmObject>() {
            @Override
            public void call(final Subscriber<? super DynamicRealmObject> subscriber) {
                // Get instance to make sure that the Realm is open for as long as the
                // Observable is subscribed to it.
                final DynamicRealm observableRealm = DynamicRealm.getInstance(realmConfig);
                objectRefs.get().acquireReference(object);

                final RealmChangeListener<DynamicRealmObject> listener = new RealmChangeListener<DynamicRealmObject>() {
                    @Override
                    public void onChange(DynamicRealmObject object) {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(object);
                        }
                    }
                };
                RealmObject.addChangeListener(object, listener);
                subscriber.add(Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        RealmObject.removeChangeListener(object, listener);
                        observableRealm.close();
                        objectRefs.get().releaseReference(object);
                    }
                }));

                // Immediately call onNext with the current value, as due to Realm's auto-update, it will be the latest
                // value.
                subscriber.onNext(object);
            }
        });
    }

    @Override
    public <E extends RealmModel> Observable<RealmQuery<E>> from(Realm realm, RealmQuery<E> query) {
        throw new RuntimeException("RealmQuery not supported yet.");
    }

    @Override
    public Observable<RealmQuery<DynamicRealmObject>> from(DynamicRealm realm, RealmQuery<DynamicRealmObject> query) {
        throw new RuntimeException("RealmQuery not supported yet.");
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof RealmObservableFactory;
    }

    @Override
    public int hashCode() {
        return 37;
    }


    // Helper class for keeping track of strong references to objects.
    private static class StrongReferenceCounter<K> {

        private final Map<K, Integer> references = new IdentityHashMap<K, Integer>();

        public void acquireReference(K object) {
            Integer count = references.get(object);
            if (count == null) {
                references.put(object, 1);
            } else {
                references.put(object, count + 1);
            }
        }

        public void releaseReference(K object) {
            Integer count = references.get(object);
            if (count == null) {
                throw new IllegalStateException("Object does not have any references: " + object);
            } else if (count > 1) {
                references.put(object, count - 1);
            } else if (count == 1) {
                references.remove(object);
            } else {
                throw new IllegalStateException("Invalid reference count: " + count);
            }
        }
    }
}
