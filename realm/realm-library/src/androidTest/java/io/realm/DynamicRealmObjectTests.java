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

import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import io.realm.entities.AllJavaTypes;
import io.realm.entities.AllTypes;
import io.realm.entities.CyclicType;
import io.realm.entities.Dog;
import io.realm.entities.NullTypes;
import io.realm.exceptions.RealmException;
import io.realm.entities.Owner;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class DynamicRealmObjectTests {
    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private Realm realm;
    private AllJavaTypes typedObj;
    private DynamicRealmObject dObj;

    @Before
    public void setUp() {
        RealmConfiguration realmConfig = configFactory.createConfiguration();
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

    @After
    public void tearDown() {
        if (realm != null) {
            realm.close();
        }
    }

    // Types supported by the DynamicRealmObject.
    private enum SupportedType {
        BOOLEAN, SHORT, INT, LONG, BYTE, FLOAT, DOUBLE, STRING, BINARY, DATE, OBJECT, LIST
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructor_nullThrows () {
        new DynamicRealmObject((RealmObject)null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructor_dynamicObjectThrows () {
        new DynamicRealmObject(dObj);
    }

    @Test
    public void constructor_deletedObjectThrows() {
        realm.beginTransaction();
        typedObj.removeFromRealm();
        realm.commitTransaction();
        thrown.expect(IllegalArgumentException.class);
        new DynamicRealmObject(typedObj);
    }

    // Test that all getters fail if given invalid field name
    @Test
    public void typedGetter_illegalFieldNameThrows() {
        // Set arguments
        String linkedField = AllJavaTypes.FIELD_OBJECT + "." + AllJavaTypes.FIELD_STRING;
        List<String> arguments = Arrays.asList(null, "foo", AllJavaTypes.FIELD_STRING, linkedField);
        List<String> stringArguments = Arrays.asList(null, "foo", AllJavaTypes.FIELD_BOOLEAN, linkedField);

        // Test all getters
        for (SupportedType type : SupportedType.values()) {

            // We cannot modularize everything, so STRING is a special case with its own set
            // of failing values. Only difference is the wrong type column has to be different.
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
    @Test
    public void typedSetter_illegalFieldNameThrows() {

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
    @Test
    public void typedGettersAndSetters() {
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
                        assertEquals(1.23f, dObj.getFloat(AllJavaTypes.FIELD_FLOAT), 0f);
                        break;
                    case DOUBLE:
                        dObj.setDouble(AllJavaTypes.FIELD_DOUBLE, 1.234d);
                        assertEquals(1.234d, dObj.getDouble(AllJavaTypes.FIELD_DOUBLE), 0d);
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
                        // ignore, see testGetList/testSetList
                        break;
                    default:
                        fail();
                }
            }
        } finally {
            realm.cancelTransaction();
        }
    }

    @Test
    public void setter_null() {
        realm.beginTransaction();
        NullTypes obj = realm.createObject(NullTypes.class);
        DynamicRealmObject dObj = new DynamicRealmObject(obj);
        try {
            for (SupportedType type : SupportedType.values()) {
                switch (type) {
                    case OBJECT:
                        NullTypes childObj = new NullTypes();
                        childObj.setId(1);
                        DynamicRealmObject dynamicChildObject = new DynamicRealmObject(realm.copyToRealm(childObj));
                        dObj.setObject(NullTypes.FIELD_OBJECT_NULL, dynamicChildObject);
                        assertNotNull(dObj.getObject(NullTypes.FIELD_OBJECT_NULL));
                        dObj.setNull(NullTypes.FIELD_OBJECT_NULL);
                        assertNull(dObj.getObject(NullTypes.FIELD_OBJECT_NULL));
                        break;
                    case LIST:
                        try {
                            dObj.setNull(NullTypes.FIELD_LIST_NULL);
                            fail();
                        } catch (IllegalArgumentException ignored) {
                        }
                        break;
                    case BOOLEAN:
                        dObj.setNull(NullTypes.FIELD_BOOLEAN_NULL);
                        assertTrue(dObj.isNull(NullTypes.FIELD_BOOLEAN_NULL));
                        break;
                    case BYTE:
                        dObj.setNull(NullTypes.FIELD_BYTE_NULL);
                        assertTrue(dObj.isNull(NullTypes.FIELD_BYTE_NULL));
                        break;
                    case SHORT:
                        dObj.setNull(NullTypes.FIELD_SHORT_NULL);
                        assertTrue(dObj.isNull(NullTypes.FIELD_SHORT_NULL));
                        break;
                    case INT:
                        dObj.setNull(NullTypes.FIELD_INTEGER_NULL);
                        assertTrue(dObj.isNull(NullTypes.FIELD_INTEGER_NULL));
                        break;
                    case LONG:
                        dObj.setNull(NullTypes.FIELD_LONG_NULL);
                        assertTrue(dObj.isNull(NullTypes.FIELD_LONG_NULL));
                        break;
                    case FLOAT:
                        dObj.setNull(NullTypes.FIELD_FLOAT_NULL);
                        assertTrue(dObj.isNull(NullTypes.FIELD_FLOAT_NULL));
                        break;
                    case DOUBLE:
                        dObj.setNull(NullTypes.FIELD_DOUBLE_NULL);
                        assertTrue(dObj.isNull(NullTypes.FIELD_DOUBLE_NULL));
                        break;
                    case STRING:
                        dObj.setNull(NullTypes.FIELD_STRING_NULL);
                        assertTrue(dObj.isNull(NullTypes.FIELD_STRING_NULL));
                        break;
                    case BINARY:
                        dObj.setNull(NullTypes.FIELD_BYTES_NULL);
                        assertTrue(dObj.isNull(NullTypes.FIELD_BYTES_NULL));
                        break;
                    case DATE:
                        dObj.setNull(NullTypes.FIELD_DATE_NULL);
                        assertTrue(dObj.isNull(NullTypes.FIELD_DATE_NULL));
                        break;
                    default:
                        fail("Unknown type: " + type);
                }
            }
        } finally {
            realm.cancelTransaction();
        }
    }

    @Test
    public void setter_nullOnRequiredFieldsThrows() {
        realm.beginTransaction();
        NullTypes obj = realm.createObject(NullTypes.class);
        DynamicRealmObject dObj = new DynamicRealmObject(obj);
        try {
            for (SupportedType type : SupportedType.values()) {
                try {
                    switch (type) {
                        case OBJECT: continue; // Ignore
                        case LIST: dObj.setNull(NullTypes.FIELD_LIST_NULL); break;
                        case BOOLEAN: dObj.setNull(NullTypes.FIELD_BOOLEAN_NOT_NULL); break;
                        case BYTE: dObj.setNull(NullTypes.FIELD_BYTE_NOT_NULL); break;
                        case SHORT: dObj.setNull(NullTypes.FIELD_SHORT_NOT_NULL); break;
                        case INT: dObj.setNull(NullTypes.FIELD_INTEGER_NOT_NULL); break;
                        case LONG: dObj.setNull(NullTypes.FIELD_LONG_NOT_NULL); break;
                        case FLOAT: dObj.setNull(NullTypes.FIELD_FLOAT_NOT_NULL); break;
                        case DOUBLE: dObj.setNull(NullTypes.FIELD_DOUBLE_NOT_NULL); break;
                        case STRING: dObj.setNull(NullTypes.FIELD_STRING_NOT_NULL); break;
                        case BINARY: dObj.setNull(NullTypes.FIELD_BYTES_NOT_NULL); break;
                        case DATE: dObj.setNull(NullTypes.FIELD_DATE_NOT_NULL); break;
                        default:
                            fail("Unknown type: " + type);
                    }
                    fail("Setting value to null should throw: " + type);
                } catch (IllegalArgumentException ignored) {
                }
            }
        } finally {
            realm.cancelTransaction();
        }
    }

    // Test types where you can set null using the typed setter instead of using setNull().
    @Test
    public void typedSetter_null() {
        realm.beginTransaction();
        NullTypes obj = realm.createObject(NullTypes.class);
        DynamicRealmObject dObj = new DynamicRealmObject(obj);
        try {
            for (SupportedType type : SupportedType.values()) {
                switch (type) {
                    case OBJECT:
                        dObj.setObject(NullTypes.FIELD_OBJECT_NULL, null);
                        assertNull(dObj.getObject(NullTypes.FIELD_OBJECT_NULL));
                        break;
                    case LIST:
                        try {
                            dObj.setList(NullTypes.FIELD_LIST_NULL, null);
                            fail();
                        } catch (IllegalArgumentException ignored) {
                        }
                        break;
                    case DATE:
                        dObj.setDate(NullTypes.FIELD_DATE_NULL, null);
                        assertNull(dObj.getDate(NullTypes.FIELD_DATE_NULL));
                        break;
                    case STRING:
                        dObj.setString(NullTypes.FIELD_STRING_NULL, null);
                        assertNull(dObj.getString(NullTypes.FIELD_STRING_NULL));
                        break;
                    case BINARY:
                        dObj.setBlob(NullTypes.FIELD_BYTES_NULL, null);
                        assertNull(dObj.getBlob(NullTypes.FIELD_BYTES_NULL));
                        break;
                    case BOOLEAN:
                    case SHORT:
                    case INT:
                    case LONG:
                    case FLOAT:
                    case DOUBLE:
                    default:
                        // The typed setters for these cannot accept null as input.
                }
            }
        } finally {
            realm.cancelTransaction();
        }
    }

    @Test
    public void setObject_differentType() {
        realm.beginTransaction();
        DynamicRealmObject dog = new DynamicRealmObject(realm.createObject(Dog.class));
        DynamicRealmObject owner = new DynamicRealmObject(realm.createObject(Owner.class));
        owner.setString("name", "John");
        dog.setObject("owner", owner);
        realm.commitTransaction();

        owner = dog.getObject("owner");
        assertNotNull(owner);
        assertEquals("John", owner.getString("name"));
    }

    @Test
    public void setObject_wrongTypeThrows() {
        realm.beginTransaction();
        AllJavaTypes obj = realm.createObject(AllJavaTypes.class);
        Dog otherObj = realm.createObject(Dog.class);
        DynamicRealmObject dynamicObj = new DynamicRealmObject(obj);
        DynamicRealmObject dynamicWrongType = new DynamicRealmObject(otherObj);
        thrown.expect(IllegalArgumentException.class);
        dynamicObj.setObject(AllJavaTypes.FIELD_OBJECT, dynamicWrongType);
    }

    @Test
    public void setList_wrongTypeThrows() {
        realm.beginTransaction();
        AllTypes wrongObj = realm.createObject(AllTypes.class);
        DynamicRealmObject wrongDynamicObject = new DynamicRealmObject(wrongObj);
        RealmList<DynamicRealmObject> wrongDynamicList = wrongDynamicObject.getList(AllTypes.FIELD_REALMLIST);
        thrown.expect(IllegalArgumentException.class);
        dObj.setList(AllJavaTypes.FIELD_LIST, wrongDynamicList);
    }

    @Test
    public void untypedSetter_listWrongTypeThrows() {
        realm.beginTransaction();
        AllTypes wrongObj = realm.createObject(AllTypes.class);
        thrown.expect(IllegalArgumentException.class);
        dObj.set(AllJavaTypes.FIELD_LIST, wrongObj.getColumnRealmList());
    }

    @Test
    public void untypedSetter_listMixedTypesThrows() {
        realm.beginTransaction();
        AllJavaTypes obj1 = realm.createObject(AllJavaTypes.class);
        obj1.setFieldLong(2);
        CyclicType obj2 = realm.createObject(CyclicType.class);

        RealmList<DynamicRealmObject> list = new RealmList<DynamicRealmObject>();
        list.add(new DynamicRealmObject(obj1));
        list.add(new DynamicRealmObject(obj2));
        thrown.expect(IllegalArgumentException.class);
        dObj.set(AllJavaTypes.FIELD_LIST, list);
    }

    // List is not a simple getter, test separately.
    @Test
    public void getList() {
        RealmList<DynamicRealmObject> list = dObj.getList(AllJavaTypes.FIELD_LIST);
        assertEquals(1, list.size());
        assertEquals(dObj, list.get(0));
    }

    @Test
    public void untypedGetterSetter() {
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

    @Test
    public void untypedSetter_usingStringConversion() {
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
                        assertEquals(1.23f, dObj.getFloat(AllJavaTypes.FIELD_FLOAT), 0f);
                        break;
                    case DOUBLE:
                        dObj.set(AllJavaTypes.FIELD_DOUBLE, "1.234");
                        assertEquals(1.234d, dObj.getDouble(AllJavaTypes.FIELD_DOUBLE), 0f);
                        break;
                    case DATE:
                        dObj.set(AllJavaTypes.FIELD_DATE, "1000");
                        assertEquals(new Date(1000), dObj.getDate(AllJavaTypes.FIELD_DATE));
                        break;
                    // These types don't have a string representation that can be parsed.
                    case OBJECT:
                    case LIST:
                    case STRING:
                    case BINARY:
                    case BYTE:
                        break;

                    default:
                        fail("Unknown type: " + type);
                        break;
                }
            }
        } finally {
            realm.cancelTransaction();
        }
    }

    @Test
    public void untypedSetter_illegalImplicitConversionThrows() {
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

                        // These types don't have a string representation that can be parsed.
                        case BOOLEAN: // Boolean is special as it returns false for all strings != "true"
                        case BYTE:
                        case OBJECT:
                        case LIST:
                        case STRING:
                        case BINARY:
                            continue;

                        default:
                            fail("Unknown type: " + type);
                    }
                    fail(type + " failed");
                } catch (IllegalArgumentException ignored) {
                } catch (RealmException e) {
                    if (!(e.getCause() instanceof ParseException)) {
                        // providing "foo" to the date parser will blow up with a RealmException
                        // and the cause will be a ParseException.
                        fail(type + " failed");
                    }
                }
            }
        } finally {
            realm.cancelTransaction();
        }
    }

    @Test
    public void isNull_nullNotSupportedField() {
        assertFalse(dObj.isNull(AllJavaTypes.FIELD_INT));
    }

    @Test
    public void isNull_true() {
        realm.beginTransaction();
        AllJavaTypes obj = realm.createObject(AllJavaTypes.class);
        realm.commitTransaction();

        assertTrue(new DynamicRealmObject(obj).isNull(AllJavaTypes.FIELD_OBJECT));
    }

    @Test
    public void isNull_false() {
        assertFalse(dObj.isNull(AllJavaTypes.FIELD_OBJECT));
    }

    @Test
    public void getFieldNames() {
        String[] expectedKeys = {AllJavaTypes.FIELD_STRING, AllJavaTypes.FIELD_SHORT, AllJavaTypes.FIELD_INT,
                AllJavaTypes.FIELD_LONG, AllJavaTypes.FIELD_BYTE, AllJavaTypes.FIELD_FLOAT, AllJavaTypes.FIELD_DOUBLE,
                AllJavaTypes.FIELD_BOOLEAN, AllJavaTypes.FIELD_DATE, AllJavaTypes.FIELD_BINARY,
                AllJavaTypes.FIELD_OBJECT, AllJavaTypes.FIELD_LIST};
        String[] keys = dObj.getFieldNames();
        assertArrayEquals(expectedKeys, keys);
    }

    @Test
    public void hasField_false() {
        assertFalse(dObj.hasField(null));
        assertFalse(dObj.hasField(""));
        assertFalse(dObj.hasField("foo"));
        assertFalse(dObj.hasField("foo.bar"));
        assertFalse(dObj.hasField(TestHelper.getRandomString(65)));
    }

    @Test
    public void hasField_true() {
        assertTrue(dObj.hasField(AllJavaTypes.FIELD_STRING));
    }

    @Test
    public void getFieldType() {
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

    @Test
    public void equals() {
        AllJavaTypes obj1 = realm.where(AllJavaTypes.class).findFirst();
        AllJavaTypes obj2 = realm.where(AllJavaTypes.class).findFirst();
        DynamicRealmObject dObj1 = new DynamicRealmObject(obj1);
        DynamicRealmObject dObj2 = new DynamicRealmObject(obj2);
        assertTrue(dObj1.equals(dObj2));
    }

    @Test
    public void equals_standardAndDynamicObjectsNotEqual() {
        AllJavaTypes standardObj = realm.where(AllJavaTypes.class).findFirst();
        //noinspection EqualsBetweenInconvertibleTypes
        assertFalse(dObj.equals(standardObj));
    }

    @Test
    public void hashcode() {
        AllJavaTypes standardObj = realm.where(AllJavaTypes.class).findFirst();
        DynamicRealmObject dObj1 = new DynamicRealmObject(standardObj);
        assertEquals(standardObj.hashCode(), dObj1.hashCode());
    }

    @Test
    public void toString_test() {
        // Check that toString() doesn't crash. And do simple formatting checks. We cannot compare to a set String as
        // eg. the byte array will be allocated each time it is accessed.
        String str = dObj.toString();
        assertTrue(str.startsWith("class_AllJavaTypes = ["));
        assertTrue(str.endsWith("}]"));
    }
}
