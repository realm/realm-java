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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.realm.Realm;
import io.realm.RealmModel;
import io.realm.exceptions.RealmException;
import io.realm.internal.ColumnInfo;
import io.realm.internal.OsObjectSchemaInfo;
import io.realm.internal.OsSchemaInfo;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.RealmProxyMediator;
import io.realm.internal.Row;
import io.realm.internal.Util;


/**
 * This class is able to merge different RealmProxyMediators, so they look like one.
 */
public class CompositeMediator extends RealmProxyMediator {

    private final Map<Class<? extends RealmModel>, RealmProxyMediator> mediators;
    private final Map<String, Class<? extends RealmModel>> internalClassNames = new HashMap<>();

    public CompositeMediator(RealmProxyMediator... mediators) {
        final HashMap<Class<? extends RealmModel>, RealmProxyMediator> tempMediators = new HashMap<>();
        //noinspection ConstantConditions
        if (mediators != null) {
            for (RealmProxyMediator mediator : mediators) {
                for (Class<? extends RealmModel> realmClass : mediator.getModelClasses()) {
                    // Verify that the module doesn't contain conflicting definitions for the same
                    // underlying internal name. Can only happen if we add a module from a library
                    // and a module from the app at the same time.
                    String newInternalName = mediator.getSimpleClassName(realmClass);
                    Class existingClass = internalClassNames.get(newInternalName);
                    if (existingClass != null && !existingClass.equals(realmClass)) {
                         throw new IllegalStateException(String.format("It is not allowed for two different " +
                                 "model classes to share the same internal name in Realm. The " +
                                 "classes %s and %s are being included from the modules '%s' and '%s' " +
                                 "and they share the same internal name '%s'.", existingClass, realmClass,
                                 tempMediators.get(existingClass), mediator,
                                 newInternalName));
                    }

                    // Store mapping between
                    tempMediators.put(realmClass, mediator);
                    internalClassNames.put(newInternalName, realmClass);
                }
            }
        }
        this.mediators = Collections.unmodifiableMap(tempMediators);
    }

    @Override
    public Map<Class<? extends RealmModel>, OsObjectSchemaInfo> getExpectedObjectSchemaInfoMap() {
        Map<Class<? extends RealmModel>, OsObjectSchemaInfo> infoMap = new HashMap<>();
        for (RealmProxyMediator mediator : mediators.values()) {
            infoMap.putAll(mediator.getExpectedObjectSchemaInfoMap());
        }
        return infoMap;
    }

    @Override
    public ColumnInfo createColumnInfo(Class<? extends RealmModel> clazz, OsSchemaInfo osSchemaInfo) {
        RealmProxyMediator mediator = getMediator(clazz);
        return mediator.createColumnInfo(clazz, osSchemaInfo);
    }

    @Override
    protected String getSimpleClassNameImpl(Class<? extends RealmModel> clazz) {
        RealmProxyMediator mediator = getMediator(clazz);
        return mediator.getSimpleClassName(clazz);
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
            throw new RealmException(clazz.getSimpleName() + " is not part of the schema for this Realm");
        }
        return mediator;
    }
}
