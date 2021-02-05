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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.realm.internal.Freezable;
import io.realm.internal.ManageableObject;
import io.realm.internal.OsMap;
import io.realm.internal.OsObjectStore;
import io.realm.internal.OsResults;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.Table;
import io.realm.internal.Row;
import io.realm.internal.core.NativeMixed;
import io.realm.internal.util.Pair;

/**
 * A {@code ManagedMapManager} abstracts the different types of keys and values a managed
 * {@link RealmMap} can contain.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
abstract class ManagedMapManager<K, V> implements Map<K, V>, ManageableObject, Freezable<RealmMap<K, V>> {

    protected final MapValueOperator<K, V> mapValueOperator;
    protected final ClassContainer classContainer;

    ManagedMapManager(MapValueOperator<K, V> mapValueOperator, ClassContainer classContainer) {
        this.mapValueOperator = mapValueOperator;
        this.classContainer = classContainer;
    }

    protected abstract RealmMap<K, V> freezeInternal(Pair<BaseRealm, OsMap> frozenBaseRealmMap);

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
        return mapValueOperator.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException("Not ready until full support for Mixed is ready");
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        //noinspection unchecked
        mapValueOperator.putAll((Map<K, V>) m);
    }

    @Override
    public void clear() {
        mapValueOperator.clear();
    }

    @Override
    public Set<K> keySet() {
        return mapValueOperator.keySet();
    }

    @Override
    public Collection<V> values() {
        return mapValueOperator.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        // Throw until we have sets in place
        throw new UnsupportedOperationException("Unmanaged maps do not support 'entrySet' yet.");
    }

    @Override
    public RealmMap<K, V> freeze() {
        Pair<BaseRealm, OsMap> frozenPair = mapValueOperator.freeze();
        return freezeInternal(frozenPair);
    }

    public OsMap getOsMap() {
        return mapValueOperator.osMap;
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

    DictionaryManager(MapValueOperator<String, V> mapValueOperator, ClassContainer classContainer) {
        super(mapValueOperator, classContainer);
    }

    @Override
    protected RealmMap<String, V> freezeInternal(Pair<BaseRealm, OsMap> frozenBaseRealmMap) {
        BaseRealm frozenBaseRealm = frozenBaseRealmMap.first;
        OsMap osMap = frozenBaseRealmMap.second;
        Class<?> clazz = classContainer.getClazz();
        String className = classContainer.getClassName();

        if (clazz != null) {
            //noinspection unchecked
            RealmDictionary<V> dictionary = new RealmDictionary<>(frozenBaseRealm, osMap, (Class<V>) clazz);
            return dictionary;
        } else if (className != null) {
            RealmDictionary<V> dictionary = new RealmDictionary<>(frozenBaseRealm, osMap, className);
            return dictionary;
        } else {
            throw new IllegalArgumentException("Either a class or a class string is required.");
        }
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
abstract class MapValueOperator<K, V> {

    protected final BaseRealm baseRealm;
    protected final OsMap osMap;
    protected final ClassContainer classContainer;

    MapValueOperator(BaseRealm baseRealm, OsMap osMap, ClassContainer classContainer) {
        this.baseRealm = baseRealm;
        this.osMap = osMap;
        this.classContainer = classContainer;
    }

    @Nullable
    public abstract V get(Object key);

    @Nullable
    public abstract V put(Object key, @Nullable V value);

    public void remove(Object key) {
        osMap.remove(key);
    }

    public int size() {
        return (int) osMap.size();
    }

    public boolean isEmpty() {
        return osMap.size() == 0;
    }

    public boolean containsKey(Object key) {
        return osMap.containsKey(key);
    }

    public boolean isValid() {
        return !baseRealm.isClosed();
    }

    public boolean isFrozen() {
        return baseRealm.isFrozen();
    }

    public void clear() {
        osMap.clear();
    }

    public void putAll(Map<K, V> map) {
        // FIXME: entrySet for managed dictionaries isn't implemented so it will fail in such case
        for (Map.Entry<K, V> entry : map.entrySet()) {
            // TODO: inefficient, pass array of keys and array of values to JNI instead,
            //  which requires operators to implement it as it varies from type to type
            put(entry.getKey(), entry.getValue());
        }
    }

    public Set<K> keySet() {
        Pair<Table, Long> tablePointerPair = osMap.keysPtr();
        if (baseRealm instanceof Realm) {
            Realm realm = (Realm) baseRealm;
            OsResults osResults = OsResults.createFromMap(baseRealm.sharedRealm, tablePointerPair.first, tablePointerPair.second);
            Class<?> clazz = classContainer.getClazz();
            if (clazz != null) {
                //noinspection unchecked
                return new HashSet<>(new RealmResults<>(realm, osResults, (Class<K>) clazz));
            }
            throw new IllegalStateException("MapValueOperator missing class in 'classContainer'.");
        }

        throw new UnsupportedOperationException("Add support for 'values' for DynamicRealms.");
    }

    public Collection<V> values() {
        Pair<Table, Long> tablePointerPair = osMap.valuesPtr();
        if (baseRealm instanceof Realm) {
            Realm realm = (Realm) baseRealm;
            OsResults osResults = OsResults.createFromMap(baseRealm.sharedRealm, tablePointerPair.first, tablePointerPair.second);
            Class<?> clazz = classContainer.getClazz();
            if (clazz != null) {
                //noinspection unchecked
                return new RealmResults<>(realm, osResults, (Class<V>) clazz);
            }
            throw new IllegalStateException("MapValueOperator missing class in 'classContainer'.");
        }

        throw new UnsupportedOperationException("Add support for 'values' for DynamicRealms.");
    }

    public Pair<BaseRealm, OsMap> freeze() {
        BaseRealm frozenRealm = baseRealm.freeze();
        return new Pair<>(frozenRealm, osMap.freeze(frozenRealm.sharedRealm));
    }
}

/**
 * {@link MapValueOperator} targeting {@link Mixed} values in {@link RealmMap}s.
 */
