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

import android.content.res.AssetManager;
import android.test.AndroidTestCase;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;

import io.realm.entities.AllTypes;
import io.realm.entities.AllTypesPrimaryKey;
import io.realm.entities.AnnotationTypes;
import io.realm.entities.Dog;
import io.realm.entities.NullTypes;
import io.realm.exceptions.RealmException;

import static io.realm.internal.test.ExtraTests.assertArrayEquals;

public class RealmJsonTest extends AndroidTestCase {

    protected Realm testRealm;

    @Override
    protected void setUp() throws Exception {
        RealmConfiguration realmConfig = TestHelper.createConfiguration(getContext());
        Realm.deleteRealm(realmConfig);
        testRealm = Realm.getInstance(realmConfig);
    }

    @Override
    protected void tearDown() throws Exception {
        testRealm.close();
    }

    private InputStream loadJsonFromAssets(String file) throws IOException {
        AssetManager assetManager = getContext().getAssets();
        return assetManager.open(file);
    }

    private InputStream convertJsonObjectToStream(JSONObject obj) {
        return new ByteArrayInputStream(obj.toString().getBytes());
    }

    public void testCreateObjectFromJson_nullObject() {
        testRealm.createObjectFromJson(AllTypes.class, (JSONObject) null);
        assertEquals(0, testRealm.allObjects(AllTypes.class).size());
    }

    public void testCreateObjectFromJson_nullArray() {
        testRealm.createAllFromJson(AllTypes.class, (JSONArray) null);
        assertEquals(0, testRealm.allObjects(AllTypes.class).size());

    }

    public void testCreateObjectFromJson_allSimpleObjectAllTypes() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("columnString", "String");
        json.put("columnLong", 1l);
        json.put("columnFloat", 1.23f);
        json.put("columnDouble", 1.23d);
        json.put("columnBoolean", true);
        json.put("columnBinary", new String(Base64.encode(new byte[] {1,2,3}, Base64.DEFAULT)));

        testRealm.beginTransaction();
        testRealm.createObjectFromJson(AllTypes.class, json);
        testRealm.commitTransaction();
        AllTypes obj = testRealm.allObjects(AllTypes.class).first();

