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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.internal.ImplicitTransaction;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.RealmProxyMediator;
import io.realm.internal.Table;

/**
 * Specialized version of a RealmProxyMediator that can further filter the available classes based on provided filter
 */
public class FilterableMediator implements RealmProxyMediator {

    private RealmProxyMediator originalMediator;
    private Set<Class<? extends RealmObject>> allowedClasses = new HashSet<Class<? extends RealmObject>>();

    /**
     * Creates a filterable Mediator.
     *
     * @param originalMediator      Original auto generated mediator.
     * @param allowedClasses                Subset of classes from original mediator to allow.
     */
    public FilterableMediator(RealmProxyMediator originalMediator, List<Class<? extends RealmObject>> allowedClasses) {
        this.originalMediator = originalMediator;
        List<Class<? extends RealmObject>> originalClasses = originalMediator.getModelClasses();
        for (Class<? extends RealmObject> clazz : allowedClasses) {
            if (originalClasses.contains(clazz)) {
                this.allowedClasses.add(clazz);
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
    public void validateTable(Class<? extends RealmObject> clazz, ImplicitTransaction transaction) {
        checkSchemaHasClass(clazz);
        originalMediator.validateTable(clazz, transaction);
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
    public <E extends RealmObject> E newInstance(Class<E> clazz) {
        checkSchemaHasClass(clazz);
        return originalMediator.newInstance(clazz);
    }

    @Override
    public List<Class<? extends RealmObject>> getModelClasses() {
        return new ArrayList<Class<? extends RealmObject>>(allowedClasses);
    }

    @Override
    public Map<String, Long> getColumnIndices(Class<? extends RealmObject> clazz) {
        checkSchemaHasClass(clazz);
        return originalMediator.getColumnIndices(clazz);
    }

    @Override
    public <E extends RealmObject> E copyOrUpdate(Realm realm, E object, boolean update, Map<RealmObject, RealmObjectProxy> cache) {
        checkSchemaHasClass(object.getClass());
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

    private void checkSchemaHasClass(Class<? extends RealmObject> clazz) {
        if (!allowedClasses.contains(clazz)) {
            throw new IllegalArgumentException(clazz.getSimpleName() + " is not part of the schema for this Realm");
        }
    }
}
