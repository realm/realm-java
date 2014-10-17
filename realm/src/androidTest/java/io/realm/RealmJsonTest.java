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
import io.realm.entities.Dog;

public class RealmJsonTest extends AndroidTestCase {

    protected Realm testRealm;

    @Override
    protected void setUp() throws Exception {
        Realm.deleteRealmFile(getContext());
        testRealm = Realm.getInstance(getContext());
        testRealm.beginTransaction();
        testRealm.clear(AllTypes.class);
        testRealm.commitTransaction();
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
        testRealm.createFromJson(AllTypes.class, (JSONObject) null);
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
        json.put("columnBinary", Base64.encode(new byte[] {1, 2, 3}, Base64.DEFAULT));

        testRealm.beginTransaction();
        testRealm.createFromJson(AllTypes.class, json);
        testRealm.commitTransaction();
        AllTypes obj = testRealm.allObjects(AllTypes.class).first();

        // Check that all primitive types are imported correctly
        assertEquals("String", obj.getColumnString());
        assertEquals(1l, obj.getColumnLong());
        assertEquals(1.23f, obj.getColumnFloat());
        assertEquals(1.23d, obj.getColumnDouble());
        assertEquals(true, obj.isColumnBoolean());
        assertEquals(new byte[]{1, 2, 3}, obj.getColumnBinary());
    }

    public void testImportJSon_dateAsLong() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("columnDate", 1000L); // Realm operates at seconds level granularity

        testRealm.beginTransaction();
        testRealm.createFromJson(AllTypes.class, json);
        testRealm.commitTransaction();

        AllTypes obj = testRealm.allObjects(AllTypes.class).first();
        assertEquals(new Date(1000), obj.getColumnDate());
    }

    public void testImportJSon_dateAsString() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("columnDate", "/Date(1000)/");

        testRealm.beginTransaction();
        testRealm.createFromJson(AllTypes.class, json);
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
        testRealm.createFromJson(AllTypes.class, allTypesObject);
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

        allTypesObject.put("columnRealmObjectList", dogList);

        testRealm.beginTransaction();
        testRealm.createFromJson(AllTypes.class, allTypesObject);
        testRealm.commitTransaction();

        AllTypes obj = testRealm.allObjects(AllTypes.class).first();
        assertEquals(3, obj.getColumnRealmObjectList().size());
        assertEquals("Fido-3", obj.getColumnRealmObjectList().get(2).getName());
    }

    public void testImportJSon_emptyChildObjectList() throws JSONException {
        JSONObject allTypesObject = new JSONObject();
        JSONArray dogList = new JSONArray();

        allTypesObject.put("columnRealmObjectList", dogList);

        testRealm.beginTransaction();
        testRealm.createFromJson(AllTypes.class, allTypesObject);
        testRealm.commitTransaction();

        AllTypes obj = testRealm.allObjects(AllTypes.class).first();
        assertEquals(0, obj.getColumnRealmObjectList().size());
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
        assertEquals("Fido-3", testRealm.allObjects(Dog.class).get(2).getName());
    }


    public void testRemoveObjetsIfImportFail() {
        fail("Test if objects created by the json import is removed if something fail during decoding");
    }

    public void testImportStream_null() {
        testRealm.createAllFromJson(AllTypes.class, (InputStream) null);
        assertEquals(0, testRealm.allObjects(AllTypes.class).size());
    }

    public void testImportStream_allSimpleTypes() throws IOException {
        InputStream in = loadJsonFromAssets("all_simple_types.json");
        testRealm.beginTransaction();
        testRealm.createAllFromJson(AllTypes.class, in);
        testRealm.commitTransaction();
        in.close();

        // Check that all primitive types are imported correctly
        AllTypes obj = testRealm.allObjects(AllTypes.class).first();
        assertEquals("String", obj.getColumnString());
        assertEquals(1l, obj.getColumnLong());
        assertEquals(1.23f, obj.getColumnFloat());
        assertEquals(1.23d, obj.getColumnDouble());
        assertEquals(true, obj.isColumnBoolean());
        assertEquals(new byte[]{1, 2, 3}, obj.getColumnBinary());
    }

    public void testImportStream_DateAsLong() {
        fail("Not implemented.");
    }

    public void testImportStream_DateAsString() {
        fail("Not implemented.");
    }

    public void testImportStream_childObject() {
        fail("Not implemented.");
    }

    public void testImportStream_emptyChildObjectList() {
        fail("Not implemented.");
    }

    public void testImportStream_childObjectList() {
        fail("Not implemented.");
    }

}
