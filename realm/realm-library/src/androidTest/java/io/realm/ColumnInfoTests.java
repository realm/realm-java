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
        assertNotSame(sourceColumnInfo.getIndicesMap(), targetColumnInfo.getIndicesMap());

        sourceColumnInfo.nameIndex = 1;
        sourceColumnInfo.ageIndex = 2;
        sourceColumnInfo.heightIndex = 3;
        sourceColumnInfo.weightIndex = 4;
        sourceColumnInfo.hasTailIndex = 5;
        sourceColumnInfo.birthdayIndex = 6;
        sourceColumnInfo.ownerIndex = 7;
        sourceColumnInfo.scaredOfDogIndex = 8;

        targetColumnInfo.nameIndex = 0;
        targetColumnInfo.ageIndex = 0;
        targetColumnInfo.heightIndex = 0;
        targetColumnInfo.weightIndex = 0;
        targetColumnInfo.hasTailIndex = 0;
        targetColumnInfo.birthdayIndex = 0;
        targetColumnInfo.ownerIndex = 0;
        targetColumnInfo.scaredOfDogIndex = 0;

        targetColumnInfo.copyFrom(sourceColumnInfo);

        assertEquals(sourceColumnInfo.nameIndex, targetColumnInfo.nameIndex);
        assertEquals(sourceColumnInfo.ageIndex, targetColumnInfo.ageIndex);
        assertEquals(sourceColumnInfo.heightIndex, targetColumnInfo.heightIndex);
        assertEquals(sourceColumnInfo.weightIndex, targetColumnInfo.weightIndex);
        assertEquals(sourceColumnInfo.hasTailIndex, targetColumnInfo.hasTailIndex);
        assertEquals(sourceColumnInfo.birthdayIndex, targetColumnInfo.birthdayIndex);
        assertEquals(sourceColumnInfo.ownerIndex, targetColumnInfo.ownerIndex);
        assertEquals(sourceColumnInfo.scaredOfDogIndex, targetColumnInfo.scaredOfDogIndex);
    }

    @Test
    public void copy_differentInstanceSameValues() {
        final io_realm_entities_CatRealmProxy.CatColumnInfo columnInfo
                = (io_realm_entities_CatRealmProxy.CatColumnInfo) mediator.createColumnInfo(Cat.class, realm.sharedRealm.getSchemaInfo());

        columnInfo.nameIndex = 1;
        columnInfo.ageIndex = 2;
        columnInfo.heightIndex = 3;
        columnInfo.weightIndex = 4;
        columnInfo.hasTailIndex = 5;
        columnInfo.birthdayIndex = 6;
        columnInfo.ownerIndex = 7;
        columnInfo.scaredOfDogIndex = 8;

        io_realm_entities_CatRealmProxy.CatColumnInfo copy = (io_realm_entities_CatRealmProxy.CatColumnInfo) columnInfo.copy(true);

        // verify that the copy is identical
        assertNotSame(columnInfo, copy);
        assertEquals(columnInfo.getIndicesMap(), copy.getIndicesMap());
        assertEquals(columnInfo.nameIndex, copy.nameIndex);
        assertEquals(columnInfo.ageIndex, copy.ageIndex);
        assertEquals(columnInfo.heightIndex, copy.heightIndex);
        assertEquals(columnInfo.weightIndex, copy.weightIndex);
        assertEquals(columnInfo.hasTailIndex, copy.hasTailIndex);
        assertEquals(columnInfo.birthdayIndex, copy.birthdayIndex);
        assertEquals(columnInfo.ownerIndex, copy.ownerIndex);
        assertEquals(columnInfo.scaredOfDogIndex, copy.scaredOfDogIndex);

        // Modify original object
        columnInfo.nameIndex = 0;
        columnInfo.ageIndex = 0;
        columnInfo.heightIndex = 0;
        columnInfo.weightIndex = 0;
        columnInfo.hasTailIndex = 0;
        columnInfo.birthdayIndex = 0;
        columnInfo.ownerIndex = 0;
        columnInfo.scaredOfDogIndex = 0;

        // the copy should not change
        assertEquals(1, copy.nameIndex);
        assertEquals(2, copy.ageIndex);
        assertEquals(3, copy.heightIndex);
        assertEquals(4, copy.weightIndex);
        assertEquals(5, copy.hasTailIndex);
        assertEquals(6, copy.birthdayIndex);
        assertEquals(7, copy.ownerIndex);
        assertEquals(8, copy.scaredOfDogIndex);
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
