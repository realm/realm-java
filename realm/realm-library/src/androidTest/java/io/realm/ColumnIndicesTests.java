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

import android.support.annotation.NonNull;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.HashMap;

import io.realm.entities.Cat;
import io.realm.entities.Dog;
import io.realm.internal.ColumnIndices;
import io.realm.internal.ColumnInfo;
import io.realm.internal.RealmProxyMediator;
import io.realm.internal.util.Pair;
import io.realm.rule.TestRealmConfigurationFactory;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertSame;
import static org.junit.Assert.assertNotEquals;


@RunWith(AndroidJUnit4.class)
public class ColumnIndicesTests {
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
        mediator = config.getSchemaMediator();
    }

    @After
    public void tearDown() {
        if (realm != null) {
            realm.close();
        }
    }

    @NonNull
    private ColumnIndices create(long schemaVersion) {
        final CatRealmProxy.CatColumnInfo catColumnInfo;
        final DogRealmProxy.DogColumnInfo dogColumnInfo;
        catColumnInfo = (CatRealmProxy.CatColumnInfo) mediator.validateTable(Cat.class, realm.sharedRealm, false);
        dogColumnInfo = (DogRealmProxy.DogColumnInfo) mediator.validateTable(Dog.class, realm.sharedRealm, false);
        Pair<Class<? extends RealmModel>, String> catDesc = Pair.<Class<? extends RealmModel>, String>create(Cat.class, "Cat");
        Pair<Class<? extends RealmModel>, String> dogDesc = Pair.<Class<? extends RealmModel>, String>create(Dog.class, "Dog");

        HashMap<Pair<Class<? extends RealmModel>, String>, ColumnInfo> map = new HashMap<>();
        map.put(catDesc, catColumnInfo);
        map.put(dogDesc, dogColumnInfo);
        return new ColumnIndices(schemaVersion, Collections.unmodifiableMap(map));
    }

    @Test
    public void copyDeeply() {
        final long schemaVersion = 100;

        final ColumnIndices columnIndices = create(schemaVersion);
        final ColumnIndices deepCopy = new ColumnIndices(columnIndices, true);
        assertNotSame(columnIndices, deepCopy);

        assertEquals(schemaVersion, deepCopy.getSchemaVersion());

        ColumnInfo colInfo = columnIndices.getColumnInfo(Cat.class);
        ColumnInfo colInfoCopy = deepCopy.getColumnInfo(Cat.class);
        assertNotSame(colInfo, colInfoCopy);
        assertEquals(colInfo.getColumnIndex(Cat.FIELD_NAME), colInfoCopy.getColumnIndex(Cat.FIELD_NAME));

        colInfo = columnIndices.getColumnInfo(Dog.class);
        colInfoCopy = deepCopy.getColumnInfo(Dog.class);
        assertNotSame(colInfo, colInfoCopy);
        assertEquals(colInfo.getColumnIndex(Dog.FIELD_AGE), colInfoCopy.getColumnIndex(Dog.FIELD_AGE));
    }

    @Test
    public void copyFrom() {
        final long sourceSchemaVersion = 101;
        final long targetSchemaVersion = 100;

        final ColumnIndices source = create(sourceSchemaVersion);
        final ColumnIndices target = create(targetSchemaVersion);

        final CatRealmProxy.CatColumnInfo catColumnInfoInSource = (CatRealmProxy.CatColumnInfo) source.getColumnInfo(Cat.class);
        final CatRealmProxy.CatColumnInfo catColumnInfoInTarget = (CatRealmProxy.CatColumnInfo) target.getColumnInfo(Cat.class);

        catColumnInfoInSource.nameIndex++;

        // Checks preconditions.
        assertNotEquals(catColumnInfoInSource.nameIndex, catColumnInfoInTarget.nameIndex);
        assertNotSame(catColumnInfoInSource.getIndicesMap(), catColumnInfoInTarget.getIndicesMap());

        target.copyFrom(source);

        assertEquals(sourceSchemaVersion, target.getSchemaVersion());
        assertEquals(catColumnInfoInSource.nameIndex, catColumnInfoInTarget.nameIndex);
        // update, not replace
        assertSame(catColumnInfoInTarget, target.getColumnInfo(Cat.class));
    }
}
