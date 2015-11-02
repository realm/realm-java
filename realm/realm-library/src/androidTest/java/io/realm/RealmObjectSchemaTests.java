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
 *
 */

package io.realm;

import android.test.AndroidTestCase;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.entities.AllJavaTypes;

public class RealmObjectSchemaTests extends AndroidTestCase {

    private RealmObjectSchema DOG_SCHEMA;
    private DynamicRealm realm;
    private RealmObjectSchema schema;
    private RealmSchema realmSchema;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        RealmConfiguration realmConfig = new RealmConfiguration.Builder(getContext()).build();
        Realm.deleteRealm(realmConfig);
        Realm.getInstance(realmConfig).close(); // Create Schema
        realm = DynamicRealm.getInstance(realmConfig);
        realmSchema = realm.getSchema();
        DOG_SCHEMA = realmSchema.getObjectSchema("Dog");
        realm.beginTransaction();
        schema = realmSchema.createClass("NewClass");
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        realm.cancelTransaction();
        realm.close();
    }

    public enum FieldType {
        STRING, SHORT, INT, LONG, BYTE, BOOLEAN, FLOAT, DOUBLE, BLOB, DATE, OBJECT, LIST
    }

    public enum IndexFieldType {
        STRING, SHORT, INT, LONG, BOOLEAN, BYTE, DATE
    }

    public enum PrimaryKeyFieldType {
        STRING, SHORT, INT, LONG  // These should also be allowed? BOOLEAN, BYTE, DATE
    }

    public void testAddRemoveField() {
        for (FieldType fieldType : FieldType.values()) {
            String fieldName;
            switch(fieldType) {
                case STRING:
                    fieldName = AllJavaTypes.FIELD_STRING;
                    schema.addField(String.class, fieldName);
                    checkAddedAndRemovable(fieldName);
                    break;
                case SHORT:
                    fieldName = AllJavaTypes.FIELD_SHORT;
                    schema.addField(short.class, fieldName);
                    checkAddedAndRemovable(fieldName);
                    break;
                case INT:
                    fieldName = AllJavaTypes.FIELD_INT;
                    schema.addField(int.class, fieldName);
                    checkAddedAndRemovable(fieldName);
                    break;
                case LONG:
                    fieldName = AllJavaTypes.FIELD_LONG;
                    schema.addField(long.class, fieldName);
                    checkAddedAndRemovable(fieldName);
                    break;
                case BYTE:
                    fieldName = AllJavaTypes.FIELD_BYTE;
                    schema.addField(byte.class, fieldName);
                    checkAddedAndRemovable(fieldName);
                    break;
                case BOOLEAN:
                    fieldName = AllJavaTypes.FIELD_BOOLEAN;
                    schema.addField(boolean.class, fieldName);
                    checkAddedAndRemovable(fieldName);
                    break;
                case FLOAT:
                    fieldName = AllJavaTypes.FIELD_FLOAT;
                    schema.addField(float.class, fieldName);
                    checkAddedAndRemovable(fieldName);
                    break;
                case DOUBLE:
                    fieldName = AllJavaTypes.FIELD_DOUBLE;
                    schema.addField(double.class, fieldName);
                    checkAddedAndRemovable(fieldName);
                    break;
                case BLOB:
                    fieldName = AllJavaTypes.FIELD_BINARY;
                    schema.addField(byte[].class, fieldName);
                    checkAddedAndRemovable(fieldName);
                    break;
                case DATE:
                    fieldName = AllJavaTypes.FIELD_DATE;
                    schema.addField(Date.class, fieldName);
                    checkAddedAndRemovable(fieldName);
                    break;
                case OBJECT:
                    fieldName = AllJavaTypes.FIELD_OBJECT;
                    schema.addLinkField(RealmObject.class, fieldName, DOG_SCHEMA);
                    checkAddedAndRemovable(fieldName);
                    break;
                case LIST:
                    fieldName = AllJavaTypes.FIELD_LIST;
                    schema.addLinkField(RealmList.class, fieldName, DOG_SCHEMA);
                    checkAddedAndRemovable(fieldName);
                    break;
                default:
                    fail();
            }
        }
    }

    // Check that field is actually added and that i can be removed again.
    private void checkAddedAndRemovable(String fieldName) {
        assertTrue(schema.hasField(fieldName));
        schema.removeField(fieldName);
        assertFalse(schema.hasField(fieldName));
    }

    public void testAddRemoveIndexedField() {
        for (IndexFieldType fieldType : IndexFieldType.values()) {
            String fieldName;
            switch (fieldType) {
                case STRING:
                    fieldName = AllJavaTypes.FIELD_STRING;
                    schema.addField(String.class, fieldName, RealmModifier.INDEXED);
                    checkAddedAndRemovable(fieldName);
                    break;
                case SHORT:
                    fieldName = AllJavaTypes.FIELD_SHORT;
                    schema.addField(short.class, fieldName, RealmModifier.INDEXED);
                    checkAddedAndRemovable(fieldName);
                    break;
                case INT:
                    fieldName = AllJavaTypes.FIELD_INT;
                    schema.addField(int.class, fieldName, RealmModifier.INDEXED);
                    checkAddedAndRemovable(fieldName);
                    break;
                case LONG:
                    fieldName = AllJavaTypes.FIELD_LONG;
                    schema.addField(long.class, fieldName, RealmModifier.INDEXED);
                    checkAddedAndRemovable(fieldName);
                    break;
                case BYTE:
                    fieldName = AllJavaTypes.FIELD_BYTE;
                    schema.addField(byte.class, fieldName, RealmModifier.INDEXED);
                    checkAddedAndRemovable(fieldName);
                    break;
                case BOOLEAN:
                    fieldName = AllJavaTypes.FIELD_BOOLEAN;
                    schema.addField(boolean.class, fieldName, RealmModifier.INDEXED);
                    checkAddedAndRemovable(fieldName);
                    break;
                case DATE:
                    fieldName = AllJavaTypes.FIELD_DATE;
                    schema.addField(Date.class, fieldName, RealmModifier.INDEXED);
                    checkAddedAndRemovable(fieldName);
                    break;
                default:
                    fail(fieldType + " wasn't handled");
            }
        }
    }

    public void testAddRemovePrimaryKeyField() {
        for (PrimaryKeyFieldType fieldType : PrimaryKeyFieldType.values()) {
            String fieldName;
            switch (fieldType) {
                case STRING:
                    fieldName = AllJavaTypes.FIELD_STRING;
                    schema.addField(String.class, fieldName, RealmModifier.PRIMARY_KEY);
                    checkAddedAndRemovable(fieldName);
                    break;
                case SHORT:
                    fieldName = AllJavaTypes.FIELD_SHORT;
                    schema.addField(short.class, fieldName, RealmModifier.PRIMARY_KEY);
                    checkAddedAndRemovable(fieldName);
                    break;
                case INT:
                    fieldName = AllJavaTypes.FIELD_INT;
                    schema.addField(int.class, fieldName, RealmModifier.PRIMARY_KEY);
                    checkAddedAndRemovable(fieldName);
                    break;
                case LONG:
                    fieldName = AllJavaTypes.FIELD_LONG;
                    schema.addField(long.class, fieldName, RealmModifier.PRIMARY_KEY);
                    checkAddedAndRemovable(fieldName);
                    break;
                default:
                    fail();
            }
        }
    }

    public void testAddField_nameAlreadyExistsThrows() {
        for (FieldType fieldType : FieldType.values()) {
            switch(fieldType) {
                case STRING:
                    checkAddFieldTwice(AllJavaTypes.FIELD_STRING, new FieldRunnable() {
                        @Override
                        public void run(String fieldName) {
                            schema.addField(String.class, fieldName);
                        }
                    });
                    break;
                case SHORT:
                    checkAddFieldTwice(AllJavaTypes.FIELD_SHORT, new FieldRunnable() {
                        @Override
                        public void run(String fieldName) {
                            schema.addField(short.class, fieldName);
                        }
                    });
                    break;
                case INT:
                    checkAddFieldTwice(AllJavaTypes.FIELD_INT, new FieldRunnable() {
                        @Override
                        public void run(String fieldName) {
                            schema.addField(int.class, fieldName);
                        }
                    });
                    break;
                case LONG:
                    checkAddFieldTwice(AllJavaTypes.FIELD_LONG, new FieldRunnable() {
                        @Override
                        public void run(String fieldName) {
                            schema.addField(long.class, fieldName);
                        }
                    });
                    break;
                case BYTE:
                    checkAddFieldTwice(AllJavaTypes.FIELD_BYTE, new FieldRunnable() {
                        @Override
                        public void run(String fieldName) {
                            schema.addField(byte.class, fieldName);
                        }
                    });
                    break;
                case BOOLEAN:
                    checkAddFieldTwice(AllJavaTypes.FIELD_BOOLEAN, new FieldRunnable() {
                        @Override
                        public void run(String fieldName) {
                            schema.addField(boolean.class, fieldName);
                        }
                    });
                    break;
                case FLOAT:
                    checkAddFieldTwice(AllJavaTypes.FIELD_FLOAT, new FieldRunnable() {
                        @Override
                        public void run(String fieldName) {
                            schema.addField(float.class, fieldName);
                        }
                    });
                    break;
                case DOUBLE:
                    checkAddFieldTwice(AllJavaTypes.FIELD_DOUBLE, new FieldRunnable() {
                        @Override
                        public void run(String fieldName) {
                            schema.addField(double.class, fieldName);
                        }
                    });
                    break;
                case BLOB:
                    checkAddFieldTwice(AllJavaTypes.FIELD_BINARY, new FieldRunnable() {
                        @Override
                        public void run(String fieldName) {
                            schema.addField(byte[].class, fieldName);
                        }
                    });
                    break;
                case DATE:
                    checkAddFieldTwice(AllJavaTypes.FIELD_DATE, new FieldRunnable() {
                        @Override
                        public void run(String fieldName) {
                            schema.addField(Date.class, fieldName);
                        }
                    });
                    break;
                case OBJECT:
                    checkAddFieldTwice(AllJavaTypes.FIELD_OBJECT, new FieldRunnable() {
                        @Override
                        public void run(String fieldName) {
                            schema.addLinkField(RealmObject.class, fieldName, DOG_SCHEMA);
                        }
                    });
                    break;
                case LIST:
                    checkAddFieldTwice(AllJavaTypes.FIELD_LIST, new FieldRunnable() {
                        @Override
                        public void run(String fieldName) {
                            schema.addLinkField(RealmList.class, fieldName, DOG_SCHEMA);
                        }
                    });
                    break;
                default:
                    fail();
            }
        }
    }

    private void checkAddFieldTwice(String fieldName, FieldRunnable runnable) {
        runnable.run(fieldName);
        try {
            runnable.run(fieldName);
            fail("Was able to add field twice: " + fieldName);
        } catch (IllegalArgumentException ignored) {
        }
    }


    public void testAddField_illegalFieldNameThrows() {
        String[] fieldNames = new String[] { null, "", "foo.bar", TestHelper.getRandomString(65) };
        for (FieldType fieldType : FieldType.values()) {
            for (String fieldName : fieldNames) {
                try {
                    switch(fieldType) {
                        case STRING: schema.addField(String.class, fieldName); break;
                        case SHORT: schema.addField(short.class, fieldName); break;
                        case INT: schema.addField(int.class, fieldName); break;
                        case LONG: schema.addField(long.class, fieldName); break;
                        case BYTE: schema.addField(byte.class, fieldName); break;
                        case BOOLEAN: schema.addField(boolean.class, fieldName); break;
                        case FLOAT: schema.addField(float.class, fieldName); break;
                        case DOUBLE: schema.addField(double.class, fieldName); break;
                        case BLOB: schema.addField(byte[].class, fieldName); break;
                        case DATE: schema.addField(Date.class, fieldName); break;
                        case OBJECT: schema.addLinkField(RealmObject.class, fieldName, DOG_SCHEMA); break;
                        case LIST: schema.addLinkField(RealmList.class, fieldName, DOG_SCHEMA); break;
                        default:
                            fail("Unknown type: " + fieldType);
                    }
                    fail(fieldType + " didn't throw");
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }

    public void testAddPrimaryKeyFieldModifier_alreadyExistsThrows() {
        for (PrimaryKeyFieldType fieldType : PrimaryKeyFieldType.values()) {
            String fieldName = null;
            switch (fieldType) {
                case STRING:
                    fieldName = AllJavaTypes.FIELD_STRING;
                    schema.addField(String.class, fieldName);
                    break;
                case SHORT:
                    fieldName = AllJavaTypes.FIELD_SHORT;
                    schema.addField(short.class, fieldName);
                    break;
                case INT:
                    fieldName = AllJavaTypes.FIELD_INT;
                    schema.addField(int.class, fieldName);
                    break;
                case LONG:
                    fieldName = AllJavaTypes.FIELD_LONG;
                    schema.addField(long.class, fieldName);
                    break;
                default:
                    fail();
            }
            schema.addPrimaryKey(fieldName);
            try {
                schema.addPrimaryKey(fieldName);
                fail();
            } catch (IllegalStateException ignored) {
                schema.removePrimaryKey();
            }
        }
    }

    public void testAddPrimaryKeyFieldModifier_illegalFieldTypeThrows() {
        String fieldName = AllJavaTypes.FIELD_DOUBLE;
        schema.addField(double.class, fieldName);
        try {
            schema.addPrimaryKey(fieldName);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    public void testAddIndexFieldModifier_illegalFieldTypeThrows() {
        String fieldName = AllJavaTypes.FIELD_DOUBLE;
        schema.addField(double.class, fieldName);
        try {
            schema.addIndex(fieldName);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    public void testAddIndexFieldModifier_alreadyIndexedThrows() {
        for (IndexFieldType fieldType : IndexFieldType.values()) {
            String fieldName = null;
            switch (fieldType) {
                case STRING:
                    fieldName = AllJavaTypes.FIELD_STRING;
                    schema.addField(String.class, fieldName);
                    break;
                case SHORT:
                    fieldName = AllJavaTypes.FIELD_SHORT;
                    schema.addField(short.class, fieldName);
                    break;
                case INT:
                    fieldName = AllJavaTypes.FIELD_INT;
                    schema.addField(int.class, fieldName);
                    break;
                case LONG:
                    fieldName = AllJavaTypes.FIELD_LONG;
                    schema.addField(long.class, fieldName);
                    break;
                case BOOLEAN:
                    fieldName = AllJavaTypes.FIELD_BOOLEAN;
                    schema.addField(boolean.class, fieldName);
                    break;
                case DATE:
                    fieldName = AllJavaTypes.FIELD_DATE;
                    schema.addField(Date.class, fieldName);
                    break;
                case BYTE:
                    fieldName = AllJavaTypes.FIELD_BYTE;
                    schema.addField(byte.class, fieldName);
                    break;
                default:
                    fail(fieldType + " failed");
            }
            schema.addIndex(fieldName);
            try {
                schema.addIndex(fieldName);
                fail();
            } catch (IllegalStateException ignored) {
            }
        }
    }

    public void testSetRemoveNullableField() {
        for (FieldType fieldType : FieldType.values()) {
            switch (fieldType) {
                case STRING:
                    schema.addField(String.class, AllJavaTypes.FIELD_STRING);
                    assertTrue(schema.isNullable(AllJavaTypes.FIELD_STRING));
                    schema.setNullable(AllJavaTypes.FIELD_STRING, false);
                    assertFalse(schema.isNullable(AllJavaTypes.FIELD_STRING));
                    break;
                case SHORT:
                    schema.addField(Short.class, AllJavaTypes.FIELD_SHORT);
                    assertTrue(schema.isNullable(AllJavaTypes.FIELD_SHORT));
                    schema.setNullable(AllJavaTypes.FIELD_SHORT, false);
                    assertFalse(schema.isNullable(AllJavaTypes.FIELD_SHORT));
                    break;
                case INT:
                    schema.addField(Integer.class, AllJavaTypes.FIELD_INT);
                    assertTrue(schema.isNullable(AllJavaTypes.FIELD_INT));
                    schema.setNullable(AllJavaTypes.FIELD_INT, false);
                    assertFalse(schema.isNullable(AllJavaTypes.FIELD_INT));
                    break;
                case LONG:
                    schema.addField(Long.class, AllJavaTypes.FIELD_LONG);
                    assertTrue(schema.isNullable(AllJavaTypes.FIELD_LONG));
                    schema.setNullable(AllJavaTypes.FIELD_LONG, false);
                    assertFalse(schema.isNullable(AllJavaTypes.FIELD_LONG));
                    break;
                case BYTE:
                    schema.addField(Byte.class, AllJavaTypes.FIELD_BYTE);
                    assertTrue(schema.isNullable(AllJavaTypes.FIELD_BYTE));
                    schema.setNullable(AllJavaTypes.FIELD_BYTE, false);
                    assertFalse(schema.isNullable(AllJavaTypes.FIELD_BYTE));
                    break;
                case BOOLEAN:
                    schema.addField(Boolean.class, AllJavaTypes.FIELD_BOOLEAN);
                    assertTrue(schema.isNullable(AllJavaTypes.FIELD_BOOLEAN));
                    schema.setNullable(AllJavaTypes.FIELD_BOOLEAN, false);
                    assertFalse(schema.isNullable(AllJavaTypes.FIELD_BOOLEAN));
                    break;
                case FLOAT:
                    schema.addField(Float.class, AllJavaTypes.FIELD_FLOAT);
                    assertTrue(schema.isNullable(AllJavaTypes.FIELD_FLOAT));
                    schema.setNullable(AllJavaTypes.FIELD_FLOAT, false);
                    assertFalse(schema.isNullable(AllJavaTypes.FIELD_FLOAT));
                    break;
                case DOUBLE:
                    schema.addField(Double.class, AllJavaTypes.FIELD_DOUBLE);
                    assertTrue(schema.isNullable(AllJavaTypes.FIELD_DOUBLE));
                    schema.setNullable(AllJavaTypes.FIELD_DOUBLE, false);
                    assertFalse(schema.isNullable(AllJavaTypes.FIELD_DOUBLE));
                    break;
                case BLOB:
                    schema.addField(byte[].class, AllJavaTypes.FIELD_BINARY);
                    assertTrue(schema.isNullable(AllJavaTypes.FIELD_BINARY));
                    schema.setNullable(AllJavaTypes.FIELD_BINARY, false);
                    assertFalse(schema.isNullable(AllJavaTypes.FIELD_SHORT));
                    break;
                case DATE:
                    schema.addField(Date.class, AllJavaTypes.FIELD_DATE);
                    assertTrue(schema.isNullable(AllJavaTypes.FIELD_DATE));
                    schema.setNullable(AllJavaTypes.FIELD_DATE, false);
                    assertFalse(schema.isNullable(AllJavaTypes.FIELD_DATE));
                    break;
                case OBJECT:
                    // Objects are always nullable and cannot be changed.
                    schema.addLinkField(RealmObject.class, AllJavaTypes.FIELD_OBJECT, schema);
                    assertTrue(schema.isNullable(AllJavaTypes.FIELD_OBJECT));
                    try {
                        schema.setNullable(AllJavaTypes.FIELD_OBJECT, false);
                        fail();
                    } catch (IllegalArgumentException ignored) {
                    }
                    break;
                case LIST:
                    // Lists are not nullable and cannot be configured to be so.
                    schema.addLinkField(RealmList.class, AllJavaTypes.FIELD_LIST, schema);
                    assertFalse(schema.isNullable(AllJavaTypes.FIELD_LIST));
                    try {
                        schema.setNullable(AllJavaTypes.FIELD_LIST, true);
                        fail();
                    } catch (IllegalArgumentException ignored) {
                    }
                    break;
                default:
                    fail("Unknown field type: " + fieldType);
            }
        }
    }

    public void testSetRemoveRequiredField() {
        for (FieldType fieldType : FieldType.values()) {
            switch (fieldType) {
                case STRING:
                    schema.addField(String.class, AllJavaTypes.FIELD_STRING, RealmModifier.REQUIRED);
                    assertTrue(schema.isRequired(AllJavaTypes.FIELD_STRING));
                    schema.setRequired(AllJavaTypes.FIELD_STRING, false);
                    assertFalse(schema.isRequired(AllJavaTypes.FIELD_STRING));
                    break;
                case SHORT:
                    schema.addField(short.class, AllJavaTypes.FIELD_SHORT);
                    assertTrue(schema.isRequired(AllJavaTypes.FIELD_SHORT));
                    schema.setRequired(AllJavaTypes.FIELD_SHORT, false);
                    assertFalse(schema.isRequired(AllJavaTypes.FIELD_SHORT));
                    break;
                case INT:
                    schema.addField(int.class, AllJavaTypes.FIELD_INT);
                    assertTrue(schema.isRequired(AllJavaTypes.FIELD_INT));
                    schema.setRequired(AllJavaTypes.FIELD_INT, false);
                    assertFalse(schema.isRequired(AllJavaTypes.FIELD_INT));
                    break;
                case LONG:
                    schema.addField(long.class, AllJavaTypes.FIELD_LONG);
                    assertTrue(schema.isRequired(AllJavaTypes.FIELD_LONG));
                    schema.setRequired(AllJavaTypes.FIELD_LONG, false);
                    assertFalse(schema.isRequired(AllJavaTypes.FIELD_LONG));
                    break;
                case BYTE:
                    schema.addField(byte.class, AllJavaTypes.FIELD_BYTE);
                    assertTrue(schema.isRequired((AllJavaTypes.FIELD_BYTE)));
                    schema.setRequired(AllJavaTypes.FIELD_BYTE, false);
                    assertFalse(schema.isRequired(AllJavaTypes.FIELD_BYTE));
                    break;
                case BOOLEAN:
                    schema.addField(boolean.class, AllJavaTypes.FIELD_BOOLEAN);
                    assertTrue(schema.isRequired(AllJavaTypes.FIELD_BOOLEAN));
                    schema.setRequired(AllJavaTypes.FIELD_BOOLEAN, false);
                    assertFalse(schema.isRequired(AllJavaTypes.FIELD_BOOLEAN));
                    break;
                case FLOAT:
                    schema.addField(float.class, AllJavaTypes.FIELD_FLOAT);
                    assertTrue(schema.isRequired((AllJavaTypes.FIELD_FLOAT)));
                    schema.setRequired(AllJavaTypes.FIELD_FLOAT, false);
                    assertFalse(schema.isRequired(AllJavaTypes.FIELD_FLOAT));
                    break;
                case DOUBLE:
                    schema.addField(double.class, AllJavaTypes.FIELD_DOUBLE);
                    assertTrue(schema.isRequired((AllJavaTypes.FIELD_DOUBLE)));
                    schema.setRequired(AllJavaTypes.FIELD_DOUBLE, false);
                    assertFalse(schema.isRequired(AllJavaTypes.FIELD_DOUBLE));
                    break;
                case BLOB:
                    schema.addField(byte[].class, AllJavaTypes.FIELD_BINARY, RealmModifier.REQUIRED);
                    assertTrue(schema.isRequired((AllJavaTypes.FIELD_BINARY)));
                    schema.setRequired(AllJavaTypes.FIELD_BINARY, false);
                    assertFalse(schema.isRequired(AllJavaTypes.FIELD_BINARY));
                    break;
                case DATE:
                    schema.addField(Date.class, AllJavaTypes.FIELD_DATE, RealmModifier.REQUIRED);
                    assertTrue(schema.isRequired((AllJavaTypes.FIELD_DATE)));
                    schema.setRequired(AllJavaTypes.FIELD_DATE, false);
                    assertFalse(schema.isRequired(AllJavaTypes.FIELD_DATE));
                    break;
                case OBJECT:
                    // Objects are always nullable and cannot be configured otherwise.
                    schema.addLinkField(RealmObject.class, AllJavaTypes.FIELD_OBJECT, schema);
                    assertFalse(schema.isRequired((AllJavaTypes.FIELD_OBJECT)));
                    try {
                        schema.setRequired(AllJavaTypes.FIELD_OBJECT, false);
                        fail();
                    } catch (IllegalArgumentException ignored) {
                    }
                    break;
                case LIST:
                    // Lists are always non-nullable and cannot be configured otherwise.
                    schema.addLinkField(RealmList.class, AllJavaTypes.FIELD_LIST, schema);
                    assertTrue(schema.isRequired((AllJavaTypes.FIELD_LIST)));
                    try {
                        schema.setRequired(AllJavaTypes.FIELD_LIST, true);
                        fail();
                    } catch (IllegalArgumentException ignored) {
                    }
                    break;
                default:
                    fail("Unknown field type: " + fieldType);
            }
        }
    }

    public void testRemovePrimaryKey() {
        String fieldName = AllJavaTypes.FIELD_STRING;
        schema.addField(String.class, fieldName);
        schema.addPrimaryKey(fieldName);
        assertTrue(schema.hasPrimaryKey());
        schema.removePrimaryKey();
        assertFalse(schema.hasPrimaryKey());
    }

    public void testRemoveNonExistingPrimaryKeyThrows() {
        String fieldName = AllJavaTypes.FIELD_STRING;
        schema.addField(String.class, fieldName);
        try {
            schema.removePrimaryKey();
            fail();
        } catch (IllegalStateException ignored) {
        }
    }

    public void testRemoveIndex() {
        String fieldName = AllJavaTypes.FIELD_STRING;
        schema.addField(String.class, fieldName, RealmModifier.INDEXED);
        assertTrue(schema.hasIndex(fieldName));
        schema.removeIndex(fieldName);
        assertFalse(schema.hasIndex(fieldName));
    }

    public void testRemoveNonExistingIndexThrows() {
        String fieldName = AllJavaTypes.FIELD_STRING;
        schema.addField(String.class, fieldName);
        try {
            schema.removeIndex(fieldName);
            fail();
        } catch (IllegalStateException ignored) {
        }
    }

    public void testRemoveField() {
        String fieldName = AllJavaTypes.FIELD_STRING;
        schema.addField(String.class, fieldName);
        assertTrue(schema.hasField(fieldName));
        schema.removeField(fieldName);
        assertFalse(schema.hasField(fieldName));
    }

    public void testRemoveNonExistingFieldThrows() {
        String fieldName = AllJavaTypes.FIELD_STRING;
        try {
            schema.removeField(fieldName);
            fail();
        } catch (IllegalStateException ignored) {
        }
    }

    public void testRenameField() {
        String oldFieldName = "old";
        String newFieldName = "new";
        schema.addField(String.class, oldFieldName);
        assertTrue(schema.hasField(oldFieldName));
        assertFalse(schema.hasField(newFieldName));
        schema.renameField(oldFieldName, newFieldName);
        assertFalse(schema.hasField(oldFieldName));
        assertTrue(schema.hasField(newFieldName));
    }

    public void testRenameNonExistingFieldThrows() {
        String oldFieldName = "old";
        String newFieldName = "new";
        try {
            schema.renameField(oldFieldName, newFieldName);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    public void testRenameFieldToIllegalNameThrows() {
        String oldFieldName = "old";
        String newFieldName = "";
        schema.addField(String.class, oldFieldName);
        try {
            schema.renameField(oldFieldName, newFieldName);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    public void testSetGetClassName() {
        assertEquals("Dog", DOG_SCHEMA.getClassName());
        String newClassName = "Darby";
        DOG_SCHEMA.setClassName(newClassName);
        assertEquals(newClassName, DOG_SCHEMA.getClassName());
        assertTrue(realmSchema.hasClass(newClassName));
    }

    public void testForEach() {
        String className = DOG_SCHEMA.getClassName();
        DynamicRealmObject dog1 = realm.createObject(className);
        dog1.setInt("age", 1);
        DynamicRealmObject dog2 = realm.createObject(className);
        dog2.setInt("age", 2);

        final AtomicInteger totalAge = new AtomicInteger(0);
        DOG_SCHEMA.forEach(new RealmObjectSchema.Transformer() {
            @Override
            public void apply(DynamicRealmObject obj) {
                totalAge.addAndGet(obj.getInt("age"));
            }
        });
        assertEquals(3, totalAge.get());
    }

    private interface FieldRunnable {
        void run(String fieldName);
    }
}
