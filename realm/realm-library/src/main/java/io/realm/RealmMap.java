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

package io.realm;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import io.realm.internal.Freezable;
import io.realm.internal.ManageableObject;
import io.realm.internal.OsMap;

/**
 * RealmMap is used to map keys to values. A RealmMap cannot contain duplicate keys and each key can
 * map to at most one value. A RealmMap cannot have {@code null} keys but can have {@code null}
 * values.
 * <p>
 * Similarly to {@link RealmList}s, a RealmDictionary can operate in managed and unmanaged modes. In
 * managed mode a RealmDictionary persists all its contents inside a Realm whereas in unmanaged mode
 * it functions like a {@link HashMap}.
 * <p>
 * Managed RealmDictionaries can only be created by Realm and will automatically update its content
 * whenever the underlying Realm is updated. Managed RealmDictionaries can only be accessed using
 * the getter that points to a RealmDictionary field of a {@link RealmObject}.
 * <p>
 * Unmanaged RealmDictionaries can be created by the user and can contain both managed and unmanaged
 * RealmObjects. This is useful when dealing with JSON deserializers like GSON or other frameworks
 * that inject values into a class. Unmanaged elements in this list can be added to a Realm using
 * the {@link Realm#copyToRealm(Iterable, ImportFlag...)} method.
 *
 * @param <K> the type of the keys stored in this map
 * @param <V> the type of the values stored in this map
 */
public abstract class RealmMap<K, V> implements Map<K, V>, ManageableObject, Freezable<RealmMap<K, V>> {

    protected final MapStrategy<K, V> mapStrategy;

    // ------------------------------------------
    // Unmanaged constructors
    // ------------------------------------------

    /**
     * Instantiates a RealmMap in unmanaged mode.
     */
    protected RealmMap() {
        this.mapStrategy = new UnmanagedMapStrategy<>();
    }

    /**
     * Instantiates a RealmMap in unmanaged mode with an initial map.
     *
     * @param map initial map.
     */
    RealmMap(Map<K, V> map) {
        this();

        mapStrategy.putAll(map);
    }

    // ------------------------------------------
    // Managed constructors
    // ------------------------------------------

    RealmMap(MapStrategy<K, V> mapStrategy) {
        this.mapStrategy = mapStrategy;
    }

