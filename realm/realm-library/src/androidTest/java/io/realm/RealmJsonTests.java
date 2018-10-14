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
import android.os.Build;
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
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.annotation.Nullable;

import io.realm.entities.AllTypes;
import io.realm.entities.AllTypesPrimaryKey;
import io.realm.entities.AnnotationTypes;
import io.realm.entities.DefaultValueOfField;
import io.realm.entities.Dog;
import io.realm.entities.NoPrimaryKeyNullTypes;
import io.realm.entities.NullTypes;
import io.realm.entities.OwnerPrimaryKey;
import io.realm.entities.PrimitiveListTypes;
import io.realm.entities.RandomPrimaryKey;
import io.realm.exceptions.RealmException;
import io.realm.internal.Util;
import io.realm.rule.TestRealmConfigurationFactory;

import static io.realm.internal.test.ExtraTests.assertArrayEquals;
import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeThat;

@RunWith(AndroidJUnit4.class)
public class RealmJsonTests {
    private static final Charset UTF_8 = Charset.forName("UTF-8");

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
        return new ByteArrayInputStream(obj.toString().getBytes(UTF_8));
    }

    // Asserts that the list of AllTypesPrimaryKey objects where inserted and updated properly.
    private void assertAllTypesPrimaryKeyUpdated() {
        assertEquals(1, realm.where(AllTypesPrimaryKey.class).count());
        AllTypesPrimaryKey obj = realm.where(AllTypesPrimaryKey.class).findFirst();
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

    // Checks the imported object from nulltyps.json[0].
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

    // Checks the imported object from nulltyps.json[1].
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
        assertEquals(0, realm.where(AllTypes.class).count());
    }

    @Test
    public void createAllFromJson_nullArray() {
        realm.createAllFromJson(AllTypes.class, (JSONArray) null);
        assertEquals(0, realm.where(AllTypes.class).count());

    }

    @Test
    public void createObjectFromJson_allSimpleObjectAllTypes() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("columnString", "String");
        json.put("columnLong", 1L);
        json.put("columnFloat", 1.23F);
        json.put("columnDouble", 1.23D);
        json.put("columnBoolean", true);
        json.put("columnBinary", new String(Base64.encode(new byte[] {1,2,3}, Base64.DEFAULT), UTF_8));

        realm.beginTransaction();
        realm.createObjectFromJson(AllTypes.class, json);
        realm.commitTransaction();
        AllTypes obj = realm.where(AllTypes.class).findFirst();

        // Checks that all primitive types are imported correctly.
        assertEquals("String", obj.getColumnString());
        assertEquals(1L, obj.getColumnLong());
        assertEquals(1.23F, obj.getColumnFloat(), 0F);
        assertEquals(1.23D, obj.getColumnDouble(), 0D);
        assertEquals(true, obj.isColumnBoolean());
        assertArrayEquals(new byte[]{1, 2, 3}, obj.getColumnBinary());
    }

    @Test
    public void createObjectFromJson_dateAsLong() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("columnDate", 1000L); // Realm operates at seconds level granularity.

        realm.beginTransaction();
        realm.createObjectFromJson(AllTypes.class, json);
        realm.commitTransaction();

        AllTypes obj = realm.where(AllTypes.class).findFirst();
        assertEquals(new Date(1000), obj.getColumnDate());
    }

    @Test
    public void createObjectFromJson_dateAsString() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("columnDate", "/Date(1000)/");

        realm.beginTransaction();
        realm.createObjectFromJson(AllTypes.class, json);
        realm.commitTransaction();

        AllTypes obj = realm.where(AllTypes.class).findFirst();
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

        AllTypes obj = realm.where(AllTypes.class).findFirst();
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("Australia/West"));
        cal.set(2015, Calendar.OCTOBER, 03, 14, 45, 33);
        cal.set(Calendar.MILLISECOND, 376);
        Date convDate = obj.getColumnDate();

        assertEquals(convDate.getTime(), cal.getTimeInMillis());
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

        AllTypes obj = realm.where(AllTypes.class).findFirst();
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

        AllTypes obj = realm.where(AllTypes.class).findFirst();
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

        AllTypes obj = realm.where(AllTypes.class).findFirst();
        assertEquals(0, obj.getColumnRealmList().size());
    }

    @Test
    public void createObjectFromJson_stringSimpleObject() {
        realm.beginTransaction();
        Dog dog = realm.createObjectFromJson(Dog.class, "{ name: \"Foo\" }");
        realm.commitTransaction();

        assertEquals("Foo", dog.getName());
        assertEquals("Foo", realm.where(Dog.class).findFirst().getName());
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
        assertEquals(0, realm.where(Dog.class).count());
    }

    @Test
    public void createAllFromJson_jsonArrayEmpty() {
        JSONArray array = new JSONArray();
        realm.beginTransaction();
        realm.createAllFromJson(AllTypes.class, array);
        realm.commitTransaction();

        assertEquals(0, realm.where(AllTypes.class).count());
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

        assertEquals(3, realm.where(Dog.class).count());
        assertEquals(1, realm.where(Dog.class).equalTo("name", "Fido-3").findAll().size());
    }

    @Test
    public void createFromJson_respectDefaultValues() throws JSONException {
        final long fieldLongPrimaryKeyValue = DefaultValueOfField.FIELD_LONG_PRIMARY_KEY_DEFAULT_VALUE + 1;

        // Step 1: Prepares almost empty JSON.
        final JSONObject json = new JSONObject();
        json.put(DefaultValueOfField.FIELD_LONG_PRIMARY_KEY, fieldLongPrimaryKeyValue);

        // Step 2: Updates with almost empty JSONObject.
        realm.beginTransaction();
        final DefaultValueOfField managedObj = realm.createOrUpdateObjectFromJson(DefaultValueOfField.class, json);
        realm.commitTransaction();

        // Step 3: Checks that default values are applied.
        assertEquals(DefaultValueOfField.FIELD_IGNORED_DEFAULT_VALUE,
                managedObj.getFieldIgnored());
        assertEquals(DefaultValueOfField.FIELD_STRING_DEFAULT_VALUE, managedObj.getFieldString());
        assertFalse(Util.isEmptyString(managedObj.getFieldRandomString()));
        assertEquals(DefaultValueOfField.FIELD_SHORT_DEFAULT_VALUE, managedObj.getFieldShort());
        assertEquals(DefaultValueOfField.FIELD_INT_DEFAULT_VALUE, managedObj.getFieldInt());
        assertEquals(fieldLongPrimaryKeyValue, managedObj.getFieldLongPrimaryKey());
        assertEquals(DefaultValueOfField.FIELD_LONG_DEFAULT_VALUE, managedObj.getFieldLong());
        assertEquals(DefaultValueOfField.FIELD_BYTE_DEFAULT_VALUE, managedObj.getFieldByte());
        assertEquals(DefaultValueOfField.FIELD_FLOAT_DEFAULT_VALUE, managedObj.getFieldFloat(), 0f);
        assertEquals(DefaultValueOfField.FIELD_DOUBLE_DEFAULT_VALUE, managedObj.getFieldDouble(), 0d);
        assertEquals(DefaultValueOfField.FIELD_BOOLEAN_DEFAULT_VALUE, managedObj.isFieldBoolean());
        assertEquals(DefaultValueOfField.FIELD_DATE_DEFAULT_VALUE, managedObj.getFieldDate());
        assertArrayEquals(DefaultValueOfField.FIELD_BINARY_DEFAULT_VALUE, managedObj.getFieldBinary());
        assertArrayEquals(DefaultValueOfField.FIELD_BYTE_LIST_DEFAULT_VALUE.toArray(), managedObj.getFieldByteList().toArray());
        assertArrayEquals(DefaultValueOfField.FIELD_SHORT_LIST_DEFAULT_VALUE.toArray(), managedObj.getFieldShortList().toArray());
        assertArrayEquals(DefaultValueOfField.FIELD_INTEGER_LIST_DEFAULT_VALUE.toArray(), managedObj.getFieldIntegerList().toArray());
        assertArrayEquals(DefaultValueOfField.FIELD_LONG_LIST_DEFAULT_VALUE.toArray(), managedObj.getFieldLongList().toArray());
        assertArrayEquals(DefaultValueOfField.FIELD_BOOLEAN_LIST_DEFAULT_VALUE.toArray(), managedObj.getFieldBooleanList().toArray());
        assertArrayEquals(DefaultValueOfField.FIELD_BINARY_LIST_DEFAULT_VALUE.toArray(), managedObj.getFieldBinaryList().toArray());
        assertArrayEquals(DefaultValueOfField.FIELD_STRING_LIST_DEFAULT_VALUE.toArray(), managedObj.getFieldStringList().toArray());
        assertArrayEquals(DefaultValueOfField.FIELD_DATE_LIST_DEFAULT_VALUE.toArray(), managedObj.getFieldDateList().toArray());
        assertEquals(RandomPrimaryKey.FIELD_INT_DEFAULT_VALUE, managedObj.getFieldObject().getFieldInt());
        assertEquals(1, managedObj.getFieldList().size());
        assertEquals(RandomPrimaryKey.FIELD_INT_DEFAULT_VALUE, managedObj.getFieldList().first().getFieldInt());

        // Makes sure that excess object by default value is not created.
        assertEquals(2, realm.where(RandomPrimaryKey.class).count());
    }

    @Test
    public void createFromJson_defaultValuesAreIgnored() throws JSONException {
        final long fieldLongPrimaryKeyValue = DefaultValueOfField.FIELD_LONG_PRIMARY_KEY_DEFAULT_VALUE + 1;

        // Step 1: Prepares JSON.
        final String fieldIgnoredValue = DefaultValueOfField.FIELD_IGNORED_DEFAULT_VALUE + ".modified";
        final String fieldStringValue = DefaultValueOfField.FIELD_STRING_DEFAULT_VALUE + ".modified";
        final String fieldRandomStringValue = "non-random";
        final short fieldShortValue = (short) (DefaultValueOfField.FIELD_SHORT_DEFAULT_VALUE + 1);
        final int fieldIntValue = DefaultValueOfField.FIELD_INT_DEFAULT_VALUE + 1;
        final long fieldLongValue = DefaultValueOfField.FIELD_LONG_DEFAULT_VALUE + 1;
        final byte fieldByteValue = (byte) (DefaultValueOfField.FIELD_BYTE_DEFAULT_VALUE + 1);
        final float fieldFloatValue = DefaultValueOfField.FIELD_FLOAT_DEFAULT_VALUE + 1;
        final double fieldDoubleValue = DefaultValueOfField.FIELD_DOUBLE_DEFAULT_VALUE + 1;
        final boolean fieldBooleanValue = !DefaultValueOfField.FIELD_BOOLEAN_DEFAULT_VALUE;
        final Date fieldDateValue = new Date(DefaultValueOfField.FIELD_DATE_DEFAULT_VALUE.getTime() + 1);
        final byte[] fieldBinaryValue = {(byte) (DefaultValueOfField.FIELD_BINARY_DEFAULT_VALUE[0] - 1)};
        final int fieldObjectIntValue = RandomPrimaryKey.FIELD_INT_DEFAULT_VALUE + 1;
        final int fieldListIntValue = RandomPrimaryKey.FIELD_INT_DEFAULT_VALUE + 2;

        final JSONObject json = new JSONObject();
        json.put(DefaultValueOfField.FIELD_LONG_PRIMARY_KEY, fieldLongPrimaryKeyValue);
        json.put(DefaultValueOfField.FIELD_IGNORED, fieldIgnoredValue);
        json.put(DefaultValueOfField.FIELD_STRING, fieldStringValue);
        json.put(DefaultValueOfField.FIELD_RANDOM_STRING, fieldRandomStringValue);
        json.put(DefaultValueOfField.FIELD_SHORT, fieldShortValue);
        json.put(DefaultValueOfField.FIELD_INT, fieldIntValue);
        json.put(DefaultValueOfField.FIELD_LONG, fieldLongValue);
        json.put(DefaultValueOfField.FIELD_BYTE, fieldByteValue);
        json.put(DefaultValueOfField.FIELD_FLOAT, fieldFloatValue);
        json.put(DefaultValueOfField.FIELD_DOUBLE, fieldDoubleValue);
        json.put(DefaultValueOfField.FIELD_BOOLEAN, fieldBooleanValue);
        json.put(DefaultValueOfField.FIELD_DATE, getISO8601Date(fieldDateValue));
        json.put(DefaultValueOfField.FIELD_BINARY, Base64.encodeToString(fieldBinaryValue, Base64.DEFAULT));
        // Value for 'fieldObject'
        final JSONObject fieldObjectJson = new JSONObject();
        fieldObjectJson.put(RandomPrimaryKey.FIELD_RANDOM_PRIMARY_KEY, "pk of fieldObject");
        fieldObjectJson.put(RandomPrimaryKey.FIELD_INT, fieldObjectIntValue);
        json.put(DefaultValueOfField.FIELD_OBJECT, fieldObjectJson);
        // Value for 'fieldList'
        final JSONArray fieldListArrayJson = new JSONArray();
        final JSONObject fieldListItem0Json = new JSONObject();
        fieldListItem0Json.put(RandomPrimaryKey.FIELD_RANDOM_PRIMARY_KEY, "pk1 of fieldList");
        fieldListItem0Json.put(RandomPrimaryKey.FIELD_INT, fieldListIntValue);
        fieldListArrayJson.put(fieldListItem0Json);
        final JSONObject fieldListItem1Json = new JSONObject();
        fieldListItem1Json.put(RandomPrimaryKey.FIELD_RANDOM_PRIMARY_KEY, "pk2 of fieldList");
        fieldListItem1Json.put(RandomPrimaryKey.FIELD_INT, fieldListIntValue + 1);
        fieldListArrayJson.put(fieldListItem1Json);
        json.put(DefaultValueOfField.FIELD_LIST, fieldListArrayJson);

        // Step 3: Updates with JSONObject.
        realm.beginTransaction();
        final DefaultValueOfField managedObj = realm.createOrUpdateObjectFromJson(DefaultValueOfField.class, json);
        realm.commitTransaction();

        // Step 4: Checks that properly created.
        assertEquals(DefaultValueOfField.FIELD_IGNORED_DEFAULT_VALUE /* not fieldIgnoredValue */,
                managedObj.getFieldIgnored());
        assertEquals(fieldStringValue, managedObj.getFieldString());
        assertEquals(fieldRandomStringValue, managedObj.getFieldRandomString());
        assertEquals(fieldShortValue, managedObj.getFieldShort());
        assertEquals(fieldIntValue, managedObj.getFieldInt());
        assertEquals(fieldLongPrimaryKeyValue, managedObj.getFieldLongPrimaryKey());
        assertEquals(fieldLongValue, managedObj.getFieldLong());
        assertEquals(fieldByteValue, managedObj.getFieldByte());
        assertEquals(fieldFloatValue, managedObj.getFieldFloat(), 0f);
        assertEquals(fieldDoubleValue, managedObj.getFieldDouble(), 0d);
        assertEquals(fieldBooleanValue, managedObj.isFieldBoolean());
        assertEquals(fieldDateValue, managedObj.getFieldDate());
        assertTrue(Arrays.equals(fieldBinaryValue, managedObj.getFieldBinary()));
        assertEquals(fieldObjectJson.getString(RandomPrimaryKey.FIELD_RANDOM_PRIMARY_KEY),
                managedObj.getFieldObject().getFieldRandomPrimaryKey());
        assertEquals(fieldObjectIntValue, managedObj.getFieldObject().getFieldInt());
        assertEquals(2, managedObj.getFieldList().size());
        assertEquals(fieldListItem0Json.get(RandomPrimaryKey.FIELD_RANDOM_PRIMARY_KEY),
                managedObj.getFieldList().get(0).getFieldRandomPrimaryKey());
        assertEquals(fieldListIntValue, managedObj.getFieldList().get(0).getFieldInt());
        assertEquals(fieldListItem1Json.get(RandomPrimaryKey.FIELD_RANDOM_PRIMARY_KEY),
                managedObj.getFieldList().get(1).getFieldRandomPrimaryKey());
        assertEquals(fieldListIntValue + 1, managedObj.getFieldList().get(1).getFieldInt());

        // Makes sure that excess object by default value is not created.
        assertEquals(3, realm.where(RandomPrimaryKey.class).count());
    }

    private String getISO8601Date(Date date) {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
        df.setTimeZone(tz);
        return df.format(date);
    }

    @Test
    public void updateFromJson_defaultValuesAreIgnored() throws JSONException {
        final long fieldLongPrimaryKeyValue = DefaultValueOfField.FIELD_LONG_PRIMARY_KEY_DEFAULT_VALUE + 1;

        // Step 1: Creates an object with default values.
        final DefaultValueOfField original;
        realm.beginTransaction(); {
            original = realm.createObject(DefaultValueOfField.class, fieldLongPrimaryKeyValue);
        }
        realm.commitTransaction();

        // Step 2: Prepares JSON.
        final String fieldIgnoredValue = DefaultValueOfField.FIELD_IGNORED_DEFAULT_VALUE + ".modified";
        final String fieldStringValue = DefaultValueOfField.FIELD_STRING_DEFAULT_VALUE + ".modified";
        final String fieldRandomStringValue = "non-random";
        final short fieldShortValue = (short) (DefaultValueOfField.FIELD_SHORT_DEFAULT_VALUE + 1);
        final int fieldIntValue = DefaultValueOfField.FIELD_INT_DEFAULT_VALUE + 1;
        final long fieldLongValue = DefaultValueOfField.FIELD_LONG_DEFAULT_VALUE + 1;
        final byte fieldByteValue = (byte) (DefaultValueOfField.FIELD_BYTE_DEFAULT_VALUE + 1);
        final float fieldFloatValue = DefaultValueOfField.FIELD_FLOAT_DEFAULT_VALUE + 1;
        final double fieldDoubleValue = DefaultValueOfField.FIELD_DOUBLE_DEFAULT_VALUE + 1;
        final boolean fieldBooleanValue = !DefaultValueOfField.FIELD_BOOLEAN_DEFAULT_VALUE;
        final Date fieldDateValue = new Date(DefaultValueOfField.FIELD_DATE_DEFAULT_VALUE.getTime() + 1);
        final byte[] fieldBinaryValue = {(byte) (DefaultValueOfField.FIELD_BINARY_DEFAULT_VALUE[0] - 1)};
        final int fieldObjectIntValue = RandomPrimaryKey.FIELD_INT_DEFAULT_VALUE + 1;
        final int fieldListIntValue = RandomPrimaryKey.FIELD_INT_DEFAULT_VALUE + 2;

        final JSONObject json = new JSONObject();
        json.put(DefaultValueOfField.FIELD_LONG_PRIMARY_KEY, fieldLongPrimaryKeyValue);
        json.put(DefaultValueOfField.FIELD_IGNORED, fieldIgnoredValue);
        json.put(DefaultValueOfField.FIELD_STRING, fieldStringValue);
        json.put(DefaultValueOfField.FIELD_RANDOM_STRING, fieldRandomStringValue);
        json.put(DefaultValueOfField.FIELD_SHORT, fieldShortValue);
        json.put(DefaultValueOfField.FIELD_INT, fieldIntValue);
        json.put(DefaultValueOfField.FIELD_LONG, fieldLongValue);
        json.put(DefaultValueOfField.FIELD_BYTE, fieldByteValue);
        json.put(DefaultValueOfField.FIELD_FLOAT, fieldFloatValue);
        json.put(DefaultValueOfField.FIELD_DOUBLE, fieldDoubleValue);
        json.put(DefaultValueOfField.FIELD_BOOLEAN, fieldBooleanValue);
        json.put(DefaultValueOfField.FIELD_DATE, getISO8601Date(fieldDateValue));
        json.put(DefaultValueOfField.FIELD_BINARY, Base64.encodeToString(fieldBinaryValue, Base64.DEFAULT));
        // value for 'fieldObject'
        final JSONObject fieldObjectJson = new JSONObject();
        fieldObjectJson.put(RandomPrimaryKey.FIELD_RANDOM_PRIMARY_KEY,
                original.getFieldObject().getFieldRandomPrimaryKey());
        fieldObjectJson.put(RandomPrimaryKey.FIELD_INT, fieldObjectIntValue);
        json.put(DefaultValueOfField.FIELD_OBJECT, fieldObjectJson);
        // Value for 'fieldList'
        final JSONArray fieldListArrayJson = new JSONArray();
        final JSONObject fieldListItem0Json = new JSONObject(); // To be added.
        fieldListItem0Json.put(RandomPrimaryKey.FIELD_RANDOM_PRIMARY_KEY,  "unique value");
        fieldListItem0Json.put(RandomPrimaryKey.FIELD_INT, fieldListIntValue);
        fieldListArrayJson.put(fieldListItem0Json);
        final JSONObject fieldListItem1Json = new JSONObject(); // To be updated.
        fieldListItem1Json.put(RandomPrimaryKey.FIELD_RANDOM_PRIMARY_KEY,
                original.getFieldList().first().getFieldRandomPrimaryKey());
        fieldListItem1Json.put(RandomPrimaryKey.FIELD_INT, fieldListIntValue + 1);
        fieldListArrayJson.put(fieldListItem1Json);
        json.put(DefaultValueOfField.FIELD_LIST, fieldListArrayJson);

        // Step 3: Updates with JSONObject.
        realm.beginTransaction();
        final DefaultValueOfField managedObj = realm.createOrUpdateObjectFromJson(DefaultValueOfField.class, json);
        realm.commitTransaction();

        // Step 4: Checks that properly updated.
        assertEquals(DefaultValueOfField.FIELD_IGNORED_DEFAULT_VALUE /* not fieldIgnoredValue */,
                managedObj.getFieldIgnored());
        assertEquals(fieldStringValue, managedObj.getFieldString());
        assertEquals(fieldRandomStringValue, managedObj.getFieldRandomString());
        assertEquals(fieldShortValue, managedObj.getFieldShort());
        assertEquals(fieldIntValue, managedObj.getFieldInt());
        assertEquals(fieldLongPrimaryKeyValue, managedObj.getFieldLongPrimaryKey());
        assertEquals(fieldLongValue, managedObj.getFieldLong());
        assertEquals(fieldByteValue, managedObj.getFieldByte());
        assertEquals(fieldFloatValue, managedObj.getFieldFloat(), 0f);
        assertEquals(fieldDoubleValue, managedObj.getFieldDouble(), 0d);
        assertEquals(fieldBooleanValue, managedObj.isFieldBoolean());
        assertEquals(fieldDateValue, managedObj.getFieldDate());
        assertTrue(Arrays.equals(fieldBinaryValue, managedObj.getFieldBinary()));
        assertEquals(fieldObjectIntValue, managedObj.getFieldObject().getFieldInt());
        assertEquals(2, managedObj.getFieldList().size());
        assertEquals("unique value", managedObj.getFieldList().get(0).getFieldRandomPrimaryKey());
        assertEquals(fieldListIntValue, managedObj.getFieldList().get(0).getFieldInt());
        assertEquals(fieldListItem1Json.get(RandomPrimaryKey.FIELD_RANDOM_PRIMARY_KEY),
                managedObj.getFieldList().get(1).getFieldRandomPrimaryKey());
        assertEquals(fieldListIntValue + 1, managedObj.getFieldList().get(1).getFieldInt());

        // Makes sure that excess object by default value is not created.
        assertEquals(3/* 2 updated + 1 added*/, realm.where(RandomPrimaryKey.class).count());
    }

    // Tests if Json object doesn't have the field, then the field should have default value.
    @Test
    public void createObjectFromJson_noValues() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("noThingHere", JSONObject.NULL);

        realm.beginTransaction();
        realm.createObjectFromJson(AllTypes.class, json);
        realm.commitTransaction();

        // Checks that all primitive types are imported correctly.
        AllTypes obj = realm.where(AllTypes.class).findFirst();
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

    // Tests that given an exception everything up to the exception is saved.
    @Test
    public void createObjectFromJson_jsonException() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("columnString", "Foo");
        json.put("columnDate", "Boom");

        realm.beginTransaction();
        try {
            realm.createObjectFromJson(AllTypes.class, json);
            fail();
        } catch (RealmException ignored) {
        } finally {
            realm.commitTransaction();
        }

        AllTypes obj = realm.where(AllTypes.class).findFirst();
        assertEquals("Foo", obj.getColumnString());
        assertEquals(new Date(0), obj.getColumnDate());
    }

    @Test
    public void createObjectFromJson_respectIgnoredFields() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", 0);
        json.put("indexString", "Foo");
        json.put("notIndexString", "Bar");
        json.put("ignoreString", "Baz");

        realm.beginTransaction();
        realm.createObjectFromJson(AnnotationTypes.class, json);
        realm.commitTransaction();

        AnnotationTypes annotationsObject = realm.where(AnnotationTypes.class).findFirst();
        assertEquals("Foo", annotationsObject.getIndexString());
        assertEquals(null, annotationsObject.getIgnoreString());
    }

    @Test
    public void createAllFromJson_stringArraySimpleArray() {
        realm.beginTransaction();
        realm.createAllFromJson(Dog.class, "[{ name: \"Foo\" }, { name: \"Bar\" }]");
        realm.commitTransaction();

        assertEquals(2, realm.where(Dog.class).count());
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

        assertEquals(0, realm.where(Dog.class).count());
    }

    @Test
    public void createAllFromJson_stringEmptyArray() {
        realm.beginTransaction();
        realm.createAllFromJson(Dog.class, "");
        realm.commitTransaction();
        assertEquals(0, realm.where(Dog.class).count());
    }

    @Test
    public void createAllFromJson_stringNullClass() {
        realm.beginTransaction();
        realm.createAllFromJson(null, "[{ name: \"Foo\" }]");
        realm.commitTransaction();

        assertEquals(0, realm.where(Dog.class).count());
    }


    @Test
    public void createAllFromJson_streamNull() throws IOException {
        assumeThat(Build.VERSION.SDK_INT, greaterThanOrEqualTo(Build.VERSION_CODES.HONEYCOMB));

        realm.createAllFromJson(AllTypes.class, (InputStream) null);
        assertEquals(0, realm.where(AllTypes.class).count());
    }

    @Test
    public void createObjectFromJson_streamAllSimpleTypes() throws IOException {
        assumeThat(Build.VERSION.SDK_INT, greaterThanOrEqualTo(Build.VERSION_CODES.HONEYCOMB));

        InputStream in = TestHelper.loadJsonFromAssets(context, "all_simple_types.json");
        realm.beginTransaction();
        realm.createObjectFromJson(AllTypes.class, in);
        realm.commitTransaction();
        in.close();

        // Checks that all primitive types are imported correctly.
        AllTypes obj = realm.where(AllTypes.class).findFirst();
        assertEquals("String", obj.getColumnString());
        assertEquals(1L, obj.getColumnLong());
        assertEquals(1.23F, obj.getColumnFloat(), 0F);
        assertEquals(1.23D, obj.getColumnDouble(), 0D);
        assertEquals(true, obj.isColumnBoolean());
        assertArrayEquals(new byte[]{1, 2, 3}, obj.getColumnBinary());
    }

    @Test
    public void createObjectFromJson_streamDateAsLong() throws IOException {
        assumeThat(Build.VERSION.SDK_INT, greaterThanOrEqualTo(Build.VERSION_CODES.HONEYCOMB));

        InputStream in = TestHelper.loadJsonFromAssets(context, "date_as_long.json");
        realm.beginTransaction();
        realm.createObjectFromJson(AllTypes.class, in);
        realm.commitTransaction();
        in.close();

        // Checks that all primitive types are imported correctly.
        AllTypes obj = realm.where(AllTypes.class).findFirst();
        assertEquals(new Date(1000), obj.getColumnDate());
    }

    @Test
    public void createObjectFromJson_streamDateAsString() throws IOException {
        assumeThat(Build.VERSION.SDK_INT, greaterThanOrEqualTo(Build.VERSION_CODES.HONEYCOMB));

        InputStream in = TestHelper.loadJsonFromAssets(context, "date_as_string.json");
        realm.beginTransaction();
        realm.createObjectFromJson(AllTypes.class, in);
        realm.commitTransaction();
        in.close();

        // Checks that all primitive types are imported correctly.
        AllTypes obj = realm.where(AllTypes.class).findFirst();
        assertEquals(new Date(1000), obj.getColumnDate());
    }

    @Test
    public void createObjectFromJson_streamDateAsISO8601String() throws IOException {
        assumeThat(Build.VERSION.SDK_INT, greaterThanOrEqualTo(Build.VERSION_CODES.HONEYCOMB));

        InputStream in = TestHelper.loadJsonFromAssets(context, "date_as_iso8601_string.json");
        realm.beginTransaction();
        realm.createObjectFromJson(AllTypes.class, in);
        realm.commitTransaction();
        in.close();

        Calendar cal = new GregorianCalendar(2007, 8 - 1, 13, 19, 51, 23);
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        cal.set(Calendar.MILLISECOND, 789);
        Date date = cal.getTime();

        // Checks that all primitive types are imported correctly.
        AllTypes obj = realm.where(AllTypes.class).findFirst();
        assertEquals(date, obj.getColumnDate());
    }

    @Test
    public void createObjectFromJson_streamChildObject() throws IOException {
        assumeThat(Build.VERSION.SDK_INT, greaterThanOrEqualTo(Build.VERSION_CODES.HONEYCOMB));

        InputStream in = TestHelper.loadJsonFromAssets(context, "single_child_object.json");
        realm.beginTransaction();
        realm.createObjectFromJson(AllTypes.class, in);
        realm.commitTransaction();
        in.close();

        AllTypes obj = realm.where(AllTypes.class).findFirst();
        assertEquals("Fido", obj.getColumnRealmObject().getName());
    }

    @Test
    public void createObjectFromJson_streamEmptyChildObjectList() throws IOException {
        assumeThat(Build.VERSION.SDK_INT, greaterThanOrEqualTo(Build.VERSION_CODES.HONEYCOMB));

        InputStream in = TestHelper.loadJsonFromAssets(context, "realmlist_empty.json");
        realm.beginTransaction();
        realm.createObjectFromJson(AllTypes.class, in);
        realm.commitTransaction();
        in.close();

        AllTypes obj = realm.where(AllTypes.class).findFirst();
        assertEquals(0, obj.getColumnRealmList().size());
    }

    @Test
    public void createObjectFromJson_streamChildObjectList() throws IOException {
        assumeThat(Build.VERSION.SDK_INT, greaterThanOrEqualTo(Build.VERSION_CODES.HONEYCOMB));

        InputStream in = TestHelper.loadJsonFromAssets(context, "realmlist.json");
        realm.beginTransaction();
        realm.createObjectFromJson(AllTypes.class, in);
        realm.commitTransaction();
        in.close();

        assertEquals(3, realm.where(Dog.class).count());
        assertEquals(1, realm.where(Dog.class).equalTo("name", "Fido-3").findAll().size());
    }

    @Test
    public void createAllFromJson_streamArray() throws IOException {
        assumeThat(Build.VERSION.SDK_INT, greaterThanOrEqualTo(Build.VERSION_CODES.HONEYCOMB));

        InputStream in = TestHelper.loadJsonFromAssets(context, "array.json");
        realm.beginTransaction();
        realm.createAllFromJson(Dog.class, in);
        realm.commitTransaction();

        assertEquals(3, realm.where(Dog.class).count());
        assertEquals(1, realm.where(Dog.class).equalTo("name", "Fido-3").findAll().size());
    }


    // Tests if Json object doesn't have the field, then the field should have default value. Stream version.
    @Test
    public void createObjectFromJson_streamNoValues() throws IOException {
        assumeThat(Build.VERSION.SDK_INT, greaterThanOrEqualTo(Build.VERSION_CODES.HONEYCOMB));

        InputStream in = TestHelper.loadJsonFromAssets(context, "other_json_object.json");
        realm.beginTransaction();
        realm.createObjectFromJson(AllTypes.class, in);
        realm.commitTransaction();
        in.close();

        // Checks that all primitive types are imported correctly.
        AllTypes obj = realm.where(AllTypes.class).findFirst();
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

    @Test
    public void createObjectFromJson_streamNullClass() throws IOException {
        assumeThat(Build.VERSION.SDK_INT, greaterThanOrEqualTo(Build.VERSION_CODES.HONEYCOMB));

        InputStream in = TestHelper.loadJsonFromAssets(context, "array.json");
        realm.beginTransaction();
        assertNull(realm.createObjectFromJson(null, in));
        realm.commitTransaction();
        in.close();
    }

    @Test
    public void createObjectFromJson_streamNullJson() throws IOException {
        assumeThat(Build.VERSION.SDK_INT, greaterThanOrEqualTo(Build.VERSION_CODES.HONEYCOMB));

        InputStream in = TestHelper.loadJsonFromAssets(context, "all_types_invalid.json");
        realm.beginTransaction();
        try {
            realm.createObjectFromJson(AnnotationTypes.class, in);
            fail();
        } catch (RealmException ignored) {
        } finally {
            realm.commitTransaction();
            in.close();
        }
    }

    @Test
    public void createObjectFromJson_streamNullInputStream() throws IOException {
        assumeThat(Build.VERSION.SDK_INT, greaterThanOrEqualTo(Build.VERSION_CODES.HONEYCOMB));

        realm.beginTransaction();
        assertNull(realm.createObjectFromJson(AnnotationTypes.class, (InputStream) null));
        realm.commitTransaction();
    }

    /**
     * Tests updating a existing object with JSON stream. Only primary key in JSON.
     * No value should be changed.
     */
    @Test
    public void createOrUpdateObjectFromJson_streamNullValues() throws IOException {
        assumeThat(Build.VERSION.SDK_INT, greaterThanOrEqualTo(Build.VERSION_CODES.HONEYCOMB));

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

        // Checks that all primitive types are imported correctly.
        obj = realm.where(AllTypesPrimaryKey.class).findFirst();
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
    public void createOrUpdateObjectFromJson_streamNullClass() throws IOException {
        assumeThat(Build.VERSION.SDK_INT, greaterThanOrEqualTo(Build.VERSION_CODES.HONEYCOMB));

        InputStream in = TestHelper.loadJsonFromAssets(context, "all_types_primary_key_field_only.json");
        realm.beginTransaction();
        assertNull(realm.createOrUpdateObjectFromJson(null, in));
        realm.commitTransaction();
        in.close();
    }

    @Test
    public void createOrUpdateObjectFromJson_streamInvalidJson() throws IOException {
        assumeThat(Build.VERSION.SDK_INT, greaterThanOrEqualTo(Build.VERSION_CODES.HONEYCOMB));

        AllTypesPrimaryKey obj = new AllTypesPrimaryKey();
        obj.setColumnLong(1);
        realm.beginTransaction();
        realm.copyToRealm(obj);
        realm.commitTransaction();

        InputStream in = TestHelper.loadJsonFromAssets(context, "all_types_invalid.json");
        realm.beginTransaction();
        try {
            realm.createOrUpdateObjectFromJson(AllTypesPrimaryKey.class, in);
            fail();
        } catch (RealmException ignored) {
        } finally {
            realm.commitTransaction();
            in.close();
        }
    }

    @Test
    public void createOrUpdateObjectFromJson_streamNoPrimaryKeyThrows() throws IOException {
        assumeThat(Build.VERSION.SDK_INT, greaterThanOrEqualTo(Build.VERSION_CODES.HONEYCOMB));

        try {
            realm.createOrUpdateObjectFromJson(AllTypes.class, new TestHelper.StubInputStream());
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void createOrUpdateAllFromJson_streamInvalidJSonCurlyBracketThrows() throws IOException {
        assumeThat(Build.VERSION.SDK_INT, greaterThanOrEqualTo(Build.VERSION_CODES.HONEYCOMB));

        try {
            realm.createOrUpdateAllFromJson(AllTypesPrimaryKey.class, TestHelper.stringToStream("{"));
            fail();
        } catch (RealmException ignored) {
        }
    }

    @Test
    public void createOrUpdateObjectFromJson_streamIgnoreUnsetProperties() throws IOException {
        assumeThat(Build.VERSION.SDK_INT, greaterThanOrEqualTo(Build.VERSION_CODES.HONEYCOMB));

        realm.beginTransaction();
        realm.createOrUpdateAllFromJson(AllTypesPrimaryKey.class, TestHelper.loadJsonFromAssets(context, "list_alltypes_primarykey.json"));
        realm.commitTransaction();

        // No-op as no properties should be updated.
        realm.beginTransaction();
        realm.createOrUpdateObjectFromJson(AllTypesPrimaryKey.class, TestHelper.stringToStream("{ \"columnLong\":1 }"));
        realm.commitTransaction();

        assertAllTypesPrimaryKeyUpdated();
    }

    @Test
    public void createOrUpdateObjectFromJson_inputStream() throws IOException {
        assumeThat(Build.VERSION.SDK_INT, greaterThanOrEqualTo(Build.VERSION_CODES.HONEYCOMB));

        realm.beginTransaction();

        AllTypesPrimaryKey obj = new AllTypesPrimaryKey();
        obj.setColumnLong(1);
        obj.setColumnString("Foo");
        realm.copyToRealm(obj);

        InputStream in = TestHelper.stringToStream("{ \"columnLong\" : 1, \"columnString\" : \"bar\" }");
        AllTypesPrimaryKey newObj = realm.createOrUpdateObjectFromJson(AllTypesPrimaryKey.class, in);
        realm.commitTransaction();

        assertEquals(1, realm.where(AllTypesPrimaryKey.class).count());
        assertEquals("bar", newObj.getColumnString());
    }

    /**
     * Checks that using createOrUpdateObject will set the primary key directly instead of first setting
     * it to the default value (which can fail).
     */
    @Test
    public void createOrUpdateObjectFromJson_objectWithPrimaryKeySetValueDirectlyFromStream() throws JSONException, IOException {
        assumeThat(Build.VERSION.SDK_INT, greaterThanOrEqualTo(Build.VERSION_CODES.HONEYCOMB));

        InputStream stream = TestHelper.stringToStream("{\"id\": 1, \"name\": \"bar\"}");
        realm.beginTransaction();
        realm.createObject(OwnerPrimaryKey.class, 0); // id = 0
        realm.createOrUpdateObjectFromJson(OwnerPrimaryKey.class, stream);
        realm.commitTransaction();

        RealmResults<OwnerPrimaryKey> owners = realm.where(OwnerPrimaryKey.class).findAll();
        assertEquals(2, owners.size());
        assertEquals(1, owners.get(1).getId());
        assertEquals("bar", owners.get(1).getName());
    }

    // Tests updating a existing object with JSON object with only primary key.
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

        // Checks that all primitive types are imported correctly.
        obj = realm.where(AllTypesPrimaryKey.class).findFirst();
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
    public void createOrUpdateObjectFromJson_stringNoPrimaryKeyThrows() throws IOException {
        try {
            realm.createOrUpdateObjectFromJson(AllTypes.class, "{}");
            fail();
        } catch (IllegalArgumentException ignored) {
        }
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
    public void createOrUpdateObjectFromJson_inputString() throws IOException {
        TestHelper.populateSimpleAllTypesPrimaryKey(realm);

        realm.beginTransaction();
        AllTypesPrimaryKey newObj = realm.createOrUpdateObjectFromJson(AllTypesPrimaryKey.class, "{ \"columnLong\" : 1, \"columnString\" : \"bar\" }");
        realm.commitTransaction();

        assertEquals(1, realm.where(AllTypesPrimaryKey.class).count());
        assertEquals("bar", newObj.getColumnString());
    }

    @Test
    public void createOrUpdateObjectFromJson_inputStringNullClass() throws IOException {
        TestHelper.populateSimpleAllTypesPrimaryKey(realm);

        realm.beginTransaction();
        assertNull(realm.createOrUpdateObjectFromJson(null, "{ \"columnLong\" : 1, \"columnString\" : \"bar\" }"));
        realm.commitTransaction();
    }

    @Test
    public void createOrUpdateObjectFromJson_nullInputString() throws IOException {
        TestHelper.populateSimpleAllTypesPrimaryKey(realm);

        realm.beginTransaction();
        assertNull(realm.createOrUpdateObjectFromJson(AllTypesPrimaryKey.class, (String) null));
        realm.commitTransaction();
    }

    @Test
    public void createOrUpdateObjectFromJson_emptyInputString() throws IOException {
        TestHelper.populateSimpleAllTypesPrimaryKey(realm);

        realm.beginTransaction();
        assertNull(realm.createOrUpdateObjectFromJson(AllTypesPrimaryKey.class, ""));
        realm.commitTransaction();
    }

    @Test
    public void createOrUpdateObjectFromJson_invalidInputString() throws IOException {
        TestHelper.populateSimpleAllTypesPrimaryKey(realm);

        realm.beginTransaction();
        try {
            realm.createOrUpdateObjectFromJson(AllTypesPrimaryKey.class, "{ \"columnLong\" : 1,");
            fail();
        } catch (RealmException ignored) {
        } finally {
            realm.commitTransaction();
        }
    }

    @Test
    public void createOrUpdateObjectFromJson_noPrimaryKeyThrows() {
        try {
            realm.createOrUpdateObjectFromJson(AllTypes.class, new JSONObject());
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void createOrUpdateObjectFromJson_withJsonObject() throws JSONException {
        TestHelper.populateSimpleAllTypesPrimaryKey(realm);

        realm.beginTransaction();
        JSONObject json = new JSONObject();
        json.put("columnLong", 1);
        json.put("columnString", "bar");
        AllTypesPrimaryKey newObj = realm.createOrUpdateObjectFromJson(AllTypesPrimaryKey.class, json);

        realm.commitTransaction();

        assertEquals(1, realm.where(AllTypesPrimaryKey.class).count());
        assertEquals("bar", newObj.getColumnString());
    }

    @Test
    public void createOrUpdateObjectFromJson_jsonObjectNullClass() throws JSONException {
        TestHelper.populateSimpleAllTypesPrimaryKey(realm);

        realm.beginTransaction();
        JSONObject json = new JSONObject();
        json.put("columnLong", 1);
        json.put("columnString", "bar");
        assertNull(realm.createOrUpdateObjectFromJson(null, json));
        realm.commitTransaction();

        AllTypesPrimaryKey obj2 = realm.where(AllTypesPrimaryKey.class).findFirst();
        assertEquals("Foo", obj2.getColumnString());
    }

    @Test
    public void createOrUpdateObjectFromJson_nullJsonObject() throws JSONException {
        realm.beginTransaction();
        assertNull(realm.createOrUpdateObjectFromJson(AllTypesPrimaryKey.class, (JSONObject) null));
        realm.commitTransaction();
        assertEquals(0, realm.where(AllTypesPrimaryKey.class).count());
    }

    @Test
    public void createOrUpdateObjectFromJson_invalidJsonObject() throws JSONException {
        TestHelper.populateSimpleAllTypesPrimaryKey(realm);

        realm.beginTransaction();
        JSONObject json = new JSONObject();
        json.put("columnLong", "A");
        try {
            realm.createOrUpdateObjectFromJson(AllTypesPrimaryKey.class, json);
            fail();
        } catch (RealmException ignored) {
        } finally {
            realm.commitTransaction();
        }
        AllTypesPrimaryKey obj2 = realm.where(AllTypesPrimaryKey.class).findFirst();
        assertEquals("Foo", obj2.getColumnString());
    }

    /**
     * Checks that using createOrUpdateObject will set the primary key directly instead of first setting
     * it to the default value (which can fail).
     */
    @Test
    public void createOrUpdateObjectFromJson_objectWithPrimaryKeySetValueDirectlyFromJsonObject() throws JSONException {
        JSONObject newObject = new JSONObject("{\"id\": 1, \"name\": \"bar\"}");
        realm.beginTransaction();
        realm.createObject(OwnerPrimaryKey.class, 0); // id = 0
        realm.createOrUpdateObjectFromJson(OwnerPrimaryKey.class, newObject);
        realm.commitTransaction();

        RealmResults<OwnerPrimaryKey> owners = realm.where(OwnerPrimaryKey.class).findAll();
        assertEquals(2, owners.size());
        assertEquals(1, owners.get(1).getId());
        assertEquals("bar", owners.get(1).getName());
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
    public void createOrUpdateAllFromJson_jsonNullClass() {
        realm.createOrUpdateAllFromJson(null, new JSONArray());
        assertEquals(0, realm.where(AllTypes.class).count());
    }

    @Test
    public void createOrUpdateAllFromJson_jsonNullJson() {
        realm.createOrUpdateAllFromJson(AllTypes.class, (JSONArray) null);
        assertEquals(0, realm.where(AllTypes.class).count());
    }

    @Test
    public void createOrUpdateAllFromJson_streamNoPrimaryKeyThrows() throws IOException {
        assumeThat(Build.VERSION.SDK_INT, greaterThanOrEqualTo(Build.VERSION_CODES.HONEYCOMB));

        try {
            realm.createOrUpdateAllFromJson(AllTypes.class, new TestHelper.StubInputStream());
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void createOrUpdateAllFromJson_streamInvalidJSonBracketThrows() throws IOException {
        assumeThat(Build.VERSION.SDK_INT, greaterThanOrEqualTo(Build.VERSION_CODES.HONEYCOMB));

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
    public void createOrUpdateAllFromJson_inputStringNullClass() {
        realm.beginTransaction();
        realm.createOrUpdateAllFromJson((Class<AllTypesPrimaryKey>) null, "{ \"columnLong\" : 1 }");
        realm.commitTransaction();
        assertEquals(0, realm.where(AllTypesPrimaryKey.class).count());
    }

    @Test
    public void createOrUpdateAllFromJson_inputStringNullJson() {
        realm.beginTransaction();
        realm.createOrUpdateAllFromJson(AllTypesPrimaryKey.class, (String) null);
        realm.commitTransaction();
        assertEquals(0, realm.where(AllTypesPrimaryKey.class).count());
    }

    @Test
    public void createOrUpdateAllFromJson_inputStringEmptyJson() {
        realm.beginTransaction();
        realm.createOrUpdateAllFromJson(AllTypesPrimaryKey.class, "");
        realm.commitTransaction();
        assertEquals(0, realm.where(AllTypesPrimaryKey.class).count());
    }

    @Test
    public void createOrUpdateAllFromJson_inputStringInvalidJson() {
        realm.beginTransaction();
        try {
            realm.createOrUpdateAllFromJson(AllTypesPrimaryKey.class, "{ \"columnLong\" : 1");
            fail();
        } catch (RealmException ignored) {
        } finally {
            realm.commitTransaction();
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
        assumeThat(Build.VERSION.SDK_INT, greaterThanOrEqualTo(Build.VERSION_CODES.HONEYCOMB));

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

    // Tests creating objects from Json, all nullable fields with null values or non-null values.
    @Test
    public void createAllFromJson_nullTypesJsonWithNulls() throws IOException, JSONException {
        String json = TestHelper.streamToString(TestHelper.loadJsonFromAssets(context, "nulltypes.json"));
        JSONArray array = new JSONArray(json);
        realm.beginTransaction();
        realm.createAllFromJson(NullTypes.class, array);
        realm.commitTransaction();

        RealmResults<NullTypes> nullTypesRealmResults = realm.where(NullTypes.class).findAll();
        assertEquals(3, nullTypesRealmResults.size());

        NullTypes nullTypes1 = nullTypesRealmResults.where().equalTo("id", 1).findFirst();
        checkNullableValuesAreNull(nullTypes1);

        NullTypes nullTypes2 = nullTypesRealmResults.where().equalTo("id", 2).findFirst();
        checkNullableValuesAreNotNull(nullTypes2);
    }

    // Tests creating objects form JSON stream, all nullable fields with null values or non-null values.
    @Test
    public void createAllFromJson_nullTypesStreamJSONWithNulls() throws IOException {
        assumeThat(Build.VERSION.SDK_INT, greaterThanOrEqualTo(Build.VERSION_CODES.HONEYCOMB));

        realm.beginTransaction();
        realm.createAllFromJson(NullTypes.class, TestHelper.loadJsonFromAssets(context, "nulltypes.json"));
        realm.commitTransaction();

        RealmResults<NullTypes> nullTypesRealmResults = realm.where(NullTypes.class).findAll();
        assertEquals(3, nullTypesRealmResults.size());

        NullTypes nullTypes1 = nullTypesRealmResults.where().equalTo("id", 1).findFirst();
        checkNullableValuesAreNull(nullTypes1);

        NullTypes nullTypes2 = nullTypesRealmResults.where().equalTo("id", 2).findFirst();
        checkNullableValuesAreNotNull(nullTypes2);
    }

    /**
     * Tests a nullable field already has a non-null value, update it through JSON with null value
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

        RealmResults<NullTypes> nullTypesRealmResults = realm.where(NullTypes.class).findAll();
        assertEquals(2, nullTypesRealmResults.size());
        checkNullableValuesAreNotNull(nullTypesRealmResults.where().equalTo("id", 1).findFirst());

        // Updates object with id 1, nullable fields should have null values.
        JSONArray array = new JSONArray(json);
        realm.beginTransaction();
        realm.createOrUpdateAllFromJson(NullTypes.class, array);
        realm.commitTransaction();

        nullTypesRealmResults = realm.where(NullTypes.class).findAll();
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
        } catch (IllegalArgumentException ignored) {
            assertTrue(ignored.getMessage().contains(NullTypes.FIELD_STRING_NOT_NULL));
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }

        // 2 Bytes
        try {
            realm.createObjectFromJson(NullTypes.class, array.getJSONObject(1));
            fail();
        } catch (IllegalArgumentException ignored) {
            assertTrue(ignored.getMessage().contains(NullTypes.FIELD_BYTES_NOT_NULL));
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }

        // 3 Boolean
        try {
            realm.createObjectFromJson(NullTypes.class, array.getJSONObject(2));
            fail();
        } catch (IllegalArgumentException ignored) {
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }

        // 4 Byte
        try {
            realm.createObjectFromJson(NullTypes.class, array.getJSONObject(3));
            fail();
        } catch (IllegalArgumentException ignored) {
            assertTrue(ignored.getMessage().contains(NullTypes.FIELD_BYTE_NOT_NULL));
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }

        // 5 Short
        try {
            realm.createObjectFromJson(NullTypes.class, array.getJSONObject(4));
            fail();
        } catch (IllegalArgumentException ignored) {
            assertTrue(ignored.getMessage().contains(NullTypes.FIELD_SHORT_NOT_NULL));
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }

        // 6 Integer
        try {
            realm.createObjectFromJson(NullTypes.class, array.getJSONObject(5));
            fail();
        } catch (IllegalArgumentException ignored) {
            assertTrue(ignored.getMessage().contains(NullTypes.FIELD_INTEGER_NOT_NULL));
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }

        // 7 Long
        try {
            realm.createObjectFromJson(NullTypes.class, array.getJSONObject(6));
            fail();
        } catch (IllegalArgumentException ignored) {
            assertTrue(ignored.getMessage().contains(NullTypes.FIELD_LONG_NOT_NULL));
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }

        // 8 Float
        try {
            realm.createObjectFromJson(NullTypes.class, array.getJSONObject(7));
            fail();
        } catch (IllegalArgumentException ignored) {
            assertTrue(ignored.getMessage().contains(NullTypes.FIELD_FLOAT_NOT_NULL));
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }

        // 9 Double
        try {
            realm.createObjectFromJson(NullTypes.class, array.getJSONObject(8));
            fail();
        } catch (IllegalArgumentException ignored) {
            assertTrue(ignored.getMessage().contains(NullTypes.FIELD_DOUBLE_NOT_NULL));
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }

        // 10 Date
        try {
            realm.createObjectFromJson(NullTypes.class, array.getJSONObject(9));
            fail();
        } catch (IllegalArgumentException ignored) {
            assertTrue(ignored.getMessage().contains(NullTypes.FIELD_DATE_NOT_NULL));
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }

        realm.cancelTransaction();
    }

    /**
     * If JSON has a field with value null, and corresponding object's field is not nullable,
     * an exception should be throw. Stream version.
     */
    @Test
    public void createObjectFromJson_nullTypesJSONStreamToNotNullFields() throws IOException, JSONException {
        assumeThat(Build.VERSION.SDK_INT, greaterThanOrEqualTo(Build.VERSION_CODES.HONEYCOMB));

        String json = TestHelper.streamToString(TestHelper.loadJsonFromAssets(context, "nulltypes_invalid.json"));
        JSONArray array = new JSONArray(json);

        // 1 String
        try {
            realm.beginTransaction();
            realm.createObjectFromJson(NoPrimaryKeyNullTypes.class, convertJsonObjectToStream(array.getJSONObject(0)));
            fail();
        } catch (IllegalArgumentException ignored) {
            assertTrue(ignored.getMessage().contains(NullTypes.FIELD_STRING_NOT_NULL));
        } finally {
            realm.cancelTransaction();
        }
        // 2 Bytes
        try {
            realm.beginTransaction();
            realm.createObjectFromJson(NoPrimaryKeyNullTypes.class, convertJsonObjectToStream(array.getJSONObject(1)));
            fail();
        } catch (IllegalArgumentException ignored) {
            assertTrue(ignored.getMessage().contains(NullTypes.FIELD_BYTES_NOT_NULL));
        } finally {
            realm.cancelTransaction();
        }
        // 3 Boolean
        try {
            realm.beginTransaction();
            realm.createObjectFromJson(NoPrimaryKeyNullTypes.class, convertJsonObjectToStream(array.getJSONObject(2)));
            fail();
        } catch (IllegalArgumentException ignored) {
            assertTrue(ignored.getMessage().contains(NullTypes.FIELD_BOOLEAN_NOT_NULL));
        } finally {
            realm.cancelTransaction();
        }
        // 4 Byte
        try {
            realm.beginTransaction();
            realm.createObjectFromJson(NoPrimaryKeyNullTypes.class, convertJsonObjectToStream(array.getJSONObject(3)));
            fail();
        } catch (IllegalArgumentException ignored) {
            assertTrue(ignored.getMessage().contains(NullTypes.FIELD_BYTE_NOT_NULL));
        } finally {
            realm.cancelTransaction();
        }
        // 5 Short
        try {
            realm.beginTransaction();
            realm.createObjectFromJson(NoPrimaryKeyNullTypes.class, convertJsonObjectToStream(array.getJSONObject(4)));
            fail();
        } catch (IllegalArgumentException ignored) {
            assertTrue(ignored.getMessage().contains(NullTypes.FIELD_SHORT_NOT_NULL));
        } finally {
            realm.cancelTransaction();
        }
        // 6 Integer
        try {
            realm.beginTransaction();
            realm.createObjectFromJson(NoPrimaryKeyNullTypes.class, convertJsonObjectToStream(array.getJSONObject(5)));
            fail();
        } catch (IllegalArgumentException ignored) {
            assertTrue(ignored.getMessage().contains(NullTypes.FIELD_INTEGER_NOT_NULL));
        } finally {
            realm.cancelTransaction();
        }
        // 7 Long
        try {
            realm.beginTransaction();
            realm.createObjectFromJson(NoPrimaryKeyNullTypes.class, convertJsonObjectToStream(array.getJSONObject(6)));
            fail();
        } catch (IllegalArgumentException ignored) {
            assertTrue(ignored.getMessage().contains(NullTypes.FIELD_LONG_NOT_NULL));
        } finally {
            realm.cancelTransaction();
        }
        // 8 Float
        try {
            realm.beginTransaction();
            realm.createObjectFromJson(NoPrimaryKeyNullTypes.class, convertJsonObjectToStream(array.getJSONObject(7)));
            fail();
        } catch (IllegalArgumentException ignored) {
            assertTrue(ignored.getMessage().contains(NullTypes.FIELD_FLOAT_NOT_NULL));
        } finally {
            realm.cancelTransaction();
        }
        // 9 Double
        try {
            realm.beginTransaction();
            realm.createObjectFromJson(NoPrimaryKeyNullTypes.class, convertJsonObjectToStream(array.getJSONObject(8)));
            fail();
        } catch (IllegalArgumentException ignored) {
            assertTrue(ignored.getMessage().contains(NullTypes.FIELD_DOUBLE_NOT_NULL));
        } finally {
            realm.cancelTransaction();
        }
        // 10 Date
        try {
            realm.beginTransaction();
            realm.createObjectFromJson(NoPrimaryKeyNullTypes.class, convertJsonObjectToStream(array.getJSONObject(9)));
            fail();
        } catch (IllegalArgumentException ignored) {
            assertTrue(ignored.getMessage().contains(NullTypes.FIELD_DATE_NOT_NULL));
        } finally {
            realm.cancelTransaction();
        }
    }

    /**
     * Checks that using createOrUpdateObject will set the primary key directly instead of first setting
     * it to the default value (which can fail).
     */
    @Test
    public void createObjectFromJson_objectWithPrimaryKeySetValueDirectlyFromJsonObject() throws JSONException {
        JSONObject newObject = new JSONObject("{\"id\": 1, \"name\": \"bar\"}");
        realm.beginTransaction();
        realm.createObject(OwnerPrimaryKey.class, 0); // id = 0
        realm.createObjectFromJson(OwnerPrimaryKey.class, newObject);
        realm.commitTransaction();

        RealmResults<OwnerPrimaryKey> owners = realm.where(OwnerPrimaryKey.class).findAll();
        assertEquals(2, owners.size());
        assertEquals(1, owners.get(1).getId());
        assertEquals("bar", owners.get(1).getName());
    }

    @Test
    public void createObjectFromJson_objectNullClass() throws JSONException {
        JSONObject newObject = new JSONObject("{\"id\": 1, \"name\": \"bar\"}");
        realm.beginTransaction();
        assertNull(realm.createObjectFromJson(null, newObject));
        realm.commitTransaction();
    }

    /**
     * createObject using primary keys doesn't work if the Check that using createOrUpdateObject
     * will set the primary key directly instead of first setting it to the default value (which can fail).
     */
    @Test
    public void createObjectFromJson_objectWithPrimaryKeySetValueDirectlyFromStream() throws JSONException, IOException {
        assumeThat(Build.VERSION.SDK_INT, greaterThanOrEqualTo(Build.VERSION_CODES.HONEYCOMB));

        InputStream stream = TestHelper.stringToStream("{\"id\": 1, \"name\": \"bar\"}");
        realm.beginTransaction();
        realm.createObject(OwnerPrimaryKey.class, 0); // id = 0
        realm.createObjectFromJson(OwnerPrimaryKey.class, stream);
        realm.commitTransaction();

        RealmResults<OwnerPrimaryKey> owners = realm.where(OwnerPrimaryKey.class).findAll();
        assertEquals(2, owners.size());
        assertEquals(1, owners.get(1).getId());
        assertEquals("bar", owners.get(1).getName());
    }

    private void testPrimitiveListWithValues(String fieldName, Object[] values) throws JSONException, IOException {
        testPrimitiveListWithValues(fieldName, values, values);
    }

    private void testPrimitiveListWithValues(String fieldName, @Nullable Object[] valuesToSave, Object[] valuesToLoad)
            throws JSONException, IOException {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = valuesToSave != null ? new JSONArray(valuesToSave) : null;
        jsonObject.put(fieldName, jsonArray);

        // Test from JSONObject
        realm.beginTransaction();
        PrimitiveListTypes primitiveListTypes = realm.createObjectFromJson(PrimitiveListTypes.class, jsonObject);
        realm.commitTransaction();
        assertNotNull(primitiveListTypes);
        assertArrayEquals(valuesToLoad, primitiveListTypes.getList(fieldName).toArray());

        // Test from JSONStream
        realm.beginTransaction();
        primitiveListTypes = realm.createObjectFromJson(PrimitiveListTypes.class, convertJsonObjectToStream(jsonObject));
        realm.commitTransaction();
        assertNotNull(primitiveListTypes);
        assertArrayEquals(valuesToLoad, primitiveListTypes.getList(fieldName).toArray());
    }

    @Test
    public void createObjectFromJson_primitiveList_mixedValues() throws JSONException, IOException {
        testPrimitiveListWithValues(PrimitiveListTypes.FIELD_STRING_LIST, new String[] {"a", null, "bc"});
        testPrimitiveListWithValues(PrimitiveListTypes.FIELD_BOOLEAN_LIST, new Boolean[] {true, null, false});
        testPrimitiveListWithValues(PrimitiveListTypes.FIELD_DOUBLE_LIST, new Double[] {1.0d, null, 2.0d});
        testPrimitiveListWithValues(PrimitiveListTypes.FIELD_FLOAT_LIST, new Float[] {1.0f, null, 2.0f});
        testPrimitiveListWithValues(PrimitiveListTypes.FIELD_BYTE_LIST, new Byte[] {1, null, 2});
        testPrimitiveListWithValues(PrimitiveListTypes.FIELD_SHORT_LIST, new Short[] {1, null, 2});
        testPrimitiveListWithValues(PrimitiveListTypes.FIELD_INT_LIST, new Integer[] {1, null, 2});
        testPrimitiveListWithValues(PrimitiveListTypes.FIELD_LONG_LIST, new Long[] {1L, null, 2L});

        // Date as integer
        testPrimitiveListWithValues(PrimitiveListTypes.FIELD_DATE_LIST,
                new Integer[] {0, null, 1},
                new Date[] {new Date(0), null, new Date(1)});
        // Date as String
        testPrimitiveListWithValues(PrimitiveListTypes.FIELD_DATE_LIST,
                new String [] {"/Date(1000)/", null, "/Date(2000)/"},
                new Date[] {new Date(1000), null, new Date(2000)});
        // Date as String timezone
        // Oct 03 2015 14:45.33
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("Australia/West"));
        cal.set(2015, Calendar.OCTOBER, 3, 14, 45, 33);
        cal.set(Calendar.MILLISECOND, 376);
        testPrimitiveListWithValues(PrimitiveListTypes.FIELD_DATE_LIST,
                new String [] {"/Date(1443854733376+0800)/", null},
                new Date[] {cal.getTime(), null});


        testPrimitiveListWithValues(PrimitiveListTypes.FIELD_BINARY_LIST,
                new String[] {new String(Base64.encode(new byte[] {1, 2, 3}, Base64.DEFAULT), UTF_8),
                        null, new String(Base64.encode(new byte[] {4, 5, 6}, Base64.DEFAULT), UTF_8)},
                new byte[][] {new byte[]{1, 2, 3}, null, new byte[]{4, 5, 6}});
    }

    // Null list will be saved as empty list since We don't support nullable RealmList
    @Test
    public void createObjectFromJson_primitiveList_nullList() throws IOException, JSONException {
        testPrimitiveListWithValues(PrimitiveListTypes.FIELD_STRING_LIST, null, new String[0]);
        testPrimitiveListWithValues(PrimitiveListTypes.FIELD_BOOLEAN_LIST, null, new Boolean[0]);
        testPrimitiveListWithValues(PrimitiveListTypes.FIELD_DOUBLE_LIST, null, new Double[0]);
        testPrimitiveListWithValues(PrimitiveListTypes.FIELD_FLOAT_LIST, null, new Float[0]);
        testPrimitiveListWithValues(PrimitiveListTypes.FIELD_BYTE_LIST, null, new Byte[0]);
        testPrimitiveListWithValues(PrimitiveListTypes.FIELD_SHORT_LIST, null, new Short[0]);
        testPrimitiveListWithValues(PrimitiveListTypes.FIELD_INT_LIST, null, new Integer[0]);
        testPrimitiveListWithValues(PrimitiveListTypes.FIELD_LONG_LIST, null, new Long[0]);
        testPrimitiveListWithValues(PrimitiveListTypes.FIELD_DATE_LIST, null, new Date[0]);
        testPrimitiveListWithValues(PrimitiveListTypes.FIELD_BYTE_LIST, null, new byte[0][]);
    }

    private void testRequiredPrimitiveListWithNullValue(String fieldName) throws JSONException, IOException {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray =new JSONArray();
        jsonArray.put(null);
        jsonObject.put(fieldName, jsonArray);

        // Test from JSONObject
        realm.beginTransaction();
        try {
            realm.createObjectFromJson(PrimitiveListTypes.class, jsonObject);
            fail();
        } catch (IllegalArgumentException ignored) {
        } finally {
            realm.cancelTransaction();
        }

        // Test from JSONStream
        realm.beginTransaction();
        try {
            realm.createObjectFromJson(PrimitiveListTypes.class, convertJsonObjectToStream(jsonObject));
            fail();
        } catch (IllegalArgumentException ignored) {
        } finally {
            realm.cancelTransaction();
        }
    }

    @Test
    public void createObjectFromJson_primitiveList_nullValueForRequiredField() throws IOException, JSONException {
        testRequiredPrimitiveListWithNullValue(PrimitiveListTypes.FIELD_REQUIRED_STRING_LIST);
        testRequiredPrimitiveListWithNullValue(PrimitiveListTypes.FIELD_REQUIRED_BOOLEAN_LIST);
        testRequiredPrimitiveListWithNullValue(PrimitiveListTypes.FIELD_REQUIRED_DOUBLE_LIST);
        testRequiredPrimitiveListWithNullValue(PrimitiveListTypes.FIELD_REQUIRED_FLOAT_LIST);
        testRequiredPrimitiveListWithNullValue(PrimitiveListTypes.FIELD_REQUIRED_BYTE_LIST);
        testRequiredPrimitiveListWithNullValue(PrimitiveListTypes.FIELD_REQUIRED_SHORT_LIST);
        testRequiredPrimitiveListWithNullValue(PrimitiveListTypes.FIELD_REQUIRED_INT_LIST);
        testRequiredPrimitiveListWithNullValue(PrimitiveListTypes.FIELD_REQUIRED_LONG_LIST);
        testRequiredPrimitiveListWithNullValue(PrimitiveListTypes.FIELD_REQUIRED_DATE_LIST);
        testRequiredPrimitiveListWithNullValue(PrimitiveListTypes.FIELD_REQUIRED_BYTE_LIST);
    }
}
