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

package io.realm.examples;

import io.realm.Realm;
import io.realm.RealmMigration;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.examples.junkyard.DynamicRealmObject;
import io.realm.examples.junkyard.MigrationIterator;
import io.realm.internal.migration.Migration;
import io.realm.internal.migration.RealmObjectSpec;

/**
 * Examples of different usages of the Migration API. Hopefully self explanatory and possible to extrapolate missing
 * methods.
 */
public class LinearMigrationAPIExamples {

    //
    // Realm model classes
    //

    public class Old extends RealmObject {
        private String x;
        private int y;
        private boolean z;
    }

    public class Updated extends RealmObject {
        private String x;
        private int y;
        private boolean z;
        private String w;

    }

    public class New extends RealmObject {
        @Index
        private String foo;
    }

    //
    // Migration examples
    //
    public class ClassMigrations implements RealmMigration {

        @Override
        @Deprecated
        public long execute(Realm realm, long version) {
            return 0;
        }

        @Override
        public long migrate(Migration realmSpec, int version) {

            // Adding a new object requires creating a new tablespec. using builder pattern
            RealmObjectSpec manual = new RealmObjectSpec.Builder("New")
                    .addField("foo", String.class, Index.class) // This is how you normally think of an field, ie. name, type, otherstuff
                    .build();

            // Or manipulating the old
            RealmObjectSpec oldSpec = realmSpec.getClass("New");

            // or automatically creating a spec from a class definition
            RealmObjectSpec autoSpec = RealmObjectSpec.fromClass(New.class);

            // Usecase 1: Adding new classes
            realmSpec.addClass(manual);
            realmSpec.addClass(autoSpec);    // This should fail as spec already exists for that name

            // Use case 2: Migrating class definitions
            realmSpec.replaceSpec(autoSpec); // You have to specify overriding/replacing old spec.

            // Use case 3: Removing classes
            realmSpec.removeClass(autoSpec); // Remove table
            realmSpec.removeClass("New");    // Shorthand if you don't want to find table

            // Use case 4: Renaming classes
            realmSpec.renameClass("Old", "New");

            return 1;
        }
    }


    public class FieldMigrations implements RealmMigration {

        @Override
        @Deprecated
        public long execute(Realm realm, long version) {
            return 0;
        }

        @Override
        public long migrate(Migration realmSpec, int version) {

            RealmObjectSpec classSpec = realmSpec.getClass("Old");

            // Use case 1: Add field
            classSpec.addField("w", String.class);

            // Use case 2: Delete field
            classSpec.deleteField("x");

            // Use case 3: Rename field
            classSpec.renameField("x", "y");

            // Use case 4: switch type
            // We would have to specify a LOT of rules here, eg. for bool -> int and str -> int
            classSpec.setType("x", int.class);

            // Use case 5/6: Merge and Split. This should be supported through enumerating the objects. Trying to
            // add typesafe method for this will result in n^2 methods in the number of types, eg. well above 100 methods

            // Challenge: Best way to create iterator?
            // Why does Cocoa have both old and new object definition? Doesn't that imply that all data is duplicated?
            realmSpec.forEach("New", new MigrationIterator() {
                @Override
                public void handle(DynamicRealmObject obj) {
                    // Use case 5: Merge. Require 1) create new column 2) merge data there 3) delete old data
                    obj.setString("w", obj.getString("x") + obj.getInt("y"));

                    // Use case 6: Split. Require 1) create new column2 2) split data 3) delete old data
                    obj.setString("y", obj.getString("x").substring(0, 2));
                    obj.setString("x", obj.getString("x").substring(2));
                }

                @Override
                public void next(DynamicRealmObject currentObject, DynamicRealmObject newObject) {
                    // ignore
                }
            });

            // Use case 7: Move field to another class
            RealmObjectSpec otherClass = realmSpec.getClass("Other");
            classSpec.moveFieldToClass("x", otherClass); // Move field to other class by classpec
            classSpec.moveFieldToClass("x", "Other"); // Move field to other class using name

            return 1;
        }
    }


    // This example:
    // v1) adds a new object
    // v2),renames an old property and add a new one to the old object
    public class ExampleMigration1 implements RealmMigration {

        @Override
        @Deprecated
        public long execute(Realm realm, long version) {
            return 0;
        }

        @Override
        public long migrate(Migration oldRealm, int version) {

            if (version < 1) {
                oldRealm.addClass(RealmObjectSpec.fromClass(New.class)); // Highly risky, should be reconsidered.
            }

            if (version < 2) {
                oldRealm.getClass("New")
                        .renameField("oldField", "newField")
                        .addField("thirdField", boolean.class);
            }

            return 2;
        }
    }
}
