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
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

import io.realm.internal.Freezable;
import io.realm.internal.ManageableObject;
import io.realm.internal.ObservableMap;
import io.realm.internal.ObserverPairList;
import io.realm.internal.OsMap;
import io.realm.internal.RealmObjectProxy;
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
abstract class ManagedMapManager<K, V> implements Map<K, V>, ManageableObject, Freezable<RealmMap<K, V>>, ObservableMap {

    protected final BaseRealm baseRealm;
    protected final MapValueOperator<K, V> mapValueOperator;
    protected final TypeSelectorForMap<K, V> typeSelectorForMap;
    protected final ObserverPairList<MapObserverPair<K, V>> mapObserverPairs = new ObserverPairList<>();

    ManagedMapManager(BaseRealm baseRealm,
                      MapValueOperator<K, V> mapValueOperator,
                      TypeSelectorForMap<K, V> typeSelectorForMap) {
        this.baseRealm = baseRealm;
        this.mapValueOperator = mapValueOperator;
        this.typeSelectorForMap = typeSelectorForMap;
    }

    protected abstract boolean containsKeyInternal(@Nullable Object key);

    protected abstract void validateMap(Map<? extends K, ? extends V> map);

    protected abstract RealmMap<K, V> freezeInternal(Pair<BaseRealm, OsMap> frozenBaseRealmMap);

    protected abstract MapChangeSet<K> changeSetFactory(long nativeChangeSetPtr);


    @Override
    public abstract V put(K key, V value);

