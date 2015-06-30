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

import io.realm.RealmMigration;
import io.realm.dynamic.DynamicRealmObject;
import io.realm.dynamic.RealmObjectSchema;
import io.realm.dynamic.RealmSchema;
import io.realm.internal.ColumnType;
import io.realm.internal.Table;

/**
 * Migration code used to migrate a previous schema to the newest one
 * TODO Show how to set Indexed, Primary key as well
 */
public class Migration implements RealmMigration {

    @Override
    public void migrate(RealmSchema schema, long oldVersion, long newVersion) {
        /*
            // Version 0
            class Person
                String fullName;
                String lastName;
                int    age;

            // Version 1
            class Person
                String fullName;        // combine firstName and lastName into single field
                int age;
        */
        if (oldVersion == 0) {
            schema.getClass("Person")
                    .addString("fullName")
                    .forEach(new RealmObjectSchema.Iterator() {
                        @Override
                        public void next(DynamicRealmObject obj) {
                            obj.setString("fullName", obj.getString("firstName") + " " + obj.getString("lastName"));
                        }
                    })
                    .removeField("firstName")
                    .removeField("lastName");
            oldVersion++;
        }

        /*
            // Version 2
            class Pet                   // add a new model class
                String name;
                String type;

            class Person
                String fullName;
                int age;
                RealmList<Pet> pets;    // add a RealmList field
        */
        if (oldVersion == 1) {

            final RealmObjectSchema petSchema = schema.addClass("Pet")
                    .addString("name")
                    .addString("type");

            schema.getClass("Person")
                    .addList("pets", petSchema)
                    .forEach(new RealmObjectSchema.Iterator() { // TODO Should we add query support now?
                        @Override
                        public void next(DynamicRealmObject obj) {
                            if (obj.getString("fullName").equals("JP McDonald")) {
                                DynamicRealmObject pet = petSchema.createObject();
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
                @Index
                String name;            // name is indexed
                int type;               // type becomes int

            class Person
                String fullName;
                RealmList<Pet> pets;    // age and pets re-ordered
                int age;
        */
        if (oldVersion == 2) {
            schema.getClass("Pet")
                    .addIndex("name")
                    .addInt("typeTmp")
                    .forEach(new RealmObjectSchema.Iterator() {
                        @Override
                        public void next(DynamicRealmObject obj) {
                            String type = obj.getString("type");
                            if (type.equals("dog")) {
                                obj.setInt("typeTmp", 1);
                            } else if (type.equals("cat")) {
                                obj.setInt("typeTmp", 2);
                            } else if (type.equals("hamster")) {
                                obj.setInt("typeTmp", 3);
                            }
                        }
                    })
                    .removeField("type")
                    .renameField("typeTmp", "type");

            // TODO 4 instructions to change a type, is that acceptable? But the alternative would be an
            // API explosion or to expose something like a "RealmField" which would mean we would have to expose
            // the internal Realm types
            // schema.addField(new RealmField("type", ColumnType.INTEGER))
            oldVersion++;
        }
    }
}
