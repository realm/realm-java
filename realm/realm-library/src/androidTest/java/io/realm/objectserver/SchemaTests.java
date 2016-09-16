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

package io.realm.objectserver;


import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import io.realm.Realm;
import io.realm.entities.StringOnly;
import io.realm.rule.TestRealmConfigurationFactory;

import static io.realm.objectserver.SyncTestUtils.createTestUser;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertFalse;

@RunWith(AndroidJUnit4.class)
public class SchemaTests {
    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private Context context;
    private User user;
    private SyncConfiguration config;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getContext();
        User user = createTestUser();
        config = new SyncConfiguration.Builder(context)
                .user(user)
                .serverUrl("realm://objectserver.realm.io/~/default")
                .build();
    }

    @After
    public void tearDown() throws Exception {
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
        thrown.expect(IllegalArgumentException.class);
        realm.getSchema().remove(className);
        realm.cancelTransaction();
        realm.close();
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
        thrown.expect(IllegalArgumentException.class);
        realm.getSchema().rename(className, "Dogplace");
        realm.cancelTransaction();
        assertTrue(realm.getSchema().contains(className));
        realm.close();
    }

    @Test
    public void disallow_removeField() {
        Realm realm = Realm.getInstance(config);
        String className = "StringOnly";
        String fieldName = "chars";
        realm.beginTransaction();
        assertTrue(realm.getSchema().get(className).hasField(fieldName));
        thrown.expect(IllegalArgumentException.class);
        realm.getSchema().get(className).removeField(fieldName);
        realm.cancelTransaction();
        realm.close();
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
}
