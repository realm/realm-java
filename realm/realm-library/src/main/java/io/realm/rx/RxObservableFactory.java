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

package io.realm.rx;

import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.Subscriptions;

// TODO Would it be better (= more efficient) to try to reuse RealmChangeListeners across observables?
// TODO Extract factory interface
// TODO Convert this class to non-static. Add Configuration option for injecting factory instances, RealmConfiguration is kinda meh.
public class RxObservableFactory {

    /**
     * Creates an Observable for a RealmObject. It will emit the initial object when subscribed to and on each update
     * to the object.
     *
     * RealmObject observables are effectively hot observables as RealmObjects auto-update.
     *
     * @param object RealmObject to listen to changes for.
     * @param <E> Type of RealmObject
     */
    public static <E extends RealmObject> Observable<E> from(final E object) {
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
                // value. This mimics a BehaviorSubject.
                subscriber.onNext(object);
            }
        });
    }

    /**
     * Creates an Observable for a RealmResults. It will emit the initial results when subscribed to and on each
     * update to the results.
     *
     * RealmResults observables are effectively hot observables as RealmObjects auto-update.
     *
     * @param results RealmResults to listen to changes for.
     * @param <E> Type of RealmObject
     */
    public static <E extends RealmObject> Observable<RealmResults<E>> from(final RealmResults<E> results) {
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
                // value. This mimics a BehaviorSubject.
                subscriber.onNext(results);
            }
        });
    }

    /**
     * Creates an Observable for a RealmResults. It will emit the initial list when subscribed to and on each
     * update to the list.
     *
     * @param list RealmObject to listen to changes for.
     * @param <E> Type of RealmObject
     */
    public static <E extends RealmObject> Observable<RealmList<E>> from(final RealmList<E> list) {
        final BehaviorSubject<RealmList<E>> subject = BehaviorSubject.create(list);
        // TODO Need to add ChangeListener to RealmList. Require fine-grained notifications.
        throw new RuntimeException("Not supported yet");
    }
}
