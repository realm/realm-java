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

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import io.realm.entities.PrimaryKeyAsBoxedByte;
import io.realm.entities.PrimaryKeyAsBoxedInteger;
import io.realm.entities.PrimaryKeyAsBoxedLong;
import io.realm.entities.PrimaryKeyAsBoxedShort;
import io.realm.entities.PrimaryKeyAsString;
import io.realm.objectid.NullPrimaryKey;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class RealmJsonNullPrimaryKeyTests {
    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    protected Realm realm;

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

    // Parameters for testing null primary key value. PrimaryKey field is explicitly null.
    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {PrimaryKeyAsBoxedByte.class,    "OhThisIsNullKey?!", "{ \"id\":null, \"name\":\"OhThisIsNullKey?!\" }"},
            {PrimaryKeyAsBoxedShort.class,   "YouBetItIsNullKey", "{ \"id\":null, \"name\":\"YouBetItIsNullKey\" }"},
            {PrimaryKeyAsBoxedInteger.class, "Gosh Didnt KnowIt", "{ \"id\":null, \"name\":\"Gosh Didnt KnowIt\" }"},
            {PrimaryKeyAsBoxedLong.class,    "?YOUNOWKNOWRIGHT?", "{ \"id\":null, \"name\":\"?YOUNOWKNOWRIGHT?\" }"},
            {PrimaryKeyAsString.class,       "4299121",           "{ \"name\":null, \"id\":4299121  }"},
        });
    }

    final private Class<? extends RealmObject> clazz;
    final private String secondaryFieldValue;
    final private String jsonString;

    public RealmJsonNullPrimaryKeyTests(Class<? extends RealmObject> clazz, String secondFieldValue, String jsonString) {
        this.jsonString = jsonString;
        this.secondaryFieldValue = secondFieldValue;
        this.clazz = clazz;
    }

    // Tests null primary key value for createObjectFromJson().
    @Test
    public void createObjectFromJson_primaryKey_isNull_fromJsonObject() throws JSONException {
        realm.beginTransaction();
        realm.createObjectFromJson(clazz, new JSONObject(jsonString));
        realm.commitTransaction();

        // PrimaryKeyAsString
        if (clazz.equals(PrimaryKeyAsString.class)) {
            RealmResults<PrimaryKeyAsString> results = realm.where(PrimaryKeyAsString.class).findAll();
            assertEquals(1, results.size());
            assertEquals(Long.valueOf(secondaryFieldValue).longValue(), results.first().getId());
            assertEquals(null, results.first().getName());

        // PrimaryKeyAsNumber
        } else {
            RealmResults results = realm.where(clazz).findAll();
            assertEquals(1, results.size());
            assertEquals(null, ((NullPrimaryKey)results.first()).getId());
            assertEquals(secondaryFieldValue, ((NullPrimaryKey)results.first()).getName());
        }
    }

    // Tests null primary key value for createOrUpdateObjectFromJson().
    @Test
    public void createOrUpdateObjectFromJson_primaryKey_isNull_fromJsonObject() throws JSONException {
        realm.beginTransaction();
        realm.createOrUpdateObjectFromJson(clazz, new JSONObject(jsonString));
        realm.commitTransaction();

        // PrimaryKeyAsString
        if (clazz.equals(PrimaryKeyAsString.class)) {
            RealmResults<PrimaryKeyAsString> results = realm.where(PrimaryKeyAsString.class).findAll();
            assertEquals(1, results.size());
            assertEquals(Long.valueOf(secondaryFieldValue).longValue(), results.first().getId());
            assertEquals(null, results.first().getName());

        // PrimaryKeyAsNumber
        } else {
            RealmResults results = realm.where(clazz).findAll();
            assertEquals(1, results.size());
            assertEquals(null, ((NullPrimaryKey)results.first()).getId());
            assertEquals(secondaryFieldValue, ((NullPrimaryKey)results.first()).getName());
        }
    }

    // Tests null primary key value for createObject() -> createOrUpdateObjectFromJson().
    @Test
    public void createOrUpdateObjectFromJson_primaryKey_isNull_updateFromJsonObject() throws JSONException {
        realm.beginTransaction();
        realm.createObject(clazz, null); // name = null, id =null
        realm.createOrUpdateObjectFromJson(clazz, new JSONObject(jsonString));
        realm.commitTransaction();

        // PrimaryKeyAsString
        if (clazz.equals(PrimaryKeyAsString.class)) {
            RealmResults<PrimaryKeyAsString> results = realm.where(PrimaryKeyAsString.class).findAll();
            assertEquals(1, results.size());
            assertEquals(Long.valueOf(secondaryFieldValue).longValue(), results.first().getId());
            assertEquals(null, results.first().getName());
        // PrimaryKeyAsNumber
        } else {
            RealmResults results = realm.where(clazz).findAll();
            assertEquals(1, results.size());
            assertEquals(null, ((NullPrimaryKey)results.first()).getId());
            assertEquals(secondaryFieldValue, ((NullPrimaryKey)results.first()).getName());
        }
    }
}
