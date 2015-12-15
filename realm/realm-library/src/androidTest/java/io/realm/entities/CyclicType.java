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

public class CyclicType extends RealmObject {

    private String name;
    private CyclicType object;
    private CyclicType otherObject;
    private RealmList<CyclicType> objects;

    public CyclicType() {
    }

    public CyclicType(String name) {
        this.name = name;
    }

    public String getName() {
        return realmGetter$name();
    }

    public void setName(String name) {
        realmSetter$name(name);
    }

    public String realmGetter$name() {
        return name;
    }

    public void realmSetter$name(String name) {
        this.name = name;
    }

    public CyclicType getObject() {
        return realmGetter$object();
    }

    public void setObject(CyclicType object) {
        realmSetter$object(object);
    }

    public CyclicType realmGetter$object() {
        return object;
    }

    public void realmSetter$object(CyclicType object) {
        this.object = object;
    }

    public CyclicType getOtherObject() {
        return realmGetter$otherObject();
    }

    public void setOtherObject(CyclicType otherObject) {
        realmSetter$otherObject(otherObject);
    }

    public CyclicType realmGetter$otherObject() {
        return otherObject;
    }

    public void realmSetter$otherObject(CyclicType otherObject) {
        this.otherObject = otherObject;
    }

    public RealmList<CyclicType> getObjects() {
        return realmGetter$objects();
    }

    public void setObjects(RealmList<CyclicType> objects) {
        realmSetter$objects(objects);
    }

    public RealmList<CyclicType> realmGetter$objects() {
        return objects;
    }

    public void realmSetter$objects(RealmList<CyclicType> objects) {
        this.objects = objects;
    }
}
