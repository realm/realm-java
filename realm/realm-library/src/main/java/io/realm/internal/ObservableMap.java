/*
 * Copyright 2020 Realm Inc.
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

import org.jetbrains.annotations.Nullable;

import io.realm.MapChangeListener;
import io.realm.MapChangeSet;
import io.realm.RealmChangeListener;
import io.realm.RealmMap;

// Helper class for supporting add change listeners to RealmMaps via ManagedMapManager and its subclases.
@Keep   // Prevent this class from being obfuscated by proguard
public interface ObservableMap {

    void notifyChangeListeners(long nativeChangeSetPtr);

    class MapObserverPair<K, V> extends ObserverPairList.ObserverPair<RealmMap<K, V>, Object> {
        public MapObserverPair(RealmMap<K, V> observer, MapChangeListener<K, V> listener) {
            super(observer, listener);
        }

        public void onChange(Object observer, MapChangeSet<K> changes) {
            //noinspection unchecked
            ((MapChangeListener<K, V>) listener).onChange((RealmMap<K, V>) observer, changes);
        }
    }

    class RealmChangeListenerWrapper<K, V> implements MapChangeListener<K, V> {
        private final RealmChangeListener<RealmMap<K, V>> listener;

        public RealmChangeListenerWrapper(RealmChangeListener<RealmMap<K, V>> listener) {
            this.listener = listener;
        }

        @Override
        public void onChange(RealmMap<K, V> map, @Nullable MapChangeSet<K> changes) {
            listener.onChange(map);
        }

        @Override
        public boolean equals(Object o) {
            //noinspection unchecked
            return o instanceof RealmChangeListenerWrapper &&
                    listener == ((RealmChangeListenerWrapper<K, V>) o).listener;
        }

        @Override
        public int hashCode() {
            return listener.hashCode();
        }
    }

    class Callback<K, V> implements ObserverPairList.Callback<MapObserverPair<K, V>> {

        private final MapChangeSet<K> changeSet;

        public Callback(MapChangeSet<K> changeSet) {
            this.changeSet = changeSet;
        }

        @Override
        public void onCalled(MapObserverPair<K, V> pair, Object observer) {
            pair.onChange(observer, changeSet);
        }
    }
}
