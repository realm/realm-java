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
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import io.realm.internal.ManageableObject;
import io.realm.internal.OsMap;

/**
 * A {@code ManagedMapManager} abstracts the different types of keys and values a managed
 * {@link RealmMap} can contain.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
abstract class ManagedMapManager<K, V> implements Map<K, V>, ManageableObject {

    protected final Class<K> keyClass;
    protected final MapValueOperator<V> mapValueOperator;

    ManagedMapManager(Class<K> keyClass, MapValueOperator<V> mapValueOperator) {
        this.keyClass = keyClass;
        this.mapValueOperator = mapValueOperator;
    }

    @Override
    public abstract V put(K key, V value);

    @Override
    public boolean isManaged() {
        return true;
    }

    @Override
    public boolean isValid() {
        return mapValueOperator.isValid();
    }

    @Override
    public boolean isFrozen() {
        return mapValueOperator.isFrozen();
    }

    @Override
    public int size() {
        return mapValueOperator.size();
    }

    @Override
    public boolean isEmpty() {
        return mapValueOperator.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        // TODO: use operator + do it natively
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        // TODO: use operator + do it natively
        return false;
    }

    @Override
    public void putAll(Map m) {
        // TODO: use operator + do it natively
    }

    @Override
    public void clear() {
        mapValueOperator.clear();
    }

    @Override
    public Set<K> keySet() {
        // TODO: use operator + do it natively
        return null;
    }

    @Override
    public Collection<V> values() {
        // TODO: use operator + do it natively
        return null;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        // TODO: use operator + do it natively
        return null;
    }
}

/**
 * Specialization for {@link ManagedMapManager}s targeting {@link RealmDictionary}.
 * <p>
 * Dictionaries can in turn contain values of type {@link Mixed} and Realm primitive types, i.e.
 * integer, boolean, string, byte array, date, float, double, decimal, object id, UUID and
 * {@link RealmModel}.
 * <p>
 * A {@link MapValueOperator} representing the {@code V}-value type has to be used when
 * instantiating this operator.
 *
 * @param <V> the value type
 */
class DictionaryManager<V> extends ManagedMapManager<String, V> {

    DictionaryManager(MapValueOperator<V> mapValueOperator) {
        super(String.class, mapValueOperator);
    }

    @Override
    public V get(Object key) {
        return mapValueOperator.get(key);
    }

    @Override
    public V put(String key, V value) {
        return mapValueOperator.put(key, value);
    }

    @Override
    public V remove(Object key) {
        V removedValue = mapValueOperator.get(key);
        mapValueOperator.remove(key);
        return removedValue;
    }
}

/**
 * Abstraction for different map value types. Here are defined as generics but specializations
 * should provide concrete types.
 *
 * @param <V> the value type
 */
abstract class MapValueOperator<V> {

    protected final BaseRealm baseRealm;
    protected final OsMap osMap;

    MapValueOperator(@Nullable BaseRealm baseRealm, OsMap osMap) {
        this.baseRealm = baseRealm;
        this.osMap = osMap;
    }

    public abstract V get(Object key);

    public abstract V put(Object key, V value);

    public abstract void remove(Object key);

    public int size() {
        return (int) osMap.size();
    }

    public boolean isEmpty() {
        return osMap.size() == 0;
    }

    public boolean isValid() {
        if (baseRealm == null) {
            return true;
        }
        return !baseRealm.isClosed();
    }

    public boolean isFrozen() {
        return (baseRealm != null && baseRealm.isFrozen());
    }

    public void clear() {
        osMap.clear();
    }
}

/**
 * {@link MapValueOperator} targeting {@link Mixed} values in {@link RealmMap}s.
 */
class MixedValueOperator extends MapValueOperator<Mixed> {

    MixedValueOperator(BaseRealm baseRealm, OsMap osMap) {
        super(baseRealm, osMap);
    }

    @Override
    public Mixed get(Object key) {
        return (Mixed) osMap.get(key);
    }

    @Override
    public Mixed put(Object key, Mixed value) {
        Mixed original = (Mixed) osMap.get(key);
        osMap.put(key, value.getNativePtr());
        return original;
    }

    @Override
    public void remove(Object key) {
        osMap.remove(key);
    }
}

/**
 * {@link MapValueOperator} targeting {@link Boolean} values in {@link RealmMap}s.
 */
class BooleanValueOperator extends MapValueOperator<Boolean> {

    BooleanValueOperator(BaseRealm baseRealm, OsMap osMap) {
        super(baseRealm, osMap);
    }

    @Override
    public Boolean get(Object key) {
        return (Boolean) osMap.get(key);
    }

    @Override
    public Boolean put(Object key, Boolean value) {
        Boolean original = (Boolean) osMap.get(key);
        osMap.put(key, value);
        return original;
    }

    @Override
    public void remove(Object key) {
        osMap.remove(key);
    }
}

// TODO: add more value type operators ad-hoc
