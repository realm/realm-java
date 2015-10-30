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

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;
import rx.Observable;
import rx.subjects.BehaviorSubject;

public class Rx1ObservableFactory {

    /**
     * Creates an Observable for a RealmObject. It will emit the initial object when subscribed to and on each update
     * to the object.
     *
     * @param object RealmObject to listen to changes for.
     * @param <E> Type of RealmObject
     */
    public static <E extends RealmObject> Observable<RealmObject<E>> from(final RealmObject<E> object) {
        final BehaviorSubject<RealmObject<E>> subject = BehaviorSubject.create(object);
        // TODO This currently triggers on all changes in Realm. Replace with fine-grained notifications when possible.
        object.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                // TODO This should ideally be a threadsafe, immutable object. Currently it is not :(
                subject.onNext(object);
            }
        });
        return subject.asObservable();
    }

    /**
     * Creates an Observable for a RealmResults. It will emit the initial results when subscribed to and on each
     * update to the results.
     *
     * @param results RealmObject to listen to changes for.
     * @param <E> Type of RealmObject
     */
    public static <E extends RealmObject> Observable<RealmResults<E>> from(final RealmResults<E> results) {
        final BehaviorSubject<RealmResults<E>> subject = BehaviorSubject.create(results);
        // TODO This currently triggers on all changes in Realm. Replace with fine-grained notifications when possible.
        results.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                // TODO This should ideally be a threadsafe, immutable object. Currently it is not :(
                subject.onNext(results);
            }
        });
        return subject.asObservable();
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
