/*
 * Copyright 2014 Realm Inc.
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import io.realm.annotations.RealmClass;
import io.realm.internal.Row;

/**
 * In Realm you define your model classes by subclassing RealmObject and adding fields to be
 * persisted. You then create your objects within a Realm, and use your custom subclasses instead
 * of using the RealmObject class directly.
 *
 * An annotation processor will create a proxy class for your RealmObject subclass. The getters and
 * setters should not contain any custom code of logic as they are overridden as part of the annotation
 * process.
 *
 * @see Realm#createObject(Class)
 */

@RealmClass
public abstract class RealmObject {

    protected Row row;
    protected Realm realm;

    protected Realm getRealm() {
        return realm;
    }

    protected void setRealm(Realm realm) {
        this.realm = realm;
    }

    protected Row realmGetRow() {
        return row;
    }

    protected void realmSetRow(Row row) {
        this.row = row;
    }

    protected void populateFromJsonObject(JSONObject json) throws JSONException {
        throw new IllegalStateException("Only use this method on objects created or fetched in a Realm. Realm.createObject() or Realm.where()");
    }

    protected void populateFromJsonStream(InputStream inputStream) throws IOException {
        throw new IllegalStateException("Only use this method on objects created or fetched in a Realm. Realm.createObject() or Realm.where()");
    }
}
