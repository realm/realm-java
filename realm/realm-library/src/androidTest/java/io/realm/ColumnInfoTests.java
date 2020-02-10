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
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import io.realm.entities.Cat;
import io.realm.internal.RealmProxyMediator;
import io.realm.rule.TestRealmConfigurationFactory;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertFalse;


@RunWith(AndroidJUnit4.class)
public class ColumnInfoTests {
    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private Realm realm;
    private RealmProxyMediator mediator;

    @Before
    public void setUp() {
        RealmConfiguration config = configFactory.createConfiguration();
        realm = Realm.getInstance(config);
        mediator = realm.getConfiguration().getSchemaMediator();
    }

    @After
    public void tearDown() {
        if (realm != null) {
            realm.close();
        }
    }

    @Test
    public void copyColumnInfoFrom_checkIndex() {
        io_realm_entities_CatRealmProxy.CatColumnInfo sourceColumnInfo
                = (io_realm_entities_CatRealmProxy.CatColumnInfo) mediator.createColumnInfo(Cat.class, realm.sharedRealm.getSchemaInfo());
        io_realm_entities_CatRealmProxy.CatColumnInfo targetColumnInfo
                = (io_realm_entities_CatRealmProxy.CatColumnInfo) mediator.createColumnInfo(Cat.class, realm.sharedRealm.getSchemaInfo());

        // Checks precondition.
        assertNotSame(sourceColumnInfo, targetColumnInfo);
        assertFalse(sourceColumnInfo.getColumnKeysMap().equals(targetColumnInfo.getColumnKeysMap()));

        sourceColumnInfo.nameColKey = 1;
        sourceColumnInfo.ageColKey = 2;
        sourceColumnInfo.heightColKey = 3;
        sourceColumnInfo.weightColKey = 4;
        sourceColumnInfo.hasTailColKey = 5;
        sourceColumnInfo.birthdayColKey = 6;
        sourceColumnInfo.ownerColKey = 7;
        sourceColumnInfo.scaredOfDogColKey = 8;

        targetColumnInfo.nameColKey = 0;
        targetColumnInfo.ageColKey = 0;
        targetColumnInfo.heightColKey = 0;
        targetColumnInfo.weightColKey = 0;
        targetColumnInfo.hasTailColKey = 0;
        targetColumnInfo.birthdayColKey = 0;
        targetColumnInfo.ownerColKey = 0;
        targetColumnInfo.scaredOfDogColKey = 0;

        targetColumnInfo.copyFrom(sourceColumnInfo);

        assertEquals(sourceColumnInfo.nameColKey, targetColumnInfo.nameColKey);
        assertEquals(sourceColumnInfo.ageColKey, targetColumnInfo.ageColKey);
        assertEquals(sourceColumnInfo.heightColKey, targetColumnInfo.heightColKey);
        assertEquals(sourceColumnInfo.weightColKey, targetColumnInfo.weightColKey);
        assertEquals(sourceColumnInfo.hasTailColKey, targetColumnInfo.hasTailColKey);
        assertEquals(sourceColumnInfo.birthdayColKey, targetColumnInfo.birthdayColKey);
        assertEquals(sourceColumnInfo.ownerColKey, targetColumnInfo.ownerColKey);
        assertEquals(sourceColumnInfo.scaredOfDogColKey, targetColumnInfo.scaredOfDogColKey);
    }

    @Test
    public void copy_differentInstanceSameValues() {
        final io_realm_entities_CatRealmProxy.CatColumnInfo columnInfo
                = (io_realm_entities_CatRealmProxy.CatColumnInfo) mediator.createColumnInfo(Cat.class, realm.sharedRealm.getSchemaInfo());

        columnInfo.nameColKey = 1;
        columnInfo.ageColKey = 2;
        columnInfo.heightColKey = 3;
        columnInfo.weightColKey = 4;
        columnInfo.hasTailColKey = 5;
        columnInfo.birthdayColKey = 6;
        columnInfo.ownerColKey = 7;
        columnInfo.scaredOfDogColKey = 8;

        io_realm_entities_CatRealmProxy.CatColumnInfo copy = (io_realm_entities_CatRealmProxy.CatColumnInfo) columnInfo.copy(true);

        // verify that the copy is identical
        assertNotSame(columnInfo, copy);
        assertEquals(columnInfo.getColumnKeysMap(), copy.getColumnKeysMap());
        assertEquals(columnInfo.nameColKey, copy.nameColKey);
        assertEquals(columnInfo.ageColKey, copy.ageColKey);
        assertEquals(columnInfo.heightColKey, copy.heightColKey);
        assertEquals(columnInfo.weightColKey, copy.weightColKey);
        assertEquals(columnInfo.hasTailColKey, copy.hasTailColKey);
        assertEquals(columnInfo.birthdayColKey, copy.birthdayColKey);
        assertEquals(columnInfo.ownerColKey, copy.ownerColKey);
        assertEquals(columnInfo.scaredOfDogColKey, copy.scaredOfDogColKey);

        // Modify original object
        columnInfo.nameColKey = 0;
        columnInfo.ageColKey = 0;
        columnInfo.heightColKey = 0;
        columnInfo.weightColKey = 0;
        columnInfo.hasTailColKey = 0;
        columnInfo.birthdayColKey = 0;
        columnInfo.ownerColKey = 0;
        columnInfo.scaredOfDogColKey = 0;

        // the copy should not change
        assertEquals(1, copy.nameColKey);
        assertEquals(2, copy.ageColKey);
        assertEquals(3, copy.heightColKey);
        assertEquals(4, copy.weightColKey);
        assertEquals(5, copy.hasTailColKey);
        assertEquals(6, copy.birthdayColKey);
        assertEquals(7, copy.ownerColKey);
        assertEquals(8, copy.scaredOfDogColKey);
    }

    @Test
    public void copy_immutableThrows() {
        final io_realm_entities_CatRealmProxy.CatColumnInfo original
                = (io_realm_entities_CatRealmProxy.CatColumnInfo) mediator.createColumnInfo(Cat.class, realm.sharedRealm.getSchemaInfo());

        io_realm_entities_CatRealmProxy.CatColumnInfo copy = (io_realm_entities_CatRealmProxy.CatColumnInfo) original.copy(false);
        try {
            copy.copyFrom(original);
            fail("Attempt to copy to an immutable ColumnInfo should throwS");
        } catch (UnsupportedOperationException ignore) {
        }
    }
}
