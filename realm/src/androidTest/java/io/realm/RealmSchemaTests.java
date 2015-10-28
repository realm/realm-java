/*
 * Copyright 2015 Realm Inc.
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
 *
 */

package io.realm;

import android.test.AndroidTestCase;
import android.util.Log;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import io.realm.entities.AllJavaTypes;
import io.realm.entities.Owner;

public class RealmSchemaTests extends AndroidTestCase {

    public static final String CLASS_ALL_JAVA_TYPES = "AllJavaTypes";
    private DynamicRealm realm;
    private RealmSchema realmSchema;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        RealmConfiguration realmConfig = new RealmConfiguration.Builder(getContext())
                .schema(AllJavaTypes.class, Owner.class)
                .build();
        Realm.deleteRealm(realmConfig);
        Realm.getInstance(realmConfig).close(); // create Schema
        this.realm = DynamicRealm.getInstance(realmConfig);
        realmSchema = this.realm.getSchema();
        this.realm.beginTransaction();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        realm.cancelTransaction();
        realm.close();
    }

    public void testGetAllClasses() {
        Set<RealmObjectSchema> objectSchemas = realmSchema.getAllClasses();
        assertEquals(5, objectSchemas.size());

        List<String> expectedTables = Arrays.asList(CLASS_ALL_JAVA_TYPES, "Owner", "Cat", "Dog", "DogPrimaryKey");
        for (RealmObjectSchema objectSchema : objectSchemas) {
            if (!expectedTables.contains(objectSchema.getClassName())) {
                fail(objectSchema.getClassName() + " was not found");
            }
        }
    }

    public void testCreateClass() {
        realmSchema.createClass("Foo");
        assertTrue(realmSchema.hasClass("Foo"));
    }

    public void testCreateClassInvalidNameThrows() {
        String[] names = { null, "", TestHelper.getRandomString(57) };

        for (String name : names) {
            try {
                realmSchema.createClass(name);
            } catch (IllegalArgumentException expected) {
            }
            assertFalse(String.format("'%s' failed", name), realmSchema.hasClass(name));
        }
    }

    public void testGetClass() {
        RealmObjectSchema objectSchema = realmSchema.getClass(CLASS_ALL_JAVA_TYPES);
        assertNotNull(objectSchema);
        assertEquals(CLASS_ALL_JAVA_TYPES, objectSchema.getClassName());
    }

    public void testGetClassNotInSchema() {
        assertNull(realmSchema.getClass("Foo"));
    }

    public void testRenameClass() {
        realmSchema.renameClass("Owner", "Owner2");
        assertFalse(realmSchema.hasClass("Owner"));
        assertTrue(realmSchema.hasClass("Owner2"));
    }

    public void testRenameClassInvalidArgumentsThrows() {
        try {
            realmSchema.renameClass(null, CLASS_ALL_JAVA_TYPES);
            fail();
        } catch (IllegalArgumentException expected) {
        }

        try {
            realmSchema.renameClass(CLASS_ALL_JAVA_TYPES, null);
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testRemoveClass() {
        realmSchema.removeClass(CLASS_ALL_JAVA_TYPES);
        assertFalse(realmSchema.hasClass(CLASS_ALL_JAVA_TYPES));
    }

    public void testRemoveClassInvalidClassNameThrows() {
        try {
            realmSchema.removeClass("Foo");
            fail();
        } catch (IllegalArgumentException expected) {
        }

        try {
            realmSchema.removeClass(null);
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    // Test that it if { A -> B  && B -> A } you should remove the individual fields first before removing the entire
    // class. This also include transitive dependencies :/
    public void testRemoveClassWithReferencesThrows() {
        try {
            realmSchema.removeClass("Cat");
            fail();
        } catch (IllegalStateException ignored) {
        }

        realmSchema.getClass("Owner").removeField("cat");
        realmSchema.getClass("Cat").removeField("owner");
        assertFalse(realmSchema.getClass("Cat").hasField("owner"));
        assertFalse(realmSchema.getClass("Owner").hasField("cat"));
        try {
            realmSchema.removeClass("Cat");

        } catch (IllegalStateException ignored) {
        }
        assertFalse(realmSchema.hasClass("Cat"));
    }
}
