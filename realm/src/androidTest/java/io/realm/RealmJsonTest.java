package io.realm;

import android.test.AndroidTestCase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.realm.entities.AllTypes;

public class RealmJsonTest extends AndroidTestCase {

    protected Realm testRealm;

    @Override
    protected void setUp() throws Exception {
        Realm.deleteRealmFile(getContext());
        testRealm = Realm.getInstance(getContext());
    }

    public void testImportJSonNullObject() {
        testRealm.createFromJson(AllTypes.class, (JSONObject) null);
        assertEquals(0, testRealm.allObjects(AllTypes.class).size());
    }

    public void testImportJSonNullArray() {
        testRealm.addFromJson(AllTypes.class, (JSONArray) null);
        assertEquals(0, testRealm.allObjects(AllTypes.class).size());

    }

    public void testImportJSonAllSimpSimpleObjectAllTypes() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("columnString", "String");
        json.put("columnLong", 1l);
        json.put("columnFloat", 1.23f);
        json.put("columnDouble", 1.23d);
        json.put("columnBoolean", true);
//        json.put("columnDate", new Date(100).toString()); // ISO 8601 encoded
//        json.put("columnBinary", Base64.encode(new byte[] {1, 2, 3}, Base64.DEFAULT)); // Base 64 encoded

        testRealm.beginTransaction();
        testRealm.createFromJson(AllTypes.class, json);
        testRealm.commitTransaction();
        AllTypes obj = testRealm.allObjects(AllTypes.class).first();

        // Check that all primative types are imported correctly
        assertEquals("String", obj.getColumnString());
        assertEquals(1l, obj.getColumnLong());
        assertEquals(1.23f, obj.getColumnFloat());
        assertEquals(1.23d, obj.getColumnDouble());
        assertEquals(true, obj.isColumnBoolean());
//        assertEquals(new Date(100), obj.getColumnDate());
//        assertEquals(new byte[] {1, 2, 3}, obj.getColumnBinary());
    }

    public void testImportJSonNestedObjects() {
        fail("Not implemented.");
    }


    public void testRemoveObjetsIfImportFail() {
        fail("Test if objects created by the json import is removed if something fail during decoding");
    }

    public void testImportJsonNullStream() {
        testRealm.addFromJson(AllTypes.class, (JSONArray) null);
        assertEquals(0, testRealm.allObjects(AllTypes.class).size());
    }

    public void testImportJsonStreamAllTypes() {
        fail("Not implemented.");
    }

    public void testImportJsonStreamNestedTypes() {
        fail("Not implemented.");
    }
}
