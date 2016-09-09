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

import java.util.Map;

import io.realm.entities.Cat;
import io.realm.internal.RealmProxyMediator;
import io.realm.rule.TestRealmConfigurationFactory;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class ColumnInfoTests {
    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private Realm realm;

    @Before
    public void setUp() {
        RealmConfiguration config = configFactory.createConfiguration();
        realm = Realm.getInstance(config);
    }

    @After
    public void tearDown() {
        if (realm != null) {
            realm.close();
        }
    }

    @Test
    public void copyFrom_checkIndex() {
        final RealmProxyMediator mediator = realm.getConfiguration().getSchemaMediator();
        final CatRealmProxy.CatColumnInfo sourceColumnInfo, targetColumnInfo;
        sourceColumnInfo = (CatRealmProxy.CatColumnInfo) mediator.validateTable(Cat.class, realm.sharedRealm);
        targetColumnInfo = (CatRealmProxy.CatColumnInfo) mediator.validateTable(Cat.class, realm.sharedRealm);

        // check precondition
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

        assertEquals(1, targetColumnInfo.nameIndex);
        assertEquals(2, targetColumnInfo.ageIndex);
        assertEquals(3, targetColumnInfo.heightIndex);
        assertEquals(4, targetColumnInfo.weightIndex);
        assertEquals(5, targetColumnInfo.hasTailIndex);
        assertEquals(6, targetColumnInfo.birthdayIndex);
        assertEquals(7, targetColumnInfo.ownerIndex);
        assertEquals(8, targetColumnInfo.scaredOfDogIndex);

        // current implementation shares the indices map since it's unmodifiable.
        assertSame(sourceColumnInfo.getIndicesMap(), targetColumnInfo.getIndicesMap());
    }

    @Test
    public void copy_hasSameValue() {
        final RealmProxyMediator mediator = realm.getConfiguration().getSchemaMediator();
        final CatRealmProxy.CatColumnInfo columnInfo;
        columnInfo = (CatRealmProxy.CatColumnInfo) mediator.validateTable(Cat.class, realm.sharedRealm);

        columnInfo.nameIndex = 1;
        columnInfo.ageIndex = 2;
        columnInfo.heightIndex = 3;
        columnInfo.weightIndex = 4;
        columnInfo.hasTailIndex = 5;
        columnInfo.birthdayIndex = 6;
        columnInfo.ownerIndex = 7;
        columnInfo.scaredOfDogIndex = 8;

        CatRealmProxy.CatColumnInfo copy = columnInfo.copy();

        // modify original object
        columnInfo.nameIndex = 0;
        columnInfo.ageIndex = 0;
        columnInfo.heightIndex = 0;
        columnInfo.weightIndex = 0;
        columnInfo.hasTailIndex = 0;
        columnInfo.birthdayIndex = 0;
        columnInfo.ownerIndex = 0;
        columnInfo.scaredOfDogIndex = 0;

        assertNotSame(columnInfo, copy);

        assertEquals(1, copy.nameIndex);
        assertEquals(2, copy.ageIndex);
        assertEquals(3, copy.heightIndex);
        assertEquals(4, copy.weightIndex);
        assertEquals(5, copy.hasTailIndex);
        assertEquals(6, copy.birthdayIndex);
        assertEquals(7, copy.ownerIndex);
        assertEquals(8, copy.scaredOfDogIndex);

        // current implementation shares the indices map between copies, it's OK since map is unmodifiable.
        assertSame(columnInfo.getIndicesMap(), copy.getIndicesMap());
    }

    @Test
    public void copyFrom_mapIsUnmodifiable() {
        final RealmProxyMediator mediator = realm.getConfiguration().getSchemaMediator();
        final CatRealmProxy.CatColumnInfo columnInfo, columnInfo2;
        columnInfo = (CatRealmProxy.CatColumnInfo) mediator.validateTable(Cat.class, realm.sharedRealm);
        columnInfo2 = (CatRealmProxy.CatColumnInfo) mediator.validateTable(Cat.class, realm.sharedRealm);

        // check precondition
        assertNotSame(columnInfo, columnInfo2);

        Map<String, Long> map;

        map = columnInfo.getIndicesMap();
        try {
            map.clear();
            fail();
        } catch (UnsupportedOperationException ignored) {
        }

        map = columnInfo.copy().getIndicesMap();
        try {
            map.clear();
            fail();
        } catch (UnsupportedOperationException ignored) {
        }

        final CatRealmProxy.CatColumnInfo copy = columnInfo.copy();
        copy.copyFrom(columnInfo);
        map = copy.getIndicesMap();
        try {
            map.clear();
            fail();
        } catch (UnsupportedOperationException ignored) {
        }
    }
}
