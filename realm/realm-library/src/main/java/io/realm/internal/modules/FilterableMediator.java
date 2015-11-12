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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.internal.ColumnInfo;
import io.realm.internal.ImplicitTransaction;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.RealmProxyMediator;
import io.realm.internal.Table;
import io.realm.internal.Util;

/**
 * Specialized version of a {@link RealmProxyMediator} that can further filter the available classes based on provided
 * filter.
 */
public class FilterableMediator extends RealmProxyMediator {

    private RealmProxyMediator originalMediator;
    private Set<Class<? extends RealmObject>> allowedClasses = new HashSet<Class<? extends RealmObject>>();

    /**
     * Creates a filterable {@link RealmProxyMediator}.
     *
     * @param originalMediator the original auto generated mediator.
     * @param allowedClasses the subset of classes from original mediator to allow.
     */
    public FilterableMediator(RealmProxyMediator originalMediator, Collection<Class<? extends RealmObject>> allowedClasses) {
        this.originalMediator = originalMediator;
        if (originalMediator != null) {
            Set<Class<? extends RealmObject>> originalClasses = originalMediator.getModelClasses();
            for (Class<? extends RealmObject> clazz : allowedClasses) {
                if (originalClasses.contains(clazz)) {
                    this.allowedClasses.add(clazz);
                }
            }
        }
    }

    public RealmProxyMediator getOriginalMediator() {
        return originalMediator;
    }

    @Override
    public Table createTable(Class<? extends RealmObject> clazz, ImplicitTransaction transaction) {
        checkSchemaHasClass(clazz);
        return originalMediator.createTable(clazz, transaction);
    }

    @Override
    public ColumnInfo validateTable(Class<? extends RealmObject> clazz, ImplicitTransaction transaction) {
        checkSchemaHasClass(clazz);
        return originalMediator.validateTable(clazz, transaction);
    }

    @Override
    public List<String> getFieldNames(Class<? extends RealmObject> clazz) {
        checkSchemaHasClass(clazz);
        return originalMediator.getFieldNames(clazz);
    }

    @Override
    public String getTableName(Class<? extends RealmObject> clazz) {
        checkSchemaHasClass(clazz);
        return originalMediator.getTableName(clazz);
    }

    @Override
    public <E extends RealmObject> E newInstance(Class<E> clazz, ColumnInfo columnInfo) {
        checkSchemaHasClass(clazz);
        return originalMediator.newInstance(clazz, columnInfo);
    }

    @Override
    public Set<Class<? extends RealmObject>> getModelClasses() {
        return new HashSet<Class<? extends RealmObject>>(allowedClasses);
    }

    @Override
    public <E extends RealmObject> E copyOrUpdate(Realm realm, E object, boolean update, Map<RealmObject, RealmObjectProxy> cache) {
        checkSchemaHasClass(Util.getOriginalModelClass(object.getClass()));
        return originalMediator.copyOrUpdate(realm, object, update, cache);
    }

    @Override
    public <E extends RealmObject> E createOrUpdateUsingJsonObject(Class<E> clazz, Realm realm, JSONObject json, boolean update) throws JSONException {
        checkSchemaHasClass(clazz);
        return originalMediator.createOrUpdateUsingJsonObject(clazz, realm, json, update);
    }

    @Override
    public <E extends RealmObject> E createUsingJsonStream(Class<E> clazz, Realm realm, JsonReader reader) throws IOException {
        checkSchemaHasClass(clazz);
        return originalMediator.createUsingJsonStream(clazz, realm, reader);
    }

    // Validate if a model class (not RealmProxy) is part of this Schema.
    private void checkSchemaHasClass(Class<? extends RealmObject> clazz) {
        if (!allowedClasses.contains(clazz)) {
            throw new IllegalArgumentException(clazz.getSimpleName() + " is not part of the schema for this Realm");
        }
    }
}
