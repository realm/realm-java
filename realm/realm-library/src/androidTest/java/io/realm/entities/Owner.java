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

import io.realm.RealmList;
import io.realm.RealmObject;

public class Owner extends RealmObject {
    private String name;
    private RealmList<Dog> dogs;
    private Cat cat;

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

    public RealmList<Dog> getDogs() {
        return realmGetter$dogs();
    }

    public void setDogs(RealmList<Dog> dogs) {
        realmSetter$dogs(dogs);
    }

    public RealmList<Dog> realmGetter$dogs() {
        return dogs;
    }

    public void realmSetter$dogs(RealmList<Dog> dogs) {
        this.dogs = dogs;
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
