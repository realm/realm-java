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

import io.realm.DirectRealmMigration;
import io.realm.Realm;
import io.realm.RealmMigration;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.examples.junkyard.DirectMigrationIterator;
import io.realm.examples.junkyard.DynamicRealmObject;
import io.realm.internal.migration.Migration;

/**
 * Examples of different usages of the Migration API. Hopefully self explanatory and possible to extrapolate missing
 * methods.
 */
public class DirectMigrationAPIExamples {

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
    public class ClassMigrations implements DirectRealmMigration {

        @Override
        public long migrate(int version, Migration migration) {

            // Usecase 1: Adding new classes -> Do nothing
            // Use case 2: Migrating class definitions -> Do nothing
            // Use case 3: Removing classes -> Do nothing
            // Use case 4: Renaming classes / Not supported by Cocoa
            migration.renameClass("Old", "New");

            return 0;
        }
    }


    public class FieldMigrations implements DirectRealmMigration {

        @Override
        public long migrate(int currentVersion, Migration migration) {

            // Use case 1: Add field -> Do nothing
            // Use case 2: Delete field -> Do nothing
            // Use case 3: Rename field
            migration.getClass("foo").renameField("x", "y");

            // Use case 4: switch type
            migration.getClass("foo").setType("y", int.class);

            // Use case 5/6: Merge and Split. This should be supported through enumerating the objects.
            // The above changes will probably use this mechanism behind the scenes
            migration.forEach("foo", new DirectMigrationIterator() {
                @Override
                public void next(DynamicRealmObject currentObject, DynamicRealmObject newObject) {
                    newObject.setString("splitA", currentObject.getString("x").substring(0, 2));
                    newObject.setString("splitB", currentObject.getString("x").substring(2));
                }
            });

            migration.forEach("foo", new DirectMigrationIterator() {
                @Override
                public void next(DynamicRealmObject currentObject, DynamicRealmObject newObject) {
                    newObject.setString("merge", currentObject.getString("x").substring(0, 2));
                }
            });

            // Use case 7: Move field to another class
            migration.getClass("foo").moveFieldToClass("x", "bar"); // If primary key will match on those, otherwise

            return 0;
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
                // Do nothing
            }

            if (version < 2) {
                oldRealm.getClass("New").renameField("oldField", "newField");
            }

            return 2;
        }
    }
}
