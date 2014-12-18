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

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import io.realm.entities.AllTypes;
import io.realm.entities.AnnotationTypes;
import io.realm.entities.Dog;
import io.realm.exceptions.RealmException;

import static io.realm.internal.test.ExtraTests.assertArrayEquals;

public class RealmJsonTest extends AndroidTestCase {

    protected Realm testRealm;

    @Override
    protected void setUp() throws Exception {
        Realm.deleteRealmFile(getContext());
        testRealm = Realm.getInstance(getContext());
        testRealm.beginTransaction();
        testRealm.clear(AllTypes.class);
        testRealm.clear(Dog.class);
        testRealm.clear(AnnotationTypes.class);
        testRealm.commitTransaction();
    }

    @Override
    protected void tearDown() throws Exception {
        testRealm.close();
    }

    private InputStream loadJsonFromAssets(String file) {
        AssetManager assetManager = getContext().getAssets();
        InputStream input = null;
        try {
            input = assetManager.open(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            return input;
        }
    }

    public void testImportJSon_nullObject() {
        testRealm.createObjectFromJson(AllTypes.class, (JSONObject) null);
        assertEquals(0, testRealm.allObjects(AllTypes.class).size());
    }

    public void testImportJSon_nullArray() {
        testRealm.createAllFromJson(AllTypes.class, (JSONArray) null);
        assertEquals(0, testRealm.allObjects(AllTypes.class).size());

    }

    public void testImportJSon_allSimpSimpleObjectAllTypes() throws JSONException {
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

    public void testImportJSon_dateAsLong() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("columnDate", 1000L); // Realm operates at seconds level granularity

        testRealm.beginTransaction();
        testRealm.createObjectFromJson(AllTypes.class, json);
        testRealm.commitTransaction();

        AllTypes obj = testRealm.allObjects(AllTypes.class).first();
        assertEquals(new Date(1000), obj.getColumnDate());
    }

    public void testImportJSon_dateAsString() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("columnDate", "/Date(1000)/");

        testRealm.beginTransaction();
        testRealm.createObjectFromJson(AllTypes.class, json);
        testRealm.commitTransaction();

        AllTypes obj = testRealm.allObjects(AllTypes.class).first();
        assertEquals(new Date(1000), obj.getColumnDate());
    }

