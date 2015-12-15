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

import java.util.Date;

import io.realm.RealmObject;

public class Cat extends RealmObject {
    private String name;
    private long age;
    private float height;
    private double weight;
    private boolean hasTail;
    private Date birthday;
    private Owner owner;
    private DogPrimaryKey scaredOfDog;

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

    public long getAge() {
        return realmGetter$age();
    }

    public void setAge(long age) {
        realmSetter$age(age);
    }

    public long realmGetter$age() {
        return age;
    }

    public void realmSetter$age(long age) {
        this.age = age;
    }

    public float getHeight() {
        return realmGetter$height();
    }

    public void setHeight(float height) {
        realmSetter$height(height);
    }

    public float realmGetter$height() {
        return height;
    }

    public void realmSetter$height(float height) {
        this.height = height;
    }

    public double getWeight() {
        return realmGetter$weight();
    }

    public void setWeight(double weight) {
        realmSetter$weight(weight);
    }

    public double realmGetter$weight() {
        return weight;
    }

    public void realmSetter$weight(double weight) {
        this.weight = weight;
    }

    public boolean getHasTail() {
        return realmGetter$hasTail();
    }

    public void setHasTail(boolean hasTail) {
        realmSetter$hasTail(hasTail);
    }

    public boolean realmGetter$hasTail() {
        return hasTail;
    }

    public void realmSetter$hasTail(boolean hasTail) {
        this.hasTail = hasTail;
    }

    public Date getBirthday() {
        return realmGetter$birthday();
    }

    public void setBirthday(Date birthday) {
        realmSetter$birthday(birthday);
    }

    public Date realmGetter$birthday() {
        return birthday;
    }

    public void realmSetter$birthday(Date birthday) {
        this.birthday = birthday;
    }

    public Owner getOwner() {
        return realmGetter$owner();
    }

    public void setOwner(Owner owner) {
        realmSetter$owner(owner);
    }

    public Owner realmGetter$owner() {
        return owner;
    }

    public void realmSetter$owner(Owner owner) {
        this.owner = owner;
    }

    public DogPrimaryKey getScaredOfDog() {
        return realmGetter$scaredOfDog();
    }

    public void setScaredOfDog(DogPrimaryKey scaredOfDog) {
        realmSetter$scaredOfDog(scaredOfDog);
    }

    public DogPrimaryKey realmGetter$scaredOfDog() {
        return scaredOfDog;
    }

    public void realmSetter$scaredOfDog(DogPrimaryKey scaredOfDog) {
        this.scaredOfDog = scaredOfDog;
    }
}
