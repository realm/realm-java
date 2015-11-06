/*
 * Copyright 2014 Realm Inc.
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

package io.realm;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

/**
 * Factory class for creating Observables for RxJava (<=1.0.15).
 * TODO Test compatibility with RxJava2/RxMobile
 */
public class RxJavaFactory implements RxObservableFactory {

    private boolean rxJavaAvailble;

    public RxJavaFactory() {
        try {
            Class.forName("rx.Observable");
            rxJavaAvailble = true;
        } catch (ClassNotFoundException ignore) {
            rxJavaAvailble = false;
        }
    }

    @Override
    public <E extends BaseRealm> Observable<E> from(final E realm) {
        checkRxJavaAvailable();
        return Observable.create(new Observable.OnSubscribe<E>() {
            @Override
            public void call(final Subscriber<? super E> subscriber) {
                final RealmChangeListener listener = new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        subscriber.onNext(realm);
                    }
                };
                realm.addChangeListener(listener);
                subscriber.add(Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        realm.removeChangeListener(listener);
                    }
                }));

                // Immediately call onNext with the current value as due to Realms auto-update it will be the latest
                // value.
                subscriber.onNext(realm);
            }
        });
    }

    @Override
    public <E extends RealmObject<E>> Observable<RealmResults<E>> from(final RealmResults<E> results) {
        checkRxJavaAvailable();
        return Observable.create(new Observable.OnSubscribe<RealmResults<E>>() {
            @Override
            public void call(final Subscriber<? super RealmResults<E>> subscriber) {
                final RealmChangeListener listener = new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        subscriber.onNext(results);
                    }
                };
                results.addChangeListener(listener);
                subscriber.add(Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        results.removeChangeListener(listener);
                    }
                }));

                // Immediately call onNext with the current value as due to Realms auto-update it will be the latest
                // value.
                subscriber.onNext(results);
            }
        });
    }

    @Override
    public <E extends RealmObject<E>> Observable<RealmList<E>> from(RealmList<E> list) {
        checkRxJavaAvailable();
        throw new RuntimeException("RealmList does not support change listeners yet, so cannot create an Observable");
    }

    @Override
    public <E extends RealmObject<E>> Observable<E> from(final E object) {
        checkRxJavaAvailable();
        return Observable.create(new Observable.OnSubscribe<E>() {
            @Override
            public void call(final Subscriber<? super E> subscriber) {
                final RealmChangeListener listener = new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        subscriber.onNext(object);
                    }
                };
                object.addChangeListener(listener);
                subscriber.add(Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        object.removeChangeListener(listener);
                    }
                }));

                // Immediately call onNext with the current value as due to Realms auto-update it will be the latest
                // value.
                subscriber.onNext(object);
            }
        });
    }

    private void checkRxJavaAvailable() {
        if (rxJavaAvailble) {
            throw new IllegalStateException("RxJava seems to be missing from the classpath. " +
                    "Remember to add it as a compile dependency. See XXX for more details.");
        }
    }
}