    @Override
    public abstract Set<Entry<K, V>> entrySet();

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
    public V remove(Object key) {
        if (key == null) {
            throw new NullPointerException("Null keys are not allowed.");
        }

        //noinspection unchecked
        V removedValue = mapValueOperator.get((K) key);
        mapValueOperator.remove(key);
        return removedValue;
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
    public boolean containsKey(@Nullable Object key) {
        return containsKeyInternal(key);
    }

    @Override
    public boolean containsValue(@Nullable Object value) {
        return mapValueOperator.containsValue(value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        validateMap(m);
        mapValueOperator.putAll(m);
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
    public RealmMap<K, V> freeze() {
        return freezeInternal(mapValueOperator.freeze());
    }

    @Override
    public void notifyChangeListeners(long nativeChangeSetPtr) {
        MapChangeSet<K> mapChangeSet = new MapChangeSetImpl<>(changeSetFactory(nativeChangeSetPtr));
        if (mapChangeSet.isEmpty()) {
            // First callback we get is right after subscription: do nothing
            return;
        }
        mapObserverPairs.foreach(new Callback<>(mapChangeSet));
    }

    public void addChangeListener(RealmMap<K, V> realmMap, MapChangeListener<K, V> listener) {
        CollectionUtils.checkForAddRemoveListener(baseRealm, listener, true);
        if (mapObserverPairs.isEmpty()) {
            mapValueOperator.startListening(this);
        }
        ObservableMap.MapObserverPair<K, V> mapObserverPair = new MapObserverPair<>(realmMap, listener);
        mapObserverPairs.add(mapObserverPair);
    }

    public void addChangeListener(RealmMap<K, V> realmMap, RealmChangeListener<RealmMap<K, V>> listener) {
        addChangeListener(realmMap, new RealmChangeListenerWrapper<>(listener));
    }

    public void removeListener(RealmMap<K, V> realmMap, MapChangeListener<K, V> listener) {
        mapObserverPairs.remove(realmMap, listener);
        if (mapObserverPairs.isEmpty()) {
            mapValueOperator.stopListening();
        }
    }

    public void removeListener(RealmMap<K, V> realmMap, RealmChangeListener<RealmMap<K, V>> listener) {
        removeListener(realmMap, new RealmChangeListenerWrapper<>(listener));
    }

    public void removeAllChangeListeners() {
        CollectionUtils.checkForAddRemoveListener(baseRealm, null, false);
        mapObserverPairs.clear();
        mapValueOperator.stopListening();
    }

    public boolean hasListeners() {
        return !mapObserverPairs.isEmpty();
    }

    protected boolean isNotNullItemTypeValid(@Nullable Object item, Class<?> clazz) {
        return item == null || item.getClass() == clazz;
    }

    OsMap getOsMap() {
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

    DictionaryManager(BaseRealm baseRealm,
                      MapValueOperator<String, V> mapValueOperator,
                      TypeSelectorForMap<String, V> typeSelectorForMap) {
        super(baseRealm, mapValueOperator, typeSelectorForMap);
    }

    @Override
    public boolean containsKeyInternal(Object key) {
        if (key == null) {
            throw new NullPointerException("Null keys are not allowed when calling 'containsKey'.");
        }
        if (!isNotNullItemTypeValid(key, String.class)) {
            throw new ClassCastException("Only String keys can be used with 'containsKey'.");
        }
        return mapValueOperator.containsKey(key);
    }

    @Override
    protected void validateMap(Map<? extends String, ? extends V> map) {
        for (Map.Entry<? extends String, ? extends V> entry : map.entrySet()) {
            String key = entry.getKey();
            if (key == null) {
                throw new NullPointerException("Null keys are not allowed.");
            }
        }
    }

    @Override
    protected RealmDictionary<V> freezeInternal(Pair<BaseRealm, OsMap> frozenBaseRealmMap) {
        BaseRealm frozenBaseRealm = frozenBaseRealmMap.first;
        return typeSelectorForMap.freeze(frozenBaseRealm);
    }

    @Override
    public V get(Object key) {
        if (key == null) {
            throw new NullPointerException("Null keys are not allowed when calling 'get'.");
        }
        if (!isNotNullItemTypeValid(key, String.class)) {
            throw new ClassCastException("Only String keys can be used with 'containsKey'.");
        }

        return mapValueOperator.get((String) key);
    }

    @Override
    public V put(String key, V value) {
        if (key == null) {
            throw new NullPointerException("Null keys are not allowed.");
        }
        try {
            return mapValueOperator.put(key, value);
        } catch (IllegalStateException e) {
            // If the exception caught here is caused by adding null to a dictionary marked as
            // "@Required" we have to convert it to NullPointerException as per the Java Map
            // interface
            if (Objects.requireNonNull(e.getMessage()).contains("Data type mismatch")) {
                throw new NullPointerException("Cannot insert null values in a dictionary marked with '@Required'.");
            } else {
                throw e;
            }
        }
    }

    @Override
    public Set<Entry<String, V>> entrySet() {
        return mapValueOperator.entrySet();
    }

    @Override
    protected MapChangeSet<String> changeSetFactory(long nativeChangeSetPtr) {
        return new StringMapChangeSet(nativeChangeSetPtr);
    }
}

/**
 * Abstraction for different map value types. Here are defined as generics but specializations
 * should provide concrete types.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
abstract class MapValueOperator<K, V> {

    protected final Class<V> valueClass;
    protected final BaseRealm baseRealm;
    protected final OsMap osMap;
    protected final TypeSelectorForMap<K, V> typeSelectorForMap;
    protected final RealmMapEntrySet.IteratorType iteratorType;

    MapValueOperator(Class<V> valueClass,
                     BaseRealm baseRealm,
                     OsMap osMap,
                     TypeSelectorForMap<K, V> typeSelectorForMap,
                     RealmMapEntrySet.IteratorType iteratorType) {
        this.valueClass = valueClass;
        this.baseRealm = baseRealm;
        this.osMap = osMap;
        this.typeSelectorForMap = typeSelectorForMap;
        this.iteratorType = iteratorType;
    }

    @Nullable
    public abstract V get(K key);

    @Nullable
    public abstract V put(K key, @Nullable V value);

    public abstract Set<Map.Entry<K, V>> entrySet();

    public abstract boolean containsValueInternal(@Nullable Object value);

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

    public boolean containsValue(@Nullable Object value) {
        if (value != null && value.getClass() != valueClass) {
            throw new ClassCastException("Only '" + valueClass.getSimpleName() +
                    "'  values can be used with 'containsValue'.");
        }
        return containsValueInternal(value);
    }

    public boolean isValid() {
        if (baseRealm.isClosed()) {
            return false;
        }
        return osMap.isValid();
    }

    public boolean isFrozen() {
        return baseRealm.isFrozen();
    }

    public void clear() {
        osMap.clear();
    }

    public void putAll(Map<? extends K, ? extends V> map) {
        // TODO: inefficient, pass array of keys and array of values to JNI instead,
        //  which requires operators to implement it as it varies from type to type
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public Set<K> keySet() {
        return typeSelectorForMap.keySet();
    }

    public Collection<V> values() {
        return typeSelectorForMap.getValues();
    }

    public Pair<BaseRealm, OsMap> freeze() {
        BaseRealm frozenRealm = baseRealm.freeze();
        return new Pair<>(frozenRealm, osMap.freeze(frozenRealm.sharedRealm));
    }

    public void startListening(ObservableMap observableMap) {
        osMap.startListening(observableMap);
    }

    public void stopListening() {
        osMap.stopListening();
    }
}

/**
 * {@link MapValueOperator} targeting {@link Mixed} values in {@link RealmMap}s.
 */
class MixedValueOperator<K> extends MapValueOperator<K, Mixed> {

    MixedValueOperator(BaseRealm baseRealm,
                       OsMap osMap,
                       TypeSelectorForMap<K, Mixed> typeSelectorForMap) {
        super(Mixed.class, baseRealm, osMap, typeSelectorForMap, RealmMapEntrySet.IteratorType.MIXED);
    }

    @Nullable
    @Override
    public Mixed get(Object key) {
        long mixedPtr = osMap.getMixedPtr(key);
        if (mixedPtr == OsMap.NOT_FOUND) {
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

    @Override
    public Set<Map.Entry<K, Mixed>> entrySet() {
        return new RealmMapEntrySet<>(baseRealm, osMap, RealmMapEntrySet.IteratorType.MIXED, null);
    }

    @Override
    public boolean containsValueInternal(@Nullable Object value) {
        // Mixed dictionaries store null values as Mixed.nullValue()
        if (value == null) {
            return false;
        }
        if (value instanceof Mixed) {
            return osMap.containsMixedValue(((Mixed) value).getNativePtr());
        }
        throw new IllegalArgumentException("This dictionary can only contain 'Mixed' values.");
    }
}

/**
 * {@link MapValueOperator} targeting boxable values in {@link RealmMap}s.
 */
class GenericPrimitiveValueOperator<K, V> extends MapValueOperator<K, V> {

    private final EqualsHelper<K, V> equalsHelper;

    GenericPrimitiveValueOperator(Class<V> valueClass,
                                  BaseRealm baseRealm,
                                  OsMap osMap,
                                  TypeSelectorForMap<K, V> typeSelectorForMap,
                                  RealmMapEntrySet.IteratorType iteratorType) {
        this(valueClass, baseRealm, osMap, typeSelectorForMap, iteratorType, new GenericEquals<>());
    }

    GenericPrimitiveValueOperator(Class<V> valueClass,
                                  BaseRealm baseRealm,
                                  OsMap osMap,
                                  TypeSelectorForMap<K, V> typeSelectorForMap,
                                  RealmMapEntrySet.IteratorType iteratorType,
                                  EqualsHelper<K, V> equalsHelper) {
        super(valueClass, baseRealm, osMap, typeSelectorForMap, iteratorType);
        this.equalsHelper = equalsHelper;
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
    public V put(K key, @Nullable V value) {
        V original = get(key);
        osMap.put(key, value);
        return original;
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return new RealmMapEntrySet<>(baseRealm, osMap, iteratorType, equalsHelper, null);
    }

    @Override
    public boolean containsValueInternal(@Nullable Object value) {
        return osMap.containsPrimitiveValue(value);
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
 * instead of {@link GenericPrimitiveValueOperator} to avoid and typecast exception when converting the
 * {@link Long} result from JNI to {@link Integer}.
 */
class IntegerValueOperator<K> extends GenericPrimitiveValueOperator<K, Integer> {

    IntegerValueOperator(BaseRealm baseRealm,
                         OsMap osMap,
                         TypeSelectorForMap<K, Integer> typeSelectorForMap) {
        super(Integer.class, baseRealm, osMap, typeSelectorForMap, RealmMapEntrySet.IteratorType.INTEGER);
    }

    @Override
    protected Integer processValue(Object value) {
        return ((Long) value).intValue();
    }
}

/**
 * {@link MapValueOperator} targeting {@link Short} values in {@link RealmMap}s. Use this one
 * instead of {@link GenericPrimitiveValueOperator} to avoid and typecast exception when converting the
 * {@link Long} result from JNI to {@link Short}.
 */
class ShortValueOperator<K> extends GenericPrimitiveValueOperator<K, Short> {

    ShortValueOperator(BaseRealm baseRealm,
                       OsMap osMap,
                       TypeSelectorForMap<K, Short> typeSelectorForMap) {
        super(Short.class, baseRealm, osMap, typeSelectorForMap, RealmMapEntrySet.IteratorType.SHORT);
    }

    @Override
    protected Short processValue(Object value) {
        return ((Long) value).shortValue();
    }
}

/**
 * {@link MapValueOperator} targeting {@link Byte} values in {@link RealmMap}s. Use this one
 * instead of {@link GenericPrimitiveValueOperator} to avoid and typecast exception when converting the
 * {@link Long} result from JNI to {@link Byte}.
 */
class ByteValueOperator<K> extends GenericPrimitiveValueOperator<K, Byte> {

    ByteValueOperator(BaseRealm baseRealm,
                      OsMap osMap,
                      TypeSelectorForMap<K, Byte> typeSelectorForMap) {
        super(Byte.class, baseRealm, osMap, typeSelectorForMap, RealmMapEntrySet.IteratorType.BYTE);
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

    RealmModelValueOperator(BaseRealm baseRealm,
                            OsMap osMap,
                            TypeSelectorForMap<K, V> typeSelectorForMap) {
        //noinspection unchecked
        super((Class<V>) RealmModel.class, baseRealm, osMap, typeSelectorForMap, RealmMapEntrySet.IteratorType.OBJECT);
    }

    @Nullable
    @Override
    public V get(Object key) {
        long realmModelKey = osMap.getModelRowKey(key);
        if (realmModelKey == OsMap.NOT_FOUND) {
            return null;
        }

        return typeSelectorForMap.getRealmModel(baseRealm, realmModelKey);
    }

    @Nullable
    @Override
    public V put(K key, @Nullable V value) {
        return typeSelectorForMap.putRealmModel(baseRealm, osMap, key, value);
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return new RealmMapEntrySet<>(baseRealm, osMap, RealmMapEntrySet.IteratorType.OBJECT, typeSelectorForMap);
    }

    @Override
    public boolean containsValueInternal(@Nullable Object value) {
        if (value == null) {
            return osMap.containsPrimitiveValue(null);
        } else if (value instanceof RealmObjectProxy) {
            Row row$realm = ((RealmObjectProxy) value).realmGet$proxyState().getRow$realm();
            long tablePtr = row$realm.getTable().getNativePtr();
            return osMap.containsRealmModel(row$realm.getObjectKey(), tablePtr);
        }
        throw new IllegalArgumentException("Only managed models can be contained in this dictionary.");
    }

    @Override
    public boolean containsValue(@Nullable Object value) {
        if (value != null && !RealmModel.class.isAssignableFrom(value.getClass())) {
            throw new ClassCastException("Only RealmModel values can be used with 'containsValue'.");
        }
        return containsValueInternal(value);
    }
}

