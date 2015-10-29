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

package io.realm.entities;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class CyclicTypePrimaryKey extends RealmObject {

    @PrimaryKey
    private long id;
    private String name;
    private CyclicTypePrimaryKey object;
    private RealmList<CyclicTypePrimaryKey> objects;

    public CyclicTypePrimaryKey() {
    }

    public CyclicTypePrimaryKey(long id) {
        this.id = id;
    }

    public CyclicTypePrimaryKey(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CyclicTypePrimaryKey getObject() {
        return object;
    }

    public void setObject(CyclicTypePrimaryKey object) {
        this.object = object;
    }

    public RealmList<CyclicTypePrimaryKey> getObjects() {
        return objects;
    }

    public void setObjects(RealmList<CyclicTypePrimaryKey> objects) {
        this.objects = objects;
    }
}
