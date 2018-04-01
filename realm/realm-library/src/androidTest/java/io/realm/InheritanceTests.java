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

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import io.realm.entities.AllTypes;
import io.realm.entities.inheritence.Sub;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * This class check that inheritance (non-polymorphic works as intended
 */
@RunWith(AndroidJUnit4.class)
public class InheritanceTests {

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    private Realm realm;

    @Before
    public void setUp() {
        RealmConfiguration realmConfig = configFactory.createConfiguration();
        realm = Realm.getInstance(realmConfig);
    }

    @After
    public void tearDown() {
        if (realm != null) {
            realm.close();
        }
    }
    @Rule


    @Test
    public void getSetFields_unmanagedObject() {
        Sub sub = new Sub();
        assertEquals(0, sub.id);
        assertEquals(null, sub.name);
        sub.id = 42;
        sub.name = "foo";
        assertEquals(42, sub.id);
        assertEquals("foo", sub.name);
    }

    @Test
    public void getSetFields_managedObject() {
        realm.beginTransaction();
        Sub sub = realm.createObject(Sub.class);
        assertEquals(0, sub.id);
        assertEquals(null, sub.name);
        sub.id = 42;
        sub.name = "foo";
        assertEquals(42, sub.id);
        assertEquals("foo", sub.name);
        realm.cancelTransaction();
    }

    @Test
    public void doNotOverrideEqualsHashCodeToStringInSuperClass() {
        realm.beginTransaction();
        Sub sub = realm.createObject(Sub.class);
        assertTrue(sub.equals(null)); // Implemention accepts everything
        assertEquals(37, sub.hashCode());
        assertEquals("Foo", sub.toString());
        realm.cancelTransaction();
    }

    @Test
    public void subclassIncludeBaseClassFields() {
        fail();
    }

    @Test
    public void subClassOverrideBaseClassFieldsAndAnnotations() {
        fail();
    }

    @Test
    public void abstractClassesNotInSchema() {
        fail();
    }

    @Test
    public void where_abstractClassQueriesThrows() {
        fail();
    }

    @Test
    public void createObject_abstractClassesThrows() {
        fail();
    }

    @Test
    public void insert_abstractClassesThrows() {
        fail();
    }

    @Test
    public void copyToRealm_abstractClassesThrows() {
        fail();
    }

}
