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

import android.os.Looper;

import java.util.IdentityHashMap;
import java.util.Map;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
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

    private final boolean returnFrozenObjects;

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
        if (realm.isFrozen()) {
            return Flowable.just(realm);
        }
        final RealmConfiguration realmConfig = realm.getConfiguration();
        Scheduler scheduler = getScheduler();
        return Flowable.create(new FlowableOnSubscribe <Realm>() {
            @Override
            public void subscribe(final FlowableEmitter<Realm> emitter) throws Exception {
                // Instance is cached by Realm, so no need to keep strong reference
                final Realm observableRealm = Realm.getInstance(realmConfig);
                final RealmChangeListener<Realm> listener = new RealmChangeListener<Realm>() {
                    @Override
                    public void onChange(Realm realm) {
                        if (!emitter.isCancelled()) {
                            emitter.onNext(returnFrozenObjects ? realm.freeze() : realm);
                        }
                    }
                };
                observableRealm.addChangeListener(listener);

                // Cleanup when stream is disposed
                emitter.setDisposable(Disposables.fromRunnable(new Runnable() {
                    @Override
                    public void run() {
                        if (!observableRealm.isClosed()) {
                            observableRealm.removeChangeListener(listener);
                            observableRealm.close();
                        }
                    }
                }));

                // Emit current value immediately
                emitter.onNext(returnFrozenObjects ? observableRealm.freeze() : observableRealm);
            }
        }, BACK_PRESSURE_STRATEGY).subscribeOn(scheduler).unsubscribeOn(scheduler);
    }

    /**
     * Constructs the factory for creating Realm observables for RxJava.
     *
     * @param emitFrozenObjects {@code true} if all objects should be frozen before being returned
 *                              to the user. {@code false} if they should be live objects.
     */
    public RealmObservableFactory(boolean emitFrozenObjects) {
        this.returnFrozenObjects = emitFrozenObjects;
    }

    @Override
    public Flowable<DynamicRealm> from(DynamicRealm realm) {
        if (realm.isFrozen()) {
            return Flowable.just(realm);
        }
        final RealmConfiguration realmConfig = realm.getConfiguration();
        Scheduler scheduler = getScheduler();
        return Flowable.create(new FlowableOnSubscribe<DynamicRealm>() {
            @Override
            public void subscribe(final FlowableEmitter<DynamicRealm> emitter) throws Exception {
                // Instance is cached by Realm, so no need to keep strong reference
                final DynamicRealm observableRealm = DynamicRealm.getInstance(realmConfig);
                final RealmChangeListener<DynamicRealm> listener = new RealmChangeListener<DynamicRealm>() {
                    @Override
                    public void onChange(DynamicRealm realm) {
                        if (!emitter.isCancelled()) {
                            emitter.onNext(returnFrozenObjects ? realm.freeze() : realm);
                        }
                    }
                };
                observableRealm.addChangeListener(listener);

                // Cleanup when stream is disposed
                emitter.setDisposable(Disposables.fromRunnable(new Runnable() {
                    @Override
                    public void run() {
                        if (!observableRealm.isClosed()) {
                            observableRealm.removeChangeListener(listener);
                            observableRealm.close();
                        }
                    }
                }));

                // Emit current value immediately
                emitter.onNext(returnFrozenObjects ? observableRealm.freeze() : observableRealm);
            }
        }, BACK_PRESSURE_STRATEGY).subscribeOn(scheduler).unsubscribeOn(scheduler);
    }

    @Override
    public <E> Flowable<RealmResults<E>> from(final Realm realm, final RealmResults<E> results) {
        if (realm.isFrozen()) {
            return Flowable.just(results);
        }
        final RealmConfiguration realmConfig = realm.getConfiguration();
        Scheduler scheduler = getScheduler();
        return Flowable.create(new FlowableOnSubscribe<RealmResults<E>>() {
            @Override
            public void subscribe(final FlowableEmitter<RealmResults<E>> emitter) {
                // If the Realm has been closed, just create an empty Observable because we assume it is going to be disposed shortly.
                if (!results.isValid()) return;

                // Gets instance to make sure that the Realm is open for as long as the
                // Observable is subscribed to it.
                final Realm observableRealm = Realm.getInstance(realmConfig);
                resultsRefs.get().acquireReference(results);
                final RealmChangeListener<RealmResults<E>> listener = new RealmChangeListener<RealmResults<E>>() {
                    @Override
                    public void onChange(RealmResults<E> results) {
                        if (!emitter.isCancelled()) {
                            emitter.onNext(returnFrozenObjects ? results.freeze() : results);
                        }
                    }
                };
                results.addChangeListener(listener);

                // Cleanup when stream is disposed
                emitter.setDisposable(Disposables.fromRunnable(new Runnable() {
                    @Override
                    public void run() {
                        if (!observableRealm.isClosed()) {
                            results.removeChangeListener(listener);
                            observableRealm.close();
                        }
                        resultsRefs.get().releaseReference(results);
                    }
                }));

                // Emit current value immediately
                emitter.onNext(returnFrozenObjects ? results.freeze() : results);

            }
        }, BACK_PRESSURE_STRATEGY).subscribeOn(scheduler).unsubscribeOn(scheduler);
    }

    private Scheduler getScheduler() {
        Looper looper = Looper.myLooper();
        if (looper == null) {
            throw new IllegalStateException("No looper found");
        }
        return AndroidSchedulers.from(looper);
    }

    @Override
    public <E> Observable<CollectionChange<RealmResults<E>>> changesetsFrom(Realm realm, final RealmResults<E> results) {
        if (realm.isFrozen()) {
            return Observable.just(new CollectionChange<RealmResults<E>>(results, null));
        }
        final RealmConfiguration realmConfig = realm.getConfiguration();
        Scheduler scheduler = getScheduler();
        return Observable.create(new ObservableOnSubscribe<CollectionChange<RealmResults<E>>>() {
            @Override
            public void subscribe(final ObservableEmitter<CollectionChange<RealmResults<E>>> emitter) {
                // If the Realm has been closed, just create an empty Observable because we assume it is going to be disposed shortly.
                if (!results.isValid()) return;

                // Gets instance to make sure that the Realm is open for as long as the
                // Observable is subscribed to it.
                final Realm observableRealm = Realm.getInstance(realmConfig);
                resultsRefs.get().acquireReference(results);
                final OrderedRealmCollectionChangeListener<RealmResults<E>> listener = new OrderedRealmCollectionChangeListener<RealmResults<E>>() {
                    @Override
                    public void onChange(RealmResults<E> e, OrderedCollectionChangeSet changeSet) {
                        if (!emitter.isDisposed()) {
                            emitter.onNext(new CollectionChange<RealmResults<E>>(returnFrozenObjects ? results.freeze() : results, changeSet));
                        }
                    }
                };
                results.addChangeListener(listener);

                // Cleanup when stream is disposed
                emitter.setDisposable(Disposables.fromRunnable(new Runnable() {
                    @Override
                    public void run() {
                        if (!observableRealm.isClosed()) {
                            results.removeChangeListener(listener);
                            observableRealm.close();
                        }
                        resultsRefs.get().releaseReference(results);
                    }
                }));

                // Emit current value immediately
                emitter.onNext(new CollectionChange<>(returnFrozenObjects ? results.freeze() : results, null));
            }
        }).subscribeOn(scheduler).unsubscribeOn(scheduler);
    }

    @Override
    public <E> Flowable<RealmResults<E>> from(DynamicRealm realm, final RealmResults<E> results) {
        if (realm.isFrozen()) {
            return Flowable.just(results);
        }
        final RealmConfiguration realmConfig = realm.getConfiguration();
        Scheduler scheduler = getScheduler();
        return Flowable.create(new FlowableOnSubscribe<RealmResults<E>>() {
            @Override
            public void subscribe(final FlowableEmitter<RealmResults<E>> emitter) {
                // If the Realm has been closed, just create an empty Observable because we assume it is going to be disposed shortly.
                if (!results.isValid()) return;

                // Gets instance to make sure that the Realm is open for as long as the
                // Observable is subscribed to it.
                final DynamicRealm observableRealm = DynamicRealm.getInstance(realmConfig);
                resultsRefs.get().acquireReference(results);
                final RealmChangeListener<RealmResults<E>> listener = new RealmChangeListener<RealmResults<E>>() {
                    @Override
                    public void onChange(RealmResults<E> results) {
                        if (!emitter.isCancelled()) {
                            emitter.onNext(returnFrozenObjects ? results.freeze() : results);
                        }
                    }
                };
                results.addChangeListener(listener);

                // Cleanup when stream is disposed
                emitter.setDisposable(Disposables.fromRunnable(new Runnable() {
                    @Override
                    public void run() {
                        if (!observableRealm.isClosed()) {
                            results.removeChangeListener(listener);
                            observableRealm.close();
                        }
                        resultsRefs.get().releaseReference(results);
                    }
                }));

                // Emit current value immediately
                emitter.onNext(returnFrozenObjects ? results.freeze() : results);

            }
        }, BACK_PRESSURE_STRATEGY).subscribeOn(scheduler).unsubscribeOn(scheduler);
    }

    @Override
    public <E> Observable<CollectionChange<RealmResults<E>>> changesetsFrom(DynamicRealm realm, final RealmResults<E> results) {
        if (realm.isFrozen()) {
            return Observable.just(new CollectionChange<RealmResults<E>>(results, null));
        }
        final RealmConfiguration realmConfig = realm.getConfiguration();
        Scheduler scheduler = getScheduler();
        return Observable.create(new ObservableOnSubscribe<CollectionChange<RealmResults<E>>>() {
            @Override
            public void subscribe(final ObservableEmitter<CollectionChange<RealmResults<E>>> emitter) {
                // If the Realm has been closed, just create an empty Observable because we assume it is going to be disposed shortly.
                if (!results.isValid()) return;

                // Gets instance to make sure that the Realm is open for as long as the
                // Observable is subscribed to it.
                final DynamicRealm observableRealm = DynamicRealm.getInstance(realmConfig);
                resultsRefs.get().acquireReference(results);
                final OrderedRealmCollectionChangeListener<RealmResults<E>> listener = new OrderedRealmCollectionChangeListener<RealmResults<E>>() {
                    @Override
                    public void onChange(RealmResults<E> results, OrderedCollectionChangeSet changeSet) {
                        if (!emitter.isDisposed()) {
                            emitter.onNext(new CollectionChange<>(returnFrozenObjects ? results.freeze() : results, changeSet));
                        }
                    }
                };
                results.addChangeListener(listener);

                // Cleanup when stream is disposed
                emitter.setDisposable(Disposables.fromRunnable(new Runnable() {
                    @Override
                    public void run() {
                        if (!observableRealm.isClosed()) {
                            results.removeChangeListener(listener);
                            observableRealm.close();
                        }
                        resultsRefs.get().releaseReference(results);
                    }
                }));

                // Emit current value immediately
                emitter.onNext(new CollectionChange<>(returnFrozenObjects ? results.freeze() : results, null));
            }
        }).subscribeOn(scheduler).unsubscribeOn(scheduler);
    }

    @Override
    public <E> Flowable<RealmList<E>> from(Realm realm, final RealmList<E> list) {
        if (realm.isFrozen()) {
            return Flowable.just(list);
        }
        final RealmConfiguration realmConfig = realm.getConfiguration();
        Scheduler scheduler = getScheduler();
        return Flowable.create(new FlowableOnSubscribe<RealmList<E>>() {
            @Override
            public void subscribe(final FlowableEmitter<RealmList<E>> emitter) {
                // If the Realm has been closed, just create an empty Observable because we assume it is going to be disposed shortly.
                if (!list.isValid()) return;

                // Gets instance to make sure that the Realm is open for as long as the
                // Observable is subscribed to it.
                final Realm observableRealm = Realm.getInstance(realmConfig);
                listRefs.get().acquireReference(list);
                final RealmChangeListener<RealmList<E>> listener = new RealmChangeListener<RealmList<E>>() {
                    @Override
                    public void onChange(RealmList<E> list) {
                        if (!emitter.isCancelled()) {
                            emitter.onNext(returnFrozenObjects ? list.freeze() : list);
                        }
                    }
                };
                list.addChangeListener(listener);

                // Cleanup when stream is disposed
                emitter.setDisposable(Disposables.fromRunnable(new Runnable() {
                    @Override
                    public void run() {
                        if (!observableRealm.isClosed()) {
                            list.removeChangeListener(listener);
                            observableRealm.close();
                        }
                        listRefs.get().releaseReference(list);
                    }
                }));

                // Emit current value immediately
                emitter.onNext(returnFrozenObjects ? list.freeze() : list);

            }
        }, BACK_PRESSURE_STRATEGY).subscribeOn(scheduler).unsubscribeOn(scheduler);
    }

    @Override
    public <E> Observable<CollectionChange<RealmList<E>>> changesetsFrom(Realm realm, final RealmList<E> list) {
        if (realm.isFrozen()) {
            return Observable.just(new CollectionChange<RealmList<E>>(list, null));
        }
        final RealmConfiguration realmConfig = realm.getConfiguration();
        Scheduler scheduler = getScheduler();
        return Observable.create(new ObservableOnSubscribe<CollectionChange<RealmList<E>>>() {
            @Override
            public void subscribe(final ObservableEmitter<CollectionChange<RealmList<E>>> emitter) {
                // If the Realm has been closed, just create an empty Observable because we assume it is going to be disposed shortly.
                if (!list.isValid()) return;

                // Gets instance to make sure that the Realm is open for as long as the
                // Observable is subscribed to it.
                final Realm observableRealm = Realm.getInstance(realmConfig);
                listRefs.get().acquireReference(list);
                final OrderedRealmCollectionChangeListener<RealmList<E>> listener = new OrderedRealmCollectionChangeListener<RealmList<E>>() {
                    @Override
                    public void onChange(RealmList<E> list, OrderedCollectionChangeSet changeSet) {
                        if (!emitter.isDisposed()) {
                            emitter.onNext(new CollectionChange<>(returnFrozenObjects ? list.freeze() : list, changeSet));
                        }
                    }
                };
                list.addChangeListener(listener);

                // Cleanup when stream is disposed
                emitter.setDisposable(Disposables.fromRunnable(new Runnable() {
                    @Override
                    public void run() {
                        if (!observableRealm.isClosed()) {
                            list.removeChangeListener(listener);
                            observableRealm.close();
                        }
                        listRefs.get().releaseReference(list);
                    }
                }));

                // Emit current value immediately
                emitter.onNext(new CollectionChange<>(returnFrozenObjects ? list.freeze() : list, null));
            }
        }).subscribeOn(scheduler).unsubscribeOn(scheduler);
    }

    @Override
    public <E> Flowable<RealmList<E>> from(DynamicRealm realm, final RealmList<E> list) {
        if (realm.isFrozen()) {
            return Flowable.just(list);
        }
        final RealmConfiguration realmConfig = realm.getConfiguration();
        Scheduler scheduler = getScheduler();
        return Flowable.create(new FlowableOnSubscribe<RealmList<E>>() {
            @Override
            public void subscribe(final FlowableEmitter<RealmList<E>> emitter) {
                // If the Realm has been closed, just create an empty Observable because we assume it is going to be disposed shortly.
                if (!list.isValid()) return;

                // Gets instance to make sure that the Realm is open for as long as the
                // Observable is subscribed to it.
                final DynamicRealm observableRealm = DynamicRealm.getInstance(realmConfig);
                listRefs.get().acquireReference(list);
                final RealmChangeListener<RealmList<E>> listener = new RealmChangeListener<RealmList<E>>() {
                    @Override
                    public void onChange(RealmList<E> list) {
                        if (!emitter.isCancelled()) {
                            emitter.onNext(returnFrozenObjects ? list.freeze() : list);
                        }
                    }
                };
                list.addChangeListener(listener);

                // Cleanup when stream is disposed
                emitter.setDisposable(Disposables.fromRunnable(new Runnable() {
                    @Override
                    public void run() {
                        if (!observableRealm.isClosed()) {
                            list.removeChangeListener(listener);
                            observableRealm.close();
                        }
                        listRefs.get().releaseReference(list);
                    }
                }));

                // Emit current value immediately
                emitter.onNext(returnFrozenObjects ? list.freeze() : list);

            }
        }, BACK_PRESSURE_STRATEGY).subscribeOn(scheduler).unsubscribeOn(scheduler);
    }

    @Override
    public <E> Observable<CollectionChange<RealmList<E>>> changesetsFrom(DynamicRealm realm, final RealmList<E> list) {
        if (realm.isFrozen()) {
            return Observable.just(new CollectionChange<RealmList<E>>(list, null));
        }
        final RealmConfiguration realmConfig = realm.getConfiguration();
        Scheduler scheduler = getScheduler();
        return Observable.create(new ObservableOnSubscribe<CollectionChange<RealmList<E>>>() {
            @Override
            public void subscribe(final ObservableEmitter<CollectionChange<RealmList<E>>> emitter) {
                // If the Realm has been closed, just create an empty Observable because we assume it is going to be disposed shortly.
                if (!list.isValid()) return;

                // Gets instance to make sure that the Realm is open for as long as the
                // Observable is subscribed to it.
                final DynamicRealm observableRealm = DynamicRealm.getInstance(realmConfig);
                listRefs.get().acquireReference(list);
                final OrderedRealmCollectionChangeListener<RealmList<E>> listener = new OrderedRealmCollectionChangeListener<RealmList<E>>() {
                    @Override
                    public void onChange(RealmList<E> list, OrderedCollectionChangeSet changeSet) {
                        if (!emitter.isDisposed()) {
                            emitter.onNext(new CollectionChange<>(returnFrozenObjects ? list.freeze() : list, changeSet));
                        }
                    }
                };
                list.addChangeListener(listener);

                // Cleanup when stream is disposed
                emitter.setDisposable(Disposables.fromRunnable(new Runnable() {
                    @Override
                    public void run() {
                        if (!observableRealm.isClosed()) {
                            list.removeChangeListener(listener);
                            observableRealm.close();
                        }
                        listRefs.get().releaseReference(list);
                    }
                }));

                // Emit current value immediately
                emitter.onNext(new CollectionChange<>(returnFrozenObjects ? list.freeze() : list, null));
            }
        }).subscribeOn(scheduler).unsubscribeOn(scheduler);
    }

    @Override
    public <E extends RealmModel> Flowable<E> from(final Realm realm, final E object) {
        if (realm.isFrozen()) {
            return Flowable.just(object);
        }
        final RealmConfiguration realmConfig = realm.getConfiguration();
        Scheduler scheduler = getScheduler();
        return Flowable.create(new FlowableOnSubscribe<E>() {
            @Override
            public void subscribe(final FlowableEmitter<E> emitter) {
                // If the Realm has been closed, just create an empty Observable because we assume it is going to be disposed shortly.
                if (!RealmObject.isValid(object)) return;

                // Gets instance to make sure that the Realm is open for as long as the
                // Observable is subscribed to it.
                final Realm observableRealm = Realm.getInstance(realmConfig);
                objectRefs.get().acquireReference(object);
                final RealmChangeListener<E> listener = new RealmChangeListener<E>() {
                    @Override
                    public void onChange(E obj) {
                        if (!emitter.isCancelled()) {
                            emitter.onNext(returnFrozenObjects ? RealmObject.freeze(obj) : obj);
                        }
                    }
                };
                RealmObject.addChangeListener(object, listener);

                // Cleanup when stream is disposed
                emitter.setDisposable(Disposables.fromRunnable(new Runnable() {
                    @Override
                    public void run() {
                        if (!observableRealm.isClosed()) {
                            RealmObject.removeChangeListener(object, listener);
                            observableRealm.close();
                        }
                        objectRefs.get().releaseReference(object);
                    }
                }));

                // Emit current value immediately
                emitter.onNext(returnFrozenObjects ? RealmObject.freeze(object) : object);

            }
        }, BACK_PRESSURE_STRATEGY).subscribeOn(scheduler).unsubscribeOn(scheduler);
    }

    @Override
    public <E extends RealmModel> Observable<ObjectChange<E>> changesetsFrom(Realm realm, final E object) {
        if (realm.isFrozen()) {
            return Observable.just(new ObjectChange<E>(object, null));
        }
        final RealmConfiguration realmConfig = realm.getConfiguration();
        Scheduler scheduler = getScheduler();
        return Observable.create(new ObservableOnSubscribe<ObjectChange<E>>() {
            @Override
            public void subscribe(final ObservableEmitter<ObjectChange<E>> emitter) {
                // If the Realm has been closed, just create an empty Observable because we assume it is going to be disposed shortly.
                if (!RealmObject.isValid(object)) return;

                // Gets instance to make sure that the Realm is open for as long as the
                // Observable is subscribed to it.
                final Realm observableRealm = Realm.getInstance(realmConfig);
                objectRefs.get().acquireReference(object);
                final RealmObjectChangeListener<E> listener = new RealmObjectChangeListener<E>() {
                    @Override
                    public void onChange(E obj, ObjectChangeSet changeSet) {
                        if (!emitter.isDisposed()) {
                            emitter.onNext(new ObjectChange<>(returnFrozenObjects ? RealmObject.freeze(obj) : obj, changeSet));
                        }
                    }
                };
                RealmObject.addChangeListener(object, listener);

                // Cleanup when stream is disposed
                emitter.setDisposable(Disposables.fromRunnable(new Runnable() {
                    @Override
                    public void run() {
                        if (!observableRealm.isClosed()) {
                            RealmObject.removeChangeListener(object, listener);
                            observableRealm.close();
                        }
                        objectRefs.get().releaseReference(object);
                    }
                }));

                // Emit current value immediately
                emitter.onNext(new ObjectChange<>(returnFrozenObjects ? RealmObject.freeze(object) : object, null));
            }
        }).subscribeOn(scheduler).unsubscribeOn(scheduler);
    }

    @Override
    public Flowable<DynamicRealmObject> from(DynamicRealm realm, final DynamicRealmObject object) {
        if (realm.isFrozen()) {
            return Flowable.just(object);
        }
        final RealmConfiguration realmConfig = realm.getConfiguration();
        Scheduler scheduler = getScheduler();
        return Flowable.create(new FlowableOnSubscribe<DynamicRealmObject>() {
            @Override
            public void subscribe(final FlowableEmitter<DynamicRealmObject> emitter) {
                // If the Realm has been closed, just create an empty Observable because we assume it is going to be disposed shortly.
                if (!RealmObject.isValid(object)) return;

                // Gets instance to make sure that the Realm is open for as long as the
                // Observable is subscribed to it.
                final DynamicRealm observableRealm = DynamicRealm.getInstance(realmConfig);
                objectRefs.get().acquireReference(object);
                final RealmChangeListener<DynamicRealmObject> listener = new RealmChangeListener<DynamicRealmObject>() {
                    @Override
                    public void onChange(DynamicRealmObject obj) {
                        if (!emitter.isCancelled()) {
                            emitter.onNext(returnFrozenObjects ? RealmObject.freeze(obj) : obj);
                        }
                    }
                };
                RealmObject.addChangeListener(object, listener);

                // Cleanup when stream is disposed
                emitter.setDisposable(Disposables.fromRunnable(new Runnable() {
                    @Override
                    public void run() {
                        if (!observableRealm.isClosed()) {
                            RealmObject.removeChangeListener(object, listener);
                            observableRealm.close();
                        }
                        objectRefs.get().releaseReference(object);
                    }
                }));

                // Emit current value immediately
                emitter.onNext(returnFrozenObjects ? RealmObject.freeze(object) : object);

            }
        }, BACK_PRESSURE_STRATEGY).subscribeOn(scheduler).unsubscribeOn(scheduler);
    }

    @Override
    public Observable<ObjectChange<DynamicRealmObject>> changesetsFrom(DynamicRealm realm, final DynamicRealmObject object) {
        if (realm.isFrozen()) {
            return Observable.just(new ObjectChange<DynamicRealmObject>(object, null));
        }
        final RealmConfiguration realmConfig = realm.getConfiguration();
        Scheduler scheduler = getScheduler();
        return Observable.create(new ObservableOnSubscribe<ObjectChange<DynamicRealmObject>>() {
            @Override
            public void subscribe(final ObservableEmitter<ObjectChange<DynamicRealmObject>> emitter) {
                // If the Realm has been closed, just create an empty Observable because we assume it is going to be disposed shortly.
                if (!RealmObject.isValid(object)) return;

                // Gets instance to make sure that the Realm is open for as long as the
                // Observable is subscribed to it.
                final DynamicRealm observableRealm = DynamicRealm.getInstance(realmConfig);
                objectRefs.get().acquireReference(object);
                final RealmObjectChangeListener<DynamicRealmObject> listener = new RealmObjectChangeListener<DynamicRealmObject>() {
                    @Override
                    public void onChange(DynamicRealmObject obj, ObjectChangeSet changeSet) {
                        if (!emitter.isDisposed()) {
                            emitter.onNext(new ObjectChange<>(returnFrozenObjects ? RealmObject.freeze(obj) : obj, changeSet));
                        }
                    }
                };
                object.addChangeListener(listener);

                // Cleanup when stream is disposed
                emitter.setDisposable(Disposables.fromRunnable(new Runnable() {
                    @Override
                    public void run() {
                        if (!observableRealm.isClosed()) {
                            RealmObject.removeChangeListener(object, listener);
                            observableRealm.close();
                        }
                        objectRefs.get().releaseReference(object);
                    }
                }));

                // Emit current value immediately
                emitter.onNext(new ObjectChange<>(returnFrozenObjects ? RealmObject.freeze(object) : object, null));
            }
        }).subscribeOn(scheduler).unsubscribeOn(scheduler);
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