    public void testImportJSon_childObject() throws JSONException {
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

    public void testImportJSon_childObjectList() throws JSONException {
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

    public void testImportJSon_emptyChildObjectList() throws JSONException {
        JSONObject allTypesObject = new JSONObject();
        JSONArray dogList = new JSONArray();

        allTypesObject.put("columnRealmList", dogList);

        testRealm.beginTransaction();
        testRealm.createObjectFromJson(AllTypes.class, allTypesObject);
        testRealm.commitTransaction();

        AllTypes obj = testRealm.allObjects(AllTypes.class).first();
        assertEquals(0, obj.getColumnRealmList().size());
    }

    public void testImportJsonString_simpleObject() {
        testRealm.beginTransaction();
        Dog dog = testRealm.createObjectFromJson(Dog.class, "{ name: \"Foo\" }");
        testRealm. commitTransaction();

        assertEquals("Foo", dog.getName());
        assertEquals("Foo", testRealm.allObjects(Dog.class).first().getName());
    }


    public void testImportJsonString_faultyJson() {
        testRealm.beginTransaction();
        try {
            Dog dog = testRealm.createObjectFromJson(Dog.class, "{ name \"Foo\" }");
        } catch (RealmException e) {
            return;
        } finally {
            testRealm.commitTransaction();
        }

        fail("Doesn't handle faulty JSON properly");
    }


    public void testImportJsonString_null() {
        testRealm.beginTransaction();
        Dog dog = testRealm.createObjectFromJson(Dog.class, (String) null);
        testRealm.commitTransaction();

        assertNull(dog);
        assertEquals(0, testRealm.allObjects(Dog.class).size());
    }

    public void testImportJsonArray_empty() throws JSONException {
        JSONArray array = new JSONArray();
        testRealm.beginTransaction();
        testRealm.createAllFromJson(AllTypes.class, array);
        testRealm.commitTransaction();

        assertEquals(0, testRealm.allObjects(AllTypes.class).size());
    }

    public void testImportJsonArray() throws JSONException {
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

    public void testImportJson_nullValues() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("columnString", null);
        json.put("columnLong", null);
        json.put("columnFloat", null);
        json.put("columnDouble", null);
        json.put("columnBoolean", null);
        json.put("columnBinary", null);
        json.put("columnDate", null);
        json.put("columnRealmObject", null);
        json.put("columnRealmList", null);

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
    public void testImportJson_jsonexception() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("columnString", "Foo");
        json.put("columnDate", "Boom");

        try {
            testRealm.beginTransaction();
            testRealm.createObjectFromJson(AllTypes.class, json);
        } catch (Exception e) {
            // Ignore
        } finally {
            testRealm.commitTransaction();
        }

        AllTypes obj = testRealm.allObjects(AllTypes.class).first();
        assertEquals("Foo", obj.getColumnString());
        assertEquals(new Date(0), obj.getColumnDate());
    }

    public void testImportJson_respectIgnoredFields() throws JSONException {
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

    public void testImportJsonArrayString_simpleArray() {
        testRealm.beginTransaction();
        testRealm.createAllFromJson(Dog.class, "[{ name: \"Foo\" }, { name: \"Bar\" }]");
        testRealm. commitTransaction();

        assertEquals(2, testRealm.allObjects(Dog.class).size());
    }

    public void testImportJsonArrayString_faultyJson() {
        testRealm.beginTransaction();
        try {
            testRealm.createAllFromJson(Dog.class, "[{ name : \"Foo\" ]");
        } catch (RealmException e) {
            return;
        } finally {
            testRealm.commitTransaction();
        }

        fail("Doesn't handle faulty JSON properly");
    }


    public void testImportJsonArrayString_null() {
        testRealm.beginTransaction();
        testRealm.createAllFromJson(Dog.class, (String) null);
        testRealm.commitTransaction();

        assertEquals(0, testRealm.allObjects(Dog.class).size());
    }

   public void testImportStream_null() throws IOException {
        testRealm.createAllFromJson(AllTypes.class, (InputStream) null);
        assertEquals(0, testRealm.allObjects(AllTypes.class).size());
    }

    public void testImportStream_allSimpleTypes() throws IOException {
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

    public void testImportStream_DateAsLong() throws IOException {
        InputStream in = loadJsonFromAssets("date_as_long.json");
        testRealm.beginTransaction();
        testRealm.createObjectFromJson(AllTypes.class, in);
        testRealm.commitTransaction();
        in.close();

        // Check that all primitive types are imported correctly
        AllTypes obj = testRealm.allObjects(AllTypes.class).first();
        assertEquals(new Date(1000), obj.getColumnDate());
    }

    public void testImportStream_DateAsString() throws IOException {
        InputStream in = loadJsonFromAssets("date_as_string.json");
        testRealm.beginTransaction();
        testRealm.createObjectFromJson(AllTypes.class, in);
        testRealm.commitTransaction();
        in.close();

        // Check that all primitive types are imported correctly
        AllTypes obj = testRealm.allObjects(AllTypes.class).first();
        assertEquals(new Date(1000), obj.getColumnDate());
    }

    public void testImportStream_childObject() throws IOException {
        InputStream in = loadJsonFromAssets("single_child_object.json");
        testRealm.beginTransaction();
        testRealm.createObjectFromJson(AllTypes.class, in);
        testRealm.commitTransaction();
        in.close();

        AllTypes obj = testRealm.allObjects(AllTypes.class).first();
        assertEquals("Fido", obj.getColumnRealmObject().getName());
    }

    public void testImportStream_emptyChildObjectList() throws IOException {
        InputStream in = loadJsonFromAssets("realmlist_empty.json");
        testRealm.beginTransaction();
        testRealm.createObjectFromJson(AllTypes.class, in);
        testRealm.commitTransaction();
        in.close();

        AllTypes obj = testRealm.allObjects(AllTypes.class).first();
        assertEquals(0, obj.getColumnRealmList().size());
    }

    public void testImportStream_childObjectList() throws IOException {
        InputStream in = loadJsonFromAssets("realmlist.json");
        testRealm.beginTransaction();
        testRealm.createObjectFromJson(AllTypes.class, in);
        testRealm.commitTransaction();
        in.close();

        assertEquals(3, testRealm.allObjects(Dog.class).size());
        assertEquals(1, testRealm.where(Dog.class).equalTo("name", "Fido-3").findAll().size());
    }

    public void testImportStream_array() throws IOException {
        InputStream in = loadJsonFromAssets("array.json");

        testRealm.beginTransaction();
        testRealm.createAllFromJson(Dog.class, in);
        testRealm.commitTransaction();

        assertEquals(3, testRealm.allObjects(Dog.class).size());
        assertEquals(1, testRealm.where(Dog.class).equalTo("name", "Fido-3").findAll().size());
    }


    public void testImportStream_nullValues() throws IOException {
        InputStream in = loadJsonFromAssets("all_types_null.json");
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
}
