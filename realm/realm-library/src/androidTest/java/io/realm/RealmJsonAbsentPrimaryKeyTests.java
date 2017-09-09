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

import android.os.Build;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Arrays;

import io.realm.entities.PrimaryKeyAsBoxedByte;
import io.realm.entities.PrimaryKeyAsBoxedInteger;
import io.realm.entities.PrimaryKeyAsBoxedLong;
import io.realm.entities.PrimaryKeyAsBoxedShort;
import io.realm.entities.PrimaryKeyAsString;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;
import static org.junit.Assume.assumeThat;

@RunWith(Parameterized.class)
public class RealmJsonAbsentPrimaryKeyTests {
    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

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

    // Parameters for testing absent primary key value. PrimaryKey field is absent.
    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {PrimaryKeyAsBoxedByte.class,    "{ \"name\":\"HaHaHaHaHaHaHaHaH\" }"},
            {PrimaryKeyAsBoxedShort.class,   "{ \"name\":\"KeyValueTestIsFun\" }"},
            {PrimaryKeyAsBoxedInteger.class, "{ \"name\":\"FunValueTestIsKey\" }"},
            {PrimaryKeyAsBoxedLong.class,    "{ \"name\":\"NameAsBoxedLong-!\" }"},
            {PrimaryKeyAsString.class,       "{ \"id\":2429214 }"}
        });
    }

    final private Class<? extends RealmObject> clazz;
    final private String jsonString;

    public RealmJsonAbsentPrimaryKeyTests(Class<? extends RealmObject> clazz, String jsonString) {
        this.jsonString = jsonString;
        this.clazz = clazz;
    }

    // Tests absent primary key value for createObjectFromJson().
    @Test
    public void createObjectFromJson_primaryKey_isAbsent_fromJsonObject() throws JSONException {
        realm.beginTransaction();
        thrown.expect(IllegalArgumentException.class);
        realm.createObjectFromJson(clazz, new JSONObject(jsonString));
        realm.commitTransaction();
    }

    // Tests absent primary key value for createOrUpdateObjectFromJson().
    @Test
    public void createOrUpdateObjectFromJson_primaryKey_isAbsent_fromJsonObject() throws JSONException {
        realm.beginTransaction();
        thrown.expect(IllegalArgumentException.class);
        realm.createOrUpdateObjectFromJson(clazz, new JSONObject(jsonString));
        realm.commitTransaction();
    }

    // Tests absent primary key value for createAllFromJson().
    @Test
    public void createAllFromJson_primaryKey_isAbsent_fromJsonObject() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(new JSONObject(jsonString));
        realm.beginTransaction();
        thrown.expect(IllegalArgumentException.class);
        realm.createAllFromJson(clazz, jsonArray);
        realm.commitTransaction();
    }

    // Tests absent primary key value for createOrUpdateAllFromJson().
    @Test
    public void createOrUpdateAllFromJson_primaryKey_isAbsent_fromJsonObject() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(new JSONObject(jsonString));
        realm.beginTransaction();
        thrown.expect(IllegalArgumentException.class);
        realm.createOrUpdateAllFromJson(clazz, jsonArray);
        realm.commitTransaction();
    }

    // Tests absent primary key value for createObjectFromJson() stream version.
    @Test
    public void createObjectFromJson_primaryKey_isAbsent_fromJsonStream() throws JSONException, IOException {
        assumeThat(Build.VERSION.SDK_INT, greaterThanOrEqualTo(Build.VERSION_CODES.HONEYCOMB));

        realm.beginTransaction();
        thrown.expect(IllegalArgumentException.class);
        realm.createObjectFromJson(clazz, TestHelper.stringToStream(jsonString));
        realm.commitTransaction();
    }

    // Tests absent primary key value for createOrUpdateObjectFromJson() stream version.
    @Test
    public void createOrUpdateObjectFromJson_primaryKey_isAbsent_fromJsonStream() throws JSONException, IOException {
        realm.beginTransaction();
        thrown.expect(IllegalArgumentException.class);
        realm.createOrUpdateObjectFromJson(clazz, TestHelper.stringToStream(jsonString));
        realm.commitTransaction();
    }

    // Tests absent primary key value for createAllFromJson() stream version.
    @Test
    public void createAllFromJson_primaryKey_isAbsent_fromJsonStream() throws JSONException, IOException {
        assumeThat(Build.VERSION.SDK_INT, greaterThanOrEqualTo(Build.VERSION_CODES.HONEYCOMB));

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(new JSONObject(jsonString));
        realm.beginTransaction();
        thrown.expect(IllegalArgumentException.class);
        realm.createAllFromJson(clazz, TestHelper.stringToStream(jsonArray.toString()));
        realm.commitTransaction();
    }

    // Tests absent primary key value for createOrUpdateAllFromJson() stream version.
    @Test
    public void createOrUpdateAllFromJson_primaryKey_isAbsent_fromJsonStream() throws JSONException, IOException {
        assumeThat(Build.VERSION.SDK_INT, greaterThanOrEqualTo(Build.VERSION_CODES.HONEYCOMB));

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(new JSONObject(jsonString));
        realm.beginTransaction();
        thrown.expect(IllegalArgumentException.class);
        realm.createOrUpdateAllFromJson(clazz, TestHelper.stringToStream(jsonArray.toString()));
        realm.commitTransaction();
    }
}
