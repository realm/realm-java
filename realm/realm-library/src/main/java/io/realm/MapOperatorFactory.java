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

import io.realm.internal.ManageableObject;

/**
 * This factory instantiates a {@link MapOperator} matching the map's key and value types.
 * <p>
 * Note: at the moment {@link RealmMap}s can only use {@code String}s as keys and primitive, Mixed,
 * RealmList, RealmSet and RealmMap as values - TODO at the moment we only support integer values.
 */
public class MapOperatorFactory {

    /**
     * FIXME
     *
     * @param keyClass
     * @param valueClass
     * @param baseRealm
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> MapOperator<K, V> getOperator(Class<K> keyClass, Class<V> valueClass, BaseRealm baseRealm) {
        // TODO: only String keys for now
        if (keyClass != String.class) {
            throw new IllegalArgumentException("Only String keys are allowed in RealmMaps.");
        } else {
            // TODO: add other types when ready
            if (valueClass == Integer.class) {
                return new MapOperator<>(baseRealm, keyClass, new IntegerValueOperator(baseRealm));
            } else {
                throw new IllegalArgumentException("Only Integer values are allowed in RealmMaps.");
            }
        }
    }
}

/**
 * FIXME
 */
class MapOperator<K, V> implements Map<K, V>, ManageableObject {

    private final BaseRealm baseRealm;
    private final Class<K> keyClass;
    private final MapValueOperator mapValueOperator;

    public MapOperator(BaseRealm baseRealm, Class<K> keyClass, MapValueOperator mapValueOperator) {
        this.baseRealm = baseRealm;
        this.keyClass = keyClass;
        this.mapValueOperator = mapValueOperator;
    }

    @Override
    public boolean isManaged() {
        return true;
    }

    @Override
    public boolean isValid() {
        if (baseRealm == null) {
            return true;
        }
        if (baseRealm.isClosed()) {
            return false;
        }

        // TODO: use operator + do it natively
        return true;
    }

    @Override
    public boolean isFrozen() {
        return (baseRealm != null && baseRealm.isFrozen());
    }

    @Override
    public int size() {
        // TODO: use operator + do it natively
        return 0;
    }

    @Override
    public boolean isEmpty() {
        // TODO: use operator + do it natively
        return false;
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
    public V get(Object key) {
        // TODO: use operator + do it natively
        return null;
    }

    @Override
    public V put(K key, V value) {
        // TODO: use operator + do it natively
        return null;
    }

    @Override
    public V remove(Object key) {
        // TODO: use operator + do it natively
        return null;
    }

    @Override
    public void putAll(Map m) {
        // TODO: use operator + do it natively
    }

    @Override
    public void clear() {
        // TODO: use operator + do it natively
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
 * FIXME
 */
abstract class MapValueOperator {

    protected final BaseRealm baseRealm;

    public MapValueOperator(BaseRealm baseRealm) {
        this.baseRealm = baseRealm;
    }
}

/**
 * FIXME
 */
class IntegerValueOperator extends MapValueOperator {

    public IntegerValueOperator(BaseRealm baseRealm) {
        super(baseRealm);
    }
}
