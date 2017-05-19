/*
 * Copyright 2015 Realm Inc.
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

package io.realm.internal.modules;

import android.util.JsonReader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.realm.Realm;
import io.realm.RealmModel;
import io.realm.internal.ColumnInfo;
import io.realm.internal.OsObjectSchemaInfo;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.RealmProxyMediator;
import io.realm.internal.Row;
import io.realm.internal.SharedRealm;
import io.realm.internal.Util;


/**
 * This class is able to merge different RealmProxyMediators, so they look like one.
 */
public class CompositeMediator extends RealmProxyMediator {

    private final Map<Class<? extends RealmModel>, RealmProxyMediator> mediators;

    public CompositeMediator(RealmProxyMediator... mediators) {
        final HashMap<Class<? extends RealmModel>, RealmProxyMediator> tempMediators = new HashMap<>();
        if (mediators != null) {
            for (RealmProxyMediator mediator : mediators) {
                for (Class<? extends RealmModel> realmClass : mediator.getModelClasses()) {
                    tempMediators.put(realmClass, mediator);
                }
            }
        }
        this.mediators = Collections.unmodifiableMap(tempMediators);
    }

    @Override
    public List<OsObjectSchemaInfo> getExpectedObjectSchemaInfoList() {
        List<OsObjectSchemaInfo> infoList = new ArrayList<OsObjectSchemaInfo>();
        for (RealmProxyMediator mediator : mediators.values()) {
            infoList.addAll(mediator.getExpectedObjectSchemaInfoList());
        }
        return infoList;
    }

    @Override
    public ColumnInfo validateTable(Class<? extends RealmModel> clazz, SharedRealm sharedRealm,
            boolean allowExtraColumns) {
        RealmProxyMediator mediator = getMediator(clazz);
        return mediator.validateTable(clazz, sharedRealm, allowExtraColumns);
    }

    @Override
    public List<String> getFieldNames(Class<? extends RealmModel> clazz) {
        RealmProxyMediator mediator = getMediator(clazz);
        return mediator.getFieldNames(clazz);
    }

    @Override
    public String getTableName(Class<? extends RealmModel> clazz) {
        RealmProxyMediator mediator = getMediator(clazz);
        return mediator.getTableName(clazz);
    }

    @Override
    public <E extends RealmModel> E newInstance(Class<E> clazz,
            Object baseRealm,
            Row row,
            ColumnInfo columnInfo,
            boolean acceptDefaultValue,
            List<String> excludeFields) {
        RealmProxyMediator mediator = getMediator(clazz);
        return mediator.newInstance(clazz, baseRealm, row, columnInfo, acceptDefaultValue, excludeFields);
    }

    @Override
    public Set<Class<? extends RealmModel>> getModelClasses() {
        return mediators.keySet();
    }

    @Override
    public <E extends RealmModel> E copyOrUpdate(Realm realm, E object, boolean update, Map<RealmModel, RealmObjectProxy> cache) {
        RealmProxyMediator mediator = getMediator(Util.getOriginalModelClass(object.getClass()));
        return mediator.copyOrUpdate(realm, object, update, cache);
    }

    @Override
    public void insert(Realm realm, RealmModel object, Map<RealmModel, Long> cache) {
        RealmProxyMediator mediator = getMediator(Util.getOriginalModelClass(object.getClass()));
        mediator.insert(realm, object, cache);
    }

    @Override
    public void insert(Realm realm, Collection<? extends RealmModel> objects) {
        RealmProxyMediator mediator = getMediator(Util.getOriginalModelClass(Util.getOriginalModelClass(objects.iterator().next().getClass())));
        mediator.insert(realm, objects);
    }

    @Override
    public void insertOrUpdate(Realm realm, RealmModel object, Map<RealmModel, Long> cache) {
        RealmProxyMediator mediator = getMediator(Util.getOriginalModelClass(object.getClass()));
        mediator.insertOrUpdate(realm, object, cache);
    }

    @Override
    public void insertOrUpdate(Realm realm, Collection<? extends RealmModel> objects) {
        RealmProxyMediator mediator = getMediator(Util.getOriginalModelClass(Util.getOriginalModelClass(objects.iterator().next().getClass())));
        mediator.insertOrUpdate(realm, objects);
    }

    @Override
    public <E extends RealmModel> E createOrUpdateUsingJsonObject(Class<E> clazz, Realm realm, JSONObject json, boolean update) throws JSONException {
        RealmProxyMediator mediator = getMediator(clazz);
        return mediator.createOrUpdateUsingJsonObject(clazz, realm, json, update);
    }

    @Override
    public <E extends RealmModel> E createUsingJsonStream(Class<E> clazz, Realm realm, JsonReader reader) throws IOException {
        RealmProxyMediator mediator = getMediator(clazz);
        return mediator.createUsingJsonStream(clazz, realm, reader);
    }

    @Override
    public <E extends RealmModel> E createDetachedCopy(E realmObject, int maxDepth, Map<RealmModel, RealmObjectProxy.CacheData<RealmModel>> cache) {
        RealmProxyMediator mediator = getMediator(Util.getOriginalModelClass(realmObject.getClass()));
        return mediator.createDetachedCopy(realmObject, maxDepth, cache);
    }

    @Override
    public boolean transformerApplied() {
        for (Map.Entry<Class<? extends RealmModel>, RealmProxyMediator> entry : mediators.entrySet()) {
            if (!entry.getValue().transformerApplied()) {
                return false;
            }
        }
        return true;
    }

    // Returns the mediator for a given model class (not RealmProxy) or throws exception
    private RealmProxyMediator getMediator(Class<? extends RealmModel> clazz) {
        RealmProxyMediator mediator = mediators.get(clazz);
        if (mediator == null) {
            throw new IllegalArgumentException(clazz.getSimpleName() + " is not part of the schema for this Realm");
        }
        return mediator;
    }
}
