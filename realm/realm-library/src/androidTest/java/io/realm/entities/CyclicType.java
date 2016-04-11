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

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;

public class CyclicType extends RealmObject {

    public static final String FIELD_NAME = "name";
    public static final String FIELD_ID = "id";
    public static final String FIELD_DATE = "date";

    private long id;
    private String name;
    private Date date;
    private CyclicType object;
    private CyclicType otherObject;
    private RealmList<CyclicType> objects;

    public CyclicType() {
    }

    public CyclicType(String name) {
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

    public CyclicType getObject() {
        return object;
    }

    public void setObject(CyclicType object) {
        this.object = object;
    }

    public RealmList<CyclicType> getObjects() {
        return objects;
    }

    public void setObjects(RealmList<CyclicType> objects) {
        this.objects = objects;
    }

    public CyclicType getOtherObject() {
        return otherObject;
    }

    public void setOtherObject(CyclicType otherObject) {
        this.otherObject = otherObject;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
