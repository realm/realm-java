/*
 * Copyright 2016 Realm Inc.
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

public class PrimaryKeyAsBoxedByte extends RealmObject implements NullPrimaryKey<Byte, String> {

    public static final String CLASS_NAME = "PrimaryKeyAsBoxedByte";
    public static final String FIELD_PRIMARY_KEY = "id";

    @PrimaryKey
    private Byte id;

    private String name;

    public PrimaryKeyAsBoxedByte() {}
    public PrimaryKeyAsBoxedByte(Byte id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public Byte getId() {
        return id;
    }

    @Override
    public void setId(Byte id) {
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
