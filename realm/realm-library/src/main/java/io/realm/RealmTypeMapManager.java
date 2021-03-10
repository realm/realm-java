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

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import io.realm.internal.OsMap;
import io.realm.internal.OsResults;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.Table;
import io.realm.internal.util.Pair;

/**
 * Abstracts certain operations from value operators depending on the type of Realm we are working
 * with.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
abstract class RealmTypeMapManager<K, V> {

    protected final BaseRealm baseRealm;
    protected final OsMap osMap;

    RealmTypeMapManager(BaseRealm baseRealm, OsMap osMap) {
        this.baseRealm = baseRealm;
        this.osMap = osMap;
    }

    protected V getRealmModel(BaseRealm baseRealm, long realmModelKey) {
        // do nothing here, override only for RealModels
        return null;
    }

    protected V putRealmModel(BaseRealm baseRealm, OsMap osMap, K key, @Nullable V value) {
        // do nothing here, override only for RealModels
        return null;
    }

    protected Map.Entry<K, V> getModelEntry(BaseRealm baseRealm, long objRow, K key) {
        // do nothing here, override only for RealModels
        return null;
    }

    abstract Set<K> keySet();

    abstract Collection<V> getValues();

    abstract RealmDictionary<V> freeze(BaseRealm frozenBaseRealm);
}

/**
 * Implementation for ordinary Realms.
 */
class RealmMapManager<K, V> extends RealmTypeMapManager<K, V> {

    protected final Class<K> keyClass;
    protected final Class<V> valueClass;

    public RealmMapManager(BaseRealm baseRealm,
                           OsMap osMap,
                           Class<K> keyClass,
                           Class<V> valueClass) {
        super(baseRealm, osMap);
        this.keyClass = keyClass;
        this.valueClass = valueClass;
    }

    @Override
    public Set<K> keySet() {
        return new HashSet<>(produceResults(baseRealm, osMap.tableAndKeyPtrs(), true, keyClass));
    }

    @Override
    public Collection<V> getValues() {
        boolean forPrimitives = !RealmModel.class.isAssignableFrom(valueClass);
        return produceResults(baseRealm, osMap.tableAndValuePtrs(), forPrimitives, valueClass);
    }

    @Override
    public RealmDictionary<V> freeze(BaseRealm frozenBaseRealm) {
        return new RealmDictionary<>(frozenBaseRealm, osMap, valueClass);
    }

    // Do not use <K> or <V> as this method can be used for either keys or values
    private <T> RealmResults<T> produceResults(BaseRealm baseRealm,
                                               Pair<Table, Long> tableAndValuesPtr,
                                               boolean forPrimitives,
                                               @Nullable Class<T> clazz) {
        if (baseRealm instanceof Realm) {
            Realm realm = (Realm) baseRealm;
            Long valuesPtr = tableAndValuesPtr.second;
            OsResults osResults = OsResults.createFromMap(baseRealm.sharedRealm, valuesPtr);
            if (clazz != null) {
                return new RealmResults<>(realm, osResults, clazz, forPrimitives);
            }
            throw new IllegalStateException("MapValueOperator missing class.");
        }

        throw new UnsupportedOperationException("Add support for 'values' for DynamicRealms.");
    }
}

/**
 * Implementation for ordinary Realms in case we are working with RealmModels.
 */
class LinkRealmMapManager<K, V extends RealmModel> extends RealmMapManager<K, V> {

    public LinkRealmMapManager(BaseRealm baseRealm, OsMap osMap, Class<K> keyClass, Class<V> valueClass) {
        super(baseRealm, osMap, keyClass, valueClass);
    }

    @Override
    public V getRealmModel(BaseRealm baseRealm, long realmModelKey) {
        return baseRealm.get(valueClass, null, realmModelKey);
    }

    @Override
    public V putRealmModel(BaseRealm baseRealm, OsMap osMap, K key, @Nullable V value) {
        long rowModelKey = osMap.getModelRowKey(key);

        if (value == null) {
            osMap.put(key, null);
        } else {
            boolean isEmbedded = baseRealm.getSchema().getSchemaForClass(valueClass).isEmbedded();
            if (isEmbedded) {
                long objKey = osMap.createAndPutEmbeddedObject(key);
                CollectionUtils.updateEmbeddedObject((Realm) baseRealm, value, objKey);
            } else {
                boolean copyObject = CollectionUtils.checkCanObjectBeCopied(baseRealm, value, valueClass.getSimpleName());
                RealmObjectProxy proxy = (RealmObjectProxy) ((copyObject) ? CollectionUtils.copyToRealm(baseRealm, value) : value);
                osMap.putRow(key, proxy.realmGet$proxyState().getRow$realm().getObjectKey());
            }
        }

        if (rowModelKey == OsMap.NOT_FOUND) {
            return null;
        } else {
            return (V) baseRealm.get(valueClass, rowModelKey, false, new ArrayList<>());
        }
    }

    @Override
    public Map.Entry<K, V> getModelEntry(BaseRealm baseRealm, long objRow, K key) {
        V realmModel = baseRealm.get(valueClass, null, objRow);
        return new AbstractMap.SimpleImmutableEntry<>(key, realmModel);
    }
}

/**
 * Implementation for DynamicRealms.
 */
class DynamicRealmMapManager<K, V> extends RealmTypeMapManager<K, V> {

    private final String className;

    public DynamicRealmMapManager(BaseRealm baseRealm,
                                  OsMap osMap,
                                  String className) {
        super(baseRealm, osMap);
        this.className = className;
    }

    @Override
    public V getRealmModel(BaseRealm baseRealm, long realmModelKey) {
        // TODO
        throw new UnsupportedOperationException("Support for getRealmModel for DynamicRealms not ready yet ");
    }

    @Override
    public V putRealmModel(BaseRealm baseRealm, OsMap osMap, K key, @Nullable V value) {
        // TODO
        throw new UnsupportedOperationException("Support for putRealmModel for DynamicRealms not ready yet ");
    }

    @Override
    public Map.Entry<K, V> getModelEntry(BaseRealm baseRealm, long objRow, K key) {
        // TODO
        throw new UnsupportedOperationException("Support for getModelEntry for DynamicRealms not ready yet ");
    }

    @Override
    public Set<K> keySet() {
        // TODO
        throw new UnsupportedOperationException("Support for keySet for DynamicRealms not ready yet ");
    }

    @Override
    public Collection<V> getValues() {
        // TODO
        throw new UnsupportedOperationException("Support for getValues for DynamicRealms not ready yet ");
    }

    @Override
    public RealmDictionary<V> freeze(BaseRealm frozenBaseRealm) {
        // TODO
        throw new UnsupportedOperationException("Support for freeze for DynamicRealms not ready yet ");
    }
}
