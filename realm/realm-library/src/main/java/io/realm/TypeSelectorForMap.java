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

import static io.realm.CollectionUtils.DICTIONARY_TYPE;


/**
 * Abstracts certain operations from value operators depending on the type of Realm we are working
 * with.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
abstract class TypeSelectorForMap<K, V> {

    protected final BaseRealm baseRealm;
    protected final OsMap osMap;

    TypeSelectorForMap(BaseRealm baseRealm, OsMap osMap) {
        this.baseRealm = baseRealm;
        this.osMap = osMap;
    }

    protected V getRealmModel(BaseRealm baseRealm, long realmModelKey) {
        throw new UnsupportedOperationException("Function 'getRealmModel' can only be called from 'LinkSelectorForMap' instances.");
    }

    protected V putRealmModel(BaseRealm baseRealm, OsMap osMap, K key, @Nullable V value) {
        throw new UnsupportedOperationException("Function 'putRealmModel' can only be called from 'LinkSelectorForMap' instances.");
    }

    protected Map.Entry<K, V> getModelEntry(BaseRealm baseRealm, long objRow, K key) {
        throw new UnsupportedOperationException("Function 'getModelEntry' can only be called from 'LinkSelectorForMap' instances.");
    }

    abstract Set<K> keySet();

    abstract Collection<V> getValues();

    abstract RealmDictionary<V> freeze(BaseRealm frozenBaseRealm);

    abstract String getValueClassName();

    abstract Class<V> getValueClass();
}

/**
 * Implementation for ordinary Realms.
 */
class SelectorForMap<K, V> extends TypeSelectorForMap<K, V> {

    protected final Class<K> keyClass;
    protected final Class<V> valueClass;

    SelectorForMap(BaseRealm baseRealm,
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
        boolean forPrimitives = !CollectionUtils.isClassForRealmModel(valueClass);
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
        Long valuesPtr = tableAndValuesPtr.second;
        OsResults osResults = OsResults.createFromMap(baseRealm.sharedRealm, valuesPtr);
        return new RealmResults<>(baseRealm, osResults, clazz, forPrimitives);
    }

    @Override
    Class<V> getValueClass() {
        return valueClass;
    }

    @Override
    String getValueClassName() {
        return null;
    }
}

/**
 * Implementation for ordinary Realms in case we are working with RealmModels.
 */
class LinkSelectorForMap<K, V extends RealmModel> extends SelectorForMap<K, V> {

    LinkSelectorForMap(BaseRealm baseRealm, OsMap osMap, Class<K> keyClass, Class<V> valueClass) {
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
                boolean copyObject = CollectionUtils.checkCanObjectBeCopied(baseRealm, value, valueClass.getSimpleName(), DICTIONARY_TYPE);
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
class DynamicSelectorForMap<K> extends TypeSelectorForMap<K, DynamicRealmObject> {

    private final String className;

    DynamicSelectorForMap(BaseRealm baseRealm,
                                 OsMap osMap,
                                 String className) {
        super(baseRealm, osMap);
        this.className = className;
    }

    @Override
    public DynamicRealmObject getRealmModel(BaseRealm baseRealm, long realmModelKey) {
        return baseRealm.get(DynamicRealmObject.class, className, realmModelKey);
    }

    @Override
    public DynamicRealmObject putRealmModel(BaseRealm baseRealm, OsMap osMap, K key, @Nullable DynamicRealmObject value) {
        long rowModelKey = osMap.getModelRowKey(key);

        if (value == null) {
            osMap.put(key, null);
        } else {
            boolean isEmbedded = baseRealm.getSchema().getSchemaForClass(className).isEmbedded();
            if (isEmbedded) {
                long objKey = osMap.createAndPutEmbeddedObject(key);
                CollectionUtils.updateEmbeddedObject((Realm) baseRealm, value, objKey);
            } else {
                boolean copyObject = CollectionUtils.checkCanObjectBeCopied(baseRealm, value, className, DICTIONARY_TYPE);
                RealmObjectProxy proxy = (RealmObjectProxy) ((copyObject) ? CollectionUtils.copyToRealm(baseRealm, value) : value);
                osMap.putRow(key, proxy.realmGet$proxyState().getRow$realm().getObjectKey());
            }
        }

        if (rowModelKey == OsMap.NOT_FOUND) {
            return null;
        } else {
            return baseRealm.get(DynamicRealmObject.class, className, rowModelKey);
        }
    }

    // Do not use <K> or <V> as this method can be used for either keys or values
    private <T> RealmResults<T> produceResults(BaseRealm baseRealm,
            Pair<Table, Long> tableAndValuesPtr,
            boolean forPrimitives,
            String className) {
        Long valuesPtr = tableAndValuesPtr.second;
        OsResults osResults = OsResults.createFromMap(baseRealm.sharedRealm, valuesPtr);
        return new RealmResults<>(baseRealm, osResults, className, forPrimitives);
    }

    @Override
    public Map.Entry<K, DynamicRealmObject> getModelEntry(BaseRealm baseRealm, long objRow, K key) {
        DynamicRealmObject realmModel = baseRealm.get(DynamicRealmObject.class, className, objRow);
        return new AbstractMap.SimpleImmutableEntry<>(key, realmModel);
    }

    @Override
    public Set<K> keySet() {
        return new HashSet<>(produceResults(baseRealm, osMap.tableAndKeyPtrs(), false, className));
    }

    @Override
    public Collection<DynamicRealmObject> getValues() {
        return produceResults(baseRealm, osMap.tableAndValuePtrs(), false, className);
    }

    @Override
    public RealmDictionary<DynamicRealmObject> freeze(BaseRealm frozenBaseRealm) {
        return new RealmDictionary<>(frozenBaseRealm, osMap, className);
    }

    @Override
    Class<DynamicRealmObject> getValueClass() {
        return DynamicRealmObject.class;
    }

    @Override
    String getValueClassName() {
        return className;
    }
}
