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
 */

package io.realm;

import android.test.AndroidTestCase;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import io.realm.entities.AllJavaTypes;
import io.realm.entities.Owner;
import io.realm.internal.Util;

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
        Set<RealmObjectSchema> objectSchemas = realmSchema.getAll();
        assertEquals(5, objectSchemas.size());

        List<String> expectedTables = Arrays.asList(CLASS_ALL_JAVA_TYPES, "Owner", "Cat", "Dog", "DogPrimaryKey");
        for (RealmObjectSchema objectSchema : objectSchemas) {
            if (!expectedTables.contains(objectSchema.getClassName())) {
                fail(objectSchema.getClassName() + " was not found");
            }
        }
    }

    public void testCreateClass() {
        realmSchema.create("Foo");
        assertTrue(realmSchema.contains("Foo"));
    }

    public void testCreateClassInvalidNameThrows() {
        String[] names = { null, "", TestHelper.getRandomString(57) };

        for (String name : names) {
            try {
                realmSchema.create(name);
            } catch (IllegalArgumentException ignored) {
            }
            assertFalse(String.format("'%s' failed", name), realmSchema.contains(name));
        }
    }

    public void testGetClass() {
        RealmObjectSchema objectSchema = realmSchema.get(CLASS_ALL_JAVA_TYPES);
        assertNotNull(objectSchema);
        assertEquals(CLASS_ALL_JAVA_TYPES, objectSchema.getClassName());
    }

    public void testGetClassNotInSchema() {
        assertNull(realmSchema.get("Foo"));
    }

    public void testRenameClass() {
        realmSchema.rename("Owner", "Owner2");
        assertFalse(realmSchema.contains("Owner"));
        assertTrue(realmSchema.contains("Owner2"));
    }

    public void testRenameClassInvalidArgumentsThrows() {
        String[] illegalNames = new String[] { null, "" };

        // Test as first parameters
        for (String illegalName : illegalNames) {
            try {
                realmSchema.rename(CLASS_ALL_JAVA_TYPES, illegalName);
                fail(illegalName + " should throw an exception");
            } catch (IllegalArgumentException ignored) {
            }
        }

        // Test as last parameter
        for (String illegalName : illegalNames) {
            try {
                realmSchema.rename(illegalName, CLASS_ALL_JAVA_TYPES);
                fail(illegalName + " should throw an exception");
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public void testRemoveClass() {
        realmSchema.remove(CLASS_ALL_JAVA_TYPES);
        assertFalse(realmSchema.contains(CLASS_ALL_JAVA_TYPES));
    }

    public void testRemoveClassInvalidClassNameThrows() {
        try {
            realmSchema.remove("Foo");
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        try {
            realmSchema.remove(null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    // Test that it if { A -> B  && B -> A } you should remove the individual fields first before removing the entire
    // class. This also include transitive dependencies :/
    // Re-enable when this if fixed: https://github.com/realm/realm-core/pull/1267
    public void FIMXEtestRemoveClassWithReferencesThrows() {
        Util.setDebugLevel(2);
        try {
            realmSchema.remove("Cat");
            fail();
        } catch (IllegalStateException ignored) {
        }

        RealmObjectSchema ownerSchema = realmSchema.get("Owner");
        RealmObjectSchema catSchema = realmSchema.get("Cat");
        ownerSchema.removeField("cat");
        catSchema.removeField("owner");
        realmSchema.remove("Cat");
        assertFalse(realmSchema.contains("Cat"));
    }
}
