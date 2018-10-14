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

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.disposables.Disposables;
import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.ObjectChangeSet;
import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.RealmObject;
import io.realm.RealmObjectChangeListener;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Factory class for creating Observables for RxJava (&lt;=2.0.*).
 *
 * @see Realm#asFlowable() ()
 * @see RealmObject#asFlowable()
 * @see RealmResults#asFlowable()
 * @see DynamicRealm#asFlowable()
 * @see DynamicRealmObject#asFlowable()
 */
public class RealmObservableFactory implements RxObservableFactory {

    // Maps for storing strong references to Realm classes while they are subscribed to.
    // This is needed if users create Observables without manually maintaining a reference to them.
    // In that case RealmObjects/RealmResults/RealmLists might be GC'ed too early.
    private ThreadLocal<StrongReferenceCounter<RealmResults>> resultsRefs = new ThreadLocal<StrongReferenceCounter<RealmResults>>() {
        @Override
        protected StrongReferenceCounter<RealmResults> initialValue() {
            return new StrongReferenceCounter<>();
        }
    };
    private ThreadLocal<StrongReferenceCounter<RealmList>> listRefs = new ThreadLocal<StrongReferenceCounter<RealmList>>() {
        @Override
        protected StrongReferenceCounter<RealmList> initialValue() {
            return new StrongReferenceCounter<>();
        }
    };
    private ThreadLocal<StrongReferenceCounter<RealmModel>> objectRefs = new ThreadLocal<StrongReferenceCounter<RealmModel>>() {
        @Override
        protected StrongReferenceCounter<RealmModel> initialValue() {
            return new StrongReferenceCounter<>();
        }
    };

    private static final BackpressureStrategy BACK_PRESSURE_STRATEGY = BackpressureStrategy.LATEST;

    @Override
    public Flowable<Realm> from(Realm realm) {
        final RealmConfiguration realmConfig = realm.getConfiguration();
        return Flowable.create(new FlowableOnSubscribe <Realm>() {
            @Override
            public void subscribe(final FlowableEmitter<Realm> emitter) throws Exception {
                // Instance is cached by Realm, so no need to keep strong reference
                final Realm observableRealm = Realm.getInstance(realmConfig);
                final RealmChangeListener<Realm> listener = new RealmChangeListener<Realm>() {
                    @Override
                    public void onChange(Realm realm) {
                        if (!emitter.isCancelled()) {
                            emitter.onNext(realm);
                        }
                    }
                };
                observableRealm.addChangeListener(listener);

                // Cleanup when stream is disposed
                emitter.setDisposable(Disposables.fromRunnable(new Runnable() {
                    @Override
                    public void run() {
                        observableRealm.removeChangeListener(listener);
                        observableRealm.close();
                    }
                }));

                // Emit current value immediately
                emitter.onNext(observableRealm);
            }
        }, BACK_PRESSURE_STRATEGY);
    }

    @Override
    public Flowable<DynamicRealm> from(DynamicRealm realm) {
        final RealmConfiguration realmConfig = realm.getConfiguration();
        return Flowable.create(new FlowableOnSubscribe<DynamicRealm>() {
            @Override
            public void subscribe(final FlowableEmitter<DynamicRealm> emitter) throws Exception {
                // Instance is cached by Realm, so no need to keep strong reference
                final DynamicRealm observableRealm = DynamicRealm.getInstance(realmConfig);
                final RealmChangeListener<DynamicRealm> listener = new RealmChangeListener<DynamicRealm>() {
                    @Override
                    public void onChange(DynamicRealm realm) {
                        if (!emitter.isCancelled()) {
                            emitter.onNext(realm);
                        }
                    }
                };
                observableRealm.addChangeListener(listener);

                // Cleanup when stream is disposed
                emitter.setDisposable(Disposables.fromRunnable(new Runnable() {
                    @Override
                    public void run() {
                        observableRealm.removeChangeListener(listener);
                        observableRealm.close();
                    }
                }));

                // Emit current value immediately
                emitter.onNext(observableRealm);
            }
        }, BACK_PRESSURE_STRATEGY);
    }

