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
import java.util.Set;
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
        DOG_SCHEMA = realmSchema.get("Dog");
        realm.beginTransaction();
        schema = realmSchema.create("NewClass");
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
                    schema.addField(fieldName, String.class);
                    checkAddedAndRemovable(fieldName);
                    break;
                case SHORT:
                    fieldName = AllJavaTypes.FIELD_SHORT;
                    schema.addField(fieldName, short.class);
                    checkAddedAndRemovable(fieldName);
                    break;
                case INT:
                    fieldName = AllJavaTypes.FIELD_INT;
                    schema.addField(fieldName, int.class);
                    checkAddedAndRemovable(fieldName);
                    break;
                case LONG:
                    fieldName = AllJavaTypes.FIELD_LONG;
                    schema.addField(fieldName, long.class);
                    checkAddedAndRemovable(fieldName);
                    break;
                case BYTE:
                    fieldName = AllJavaTypes.FIELD_BYTE;
                    schema.addField(fieldName, byte.class);
                    checkAddedAndRemovable(fieldName);
                    break;
                case BOOLEAN:
                    fieldName = AllJavaTypes.FIELD_BOOLEAN;
                    schema.addField(fieldName, boolean.class);
                    checkAddedAndRemovable(fieldName);
                    break;
                case FLOAT:
                    fieldName = AllJavaTypes.FIELD_FLOAT;
                    schema.addField(fieldName, float.class);
                    checkAddedAndRemovable(fieldName);
                    break;
                case DOUBLE:
                    fieldName = AllJavaTypes.FIELD_DOUBLE;
                    schema.addField(fieldName, double.class);
                    checkAddedAndRemovable(fieldName);
                    break;
                case BLOB:
                    fieldName = AllJavaTypes.FIELD_BINARY;
                    schema.addField(fieldName, byte[].class);
                    checkAddedAndRemovable(fieldName);
                    break;
                case DATE:
                    fieldName = AllJavaTypes.FIELD_DATE;
                    schema.addField(fieldName, Date.class);
                    checkAddedAndRemovable(fieldName);
                    break;
                case OBJECT:
                    fieldName = AllJavaTypes.FIELD_OBJECT;
                    schema.addRealmObjectField(fieldName, DOG_SCHEMA);
                    checkAddedAndRemovable(fieldName);
                    break;
                case LIST:
                    fieldName = AllJavaTypes.FIELD_LIST;
                    schema.addRealmListField(fieldName, DOG_SCHEMA);
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
                    schema.addField(fieldName, String.class, RealmModifier.INDEXED);
                    checkAddedAndRemovable(fieldName);
                    break;
                case SHORT:
                    fieldName = AllJavaTypes.FIELD_SHORT;
                    schema.addField(fieldName, short.class, RealmModifier.INDEXED);
                    checkAddedAndRemovable(fieldName);
                    break;
                case INT:
                    fieldName = AllJavaTypes.FIELD_INT;
                    schema.addField(fieldName, int.class, RealmModifier.INDEXED);
                    checkAddedAndRemovable(fieldName);
                    break;
                case LONG:
                    fieldName = AllJavaTypes.FIELD_LONG;
                    schema.addField(fieldName, long.class, RealmModifier.INDEXED);
                    checkAddedAndRemovable(fieldName);
                    break;
                case BYTE:
                    fieldName = AllJavaTypes.FIELD_BYTE;
                    schema.addField(fieldName, byte.class, RealmModifier.INDEXED);
                    checkAddedAndRemovable(fieldName);
                    break;
                case BOOLEAN:
                    fieldName = AllJavaTypes.FIELD_BOOLEAN;
                    schema.addField(fieldName, boolean.class, RealmModifier.INDEXED);
                    checkAddedAndRemovable(fieldName);
                    break;
                case DATE:
                    fieldName = AllJavaTypes.FIELD_DATE;
                    schema.addField(fieldName, Date.class, RealmModifier.INDEXED);
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
                    schema.addField(fieldName, String.class, RealmModifier.PRIMARY_KEY);
                    checkAddedAndRemovable(fieldName);
                    break;
                case SHORT:
                    fieldName = AllJavaTypes.FIELD_SHORT;
                    schema.addField(fieldName, short.class, RealmModifier.PRIMARY_KEY);
                    checkAddedAndRemovable(fieldName);
                    break;
                case INT:
                    fieldName = AllJavaTypes.FIELD_INT;
                    schema.addField(fieldName, int.class, RealmModifier.PRIMARY_KEY);
                    checkAddedAndRemovable(fieldName);
                    break;
                case LONG:
                    fieldName = AllJavaTypes.FIELD_LONG;
                    schema.addField(fieldName, long.class, RealmModifier.PRIMARY_KEY);
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
                            schema.addField(fieldName, String.class);
                        }
                    });
                    break;
                case SHORT:
                    checkAddFieldTwice(AllJavaTypes.FIELD_SHORT, new FieldRunnable() {
                        @Override
                        public void run(String fieldName) {
                            schema.addField(fieldName, short.class);
                        }
                    });
                    break;
                case INT:
                    checkAddFieldTwice(AllJavaTypes.FIELD_INT, new FieldRunnable() {
                        @Override
                        public void run(String fieldName) {
                            schema.addField(fieldName, int.class);
                        }
                    });
                    break;
                case LONG:
                    checkAddFieldTwice(AllJavaTypes.FIELD_LONG, new FieldRunnable() {
                        @Override
                        public void run(String fieldName) {
                            schema.addField(fieldName, long.class);
                        }
                    });
                    break;
                case BYTE:
                    checkAddFieldTwice(AllJavaTypes.FIELD_BYTE, new FieldRunnable() {
                        @Override
                        public void run(String fieldName) {
                            schema.addField(fieldName, byte.class);
                        }
                    });
                    break;
                case BOOLEAN:
                    checkAddFieldTwice(AllJavaTypes.FIELD_BOOLEAN, new FieldRunnable() {
                        @Override
                        public void run(String fieldName) {
                            schema.addField(fieldName, boolean.class);
                        }
                    });
                    break;
                case FLOAT:
                    checkAddFieldTwice(AllJavaTypes.FIELD_FLOAT, new FieldRunnable() {
                        @Override
                        public void run(String fieldName) {
                            schema.addField(fieldName, float.class);
                        }
                    });
                    break;
                case DOUBLE:
                    checkAddFieldTwice(AllJavaTypes.FIELD_DOUBLE, new FieldRunnable() {
                        @Override
                        public void run(String fieldName) {
                            schema.addField(fieldName, double.class);
                        }
                    });
                    break;
                case BLOB:
                    checkAddFieldTwice(AllJavaTypes.FIELD_BINARY, new FieldRunnable() {
                        @Override
                        public void run(String fieldName) {
                            schema.addField(fieldName, byte[].class);
                        }
                    });
                    break;
                case DATE:
                    checkAddFieldTwice(AllJavaTypes.FIELD_DATE, new FieldRunnable() {
                        @Override
                        public void run(String fieldName) {
                            schema.addField(fieldName, Date.class);
                        }
                    });
                    break;
                case OBJECT:
                    checkAddFieldTwice(AllJavaTypes.FIELD_OBJECT, new FieldRunnable() {
                        @Override
                        public void run(String fieldName) {
                            schema.addRealmObjectField(fieldName, DOG_SCHEMA);
                        }
                    });
                    break;
                case LIST:
                    checkAddFieldTwice(AllJavaTypes.FIELD_LIST, new FieldRunnable() {
                        @Override
                        public void run(String fieldName) {
                            schema.addRealmListField(fieldName, DOG_SCHEMA);
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
                        case STRING: schema.addField(fieldName, String.class); break;
                        case SHORT: schema.addField(fieldName, short.class); break;
                        case INT: schema.addField(fieldName, int.class); break;
                        case LONG: schema.addField(fieldName, long.class); break;
                        case BYTE: schema.addField(fieldName, byte.class); break;
                        case BOOLEAN: schema.addField(fieldName, boolean.class); break;
                        case FLOAT: schema.addField(fieldName, float.class); break;
                        case DOUBLE: schema.addField(fieldName, double.class); break;
                        case BLOB: schema.addField(fieldName, byte[].class); break;
                        case DATE: schema.addField(fieldName, Date.class); break;
                        case OBJECT: schema.addRealmObjectField(fieldName, DOG_SCHEMA); break;
                        case LIST: schema.addRealmListField(fieldName, DOG_SCHEMA); break;
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
                    schema.addField(fieldName, String.class);
                    break;
                case SHORT:
                    fieldName = AllJavaTypes.FIELD_SHORT;
                    schema.addField(fieldName, short.class);
                    break;
                case INT:
                    fieldName = AllJavaTypes.FIELD_INT;
                    schema.addField(fieldName, int.class);
                    break;
                case LONG:
                    fieldName = AllJavaTypes.FIELD_LONG;
                    schema.addField(fieldName, long.class);
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
        schema.addField(fieldName, double.class);
        try {
            schema.addPrimaryKey(fieldName);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    public void testAddIndexFieldModifier_illegalFieldTypeThrows() {
        String fieldName = AllJavaTypes.FIELD_DOUBLE;
        schema.addField(fieldName, double.class);
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
                    schema.addField(fieldName, String.class);
                    break;
                case SHORT:
                    fieldName = AllJavaTypes.FIELD_SHORT;
                    schema.addField(fieldName, short.class);
                    break;
                case INT:
                    fieldName = AllJavaTypes.FIELD_INT;
                    schema.addField(fieldName, int.class);
                    break;
                case LONG:
                    fieldName = AllJavaTypes.FIELD_LONG;
                    schema.addField(fieldName, long.class);
                    break;
                case BOOLEAN:
                    fieldName = AllJavaTypes.FIELD_BOOLEAN;
                    schema.addField(fieldName, boolean.class);
                    break;
                case DATE:
                    fieldName = AllJavaTypes.FIELD_DATE;
                    schema.addField(fieldName, Date.class);
                    break;
                case BYTE:
                    fieldName = AllJavaTypes.FIELD_BYTE;
                    schema.addField(fieldName, byte.class);
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
                    schema.addField(AllJavaTypes.FIELD_STRING, String.class);
                    assertTrue(schema.isNullable(AllJavaTypes.FIELD_STRING));
                    schema.setNullable(AllJavaTypes.FIELD_STRING, false);
                    assertFalse(schema.isNullable(AllJavaTypes.FIELD_STRING));
                    break;
                case SHORT:
                    schema.addField(AllJavaTypes.FIELD_SHORT, Short.class);
                    assertTrue(schema.isNullable(AllJavaTypes.FIELD_SHORT));
                    schema.setNullable(AllJavaTypes.FIELD_SHORT, false);
                    assertFalse(schema.isNullable(AllJavaTypes.FIELD_SHORT));
                    break;
                case INT:
                    schema.addField(AllJavaTypes.FIELD_INT, Integer.class);
                    assertTrue(schema.isNullable(AllJavaTypes.FIELD_INT));
                    schema.setNullable(AllJavaTypes.FIELD_INT, false);
                    assertFalse(schema.isNullable(AllJavaTypes.FIELD_INT));
                    break;
                case LONG:
                    schema.addField(AllJavaTypes.FIELD_LONG, Long.class);
                    assertTrue(schema.isNullable(AllJavaTypes.FIELD_LONG));
                    schema.setNullable(AllJavaTypes.FIELD_LONG, false);
                    assertFalse(schema.isNullable(AllJavaTypes.FIELD_LONG));
                    break;
                case BYTE:
                    schema.addField(AllJavaTypes.FIELD_BYTE, Byte.class);
                    assertTrue(schema.isNullable(AllJavaTypes.FIELD_BYTE));
                    schema.setNullable(AllJavaTypes.FIELD_BYTE, false);
                    assertFalse(schema.isNullable(AllJavaTypes.FIELD_BYTE));
                    break;
                case BOOLEAN:
                    schema.addField(AllJavaTypes.FIELD_BOOLEAN, Boolean.class);
                    assertTrue(schema.isNullable(AllJavaTypes.FIELD_BOOLEAN));
                    schema.setNullable(AllJavaTypes.FIELD_BOOLEAN, false);
                    assertFalse(schema.isNullable(AllJavaTypes.FIELD_BOOLEAN));
                    break;
                case FLOAT:
                    schema.addField(AllJavaTypes.FIELD_FLOAT, Float.class);
                    assertTrue(schema.isNullable(AllJavaTypes.FIELD_FLOAT));
                    schema.setNullable(AllJavaTypes.FIELD_FLOAT, false);
                    assertFalse(schema.isNullable(AllJavaTypes.FIELD_FLOAT));
                    break;
                case DOUBLE:
                    schema.addField(AllJavaTypes.FIELD_DOUBLE, Double.class);
                    assertTrue(schema.isNullable(AllJavaTypes.FIELD_DOUBLE));
                    schema.setNullable(AllJavaTypes.FIELD_DOUBLE, false);
                    assertFalse(schema.isNullable(AllJavaTypes.FIELD_DOUBLE));
                    break;
                case BLOB:
                    schema.addField(AllJavaTypes.FIELD_BINARY, byte[].class);
                    assertTrue(schema.isNullable(AllJavaTypes.FIELD_BINARY));
                    schema.setNullable(AllJavaTypes.FIELD_BINARY, false);
                    assertFalse(schema.isNullable(AllJavaTypes.FIELD_SHORT));
                    break;
                case DATE:
                    schema.addField(AllJavaTypes.FIELD_DATE, Date.class);
                    assertTrue(schema.isNullable(AllJavaTypes.FIELD_DATE));
                    schema.setNullable(AllJavaTypes.FIELD_DATE, false);
                    assertFalse(schema.isNullable(AllJavaTypes.FIELD_DATE));
                    break;
                case OBJECT:
                    // Objects are always nullable and cannot be changed.
                    schema.addRealmObjectField(AllJavaTypes.FIELD_OBJECT, schema);
                    assertTrue(schema.isNullable(AllJavaTypes.FIELD_OBJECT));
                    try {
                        schema.setNullable(AllJavaTypes.FIELD_OBJECT, false);
                        fail();
                    } catch (IllegalArgumentException ignored) {
                    }
                    break;
                case LIST:
                    // Lists are not nullable and cannot be configured to be so.
                    schema.addRealmListField(AllJavaTypes.FIELD_LIST, schema);
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
                    schema.addField(AllJavaTypes.FIELD_STRING, String.class, RealmModifier.REQUIRED);
                    assertTrue(schema.isRequired(AllJavaTypes.FIELD_STRING));
                    schema.setRequired(AllJavaTypes.FIELD_STRING, false);
                    assertFalse(schema.isRequired(AllJavaTypes.FIELD_STRING));
                    break;
                case SHORT:
                    schema.addField(AllJavaTypes.FIELD_SHORT, short.class);
                    assertTrue(schema.isRequired(AllJavaTypes.FIELD_SHORT));
                    schema.setRequired(AllJavaTypes.FIELD_SHORT, false);
                    assertFalse(schema.isRequired(AllJavaTypes.FIELD_SHORT));
                    break;
                case INT:
                    schema.addField(AllJavaTypes.FIELD_INT, int.class);
                    assertTrue(schema.isRequired(AllJavaTypes.FIELD_INT));
                    schema.setRequired(AllJavaTypes.FIELD_INT, false);
                    assertFalse(schema.isRequired(AllJavaTypes.FIELD_INT));
                    break;
                case LONG:
                    schema.addField(AllJavaTypes.FIELD_LONG, long.class);
                    assertTrue(schema.isRequired(AllJavaTypes.FIELD_LONG));
                    schema.setRequired(AllJavaTypes.FIELD_LONG, false);
                    assertFalse(schema.isRequired(AllJavaTypes.FIELD_LONG));
                    break;
                case BYTE:
                    schema.addField(AllJavaTypes.FIELD_BYTE, byte.class);
                    assertTrue(schema.isRequired((AllJavaTypes.FIELD_BYTE)));
                    schema.setRequired(AllJavaTypes.FIELD_BYTE, false);
                    assertFalse(schema.isRequired(AllJavaTypes.FIELD_BYTE));
                    break;
                case BOOLEAN:
                    schema.addField(AllJavaTypes.FIELD_BOOLEAN, boolean.class);
                    assertTrue(schema.isRequired(AllJavaTypes.FIELD_BOOLEAN));
                    schema.setRequired(AllJavaTypes.FIELD_BOOLEAN, false);
                    assertFalse(schema.isRequired(AllJavaTypes.FIELD_BOOLEAN));
                    break;
                case FLOAT:
                    schema.addField(AllJavaTypes.FIELD_FLOAT, float.class);
                    assertTrue(schema.isRequired((AllJavaTypes.FIELD_FLOAT)));
                    schema.setRequired(AllJavaTypes.FIELD_FLOAT, false);
                    assertFalse(schema.isRequired(AllJavaTypes.FIELD_FLOAT));
                    break;
                case DOUBLE:
                    schema.addField(AllJavaTypes.FIELD_DOUBLE, double.class);
                    assertTrue(schema.isRequired((AllJavaTypes.FIELD_DOUBLE)));
                    schema.setRequired(AllJavaTypes.FIELD_DOUBLE, false);
                    assertFalse(schema.isRequired(AllJavaTypes.FIELD_DOUBLE));
                    break;
                case BLOB:
                    schema.addField(AllJavaTypes.FIELD_BINARY, byte[].class, RealmModifier.REQUIRED);
                    assertTrue(schema.isRequired((AllJavaTypes.FIELD_BINARY)));
                    schema.setRequired(AllJavaTypes.FIELD_BINARY, false);
                    assertFalse(schema.isRequired(AllJavaTypes.FIELD_BINARY));
                    break;
                case DATE:
                    schema.addField(AllJavaTypes.FIELD_DATE, Date.class, RealmModifier.REQUIRED);
                    assertTrue(schema.isRequired((AllJavaTypes.FIELD_DATE)));
                    schema.setRequired(AllJavaTypes.FIELD_DATE, false);
                    assertFalse(schema.isRequired(AllJavaTypes.FIELD_DATE));
                    break;
                case OBJECT:
                    // Objects are always nullable and cannot be configured otherwise.
                    schema.addRealmObjectField(AllJavaTypes.FIELD_OBJECT, schema);
                    assertFalse(schema.isRequired((AllJavaTypes.FIELD_OBJECT)));
                    try {
                        schema.setRequired(AllJavaTypes.FIELD_OBJECT, false);
                        fail();
                    } catch (IllegalArgumentException ignored) {
                    }
                    break;
                case LIST:
                    // Lists are always non-nullable and cannot be configured otherwise.
                    schema.addRealmListField(AllJavaTypes.FIELD_LIST, schema);
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
        schema.addField(fieldName, String.class);
        schema.addPrimaryKey(fieldName);
        assertTrue(schema.hasPrimaryKey());
        schema.removePrimaryKey();
        assertFalse(schema.hasPrimaryKey());
    }

    public void testRemoveNonExistingPrimaryKeyThrows() {
        String fieldName = AllJavaTypes.FIELD_STRING;
        schema.addField(fieldName, String.class);
        try {
            schema.removePrimaryKey();
            fail();
        } catch (IllegalStateException ignored) {
        }
    }

    public void testRemoveIndex() {
        String fieldName = AllJavaTypes.FIELD_STRING;
        schema.addField(fieldName, String.class, RealmModifier.INDEXED);
        assertTrue(schema.hasIndex(fieldName));
        schema.removeIndex(fieldName);
        assertFalse(schema.hasIndex(fieldName));
    }

    public void testRemoveNonExistingIndexThrows() {
        String fieldName = AllJavaTypes.FIELD_STRING;
        schema.addField(fieldName, String.class);
        try {
            schema.removeIndex(fieldName);
            fail();
        } catch (IllegalStateException ignored) {
        }
    }

    public void testRemoveField() {
        String fieldName = AllJavaTypes.FIELD_STRING;
        schema.addField(fieldName, String.class);
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
        schema.addField(oldFieldName, String.class);
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
        schema.addField(oldFieldName, String.class);
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
        assertTrue(realmSchema.contains(newClassName));
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

    public void testGetFieldNames() {
        Set<String> fieldNames = DOG_SCHEMA.getFieldNames();
        assertEquals(7, fieldNames.size());
        assertTrue(fieldNames.contains("name"));
        assertTrue(fieldNames.contains("age"));
        assertTrue(fieldNames.contains("height"));
        assertTrue(fieldNames.contains("weight"));
        assertTrue(fieldNames.contains("hasTail"));
        assertTrue(fieldNames.contains("birthday"));
        assertTrue(fieldNames.contains("owner"));
    }

    private interface FieldRunnable {
        void run(String fieldName);
    }
}
