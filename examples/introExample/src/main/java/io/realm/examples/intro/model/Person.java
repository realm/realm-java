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

package io.realm.examples.intro.model;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;

// Your model just have to extend RealmObject.
// This will inherit an annotation which produces proxy getters and setters for all fields.
public class Person extends RealmObject {

    // All fields are by default persisted.
    public String name;
    public int age;

    // Other objects in a one-to-one relation must also subclass RealmObject
    public Dog dog;

    // One-to-many relations is simply a RealmList of the objects which also subclass RealmObject
    public RealmList<Cat> cats;

    // You can instruct Realm to ignore a field and not persist it.
    @Ignore
    public int tempReference;

    public long id;
}
