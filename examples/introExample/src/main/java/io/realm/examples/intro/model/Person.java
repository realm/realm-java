/*
 * Copyright 2018 Realm Inc.
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

package io.realm.examples.intro.model;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

// Your model just have to extend RealmObject.
// This will inherit an annotation which produces proxy getters and setters for all fields.
// It is also possible to use @RealmClass annotation, and implement RealmModel interface.
public class Person extends RealmObject {

    // All fields are by default persisted.
    private int age;

    // Adding an index makes queries execute faster on that field.
    @Index
    private String name;

    // Primary keys are optional, but it allows identifying a specific object
    // when Realm writes are instructed to update if the object already exists in the Realm
    @PrimaryKey
    private long id;

    // Other objects in a one-to-one relation must also implement RealmModel, or extend RealmObject
    private Dog dog;

    // One-to-many relations is simply a RealmList of the objects which also implements RealmModel
    private RealmList<Cat> cats;

    // It is also possible to have list of primitive types (long, String, Date, byte[], etc.)
    private RealmList<String> phoneNumbers;

    // You can instruct Realm to ignore a field and not persist it.
    @Ignore
    private int tempReference;

    // Let your IDE generate getters and setters for you!
    // Or if you like you can even have public fields and no accessors! See Dog.java and Cat.java
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Dog getDog() {
        return dog;
    }

    public void setDog(Dog dog) {
        this.dog = dog;
    }

    public RealmList<Cat> getCats() {
        return cats;
    }

    public void setCats(RealmList<Cat> cats) {
        this.cats = cats;
    }

    public int getTempReference() {
        return tempReference;
    }

    public void setTempReference(int tempReference) {
        this.tempReference = tempReference;
    }

    public RealmList<String> getPhoneNumbers() {
        return phoneNumbers;
    }

    public void setPhoneNumbers(RealmList<String> phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }
}
