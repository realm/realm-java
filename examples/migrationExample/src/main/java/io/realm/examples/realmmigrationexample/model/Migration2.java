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
import io.realm.RealmList;
import io.realm.RealmMigration;
import io.realm.RealmModifier;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;
import io.realm.annotations.Required;

/**
 * Example of migrating a Realm file from version 0 (initial version) to it's last version (version 3).
 */
public class Migration2 implements RealmMigration {

    @Override
    public void migrate(final DynamicRealm realm, long oldVersion, long newVersion) {

        // Primary design criteria so far has been:
        // 1. Easy to write for users (they hate migrations already, no reason to make it worse)
        // 2. Easy to read (it should be easy to match a model class with it's migration
        // 3. As close to the Object store model as possible (try to hide underlying Realm datatypes as much as possible)


        // Most common migration case is adding a new class/fields. Our current Migration example doesn't really do
        // this. It mostly showcase advanced features.

        // Current API
        // + More autocomplete power
        // + Sensible defaults (addStringField is nullable, addIntField uses primitive int)
        // - Have to introduce RealmModifier enum to express Nullable. Cannot reference annotations.
        // - User have to learn the defaults for each field type
        RealmSchema schema = realm.getSchema();
        schema.createObjectSchema("Foo")
                .addStringField("name")                                                                     // String (Nullable as default)
                .addIntField("age")                                                                         // int (not nullable as default)
                .addBooleanField("married", EnumSet.of(RealmModifier.NULLABLE, RealmModifier.REQUIRED))     // Boolean (change nullable from default)
                .addBooleanField("children")
                    .setNullable("children")                                                                // Instead of using EnumSet
                .addListField("pets", schema.getObjectSchema("Pet"));

        // Most common migration is adding
        // + Simple API = One (overloaded) method
        // + Types match the Java types 1:1
        // + Can reference annotations directly. No RealmModifier enum
        // + Less typing required for the advanced cases.
        // - More typing that previous version for the simple version.
        // - Has readability decreased a bit?
        RealmSchema schema2 = realm.getSchema();
        schema2.createObjectSchema("Foo")
                .addField(String.class, "name")                                     // String (Nullable as default)
                .addField(int.class, "age")                                         // int, use Integer.class for nullability
                .addField(Boolean.class, "married", Required.class)                 // Boolean that is required
                .addField(Boolean.class, "children")                                // No need for Nullable. It comes from the class.
                .addField(RealmList.class, "pets", schema.getObjectSchema("Pet"));
    }
}
