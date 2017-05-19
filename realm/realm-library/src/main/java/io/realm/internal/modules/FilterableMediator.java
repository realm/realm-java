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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.realm.Realm;
import io.realm.RealmModel;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;
import io.realm.internal.ColumnInfo;
import io.realm.internal.OsObjectSchemaInfo;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.RealmProxyMediator;
import io.realm.internal.Row;
import io.realm.internal.SharedRealm;
import io.realm.internal.Util;


/**
 * Specialized version of a {@link RealmProxyMediator} that can further filter the available classes based on provided
 * filter.
 */
public class FilterableMediator extends RealmProxyMediator {

    private final RealmProxyMediator originalMediator;
    private final Set<Class<? extends RealmModel>> allowedClasses;
    private final Set<String> allowedClasseNames;

    /**
     * Creates a filterable {@link RealmProxyMediator}.
     *
     * @param originalMediator the original auto generated mediator.
     * @param allowedClasses the subset of classes from original mediator to allow.
     */
    public FilterableMediator(RealmProxyMediator originalMediator, Collection<Class<? extends RealmModel>> allowedClasses) {
        this.originalMediator = originalMediator;

        Set<Class<? extends RealmModel>> tempAllowedClasses = new HashSet<>();
        if (originalMediator != null) {
            Set<Class<? extends RealmModel>> originalClasses = originalMediator.getModelClasses();
            for (Class<? extends RealmModel> clazz : allowedClasses) {
                if (originalClasses.contains(clazz)) {
                    tempAllowedClasses.add(clazz);
                }
            }
        }
        this.allowedClasses = Collections.unmodifiableSet(tempAllowedClasses);

        Set<String> tempAllowedClassNames = new HashSet<String>();
        for (Class<? extends RealmModel> clazz : this.allowedClasses) {
            tempAllowedClassNames.add(clazz.getSimpleName());
        }
        this.allowedClasseNames = Collections.unmodifiableSet(tempAllowedClassNames);
    }

    @Override
    public List<OsObjectSchemaInfo> getExpectedObjectSchemaInfoList() {
        List<OsObjectSchemaInfo> infoList = new ArrayList<OsObjectSchemaInfo>();
        for (OsObjectSchemaInfo objectSchemaInfo : originalMediator.getExpectedObjectSchemaInfoList()) {
            if (allowedClasseNames.contains(objectSchemaInfo.getClassName())) {
                infoList.add(objectSchemaInfo);
            }
        }
        return infoList;
    }

    @Override
    public ColumnInfo validateTable(Class<? extends RealmModel> clazz, SharedRealm sharedRealm,
            boolean allowExtraColumns) {
        checkSchemaHasClass(clazz);
        return originalMediator.validateTable(clazz, sharedRealm, allowExtraColumns);
    }

    @Override
    public List<String> getFieldNames(Class<? extends RealmModel> clazz) {
        checkSchemaHasClass(clazz);
        return originalMediator.getFieldNames(clazz);
    }

    @Override
    public String getTableName(Class<? extends RealmModel> clazz) {
        checkSchemaHasClass(clazz);
        return originalMediator.getTableName(clazz);
    }

    @Override
    public <E extends RealmModel> E newInstance(Class<E> clazz,
            Object baseRealm,
            Row row,
            ColumnInfo columnInfo,
            boolean acceptDefaultValue,
            List<String> excludeFields) {
        checkSchemaHasClass(clazz);
        return originalMediator.newInstance(clazz, baseRealm, row, columnInfo, acceptDefaultValue, excludeFields);
    }

    @Override
    public Set<Class<? extends RealmModel>> getModelClasses() {
        return allowedClasses;
    }

    @Override
    public <E extends RealmModel> E copyOrUpdate(Realm realm, E object, boolean update, Map<RealmModel, RealmObjectProxy> cache) {
        checkSchemaHasClass(Util.getOriginalModelClass(object.getClass()));
        return originalMediator.copyOrUpdate(realm, object, update, cache);
    }

    @Override
    public void insert(Realm realm, RealmModel object, Map<RealmModel, Long> cache) {
        checkSchemaHasClass(Util.getOriginalModelClass(object.getClass()));
        originalMediator.insert(realm, object, cache);
    }

    @Override
    public void insert(Realm realm, Collection<? extends RealmModel> objects) {
        checkSchemaHasClass(Util.getOriginalModelClass(objects.iterator().next().getClass()));
        originalMediator.insert(realm, objects);
    }

    @Override
    public void insertOrUpdate(Realm realm, RealmModel object, Map<RealmModel, Long> cache) {
        checkSchemaHasClass(Util.getOriginalModelClass(object.getClass()));
        originalMediator.insertOrUpdate(realm, object, cache);
    }

    @Override
    public void insertOrUpdate(Realm realm, Collection<? extends RealmModel> objects) {
        checkSchemaHasClass(Util.getOriginalModelClass(objects.iterator().next().getClass()));
        originalMediator.insertOrUpdate(realm, objects);
    }

    @Override
    public <E extends RealmModel> E createOrUpdateUsingJsonObject(Class<E> clazz, Realm realm, JSONObject json, boolean update) throws JSONException {
        checkSchemaHasClass(clazz);
        return originalMediator.createOrUpdateUsingJsonObject(clazz, realm, json, update);
    }

    @Override
    public <E extends RealmModel> E createUsingJsonStream(Class<E> clazz, Realm realm, JsonReader reader) throws IOException {
        checkSchemaHasClass(clazz);
        return originalMediator.createUsingJsonStream(clazz, realm, reader);
    }

    @Override
    public <E extends RealmModel> E createDetachedCopy(E realmObject, int maxDepth, Map<RealmModel, RealmObjectProxy.CacheData<RealmModel>> cache) {
        checkSchemaHasClass(Util.getOriginalModelClass(realmObject.getClass()));
        return originalMediator.createDetachedCopy(realmObject, maxDepth, cache);
    }

    @Override
    public boolean transformerApplied() {
        //noinspection SimplifiableIfStatement
        if (originalMediator == null) {
            return true;
        }
        return originalMediator.transformerApplied();
    }

    // Validates if a model class (not RealmProxy) is part of this Schema.
    private void checkSchemaHasClass(Class<? extends RealmModel> clazz) {
        if (!allowedClasses.contains(clazz)) {
            throw new IllegalArgumentException(clazz.getSimpleName() + " is not part of the schema for this Realm");
        }
    }
}
