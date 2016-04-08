/*
 * Copyright 2014 Realm Inc.
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
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import io.realm.entities.AllTypes;
import io.realm.entities.AllTypesPrimaryKey;
import io.realm.entities.AnnotationTypes;
import io.realm.entities.Dog;
import io.realm.entities.NoPrimaryKeyNullTypes;
import io.realm.entities.NullTypes;
import io.realm.entities.OwnerPrimaryKey;
import io.realm.entities.PrimaryKeyAsBoxedByte;
import io.realm.entities.PrimaryKeyAsBoxedInteger;
import io.realm.entities.PrimaryKeyAsBoxedLong;
import io.realm.entities.PrimaryKeyAsBoxedShort;
import io.realm.entities.PrimaryKeyAsString;
import io.realm.exceptions.RealmException;
import io.realm.rule.TestRealmConfigurationFactory;

import static io.realm.internal.test.ExtraTests.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class RealmJsonTests {
    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    protected Realm realm;
    private Context context;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getTargetContext();
        RealmConfiguration realmConfig = configFactory.createConfiguration();
        realm = Realm.getInstance(realmConfig);
    }

    @After
    public void tearDown() {
        if (realm != null) {
            realm.close();
        }
    }

    private InputStream convertJsonObjectToStream(JSONObject obj) {
        return new ByteArrayInputStream(obj.toString().getBytes());
    }

    // Assert that the list of AllTypesPrimaryKey objects where inserted and updated properly.
    private void assertAllTypesPrimaryKeyUpdated() {
        assertEquals(1, realm.allObjects(AllTypesPrimaryKey.class).size());
        AllTypesPrimaryKey obj = realm.allObjects(AllTypesPrimaryKey.class).first();
        assertEquals("Bar", obj.getColumnString());
        assertEquals(2.23F, obj.getColumnFloat(), 0F);
        assertEquals(2.234D, obj.getColumnDouble(), 0D);
        assertEquals(true, obj.isColumnBoolean());
        assertArrayEquals(new byte[]{1, 2, 3}, obj.getColumnBinary());
        assertEquals(new Date(2000), obj.getColumnDate());
        assertEquals("Dog4", obj.getColumnRealmObject().getName());
        assertEquals(2, obj.getColumnRealmList().size());
        assertEquals("Dog5", obj.getColumnRealmList().get(0).getName());
    }

    // Check the imported object from nulltyps.json[0].
    private void checkNullableValuesAreNull(NullTypes nullTypes1) {
        // 1 String
        assertNull(nullTypes1.getFieldStringNull());
        assertEquals("", nullTypes1.getFieldStringNotNull());
        // 2 Bytes
        assertNull(nullTypes1.getFieldBytesNull());
        assertTrue(Arrays.equals(new byte[0], nullTypes1.getFieldBytesNotNull()));
        // 3 Boolean
        assertNull(nullTypes1.getFieldBooleanNull());
        assertFalse(nullTypes1.getFieldBooleanNotNull());
        // 4 Byte
        assertNull(nullTypes1.getFieldByteNull());
        assertEquals(0, nullTypes1.getFieldByteNotNull().byteValue());
        // 5 Short
        assertNull(nullTypes1.getFieldShortNull());
        assertEquals(0, nullTypes1.getFieldShortNotNull().shortValue());
        // 6 Integer
        assertNull(nullTypes1.getFieldIntegerNull());
        assertEquals(0, nullTypes1.getFieldIntegerNotNull().intValue());
        // 7 Long
        assertNull(nullTypes1.getFieldLongNull());
        assertEquals(0, nullTypes1.getFieldLongNotNull().longValue());
        // 8 Float
        assertNull(nullTypes1.getFieldFloatNull());
        assertEquals((Float)0F, nullTypes1.getFieldFloatNotNull());
        // 9 Double
        assertNull(nullTypes1.getFieldDoubleNull());
        assertEquals((Double)0D, nullTypes1.getFieldDoubleNotNull());
        // 10 Date
        assertNull(nullTypes1.getFieldDateNull());
        assertEquals(new Date(0), nullTypes1.getFieldDateNotNull());
        // 11 RealmObject
        assertNull(nullTypes1.getFieldObjectNull());
    }

    // Check the imported object from nulltyps.json[1].
    private void checkNullableValuesAreNotNull(NullTypes nullTypes2) {
        // 1 String
        assertEquals("", nullTypes2.getFieldStringNull());
        assertEquals("", nullTypes2.getFieldStringNotNull());
        // 2 Bytes
        assertTrue(Arrays.equals(new byte[0], nullTypes2.getFieldBytesNull()));
        assertTrue(Arrays.equals(new byte[0], nullTypes2.getFieldBytesNotNull()));
        // 3 Boolean
        assertFalse(nullTypes2.getFieldBooleanNull());
        assertFalse(nullTypes2.getFieldBooleanNotNull());
        // 4 Byte
        assertEquals(0, nullTypes2.getFieldByteNull().byteValue());
        assertEquals(0, nullTypes2.getFieldByteNotNull().byteValue());
        // 5 Short
        assertEquals(0, nullTypes2.getFieldShortNull().shortValue());
        assertEquals(0, nullTypes2.getFieldShortNotNull().shortValue());
        // 6 Integer
        assertEquals(0, nullTypes2.getFieldIntegerNull().intValue());
        assertEquals(0, nullTypes2.getFieldIntegerNotNull().intValue());
        // 7 Long
        assertEquals(0, nullTypes2.getFieldLongNull().longValue());
        assertEquals(0, nullTypes2.getFieldLongNotNull().longValue());
        // 8 Float
        assertEquals((Float)0F, nullTypes2.getFieldFloatNull());
        assertEquals((Float)0F, nullTypes2.getFieldFloatNotNull());
        // 9 Double
        assertEquals((Double)0D, nullTypes2.getFieldDoubleNull());
        assertEquals((Double)0D, nullTypes2.getFieldDoubleNotNull());
        // 10 Date
        assertEquals(new Date(0), nullTypes2.getFieldDateNull());
        assertEquals(new Date(0), nullTypes2.getFieldDateNotNull());
        // 11 RealmObject
        assertTrue(nullTypes2.getFieldObjectNull() != null);
    }

    @Test
    public void createObject_fromJsonNullObject() {
        realm.createObjectFromJson(AllTypes.class, (JSONObject) null);
        assertEquals(0, realm.allObjects(AllTypes.class).size());
    }

    @Test
    public void createAllFromJson_nullArray() {
        realm.createAllFromJson(AllTypes.class, (JSONArray) null);
        assertEquals(0, realm.allObjects(AllTypes.class).size());

    }

    @Test
    public void createObjectFromJson_allSimpleObjectAllTypes() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("columnString", "String");
        json.put("columnLong", 1L);
        json.put("columnFloat", 1.23F);
        json.put("columnDouble", 1.23D);
        json.put("columnBoolean", true);
        json.put("columnBinary", new String(Base64.encode(new byte[] {1,2,3}, Base64.DEFAULT)));

        realm.beginTransaction();
        realm.createObjectFromJson(AllTypes.class, json);
        realm.commitTransaction();
        AllTypes obj = realm.allObjects(AllTypes.class).first();

        // Check that all primitive types are imported correctly
        assertEquals("String", obj.getColumnString());
        assertEquals(1L, obj.getColumnLong());
        assertEquals(1.23F, obj.getColumnFloat(),0F);
        assertEquals(1.23D, obj.getColumnDouble(),0D);
        assertEquals(true, obj.isColumnBoolean());
        assertArrayEquals(new byte[]{1, 2, 3}, obj.getColumnBinary());
    }

    @Test
    public void createObjectFromJson_dateAsLong() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("columnDate", 1000L); // Realm operates at seconds level granularity

        realm.beginTransaction();
        realm.createObjectFromJson(AllTypes.class, json);
        realm.commitTransaction();

        AllTypes obj = realm.allObjects(AllTypes.class).first();
        assertEquals(new Date(1000), obj.getColumnDate());
    }

    @Test
    public void createObjectFromJson_dateAsString() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("columnDate", "/Date(1000)/");

        realm.beginTransaction();
        realm.createObjectFromJson(AllTypes.class, json);
        realm.commitTransaction();

        AllTypes obj = realm.allObjects(AllTypes.class).first();
        assertEquals(new Date(1000), obj.getColumnDate());
    }

    @Test
    public void createObjectFromJson_dateAsStringTimeZone() throws JSONException {
        // Oct 03 2015 14:45.33
        JSONObject json = new JSONObject();
        json.put("columnDate", "/Date(1443854733376+0800)/");

        realm.beginTransaction();
        realm.createObjectFromJson(AllTypes.class, json);
        realm.commitTransaction();

        AllTypes obj = realm.allObjects(AllTypes.class).first();
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("Australia/West"));
        cal.set(2015, Calendar.OCTOBER, 03, 14, 45, 33);
        cal.set(Calendar.MILLISECOND, 0);
        Date convDate = obj.getColumnDate();

        assertEquals(convDate.getTime(), cal.getTime().getTime());
    }

    @Test
    public void createObjectFromJson_childObject() throws JSONException {
        JSONObject allTypesObject = new JSONObject();
        JSONObject dogObject = new JSONObject();
        dogObject.put("name", "Fido");
        allTypesObject.put("columnRealmObject", dogObject);

        realm.beginTransaction();
        realm.createObjectFromJson(AllTypes.class, allTypesObject);
        realm.commitTransaction();

        AllTypes obj = realm.allObjects(AllTypes.class).first();
        assertEquals("Fido", obj.getColumnRealmObject().getName());
    }

    @Test
    public void createObjectFromJson_childObjectList() throws JSONException {
        JSONObject allTypesObject = new JSONObject();
        JSONObject dog1 = new JSONObject(); dog1.put("name", "Fido-1");
        JSONObject dog2 = new JSONObject(); dog2.put("name", "Fido-2");
        JSONObject dog3 = new JSONObject(); dog3.put("name", "Fido-3");
        JSONArray dogList = new JSONArray();
        dogList.put(dog1);
        dogList.put(dog2);
        dogList.put(dog3);
        allTypesObject.put("columnRealmList", dogList);

        realm.beginTransaction();
        realm.createObjectFromJson(AllTypes.class, allTypesObject);
        realm.commitTransaction();

        AllTypes obj = realm.allObjects(AllTypes.class).first();
        assertEquals(3, obj.getColumnRealmList().size());
        assertEquals("Fido-3", obj.getColumnRealmList().get(2).getName());
    }

    @Test
    public void createObjectFromJson_emptyChildObjectList() throws JSONException {
        JSONObject allTypesObject = new JSONObject();
        JSONArray dogList = new JSONArray();
        allTypesObject.put("columnRealmList", dogList);

        realm.beginTransaction();
        realm.createObjectFromJson(AllTypes.class, allTypesObject);
        realm.commitTransaction();

        AllTypes obj = realm.allObjects(AllTypes.class).first();
        assertEquals(0, obj.getColumnRealmList().size());
    }

    @Test
    public void createObjectFromJson_stringSimpleObject() {
        realm.beginTransaction();
        Dog dog = realm.createObjectFromJson(Dog.class, "{ name: \"Foo\" }");
        realm. commitTransaction();

        assertEquals("Foo", dog.getName());
        assertEquals("Foo", realm.allObjects(Dog.class).first().getName());
    }

    @Test
    public void createObjectFromJson_stringFaultyJsonThrows() {
        realm.beginTransaction();
        try {
            realm.createObjectFromJson(Dog.class, "{ name \"Foo\" }");
            fail("Faulty JSON should result in a RealmException");
        } catch (RealmException ignored) {
        } finally {
            realm.commitTransaction();
        }
    }

    @Test
    public void createObjectFromJson_stringNull() {
        realm.beginTransaction();
        Dog dog = realm.createObjectFromJson(Dog.class, (String) null);
        realm.commitTransaction();

        //noinspection ConstantConditions
        assertNull(dog);
        assertEquals(0, realm.allObjects(Dog.class).size());
    }

    @Test
    public void createAllFromJson_jsonArrayEmpty() {
        JSONArray array = new JSONArray();
        realm.beginTransaction();
        realm.createAllFromJson(AllTypes.class, array);
        realm.commitTransaction();

        assertEquals(0, realm.allObjects(AllTypes.class).size());
    }

    @Test
    public void createAllFromJson_jsonArray() throws JSONException {
        JSONObject dog1 = new JSONObject(); dog1.put("name", "Fido-1");
        JSONObject dog2 = new JSONObject(); dog2.put("name", "Fido-2");
        JSONObject dog3 = new JSONObject(); dog3.put("name", "Fido-3");
        JSONArray dogList = new JSONArray();
        dogList.put(dog1);
        dogList.put(dog2);
        dogList.put(dog3);

        realm.beginTransaction();
        realm.createAllFromJson(Dog.class, dogList);
        realm.commitTransaction();

        assertEquals(3, realm.allObjects(Dog.class).size());
        assertEquals(1, realm.where(Dog.class).equalTo("name", "Fido-3").findAll().size());
    }

    // Test if Json object doesn't have the field, then the field should have default value.
    @Test
    public void createObjectFromJson_noValues() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("noThingHere", JSONObject.NULL);

        realm.beginTransaction();
        realm.createObjectFromJson(AllTypes.class, json);
        realm.commitTransaction();

        // Check that all primitive types are imported correctly
        AllTypes obj = realm.allObjects(AllTypes.class).first();
        assertEquals("", obj.getColumnString());
        assertEquals(0L, obj.getColumnLong());
        assertEquals(0F, obj.getColumnFloat(), 0F);
        assertEquals(0D, obj.getColumnDouble(), 0D);
        assertEquals(false, obj.isColumnBoolean());
        assertEquals(new Date(0), obj.getColumnDate());
        assertArrayEquals(new byte[0], obj.getColumnBinary());
        assertNull(obj.getColumnRealmObject());
        assertEquals(0, obj.getColumnRealmList().size());
    }

    // Test that given an exception everything up to the exception is saved
    @Test
    public void createObjectFromJson_jsonException() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("columnString", "Foo");
        json.put("columnDate", "Boom");

        realm.beginTransaction();
        try {
            realm.createObjectFromJson(AllTypes.class, json);
        } catch (RealmException ignored) {
        } finally {
            realm.commitTransaction();
        }

        AllTypes obj = realm.allObjects(AllTypes.class).first();
        assertEquals("Foo", obj.getColumnString());
        assertEquals(new Date(0), obj.getColumnDate());
    }

    @Test
    public void createObjectFromJson_respectIgnoredFields() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("indexString", "Foo");
        json.put("notIndexString", "Bar");
        json.put("ignoreString", "Baz");

        realm.beginTransaction();
        realm.createObjectFromJson(AnnotationTypes.class, json);
        realm.commitTransaction();

        AnnotationTypes annotationsObject = realm.allObjects(AnnotationTypes.class).first();
        assertEquals("Foo", annotationsObject.getIndexString());
        assertEquals(null, annotationsObject.getIgnoreString());
    }

    @Test
    public void createAllFromJson_stringArraySimpleArray() {
        realm.beginTransaction();
        realm.createAllFromJson(Dog.class, "[{ name: \"Foo\" }, { name: \"Bar\" }]");
        realm.commitTransaction();

        assertEquals(2, realm.allObjects(Dog.class).size());
    }

    @Test
    public void createAllFromJson_stringArrayFaultyJsonThrows() {
        realm.beginTransaction();
        try {
            realm.createAllFromJson(Dog.class, "[{ name : \"Foo\" ]");
            fail("Faulty JSON should result in a RealmException");
        } catch (RealmException ignored) {
        } finally {
            realm.commitTransaction();
        }
    }

    @Test
    public void createAllFromJson_stringArrayNull() {
        realm.beginTransaction();
        realm.createAllFromJson(Dog.class, (String) null);
        realm.commitTransaction();

        assertEquals(0, realm.allObjects(Dog.class).size());
    }

    @Test
    public void createAllFromJson_streamNull() throws IOException {
        realm.createAllFromJson(AllTypes.class, (InputStream) null);
        assertEquals(0, realm.allObjects(AllTypes.class).size());
    }

    @Test
    public void createObjectFromJson_streamAllSimpleTypes() throws IOException {
        InputStream in = TestHelper.loadJsonFromAssets(context, "all_simple_types.json");
        realm.beginTransaction();
        realm.createObjectFromJson(AllTypes.class, in);
        realm.commitTransaction();
        in.close();

        // Check that all primitive types are imported correctly
        AllTypes obj = realm.allObjects(AllTypes.class).first();
        assertEquals("String", obj.getColumnString());
        assertEquals(1L, obj.getColumnLong());
        assertEquals(1.23F, obj.getColumnFloat(), 0F);
        assertEquals(1.23D, obj.getColumnDouble(), 0D);
        assertEquals(true, obj.isColumnBoolean());
        assertArrayEquals(new byte[]{1, 2, 3}, obj.getColumnBinary());
    }

    @Test
    public void createObjectFromJson_streamDateAsLong() throws IOException {
        InputStream in = TestHelper.loadJsonFromAssets(context, "date_as_long.json");
        realm.beginTransaction();
        realm.createObjectFromJson(AllTypes.class, in);
        realm.commitTransaction();
        in.close();

        // Check that all primitive types are imported correctly
        AllTypes obj = realm.allObjects(AllTypes.class).first();
        assertEquals(new Date(1000), obj.getColumnDate());
    }

    @Test
    public void createObjectFromJson_streamDateAsString() throws IOException {
        InputStream in = TestHelper.loadJsonFromAssets(context, "date_as_string.json");
        realm.beginTransaction();
        realm.createObjectFromJson(AllTypes.class, in);
        realm.commitTransaction();
        in.close();

        // Check that all primitive types are imported correctly
        AllTypes obj = realm.allObjects(AllTypes.class).first();
        assertEquals(new Date(1000), obj.getColumnDate());
    }

    @Test
    public void createObjectFromJson_streamDateAsISO8601String() throws IOException {
        InputStream in = TestHelper.loadJsonFromAssets(context, "date_as_iso8601_string.json");
        realm.beginTransaction();
        realm.createObjectFromJson(AllTypes.class, in);
        realm.commitTransaction();
        in.close();

        Calendar cal = new GregorianCalendar(2007, 8 - 1, 13, 19, 51, 23);
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        cal.set(Calendar.MILLISECOND, 789);
        Date date = cal.getTime();
        cal.set(Calendar.MILLISECOND, 0);
        Date dateZeroMillis = cal.getTime();

        // Check that all primitive types are imported correctly
        AllTypes obj = realm.allObjects(AllTypes.class).first();
        assertEquals(dateZeroMillis, obj.getColumnDate());
    }

    @Test
    public void createObjectFromJson_streamChildObject() throws IOException {
        InputStream in = TestHelper.loadJsonFromAssets(context, "single_child_object.json");
        realm.beginTransaction();
        realm.createObjectFromJson(AllTypes.class, in);
        realm.commitTransaction();
        in.close();

        AllTypes obj = realm.allObjects(AllTypes.class).first();
        assertEquals("Fido", obj.getColumnRealmObject().getName());
    }

    @Test
    public void createObjectFromJson_streamEmptyChildObjectList() throws IOException {
        InputStream in = TestHelper.loadJsonFromAssets(context, "realmlist_empty.json");
        realm.beginTransaction();
        realm.createObjectFromJson(AllTypes.class, in);
        realm.commitTransaction();
        in.close();

        AllTypes obj = realm.allObjects(AllTypes.class).first();
        assertEquals(0, obj.getColumnRealmList().size());
    }

    @Test
    public void createObjectFromJson_streamChildObjectList() throws IOException {
        InputStream in = TestHelper.loadJsonFromAssets(context, "realmlist.json");
        realm.beginTransaction();
        realm.createObjectFromJson(AllTypes.class, in);
        realm.commitTransaction();
        in.close();

        assertEquals(3, realm.allObjects(Dog.class).size());
        assertEquals(1, realm.where(Dog.class).equalTo("name", "Fido-3").findAll().size());
    }

    @Test
    public void createAllFromJson_streamArray() throws IOException {
        InputStream in = TestHelper.loadJsonFromAssets(context, "array.json");
        realm.beginTransaction();
        realm.createAllFromJson(Dog.class, in);
        realm.commitTransaction();

        assertEquals(3, realm.allObjects(Dog.class).size());
        assertEquals(1, realm.where(Dog.class).equalTo("name", "Fido-3").findAll().size());
    }


    // Test if Json object doesn't have the field, then the field should have default value. Stream version.
    @Test
    public void createObjectFromJson_streamNoValues() throws IOException {
        InputStream in = TestHelper.loadJsonFromAssets(context, "other_json_object.json");
        realm.beginTransaction();
        realm.createObjectFromJson(AllTypes.class, in);
        realm.commitTransaction();
        in.close();

        // Check that all primitive types are imported correctly
        AllTypes obj = realm.allObjects(AllTypes.class).first();
        assertEquals("", obj.getColumnString());
        assertEquals(0L, obj.getColumnLong());
        assertEquals(0F, obj.getColumnFloat(), 0F);
        assertEquals(0D, obj.getColumnDouble(), 0D);
        assertEquals(false, obj.isColumnBoolean());
        assertEquals(new Date(0), obj.getColumnDate());
        assertArrayEquals(new byte[0], obj.getColumnBinary());
        assertNull(obj.getColumnRealmObject());
        assertEquals(0, obj.getColumnRealmList().size());
    }

    /**
     * Test update a existing object with JSON stream. Only primary key in JSON.
     * No value should be changed.
     */
    @Test
    public void createOrUpdateObjectFromJson_streamNullValues() throws IOException {
        AllTypesPrimaryKey obj = new AllTypesPrimaryKey();
        Date date = new Date(0);
        obj.setColumnLong(1); // ID
        obj.setColumnBinary(new byte[]{1});
        obj.setColumnBoolean(true);
        obj.setColumnDate(date);
        obj.setColumnDouble(1);
        obj.setColumnFloat(1);
        obj.setColumnString("1");

        realm.beginTransaction();
        realm.copyToRealm(obj);
        realm.commitTransaction();

        InputStream in = TestHelper.loadJsonFromAssets(context, "all_types_primary_key_field_only.json");
        realm.beginTransaction();
        realm.createOrUpdateObjectFromJson(AllTypesPrimaryKey.class, in);
        realm.commitTransaction();
        in.close();

        // Check that all primitive types are imported correctly
        obj = realm.allObjects(AllTypesPrimaryKey.class).first();
        assertEquals("1", obj.getColumnString());
        assertEquals(1L, obj.getColumnLong());
        assertEquals(1F, obj.getColumnFloat(), 0F);
        assertEquals(1D, obj.getColumnDouble(), 0D);
        assertEquals(true, obj.isColumnBoolean());
        assertEquals(date, obj.getColumnDate());
        assertArrayEquals(new byte[]{1}, obj.getColumnBinary());
        assertNull(obj.getColumnRealmObject());
        assertEquals(0, obj.getColumnRealmList().size());
    }

    // Test update a existing object with JSON object with only primary key.
    // No value should be changed.
    @Test
    public void createOrUpdateObjectFromJson_objectNullValues() throws IOException {
        AllTypesPrimaryKey obj = new AllTypesPrimaryKey();
        Date date = new Date(0);
        obj.setColumnLong(1); // ID
        obj.setColumnBinary(new byte[]{1});
        obj.setColumnBoolean(true);
        obj.setColumnDate(date);
        obj.setColumnDouble(1);
        obj.setColumnFloat(1);
        obj.setColumnString("1");

        realm.beginTransaction();
        realm.copyToRealm(obj);
        realm.commitTransaction();

        String json = TestHelper.streamToString(TestHelper.loadJsonFromAssets(context, "all_types_primary_key_field_only.json"));
        realm.beginTransaction();
        realm.createOrUpdateObjectFromJson(AllTypesPrimaryKey.class, json);
        realm.commitTransaction();

        // Check that all primitive types are imported correctly
        obj = realm.allObjects(AllTypesPrimaryKey.class).first();
        assertEquals("1", obj.getColumnString());
        assertEquals(1L, obj.getColumnLong());
        assertEquals(1F, obj.getColumnFloat(), 0F);
        assertEquals(1D, obj.getColumnDouble(), 0D);
        assertEquals(true, obj.isColumnBoolean());
        assertEquals(date, obj.getColumnDate());
        assertArrayEquals(new byte[]{1}, obj.getColumnBinary());
        assertNull(obj.getColumnRealmObject());
        assertEquals(0, obj.getColumnRealmList().size());
    }

    @Test
    public void createOrUpdateObject_noPrimaryKeyThrows() {
        try {
            realm.createOrUpdateObjectFromJson(AllTypes.class, new JSONObject());
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void createOrUpdateObjectFromJson_streamNoPrimaryKeyThrows() throws IOException {
        try {
            realm.createOrUpdateObjectFromJson(AllTypes.class, new TestHelper.StubInputStream());
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void createOrUpdateAllFromJson_streamInvalidJSonCurlyBracketThrows() throws IOException {
        try {
            realm.createOrUpdateAllFromJson(AllTypesPrimaryKey.class, TestHelper.stringToStream("{"));
            fail();
        } catch (RealmException ignored) {
        }
    }

    @Test
    public void createOrUpdateObjectFromJson_stringNoPrimaryKeyThrows() throws IOException {
        try {
            realm.createOrUpdateObjectFromJson(AllTypes.class, "{}");
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void createOrUpdateObjectFromJson_withJsonObject() throws JSONException {
        realm.beginTransaction();

        AllTypesPrimaryKey obj = new AllTypesPrimaryKey();
        obj.setColumnLong(1);
        obj.setColumnString("Foo");
        realm.copyToRealm(obj);

        JSONObject json = new JSONObject();
        json.put("columnLong", 1);
        json.put("columnString", "bar");
        AllTypesPrimaryKey newObj = realm.createOrUpdateObjectFromJson(AllTypesPrimaryKey.class, json);

        realm.commitTransaction();

        assertEquals(1, realm.allObjects(AllTypesPrimaryKey.class).size());
        assertEquals("bar", newObj.getColumnString());
    }

    @Test
    public void createOrUpdateObjectFromJson_objectIgnoreUnsetProperties() throws IOException {
        String json = TestHelper.streamToString(TestHelper.loadJsonFromAssets(context, "list_alltypes_primarykey.json"));

        realm.beginTransaction();
        realm.createOrUpdateAllFromJson(AllTypesPrimaryKey.class, json);
        realm.commitTransaction();

        // No-op as no properties should be updated
        realm.beginTransaction();
        realm.createOrUpdateObjectFromJson(AllTypesPrimaryKey.class, "{ \"columnLong\":1 }");
        realm.commitTransaction();

        assertAllTypesPrimaryKeyUpdated();
    }

    @Test
    public void createOrUpdateObjectFromJson_streamIgnoreUnsetProperties() throws IOException {
        realm.beginTransaction();
        realm.createOrUpdateAllFromJson(AllTypesPrimaryKey.class, TestHelper.loadJsonFromAssets(context, "list_alltypes_primarykey.json"));
        realm.commitTransaction();

        // No-op as no properties should be updated
        realm.beginTransaction();
        realm.createOrUpdateObjectFromJson(AllTypesPrimaryKey.class, TestHelper.stringToStream("{ \"columnLong\":1 }"));
        realm.commitTransaction();

        assertAllTypesPrimaryKeyUpdated();
    }

    @Test
    public void createOrUpdateObjectFromJson_inputStream() throws IOException {
        realm.beginTransaction();

        AllTypesPrimaryKey obj = new AllTypesPrimaryKey();
        obj.setColumnLong(1);
        obj.setColumnString("Foo");
        realm.copyToRealm(obj);

        InputStream in = TestHelper.stringToStream("{ \"columnLong\" : 1, \"columnString\" : \"bar\" }");
        AllTypesPrimaryKey newObj = realm.createOrUpdateObjectFromJson(AllTypesPrimaryKey.class, in);
        realm.commitTransaction();

        assertEquals(1, realm.allObjects(AllTypesPrimaryKey.class).size());
        assertEquals("bar", newObj.getColumnString());
    }

    @Test
    public void createOrUpdateObjectFromJson_inputString() throws IOException {
        realm.beginTransaction();

        AllTypesPrimaryKey obj = new AllTypesPrimaryKey();
        obj.setColumnLong(1);
        obj.setColumnString("Foo");
        realm.copyToRealm(obj);

        AllTypesPrimaryKey newObj = realm.createOrUpdateObjectFromJson(AllTypesPrimaryKey.class, "{ \"columnLong\" : 1, \"columnString\" : \"bar\" }");
        realm.commitTransaction();

        assertEquals(1, realm.allObjects(AllTypesPrimaryKey.class).size());
        assertEquals("bar", newObj.getColumnString());
    }

    @Test
    public void createOrUpdateAllFromJson_jsonArrayNoPrimaryKeyThrows() {
        try {
            realm.createOrUpdateAllFromJson(AllTypes.class, new JSONArray());
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void createOrUpdateAllFromJson_streamNoPrimaryKeyThrows() throws IOException {
        try {
            realm.createOrUpdateAllFromJson(AllTypes.class, new TestHelper.StubInputStream());
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void createOrUpdateAllFromJson_streamInvalidJSonBracketThrows() throws IOException {
        try {
            realm.createOrUpdateAllFromJson(AllTypesPrimaryKey.class, TestHelper.stringToStream("["));
            fail();
        } catch (RealmException ignored) {
        }
    }

    @Test
    public void createOrUpdateAllFromJson_stringNoPrimaryKeyThrows() throws IOException {
        try {
            realm.createOrUpdateAllFromJson(AllTypes.class, "{}");
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void createOrUpdateAllFromJson_jsonArray() throws JSONException, IOException {
        String json = TestHelper.streamToString(TestHelper.loadJsonFromAssets(context, "list_alltypes_primarykey.json"));
        JSONArray array = new JSONArray(json);
        realm.beginTransaction();
        realm.createOrUpdateAllFromJson(AllTypesPrimaryKey.class, array);
        realm.commitTransaction();

        assertAllTypesPrimaryKeyUpdated();
    }

    @Test
    public void createOrUpdateAllFromJson_inputStream() throws IOException {
        realm.beginTransaction();
        realm.createOrUpdateAllFromJson(AllTypesPrimaryKey.class, TestHelper.loadJsonFromAssets(context, "list_alltypes_primarykey.json"));
        realm.commitTransaction();

        assertAllTypesPrimaryKeyUpdated();
    }

    @Test
    public void createOrUpdateAllFromJson_inputString() throws IOException {
        String json = TestHelper.streamToString(TestHelper.loadJsonFromAssets(context, "list_alltypes_primarykey.json"));
        realm.beginTransaction();
        realm.createOrUpdateAllFromJson(AllTypesPrimaryKey.class, json);
        realm.commitTransaction();

        assertAllTypesPrimaryKeyUpdated();
    }

    // Testing create objects from Json, all nullable fields with null values or non-null values
    @Test
    public void createAllFromJson_nullTypesJsonWithNulls() throws IOException, JSONException {
        String json = TestHelper.streamToString(TestHelper.loadJsonFromAssets(context, "nulltypes.json"));
        JSONArray array = new JSONArray(json);
        realm.beginTransaction();
        realm.createAllFromJson(NullTypes.class, array);
        realm.commitTransaction();

        RealmResults<NullTypes> nullTypesRealmResults = realm.allObjects(NullTypes.class);
        assertEquals(3, nullTypesRealmResults.size());

        NullTypes nullTypes1 = nullTypesRealmResults.where().equalTo("id", 1).findFirst();
        checkNullableValuesAreNull(nullTypes1);

        NullTypes nullTypes2 = nullTypesRealmResults.where().equalTo("id", 2).findFirst();
        checkNullableValuesAreNotNull(nullTypes2);
    }

    // Test creating objects form JSON stream, all nullable fields with null values or non-null values
    @Test
    public void createAllFromJson_nullTypesStreamJSONWithNulls() throws IOException {
        realm.beginTransaction();
        realm.createAllFromJson(NullTypes.class, TestHelper.loadJsonFromAssets(context, "nulltypes.json"));
        realm.commitTransaction();

        RealmResults<NullTypes> nullTypesRealmResults = realm.allObjects(NullTypes.class);
        assertEquals(3, nullTypesRealmResults.size());

        NullTypes nullTypes1 = nullTypesRealmResults.where().equalTo("id", 1).findFirst();
        checkNullableValuesAreNull(nullTypes1);

        NullTypes nullTypes2 = nullTypesRealmResults.where().equalTo("id", 2).findFirst();
        checkNullableValuesAreNotNull(nullTypes2);
    }

    /**
     * Test a nullable field already has a non-null value, update it through JSON with null value
     * of the corresponding field.
     */
    @Test
    public void createObjectFromJson_updateNullTypesJSONWithNulls() throws IOException, JSONException {
        String json = TestHelper.streamToString(TestHelper.loadJsonFromAssets(context, "nulltypes.json"));
        // Nullable fields with values
        JSONArray jsonArray = new JSONArray(json);
        JSONObject jsonObject = jsonArray.getJSONObject(1);
        jsonObject.put("id", 1);

        // Now object with id 1 has values for all nullable fields.
        realm.beginTransaction();
        realm.createObjectFromJson(NullTypes.class, jsonObject);
        realm.commitTransaction();

        RealmResults<NullTypes> nullTypesRealmResults = realm.allObjects(NullTypes.class);
        assertEquals(2, nullTypesRealmResults.size());
        checkNullableValuesAreNotNull(nullTypesRealmResults.first());

        // Update object with id 1, nullable fields should have null values
        JSONArray array = new JSONArray(json);
        realm.beginTransaction();
        realm.createOrUpdateAllFromJson(NullTypes.class, array);
        realm.commitTransaction();

        nullTypesRealmResults = realm.allObjects(NullTypes.class);
        assertEquals(3, nullTypesRealmResults.size());

        NullTypes nullTypes1 = nullTypesRealmResults.where().equalTo("id", 1).findFirst();
        checkNullableValuesAreNull(nullTypes1);
    }

    /**
     * If JSON has a field with value null, and corresponding object's field is not nullable,
     * an exception should be throw.
     */
    @Test
    public void createObjectFromJson_nullTypesJSONToNotNullFields() throws IOException, JSONException {
        String json = TestHelper.streamToString(TestHelper.loadJsonFromAssets(context, "nulltypes_invalid.json"));
        JSONArray array = new JSONArray(json);
        realm.beginTransaction();

        // 1 String
        try {
            realm.createObjectFromJson(NullTypes.class, array.getJSONObject(0));
            fail();
        } catch (RealmException expected) {
            assertTrue(expected.getCause() instanceof IllegalArgumentException);
        }
        // 2 Bytes
        try {
            realm.createObjectFromJson(NullTypes.class, array.getJSONObject(1));
            fail();
        } catch (RealmException expected) {
            assertTrue(expected.getCause() instanceof IllegalArgumentException);
        }
        // 3 Boolean
        try {
            realm.createObjectFromJson(NullTypes.class, array.getJSONObject(2));
            fail();
        } catch (RealmException expected) {
            assertTrue(expected.getCause() instanceof IllegalArgumentException);
        }
        // 4 Byte
        try {
            realm.createObjectFromJson(NullTypes.class, array.getJSONObject(3));
            fail();
        } catch (RealmException expected) {
            assertTrue(expected.getCause() instanceof IllegalArgumentException);
        }
        // 5 Short
        try {
            realm.createObjectFromJson(NullTypes.class, array.getJSONObject(4));
            fail();
        } catch (RealmException expected) {
            assertTrue(expected.getCause() instanceof IllegalArgumentException);
        }
        // 6 Integer
        try {
            realm.createObjectFromJson(NullTypes.class, array.getJSONObject(5));
            fail();
        } catch (RealmException expected) {
            assertTrue(expected.getCause() instanceof IllegalArgumentException);
        }
        // 7 Long
        try {
            realm.createObjectFromJson(NullTypes.class, array.getJSONObject(6));
            fail();
        } catch (RealmException expected) {
            assertTrue(expected.getCause() instanceof IllegalArgumentException);
        }
        // 8 Float
        try {
            realm.createObjectFromJson(NullTypes.class, array.getJSONObject(7));
            fail();
        } catch (RealmException expected) {
            assertTrue(expected.getCause() instanceof IllegalArgumentException);
        }
        // 9 Double
        try {
            realm.createObjectFromJson(NullTypes.class, array.getJSONObject(8));
            fail();
        } catch (RealmException expected) {
            assertTrue(expected.getCause() instanceof IllegalArgumentException);
        }
        // 10 Date
        try {
            realm.createObjectFromJson(NullTypes.class, array.getJSONObject(9));
            fail();
        } catch (RealmException expected) {
            assertTrue(expected.getCause() instanceof IllegalArgumentException);
        }

        realm.cancelTransaction();
    }

    /**
     * If JSON has a field with value null, and corresponding object's field is not nullable,
     * an exception should be throw. Stream version.
     */
    @Test
    public void createObjectFromJson_nullTypesJSONStreamToNotNullFields() throws IOException, JSONException {
        String json = TestHelper.streamToString(TestHelper.loadJsonFromAssets(context, "nulltypes_invalid.json"));
        JSONArray array = new JSONArray(json);

        // 1 String
        try {
            realm.beginTransaction();
            realm.createObjectFromJson(NoPrimaryKeyNullTypes.class, convertJsonObjectToStream(array.getJSONObject(0)));
            fail();
        } catch (IllegalArgumentException ignored) {
        } finally {
            realm.cancelTransaction();
        }
        // 2 Bytes
        try {
            realm.beginTransaction();
            realm.createObjectFromJson(NoPrimaryKeyNullTypes.class, convertJsonObjectToStream(array.getJSONObject(1)));
            fail();
        } catch (IllegalArgumentException ignored) {
        } finally {
            realm.cancelTransaction();
        }
        // 3 Boolean
        try {
            realm.beginTransaction();
            realm.createObjectFromJson(NoPrimaryKeyNullTypes.class, convertJsonObjectToStream(array.getJSONObject(2)));
            fail();
        } catch (IllegalArgumentException ignored) {
        } finally {
            realm.cancelTransaction();
        }
        // 4 Byte
        try {
            realm.beginTransaction();
            realm.createObjectFromJson(NoPrimaryKeyNullTypes.class, convertJsonObjectToStream(array.getJSONObject(3)));
            fail();
        } catch (IllegalArgumentException ignored) {
        } finally {
            realm.cancelTransaction();
        }
        // 5 Short
        try {
            realm.beginTransaction();
            realm.createObjectFromJson(NoPrimaryKeyNullTypes.class, convertJsonObjectToStream(array.getJSONObject(4)));
            fail();
        } catch (IllegalArgumentException ignored) {
        } finally {
            realm.cancelTransaction();
        }
        // 6 Integer
        try {
            realm.beginTransaction();
            realm.createObjectFromJson(NoPrimaryKeyNullTypes.class, convertJsonObjectToStream(array.getJSONObject(5)));
            fail();
        } catch (IllegalArgumentException ignored) {
        } finally {
            realm.cancelTransaction();
        }
        // 7 Long
        try {
            realm.beginTransaction();
            realm.createObjectFromJson(NoPrimaryKeyNullTypes.class, convertJsonObjectToStream(array.getJSONObject(6)));
            fail();
        } catch (IllegalArgumentException ignored) {
        } finally {
            realm.cancelTransaction();
        }
        // 8 Float
        try {
            realm.beginTransaction();
            realm.createObjectFromJson(NoPrimaryKeyNullTypes.class, convertJsonObjectToStream(array.getJSONObject(7)));
            fail();
        } catch (IllegalArgumentException ignored) {
        } finally {
            realm.cancelTransaction();
        }
        // 9 Double
        try {
            realm.beginTransaction();
            realm.createObjectFromJson(NoPrimaryKeyNullTypes.class, convertJsonObjectToStream(array.getJSONObject(8)));
            fail();
        } catch (IllegalArgumentException ignored) {
        } finally {
            realm.cancelTransaction();
        }
        // 10 Date
        try {
            realm.beginTransaction();
            realm.createObjectFromJson(NoPrimaryKeyNullTypes.class, convertJsonObjectToStream(array.getJSONObject(9)));
            fail();
        } catch (IllegalArgumentException ignored) {
        } finally {
            realm.cancelTransaction();
        }
    }

    /**
     * Check that using createOrUpdateObject will set the primary key directly instead of first setting
     * it to the default value (which can fail)
     */
    @Test
    public void createOrUpdateObjectFromJson_objectWithPrimaryKeySetValueDirectlyFromJsonObject() throws JSONException {
        JSONObject newObject = new JSONObject("{\"id\": 1, \"name\": \"bar\"}");
        realm.beginTransaction();
        realm.createObject(OwnerPrimaryKey.class); // id = 0
        realm.createOrUpdateObjectFromJson(OwnerPrimaryKey.class, newObject);
        realm.commitTransaction();

        RealmResults<OwnerPrimaryKey> owners = realm.where(OwnerPrimaryKey.class).findAll();
        assertEquals(2, owners.size());
        assertEquals(1, owners.get(1).getId());
        assertEquals("bar", owners.get(1).getName());
    }

    /**
     * Check that using createOrUpdateObject will set the primary key directly instead of first setting
     * it to the default value (which can fail)
     */
    @Test
    public void createObjectFromJson_objectWithPrimaryKeySetValueDirectlyFromJsonObject() throws JSONException {
        JSONObject newObject = new JSONObject("{\"id\": 1, \"name\": \"bar\"}");
        realm.beginTransaction();
        realm.createObject(OwnerPrimaryKey.class); // id = 0
        realm.createObjectFromJson(OwnerPrimaryKey.class, newObject);
        realm.commitTransaction();

        RealmResults<OwnerPrimaryKey> owners = realm.where(OwnerPrimaryKey.class).findAll();
        assertEquals(2, owners.size());
        assertEquals(1, owners.get(1).getId());
        assertEquals("bar", owners.get(1).getName());
    }

    /**
     * Check that using createOrUpdateObject will set the primary key directly instead of first setting
     * it to the default value (which can fail)
     */
    @Test
    public void createOrUpdateObjectFromJson_objectWithPrimaryKeySetValueDirectlyFromStream() throws JSONException, IOException {
        InputStream stream = TestHelper.stringToStream("{\"id\": 1, \"name\": \"bar\"}");
        realm.beginTransaction();
        realm.createObject(OwnerPrimaryKey.class); // id = 0
        realm.createOrUpdateObjectFromJson(OwnerPrimaryKey.class, stream);
        realm.commitTransaction();

        RealmResults<OwnerPrimaryKey> owners = realm.where(OwnerPrimaryKey.class).findAll();
        assertEquals(2, owners.size());
        assertEquals(1, owners.get(1).getId());
        assertEquals("bar", owners.get(1).getName());
    }

    /**
     * createObject using primary keys doesn't work if the Check that using createOrUpdateObject
     * will set the primary key directly instead of first setting it to the default value (which can fail)
     */
    @Test
    public void createObjectFromJson_objectWithPrimaryKeySetValueDirectlyFromStream() throws JSONException, IOException {
        InputStream stream = TestHelper.stringToStream("{\"id\": 1, \"name\": \"bar\"}");
        realm.beginTransaction();
        realm.createObject(OwnerPrimaryKey.class); // id = 0
        realm.createObjectFromJson(OwnerPrimaryKey.class, stream);
        realm.commitTransaction();

        RealmResults<OwnerPrimaryKey> owners = realm.where(OwnerPrimaryKey.class).findAll();
        assertEquals(2, owners.size());
        assertEquals(1, owners.get(1).getId());
        assertEquals("bar", owners.get(1).getName());
    }

    // Nullable PrimaryKey tests for PrimaryKeyAsString
    @Test
    public void createObjectFromJson_stringPrimaryKeyFieldIsNullFromJsonObject() throws IOException, JSONException {
        realm.beginTransaction();
        realm.createObjectFromJson(PrimaryKeyAsString.class, new JSONObject("{ \"name\":null, \"id\":42 }"));
        realm.commitTransaction();

        RealmResults<PrimaryKeyAsString> results = realm.allObjects(PrimaryKeyAsString.class);
        assertEquals(1, results.size());
        assertEquals(null, results.first().getName());
        assertEquals(42, results.first().getId());
    }

    @Test
    public void createObjectFromJson_stringPrimaryKeyFieldIsAbsentFromJsonObject() throws IOException, JSONException {
        realm.beginTransaction();
        realm.createObjectFromJson(PrimaryKeyAsString.class, new JSONObject("{ \"id\":42 }"));
        realm.commitTransaction();

        RealmResults<PrimaryKeyAsString> results = realm.allObjects(PrimaryKeyAsString.class);
        assertEquals(1, results.size());
        assertEquals(null, results.first().getName());
        assertEquals(42, results.first().getId());
    }

    @Test
    public void createOrUpdateObjectFromJson_stringPrimaryKeyFieldIsNullFromJsonObject() throws JSONException {
        realm.beginTransaction();
        realm.createOrUpdateObjectFromJson(PrimaryKeyAsString.class, new JSONObject("{ \"name\":null, \"id\":42 }"));
        realm.commitTransaction();

        RealmResults<PrimaryKeyAsString> results = realm.allObjects(PrimaryKeyAsString.class);
        assertEquals(1, results.size());
        assertEquals(null, results.first().getName());
        assertEquals(42, results.first().getId());
    }

    @Test
    public void createOrUpdateObjectFromJson_stringPrimaryKeyFieldIsAbsentFromJsonObject() throws JSONException {
        realm.beginTransaction();
        realm.createOrUpdateObjectFromJson(PrimaryKeyAsString.class, new JSONObject("{ \"id\":42 }"));
        realm.commitTransaction();

        RealmResults<PrimaryKeyAsString> results = realm.allObjects(PrimaryKeyAsString.class);
        assertEquals(1, results.size());
        assertEquals(null, results.first().getName());
        assertEquals(42, results.first().getId());
    }

    @Test
    public void createOrUpdateObjectFromJson_stringPrimaryKeyFieldIsNullUpdateFromJsonObject() throws JSONException {
        realm.beginTransaction();
        realm.createObject(PrimaryKeyAsString.class); // name = null, id = 0
        realm.createOrUpdateObjectFromJson(PrimaryKeyAsString.class, new JSONObject("{ \"name\":null, \"id\":42 }"));
        realm.commitTransaction();

        RealmResults<PrimaryKeyAsString> results = realm.allObjects(PrimaryKeyAsString.class);
        assertEquals(1, results.size());
        assertEquals(null, results.first().getName());
        assertEquals(42, results.first().getId());
    }

    @Test
    public void createOrUpdateObjectFromJson_stringPrimaryKeyFieldIsAbsentUpdateFromJsonObject() throws JSONException {
        realm.beginTransaction();
        realm.createObject(PrimaryKeyAsString.class); // name = null, id = 0
        realm.createOrUpdateObjectFromJson(PrimaryKeyAsString.class, new JSONObject("{ \"id\":42 }"));
        realm.commitTransaction();

        RealmResults<PrimaryKeyAsString> results = realm.allObjects(PrimaryKeyAsString.class);
        assertEquals(1, results.size());
        assertEquals(null, results.first().getName());
        assertEquals(42, results.first().getId());
    }

    // Nullable PrimaryKey tests for PrimaryKeyAsBoxedByte
    @Test
    public void createObjectFromJson_bytePrimaryKeyFieldIsNullFromJsonObject() throws IOException, JSONException {
        realm.beginTransaction();
        realm.createObjectFromJson(PrimaryKeyAsBoxedByte.class, new JSONObject("{ \"id\":null, \"name\":\"nullPrimaryKeyObj\" }"));
        realm.commitTransaction();

        RealmResults<PrimaryKeyAsBoxedByte> results = realm.allObjects(PrimaryKeyAsBoxedByte.class);
        assertEquals(1, results.size());
        assertEquals("nullPrimaryKeyObj", results.first().getName());
        assertEquals(null, results.first().getId());
    }

    @Test
    public void createObjectFromJson_bytePrimaryKeyFieldIsAbsentFromJsonObject() throws IOException, JSONException {
        realm.beginTransaction();
        realm.createObjectFromJson(PrimaryKeyAsBoxedByte.class, new JSONObject("{ \"name\":\"nullPrimaryKeyObj\" }"));
        realm.commitTransaction();

        RealmResults<PrimaryKeyAsBoxedByte> results = realm.allObjects(PrimaryKeyAsBoxedByte.class);
        assertEquals(1, results.size());
        assertEquals("nullPrimaryKeyObj", results.first().getName());
        assertEquals(null, results.first().getId());
    }

    @Test
    public void createOrUpdateObjectFromJson_bytePrimaryKeyFieldIsNullFromJsonObject() throws IOException, JSONException {
        realm.beginTransaction();
        realm.createOrUpdateObjectFromJson(PrimaryKeyAsBoxedByte.class, new JSONObject("{ \"id\":null, \"name\":\"nullPrimaryKeyObj\" }"));
        realm.commitTransaction();

        RealmResults<PrimaryKeyAsBoxedByte> results = realm.allObjects(PrimaryKeyAsBoxedByte.class);
        assertEquals(1, results.size());
        assertEquals("nullPrimaryKeyObj", results.first().getName());
        assertEquals(null, results.first().getId());
    }

    @Test
    public void createOrUpdateObjectFromJson_bytePrimaryKeyFieldIsAbsentFromJsonObject() throws IOException, JSONException {
        realm.beginTransaction();
        realm.createOrUpdateObjectFromJson(PrimaryKeyAsBoxedByte.class, new JSONObject("{ \"name\":\"nullPrimaryKeyObj\" }"));
        realm.commitTransaction();

        RealmResults<PrimaryKeyAsBoxedByte> results = realm.allObjects(PrimaryKeyAsBoxedByte.class);
        assertEquals(1, results.size());
        assertEquals("nullPrimaryKeyObj", results.first().getName());
        assertEquals(null, results.first().getId());
    }

    @Test
    public void createOrUpdateObjectFromJson_bytePrimaryKeyFieldIsNullUpdateFromJsonObject() throws IOException, JSONException {
        realm.beginTransaction();
        realm.createObject(PrimaryKeyAsBoxedByte.class); // id = null, name = null
        realm.createOrUpdateObjectFromJson(PrimaryKeyAsBoxedByte.class, new JSONObject("{ \"id\":null, \"name\":\"nullPrimaryKeyObj\" }"));
        realm.commitTransaction();

        RealmResults<PrimaryKeyAsBoxedByte> results = realm.allObjects(PrimaryKeyAsBoxedByte.class);
        assertEquals(1, results.size());
        assertEquals("nullPrimaryKeyObj", results.first().getName());
        assertEquals(null, results.first().getId());
    }

    @Test
    public void createOrUpdateObjectFromJson_bytePrimaryKeyFieldIsAbsentUpdateFromJsonObject() throws IOException, JSONException {
        realm.beginTransaction();
        realm.createObject(PrimaryKeyAsBoxedByte.class); // id = null, name = null
        realm.createOrUpdateObjectFromJson(PrimaryKeyAsBoxedByte.class, new JSONObject("{ \"name\":\"nullPrimaryKeyObj\" }"));
        realm.commitTransaction();

        RealmResults<PrimaryKeyAsBoxedByte> results = realm.allObjects(PrimaryKeyAsBoxedByte.class);
        assertEquals(1, results.size());
        assertEquals("nullPrimaryKeyObj", results.first().getName());
        assertEquals(null, results.first().getId());
    }

    // Nullable PrimaryKey tests for PrimaryKeyAsBoxedShort
    @Test
    public void createObjectFromJson_shortPrimaryKeyFieldIsNullFromJsonObject() throws IOException, JSONException {
        realm.beginTransaction();
        realm.createObjectFromJson(PrimaryKeyAsBoxedShort.class, new JSONObject("{ \"id\":null, \"name\":\"nullPrimaryKeyObj\" }"));
        realm.commitTransaction();

        RealmResults<PrimaryKeyAsBoxedShort> results = realm.allObjects(PrimaryKeyAsBoxedShort.class);
        assertEquals(1, results.size());
        assertEquals("nullPrimaryKeyObj", results.first().getName());
        assertEquals(null, results.first().getId());
    }

    @Test
    public void createObjectFromJson_shortPrimaryKeyFieldIsAbsentFromJsonObject() throws IOException, JSONException {
        realm.beginTransaction();
        realm.createObjectFromJson(PrimaryKeyAsBoxedShort.class, new JSONObject("{ \"name\":\"nullPrimaryKeyObj\" }"));
        realm.commitTransaction();

        RealmResults<PrimaryKeyAsBoxedShort> results = realm.allObjects(PrimaryKeyAsBoxedShort.class);
        assertEquals(1, results.size());
        assertEquals("nullPrimaryKeyObj", results.first().getName());
        assertEquals(null, results.first().getId());
    }

    @Test
    public void createOrUpdateObjectFromJson_shortPrimaryKeyFieldIsNullFromJsonObject() throws IOException, JSONException {
        realm.beginTransaction();
        realm.createOrUpdateObjectFromJson(PrimaryKeyAsBoxedShort.class, new JSONObject("{ \"id\":null, \"name\":\"nullPrimaryKeyObj\" }"));
        realm.commitTransaction();

        RealmResults<PrimaryKeyAsBoxedShort> results = realm.allObjects(PrimaryKeyAsBoxedShort.class);
        assertEquals(1, results.size());
        assertEquals("nullPrimaryKeyObj", results.first().getName());
        assertEquals(null, results.first().getId());
    }

    @Test
    public void createOrUpdateObjectFromJson_shortPrimaryKeyFieldIsAbsentFromJsonObject() throws IOException, JSONException {
        realm.beginTransaction();
        realm.createOrUpdateObjectFromJson(PrimaryKeyAsBoxedShort.class, new JSONObject("{ \"name\":\"nullPrimaryKeyObj\" }"));
        realm.commitTransaction();

        RealmResults<PrimaryKeyAsBoxedShort> results = realm.allObjects(PrimaryKeyAsBoxedShort.class);
        assertEquals(1, results.size());
        assertEquals("nullPrimaryKeyObj", results.first().getName());
        assertEquals(null, results.first().getId());
    }

    @Test
    public void createOrUpdateObjectFromJson_shortPrimaryKeyFieldIsNullUpdateFromJsonObject() throws IOException, JSONException {
        realm.beginTransaction();
        realm.createObject(PrimaryKeyAsBoxedShort.class); // id = null, name = null
        realm.createOrUpdateObjectFromJson(PrimaryKeyAsBoxedShort.class, new JSONObject("{ \"id\":null, \"name\":\"nullPrimaryKeyObj\" }"));
        realm.commitTransaction();

        RealmResults<PrimaryKeyAsBoxedShort> results = realm.allObjects(PrimaryKeyAsBoxedShort.class);
        assertEquals(1, results.size());
        assertEquals("nullPrimaryKeyObj", results.first().getName());
        assertEquals(null, results.first().getId());
    }

    @Test
    public void createOrUpdateObjectFromJson_shortPrimaryKeyFieldIsAbsentUpdateFromJsonObject() throws IOException, JSONException {
        realm.beginTransaction();
        realm.createObject(PrimaryKeyAsBoxedShort.class); // id = null, name = null
        realm.createOrUpdateObjectFromJson(PrimaryKeyAsBoxedShort.class, new JSONObject("{ \"name\":\"nullPrimaryKeyObj\" }"));
        realm.commitTransaction();

        RealmResults<PrimaryKeyAsBoxedShort> results = realm.allObjects(PrimaryKeyAsBoxedShort.class);
        assertEquals(1, results.size());
        assertEquals("nullPrimaryKeyObj", results.first().getName());
        assertEquals(null, results.first().getId());
    }

    // Nullable PrimaryKey tests for PrimaryKeyAsBoxedInteger
    @Test
    public void createObjectFromJson_integerPrimaryKeyFieldIsNullFromJsonObject() throws IOException, JSONException {
        realm.beginTransaction();
        realm.createObjectFromJson(PrimaryKeyAsBoxedInteger.class, new JSONObject("{ \"id\":null, \"name\":\"nullPrimaryKeyObj\" }"));
        realm.commitTransaction();

        RealmResults<PrimaryKeyAsBoxedInteger> results = realm.allObjects(PrimaryKeyAsBoxedInteger.class);
        assertEquals(1, results.size());
        assertEquals("nullPrimaryKeyObj", results.first().getName());
        assertEquals(null, results.first().getId());
    }

    @Test
    public void createObjectFromJson_integerPrimaryKeyFieldIsAbsentFromJsonObject() throws IOException, JSONException {
        realm.beginTransaction();
        realm.createObjectFromJson(PrimaryKeyAsBoxedInteger.class, new JSONObject("{ \"name\":\"nullPrimaryKeyObj\" }"));
        realm.commitTransaction();

        RealmResults<PrimaryKeyAsBoxedInteger> results = realm.allObjects(PrimaryKeyAsBoxedInteger.class);
        assertEquals(1, results.size());
        assertEquals("nullPrimaryKeyObj", results.first().getName());
        assertEquals(null, results.first().getId());
    }

    @Test
    public void createOrUpdateObjectFromJson_integerPrimaryKeyFieldIsNullFromJsonObject() throws IOException, JSONException {
        realm.beginTransaction();
        realm.createOrUpdateObjectFromJson(PrimaryKeyAsBoxedInteger.class, new JSONObject("{ \"id\":null, \"name\":\"nullPrimaryKeyObj\" }"));
        realm.commitTransaction();

        RealmResults<PrimaryKeyAsBoxedInteger> results = realm.allObjects(PrimaryKeyAsBoxedInteger.class);
        assertEquals(1, results.size());
        assertEquals("nullPrimaryKeyObj", results.first().getName());
        assertEquals(null, results.first().getId());
    }

    @Test
    public void createOrUpdateObjectFromJson_integerPrimaryKeyFieldIsAbsentFromJsonObject() throws IOException, JSONException {
        realm.beginTransaction();
        realm.createOrUpdateObjectFromJson(PrimaryKeyAsBoxedInteger.class, new JSONObject("{ \"name\":\"nullPrimaryKeyObj\" }"));
        realm.commitTransaction();

        RealmResults<PrimaryKeyAsBoxedInteger> results = realm.allObjects(PrimaryKeyAsBoxedInteger.class);
        assertEquals(1, results.size());
        assertEquals("nullPrimaryKeyObj", results.first().getName());
        assertEquals(null, results.first().getId());
    }

    @Test
    public void createOrUpdateObjectFromJson_integerPrimaryKeyFieldIsNullUpdateFromJsonObject() throws IOException, JSONException {
        realm.beginTransaction();
        realm.createObject(PrimaryKeyAsBoxedInteger.class); // id = null, name = null
        realm.createOrUpdateObjectFromJson(PrimaryKeyAsBoxedInteger.class, new JSONObject("{ \"id\":null, \"name\":\"nullPrimaryKeyObj\" }"));
        realm.commitTransaction();

        RealmResults<PrimaryKeyAsBoxedInteger> results = realm.allObjects(PrimaryKeyAsBoxedInteger.class);
        assertEquals(1, results.size());
        assertEquals("nullPrimaryKeyObj", results.first().getName());
        assertEquals(null, results.first().getId());
    }

    @Test
    public void createOrUpdateObjectFromJson_integerPrimaryKeyFieldIsAbsentUpdateFromJsonObject() throws IOException, JSONException {
        realm.beginTransaction();
        realm.createObject(PrimaryKeyAsBoxedInteger.class); // id = null, name = null
        realm.createOrUpdateObjectFromJson(PrimaryKeyAsBoxedInteger.class, new JSONObject("{ \"name\":\"nullPrimaryKeyObj\" }"));
        realm.commitTransaction();

        RealmResults<PrimaryKeyAsBoxedInteger> results = realm.allObjects(PrimaryKeyAsBoxedInteger.class);
        assertEquals(1, results.size());
        assertEquals("nullPrimaryKeyObj", results.first().getName());
        assertEquals(null, results.first().getId());
    }

    // Nullable PrimaryKey tests for PrimaryKeyAsBoxedLong
    @Test
    public void createObjectFromJson_longPrimaryKeyFieldIsNullFromJsonObject() throws IOException, JSONException {
        realm.beginTransaction();
        realm.createObjectFromJson(PrimaryKeyAsBoxedLong.class, new JSONObject("{ \"id\":null, \"name\":\"nullPrimaryKeyObj\" }"));
        realm.commitTransaction();

        RealmResults<PrimaryKeyAsBoxedLong> results = realm.allObjects(PrimaryKeyAsBoxedLong.class);
        assertEquals(1, results.size());
        assertEquals("nullPrimaryKeyObj", results.first().getName());
        assertEquals(null, results.first().getId());
    }

    @Test
    public void createObjectFromJson_longPrimaryKeyFieldIsAbsentFromJsonObject() throws IOException, JSONException {
        realm.beginTransaction();
        realm.createObjectFromJson(PrimaryKeyAsBoxedLong.class, new JSONObject("{ \"name\":\"nullPrimaryKeyObj\" }"));
        realm.commitTransaction();

        RealmResults<PrimaryKeyAsBoxedLong> results = realm.allObjects(PrimaryKeyAsBoxedLong.class);
        assertEquals(1, results.size());
        assertEquals("nullPrimaryKeyObj", results.first().getName());
        assertEquals(null, results.first().getId());
    }

    @Test
    public void createOrUpdateObjectFromJson_longPrimaryKeyFieldIsNullFromJsonObject() throws IOException, JSONException {
        realm.beginTransaction();
        realm.createOrUpdateObjectFromJson(PrimaryKeyAsBoxedLong.class, new JSONObject("{ \"id\":null, \"name\":\"nullPrimaryKeyObj\" }"));
        realm.commitTransaction();

        RealmResults<PrimaryKeyAsBoxedLong> results = realm.allObjects(PrimaryKeyAsBoxedLong.class);
        assertEquals(1, results.size());
        assertEquals("nullPrimaryKeyObj", results.first().getName());
        assertEquals(null, results.first().getId());
    }

    @Test
    public void createOrUpdateObjectFromJson_longPrimaryKeyFieldIsAbsentFromJsonObject() throws IOException, JSONException {
        realm.beginTransaction();
        realm.createOrUpdateObjectFromJson(PrimaryKeyAsBoxedLong.class, new JSONObject("{ \"name\":\"nullPrimaryKeyObj\" }"));
        realm.commitTransaction();

        RealmResults<PrimaryKeyAsBoxedLong> results = realm.allObjects(PrimaryKeyAsBoxedLong.class);
        assertEquals(1, results.size());
        assertEquals("nullPrimaryKeyObj", results.first().getName());
        assertEquals(null, results.first().getId());
    }

    @Test
    public void createOrUpdateObjectFromJson_longPrimaryKeyFieldIsNullUpdateFromJsonObject() throws IOException, JSONException {
        realm.beginTransaction();
        realm.createObject(PrimaryKeyAsBoxedLong.class); // id = null, name = null
        realm.createOrUpdateObjectFromJson(PrimaryKeyAsBoxedLong.class, new JSONObject("{ \"id\":null, \"name\":\"nullPrimaryKeyObj\" }"));
        realm.commitTransaction();

        RealmResults<PrimaryKeyAsBoxedLong> results = realm.allObjects(PrimaryKeyAsBoxedLong.class);
        assertEquals(1, results.size());
        assertEquals("nullPrimaryKeyObj", results.first().getName());
        assertEquals(null, results.first().getId());
    }

    @Test
    public void createOrUpdateObjectFromJson_longPrimaryKeyFieldIsAbsentUpdateFromJsonObject() throws IOException, JSONException {
        realm.beginTransaction();
        realm.createObject(PrimaryKeyAsBoxedLong.class); // id = null, name = null
        realm.createOrUpdateObjectFromJson(PrimaryKeyAsBoxedLong.class, new JSONObject("{ \"name\":\"nullPrimaryKeyObj\" }"));
        realm.commitTransaction();

        RealmResults<PrimaryKeyAsBoxedLong> results = realm.allObjects(PrimaryKeyAsBoxedLong.class);
        assertEquals(1, results.size());
        assertEquals("nullPrimaryKeyObj", results.first().getName());
        assertEquals(null, results.first().getId());
    }
}
