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
package io.realm;

import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.realm.entities.realmname.ClassNameOverrideModulePolicy;
import io.realm.entities.realmname.ClassWithPolicy;
import io.realm.entities.realmname.ClassWithValueDefinedNames;
import io.realm.entities.realmname.CustomRealmNamesModule;
import io.realm.entities.realmname.FieldNameOverrideClassPolicy;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * This class contains tests for checking that changing the internal Realm name
 * works correctly.
 */
@RunWith(AndroidJUnit4.class)
public class CustomRealmNameTests {

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();
    private Realm realm;
    private DynamicRealm dynamicRealm;

    @Before
    public void setUp() {
        RealmConfiguration config = configFactory.createConfigurationBuilder()
                .modules(new CustomRealmNamesModule())
                .build();
        realm = Realm.getInstance(config);
        dynamicRealm = DynamicRealm.getInstance(config);
    }


    @After
    public void tearDown() {
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
        if (dynamicRealm != null && !dynamicRealm.isClosed()) {
            dynamicRealm.close();
        }
    }

    //
    // Build checks
    //

    // Check that the module policy is used as the default for class and field names
    @Test
    public void modulePolicy_defaultPolicy() {
        assertTrue(realm.getSchema().contains("default_policy_from_module"));
        RealmObjectSchema classSchema = realm.getSchema().get("default_policy_from_module");
        assertTrue(classSchema.hasField("camel_case"));
    }

    // Check that field name policies on classes override those from modules
    @Test
    public void classFieldPolicy_overrideModuleFieldPolicy() {
        assertTrue(realm.getSchema().contains(ClassWithPolicy.CLASS_NAME));
        RealmObjectSchema classSchema = realm.getSchema().get(ClassWithPolicy.CLASS_NAME);
        for (String field : ClassWithPolicy.ALL_FIELDS) {
            assertTrue(field + " was not found.", classSchema.hasField(field));
        }
    }

    // Check that explicit class name override both module and class policies
    @Test
    public void className_overrideModuleClassPolicy() {
        assertTrue(realm.getSchema().contains(ClassNameOverrideModulePolicy.CLASS_NAME));
    }

    // Check that a explicitly setting a field name overrides a class field name policy
    @Test
    public void fieldName_overrideClassPolicy() {
        RealmObjectSchema classSchema = realm.getSchema().get(FieldNameOverrideClassPolicy.CLASS_NAME);
        assertTrue(classSchema.hasField(FieldNameOverrideClassPolicy.FIELD_CAMEL_CASE));
    }

    // Check that a explicitly setting a field name overrides a module field name policy
    @Test
    public void fieldName_overrideModulePolicy() {
        RealmObjectSchema classSchema = realm.getSchema().get(FieldNameOverrideClassPolicy.CLASS_NAME);
        assertTrue(classSchema.hasField(FieldNameOverrideClassPolicy.FIELD_CAMEL_CASE));
    }

    //
    // Query tests
    //
    // Mostly smoke test, as we only want to test that the query system correctly maps between
    // Java field names and cores.
    //
    @Test
    public void typedQueryWithJavaNames() {
        RealmResults<ClassWithPolicy> results = realm.where(ClassWithPolicy.class)
                .equalTo("camelCase", "foo") // Java name in model class
                .equalTo("parents.PascalCase", 1) // Backlinks also uses java names
                .sort("mHungarian") // Sorting uses Java names
                .distinct("customName") // Distinct uses Java names
                .findAll();
        assertTrue(results.isEmpty());
    }

    @Test
    public void typedQueryWithInternalNamesThrows() {

        // Normal predicates
        try {
            realm.where(ClassWithPolicy.class).equalTo(ClassWithPolicy.FIELD_CAMEL_CASE, "");
        } catch (IllegalArgumentException ignore) {
        }

        // Sorting
        try {
            realm.where(ClassWithPolicy.class).sort(ClassWithPolicy.FIELD_CAMEL_CASE);
        } catch (IllegalArgumentException ignore) {
        }

        // Distinct
        try {
            realm.where(ClassWithPolicy.class).distinct(ClassWithPolicy.FIELD_CAMEL_CASE);
        } catch (IllegalArgumentException ignore) {
        }

        // Backlinks do not exist as internal fields that can be queried
    }


