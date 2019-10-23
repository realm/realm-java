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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import io.realm.entities.Cat;
import io.realm.internal.RealmProxyMediator;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class RealmProxyMediatorTests {
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
    public void createColumnInfo_noDuplicateIndexInIndexFields() {
        RealmProxyMediator mediator = realm.getConfiguration().getSchemaMediator();
        io_realm_entities_CatRealmProxy.CatColumnInfo columnInfo = (io_realm_entities_CatRealmProxy.CatColumnInfo) mediator.createColumnInfo(Cat.class, realm.sharedRealm.getSchemaInfo());

        final Set<Long> indexSet = new HashSet<Long>();
        int indexCount = 0;

        indexSet.add(columnInfo.nameColKey);
        indexCount++;
        indexSet.add(columnInfo.ageColKey);
        indexCount++;
        indexSet.add(columnInfo.heightColKey);
        indexCount++;
        indexSet.add(columnInfo.weightColKey);
        indexCount++;
        indexSet.add(columnInfo.hasTailColKey);
        indexCount++;
        indexSet.add(columnInfo.birthdayColKey);
        indexCount++;
        indexSet.add(columnInfo.ownerColKey);
        indexCount++;
        indexSet.add(columnInfo.scaredOfDogColKey);
        indexCount++;

        assertEquals(indexCount, indexSet.size());
    }

    @Test
    public void createColumnInfo_noDuplicateIndexInIndicesMap() {
        RealmProxyMediator mediator = realm.getConfiguration().getSchemaMediator();
        io_realm_entities_CatRealmProxy.CatColumnInfo columnInfo;
        columnInfo = (io_realm_entities_CatRealmProxy.CatColumnInfo) mediator.createColumnInfo(Cat.class, realm.sharedRealm.getSchemaInfo());

        final Set<Long> columnKeySet = new HashSet<Long>();
        int indexCount = 0;

        // Gets index for each field and then put into set.
        for (Field field : Cat.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            columnKeySet.add(columnInfo.getColumnKey(field.getName()));
            indexCount++;
        }

        assertEquals("if no duplicates, size of set equals to field count.",
                indexCount, columnKeySet.size());
    }
}