    // ------------------------------------------
    // ManageableObject API
    // ------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isManaged() {
        return mapStrategy.isManaged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid() {
        return mapStrategy.isValid();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFrozen() {
        return mapStrategy.isFrozen();
    }

    // ------------------------------------------
    // Map API
    // ------------------------------------------

    @Override
    public int size() {
        return mapStrategy.size();
    }

    @Override
    public boolean isEmpty() {
        return mapStrategy.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return mapStrategy.containsKey(key);
    }

    @Override
    public boolean containsValue(@Nullable Object value) {
        return mapStrategy.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return mapStrategy.get(key);
    }

    @Override
    public V put(K key, @Nullable V value) {
        return mapStrategy.put(key, value);
    }

    @Override
    public V remove(Object key) {
        return mapStrategy.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        mapStrategy.putAll(m);
    }

    @Override
    public void clear() {
        mapStrategy.clear();
    }

    @Override
    public Set<K> keySet() {
        return mapStrategy.keySet();
    }

    @Override
    public Collection<V> values() {
        return mapStrategy.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return mapStrategy.entrySet();
    }

    // ------------------------------------------
    // Freezable API
    // ------------------------------------------

    @Override
    public RealmMap<K, V> freeze() {
        return mapStrategy.freeze();
    }

    // ------------------------------------------
    // RealmMap API
    // ------------------------------------------

    /**
     * Adds a change listener to this {@link RealmMap}.
     * <p>
     * Registering a change listener will not prevent the underlying RealmList from being garbage
     * collected. If the RealmMap is garbage collected, the change listener will stop being
     * triggered. To avoid this, keep a strong reference for as long as appropriate e.g. in a class
     * variable.
     * <p>
     * <pre>
     * {@code
     * public class MyActivity extends Activity {
     *
     *     private RealmMap<String, Dog> dogs; // Strong reference to keep listeners alive
     *
     *     \@Override
     *     protected void onCreate(Bundle savedInstanceState) {
     *       super.onCreate(savedInstanceState);
     *       dogs = realm.where(Person.class).findFirst().getDogs();
     *       dogs.addChangeListener(new MapChangeListener<String, Dog>() {
     *           \@Override
     *           public void onChange(RealmMap<String, Dog> map, MapChangeSet<String> changeSet) {
     *               // React to change
     *           }
     *       });
     *     }
     * }
     * }
     * </pre>
     *
     * @param listener the change listener to be notified.
     * @throws IllegalArgumentException if the change listener is {@code null}.
     * @throws IllegalStateException    if you try to add a listener from a non-Looper or
     *                                  {@link android.app.IntentService} thread.
     */
    public void addChangeListener(MapChangeListener<K, V> listener) {
        mapStrategy.addChangeListener(this, listener);
    }

    /**
     * Adds a change listener to this {@link RealmMap}.
     * <p>
     * Registering a change listener will not prevent the underlying RealmList from being garbage
     * collected. If the RealmMap is garbage collected, the change listener will stop being
     * triggered. To avoid this, keep a strong reference for as long as appropriate e.g. in a class
     * variable.
     * <p>
     * <pre>
     * {@code
     * public class MyActivity extends Activity {
     *
     *     private RealmMap<String, Dog> dogs; // Strong reference to keep listeners alive
     *
     *     \@Override
     *     protected void onCreate(Bundle savedInstanceState) {
     *       super.onCreate(savedInstanceState);
     *       dogs = realm.where(Person.class).findFirst().getDogs();
     *       dogs.addChangeListener(new RealmChangeListener<RealmMap<String, Dog>>() {
     *           \@Override
     *           public void onChange(RealmMap<String, Dog> map) {
     *               // React to change
     *           }
     *       });
     *     }
     * }
     * }
     * </pre>
     *
     * @param listener the change listener to be notified.
     * @throws IllegalArgumentException if the change listener is {@code null}.
     * @throws IllegalStateException    if you try to add a listener from a non-Looper or
     *                                  {@link android.app.IntentService} thread.
     * @see io.realm.RealmChangeListener
     */
    public void addChangeListener(RealmChangeListener<RealmMap<K, V>> listener) {
        mapStrategy.addChangeListener(this, listener);
    }

    /**
     * Removes the specified change listener.
     *
     * @param listener the change listener to be removed.
     * @throws IllegalArgumentException if the change listener is {@code null}.
     * @throws IllegalStateException    if you try to remove a listener from a non-Looper Thread.
     */
    public void removeChangeListener(MapChangeListener<K, V> listener) {
        mapStrategy.removeChangeListener(this, listener);
    }

    /**
     * Removes the specified change listener.
     *
     * @param listener the change listener to be removed.
     * @throws IllegalArgumentException if the change listener is {@code null}.
     * @throws IllegalStateException    if you try to remove a listener from a non-Looper Thread.
     * @see io.realm.RealmChangeListener
     */
    public void removeChangeListener(RealmChangeListener<RealmMap<K, V>> listener) {
        mapStrategy.removeChangeListener(this, listener);
    }

    /**
     * Removes all user-defined change listeners.
     *
     * @throws IllegalStateException if you try to remove listeners from a non-Looper Thread.
     * @see io.realm.RealmChangeListener
     */
    public void removeAllChangeListeners() {
        mapStrategy.removeAllChangeListeners();
    }

    /**
     * Indicates whether a map has any listeners attached to it.
     *
     * @return {@code true} if any listeners have been added, {@code false} otherwise.
     */
    public boolean hasListeners() {
        return mapStrategy.hasListeners();
    }

    // Needed for embedded objects
    OsMap getOsMap() {
        return mapStrategy.getOsMap();
    }

    // TODO: should we override any default methods from parent map class?

    /**
     * Strategy responsible for abstracting the managed/unmanaged logic for maps.
     *
     * @param <K> the type of the keys stored in this map
     * @param <V> the type of the values stored in this map
     */
    protected abstract static class MapStrategy<K, V> implements Map<K, V>, ManageableObject, Freezable<RealmMap<K, V>> {

        // ------------------------------------------
        // ManageableObject API
        // ------------------------------------------

        /**
         * Internal method which checks for invalid input when calling {@link RealmMap#put(Object, Object)}.
         *
         * @param key   the key to insert.
         * @param value the value to insert.
         * @return the inserted value.
         */
        protected abstract V putInternal(K key, V value);

        protected abstract void addChangeListener(RealmMap<K, V> realmMap, MapChangeListener<K, V> listener);

        protected abstract void addChangeListener(RealmMap<K, V> realmMap, RealmChangeListener<RealmMap<K, V>> listener);

        protected abstract void removeChangeListener(RealmMap<K, V> realmMap, MapChangeListener<K, V> listener);

        protected abstract void removeChangeListener(RealmMap<K, V> realmMap, RealmChangeListener<RealmMap<K, V>> listener);

        protected abstract void removeAllChangeListeners();

        protected abstract boolean hasListeners();

        abstract OsMap getOsMap();

        // ------------------------------------------
        // Map API
        // ------------------------------------------

        @Override
        public V put(K key, V value) {
            checkValidKey(key);
            return putInternal(key, value);
        }

        protected void checkValidKey(@Nullable K key) {
            if (key == null) {
                throw new IllegalArgumentException("Null keys are not allowed.");
            }

            if (key.getClass() == String.class) {
                String stringKey = (String) key;
                if (stringKey.contains(".") || stringKey.contains("$")) {
                    throw new IllegalArgumentException("Keys containing dots ('.') or dollar signs ('$') are not allowed.");
                }
            }
        }
    }

    /**
     * Concrete {@link MapStrategy} that works for managed {@link io.realm.RealmMap}s.
     *
     * @param <K> the key type
     * @param <V> the value type
     */
    protected static class ManagedMapStrategy<K, V> extends MapStrategy<K, V> {

        private final ManagedMapManager<K, V> managedMapManager;

        /**
         * Strategy constructor for managed maps.
         *
         * @param managedMapManager the manager used by the managed map
         */
        ManagedMapStrategy(ManagedMapManager<K, V> managedMapManager) {
            this.managedMapManager = managedMapManager;
        }

        // ------------------------------------------
        // ManageableObject API
        // ------------------------------------------

        @Override
        public boolean isManaged() {
            return managedMapManager.isManaged();
        }

        @Override
        public boolean isValid() {
            return managedMapManager.isValid();
        }

        @Override
        public boolean isFrozen() {
            return managedMapManager.isFrozen();
        }

        // ------------------------------------------
        // Map API
        // ------------------------------------------

        @Override
        public int size() {
            return managedMapManager.size();
        }

        @Override
        public boolean isEmpty() {
            return managedMapManager.isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            return managedMapManager.containsKey(key);
        }

        @Override
        public boolean containsValue(@Nullable Object value) {
            return managedMapManager.containsValue(value);
        }

        @Override
        public V get(Object key) {
            return managedMapManager.get(key);
        }

        @Override
        public V remove(Object key) {
            return managedMapManager.remove(key);
        }

        @Override
        public void putAll(Map<? extends K, ? extends V> m) {
            managedMapManager.putAll(m);
        }

        @Override
        public void clear() {
            managedMapManager.clear();
        }

        @Override
        public Set<K> keySet() {
            return managedMapManager.keySet();
        }

        @Override
        public Collection<V> values() {
            return managedMapManager.values();
        }

        @Override
        public Set<Entry<K, V>> entrySet() {
            return managedMapManager.entrySet();
        }

        // ------------------------------------------
        // Freezable API
        // ------------------------------------------

        @Override
        public RealmMap<K, V> freeze() {
            return managedMapManager.freeze();
        }

        // ------------------------------------------
        // MapStrategy API
        // ------------------------------------------

        @Override
        protected V putInternal(K key, V value) {
            return managedMapManager.put(key, value);
        }

        @Override
        protected void addChangeListener(RealmMap<K, V> realmMap, MapChangeListener<K, V> listener) {
            managedMapManager.addChangeListener(realmMap, listener);
        }

        @Override
        protected void addChangeListener(RealmMap<K, V> realmMap, RealmChangeListener<RealmMap<K, V>> listener) {
            managedMapManager.addChangeListener(realmMap, listener);
        }

        @Override
        protected void removeChangeListener(RealmMap<K, V> realmMap, MapChangeListener<K, V> listener) {
            managedMapManager.removeListener(realmMap, listener);
        }

        @Override
        protected void removeChangeListener(RealmMap<K, V> realmMap, RealmChangeListener<RealmMap<K, V>> listener) {
            managedMapManager.removeListener(realmMap, listener);
        }

        @Override
        protected void removeAllChangeListeners() {
            managedMapManager.removeAllChangeListeners();
        }

        @Override
        protected boolean hasListeners() {
            return managedMapManager.hasListeners();
        }

        @Override
        OsMap getOsMap() {
            return managedMapManager.getOsMap();
        }
    }

    /**
     * Concrete {@link MapStrategy} that works for unmanaged {@link io.realm.RealmMap}s.
     * <p>
     * Unmanaged maps are backed internally by a {@link HashMap}.
     *
     * @param <K> the key type
     * @param <V> the value type
     */
    private static class UnmanagedMapStrategy<K, V> extends MapStrategy<K, V> {

        private final Map<K, V> unmanagedMap = new HashMap<>();

        // ------------------------------------------
        // ManageableObject API
        // ------------------------------------------

        @Override
        public boolean isManaged() {
            return false;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public boolean isFrozen() {
            return false;
        }

        // ------------------------------------------
        // Map API
        // ------------------------------------------

        @Override
        public int size() {
            return unmanagedMap.size();
        }

        @Override
        public boolean isEmpty() {
            return unmanagedMap.isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            return unmanagedMap.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return unmanagedMap.containsValue(value);
        }

        @Override
        public V get(Object key) {
            return unmanagedMap.get(key);
        }

        @Override
        public V remove(Object key) {
            return unmanagedMap.remove(key);
        }

        @Override
        public void putAll(Map<? extends K, ? extends V> m) {
            unmanagedMap.putAll(m);
        }

        @Override
        public void clear() {
            unmanagedMap.clear();
        }

        @Override
        public Set<K> keySet() {
            return unmanagedMap.keySet();
        }

        @Override
        public Collection<V> values() {
            return unmanagedMap.values();
        }

        @Override
        public Set<Entry<K, V>> entrySet() {
            return unmanagedMap.entrySet();
        }

        // ------------------------------------------
        // Freezable API
        // ------------------------------------------

        @Override
        public RealmMap<K, V> freeze() {
            throw new UnsupportedOperationException("Unmanaged RealmMaps cannot be frozen.");
        }

        // ------------------------------------------
        // MapStrategy API
        // ------------------------------------------

        @Override
        protected V putInternal(K key, V value) {
            return unmanagedMap.put(key, value);
        }

        @Override
        protected void addChangeListener(RealmMap<K, V> realmMap, MapChangeListener<K, V> listener) {
            throw new UnsupportedOperationException("Unmanaged RealmMaps do not support change listeners.");
        }

        @Override
        protected void addChangeListener(RealmMap<K, V> realmMap, RealmChangeListener<RealmMap<K, V>> listener) {
            throw new UnsupportedOperationException("Unmanaged RealmMaps do not support change listeners.");
        }

        @Override
        protected void removeChangeListener(RealmMap<K, V> realmMap, MapChangeListener<K, V> listener) {
            throw new UnsupportedOperationException("Cannot remove change listener because unmanaged RealmMaps do not support change listeners.");
        }

        @Override
        protected void removeChangeListener(RealmMap<K, V> realmMap, RealmChangeListener<RealmMap<K, V>> listener) {
            throw new UnsupportedOperationException("Cannot remove change listener because unmanaged RealmMaps do not support change listeners.");
        }

        @Override
        protected void removeAllChangeListeners() {
            throw new UnsupportedOperationException("Cannot remove change listener because unmanaged RealmMaps do not support change listeners.");
        }

        @Override
        protected boolean hasListeners() {
            return false;
        }

        @Override
        OsMap getOsMap() {
            throw new UnsupportedOperationException("Unmanaged maps aren't represented in native code.");
        }
    }
}
