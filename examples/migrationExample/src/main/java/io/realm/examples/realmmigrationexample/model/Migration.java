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

package io.realm.examples.realmmigrationexample.model;

import java.util.EnumSet;

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.RealmMigration;
import io.realm.RealmModifier;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

/**
 * Example of migrating a Realm file from version 0 (initial version) to it's last version (version 3).
 */
public class Migration implements RealmMigration {

    @Override
    public void migrate(final DynamicRealm realm, long oldVersion, long newVersion) {
        // During a migration, a DynamicRealm is exposed. A DynamicRealm is an untyped variant of a normal Realm, but
        // otherwise have the same object creation and query capabilities.
        // A DynamicRealm uses Strings instead of Class references which is needed as the Classes might not even
        // exist or have a wrong name.

        // The Realm schema can be used to create, modify or delete classes and their fields.
        RealmSchema schema = realm.getSchema();

        /*
            // Version 0
            class Person
                @Required
                String firstName;
                @Required
                String lastName;
                int    age;

            // Version 1
            class Person
                @Required
                String fullName;        // combine firstName and lastName into single field.
                int age;
        */

        // Migrate from version 0 to version 1
        if (oldVersion == 0) {
            RealmObjectSchema personSchema = schema.getObjectSchema("Person");

            // Combine 'firstName' and 'lastName' in a new field called 'fullName'
            personSchema.addStringField("fullName", EnumSet.of(RealmModifier.REQUIRED))
                    .forEach(new RealmObjectSchema.Iterator() {
                        @Override
                        public void next(DynamicRealmObject obj) {
                            obj.set("fullName", obj.getString("firstName") + " " + obj.getString("lastName"));
                        }
                    })
                    .removeField("firstName")
                    .removeField("lastName");
            oldVersion++;
        }

        /*
            // Version 2
                class Pet                   // add a new model class
                    @Required
                    String name;
                    String type;

                class Person
                    @Required
                    String fullName;
                    int age;
                    RealmList<Pet> pets;    // add an array property

        */
        // Migrate from version 1 to version 2
        if (oldVersion == 1) {

            // Create a new class
            RealmObjectSchema petSchema = schema.createClass("Pet")
                    .addStringField("name", EnumSet.of(RealmModifier.REQUIRED))
                    .addStringField("type", EnumSet.of(RealmModifier.REQUIRED));

            // Add a new field to and old class and populate it with initial data
            schema.getObjectSchema("Person")
                .addListField("pets", petSchema)
                .forEach(new RealmObjectSchema.Iterator() {
                    @Override
                    public void next(DynamicRealmObject obj) {
                        if (obj.getString("fullName").equals("JP McDonald")) {
                            DynamicRealmObject pet = realm.createObject("Pet");
                            pet.setString("name", "Jimbo");
                            pet.setString("type", "dog");
                            obj.getList("pets").add(pet);
                        }
                    }
                });
            oldVersion++;
        }

        /*
            // Version 3
                class Pet
                    @Required
                    String name;
                    int type;               // type becomes int

                class Person
                    String fullName;        // fullName is nullable now
                    RealmList<Pet> pets;    // age and pets re-ordered
                    int age;
        */
        // Migrate from version 2 to version 3
        if (oldVersion == 2) {
            RealmObjectSchema personSchema = schema.getObjectSchema("Person");
            personSchema.setNullable("fullName", true); // fullName is nullable now.

            // Change type from String to int
            schema.getObjectSchema("Pet")
                .addIntField("type_tmp")
                .forEach(new RealmObjectSchema.Iterator() {
                    @Override
                    public void next(DynamicRealmObject obj) {
                        String oldType = obj.getString("type");
                        if (oldType.equals("dog")) {
                            obj.setLong("type_tmp", 1);
                        } else if (oldType.equals("cat")) {
                            obj.setInt("type_tmp", 2);
                        } else if (oldType.equals("hamster")) {
                            obj.setInt("type_tmp", 3);
                        }
                    }
                })
                .removeField("type")
                .renameField("type_tmp", "type");
            oldVersion++;
        }
    }
}
