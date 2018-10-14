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

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import io.realm.entities.AllJavaTypes;
import io.realm.entities.AllTypes;
import io.realm.entities.CyclicType;
import io.realm.entities.Dog;
import io.realm.entities.NullTypes;
import io.realm.entities.Owner;
import io.realm.entities.PrimaryKeyAsBoxedByte;
import io.realm.entities.PrimaryKeyAsBoxedInteger;
import io.realm.entities.PrimaryKeyAsBoxedLong;
import io.realm.entities.PrimaryKeyAsBoxedShort;
import io.realm.entities.PrimaryKeyAsByte;
import io.realm.entities.PrimaryKeyAsInteger;
import io.realm.entities.PrimaryKeyAsLong;
import io.realm.entities.PrimaryKeyAsShort;
import io.realm.entities.PrimaryKeyAsString;
import io.realm.exceptions.RealmException;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class DynamicRealmObjectTests {
    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private Realm realm;
    private DynamicRealm dynamicRealm;
    private AllJavaTypes typedObj;
    // DynamicRealmObject constructed from a typed RealmObject
    private DynamicRealmObject dObjTyped;
    // DynamicRealmObject queried from DynamicRealm
    private DynamicRealmObject dObjDynamic;

    @Before
    public void setUp() {
        RealmConfiguration realmConfig = configFactory.createConfiguration();
        realm = Realm.getInstance(realmConfig);
        realm.beginTransaction();
        typedObj = realm.createObject(AllJavaTypes.class, 1);
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
        typedObj.getFieldIntegerList().add(1);
        typedObj.getFieldStringList().add("str");
        typedObj.getFieldBooleanList().add(true);
        typedObj.getFieldFloatList().add(1.23F);
        typedObj.getFieldDoubleList().add(1.234D);
        typedObj.getFieldBinaryList().add(new byte[] {1, 2, 3});
        typedObj.getFieldDateList().add(new Date(1000));
        dObjTyped = new DynamicRealmObject(typedObj);
        realm.commitTransaction();

        dynamicRealm = DynamicRealm.getInstance(realm.getConfiguration());
        dObjDynamic = dynamicRealm.where(AllJavaTypes.CLASS_NAME).findFirst();
    }

    @After
    public void tearDown() {
        if (realm != null) {
            realm.close();
        }
        if (dynamicRealm != null) {
            dynamicRealm.close();
        }
    }

    // Types supported by the DynamicRealmObject.
    private enum SupportedType {
        BOOLEAN, SHORT, INT, LONG, BYTE, FLOAT, DOUBLE, STRING, BINARY, DATE, OBJECT, LIST,
        LIST_INTEGER, LIST_STRING, LIST_BOOLEAN, LIST_FLOAT, LIST_DOUBLE, LIST_BINARY, LIST_DATE
    }

    private enum ThreadConfinedMethods {
        GET_BOOLEAN, GET_BYTE, GET_SHORT, GET_INT, GET_LONG, GET_FLOAT, GET_DOUBLE,
        GET_BLOB, GET_STRING, GET_DATE, GET_OBJECT, GET_LIST, GET_PRIMITIVE_LIST, GET,

        SET_BOOLEAN, SET_BYTE, SET_SHORT, SET_INT, SET_LONG, SET_FLOAT, SET_DOUBLE,
        SET_BLOB, SET_STRING, SET_DATE, SET_OBJECT, SET_LIST, SET_PRIMITIVE_LIST, SET,

        IS_NULL, SET_NULL,

        HAS_FIELD, GET_FIELD_NAMES, GET_TYPE, GET_FIELD_TYPE,

        HASH_CODE, EQUALS, TO_STRING,
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored", "EqualsWithItself", "SelfEquals"})
    private static void callThreadConfinedMethod(DynamicRealmObject obj, ThreadConfinedMethods method) {
        switch (method) {
            case GET_BOOLEAN:           obj.getBoolean(AllJavaTypes.FIELD_BOOLEAN);                 break;
            case GET_BYTE:              obj.getByte(AllJavaTypes.FIELD_BYTE);                       break;
            case GET_SHORT:             obj.getShort(AllJavaTypes.FIELD_SHORT);                     break;
            case GET_INT:               obj.getInt(AllJavaTypes.FIELD_INT);                         break;
            case GET_LONG:              obj.getLong(AllJavaTypes.FIELD_LONG);                       break;
            case GET_FLOAT:             obj.getFloat(AllJavaTypes.FIELD_FLOAT);                     break;
            case GET_DOUBLE:            obj.getDouble(AllJavaTypes.FIELD_DOUBLE);                   break;
            case GET_BLOB:              obj.getBlob(AllJavaTypes.FIELD_BINARY);                     break;
            case GET_STRING:            obj.getString(AllJavaTypes.FIELD_STRING);                   break;
            case GET_DATE:              obj.getDate(AllJavaTypes.FIELD_DATE);                       break;
            case GET_OBJECT:            obj.getObject(AllJavaTypes.FIELD_OBJECT);                   break;
            case GET_LIST:              obj.getList(AllJavaTypes.FIELD_LIST);                       break;
            case GET_PRIMITIVE_LIST:    obj.getList(AllJavaTypes.FIELD_STRING_LIST, String.class);  break;
            case GET:                   obj.get(AllJavaTypes.FIELD_LONG);                           break;

            case SET_BOOLEAN:           obj.setBoolean(AllJavaTypes.FIELD_BOOLEAN, true);                           break;
            case SET_BYTE:              obj.setByte(AllJavaTypes.FIELD_BYTE,       (byte) 1);                       break;
            case SET_SHORT:             obj.setShort(AllJavaTypes.FIELD_SHORT,     (short) 1);                      break;
            case SET_INT:               obj.setInt(AllJavaTypes.FIELD_INT,         1);                              break;
            case SET_LONG:              obj.setLong(AllJavaTypes.FIELD_LONG,       1L);                             break;
            case SET_FLOAT:             obj.setFloat(AllJavaTypes.FIELD_FLOAT,     1F);                             break;
            case SET_DOUBLE:            obj.setDouble(AllJavaTypes.FIELD_DOUBLE,   1D);                             break;
            case SET_BLOB:              obj.setBlob(AllJavaTypes.FIELD_BINARY,     new byte[] {1, 2, 3});           break;
            case SET_STRING:            obj.setString(AllJavaTypes.FIELD_STRING,   "12345");                        break;
            case SET_DATE:              obj.setDate(AllJavaTypes.FIELD_DATE,       new Date(1L));                   break;
            case SET_OBJECT:            obj.setObject(AllJavaTypes.FIELD_OBJECT,   obj);                            break;
            case SET_LIST:              obj.setList(AllJavaTypes.FIELD_LIST,       new RealmList<>(obj));           break;
            case SET_PRIMITIVE_LIST:    obj.setList(AllJavaTypes.FIELD_STRING_LIST,new RealmList<String>("foo"));   break;
            case SET:                   obj.set(AllJavaTypes.FIELD_LONG,           1L);                             break;

            case IS_NULL:     obj.isNull(AllJavaTypes.FIELD_OBJECT);           break;
            case SET_NULL:    obj.setNull(AllJavaTypes.FIELD_OBJECT);          break;

            case HAS_FIELD:       obj.hasField(AllJavaTypes.FIELD_OBJECT);     break;
            case GET_FIELD_NAMES: obj.getFieldNames();                         break;
            case GET_TYPE:        obj.getType();                               break;
            case GET_FIELD_TYPE:  obj.getFieldType(AllJavaTypes.FIELD_OBJECT); break;

            case HASH_CODE:   obj.hashCode();  break;
            case EQUALS:      obj.equals(obj); break;
            case TO_STRING:   obj.toString();  break;

            default:
                throw new AssertionError("missing case for " + method);
        }
    }

    @Test
    public void callThreadConfinedMethodsFromWrongThread() throws Throwable {

        dynamicRealm.beginTransaction();
        dynamicRealm.deleteAll();
        final DynamicRealmObject obj = dynamicRealm.createObject(AllJavaTypes.CLASS_NAME, 100L);
        dynamicRealm.commitTransaction();

        final AtomicReference<Throwable> throwableFromThread = new AtomicReference<Throwable>();
        final CountDownLatch testFinished = new CountDownLatch(1);

        final String expectedMessage;
        //noinspection TryWithIdenticalCatches
        try {
            final Field expectedMessageField = BaseRealm.class.getDeclaredField("INCORRECT_THREAD_MESSAGE");
            expectedMessageField.setAccessible(true);
            expectedMessage = (String) expectedMessageField.get(null);
        } catch (NoSuchFieldException e) {
            throw new AssertionError(e);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }

        final Thread thread = new Thread("callThreadConfinedMethodsFromWrongThread") {
            @Override
            public void run() {
                try {
                    for (ThreadConfinedMethods method : ThreadConfinedMethods.values()) {
                        try {
                            callThreadConfinedMethod(obj, method);
                            fail("IllegalStateException must be thrown.");
                        } catch (IllegalStateException e) {
                            if (expectedMessage.equals(e.getMessage())) {
                                // expected exception
                                continue;
                            }
                            throwableFromThread.set(e);
                            return;
                        }
                    }
                } finally {
                    testFinished.countDown();
                }
            }
        };
        thread.start();

        TestHelper.awaitOrFail(testFinished);
        final Throwable throwable = throwableFromThread.get();
        if (throwable != null) {
            throw throwable;
        }
    }


    @Test (expected = IllegalArgumentException.class)
    public void constructor_nullThrows () {
        new DynamicRealmObject((RealmObject)null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructor_dynamicObjectThrows () {
        new DynamicRealmObject(dObjTyped);
    }

    @Test
    public void constructor_deletedObjectThrows() {
        realm.beginTransaction();
        typedObj.deleteFromRealm();
        realm.commitTransaction();
        thrown.expect(IllegalArgumentException.class);
        new DynamicRealmObject(typedObj);
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructor_unmanagedObjectThrows() {
        new DynamicRealmObject(new AllTypes());
    }

    // Tests that all getters fail if given invalid field name.
    @Test
    public void typedGetter_illegalFieldNameThrows() {
        // Sets arguments.
        String linkedField = AllJavaTypes.FIELD_OBJECT + "." + AllJavaTypes.FIELD_STRING;
        List<String> arguments = Arrays.asList(null, "foo", AllJavaTypes.FIELD_STRING, linkedField);
        List<String> stringArguments = Arrays.asList(null, "foo", AllJavaTypes.FIELD_BOOLEAN, linkedField);

        // Tests all getters.
        for (SupportedType type : SupportedType.values()) {

            // We cannot modularize everything, so STRING is a special case with its own set
            // of failing values. Only difference is the wrong type column has to be different.
            List<String> args = (type == SupportedType.STRING) ? stringArguments : arguments;
            try {
                callGetter(dObjTyped, type, args);
                fail();
            } catch (IllegalArgumentException ignored) {
            }
            try {
                callGetter(dObjDynamic, type, args);
                fail();
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @Test
    public void typedGetter_wrongUnderlyingTypeThrows() {
        for (SupportedType type : SupportedType.values()) {
            try {
                // Makes sure we hit the wrong underlying type for all types.
                if (type == SupportedType.DOUBLE) {
                    callGetter(dObjTyped, type, Arrays.asList(AllJavaTypes.FIELD_STRING));
                } else {
                    callGetter(dObjTyped, type, Arrays.asList(AllJavaTypes.FIELD_DOUBLE));
                }
                fail(type + " failed to throw.");
            } catch (IllegalArgumentException ignored) {
            }
            try {
                // Makes sure we hit the wrong underlying type for all types.
                if (type == SupportedType.DOUBLE) {
                    callGetter(dObjDynamic, type, Arrays.asList(AllJavaTypes.FIELD_STRING));
                } else {
                    callGetter(dObjDynamic, type, Arrays.asList(AllJavaTypes.FIELD_DOUBLE));
                }
                fail(type + " failed to throw.");
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    // Helper method for calling getters with different field names.
    private static void callGetter(DynamicRealmObject target, SupportedType type, List<String> fieldNames) {
        for (String fieldName : fieldNames) {
            switch (type) {
                case BOOLEAN: target.getBoolean(fieldName); break;
                case SHORT: target.getShort(fieldName); break;
                case INT: target.getInt(fieldName); break;
                case LONG: target.getLong(fieldName); break;
                case BYTE: target.getByte(fieldName); break;
                case FLOAT: target.getFloat(fieldName); break;
                case DOUBLE: target.getDouble(fieldName); break;
                case STRING: target.getString(fieldName); break;
                case BINARY: target.getBlob(fieldName); break;
                case DATE: target.getDate(fieldName); break;
                case OBJECT: target.getObject(fieldName); break;
                case LIST:
                case LIST_INTEGER:
                case LIST_STRING:
                case LIST_BOOLEAN:
                case LIST_FLOAT:
                case LIST_DOUBLE:
                case LIST_BINARY:
                case LIST_DATE:
                    target.getList(fieldName);
                    break;
                default:
                    fail();
            }
        }
    }

    // Tests that all getters fail if given an invalid field name.
    @Test
    public void typedSetter_illegalFieldNameThrows() {

        // Sets arguments.
        String linkedField = AllJavaTypes.FIELD_OBJECT + "." + AllJavaTypes.FIELD_STRING;
        List<String> arguments = Arrays.asList(null, "foo", AllJavaTypes.FIELD_STRING, linkedField);
        List<String> stringArguments = Arrays.asList(null, "foo", AllJavaTypes.FIELD_BOOLEAN, linkedField);

        // Tests all getters.
        for (SupportedType type : SupportedType.values()) {
            List<String> args = (type == SupportedType.STRING) ? stringArguments : arguments;
            try {
                callSetter(dObjTyped, type, args);
                fail();
            } catch (IllegalArgumentException ignored) {
            }
            try {
                callSetter(dObjDynamic, type, args);
                fail();
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @Test
    public void typedSetter_wrongUnderlyingTypeThrows() {
        for (SupportedType type : SupportedType.values()) {
            realm.beginTransaction();
            try {
                // Makes sure we hit the wrong underlying type for all types.
                if (type == SupportedType.STRING) {
                    callSetter(dObjTyped, type, Arrays.asList(AllJavaTypes.FIELD_BOOLEAN));
                } else {
                    callSetter(dObjTyped, type, Arrays.asList(AllJavaTypes.FIELD_STRING));
                }
                fail();
            } catch (IllegalArgumentException ignored) {
            } finally {
                realm.cancelTransaction();
            }
            dynamicRealm.beginTransaction();
            try {
                // Makes sure we hit the wrong underlying type for all types.
                if (type == SupportedType.STRING) {
                    callSetter(dObjDynamic, type, Arrays.asList(AllJavaTypes.FIELD_BOOLEAN));
                } else {
                    callSetter(dObjDynamic, type, Arrays.asList(AllJavaTypes.FIELD_STRING));
                }
                fail();
            } catch (IllegalArgumentException ignored) {
            } finally {
                dynamicRealm.cancelTransaction();
            }
        }
    }

    private void callSetterOnPrimaryKey(String className, DynamicRealmObject object) {
        switch (className) {
            case PrimaryKeyAsByte.CLASS_NAME:
                object.setByte(PrimaryKeyAsByte.FIELD_ID, (byte) 42);
                break;
            case PrimaryKeyAsShort.CLASS_NAME:
                object.setShort(PrimaryKeyAsShort.FIELD_ID, (short) 42);
                break;
            case PrimaryKeyAsInteger.CLASS_NAME:
                object.setInt(PrimaryKeyAsInteger.FIELD_ID, 42);
                break;
            case PrimaryKeyAsLong.CLASS_NAME:
                object.setLong(PrimaryKeyAsLong.FIELD_ID, 42);
                break;
            case PrimaryKeyAsString.CLASS_NAME:
                object.setString(PrimaryKeyAsString.FIELD_PRIMARY_KEY, "42");
                break;
            default:
                fail();
        }
    }

    @Test
    public void typedSetter_changePrimaryKeyThrows() {
        final String[] primaryKeyClasses = {PrimaryKeyAsByte.CLASS_NAME, PrimaryKeyAsShort.CLASS_NAME,
                PrimaryKeyAsInteger.CLASS_NAME, PrimaryKeyAsLong.CLASS_NAME, PrimaryKeyAsString.CLASS_NAME};
        for (String pkClass : primaryKeyClasses) {
            dynamicRealm.beginTransaction();
            DynamicRealmObject object;
            if (pkClass.equals(PrimaryKeyAsString.CLASS_NAME)) {
                object = dynamicRealm.createObject(pkClass, "");
            } else {
                object = dynamicRealm.createObject(pkClass, 0);
            }

            try {
                callSetterOnPrimaryKey(pkClass, object);
                fail();
            } catch (IllegalArgumentException ignored) {
            }
            dynamicRealm.cancelTransaction();
        }
    }

    // Helper method for calling setters with different field names.
    private static void callSetter(DynamicRealmObject target, SupportedType type, List<String> fieldNames) {
        for (String fieldName : fieldNames) {
            switch (type) {
                case BOOLEAN: target.setBoolean(fieldName, false); break;
                case SHORT: target.setShort(fieldName, (short) 1); break;
                case INT: target.setInt(fieldName, 1); break;
                case LONG: target.setLong(fieldName, 1L); break;
                case BYTE: target.setByte(fieldName, (byte) 4); break;
                case FLOAT: target.setFloat(fieldName, 1.23f); break;
                case DOUBLE: target.setDouble(fieldName, 1.23d); break;
                case STRING: target.setString(fieldName, "foo"); break;
                case BINARY: target.setBlob(fieldName, new byte[]{}); break;
                case DATE: target.getDate(fieldName); break;
                case OBJECT: target.setObject(fieldName, null); target.setObject(fieldName, target); break;
                case LIST: target.setList(fieldName, new RealmList<DynamicRealmObject>()); break;
                case LIST_INTEGER: target.setList(fieldName, new RealmList<Integer>(1)); break;
                case LIST_STRING: target.setList(fieldName, new RealmList<String>("foo")); break;
                case LIST_BOOLEAN: target.setList(fieldName, new RealmList<Boolean>(true)); break;
                case LIST_FLOAT: target.setList(fieldName, new RealmList<Float>(1.23F)); break;
                case LIST_DOUBLE: target.setList(fieldName, new RealmList<Double>(1.234D)); break;
                case LIST_BINARY: target.setList(fieldName, new RealmList<byte[]>(new byte[]{})); break;
                case LIST_DATE: target.setList(fieldName, new RealmList<Date>(new Date())); break;
                default:
                    fail();
            }
        }
    }

    // Tests all typed setters/setters.
    @Test
    public void typedGettersAndSetters() {
        realm.beginTransaction();
        AllJavaTypes obj = realm.createObject(AllJavaTypes.class, 0);
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
                    case LIST_INTEGER:
                        checkSetGetValueList(dObj, AllJavaTypes.FIELD_INTEGER_LIST, Integer.class, new RealmList<>(null, 1));
                        break;
                    case LIST_STRING:
                        checkSetGetValueList(dObj, AllJavaTypes.FIELD_STRING_LIST, String.class, new RealmList<>(null, "foo"));
                        break;
                    case LIST_BOOLEAN:
                        checkSetGetValueList(dObj, AllJavaTypes.FIELD_BOOLEAN_LIST, Boolean.class, new RealmList<>(null, true));
                        break;
                    case LIST_FLOAT:
                        checkSetGetValueList(dObj, AllJavaTypes.FIELD_FLOAT_LIST, Float.class, new RealmList<>(null, 1.23F));
                        break;
                    case LIST_DOUBLE:
                        checkSetGetValueList(dObj, AllJavaTypes.FIELD_DOUBLE_LIST, Double.class, new RealmList<>(null, 1.234D));
                        break;
                    case LIST_BINARY:
                        checkSetGetValueList(dObj, AllJavaTypes.FIELD_BINARY_LIST, byte[].class, new RealmList<>(null, new byte[] {1, 2, 3}));
                        break;
                    case LIST_DATE:
                        checkSetGetValueList(dObj, AllJavaTypes.FIELD_DATE_LIST, Date.class, new RealmList<>(null, new Date(1000)));
                        break;
                    case LIST:
                        // Ignores. See testGetList/testSetList.
                        break;
                    default:
                        fail();
                }
            }
        } finally {
            realm.cancelTransaction();
        }
    }

    private <E> void checkSetGetValueList(DynamicRealmObject obj, String fieldName, Class<E> primitiveType, RealmList<E> list) {
        obj.set(fieldName, list);
        assertArrayEquals(list.toArray(), obj.getList(fieldName, primitiveType).toArray());
    }

    @Test
    public void setter_null() {
        realm.beginTransaction();
        NullTypes obj = realm.createObject(NullTypes.class, 0);
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
                    case LIST_INTEGER:
                        try {
                            dObj.setNull(NullTypes.FIELD_INTEGER_LIST_NULL);
                            fail();
                        } catch (IllegalArgumentException ignored) {
                        }
                        break;
                    case LIST_STRING:
                        try {
                            dObj.setNull(NullTypes.FIELD_STRING_LIST_NULL);
                            fail();
                        } catch (IllegalArgumentException ignored) {
                        }
                        break;
                    case LIST_BOOLEAN:
                        try {
                            dObj.setNull(NullTypes.FIELD_BOOLEAN_LIST_NULL);
                            fail();
                        } catch (IllegalArgumentException ignored) {
                        }
                        break;
                    case LIST_FLOAT:
                        try {
                            dObj.setNull(NullTypes.FIELD_FLOAT_LIST_NULL);
                            fail();
                        } catch (IllegalArgumentException ignored) {
                        }
                        break;
                    case LIST_DOUBLE:
                        try {
                            dObj.setNull(NullTypes.FIELD_DOUBLE_LIST_NULL);
                            fail();
                        } catch (IllegalArgumentException ignored) {
                        }
                        break;
                    case LIST_BINARY:
                        try {
                            dObj.setNull(NullTypes.FIELD_BINARY_LIST_NULL);
                            fail();
                        } catch (IllegalArgumentException ignored) {
                        }
                        break;
                    case LIST_DATE:
                        try {
                            dObj.setNull(NullTypes.FIELD_DATE_LIST_NULL);
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
        NullTypes obj = realm.createObject(NullTypes.class, 0);
        DynamicRealmObject dObj = new DynamicRealmObject(obj);
        try {
            for (SupportedType type : SupportedType.values()) {
                String fieldName = null;
                try {
                    switch (type) {
                        case OBJECT: continue; // Ignore
                        case LIST: fieldName = NullTypes.FIELD_LIST_NULL; break;
                        case LIST_INTEGER: fieldName = NullTypes.FIELD_INTEGER_LIST_NULL; break;
                        case LIST_STRING: fieldName = NullTypes.FIELD_STRING_LIST_NULL; break;
                        case LIST_BOOLEAN: fieldName = NullTypes.FIELD_BOOLEAN_LIST_NULL; break;
                        case LIST_FLOAT: fieldName = NullTypes.FIELD_FLOAT_LIST_NULL; break;
                        case LIST_DOUBLE: fieldName = NullTypes.FIELD_DATE_LIST_NULL; break;
                        case LIST_BINARY: fieldName = NullTypes.FIELD_BINARY_LIST_NULL; break;
                        case LIST_DATE: fieldName = NullTypes.FIELD_DATE_LIST_NULL; break;
                        case BOOLEAN: fieldName = NullTypes.FIELD_BOOLEAN_NOT_NULL; break;
                        case BYTE: fieldName = NullTypes.FIELD_BYTE_NOT_NULL; break;
                        case SHORT: fieldName = NullTypes.FIELD_SHORT_NOT_NULL; break;
                        case INT: fieldName = NullTypes.FIELD_INTEGER_NOT_NULL; break;
                        case LONG: fieldName = NullTypes.FIELD_LONG_NOT_NULL; break;
                        case FLOAT: fieldName = NullTypes.FIELD_FLOAT_NOT_NULL; break;
                        case DOUBLE: fieldName = NullTypes.FIELD_DOUBLE_NOT_NULL; break;
                        case STRING: fieldName = NullTypes.FIELD_STRING_NOT_NULL; break;
                        case BINARY: fieldName = NullTypes.FIELD_BYTES_NOT_NULL; break;
                        case DATE: fieldName = NullTypes.FIELD_DATE_NOT_NULL; break;
                        default:
                            fail("Unknown type: " + type);
                    }

                    dObj.setNull(fieldName);
                    fail("Setting value to null should throw: " + type);
                } catch (IllegalArgumentException ignored) {
                    assertTrue(ignored.getMessage().contains(fieldName));
                }
            }
        } finally {
            realm.cancelTransaction();
        }
    }

    // Tests types where you can set null using the typed setter instead of using setNull().
    @Test
    public void typedSetter_null() {
        realm.beginTransaction();
        NullTypes obj = realm.createObject(NullTypes.class, 0);
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
                    case LIST_INTEGER:
                        try {
                            dObj.setList(NullTypes.FIELD_INTEGER_LIST_NULL, null);
                            fail();
                        } catch (IllegalArgumentException ignored) {
                        }
                        break;
                    case LIST_STRING:
                        try {
                            dObj.setList(NullTypes.FIELD_STRING_LIST_NULL, null);
                            fail();
                        } catch (IllegalArgumentException ignored) {
                        }
                        break;
                    case LIST_BOOLEAN:
                        try {
                            dObj.setList(NullTypes.FIELD_BOOLEAN_LIST_NULL, null);
                            fail();
                        } catch (IllegalArgumentException ignored) {
                        }
                        break;
                    case LIST_FLOAT:
                        try {
                            dObj.setList(NullTypes.FIELD_FLOAT_LIST_NULL, null);
                            fail();
                        } catch (IllegalArgumentException ignored) {
                        }
                        break;
                    case LIST_DOUBLE:
                        try {
                            dObj.setList(NullTypes.FIELD_DOUBLE_LIST_NULL, null);
                            fail();
                        } catch (IllegalArgumentException ignored) {
                        }
                        break;
                    case LIST_BINARY:
                        try {
                            dObj.setList(NullTypes.FIELD_BINARY_LIST_NULL, null);
                            fail();
                        } catch (IllegalArgumentException ignored) {
                        }
                        break;
                    case LIST_DATE:
                        try {
                            dObj.setList(NullTypes.FIELD_DATE_LIST_NULL, null);
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
    public void setNull_changePrimaryKeyThrows() {
        final String[] primaryKeyClasses = {PrimaryKeyAsBoxedByte.CLASS_NAME, PrimaryKeyAsBoxedShort.CLASS_NAME,
                PrimaryKeyAsBoxedInteger.CLASS_NAME, PrimaryKeyAsBoxedLong.CLASS_NAME, PrimaryKeyAsString.CLASS_NAME};
        for (String pkClass : primaryKeyClasses) {
            dynamicRealm.beginTransaction();
            DynamicRealmObject object;
            boolean isStringPK = pkClass.equals(PrimaryKeyAsString.CLASS_NAME);
            if (isStringPK) {
                object = dynamicRealm.createObject(pkClass, "");
            } else {
                object = dynamicRealm.createObject(pkClass, 0);
            }

            try {
                object.setNull(isStringPK ? PrimaryKeyAsString.FIELD_PRIMARY_KEY : "id");
                fail();
            } catch (IllegalArgumentException ignored) {
            }
            dynamicRealm.cancelTransaction();
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
        AllJavaTypes obj = realm.createObject(AllJavaTypes.class, 0);
        Dog otherObj = realm.createObject(Dog.class);
        DynamicRealmObject dynamicObj = new DynamicRealmObject(obj);
        DynamicRealmObject dynamicWrongType = new DynamicRealmObject(otherObj);
        thrown.expect(IllegalArgumentException.class);
        dynamicObj.setObject(AllJavaTypes.FIELD_OBJECT, dynamicWrongType);
    }

    @Test
    public void setObject_objectBelongToTypedRealmThrows() {
        dynamicRealm.beginTransaction();

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Cannot add an object from another Realm instance.");
        dynamicRealm.where(AllJavaTypes.CLASS_NAME).findFirst().setObject(AllJavaTypes.FIELD_OBJECT, dObjTyped);

        dynamicRealm.cancelTransaction();
    }

    @Test
    public void setObject_objectBelongToDiffThreadRealmThrows() {
        final CountDownLatch finishedLatch = new CountDownLatch(1);

        new Thread(new Runnable() {
            @Override
            public void run() {
                DynamicRealm dynamicRealm = DynamicRealm.getInstance(realm.getConfiguration());
                dynamicRealm.beginTransaction();

                try {
                    // ExpectedException doesn't work in another thread.
                    dynamicRealm.where(AllJavaTypes.CLASS_NAME).findFirst()
                            .setObject(AllJavaTypes.FIELD_OBJECT, dObjDynamic);
                    fail();
                } catch (IllegalArgumentException expected) {
                    assertEquals("Cannot add an object from another Realm instance.", expected.getMessage());
                }

                dynamicRealm.cancelTransaction();
                dynamicRealm.close();
                finishedLatch.countDown();
            }
        }).start();
        TestHelper.awaitOrFail(finishedLatch);
    }

    @Test
    public void setList_listWithDynamicRealmObject() {
        DynamicRealm dynamicRealm = DynamicRealm.getInstance(realm.configuration);
        dynamicRealm.beginTransaction();

        DynamicRealmObject allTypes = dynamicRealm.createObject(AllTypes.CLASS_NAME);
        allTypes.setString(AllTypes.FIELD_STRING, "bender");

        DynamicRealmObject dog = dynamicRealm.createObject(Dog.CLASS_NAME);
        dog.setString(Dog.FIELD_NAME, "nibbler");

        RealmList<DynamicRealmObject> list = new RealmList<DynamicRealmObject>();
        list.add(dog);
        allTypes.setList(AllTypes.FIELD_REALMLIST, list);

        dynamicRealm.commitTransaction();

        allTypes = dynamicRealm.where(AllTypes.CLASS_NAME)
                .equalTo(AllTypes.FIELD_STRING, "bender")
                .findFirst();
        assertEquals("nibbler", allTypes.getList(AllTypes.FIELD_REALMLIST).first().get(Dog.FIELD_NAME));
        dynamicRealm.close();
    }

    @Test
    public void setList_managedRealmList() {
        dynamicRealm.executeTransaction(new DynamicRealm.Transaction() {
            @Override
            public void execute(DynamicRealm realm) {
                realm.deleteAll();

                DynamicRealmObject allTypes = realm.createObject(AllTypes.CLASS_NAME);
                allTypes.setString(AllTypes.FIELD_STRING, "bender");

                DynamicRealmObject anotherAllTypes;
                {
                    anotherAllTypes = realm.createObject(AllTypes.CLASS_NAME);
                    anotherAllTypes.setString(AllTypes.FIELD_STRING, "bender2");
                    DynamicRealmObject dog = realm.createObject(Dog.CLASS_NAME);
                    dog.setString(Dog.FIELD_NAME, "nibbler");
                    anotherAllTypes.getList(AllTypes.FIELD_REALMLIST).add(dog);
                }

                // set managed RealmList
                allTypes.setList(AllTypes.FIELD_REALMLIST, anotherAllTypes.getList(AllTypes.FIELD_REALMLIST));
            }
        });

        DynamicRealmObject allTypes = dynamicRealm.where(AllTypes.CLASS_NAME)
                .equalTo(AllTypes.FIELD_STRING, "bender")
                .findFirst();
        assertEquals(1, allTypes.getList(AllTypes.FIELD_REALMLIST).size());
        assertEquals("nibbler", allTypes.getList(AllTypes.FIELD_REALMLIST).first().get(Dog.FIELD_NAME));

        // Check if allTypes and anotherAllTypes share the same Dog object.
        dynamicRealm.executeTransaction(new DynamicRealm.Transaction() {
            @Override
            public void execute(DynamicRealm realm) {
                DynamicRealmObject anotherAllTypes = dynamicRealm.where(AllTypes.CLASS_NAME)
                        .equalTo(AllTypes.FIELD_STRING, "bender2")
                        .findFirst();
                anotherAllTypes.getList(AllTypes.FIELD_REALMLIST).first()
                        .setString(Dog.FIELD_NAME, "nibbler_modified");
            }
        });

        assertEquals("nibbler_modified", allTypes.getList(AllTypes.FIELD_REALMLIST).first().get(Dog.FIELD_NAME));
    }

    @Test
    public void setList_elementBelongToTypedRealmThrows() {
        RealmList<DynamicRealmObject> list = new RealmList<DynamicRealmObject>();
        list.add(dObjTyped);

        dynamicRealm.beginTransaction();

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Each element in 'list' must belong to the same Realm instance.");
        dynamicRealm.where(AllJavaTypes.CLASS_NAME).findFirst().setList(AllJavaTypes.FIELD_LIST, list);

        dynamicRealm.cancelTransaction();
    }

    @Test
    public void setList_elementBelongToDiffThreadRealmThrows() {
        final CountDownLatch finishedLatch = new CountDownLatch(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                DynamicRealm dynamicRealm = DynamicRealm.getInstance(realm.getConfiguration());
                RealmList<DynamicRealmObject> list = new RealmList<DynamicRealmObject>();
                list.add(dObjDynamic);

                dynamicRealm.beginTransaction();

                try {
                    // ExpectedException doesn't work in another thread.
                    dynamicRealm.where(AllJavaTypes.CLASS_NAME).findFirst().setList(AllJavaTypes.FIELD_LIST, list);
                    fail();
                } catch (IllegalArgumentException expected) {
                    assertEquals("Each element in 'list' must belong to the same Realm instance.", expected.getMessage());
                }

                dynamicRealm.cancelTransaction();
                dynamicRealm.close();
                finishedLatch.countDown();
            }
        }).start();
        TestHelper.awaitOrFail(finishedLatch);
    }

    @Test
    public void setList_wrongTypeThrows() {
        realm.beginTransaction();
        AllTypes wrongObj = realm.createObject(AllTypes.class);
        DynamicRealmObject wrongDynamicObject = new DynamicRealmObject(wrongObj);
        RealmList<DynamicRealmObject> wrongDynamicList = wrongDynamicObject.getList(AllTypes.FIELD_REALMLIST);
        thrown.expect(IllegalArgumentException.class);
        dObjTyped.setList(AllJavaTypes.FIELD_LIST, wrongDynamicList);
    }

    @Test
    public void setList_javaModelClassesThrowProperErrorMessage() {
        dynamicRealm.beginTransaction();
        try {
            dObjDynamic.setList(AllJavaTypes.FIELD_LIST, new RealmList<>(typedObj));
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("RealmList must contain `DynamicRealmObject's, not Java model classes."));
        }
    }

    @Test
    public void setList_objectsOwnList() {
        dynamicRealm.beginTransaction();

        // Test model classes
        int originalSize = dObjDynamic.getList(AllJavaTypes.FIELD_LIST).size();
        dObjDynamic.setList(AllJavaTypes.FIELD_LIST, dObjDynamic.getList(AllJavaTypes.FIELD_LIST));
        assertEquals(originalSize, dObjDynamic.getList(AllJavaTypes.FIELD_LIST).size());

        // Smoke test value lists
        originalSize = dObjDynamic.getList(AllJavaTypes.FIELD_STRING_LIST, String.class).size();
        dObjDynamic.setList(AllJavaTypes.FIELD_STRING_LIST, dObjDynamic.getList(AllJavaTypes.FIELD_STRING_LIST, String.class));
        assertEquals(originalSize, dObjDynamic.getList(AllJavaTypes.FIELD_STRING_LIST, String.class).size());
    }

    @Test
    public void untypedSetter_listWrongTypeThrows() {
        realm.beginTransaction();
        AllTypes wrongObj = realm.createObject(AllTypes.class);
        thrown.expect(IllegalArgumentException.class);
        dObjTyped.set(AllJavaTypes.FIELD_LIST, wrongObj.getColumnRealmList());
    }

    @Test
    public void untypedSetter_listMixedTypesThrows() {
        realm.beginTransaction();
        AllJavaTypes obj1 = realm.createObject(AllJavaTypes.class, 2);
        CyclicType obj2 = realm.createObject(CyclicType.class);

        RealmList<DynamicRealmObject> list = new RealmList<DynamicRealmObject>();
        list.add(new DynamicRealmObject(obj1));
        list.add(new DynamicRealmObject(obj2));
        thrown.expect(IllegalArgumentException.class);
        dObjTyped.set(AllJavaTypes.FIELD_LIST, list);
    }

    // List is not a simple getter, tests separately.
    @Test
    public void getList() {
        realm.beginTransaction();
        AllTypes obj = realm.createObject(AllTypes.class);
        Dog dog = realm.createObject(Dog.class);
        dog.setName("fido");
        obj.getColumnRealmList().add(dog);
        realm.commitTransaction();

        DynamicRealmObject dynamicAllTypes = new DynamicRealmObject(obj);
        RealmList<DynamicRealmObject> list = dynamicAllTypes.getList(AllTypes.FIELD_REALMLIST);
        DynamicRealmObject listObject = list.get(0);

        assertEquals(1, list.size());
        assertEquals(Dog.CLASS_NAME, listObject.getType());
        assertEquals("fido", listObject.getString(Dog.FIELD_NAME));
    }


    @Test
    public void untypedGetterSetter() {
        realm.beginTransaction();
        AllJavaTypes obj = realm.createObject(AllJavaTypes.class, 0);
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
                        assertEquals(Long.parseLong("42"), dObj.<Long> get(AllJavaTypes.FIELD_SHORT).longValue());
                        break;
                    case INT:
                        dObj.set(AllJavaTypes.FIELD_INT, 42);
                        assertEquals(Long.parseLong("42"), dObj.<Long> get(AllJavaTypes.FIELD_INT).longValue());
                        break;
                    case LONG:
                        dObj.set(AllJavaTypes.FIELD_LONG, 42L);
                        assertEquals(Long.parseLong("42"), dObj.<Long> get(AllJavaTypes.FIELD_LONG).longValue());
                        break;
                    case BYTE:
                        dObj.set(AllJavaTypes.FIELD_BYTE, (byte) 4);
                        assertEquals(Long.parseLong("4"), dObj.<Long> get(AllJavaTypes.FIELD_BYTE).longValue());
                        break;
                    case FLOAT:
                        dObj.set(AllJavaTypes.FIELD_FLOAT, 1.23f);
                        assertEquals(Float.parseFloat("1.23"), dObj.<Float> get(AllJavaTypes.FIELD_FLOAT), Float.MIN_NORMAL);
                        break;
                    case DOUBLE:
                        dObj.set(AllJavaTypes.FIELD_DOUBLE, 1.234d);
                        assertEquals(Double.parseDouble("1.234"), dObj.<Double>get(AllJavaTypes.FIELD_DOUBLE), Double.MIN_NORMAL);
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
                    case LIST: {
                        RealmList<DynamicRealmObject> newList = new RealmList<DynamicRealmObject>();
                        newList.add(dObj);
                        dObj.set(AllJavaTypes.FIELD_LIST, newList);
                        RealmList<DynamicRealmObject> list = dObj.getList(AllJavaTypes.FIELD_LIST);
                        assertEquals(1, list.size());
                        assertEquals(dObj, list.get(0));
                        break;
                    }
                    case LIST_INTEGER: {
                        RealmList<Integer> newList = new RealmList<>(null, 1);
                        dObj.set(AllJavaTypes.FIELD_INTEGER_LIST, newList);
                        RealmList<Integer> list = dObj.getList(AllJavaTypes.FIELD_INTEGER_LIST, Integer.class);
                        assertEquals(2, list.size());
                        assertArrayEquals(newList.toArray(), list.toArray());
                        break;
                    }
                    case LIST_STRING: {
                        RealmList<String> newList = new RealmList<>(null, "Foo");
                        dObj.set(AllJavaTypes.FIELD_STRING_LIST, newList);
                        RealmList<String> list = dObj.getList(AllJavaTypes.FIELD_STRING_LIST, String.class);
                        assertEquals(2, list.size());
                        assertArrayEquals(newList.toArray(), list.toArray());
                        break;
                    }
                    case LIST_BOOLEAN: {
                        RealmList<Boolean> newList = new RealmList<>(null, true);
                        dObj.set(AllJavaTypes.FIELD_BOOLEAN_LIST, newList);
                        RealmList<Boolean> list = dObj.getList(AllJavaTypes.FIELD_BOOLEAN_LIST, Boolean.class);
                        assertEquals(2, list.size());
                        assertArrayEquals(newList.toArray(), list.toArray());
                        break;
                    }
                    case LIST_FLOAT: {
                        RealmList<Float> newList = new RealmList<>(null, 1.23F);
                        dObj.set(AllJavaTypes.FIELD_FLOAT_LIST, newList);
                        RealmList<Float> list = dObj.getList(AllJavaTypes.FIELD_FLOAT_LIST, Float.class);
                        assertEquals(2, list.size());
                        assertArrayEquals(newList.toArray(), list.toArray());
                        break;
                    }
                    case LIST_DOUBLE: {
                        RealmList<Double> newList = new RealmList<>(null, 1.24D);
                        dObj.set(AllJavaTypes.FIELD_DOUBLE_LIST, newList);
                        RealmList<Double> list = dObj.getList(AllJavaTypes.FIELD_DOUBLE_LIST, Double.class);
                        assertEquals(2, list.size());
                        assertArrayEquals(newList.toArray(), list.toArray());
                        break;
                    }
                    case LIST_BINARY: {
                        RealmList<byte[]> newList = new RealmList<>(null, new byte[] {1, 2, 3});
                        dObj.set(AllJavaTypes.FIELD_BINARY_LIST, newList);
                        RealmList<byte[]> list = dObj.getList(AllJavaTypes.FIELD_BINARY_LIST, byte[].class);
                        assertEquals(2, list.size());
                        assertArrayEquals(newList.toArray(), list.toArray());
                        break;
                    }
                    case LIST_DATE: {
                        RealmList<Date> newList = new RealmList<>(null, new Date(1000));
                        dObj.set(AllJavaTypes.FIELD_DATE_LIST, newList);
                        RealmList<Date> list = dObj.getList(AllJavaTypes.FIELD_DATE_LIST, Date.class);
                        assertEquals(2, list.size());
                        assertArrayEquals(newList.toArray(), list.toArray());
                        break;
                    }
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
        AllJavaTypes obj = realm.createObject(AllJavaTypes.class, 0);
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
                    case LIST_INTEGER:
                    case LIST_STRING:
                    case LIST_BOOLEAN:
                    case LIST_FLOAT:
                    case LIST_DOUBLE:
                    case LIST_BINARY:
                    case LIST_DATE:
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
        AllJavaTypes obj = realm.createObject(AllJavaTypes.class, 0);
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
                            dObj.set(AllJavaTypes.FIELD_ID, "foo");
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
                        case LIST_INTEGER:
                        case LIST_STRING:
                        case LIST_BOOLEAN:
                        case LIST_FLOAT:
                        case LIST_DOUBLE:
                        case LIST_BINARY:
                        case LIST_DATE:
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
                        // Providing "foo" to the date parser will blow up with a RealmException
                        // and the cause will be a ParseException.
                        fail(type + " failed");
                    }
                }
            }
        } finally {
            realm.cancelTransaction();
        }
    }

    private void testChangePrimaryKeyThroughUntypedSetter(String value) {
        final String[] primaryKeyClasses = {PrimaryKeyAsBoxedByte.CLASS_NAME, PrimaryKeyAsBoxedShort.CLASS_NAME,
                PrimaryKeyAsBoxedInteger.CLASS_NAME, PrimaryKeyAsBoxedLong.CLASS_NAME, PrimaryKeyAsString.CLASS_NAME};
        for (String pkClass : primaryKeyClasses) {
            dynamicRealm.beginTransaction();
            DynamicRealmObject object;
            boolean isStringPK = pkClass.equals(PrimaryKeyAsString.CLASS_NAME);
            if (isStringPK) {
                object = dynamicRealm.createObject(pkClass, "");
            } else {
                object = dynamicRealm.createObject(pkClass, 0);
            }

            try {
                object.set(isStringPK ? PrimaryKeyAsString.FIELD_PRIMARY_KEY : "id", value);
                fail();
            } catch (IllegalArgumentException ignored) {
            }
            dynamicRealm.cancelTransaction();
        }
    }

    @Test
    public void untypedSetter_setValue_changePrimaryKeyThrows() {
        testChangePrimaryKeyThroughUntypedSetter("42");
    }

    @Test
    public void untypedSetter_setNull_changePrimaryKeyThrows() {
        testChangePrimaryKeyThroughUntypedSetter(null);
    }

    @Test
    public void isNull_nullNotSupportedField() {
        assertFalse(dObjTyped.isNull(AllJavaTypes.FIELD_INT));
    }

    @Test
    public void isNull_true() {
        realm.beginTransaction();
        AllJavaTypes obj = realm.createObject(AllJavaTypes.class, 0);
        realm.commitTransaction();

        assertTrue(new DynamicRealmObject(obj).isNull(AllJavaTypes.FIELD_OBJECT));
    }

    @Test
    public void isNull_false() {
        assertFalse(dObjTyped.isNull(AllJavaTypes.FIELD_OBJECT));
    }

    @Test
    public void getFieldNames() {
        String[] expectedKeys = {AllJavaTypes.FIELD_STRING, AllJavaTypes.FIELD_ID, AllJavaTypes.FIELD_LONG,
                AllJavaTypes.FIELD_SHORT, AllJavaTypes.FIELD_INT, AllJavaTypes.FIELD_BYTE, AllJavaTypes.FIELD_FLOAT,
                AllJavaTypes.FIELD_DOUBLE, AllJavaTypes.FIELD_BOOLEAN, AllJavaTypes.FIELD_DATE,
                AllJavaTypes.FIELD_BINARY, AllJavaTypes.FIELD_OBJECT, AllJavaTypes.FIELD_LIST,
                AllJavaTypes.FIELD_STRING_LIST, AllJavaTypes.FIELD_BINARY_LIST, AllJavaTypes.FIELD_BOOLEAN_LIST,
                AllJavaTypes.FIELD_LONG_LIST, AllJavaTypes.FIELD_INTEGER_LIST, AllJavaTypes.FIELD_SHORT_LIST,
                AllJavaTypes.FIELD_BYTE_LIST, AllJavaTypes.FIELD_DOUBLE_LIST, AllJavaTypes.FIELD_FLOAT_LIST,
                AllJavaTypes.FIELD_DATE_LIST};
        String[] keys = dObjTyped.getFieldNames();
        // After the stable ID support, primary key field will be inserted first before others. So even FIELD_STRING is
        // the first defined field in the class, it will be inserted after FIELD_ID.
        // See ObjectStore::add_initial_columns #if REALM_HAVE_SYNC_STABLE_IDS branch.
        assertEquals(expectedKeys.length, keys.length);
        assertThat(Arrays.asList(expectedKeys), Matchers.hasItems(keys));
    }

    @Test
    public void hasField_false() {
        assertFalse(dObjTyped.hasField(null));
        assertFalse(dObjTyped.hasField(""));
        assertFalse(dObjTyped.hasField("foo"));
        assertFalse(dObjTyped.hasField("foo.bar"));
        assertFalse(dObjTyped.hasField(TestHelper.getRandomString(65)));
    }

    @Test
    public void hasField_true() {
        assertTrue(dObjTyped.hasField(AllJavaTypes.FIELD_STRING));
    }

    @Test
    public void getFieldType() {
        assertEquals(RealmFieldType.STRING, dObjTyped.getFieldType(AllJavaTypes.FIELD_STRING));
        assertEquals(RealmFieldType.BINARY, dObjTyped.getFieldType(AllJavaTypes.FIELD_BINARY));
        assertEquals(RealmFieldType.BOOLEAN, dObjTyped.getFieldType(AllJavaTypes.FIELD_BOOLEAN));
        assertEquals(RealmFieldType.DATE, dObjTyped.getFieldType(AllJavaTypes.FIELD_DATE));
        assertEquals(RealmFieldType.DOUBLE, dObjTyped.getFieldType(AllJavaTypes.FIELD_DOUBLE));
        assertEquals(RealmFieldType.FLOAT, dObjTyped.getFieldType(AllJavaTypes.FIELD_FLOAT));
        assertEquals(RealmFieldType.OBJECT, dObjTyped.getFieldType(AllJavaTypes.FIELD_OBJECT));
        assertEquals(RealmFieldType.LIST, dObjTyped.getFieldType(AllJavaTypes.FIELD_LIST));
        assertEquals(RealmFieldType.INTEGER, dObjTyped.getFieldType(AllJavaTypes.FIELD_BYTE));
        assertEquals(RealmFieldType.INTEGER, dObjTyped.getFieldType(AllJavaTypes.FIELD_SHORT));
        assertEquals(RealmFieldType.INTEGER, dObjTyped.getFieldType(AllJavaTypes.FIELD_INT));
        assertEquals(RealmFieldType.INTEGER, dObjTyped.getFieldType(AllJavaTypes.FIELD_LONG));
        assertEquals(RealmFieldType.INTEGER_LIST, dObjTyped.getFieldType(AllJavaTypes.FIELD_INTEGER_LIST));
        assertEquals(RealmFieldType.STRING_LIST, dObjTyped.getFieldType(AllJavaTypes.FIELD_STRING_LIST));
        assertEquals(RealmFieldType.BOOLEAN_LIST, dObjTyped.getFieldType(AllJavaTypes.FIELD_BOOLEAN_LIST));
        assertEquals(RealmFieldType.FLOAT_LIST, dObjTyped.getFieldType(AllJavaTypes.FIELD_FLOAT_LIST));
        assertEquals(RealmFieldType.DOUBLE_LIST, dObjTyped.getFieldType(AllJavaTypes.FIELD_DOUBLE_LIST));
        assertEquals(RealmFieldType.BINARY_LIST, dObjTyped.getFieldType(AllJavaTypes.FIELD_BINARY_LIST));
        assertEquals(RealmFieldType.DATE_LIST, dObjTyped.getFieldType(AllJavaTypes.FIELD_DATE_LIST));
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
        assertFalse(dObjTyped.equals(standardObj));
    }

    @Test
    public void hashcode() {
        AllJavaTypes standardObj = realm.where(AllJavaTypes.class).findFirst();
        DynamicRealmObject dObj1 = new DynamicRealmObject(standardObj);
        assertEquals(standardObj.hashCode(), dObj1.hashCode());
    }

    @Test
    public void toString_test() {
        // Checks that toString() doesn't crash, and does simple formatting checks. We cannot compare to a set String as
        // eg. the byte array will be allocated each time it is accessed.
        String str = dObjTyped.toString();
        assertTrue(str.startsWith("AllJavaTypes = dynamic["));
        assertTrue(str.endsWith("}]"));
    }

    @Test
    public void toString_nullValues() {
        dynamicRealm.beginTransaction();
        final DynamicRealmObject obj = dynamicRealm.createObject(NullTypes.CLASS_NAME, 0);
        dynamicRealm.commitTransaction();

        String str = obj.toString();
        assertTrue(str.contains(NullTypes.FIELD_STRING_NULL + ":null"));
        assertTrue(str.contains(NullTypes.FIELD_BYTES_NULL + ":null"));
        assertTrue(str.contains(NullTypes.FIELD_BOOLEAN_NULL + ":null"));
        assertTrue(str.contains(NullTypes.FIELD_BYTE_NULL + ":null"));
        assertTrue(str.contains(NullTypes.FIELD_SHORT_NULL + ":null"));
        assertTrue(str.contains(NullTypes.FIELD_INTEGER_NULL + ":null"));
        assertTrue(str.contains(NullTypes.FIELD_LONG_NULL + ":null"));
        assertTrue(str.contains(NullTypes.FIELD_FLOAT_NULL + ":null"));
        assertTrue(str.contains(NullTypes.FIELD_DOUBLE_NULL + ":null"));
        assertTrue(str.contains(NullTypes.FIELD_DATE_NULL + ":null"));
        assertTrue(str.contains(NullTypes.FIELD_OBJECT_NULL + ":null"));
        assertTrue(str.contains(NullTypes.FIELD_LIST_NULL + ":RealmList<NullTypes>[0]"));
        assertTrue(str.contains(NullTypes.FIELD_INTEGER_LIST_NULL + ":RealmList<Long>[0]"));
        assertTrue(str.contains(NullTypes.FIELD_STRING_LIST_NULL + ":RealmList<String>[0]"));
        assertTrue(str.contains(NullTypes.FIELD_BOOLEAN_LIST_NULL + ":RealmList<Boolean>[0]"));
        assertTrue(str.contains(NullTypes.FIELD_FLOAT_LIST_NULL + ":RealmList<Float>[0]"));
        assertTrue(str.contains(NullTypes.FIELD_DOUBLE_LIST_NULL + ":RealmList<Double>[0]"));
        assertTrue(str.contains(NullTypes.FIELD_BINARY_LIST_NULL + ":RealmList<byte[]>[0]"));
        assertTrue(str.contains(NullTypes.FIELD_DATE_LIST_NULL + ":RealmList<Date>[0]"));
    }

    @Test
    public void testExceptionMessage() {
        // Tests for https://github.com/realm/realm-java/issues/2141
        realm.beginTransaction();
        AllTypes obj = realm.createObject(AllTypes.class);
        realm.commitTransaction();

        DynamicRealmObject o = new DynamicRealmObject(obj);
        try {
            o.getFloat("nonExisting"); // Notes that "o" does not have "nonExisting" field.
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Illegal Argument: Field not found: nonExisting", e.getMessage());
        }
    }

    @Test
    public void getDynamicRealm() {
        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();

        dynamicRealm.refresh();
        final DynamicRealmObject object = dynamicRealm.where(AllTypes.CLASS_NAME).findFirst();

        assertSame(dynamicRealm, object.getDynamicRealm());
    }

    @Test
    public void getRealm() {
        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();

        dynamicRealm.refresh();
        final DynamicRealmObject object = dynamicRealm.where(AllTypes.CLASS_NAME).findFirst();

        thrown.expect(IllegalStateException.class);
        object.getRealm();
    }

    @Test
    public void getRealm_closedObjectThrows() {
        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();

        dynamicRealm.refresh();
        final DynamicRealmObject object = dynamicRealm.where(AllTypes.CLASS_NAME).findFirst();
        dynamicRealm.close();
        dynamicRealm = null;

        try {
            object.getDynamicRealm();
            fail();
        } catch (IllegalStateException e) {
            assertEquals(BaseRealm.CLOSED_REALM_MESSAGE, e.getMessage());
        }
    }

    @Test
    public void getRealmConfiguration_deletedObjectThrows() {
        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();

        dynamicRealm.refresh();
        final DynamicRealmObject object = dynamicRealm.where(AllTypes.CLASS_NAME).findFirst();
        dynamicRealm.beginTransaction();
        object.deleteFromRealm();
        dynamicRealm.commitTransaction();

        try {
            object.getDynamicRealm();
            fail();
        } catch (IllegalStateException e) {
            assertEquals(RealmObject.MSG_DELETED_OBJECT, e.getMessage());
        }
    }

    @Test
    public void getRealm_illegalThreadThrows() throws Throwable {
        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();

        dynamicRealm.refresh();
        final DynamicRealmObject object = dynamicRealm.where(AllTypes.CLASS_NAME).findFirst();

        final CountDownLatch threadFinished = new CountDownLatch(1);
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    object.getDynamicRealm();
                    fail();
                } catch (IllegalStateException e) {
                    assertEquals(BaseRealm.INCORRECT_THREAD_MESSAGE, e.getMessage());
                } finally {
                    threadFinished.countDown();
                }
            }
        });
        thread.start();
        TestHelper.awaitOrFail(threadFinished);
    }
}
