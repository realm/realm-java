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
import io.realm.internal.OsMap;
import io.realm.internal.Util;

/**
 * This factory instantiates a {@link ManagedMapOperator} matching the map's key and value types.
 * <p>
 * Note: at the moment {@link RealmMap}s can only use {@code String}s as keys and primitive, Mixed,
 * RealmList, RealmSet and RealmMap as values
 * <p>
 * TODO even though Integers shouldn't be accepted, we are using them until Mixed type is ready
 */
public class MapOperatorFactory {

    /**
     * FIXME
     *
     * @param keyClassString
     * @param valueClassString
     * @param baseRealm
     * @param osMap
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> ManagedMapOperator<K, V> getOperator(String keyClassString, String valueClassString, BaseRealm baseRealm, OsMap osMap) {
        // TODO: only String keys for now
        if (!keyClassString.equals(String.class.toString())) {
            throw new IllegalArgumentException("Only String keys are allowed in RealmMaps.");
        } else {
            // TODO: add other types when ready
            if (valueClassString.equals(Integer.class.toString())) {
                return new ManagedMapOperator<>(baseRealm, keyClassString, new IntegerValueOperator(baseRealm, osMap));
            } else {
                throw new IllegalArgumentException("Only Integer values are allowed in RealmMaps.");
            }
        }
    }
}

/**
 * FIXME
 *
 * @param <K>
 * @param <V>
 */
class ManagedMapOperator<K, V> implements Map<K, V>, ManageableObject {

    private final BaseRealm baseRealm;
    private final Class<K> keyClass;
    private final MapValueOperator mapValueOperator;

    ManagedMapOperator(BaseRealm baseRealm, String keyClass, MapValueOperator mapValueOperator) {
        this.baseRealm = baseRealm;
        this.keyClass = getKeyClass(keyClass);
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

    private Class<K> getKeyClass(String keyClass) {
        //noinspection unchecked
        return (Class<K>) Util.getClassForName(keyClass);
    }
}

/**
 * FIXME
 */
abstract class MapValueOperator {

    protected final BaseRealm baseRealm;
    protected final OsMap osMap;

    MapValueOperator(BaseRealm baseRealm, OsMap osMap) {
        this.baseRealm = baseRealm;
        this.osMap = osMap;
    }
}

/**
 * FIXME
 */
class IntegerValueOperator extends MapValueOperator {

    IntegerValueOperator(BaseRealm baseRealm, OsMap osMap) {
        super(baseRealm, osMap);
    }
}