    @Override
    public <E> Flowable<RealmResults<E>> from(final Realm realm, final RealmResults<E> results) {
        final RealmConfiguration realmConfig = realm.getConfiguration();
        return Flowable.create(new FlowableOnSubscribe<RealmResults<E>>() {
            @Override
            public void subscribe(final FlowableEmitter<RealmResults<E>> emitter) throws Exception {
                // Gets instance to make sure that the Realm is open for as long as the
                // Observable is subscribed to it.
                final Realm observableRealm = Realm.getInstance(realmConfig);
                resultsRefs.get().acquireReference(results);
                final RealmChangeListener<RealmResults<E>> listener = new RealmChangeListener<RealmResults<E>>() {
                    @Override
                    public void onChange(RealmResults<E> results) {
                        if (!emitter.isCancelled()) {
                            emitter.onNext(results);
                        }
                    }
                };
                results.addChangeListener(listener);

                // Cleanup when stream is disposed
                emitter.setDisposable(Disposables.fromRunnable(new Runnable() {
                    @Override
                    public void run() {
                        results.removeChangeListener(listener);
                        observableRealm.close();
                        resultsRefs.get().releaseReference(results);
                    }
                }));

                // Emit current value immediately
                emitter.onNext(results);

            }
        }, BACK_PRESSURE_STRATEGY);
    }

    @Override
    public <E> Observable<CollectionChange<RealmResults<E>>> changesetsFrom(Realm realm, final RealmResults<E> results) {
        final RealmConfiguration realmConfig = realm.getConfiguration();
        return Observable.create(new ObservableOnSubscribe<CollectionChange<RealmResults<E>>>() {
            @Override
            public void subscribe(final ObservableEmitter<CollectionChange<RealmResults<E>>> emitter) throws Exception {
                // Gets instance to make sure that the Realm is open for as long as the
                // Observable is subscribed to it.
                final Realm observableRealm = Realm.getInstance(realmConfig);
                resultsRefs.get().acquireReference(results);
                final OrderedRealmCollectionChangeListener<RealmResults<E>> listener = new OrderedRealmCollectionChangeListener<RealmResults<E>>() {
                    @Override
                    public void onChange(RealmResults<E> e, OrderedCollectionChangeSet changeSet) {
                        if (!emitter.isDisposed()) {
                            emitter.onNext(new CollectionChange<RealmResults<E>>(results, changeSet));
                        }
                    }
                };
                results.addChangeListener(listener);

                // Cleanup when stream is disposed
                emitter.setDisposable(Disposables.fromRunnable(new Runnable() {
                    @Override
                    public void run() {
                        results.removeChangeListener(listener);
                        observableRealm.close();
                        resultsRefs.get().releaseReference(results);
                    }
                }));

                // Emit current value immediately
                emitter.onNext(new CollectionChange<>(results, null));
            }
        });
    }

    @Override
    public <E> Flowable<RealmResults<E>> from(DynamicRealm realm, final RealmResults<E> results) {
        final RealmConfiguration realmConfig = realm.getConfiguration();
        return Flowable.create(new FlowableOnSubscribe<RealmResults<E>>() {
            @Override
            public void subscribe(final FlowableEmitter<RealmResults<E>> emitter) throws Exception {
                // Gets instance to make sure that the Realm is open for as long as the
                // Observable is subscribed to it.
                final DynamicRealm observableRealm = DynamicRealm.getInstance(realmConfig);
                resultsRefs.get().acquireReference(results);
                final RealmChangeListener<RealmResults<E>> listener = new RealmChangeListener<RealmResults<E>>() {
                    @Override
                    public void onChange(RealmResults<E> results) {
                        if (!emitter.isCancelled()) {
                            emitter.onNext(results);
                        }
                    }
                };
                results.addChangeListener(listener);

                // Cleanup when stream is disposed
                emitter.setDisposable(Disposables.fromRunnable(new Runnable() {
                    @Override
                    public void run() {
                        results.removeChangeListener(listener);
                        observableRealm.close();
                        resultsRefs.get().releaseReference(results);
                    }
                }));

                // Emit current value immediately
                emitter.onNext(results);

            }
        }, BACK_PRESSURE_STRATEGY);
    }

