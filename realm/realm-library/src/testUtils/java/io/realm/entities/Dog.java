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
import io.realm.annotations.Index;

public class Dog extends RealmObject {

    public static final String CLASS_NAME = "Dog";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_AGE = "age";
    public static final String FIELD_HEIGHT = "height";
    public static final String FIELD_WEIGHT = "weight";
    public static final String FIELD_BIRTHDAY = "birthday";
    public static final String FIELD_HAS_TAIL = "hasTail";

    @Index
    private String name;
    private long age;
    private float height;
    private double weight;
    private boolean hasTail;
    private Date birthday;
    private Owner owner;

    public Dog() {
    }

    public Dog(String name) {
        this.name = name;
    }
    public Dog(String name, long age) {
        this.name = name;
        this.age = age;
    }

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public boolean isHasTail() {
        return hasTail;
    }

    public void setHasTail(boolean hasTail) {
        this.hasTail = hasTail;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public long getAge() {
        return age;
    }

    public void setAge(long age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
