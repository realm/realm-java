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

package io.realm.entities;

import org.bson.types.ObjectId;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;
import io.realm.objectid.NullPrimaryKey;

public class PrimaryKeyAsObjectId extends RealmObject implements NullPrimaryKey<ObjectId, String> {

    public static final String CLASS_NAME = "PrimaryKeyAsObjectId";
    public static final String FIELD_PRIMARY_KEY = "name";
    public static final String FIELD_ID = "id";

    @PrimaryKey
    private ObjectId id;
    private String name;

    public PrimaryKeyAsObjectId() {
    }

    public PrimaryKeyAsObjectId(ObjectId id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public ObjectId getId() {
        return id;
    }

    @Override
    public void setId(ObjectId id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }
}