    @Override
    public <E> Observable<CollectionChange<RealmResults<E>>> changesetsFrom(DynamicRealm realm, final RealmResults<E> results) {
        final RealmConfiguration realmConfig = realm.getConfiguration();
        return Observable.create(new ObservableOnSubscribe<CollectionChange<RealmResults<E>>>() {
            @Override
            public void subscribe(final ObservableEmitter<CollectionChange<RealmResults<E>>> emitter) throws Exception {
                // Gets instance to make sure that the Realm is open for as long as the
                // Observable is subscribed to it.
                final DynamicRealm observableRealm = DynamicRealm.getInstance(realmConfig);
                resultsRefs.get().acquireReference(results);
                final OrderedRealmCollectionChangeListener<RealmResults<E>> listener = new OrderedRealmCollectionChangeListener<RealmResults<E>>() {
                    @Override
                    public void onChange(RealmResults<E> results, OrderedCollectionChangeSet changeSet) {
                        if (!emitter.isDisposed()) {
                            emitter.onNext(new CollectionChange<>(results, changeSet));
                        }
                    }
                };
                results.addChangeListener(listener);

                // Cleanup when stream is disposed
                emitter.setDisposable(Disposables.fromRunnable(new Runnable() {
                    @Override
                    public void run() {
                        results.removeChangeListener(listener);
                        observableRealm.close();
                        resultsRefs.get().releaseReference(results);
                    }
                }));

                // Emit current value immediately
                emitter.onNext(new CollectionChange<>(results, null));
            }
        });
    }

    @Override
    public <E> Flowable<RealmList<E>> from(Realm realm, final RealmList<E> list) {
        final RealmConfiguration realmConfig = realm.getConfiguration();
        return Flowable.create(new FlowableOnSubscribe<RealmList<E>>() {
            @Override
            public void subscribe(final FlowableEmitter<RealmList<E>> emitter) throws Exception {
                // Gets instance to make sure that the Realm is open for as long as the
                // Observable is subscribed to it.
                final Realm observableRealm = Realm.getInstance(realmConfig);
                listRefs.get().acquireReference(list);
                final RealmChangeListener<RealmList<E>> listener = new RealmChangeListener<RealmList<E>>() {
                    @Override
                    public void onChange(RealmList<E> results) {
                        if (!emitter.isCancelled()) {
                            emitter.onNext(list);
                        }
                    }
                };
                list.addChangeListener(listener);

                // Cleanup when stream is disposed
                emitter.setDisposable(Disposables.fromRunnable(new Runnable() {
                    @Override
                    public void run() {
                        list.removeChangeListener(listener);
                        observableRealm.close();
                        listRefs.get().releaseReference(list);
                    }
                }));

                // Emit current value immediately
                emitter.onNext(list);

            }
        }, BACK_PRESSURE_STRATEGY);
    }

    @Override
    public <E> Observable<CollectionChange<RealmList<E>>> changesetsFrom(Realm realm, final RealmList<E> list) {
        final RealmConfiguration realmConfig = realm.getConfiguration();
        return Observable.create(new ObservableOnSubscribe<CollectionChange<RealmList<E>>>() {
            @Override
            public void subscribe(final ObservableEmitter<CollectionChange<RealmList<E>>> emitter) throws Exception {
                // Gets instance to make sure that the Realm is open for as long as the
                // Observable is subscribed to it.
                final Realm observableRealm = Realm.getInstance(realmConfig);
                listRefs.get().acquireReference(list);
                final OrderedRealmCollectionChangeListener<RealmList<E>> listener = new OrderedRealmCollectionChangeListener<RealmList<E>>() {
                    @Override
                    public void onChange(RealmList<E> results, OrderedCollectionChangeSet changeSet) {
                        if (!emitter.isDisposed()) {
                            emitter.onNext(new CollectionChange<>(results, changeSet));
                        }
                    }
                };
                list.addChangeListener(listener);

                // Cleanup when stream is disposed
                emitter.setDisposable(Disposables.fromRunnable(new Runnable() {
                    @Override
                    public void run() {
                        list.removeChangeListener(listener);
                        observableRealm.close();
                        listRefs.get().releaseReference(list);
                    }
                }));

                // Emit current value immediately
                emitter.onNext(new CollectionChange<>(list, null));
            }
        });
    }

