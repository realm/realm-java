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

import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;


public class PrimaryKeyAsUUID extends RealmObject {

    public static final String CLASS_NAME = "PrimaryKeyAsUUID";
    public static final String FIELD_PRIMARY_KEY = "id";
    public static final String FIELD_NAME = "name";

    @PrimaryKey
    private UUID id;

    private String name;

    public PrimaryKeyAsUUID() {}
    public PrimaryKeyAsUUID(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
