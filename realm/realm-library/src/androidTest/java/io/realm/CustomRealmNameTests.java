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
import io.realm.entities.realmname.CustomRealmNamesModule;
import io.realm.entities.realmname.FieldNameOverrideClassPolicy;
import io.realm.rule.TestRealmConfigurationFactory;

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

    @Before
    public void setUp() {
        RealmConfiguration config = configFactory.createConfigurationBuilder()
                .modules(new CustomRealmNamesModule())
                .build();
        realm = Realm.getInstance(config);
    }


    @After
    public void tearDown() {
        if (realm != null) {
            realm.close();
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



        fail();
    }

    @Test
    public void typedQueryWithInternalNamesThrows() {
        fail();
    }


    @Test
    public void dynamicQueryWithInternalNames() {
        fail();
    }

    @Test
    public void dynamicQueryWithJavaNamesThrows() {
        fail();
    }

    //
    // Schema tests
    //
    @Test
    public void typedSchemaReturnsInternalNames() {
        fail();
    }

    @Test
    public void dynamicSchemaReturnsInternalNames() {
        fail();
    }

    //
    // FIXME: DynamicRealm tests
    //
}