    @Override
    public <E> Flowable<RealmList<E>> from(DynamicRealm realm, final RealmList<E> list) {
        final RealmConfiguration realmConfig = realm.getConfiguration();
        return Flowable.create(new FlowableOnSubscribe<RealmList<E>>() {
            @Override
            public void subscribe(final FlowableEmitter<RealmList<E>> emitter) throws Exception {
                // Gets instance to make sure that the Realm is open for as long as the
                // Observable is subscribed to it.
                final DynamicRealm observableRealm = DynamicRealm.getInstance(realmConfig);
                listRefs.get().acquireReference(list);
                final RealmChangeListener<RealmList<E>> listener = new RealmChangeListener<RealmList<E>>() {
                    @Override
                    public void onChange(RealmList<E> results) {
                        if (!emitter.isCancelled()) {
                            emitter.onNext(list);
                        }
                    }
                };
                list.addChangeListener(listener);

                // Cleanup when stream is disposed
                emitter.setDisposable(Disposables.fromRunnable(new Runnable() {
                    @Override
                    public void run() {
                        list.removeChangeListener(listener);
                        observableRealm.close();
                        listRefs.get().releaseReference(list);
                    }
                }));

                // Emit current value immediately
                emitter.onNext(list);

            }
        }, BACK_PRESSURE_STRATEGY);
    }

    @Override
    public <E> Observable<CollectionChange<RealmList<E>>> changesetsFrom(DynamicRealm realm, final RealmList<E> list) {
        final RealmConfiguration realmConfig = realm.getConfiguration();
        return Observable.create(new ObservableOnSubscribe<CollectionChange<RealmList<E>>>() {
            @Override
            public void subscribe(final ObservableEmitter<CollectionChange<RealmList<E>>> emitter) throws Exception {
                // Gets instance to make sure that the Realm is open for as long as the
                // Observable is subscribed to it.
                final DynamicRealm observableRealm = DynamicRealm.getInstance(realmConfig);
                listRefs.get().acquireReference(list);
                final OrderedRealmCollectionChangeListener<RealmList<E>> listener = new OrderedRealmCollectionChangeListener<RealmList<E>>() {
                    @Override
                    public void onChange(RealmList<E> results, OrderedCollectionChangeSet changeSet) {
                        if (!emitter.isDisposed()) {
                            emitter.onNext(new CollectionChange<>(results, changeSet));
                        }
                    }
                };
                list.addChangeListener(listener);

                // Cleanup when stream is disposed
                emitter.setDisposable(Disposables.fromRunnable(new Runnable() {
                    @Override
                    public void run() {
                        list.removeChangeListener(listener);
                        observableRealm.close();
                        listRefs.get().releaseReference(list);
                    }
                }));

                // Emit current value immediately
                emitter.onNext(new CollectionChange<>(list, null));
            }
        });
    }

    @Override
    public <E extends RealmModel> Flowable<E> from(final Realm realm, final E object) {
        final RealmConfiguration realmConfig = realm.getConfiguration();
        return Flowable.create(new FlowableOnSubscribe<E>() {
            @Override
            public void subscribe(final FlowableEmitter<E> emitter) throws Exception {
                // Gets instance to make sure that the Realm is open for as long as the
                // Observable is subscribed to it.
                final Realm observableRealm = Realm.getInstance(realmConfig);
                objectRefs.get().acquireReference(object);
                final RealmChangeListener<E> listener = new RealmChangeListener<E>() {
                    @Override
                    public void onChange(E obj) {
                        if (!emitter.isCancelled()) {
                            emitter.onNext(obj);
                        }
                    }
                };
                RealmObject.addChangeListener(object, listener);

                // Cleanup when stream is disposed
                emitter.setDisposable(Disposables.fromRunnable(new Runnable() {
                    @Override
                    public void run() {
                        RealmObject.removeChangeListener(object, listener);
                        observableRealm.close();
                        objectRefs.get().releaseReference(object);
                    }
                }));

                // Emit current value immediately
                emitter.onNext(object);

            }
        }, BACK_PRESSURE_STRATEGY);
    }

