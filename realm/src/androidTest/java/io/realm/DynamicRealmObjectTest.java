/*
 * Copyright 2015 Realm Inc.
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

import android.test.AndroidTestCase;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import io.realm.entities.AllJavaTypes;
import io.realm.entities.Dog;

import static io.realm.internal.test.ExtraTests.assertArrayEquals;

public class DynamicRealmObjectTest extends AndroidTestCase {

    private Realm realm;
    private AllJavaTypes typedObj;
    private DynamicRealmObject dObj;

    @Override
    protected void setUp() throws Exception {
        RealmConfiguration realmConfig = new RealmConfiguration.Builder(getContext()).build();
        Realm.deleteRealm(realmConfig);
        realm = Realm.getInstance(realmConfig);
        realm.beginTransaction();
        typedObj = realm.createObject(AllJavaTypes.class);
        typedObj.setFieldString("str");
        typedObj.setFieldShort((short) 1);
        typedObj.setFieldInt(1);
        typedObj.setFieldLong(1);
        typedObj.setFieldByte((byte) 4);
        typedObj.setFieldFloat(1.23f);
        typedObj.setFieldDouble(1.234d);
        typedObj.setFieldBinary(new byte[]{1, 2, 3});
        typedObj.setFieldBoolean(true);
        typedObj.setFieldDate(new Date(1000));
        typedObj.setFieldObject(typedObj);
        typedObj.getFieldList().add(typedObj);
        dObj = new DynamicRealmObject(typedObj);
        realm.commitTransaction();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        realm.close();
    }

    public enum SupportedType {
        BOOLEAN, SHORT, INT, LONG, BYTE, FLOAT, DOUBLE, STRING, BINARY, DATE, OBJECT, LIST;
    }

    public void testIllegalInputObjectThrows() {
        try {
            new DynamicRealmObject(null);
        } catch (IllegalArgumentException ignored) {
        }

        try {
            new DynamicRealmObject(dObj);
        } catch (IllegalArgumentException ignored) {
        }

        realm.beginTransaction();
        typedObj.removeFromRealm();
        realm.commitTransaction();
        try {
            new DynamicRealmObject(typedObj);
        } catch (IllegalArgumentException ignored) {
        }
    }

    // Test that all getters fail if given invalid field name
    public void testGetXXXIllegalFieldNameThrows() {

        // Set arguments
        String linkedField = AllJavaTypes.FIELD_OBJECT + "." + AllJavaTypes.FIELD_STRING;
        List<String> arguments = Arrays.asList(null, "foo", AllJavaTypes.FIELD_STRING, linkedField);
        List<String> stringArguments = Arrays.asList(null, "foo", AllJavaTypes.FIELD_BOOLEAN, linkedField);

        // Test all getters
        for (SupportedType type : SupportedType.values()) {
            List<String> args = (type == SupportedType.STRING) ? stringArguments : arguments;
            try {
                callGetter(type, args);
                fail();
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    // Helper method for calling getters with different field names
    private void callGetter(SupportedType type, List<String> fieldNames) {
        for (String fieldName : fieldNames) {
            switch (type) {
                case BOOLEAN: dObj.getBoolean(fieldName); break;
                case SHORT: dObj.getShort(fieldName); break;
                case INT: dObj.getInt(fieldName); break;
                case LONG: dObj.getLong(fieldName); break;
                case BYTE: dObj.getByte(fieldName); break;
                case FLOAT: dObj.getFloat(fieldName); break;
                case DOUBLE: dObj.getDouble(fieldName); break;
                case STRING: dObj.getString(fieldName); break;
                case BINARY: dObj.getBlob(fieldName); break;
                case DATE: dObj.getDate(fieldName); break;
                case OBJECT: dObj.getObject(fieldName); break;
                case LIST: dObj.getList(fieldName); break;
                default:
                    fail();
            }
        }
    }

    // Test that all getters fail if given an invalid field name
    public void testSetXXXIllegalFieldNameThrows() {

        // Set arguments
        String linkedField = AllJavaTypes.FIELD_OBJECT + "." + AllJavaTypes.FIELD_STRING;
        List<String> arguments = Arrays.asList(null, "foo", AllJavaTypes.FIELD_STRING, linkedField);
        List<String> stringArguments = Arrays.asList(null, "foo", AllJavaTypes.FIELD_BOOLEAN, linkedField);

        // Test all getters
        for (SupportedType type : SupportedType.values()) {
            List<String> args = (type == SupportedType.STRING) ? stringArguments : arguments;
            try {
                callSetter(type, args);
                fail();
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    // Helper method for calling setters with different field names
    private void callSetter(SupportedType type, List<String> fieldNames) {
        for (String fieldName : fieldNames) {
            switch (type) {
                case BOOLEAN: dObj.setBoolean(fieldName, false); break;
                case SHORT: dObj.setShort(fieldName, (short) 1); break;
                case INT: dObj.setInt(fieldName, 1); break;
                case LONG: dObj.setLong(fieldName, 1L); break;
                case BYTE: dObj.setByte(fieldName, (byte) 4); break;
                case FLOAT: dObj.setFloat(fieldName, 1.23f); break;
                case DOUBLE: dObj.setDouble(fieldName, 1.23d); break;
                case STRING: dObj.setString(fieldName, "foo"); break;
                case BINARY: dObj.setBlob(fieldName, new byte[]{}); break;
                case DATE: dObj.getDate(fieldName); break;
                case OBJECT: dObj.setObject(fieldName, null); break;
                case LIST: dObj.setList(fieldName, null); break;
                default:
                    fail();
            }
        }
    }

    // Test all typed setters/setters
    public void testTypedGetterSettersXXX() {
        realm.beginTransaction();
        AllJavaTypes obj = realm.createObject(AllJavaTypes.class);
        DynamicRealmObject dObj = new DynamicRealmObject(obj);
        try {
            for (SupportedType type : SupportedType.values()) {
                switch (type) {
                    case BOOLEAN:
                        dObj.setBoolean(AllJavaTypes.FIELD_BOOLEAN, true);
                        assertTrue(dObj.getBoolean(AllJavaTypes.FIELD_BOOLEAN));
                        break;
                    case SHORT:
                        dObj.setShort(AllJavaTypes.FIELD_SHORT, (short) 42);
                        assertEquals(42, dObj.getShort(AllJavaTypes.FIELD_SHORT));
                        break;
                    case INT:
                        dObj.setInt(AllJavaTypes.FIELD_INT, 42);
                        assertEquals(42, dObj.getInt(AllJavaTypes.FIELD_INT));
                        break;
                    case LONG:
                        dObj.setLong(AllJavaTypes.FIELD_LONG, 42L);
                        assertEquals(42, dObj.getLong(AllJavaTypes.FIELD_LONG));
                        break;
                    case BYTE:
                        dObj.setByte(AllJavaTypes.FIELD_BYTE, (byte) 4);
                        assertEquals(4, dObj.getByte(AllJavaTypes.FIELD_BYTE));
                        break;
                    case FLOAT:
                        dObj.setFloat(AllJavaTypes.FIELD_FLOAT, 1.23f);
                        assertEquals(1.23f, dObj.getFloat(AllJavaTypes.FIELD_FLOAT));
                        break;
                    case DOUBLE:
                        dObj.setDouble(AllJavaTypes.FIELD_DOUBLE, 1.234d);
                        assertEquals(1.234d, dObj.getDouble(AllJavaTypes.FIELD_DOUBLE));
                        break;
                    case STRING:
                        dObj.setString(AllJavaTypes.FIELD_STRING, "str");
                        assertEquals("str", dObj.getString(AllJavaTypes.FIELD_STRING));
                        break;
                    case BINARY:
                        dObj.setBlob(AllJavaTypes.FIELD_BINARY, new byte[]{1, 2, 3});
                        assertArrayEquals(new byte[]{1, 2, 3}, dObj.getBlob(AllJavaTypes.FIELD_BINARY));
                        break;
                    case DATE:
                        dObj.setDate(AllJavaTypes.FIELD_DATE, new Date(1000));
                        assertEquals(new Date(1000), dObj.getDate(AllJavaTypes.FIELD_DATE));
                        break;
                    case OBJECT:
                        dObj.setObject(AllJavaTypes.FIELD_OBJECT, dObj);
                        assertEquals(dObj, dObj.getObject(AllJavaTypes.FIELD_OBJECT));
                        break;
                    case LIST:
                    /* ignore, see testGetList/testSetList */
                        break;
                    default:
                        fail();
                }
            }
        } finally {
            realm.cancelTransaction();
        }
    }

    public void testTypedSetXXXWithNull() {
        realm.beginTransaction();
        AllJavaTypes obj = realm.createObject(AllJavaTypes.class);
        DynamicRealmObject dObj = new DynamicRealmObject(obj);
        try {
            for (SupportedType type : SupportedType.values()) {
                switch (type) {
                    case OBJECT:
                        dObj.setObject(AllJavaTypes.FIELD_OBJECT, null);
                        assertNull(dObj.getObject(AllJavaTypes.FIELD_OBJECT));
                        break;
                    case LIST:
                    case BOOLEAN:
                    case SHORT:
                    case INT:
                    case LONG:
                    case FLOAT:
                    case DOUBLE:
                    case STRING:
                    case BINARY:
                    case DATE:
                    default:
                        // Ignore other types for now until null support is merged.
                }
            }
        } finally {
            realm.cancelTransaction();
        }
    }

    public void testSetObjectWrongTypeThrows() {
        realm.beginTransaction();
        AllJavaTypes obj = realm.createObject(AllJavaTypes.class);
        Dog otherObj = realm.createObject(Dog.class);
        DynamicRealmObject dynamicObj = new DynamicRealmObject(obj);
        DynamicRealmObject dynamicWrongType = new DynamicRealmObject(otherObj);
        try {
            dynamicObj.setObject(AllJavaTypes.FIELD_OBJECT, dynamicWrongType);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    // List is not a simple getter, test separately.
    public void testGetList() {
        RealmList<DynamicRealmObject> list = dObj.getList(AllJavaTypes.FIELD_LIST);
        assertEquals(1, list.size());
        assertEquals(dObj, list.get(0));
    }

    public void testGenericGetterSetter() {
        realm.beginTransaction();
        AllJavaTypes obj = realm.createObject(AllJavaTypes.class);
        DynamicRealmObject dObj = new DynamicRealmObject(obj);
        try {
            for (SupportedType type : SupportedType.values()) {
                switch (type) {
                    case BOOLEAN:
                        dObj.set(AllJavaTypes.FIELD_BOOLEAN, true);
                        assertTrue((Boolean) dObj.get(AllJavaTypes.FIELD_BOOLEAN));
                        break;
                    case SHORT:
                        dObj.set(AllJavaTypes.FIELD_SHORT, (short) 42);
                        assertEquals(Long.parseLong("42"), dObj.get(AllJavaTypes.FIELD_SHORT));
                        break;
                    case INT:
                        dObj.set(AllJavaTypes.FIELD_INT, 42);
                        assertEquals(Long.parseLong("42"), dObj.get(AllJavaTypes.FIELD_INT));
                        break;
                    case LONG:
                        dObj.set(AllJavaTypes.FIELD_LONG, 42L);
                        assertEquals(Long.parseLong("42"), dObj.get(AllJavaTypes.FIELD_LONG));
                        break;
                    case BYTE:
                        dObj.set(AllJavaTypes.FIELD_BYTE, (byte) 4);
                        assertEquals(Long.parseLong("4"), dObj.get(AllJavaTypes.FIELD_BYTE));
                        break;
                    case FLOAT:
                        dObj.set(AllJavaTypes.FIELD_FLOAT, 1.23f);
                        assertEquals(Float.parseFloat("1.23"), dObj.get(AllJavaTypes.FIELD_FLOAT));
                        break;
                    case DOUBLE:
                        dObj.set(AllJavaTypes.FIELD_DOUBLE, 1.234d);
                        assertEquals(Double.parseDouble("1.234"), dObj.get(AllJavaTypes.FIELD_DOUBLE));
                        break;
                    case STRING:
                        dObj.set(AllJavaTypes.FIELD_STRING, "str");
                        assertEquals("str", dObj.get(AllJavaTypes.FIELD_STRING));
                        break;
                    case BINARY:
                        dObj.set(AllJavaTypes.FIELD_BINARY, new byte[]{1, 2, 3});
                        assertArrayEquals(new byte[]{1, 2, 3}, (byte[]) dObj.get(AllJavaTypes.FIELD_BINARY));
                        break;
                    case DATE:
                        dObj.set(AllJavaTypes.FIELD_DATE, new Date(1000));
                        assertEquals(new Date(1000), dObj.get(AllJavaTypes.FIELD_DATE));
                        break;
                    case OBJECT:
                        dObj.set(AllJavaTypes.FIELD_OBJECT, dObj);
                        assertEquals(dObj, dObj.get(AllJavaTypes.FIELD_OBJECT));
                        break;
                    case LIST:
                        RealmList<DynamicRealmObject> newList = new RealmList<DynamicRealmObject>();
                        newList.add(dObj);
                        dObj.set(AllJavaTypes.FIELD_LIST, newList);
                        RealmList<DynamicRealmObject> list = dObj.getList(AllJavaTypes.FIELD_LIST);
                        assertEquals(1, list.size());
                        assertEquals(dObj, list.get(0));
                        break;
                    default:
                        fail();
                }
            }
        } finally {
            realm.cancelTransaction();
        }
    }

    public void testUntypedSetWithNull() {
        realm.beginTransaction();
        AllJavaTypes obj = realm.createObject(AllJavaTypes.class);
        DynamicRealmObject dObj = new DynamicRealmObject(obj);
        try {
            for (SupportedType type : SupportedType.values()) {
                switch (type) {
                    case OBJECT:
                        dObj.set(AllJavaTypes.FIELD_OBJECT, null);
                        assertNull(dObj.getObject(AllJavaTypes.FIELD_OBJECT));
                        break;
                    case LIST:
                    case BOOLEAN:
                    case SHORT:
                    case INT:
                    case LONG:
                    case FLOAT:
                    case DOUBLE:
                    case STRING:
                    case BINARY:
                    case DATE:
                    default:
                        // Ignore other types for now until Null support is merged.
                }
            }
        } finally {
            realm.cancelTransaction();
        }
    }

    public void testUntypedSetUsingStringConversion() {
        realm.beginTransaction();
        AllJavaTypes obj = realm.createObject(AllJavaTypes.class);
        DynamicRealmObject dObj = new DynamicRealmObject(obj);
        try {
            for (SupportedType type : SupportedType.values()) {
                switch (type) {
                    case BOOLEAN:
                        dObj.set(AllJavaTypes.FIELD_BOOLEAN, "true");
                        assertTrue(dObj.getBoolean(AllJavaTypes.FIELD_BOOLEAN));
                        break;
                    case SHORT:
                        dObj.set(AllJavaTypes.FIELD_SHORT, "42");
                        assertEquals((short) 42, dObj.getShort(AllJavaTypes.FIELD_SHORT));
                        break;
                    case INT:
                        dObj.set(AllJavaTypes.FIELD_INT, "42");
                        assertEquals(42, dObj.getInt(AllJavaTypes.FIELD_INT));
                        break;
                    case LONG:
                        dObj.set(AllJavaTypes.FIELD_LONG, "42");
                        assertEquals((long) 42, dObj.getLong(AllJavaTypes.FIELD_LONG));
                        break;
                    case FLOAT:
                        dObj.set(AllJavaTypes.FIELD_FLOAT, "1.23");
                        assertEquals(1.23f, dObj.getFloat(AllJavaTypes.FIELD_FLOAT));
                        break;
                    case DOUBLE:
                        dObj.set(AllJavaTypes.FIELD_DOUBLE, "1.234");
                        assertEquals(1.234d, dObj.getDouble(AllJavaTypes.FIELD_DOUBLE));
                        break;
                    case DATE:
                        dObj.set(AllJavaTypes.FIELD_DATE, "1000");
                        assertEquals(new Date(1000), dObj.getDate(AllJavaTypes.FIELD_DATE));

                    // These types don't have a string representation that should be parsed.
                    case OBJECT:
                    case LIST:
                    case STRING:
                    case BINARY:
                    default:
                        // Ignore
                }
            }
        } finally {
            realm.cancelTransaction();
        }
    }

    public void testUntypedSetIllegalImplicitConversionThrows() {
        realm.beginTransaction();
        AllJavaTypes obj = realm.createObject(AllJavaTypes.class);
        DynamicRealmObject dObj = new DynamicRealmObject(obj);
        try {
            for (SupportedType type : SupportedType.values()) {
                try {
                    switch (type) {
                        case SHORT:
                            dObj.set(AllJavaTypes.FIELD_SHORT, "foo");
                            break;
                        case INT:
                            dObj.set(AllJavaTypes.FIELD_INT, "foo");
                            break;
                        case LONG:
                            dObj.set(AllJavaTypes.FIELD_LONG, "foo");
                            break;
                        case FLOAT:
                            dObj.set(AllJavaTypes.FIELD_FLOAT, "foo");
                            break;
                        case DOUBLE:
                            dObj.set(AllJavaTypes.FIELD_DOUBLE, "foo");
                            break;
                        case DATE:
                            dObj.set(AllJavaTypes.FIELD_DATE, "foo");
                            break;

                        // These types doesn't have a string representation that should be parsed.
                        case BOOLEAN: // Boolean is special as it returns false for all strings != "true"
                        case OBJECT:
                        case LIST:
                        case STRING:
                        case BINARY:
                        default:
                            continue;
                    }
                    fail(type + " failed");
                } catch (IllegalArgumentException ignored) {
                }
            }
        } finally {
            realm.cancelTransaction();
        }
    }

    public void testIsNullWithNullNotSupportedField() {
        assertFalse(dObj.isNull(AllJavaTypes.FIELD_INT));
    }

    public void testIsNullTrue() {
        realm.beginTransaction();
        AllJavaTypes obj = realm.createObject(AllJavaTypes.class);
        realm.commitTransaction();

        assertTrue(new DynamicRealmObject(obj).isNull(AllJavaTypes.FIELD_OBJECT));
    }

    public void testIsNullFalse() {
        assertFalse(dObj.isNull(AllJavaTypes.FIELD_OBJECT));
    }

    public void testGetFieldNames() {
        String[] expectedKeys = {AllJavaTypes.FIELD_STRING, AllJavaTypes.FIELD_SHORT, AllJavaTypes.FIELD_INT,
                AllJavaTypes.FIELD_LONG, AllJavaTypes.FIELD_BYTE, AllJavaTypes.FIELD_FLOAT, AllJavaTypes.FIELD_DOUBLE,
                AllJavaTypes.FIELD_BOOLEAN, AllJavaTypes.FIELD_DATE, AllJavaTypes.FIELD_BINARY,
                AllJavaTypes.FIELD_OBJECT, AllJavaTypes.FIELD_LIST};
        String[] keys = dObj.getFieldNames();
        assertArrayEquals(expectedKeys, keys);
    }

    public void testHasFieldFalse() {
        assertFalse(dObj.hasField(null));
        assertFalse(dObj.hasField(""));
        assertFalse(dObj.hasField("foo"));
        assertFalse(dObj.hasField("foo.bar"));
    }

    public void testHasFieldTrue() {
        assertTrue(dObj.hasField(AllJavaTypes.FIELD_STRING));
    }

    public void testGetFieldType() {
        assertEquals(RealmFieldType.STRING, dObj.getFieldType(AllJavaTypes.FIELD_STRING));
        assertEquals(RealmFieldType.BINARY, dObj.getFieldType(AllJavaTypes.FIELD_BINARY));
        assertEquals(RealmFieldType.BOOLEAN, dObj.getFieldType(AllJavaTypes.FIELD_BOOLEAN));
        assertEquals(RealmFieldType.DATE, dObj.getFieldType(AllJavaTypes.FIELD_DATE));
        assertEquals(RealmFieldType.DOUBLE, dObj.getFieldType(AllJavaTypes.FIELD_DOUBLE));
        assertEquals(RealmFieldType.FLOAT, dObj.getFieldType(AllJavaTypes.FIELD_FLOAT));
        assertEquals(RealmFieldType.OBJECT, dObj.getFieldType(AllJavaTypes.FIELD_OBJECT));
        assertEquals(RealmFieldType.LIST, dObj.getFieldType(AllJavaTypes.FIELD_LIST));
        assertEquals(RealmFieldType.INTEGER, dObj.getFieldType(AllJavaTypes.FIELD_BYTE));
        assertEquals(RealmFieldType.INTEGER, dObj.getFieldType(AllJavaTypes.FIELD_SHORT));
        assertEquals(RealmFieldType.INTEGER, dObj.getFieldType(AllJavaTypes.FIELD_INT));
        assertEquals(RealmFieldType.INTEGER, dObj.getFieldType(AllJavaTypes.FIELD_LONG));
    }

    public void testEquals() {
        AllJavaTypes obj1 = realm.where(AllJavaTypes.class).findFirst();
        AllJavaTypes obj2 = realm.where(AllJavaTypes.class).findFirst();
        DynamicRealmObject dObj1 = new DynamicRealmObject(obj1);
        DynamicRealmObject dObj2 = new DynamicRealmObject(obj2);
        assertTrue(dObj1.equals(dObj2));
    }

    public void testStandardAndDynamicObjectsNotEqual() {
        AllJavaTypes standardObj = realm.where(AllJavaTypes.class).findFirst();
        //noinspection EqualsBetweenInconvertibleTypes
        assertFalse(dObj.equals(standardObj));
    }

    public void testHashcode() {
        AllJavaTypes standardObj = realm.where(AllJavaTypes.class).findFirst();
        DynamicRealmObject dObj1 = new DynamicRealmObject(standardObj);
        assertEquals(standardObj.hashCode(), dObj1.hashCode());
    }

    public void testToString() {
        // Check that toString() doesn't crash. And do simple formatting checks. We cannot compare to a set String as
        // eg. the byte array will be allocated each time it is accessed.
        String str = dObj.toString();
        assertTrue(str.startsWith("class_AllJavaTypes = ["));
        assertTrue(str.endsWith("}]"));
    }
}
