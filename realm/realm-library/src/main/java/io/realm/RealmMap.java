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
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.realm.internal.Freezable;
import io.realm.internal.ManageableObject;
import io.realm.internal.OsMap;

/**
 * FIXME
 *
 * @param <K>
 * @param <V>
 */
public class RealmMap<K, V> implements Map<K, V>, ManageableObject, Freezable<RealmMap<K, V>> {

    private final MapStrategy<K, V> mapStrategy;

    // ------------------------------------------
    // Unmanaged constructors
    // ------------------------------------------

    /**
     * Instantiates a RealmMap in unmanaged mode.
     */
    public RealmMap() {
        this.mapStrategy = new UnmanagedMapStrategy<>();
    }

    /**
     * Instantiates a RealmMap in unmanaged mode with an initial map.
     *
     * @param map initial map.
     */
    public RealmMap(Map<K, V> map) {
        this();
        mapStrategy.putAll(map);
    }

    // ------------------------------------------
    // Managed constructors
    // ------------------------------------------

    /**
     * FIXME
     *
     * @param baseRealm
     * @param osMap
     * @param keyClass
     * @param valueClass
     */
    RealmMap(BaseRealm baseRealm, OsMap osMap, Class<K> keyClass, Class<V> valueClass) {
        this.mapStrategy = new ManagedMapStrategy<>(baseRealm, osMap, keyClass, valueClass);
    }

    // ------------------------------------------
    // ManageableObject API
    // ------------------------------------------

    @Override
    public boolean isManaged() {
        return mapStrategy.isManaged();
    }

    @Override
    public boolean isValid() {
        return mapStrategy.isValid();
    }

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
    public boolean containsValue(Object value) {
        return mapStrategy.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return mapStrategy.get(key);
    }

    @Override
    public V put(K key, V value) {
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

    // TODO: should we override any default methods from parent map class?
    // TODO: add additional map methods ad-hoc

    /**
     * FIXME
     *
     * @param <K>
     * @param <V>
     */
    private abstract static class MapStrategy<K, V> implements Map<K, V>, ManageableObject, Freezable<RealmMap<K, V>> {

        // ------------------------------------------
        // ManageableObject API
        // ------------------------------------------

        /**
         * FIXME
         *
         * @param key
         * @param value
         * @return
         */
        protected abstract V putInternal(K key, V value);

        // ------------------------------------------
        // Map API
        // ------------------------------------------

        @Override
        public V put(K key, V value) {
            checkValidKey(key);
            return putInternal(key, value);
        }

        protected void checkValidKey(K key) {
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
     * @param <K> the key
     * @param <V> the value
     */
    private static class ManagedMapStrategy<K, V> extends MapStrategy<K, V> {

        private final Class<K> keyClass;
        private final Class<V> valueClass;
        private final MapOperator<K, V> mapOperator;

        protected ManagedMapStrategy(BaseRealm baseRealm, OsMap osMap, Class<K> keyClass, Class<V> valueClass) {
            checkKeyClass(keyClass);
            checkValueClass(valueClass);

            this.keyClass = keyClass;
            this.valueClass = valueClass;
            this.mapOperator = MapOperatorFactory.getOperator(keyClass, valueClass, baseRealm, osMap);
        }

        // ------------------------------------------
        // ManageableObject API
        // ------------------------------------------

        @Override
        public boolean isManaged() {
            return mapOperator.isManaged();
        }

        @Override
        public boolean isValid() {
            return mapOperator.isValid();
        }

        @Override
        public boolean isFrozen() {
            return mapOperator.isFrozen();
        }

        // ------------------------------------------
        // Map API
        // ------------------------------------------

        @Override
        public int size() {
            return mapOperator.size();
        }

        @Override
        public boolean isEmpty() {
            return mapOperator.isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            return mapOperator.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return mapOperator.containsKey(value);
        }

        @Override
        public V get(Object key) {
            return mapOperator.get(key);
        }

        @Override
        public V remove(Object key) {
            return mapOperator.remove(key);
        }

        @Override
        public void putAll(Map<? extends K, ? extends V> m) {
            mapOperator.putAll(m);
        }

        @Override
        public void clear() {
            mapOperator.clear();
        }

        @Override
        public Set<K> keySet() {
            return mapOperator.keySet();
        }

        @Override
        public Collection<V> values() {
            return mapOperator.values();
        }

        @Override
        public Set<Entry<K, V>> entrySet() {
            return mapOperator.entrySet();
        }

        // ------------------------------------------
        // Freezable API
        // ------------------------------------------

        @Override
        public RealmMap<K, V> freeze() {
            return null;
        }

        // ------------------------------------------
        // MapStrategy API
        // ------------------------------------------

        @Override
        protected V putInternal(K key, V value) {
            return null;
        }

        private void checkKeyClass(Class<K> keyClass) {
            if (keyClass != String.class) {
                throw new IllegalArgumentException("Only String keys are allowed.");
            }
        }

        private void checkValueClass(Class<V> valueClass) {
            // TODO: add RealmSet as invalid type when ready for phase 1
            if (valueClass == RealmMap.class ||
                    valueClass == RealmList.class ||
                    valueClass == List.class) {
                throw new IllegalArgumentException("Instances of " + valueClass.getSimpleName() + " are not allowed as values.");
            }
        }
    }

    /**
     * Concrete {@link MapStrategy} that works for unmanaged {@link io.realm.RealmMap}s.
     *
     * @param <K> the key
     * @param <V> the value
     */
    private static class UnmanagedMapStrategy<K, V> extends MapStrategy<K, V> {

        private final Map<K, V> unmanagedMap = new HashMap<>();

        protected UnmanagedMapStrategy() {
            super();
        }

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
            return unmanagedMap.containsKey(value);
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
            throw new UnsupportedOperationException("This method is only available in managed RealmMaps.");
        }

        // ------------------------------------------
        // MapStrategy API
        // ------------------------------------------

        @Override
        protected V putInternal(K key, V value) {
            return unmanagedMap.put(key, value);
        }
    }
}