    @Override
    public <E extends RealmModel> Observable<ObjectChange<E>> changesetsFrom(Realm realm, final E object) {
        final RealmConfiguration realmConfig = realm.getConfiguration();
        return Observable.create(new ObservableOnSubscribe<ObjectChange<E>>() {
            @Override
            public void subscribe(final ObservableEmitter<ObjectChange<E>> emitter) throws Exception {
                // Gets instance to make sure that the Realm is open for as long as the
                // Observable is subscribed to it.
                final Realm observableRealm = Realm.getInstance(realmConfig);
                objectRefs.get().acquireReference(object);
                final RealmObjectChangeListener<E> listener = new RealmObjectChangeListener<E>() {
                    @Override
                    public void onChange(E obj, ObjectChangeSet changeSet) {
                        if (!emitter.isDisposed()) {
                            emitter.onNext(new ObjectChange<>(obj, changeSet));
                        }
                    }
                };
                RealmObject.addChangeListener(object, listener);

                // Cleanup when stream is disposed
                emitter.setDisposable(Disposables.fromRunnable(new Runnable() {
                    @Override
                    public void run() {
                        RealmObject.removeChangeListener(object, listener);
                        observableRealm.close();
                        objectRefs.get().releaseReference(object);
                    }
                }));

                // Emit current value immediately
                emitter.onNext(new ObjectChange<>(object, null));
            }
        });
    }

    @Override
    public Flowable<DynamicRealmObject> from(DynamicRealm realm, final DynamicRealmObject object) {
        final RealmConfiguration realmConfig = realm.getConfiguration();
        return Flowable.create(new FlowableOnSubscribe<DynamicRealmObject>() {
            @Override
            public void subscribe(final FlowableEmitter<DynamicRealmObject> emitter) throws Exception {
                // Gets instance to make sure that the Realm is open for as long as the
                // Observable is subscribed to it.
                final DynamicRealm observableRealm = DynamicRealm.getInstance(realmConfig);
                objectRefs.get().acquireReference(object);
                final RealmChangeListener<DynamicRealmObject> listener = new RealmChangeListener<DynamicRealmObject>() {
                    @Override
                    public void onChange(DynamicRealmObject obj) {
                        if (!emitter.isCancelled()) {
                            emitter.onNext(obj);
                        }
                    }
                };
                RealmObject.addChangeListener(object, listener);

                // Cleanup when stream is disposed
                emitter.setDisposable(Disposables.fromRunnable(new Runnable() {
                    @Override
                    public void run() {
                        RealmObject.removeChangeListener(object, listener);
                        observableRealm.close();
                        objectRefs.get().releaseReference(object);
                    }
                }));

                // Emit current value immediately
                emitter.onNext(object);

            }
        }, BACK_PRESSURE_STRATEGY);
    }

    @Override
    public Observable<ObjectChange<DynamicRealmObject>> changesetsFrom(DynamicRealm realm, final DynamicRealmObject object) {
        final RealmConfiguration realmConfig = realm.getConfiguration();
        return Observable.create(new ObservableOnSubscribe<ObjectChange<DynamicRealmObject>>() {
            @Override
            public void subscribe(final ObservableEmitter<ObjectChange<DynamicRealmObject>> emitter) throws Exception {
                // Gets instance to make sure that the Realm is open for as long as the
                // Observable is subscribed to it.
                final DynamicRealm observableRealm = DynamicRealm.getInstance(realmConfig);
                objectRefs.get().acquireReference(object);
                final RealmObjectChangeListener<DynamicRealmObject> listener = new RealmObjectChangeListener<DynamicRealmObject>() {
                    @Override
                    public void onChange(DynamicRealmObject obj, ObjectChangeSet changeSet) {
                        if (!emitter.isDisposed()) {
                            emitter.onNext(new ObjectChange<>(obj, changeSet));
                        }
                    }
                };
                object.addChangeListener(listener);

                // Cleanup when stream is disposed
                emitter.setDisposable(Disposables.fromRunnable(new Runnable() {
                    @Override
                    public void run() {
                        object.removeChangeListener(listener);
                        observableRealm.close();
                        objectRefs.get().releaseReference(object);
                    }
                }));

                // Emit current value immediately
                emitter.onNext(new ObjectChange<>(object, null));
            }
        });
    }

    @Override
    public <E> Single<RealmQuery<E>> from(Realm realm, RealmQuery<E> query) {
        throw new RuntimeException("RealmQuery not supported yet.");
    }

    @Override
    public <E> Single<RealmQuery<E>> from(DynamicRealm realm, RealmQuery<E> query) {
        throw new RuntimeException("RealmQuery not supported yet.");
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof RealmObservableFactory;
    }

    @Override
    public int hashCode() {
        return 37; // Random number
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
