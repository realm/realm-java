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

public class PrimaryKeyMix extends RealmObject {

    @PrimaryKey
    private long id;

    private OwnerPrimaryKey dogOwner;
    private Cat cat;

    public PrimaryKeyMix() {

    }

    public PrimaryKeyMix(long id) {
        this.id = id;
    }

    public long getId() {
        return realmGetter$id();
    }

    public void setId(long id) {
        realmSetter$id(id);
    }

    public long realmGetter$id() {
        return id;
    }

    public void realmSetter$id(long id) {
        this.id = id;
    }

    public OwnerPrimaryKey getDogOwner() {
        return realmGetter$dogOwner();
    }

    public void setDogOwner(OwnerPrimaryKey dogOwner) {
        realmSetter$dogOwner(dogOwner);
    }

    public OwnerPrimaryKey realmGetter$dogOwner() {
        return dogOwner;
    }

    public void realmSetter$dogOwner(OwnerPrimaryKey dogOwner) {
        this.dogOwner = dogOwner;
    }

    public Cat getCat() {
        return realmGetter$cat();
    }

    public void setCat(Cat cat) {
        realmSetter$cat(cat);
    }

    public Cat realmGetter$cat() {
        return cat;
    }

    public void realmSetter$cat(Cat cat) {
        this.cat = cat;
    }
}
