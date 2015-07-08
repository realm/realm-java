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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.internal.ImplicitTransaction;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.RealmProxyMediator;
import io.realm.internal.Table;
import io.realm.internal.Util;

/**
 * This class is able to merge different RealmProxyMediators, so they look like one.
 */
public class CompositeMediator extends RealmProxyMediator {

    Map<Class<? extends RealmObject>, RealmProxyMediator> mediators = new HashMap<Class<? extends RealmObject>,
            RealmProxyMediator>();

    public void addMediator(RealmProxyMediator mediator) {
        for (Class<? extends RealmObject> realmClass : mediator.getModelClasses()) {
            mediators.put(realmClass, mediator);
        }
    }

    @Override
    public Table createTable(Class<? extends RealmObject> clazz, ImplicitTransaction transaction) {
        RealmProxyMediator mediator = getMediator(clazz);
        return mediator.createTable(clazz, transaction);
    }

    @Override
    public void validateTable(Class<? extends RealmObject> clazz, ImplicitTransaction transaction) {
        RealmProxyMediator mediator = getMediator(clazz);
        mediator.validateTable(clazz, transaction);
    }

    @Override
    public List<String> getFieldNames(Class<? extends RealmObject> clazz) {
        RealmProxyMediator mediator = getMediator(clazz);
        return mediator.getFieldNames(clazz);
    }

    @Override
    public String getTableName(Class<? extends RealmObject> clazz) {
        RealmProxyMediator mediator = getMediator(clazz);
        return mediator.getTableName(clazz);
    }

    @Override
    public <E extends RealmObject> E newInstance(Class<E> clazz) {
        RealmProxyMediator mediator = getMediator(clazz);
        return mediator.newInstance(clazz);
    }

    @Override
    public List<Class<? extends RealmObject>> getModelClasses() {
        List<Class<? extends RealmObject>> list = new ArrayList<Class<? extends RealmObject>>();
        for (RealmProxyMediator mediator : mediators.values()) {
            list.addAll(mediator.getModelClasses());
        }
        return list;
    }

    @Override
    public Map<String, Long> getColumnIndices(Class<? extends RealmObject> clazz) {
        RealmProxyMediator mediator = getMediator(clazz);
        return mediator.getColumnIndices(clazz);
    }

    @Override
    public <E extends RealmObject> E copyOrUpdate(Realm realm, E object, boolean update, Map<RealmObject,
            RealmObjectProxy> cache) {
        RealmProxyMediator mediator = getMediator(Util.getOriginalModelClass(object.getClass()));
        return mediator.copyOrUpdate(realm, object, update, cache);
    }

    @Override
    public <E extends RealmObject> E createOrUpdateUsingJsonObject(Class<E> clazz, Realm realm, JSONObject json,
                                                                   boolean update) throws JSONException {
        RealmProxyMediator mediator = getMediator(clazz);
        return mediator.createOrUpdateUsingJsonObject(clazz, realm, json, update);
    }

    @Override
    public <E extends RealmObject> E createUsingJsonStream(Class<E> clazz, Realm realm, JsonReader reader) throws
            IOException {
        RealmProxyMediator mediator = getMediator(clazz);
        return mediator.createUsingJsonStream(clazz, realm, reader);
    }

    // Returns the mediator for a given model class (not RealmProxy) or throws exception
    private RealmProxyMediator getMediator(Class<? extends RealmObject> clazz) {
        RealmProxyMediator mediator = mediators.get(clazz);
        if (mediator == null) {
            throw new IllegalArgumentException(clazz.getSimpleName() + " is not part of the schema for this Realm");
        }
        return mediator;
    }
}
