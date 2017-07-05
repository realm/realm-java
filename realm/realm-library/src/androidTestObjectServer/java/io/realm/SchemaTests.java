/*
 * Copyright 2016 Realm Inc.
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

import java.util.Set;

import io.realm.entities.StringOnly;
import io.realm.rule.TestSyncConfigurationFactory;
import io.realm.util.SyncTestUtils;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class SchemaTests {
    @Rule
    public final TestSyncConfigurationFactory configFactory = new TestSyncConfigurationFactory();

    private SyncConfiguration config;

    @Before
    public void setUp() {
        SyncUser user = SyncTestUtils.createTestUser();
        config = configFactory.createSyncConfigurationBuilder(user, "realm://objectserver.realm.io/~/default").build();
    }

    @After
    public void tearDown() throws Exception {
        Realm.deleteRealm(config);
    }

    @Test
    public void getInstance() {
        Realm realm = Realm.getInstance(config);
        assertFalse(realm.isClosed());
        realm.close();
        assertTrue(realm.isClosed());
    }

    @Test
    public void createObject() {
        Realm realm = Realm.getInstance(config);
        realm.beginTransaction();
        assertTrue(realm.getSchema().contains("StringOnly"));
        StringOnly stringOnly= realm.createObject(StringOnly.class);
        stringOnly.setChars("TEST");
        realm.commitTransaction();
        assertEquals(1, realm.where(StringOnly.class).count());
        realm.close();
    }

    @Test
    public void disallow_removeClass() {
        Realm realm = Realm.getInstance(config);
        String className = "StringOnly";
        realm.beginTransaction();
        assertTrue(realm.getSchema().contains(className));
        try {
            realm.getSchema().remove(className);
            fail();
        } catch (IllegalArgumentException ignored) {
        } finally {
            realm.cancelTransaction();
            realm.close();
        }
    }

    @Test
    public void allow_createClass() {
        Realm realm = Realm.getInstance(config);
        String className = "Dogplace";
        realm.beginTransaction();
        realm.getSchema().create("Dogplace");
        realm.commitTransaction();
        assertTrue(realm.getSchema().contains(className));
        realm.close();
    }

    @Test
    public void disallow_renameClass() {
        Realm realm = Realm.getInstance(config);
        String className = "StringOnly";
        realm.beginTransaction();
        try {
            realm.getSchema().rename(className, "Dogplace");
            fail();
        } catch (IllegalArgumentException ignored) {
        } finally {
            realm.cancelTransaction();
            assertTrue(realm.getSchema().contains(className));
            realm.close();
        }
    }

    @Test
    public void disallow_removeField() {
        Realm realm = Realm.getInstance(config);
        String className = "StringOnly";
        String fieldName = "chars";
        realm.beginTransaction();
        assertTrue(realm.getSchema().get(className).hasField(fieldName));
        try {
            realm.getSchema().get(className).removeField(fieldName);
            fail();
        } catch (IllegalArgumentException ignored) {
        } finally {
            realm.cancelTransaction();
            realm.close();
        }
    }

    @Test
    public void allow_addField() {
        String className = "StringOnly";
        Realm realm = Realm.getInstance(config);

        realm.beginTransaction();
        realm.getSchema().get(className).addField("foo", String.class);
        realm.commitTransaction();

        assertTrue(realm.getSchema().get(className).hasField("foo"));

        realm.close();
    }

    @Test
    public void addPrimaryKey_notAllowed() {
        String className = "StringOnly";
        Realm realm = Realm.getInstance(config);

        realm.beginTransaction();
        RealmObjectSchema objectSchema = realm.getSchema().get(className);
        objectSchema.addField("foo", String.class);

        try {
            objectSchema.addPrimaryKey("foo");
            fail();
        } catch (UnsupportedOperationException ignored) {
        } finally {
            realm.commitTransaction();
            realm.close();
        }
    }

    @Test
    public void addField_withPrimaryKeyModifier_notAllowed() {
        String className = "StringOnly";
        Realm realm = Realm.getInstance(config);

        realm.beginTransaction();
        RealmObjectSchema objectSchema = realm.getSchema().get(className);

        try {
            objectSchema.addField("foo", String.class, FieldAttribute.PRIMARY_KEY);
            fail();
        } catch (UnsupportedOperationException ignored) {
        } finally {
            realm.commitTransaction();
            realm.close();
        }
    }

    // Special column "__OID" should be hidden from users.
    @Test
    public void getFieldNames_stableIdColumnShouldBeHidden() {
        String className = "StringOnly";
        Realm realm = Realm.getInstance(config);

        RealmObjectSchema objectSchema = realm.getSchema().get(className);
        Set<String> names = objectSchema.getFieldNames();
        assertEquals(1, names.size());
        assertEquals(StringOnly.FIELD_CHARS, names.iterator().next());
        realm.close();
    }
}
