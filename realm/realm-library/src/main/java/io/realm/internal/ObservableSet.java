/*
 * Copyright 2021 Realm Inc.
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

package io.realm.internal;

import io.realm.RealmSet;
import io.realm.SetChangeListener;
import io.realm.SetChangeSet;

// Helper class for supporting add change listeners to RealmSets via SetValueOperator and its subclasses.
@Keep   // Prevent this class from being obfuscated by proguard
public interface ObservableSet {

    void notifyChangeListeners(long nativeChangeSetPtr);

    class SetObserverPair<T> extends ObserverPairList.ObserverPair<RealmSet<T>, Object> {
        public SetObserverPair(RealmSet<T> observer, Object listener) {
            super(observer, listener);
        }

        public void onChange(Object observer, SetChangeSet changes) {
            //noinspection unchecked
            ((SetChangeListener<T>) listener).onChange((RealmSet<T>) observer, changes);
        }
    }

    class Callback<T> implements ObserverPairList.Callback<SetObserverPair<T>> {

        private final SetChangeSet changeSet;

        public Callback(SetChangeSet changeSet) {
            this.changeSet = changeSet;
        }

        @Override
        public void onCalled(SetObserverPair<T> pair, Object observer) {
            pair.onChange(observer, changeSet);
        }
    }
}