    @Test
    public void dynamicQueryWithInternalNames() {
        // Backlink queries not supported on dynamic queries
        RealmResults<DynamicRealmObject> results = dynamicRealm.where(ClassWithPolicy.CLASS_NAME)
                .equalTo(ClassWithPolicy.FIELD_CAMEL_CASE, "foo") // Normal queries use internal names
                .sort(ClassWithPolicy.FIELD_M_HUNGARIAN) // Sorting uses internal names
                .distinct(ClassWithPolicy.FIELD_CUSTOM_NAME) // Distinct uses internal names
                .findAll();
        assertTrue(results.isEmpty());
    }

    @Test
    public void dynamicQueryWithJavaNamesThrows() {
        try {
            dynamicRealm.where(ClassWithPolicy.CLASS_NAME).equalTo("camelCase", "");
        } catch (IllegalArgumentException ignore) {
        }

        // Sorting
        try {
            dynamicRealm.where(ClassWithPolicy.CLASS_NAME).sort("camelCase");
        } catch (IllegalArgumentException ignore) {
        }

        // Distinct
        try {
            dynamicRealm.where(ClassWithPolicy.CLASS_NAME).distinct("camelCase");
        } catch (IllegalArgumentException ignore) {
        }
    }

    //
    // Schema tests
    //
    @Test
    public void typedSchemaReturnsInternalNames() {
        RealmSchema schema = realm.getSchema();
        assertTrue(schema.contains(ClassWithPolicy.CLASS_NAME));
        RealmObjectSchema classSchema = schema.get(ClassWithPolicy.CLASS_NAME);
        assertEquals(ClassWithPolicy.ALL_FIELDS.size(), classSchema.getFieldNames().size());
        for (String fieldName : ClassWithPolicy.ALL_FIELDS) {
            assertTrue("Could not find: " + fieldName, classSchema.hasField(fieldName));
        }
    }

    @Test
    public void dynamicSchemaReturnsInternalNames() {
        RealmSchema schema = realm.getSchema();
        assertTrue(schema.contains(ClassWithPolicy.CLASS_NAME));
        RealmObjectSchema classSchema = schema.get(ClassWithPolicy.CLASS_NAME);
        assertEquals(ClassWithPolicy.ALL_FIELDS.size(), classSchema.getFieldNames().size());
        for (String fieldName : ClassWithPolicy.ALL_FIELDS) {
            assertTrue("Could not find: " + fieldName, classSchema.hasField(fieldName));
        }
    }

    // Verify that names using the default value() parameter on annotations are used correctly
    @Test
    public void valueParameterDefinedNamesInsteadOfExplicit() {
        RealmSchema schema = realm.getSchema();
        assertTrue(schema.contains(ClassWithValueDefinedNames.REALM_CLASS_NAME));
        assertFalse(schema.contains(ClassWithValueDefinedNames.JAVA_CLASS_NAME));

        RealmObjectSchema classSchema = schema.get(ClassWithValueDefinedNames.REALM_CLASS_NAME);
        assertTrue(classSchema.hasField(ClassWithValueDefinedNames.REALM_FIELD_NAME));
        assertFalse(classSchema.hasField(ClassWithValueDefinedNames.JAVA_FIELD_NAME));
    }

    //
    // Dynamic Realm tests
    //
    @Test
    public void createObjects() {
        dynamicRealm.executeTransaction(r -> {
            // Use internal name
            DynamicRealmObject obj = r.createObject(ClassWithPolicy.CLASS_NAME);
            assertNotNull(obj);
        });
    }


    //
    // Realm tests
    //
    @Test
    public void copyOrUpdate() {
        realm.executeTransaction(r -> {
            ClassWithPolicy obj = new ClassWithPolicy();
            try {
                r.copyToRealmOrUpdate(obj); // Verify that we correctly check that a primary key is missing
                fail();
            } catch (IllegalArgumentException e) {
                assertTrue(e.getMessage().startsWith("A RealmObject with no @PrimaryKey cannot be updated"));
            }
        });
    }
}