        // Check that all primitive types are imported correctly
        assertEquals("String", obj.getColumnString());
        assertEquals(1l, obj.getColumnLong());
        assertEquals(1.23f, obj.getColumnFloat());
        assertEquals(1.23d, obj.getColumnDouble());
        assertEquals(true, obj.isColumnBoolean());
        assertArrayEquals(new byte[]{1, 2, 3}, obj.getColumnBinary());
    }

    public void testCreateObjectFromJson_dateAsLong() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("columnDate", 1000L); // Realm operates at seconds level granularity

        testRealm.beginTransaction();
        testRealm.createObjectFromJson(AllTypes.class, json);
        testRealm.commitTransaction();

        AllTypes obj = testRealm.allObjects(AllTypes.class).first();
        assertEquals(new Date(1000), obj.getColumnDate());
    }

    public void testCreateObjectFromJson_dateAsString() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("columnDate", "/Date(1000)/");

        testRealm.beginTransaction();
        testRealm.createObjectFromJson(AllTypes.class, json);
        testRealm.commitTransaction();

        AllTypes obj = testRealm.allObjects(AllTypes.class).first();
        assertEquals(new Date(1000), obj.getColumnDate());
    }

    public void testCreateObjectFromJson_childObject() throws JSONException {
        JSONObject allTypesObject = new JSONObject();
        JSONObject dogObject = new JSONObject();
        dogObject.put("name", "Fido");
        allTypesObject.put("columnRealmObject", dogObject);

        testRealm.beginTransaction();
        testRealm.createObjectFromJson(AllTypes.class, allTypesObject);
        testRealm.commitTransaction();

        AllTypes obj = testRealm.allObjects(AllTypes.class).first();
        assertEquals("Fido", obj.getColumnRealmObject().getName());
    }

    public void testCreateObjectFromJson_childObjectList() throws JSONException {
        JSONObject allTypesObject = new JSONObject();
        JSONObject dog1 = new JSONObject(); dog1.put("name", "Fido-1");
        JSONObject dog2 = new JSONObject(); dog2.put("name", "Fido-2");
        JSONObject dog3 = new JSONObject(); dog3.put("name", "Fido-3");
        JSONArray dogList = new JSONArray();
        dogList.put(dog1);
        dogList.put(dog2);
        dogList.put(dog3);

        allTypesObject.put("columnRealmList", dogList);

        testRealm.beginTransaction();
        testRealm.createObjectFromJson(AllTypes.class, allTypesObject);
        testRealm.commitTransaction();

        AllTypes obj = testRealm.allObjects(AllTypes.class).first();
        assertEquals(3, obj.getColumnRealmList().size());
        assertEquals("Fido-3", obj.getColumnRealmList().get(2).getName());
    }

    public void testCreateObjectFromJson_emptyChildObjectList() throws JSONException {
        JSONObject allTypesObject = new JSONObject();
        JSONArray dogList = new JSONArray();

        allTypesObject.put("columnRealmList", dogList);

        testRealm.beginTransaction();
        testRealm.createObjectFromJson(AllTypes.class, allTypesObject);
        testRealm.commitTransaction();

        AllTypes obj = testRealm.allObjects(AllTypes.class).first();
        assertEquals(0, obj.getColumnRealmList().size());
    }

    public void testCreateObjectFromJsonString_simpleObject() {
        testRealm.beginTransaction();
        Dog dog = testRealm.createObjectFromJson(Dog.class, "{ name: \"Foo\" }");
        testRealm. commitTransaction();

        assertEquals("Foo", dog.getName());
        assertEquals("Foo", testRealm.allObjects(Dog.class).first().getName());
    }


    public void testCreateObjectFromJsonString_faultyJsonThrows() {
        testRealm.beginTransaction();
        try {
            testRealm.createObjectFromJson(Dog.class, "{ name \"Foo\" }");
        } catch (RealmException e) {
            return;
        } finally {
            testRealm.commitTransaction();
        }

        fail("Faulty JSON should result in a RealmException");
    }


    public void testCreateObjectFromJsonString_null() {
        testRealm.beginTransaction();
        Dog dog = testRealm.createObjectFromJson(Dog.class, (String) null);
        testRealm.commitTransaction();

        //noinspection ConstantConditions
        assertNull(dog);
        assertEquals(0, testRealm.allObjects(Dog.class).size());
    }

    public void testCreateAllFromJsonArray_empty() {
        JSONArray array = new JSONArray();
        testRealm.beginTransaction();
        testRealm.createAllFromJson(AllTypes.class, array);
        testRealm.commitTransaction();

        assertEquals(0, testRealm.allObjects(AllTypes.class).size());
    }

    public void testCreateAllFromJsonArray() throws JSONException {
        JSONObject dog1 = new JSONObject(); dog1.put("name", "Fido-1");
        JSONObject dog2 = new JSONObject(); dog2.put("name", "Fido-2");
        JSONObject dog3 = new JSONObject(); dog3.put("name", "Fido-3");
        JSONArray dogList = new JSONArray();
        dogList.put(dog1);
        dogList.put(dog2);
        dogList.put(dog3);

        testRealm.beginTransaction();
        testRealm.createAllFromJson(Dog.class, dogList);
        testRealm.commitTransaction();

        assertEquals(3, testRealm.allObjects(Dog.class).size());
        assertEquals(1, testRealm.where(Dog.class).equalTo("name", "Fido-3").findAll().size());
    }

    // Test if Json object doesn't have the field, then the field should have default value.
    public void testCreateObjectFromJson_noValues() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("noThingHere", JSONObject.NULL);

        testRealm.beginTransaction();
        testRealm.createObjectFromJson(AllTypes.class, json);
        testRealm.commitTransaction();
        AllTypes obj = testRealm.allObjects(AllTypes.class).first();

        // Check that all primitive types are imported correctly
        assertEquals("", obj.getColumnString());
        assertEquals(0L, obj.getColumnLong());
        assertEquals(0f, obj.getColumnFloat());
        assertEquals(0d, obj.getColumnDouble());
        assertEquals(false, obj.isColumnBoolean());
        assertEquals(new Date(0), obj.getColumnDate());
        assertArrayEquals(new byte[0], obj.getColumnBinary());
        assertNull(obj.getColumnRealmObject());
        assertEquals(0, obj.getColumnRealmList().size());
    }

    // Test that given an exception everything up to the exception is saved
    public void testCreateObjectFromJson_jsonException() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("columnString", "Foo");
        json.put("columnDate", "Boom");

        try {
            testRealm.beginTransaction();
            testRealm.createObjectFromJson(AllTypes.class, json);
        } catch (RealmException e) {
            // Ignore
        } finally {
            testRealm.commitTransaction();
        }

        AllTypes obj = testRealm.allObjects(AllTypes.class).first();
        assertEquals("Foo", obj.getColumnString());
        assertEquals(new Date(0), obj.getColumnDate());
    }

    public void testCreateObjectFromJson_respectIgnoredFields() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("indexString", "Foo");
        json.put("notIndexString", "Bar");
        json.put("ignoreString", "Baz");

        testRealm.beginTransaction();
        testRealm.createObjectFromJson(AnnotationTypes.class, json);
        testRealm.commitTransaction();

        AnnotationTypes annotationsObject = testRealm.allObjects(AnnotationTypes.class).first();
        assertEquals("Foo", annotationsObject.getIndexString());
        assertEquals(null, annotationsObject.getIgnoreString());
    }

    public void testCreateAllFromJsonStringArray_simpleArray() {
        testRealm.beginTransaction();
        testRealm.createAllFromJson(Dog.class, "[{ name: \"Foo\" }, { name: \"Bar\" }]");
        testRealm. commitTransaction();

        assertEquals(2, testRealm.allObjects(Dog.class).size());
    }

    public void testCreateAllFromJsonStringArray_faultyJsonThrows() {
        testRealm.beginTransaction();
        try {
            testRealm.createAllFromJson(Dog.class, "[{ name : \"Foo\" ]");
        } catch (RealmException e) {
            return;
        } finally {
            testRealm.commitTransaction();
        }

        fail("Faulty JSON should result in a RealmException");
    }


    public void testCreateAllFromJsonStringArray_null() {
        testRealm.beginTransaction();
        testRealm.createAllFromJson(Dog.class, (String) null);
        testRealm.commitTransaction();

        assertEquals(0, testRealm.allObjects(Dog.class).size());
    }

   public void testCreateAllFromJsonStream_null() throws IOException {
        testRealm.createAllFromJson(AllTypes.class, (InputStream) null);
        assertEquals(0, testRealm.allObjects(AllTypes.class).size());
   }

    public void testCreateObjectFromJsonStream_allSimpleTypes() throws IOException {
        InputStream in = loadJsonFromAssets("all_simple_types.json");
        testRealm.beginTransaction();
        testRealm.createObjectFromJson(AllTypes.class, in);
        testRealm.commitTransaction();
        in.close();

        // Check that all primitive types are imported correctly
        AllTypes obj = testRealm.allObjects(AllTypes.class).first();
        assertEquals("String", obj.getColumnString());
        assertEquals(1l, obj.getColumnLong());
        assertEquals(1.23f, obj.getColumnFloat());
        assertEquals(1.23d, obj.getColumnDouble());
        assertEquals(true, obj.isColumnBoolean());
        assertArrayEquals(new byte[]{1, 2, 3}, obj.getColumnBinary());
    }

    public void testCreateObjectFromJsonStream_dateAsLong() throws IOException {
        InputStream in = loadJsonFromAssets("date_as_long.json");
        testRealm.beginTransaction();
        testRealm.createObjectFromJson(AllTypes.class, in);
        testRealm.commitTransaction();
        in.close();

        // Check that all primitive types are imported correctly
        AllTypes obj = testRealm.allObjects(AllTypes.class).first();
        assertEquals(new Date(1000), obj.getColumnDate());
    }

    public void testCreateObjectFromJsonStream_dateAsString() throws IOException {
        InputStream in = loadJsonFromAssets("date_as_string.json");
        testRealm.beginTransaction();
        testRealm.createObjectFromJson(AllTypes.class, in);
        testRealm.commitTransaction();
        in.close();

        // Check that all primitive types are imported correctly
        AllTypes obj = testRealm.allObjects(AllTypes.class).first();
        assertEquals(new Date(1000), obj.getColumnDate());
    }

    public void testCreateObjectFromJsonStream_childObject() throws IOException {
        InputStream in = loadJsonFromAssets("single_child_object.json");
        testRealm.beginTransaction();
        testRealm.createObjectFromJson(AllTypes.class, in);
        testRealm.commitTransaction();
        in.close();

        AllTypes obj = testRealm.allObjects(AllTypes.class).first();
        assertEquals("Fido", obj.getColumnRealmObject().getName());
    }

    public void testCreateObjectFromJsonStream_emptyChildObjectList() throws IOException {
        InputStream in = loadJsonFromAssets("realmlist_empty.json");
        testRealm.beginTransaction();
        testRealm.createObjectFromJson(AllTypes.class, in);
        testRealm.commitTransaction();
        in.close();

        AllTypes obj = testRealm.allObjects(AllTypes.class).first();
        assertEquals(0, obj.getColumnRealmList().size());
    }

    public void testCreateObjectFromJsonStream_childObjectList() throws IOException {
        InputStream in = loadJsonFromAssets("realmlist.json");
        testRealm.beginTransaction();
        testRealm.createObjectFromJson(AllTypes.class, in);
        testRealm.commitTransaction();
        in.close();

        assertEquals(3, testRealm.allObjects(Dog.class).size());
        assertEquals(1, testRealm.where(Dog.class).equalTo("name", "Fido-3").findAll().size());
    }

    public void testCreateObjectFromJsonStream_array() throws IOException {
        InputStream in = loadJsonFromAssets("array.json");

        testRealm.beginTransaction();
        testRealm.createAllFromJson(Dog.class, in);
        testRealm.commitTransaction();

        assertEquals(3, testRealm.allObjects(Dog.class).size());
        assertEquals(1, testRealm.where(Dog.class).equalTo("name", "Fido-3").findAll().size());
    }


    // Test if Json object doesn't have the field, then the field should have default value. Stream version.
    public void testCreateObjectFromJsonStream_noValues() throws IOException {
        InputStream in = loadJsonFromAssets("all_types_no_value.json");
        testRealm.beginTransaction();
        testRealm.createObjectFromJson(AllTypes.class, in);
        testRealm.commitTransaction();
        in.close();

        // Check that all primitive types are imported correctly
        AllTypes obj = testRealm.allObjects(AllTypes.class).first();
        assertEquals("", obj.getColumnString());
        assertEquals(0L, obj.getColumnLong());
        assertEquals(0f, obj.getColumnFloat());
        assertEquals(0d, obj.getColumnDouble());
        assertEquals(false, obj.isColumnBoolean());
        assertEquals(new Date(0), obj.getColumnDate());
        assertArrayEquals(new byte[0], obj.getColumnBinary());
        assertNull(obj.getColumnRealmObject());
        assertEquals(0, obj.getColumnRealmList().size());
    }

    // Test update a existing object with JSON stream.
    public void testUpdateObjectFromJsonStream_nullValues() throws IOException {
        AllTypesPrimaryKey obj = new AllTypesPrimaryKey();
        Date date = new Date(0);
        // ID
        obj.setColumnLong(1);
        obj.setColumnBinary(new byte[]{1});
        obj.setColumnBoolean(true);
        obj.setColumnDate(date);
        obj.setColumnDouble(1);
        obj.setColumnFloat(1);
        obj.setColumnString("1");
        testRealm.beginTransaction();
        testRealm.copyToRealm(obj);
        testRealm.commitTransaction();

        InputStream in = loadJsonFromAssets("all_types_primary_key_null.json");
        testRealm.beginTransaction();
        testRealm.createOrUpdateObjectFromJson(AllTypesPrimaryKey.class, in);
        testRealm.commitTransaction();
        in.close();

        // Check that all primitive types are imported correctly
        obj = testRealm.allObjects(AllTypesPrimaryKey.class).first();
        // TODO: We only handle updating null for String right now.
        //       Clean this up after nullable support for all types.
        assertEquals("", obj.getColumnString());
        assertEquals(1L, obj.getColumnLong());
        assertEquals(1f, obj.getColumnFloat());
        assertEquals(1d, obj.getColumnDouble());
        assertEquals(true, obj.isColumnBoolean());
        assertEquals(date, obj.getColumnDate());
        assertArrayEquals(new byte[]{1}, obj.getColumnBinary());
        assertNull(obj.getColumnRealmObject());
        assertEquals(0, obj.getColumnRealmList().size());
    }

    // Test update a existing object with JSON object.
    public void testUpdateObjectFromJsonObject_nullValues() throws IOException {
        AllTypesPrimaryKey obj = new AllTypesPrimaryKey();
        Date date = new Date(0);
        // ID
        obj.setColumnLong(1);
        obj.setColumnBinary(new byte[]{1});
        obj.setColumnBoolean(true);
        obj.setColumnDate(date);
        obj.setColumnDouble(1);
        obj.setColumnFloat(1);
        obj.setColumnString("1");
        testRealm.beginTransaction();
        testRealm.copyToRealm(obj);
        testRealm.commitTransaction();

        String json = TestHelper.streamToString(loadJsonFromAssets("all_types_primary_key_null.json"));
        testRealm.beginTransaction();
        testRealm.createOrUpdateObjectFromJson(AllTypesPrimaryKey.class, json);
        testRealm.commitTransaction();

        // Check that all primitive types are imported correctly
        obj = testRealm.allObjects(AllTypesPrimaryKey.class).first();
        // TODO: We only handle updating String with null value right now.
        //       Clean this up after nullable support for all types.
        assertEquals("", obj.getColumnString());
        assertEquals(1L, obj.getColumnLong());
        assertEquals(1f, obj.getColumnFloat());
        assertEquals(1d, obj.getColumnDouble());
        assertEquals(true, obj.isColumnBoolean());
        assertEquals(date, obj.getColumnDate());
        assertArrayEquals(new byte[]{1}, obj.getColumnBinary());
        assertNull(obj.getColumnRealmObject());
        assertEquals(0, obj.getColumnRealmList().size());
    }

    public void testCreateOrUpdateObject_noPrimaryKeyThrows() {
        try {
            testRealm.createOrUpdateObjectFromJson(AllTypes.class, new JSONObject());
        } catch (IllegalArgumentException expected) {
            return;
        }
        fail();
    }

    public void testCreateOrUpdateObjectStream_noPrimaryKeyThrows() throws IOException {
        try {
            testRealm.createOrUpdateObjectFromJson(AllTypes.class, new TestHelper.StubInputStream());
        } catch (IllegalArgumentException expected) {
            return;
        }
        fail();
    }

    public void testCreateOrUpdateObjectStream_invalidJSonThrows() throws IOException {
        try {
            testRealm.createOrUpdateAllFromJson(AllTypesPrimaryKey.class, TestHelper.stringToStream("{"));
        } catch (RealmException expected) {
            return;
        }
        fail();
    }

    public void testCreateOrUpdateObjectString_noPrimaryKeyThrows() throws IOException {
        try {
            testRealm.createOrUpdateObjectFromJson(AllTypes.class, "{}");
        } catch (IllegalArgumentException expected) {
            return;
        }
        fail();
    }

    public void testCreateOrUpdateJsonObject() throws JSONException {
        testRealm.beginTransaction();
        AllTypesPrimaryKey obj = new AllTypesPrimaryKey();
        obj.setColumnLong(1);
        obj.setColumnString("Foo");
        testRealm.copyToRealm(obj);

        JSONObject json = new JSONObject();
        json.put("columnLong", 1);
        json.put("columnString", "bar");

        AllTypesPrimaryKey newObj = testRealm.createOrUpdateObjectFromJson(AllTypesPrimaryKey.class, json);
        testRealm.commitTransaction();

        assertEquals(1, testRealm.allObjects(AllTypesPrimaryKey.class).size());
        assertEquals("bar", newObj.getColumnString());
    }

    public void testCreateOrUpdateJsonObject_ignoreUnsetProperties() throws IOException {
        String json = TestHelper.streamToString(loadJsonFromAssets("list_alltypes_primarykey.json"));
        testRealm.beginTransaction();
        testRealm.createOrUpdateAllFromJson(AllTypesPrimaryKey.class, json);
        testRealm.commitTransaction();

        // No-op as no properties should be updated
        testRealm.beginTransaction();
        testRealm.createOrUpdateObjectFromJson(AllTypesPrimaryKey.class, "{ \"columnLong\":1 }");
        testRealm.commitTransaction();

        assertAllTypesPrimaryKeyUpdated();
    }

    public void testCreateOrUpdateJsonStream_ignoreUnsetProperties() throws IOException {
        testRealm.beginTransaction();
        testRealm.createOrUpdateAllFromJson(AllTypesPrimaryKey.class, loadJsonFromAssets("list_alltypes_primarykey.json"));
        testRealm.commitTransaction();

        // No-op as no properties should be updated
        testRealm.beginTransaction();
        testRealm.createOrUpdateObjectFromJson(AllTypesPrimaryKey.class, TestHelper.stringToStream("{ \"columnLong\":1 }"));
        testRealm.commitTransaction();

        assertAllTypesPrimaryKeyUpdated();
    }

    public void testCreateOrUpdateInputStream() throws IOException {
        testRealm.beginTransaction();
        AllTypesPrimaryKey obj = new AllTypesPrimaryKey();
        obj.setColumnLong(1);
        obj.setColumnString("Foo");
        testRealm.copyToRealm(obj);

        InputStream in = TestHelper.stringToStream("{ \"columnLong\" : 1, \"columnString\" : \"bar\" }");
        AllTypesPrimaryKey newObj = testRealm.createOrUpdateObjectFromJson(AllTypesPrimaryKey.class, in);
        testRealm.commitTransaction();

        assertEquals(1, testRealm.allObjects(AllTypesPrimaryKey.class).size());
        assertEquals("bar", newObj.getColumnString());
    }

    public void testCreateOrUpdateString() throws IOException {
        testRealm.beginTransaction();
        AllTypesPrimaryKey obj = new AllTypesPrimaryKey();
        obj.setColumnLong(1);
        obj.setColumnString("Foo");
        testRealm.copyToRealm(obj);

        AllTypesPrimaryKey newObj = testRealm.createOrUpdateObjectFromJson(AllTypesPrimaryKey.class, "{ \"columnLong\" : 1, \"columnString\" : \"bar\" }");
        testRealm.commitTransaction();

        assertEquals(1, testRealm.allObjects(AllTypesPrimaryKey.class).size());
        assertEquals("bar", newObj.getColumnString());
    }


    public void testCreateOrUpdateAll_noPrimaryKeyThrows() {
        try {
            testRealm.createOrUpdateAllFromJson(AllTypes.class, new JSONArray());
        } catch (IllegalArgumentException expected) {
            return;
        }
        fail();
    }

    public void testCreateOrUpdateAllStream_noPrimaryKeyThrows() throws IOException {
        try {
            testRealm.createOrUpdateAllFromJson(AllTypes.class, new TestHelper.StubInputStream());
        } catch (IllegalArgumentException expected) {
            return;
        }
        fail();
    }

    public void testCreateOrUpdateAllStream_invalidJSonThrows() throws IOException {
        try {
            testRealm.createOrUpdateAllFromJson(AllTypesPrimaryKey.class, TestHelper.stringToStream("["));
        } catch (RealmException expected) {
            return;
        }
        fail();
    }

    public void testCreateOrUpdateAllString_noPrimaryKeyThrows() throws IOException {
        try {
            testRealm.createOrUpdateAllFromJson(AllTypes.class, "{}");
        } catch (IllegalArgumentException expected) {
            return;
        }
        fail();
    }

    public void testCreateOrUpdateAllJsonArray() throws JSONException, IOException {
        String json = TestHelper.streamToString(loadJsonFromAssets("list_alltypes_primarykey.json"));
        JSONArray array = new JSONArray(json);
        testRealm.beginTransaction();
        testRealm.createOrUpdateAllFromJson(AllTypesPrimaryKey.class, array);
        testRealm.commitTransaction();

        assertAllTypesPrimaryKeyUpdated();
    }

    public void testCreateOrUpdateAllInputStream() throws IOException {
        testRealm.beginTransaction();
        testRealm.createOrUpdateAllFromJson(AllTypesPrimaryKey.class, loadJsonFromAssets("list_alltypes_primarykey.json"));
        testRealm.commitTransaction();

        assertAllTypesPrimaryKeyUpdated();
    }

    public void testCreateOrUpdateAllString() throws IOException {
        String json = TestHelper.streamToString(loadJsonFromAssets("list_alltypes_primarykey.json"));
        testRealm.beginTransaction();
        testRealm.createOrUpdateAllFromJson(AllTypesPrimaryKey.class, json);
        testRealm.commitTransaction();

        assertAllTypesPrimaryKeyUpdated();
    }

    // Assert that the list of AllTypesPrimaryKey objects where inserted and updated properly.
    private void assertAllTypesPrimaryKeyUpdated() {
        assertEquals(1, testRealm.allObjects(AllTypesPrimaryKey.class).size());
        AllTypesPrimaryKey obj = testRealm.allObjects(AllTypesPrimaryKey.class).first();
        assertEquals("Bar", obj.getColumnString());
        assertEquals(2.23F, obj.getColumnFloat());
        assertEquals(2.234D, obj.getColumnDouble());
        assertEquals(true, obj.isColumnBoolean());
        assertArrayEquals(new byte[]{1, 2, 3}, obj.getColumnBinary());
        assertEquals(new Date(2000), obj.getColumnDate());
        assertEquals("Dog4", obj.getColumnRealmObject().getName());
        assertEquals(2, obj.getColumnRealmList().size());
        assertEquals("Dog5", obj.getColumnRealmList().get(0).getName());
    }

    // Check the imported object from nulltyps.json[0].
    private void checkNullableValuesNull(NullTypes nullTypes1) {
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
        assertEquals(0F, nullTypes1.getFieldFloatNotNull());
        // 9 Double
        assertNull(nullTypes1.getFieldDoubleNull());
        assertEquals(0D, nullTypes1.getFieldDoubleNotNull());
        // 10 Date
        assertNull(nullTypes1.getFieldDateNull());
        assertEquals(new Date(0), nullTypes1.getFieldDateNotNull());
        // 11 RealmObject
        assertNull(nullTypes1.getFieldObjectNull());
    }

    // Check the imported object from nulltyps.json[1].
    private void checkNullableValuesNonNull(NullTypes nullTypes2) {
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
        assertEquals(0F, nullTypes2.getFieldFloatNull());
        assertEquals(0F, nullTypes2.getFieldFloatNotNull());
        // 9 Double
        assertEquals(0D, nullTypes2.getFieldDoubleNull());
        assertEquals(0D, nullTypes2.getFieldDoubleNotNull());
        // 10 Date
        assertEquals(new Date(0), nullTypes2.getFieldDateNull());
        assertEquals(new Date(0), nullTypes2.getFieldDateNotNull());
        // 11 RealmObject
        assertNotNull(nullTypes2.getFieldObjectNull());
    }

    // Testing create objects from Json, all nullable fields with null values or non-null values
    public void testNullTypesJsonWithNulls() throws IOException, JSONException {
        String json = TestHelper.streamToString(loadJsonFromAssets("nulltypes.json"));
        JSONArray array = new JSONArray(json);
        testRealm.beginTransaction();
        testRealm.createAllFromJson(NullTypes.class, array);
        testRealm.commitTransaction();

        RealmResults<NullTypes> nullTypesRealmResults = testRealm.allObjects(NullTypes.class);
        assertEquals(3, nullTypesRealmResults.size());

        NullTypes nullTypes1 = nullTypesRealmResults.where().equalTo("id", 1).findFirst();
        checkNullableValuesNull(nullTypes1);

        NullTypes nullTypes2 = nullTypesRealmResults.where().equalTo("id", 2).findFirst();
        checkNullableValuesNonNull(nullTypes2);
    }

    // Test creating objects form JSON stream, all nullable fields with non-null values or non-null values
    public void testNullTypesStreamJSONWithNulls() throws IOException {
        testRealm.beginTransaction();
        testRealm.createAllFromJson(NullTypes.class, loadJsonFromAssets("nulltypes.json"));
        testRealm.commitTransaction();

        RealmResults<NullTypes> nullTypesRealmResults = testRealm.allObjects(NullTypes.class);
        assertEquals(3, nullTypesRealmResults.size());

        NullTypes nullTypes1 = nullTypesRealmResults.where().equalTo("id", 1).findFirst();
        checkNullableValuesNull(nullTypes1);

        NullTypes nullTypes2 = nullTypesRealmResults.where().equalTo("id", 2).findFirst();
        checkNullableValuesNonNull(nullTypes2);
    }

    // Test a nullable field already has a non-null value, update it through JSON with null value
    // of the corresponding field.
    public void testUpdateNullTypesJSONWithNulls() throws IOException, JSONException {
        String json = TestHelper.streamToString(loadJsonFromAssets("nulltypes.json"));
        JSONArray jsonArray = new JSONArray(json);
        // Nullable fields with values
        JSONObject jsonObject = jsonArray.getJSONObject(1);
        jsonObject.put("id", 1);

        testRealm.beginTransaction();
        // Now object with id 1 has values for all nullable fields.
        testRealm.createObjectFromJson(NullTypes.class, jsonObject);
        testRealm.commitTransaction();
        RealmResults<NullTypes> nullTypesRealmResults = testRealm.allObjects(NullTypes.class);
        assertEquals(2, nullTypesRealmResults.size());
        checkNullableValuesNonNull(nullTypesRealmResults.first());

        // Update object with id 1, nullable fields should have null values
        JSONArray array = new JSONArray(json);
        testRealm.beginTransaction();
        testRealm.createOrUpdateAllFromJson(NullTypes.class, array);
        testRealm.commitTransaction();

        nullTypesRealmResults = testRealm.allObjects(NullTypes.class);
        assertEquals(3, nullTypesRealmResults.size());

        NullTypes nullTypes1 = nullTypesRealmResults.where().equalTo("id", 1).findFirst();
        checkNullableValuesNull(nullTypes1);
    }

    // If JSON has a field with value null, and corresponding object's field is not nullable,
    // an exception should be throw.
    public void testNullTypesJSONToNotNullFields() throws IOException, JSONException {
        String json = TestHelper.streamToString(loadJsonFromAssets("nulltypes_invalid.json"));
        JSONArray array = new JSONArray(json);
        testRealm.beginTransaction();

        // 1 String
        try {
            testRealm.createObjectFromJson(NullTypes.class, array.getJSONObject(0));
            fail();
        } catch (RealmException e) {
            assertTrue(e.getCause() instanceof IllegalArgumentException);
        }
        // 2 Bytes
        try {
            testRealm.createObjectFromJson(NullTypes.class, array.getJSONObject(1));
            fail();
        } catch (RealmException e) {
            assertTrue(e.getCause() instanceof IllegalArgumentException);
        }
        // 3 Boolean
        try {
            testRealm.createObjectFromJson(NullTypes.class, array.getJSONObject(2));
            fail();
        } catch (RealmException e) {
            assertTrue(e.getCause() instanceof IllegalArgumentException);
        }
        // 4 Byte
        try {
            testRealm.createObjectFromJson(NullTypes.class, array.getJSONObject(3));
            fail();
        } catch (RealmException e) {
            assertTrue(e.getCause() instanceof IllegalArgumentException);
        }
        // 5 Short
        try {
            testRealm.createObjectFromJson(NullTypes.class, array.getJSONObject(4));
            fail();
        } catch (RealmException e) {
            assertTrue(e.getCause() instanceof IllegalArgumentException);
        }
        // 6 Integer
        try {
            testRealm.createObjectFromJson(NullTypes.class, array.getJSONObject(5));
            fail();
        } catch (RealmException e) {
            assertTrue(e.getCause() instanceof IllegalArgumentException);
        }
        // 7 Long
        try {
            testRealm.createObjectFromJson(NullTypes.class, array.getJSONObject(6));
            fail();
        } catch (RealmException e) {
            assertTrue(e.getCause() instanceof IllegalArgumentException);
        }
        // 8 Float
        try {
            testRealm.createObjectFromJson(NullTypes.class, array.getJSONObject(7));
            fail();
        } catch (RealmException e) {
            assertTrue(e.getCause() instanceof IllegalArgumentException);
        }
        // 9 Double
        try {
            testRealm.createObjectFromJson(NullTypes.class, array.getJSONObject(8));
            fail();
        } catch (RealmException e) {
            assertTrue(e.getCause() instanceof IllegalArgumentException);
        }
        // 10 Date
        try {
            testRealm.createObjectFromJson(NullTypes.class, array.getJSONObject(9));
            fail();
        } catch (RealmException e) {
            assertTrue(e.getCause() instanceof IllegalArgumentException);
        }
        testRealm.cancelTransaction();
    }

    // If JSON has a field with value null, and corresponding object's field is not nullable,
    // an exception should be throw.
    public void testNullTypesJSONStreamToNotNullFields() throws IOException, JSONException {
        String json = TestHelper.streamToString(loadJsonFromAssets("nulltypes_invalid.json"));
        JSONArray array = new JSONArray(json);

        // 1 String
        try {
            testRealm.beginTransaction();
            testRealm.createObjectFromJson(NullTypes.class, convertJsonObjectToStream(array.getJSONObject(0)));
            fail();
        } catch (IllegalArgumentException ignored) {
        } finally {
            testRealm.cancelTransaction();
        }
        // 2 Bytes
        try {
            testRealm.beginTransaction();
            testRealm.createObjectFromJson(NullTypes.class, convertJsonObjectToStream(array.getJSONObject(1)));
            fail();
        } catch (IllegalArgumentException ignored) {
        } finally {
            testRealm.cancelTransaction();
        }
        // 3 Boolean
        try {
            testRealm.beginTransaction();
            testRealm.createObjectFromJson(NullTypes.class, convertJsonObjectToStream(array.getJSONObject(2)));
            fail();
        } catch (IllegalArgumentException ignored) {
        } finally {
            testRealm.cancelTransaction();
        }
        // 4 Byte
        try {
            testRealm.beginTransaction();
            testRealm.createObjectFromJson(NullTypes.class, convertJsonObjectToStream(array.getJSONObject(3)));
            fail();
        } catch (IllegalArgumentException ignored) {
        } finally {
            testRealm.cancelTransaction();
        }
        // 5 Short
        try {
            testRealm.beginTransaction();
            testRealm.createObjectFromJson(NullTypes.class, convertJsonObjectToStream(array.getJSONObject(4)));
            fail();
        } catch (IllegalArgumentException ignored) {
        } finally {
            testRealm.cancelTransaction();
        }
        // 6 Integer
        try {
            testRealm.beginTransaction();
            testRealm.createObjectFromJson(NullTypes.class, convertJsonObjectToStream(array.getJSONObject(5)));
            fail();
        } catch (IllegalArgumentException ignored) {
        } finally {
            testRealm.cancelTransaction();
        }
        // 7 Long
        try {
            testRealm.beginTransaction();
            testRealm.createObjectFromJson(NullTypes.class, convertJsonObjectToStream(array.getJSONObject(6)));
            fail();
        } catch (IllegalArgumentException ignored) {
        } finally {
            testRealm.cancelTransaction();
        }
        // 8 Float
        try {
            testRealm.beginTransaction();
            testRealm.createObjectFromJson(NullTypes.class, convertJsonObjectToStream(array.getJSONObject(7)));
            fail();
        } catch (IllegalArgumentException ignored) {
        } finally {
            testRealm.cancelTransaction();
        }
        // 9 Double
        try {
            testRealm.beginTransaction();
            testRealm.createObjectFromJson(NullTypes.class, convertJsonObjectToStream(array.getJSONObject(8)));
            fail();
        } catch (IllegalArgumentException ignored) {
        } finally {
            testRealm.cancelTransaction();
        }
        // 10 Date
        try {
            testRealm.beginTransaction();
            testRealm.createObjectFromJson(NullTypes.class, convertJsonObjectToStream(array.getJSONObject(9)));
            fail();
        } catch (IllegalArgumentException ignored) {
        } finally {
            testRealm.cancelTransaction();
        }
    }
}