class MixedValueOperator<K> extends MapValueOperator<K, Mixed> {

    MixedValueOperator(BaseRealm baseRealm, OsMap osMap, ClassContainer classContainer) {
        super(baseRealm, osMap, classContainer);
    }

    @Nullable
    @Override
    public Mixed get(Object key) {
        long mixedPtr = osMap.getMixedPtr(key);
        if (mixedPtr == -1) {
            return null;
        }
        NativeMixed nativeMixed = new NativeMixed(mixedPtr);
        return new Mixed(MixedOperator.fromNativeMixed(baseRealm, nativeMixed));
    }

    @Nullable
    @Override
    public Mixed put(Object key, @Nullable Mixed value) {
        Mixed original = get(key);

        if (value == null) {
            osMap.put(key, null);
        } else {
            osMap.putMixed(key, CollectionUtils.copyToRealmIfNeeded(baseRealm, value).getNativePtr());
        }
        return original;
    }
}

/**
 * {@link MapValueOperator} targeting boxable values in {@link RealmMap}s.
 */
class BoxableValueOperator<K, V> extends MapValueOperator<K, V> {

    BoxableValueOperator(BaseRealm baseRealm, OsMap osMap, ClassContainer classContainer) {
        super(baseRealm, osMap, classContainer);
    }

    @Nullable
    @Override
    public V get(Object key) {
        Object value = osMap.get(key);
        if (value == null) {
            return null;
        }
        return processValue(value);
    }

    @Nullable
    @Override

    public V put(Object key, @Nullable V value) {
        V original = get(key);
        osMap.put(key, value);
        return original;
    }

    /**
     * Normally it is enough with typecasting the value to {@code T}, but e.g. {@link Long} cannot
     * be cast directly to {@link Integer} so a special operator has to override this method to do
     * it.
     *
     * @param value the value of the dictionary entry as an {@link Object}.
     * @return the value in its right form
     */
    @Nullable
    protected V processValue(Object value) {
        //noinspection unchecked
        return (V) value;
    }
}

/**
 * {@link MapValueOperator} targeting {@link Integer} values in {@link RealmMap}s. Use this one
 * instead of {@link BoxableValueOperator} to avoid and typecast exception when converting the
 * {@link Long} result from JNI to {@link Integer}.
 */
