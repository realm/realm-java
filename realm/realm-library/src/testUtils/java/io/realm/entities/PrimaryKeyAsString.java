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

package io.realm.entities;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.objectid.NullPrimaryKey;

public class PrimaryKeyAsString extends RealmObject {

    public static final String CLASS_NAME = "PrimaryKeyAsString";
    public static final String FIELD_PRIMARY_KEY = "name";
    public static final String FIELD_ID = "id";

    @PrimaryKey
    private String name;

    private long id;

    public PrimaryKeyAsString() {}
    public PrimaryKeyAsString(String name, long id) {
        this.name = name;
        this.id = id;
    }

    public PrimaryKeyAsString(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