class IntegerValueOperator<K> extends BoxableValueOperator<K, Integer> {

    IntegerValueOperator(BaseRealm baseRealm, OsMap osMap, ClassContainer classContainer) {
        super(baseRealm, osMap, classContainer);
    }

    @Override
    protected Integer processValue(Object value) {
        return ((Long) value).intValue();
    }
}

/**
 * {@link MapValueOperator} targeting {@link Short} values in {@link RealmMap}s. Use this one
 * instead of {@link BoxableValueOperator} to avoid and typecast exception when converting the
 * {@link Long} result from JNI to {@link Short}.
 */
class ShortValueOperator<K> extends BoxableValueOperator<K, Short> {

    ShortValueOperator(BaseRealm baseRealm, OsMap osMap, ClassContainer classContainer) {
        super(baseRealm, osMap, classContainer);
    }

    @Override
    protected Short processValue(Object value) {
        return ((Long) value).shortValue();
    }
}

/**
 * {@link MapValueOperator} targeting {@link Byte} values in {@link RealmMap}s. Use this one
 * instead of {@link BoxableValueOperator} to avoid and typecast exception when converting the
 * {@link Long} result from JNI to {@link Byte}.
 */
class ByteValueOperator<K> extends BoxableValueOperator<K, Byte> {

    ByteValueOperator(BaseRealm baseRealm, OsMap osMap, ClassContainer classContainer) {
        super(baseRealm, osMap, classContainer);
    }

    @Override
    protected Byte processValue(Object value) {
        return ((Long) value).byteValue();
    }
}

/**
 * {@link MapValueOperator} targeting {@link RealmModel}s values in {@link RealmMap}s.
 */
class RealmModelValueOperator<K, V> extends MapValueOperator<K, V> {

    RealmModelValueOperator(BaseRealm baseRealm, OsMap osMap, ClassContainer classContainer) {
        super(baseRealm, osMap, classContainer);
    }

    @Nullable
    @Override
    public V get(Object key) {
        long realmModelKey = osMap.getModelRowKey(key);
        if (realmModelKey == -1) {
            return null;
        }

        //noinspection unchecked
        Class<? extends RealmModel> clazz = (Class<? extends RealmModel>) classContainer.getClazz();
        String className = classContainer.getClassName();

        //noinspection unchecked
        return (V) baseRealm.get(clazz, className, realmModelKey);
    }

    @Nullable
    @Override
    public V put(Object key, @Nullable V value) {
        //noinspection unchecked
        Class<V> clazz = (Class<V>) classContainer.getClazz();
        String className = classContainer.getClassName();
        long rowModelKey = osMap.getModelRowKey(key);

        if (value == null) {
            osMap.put(key, null);
        } else {
            if (className == null) {
                if (clazz != null) {
                    className = clazz.getCanonicalName();
                } else {
                    throw new IllegalStateException("Missing className.");
                }
            }
            boolean copyObject = CollectionUtils.checkCanObjectBeCopied(baseRealm, (RealmModel) value, className);
            RealmObjectProxy proxy = (RealmObjectProxy) ((copyObject) ? CollectionUtils.copyToRealm(baseRealm, (RealmModel) value) : (RealmModel) value);
            osMap.putRow(key, proxy.realmGet$proxyState().getRow$realm().getObjectKey());
        }

        if (rowModelKey == -1) {
            return null;
        } else {
            //noinspection unchecked
            return (V) baseRealm.get((Class<? extends RealmModel>) clazz, className, rowModelKey);
        }
    }
}

/**
 * Used to avoid passing a {@link Class} and a {@link String} via parameters to the value operators.
 */
class ClassContainer {

    @Nullable
    private final Class<?> clazz;
    @Nullable
    private final String className;

    public ClassContainer(@Nullable Class<?> clazz, @Nullable String className) {
        this.clazz = clazz;
        this.className = className;
    }

    @Nullable
    public Class<?> getClazz() {
        return clazz;
    }

    @Nullable
    public String getClassName() {
        return className;
    }
}
