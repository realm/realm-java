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

    public enum SchemaFieldType {
        SIMPLE, OBJECT, LIST
    }

    public enum FieldType {
        STRING(String.class, true),
        SHORT(Short.class, true), PRIMITIVE_SHORT(short.class, false),
        INT(Integer.class, true), PRIMITIVE_INT(int.class, false),
        LONG(Long.class, true), PRIMITIVE_LONG(long.class, false),
        BYTE(Byte.class, true), PRIMITIVE_BYTE(byte.class, false),
        BOOLEAN(Boolean.class, true), PRIMITIVE_BOOLEAN(boolean.class, false),
        FLOAT(Float.class, true), PRIMITIVE_FLOAT(float.class, false),
        DOUBLE(Double.class, true), PRIMITIVE_DOUBLE(double.class, false),
        BLOB(byte[].class, true),
        DATE(Date.class, true),
        OBJECT(RealmObject.class, false),
        LIST(RealmList.class, false);

        final Class<?> clazz;
        final boolean defaultNullable;

        FieldType(Class<?> clazz, boolean defaultNullable) {
            this.clazz = clazz;
            this.defaultNullable = defaultNullable;
        }

        public Class<?> getType() {
            return clazz;
        }

        public boolean isNullable() {
            return defaultNullable;
        }
    }

    public enum IndexFieldType {
        STRING(String.class),
        SHORT(Short.class), PRIMITIVE_SHORT(short.class),
        INT(Integer.class), PRIMITIVE_INT(int.class),
        LONG(Long.class), PRIMITIVE_LONG(long.class),
        BYTE(Byte.class), PRIMITIVE_BYTE(byte.class),
        BOOLEAN(Boolean.class), PRIMITIVE_BOOLEAN(boolean.class),
        DATE(Date.class);

        Class<?> clazz;

        public Class<?> getType() {
            return clazz;
        }

        IndexFieldType(Class<?> clazz) {
            this.clazz = clazz;
        }
    }

    // TODO These should also be allowed? BOOLEAN, BYTE, DATE
    public enum PrimaryKeyFieldType {
        STRING(String.class),
        SHORT(Short.class), PRIMITIVE_SHORT(short.class),
        INT(Integer.class), PRIMITIVE_INT(int.class),
        LONG(Long.class), PRIMITIVE_LONG(long.class);

        Class<?> clazz;

        public Class<?> getType() {
            return clazz;
        }

        PrimaryKeyFieldType(Class<?> clazz) {
            this.clazz = clazz;
        }
    }

    public void testAddRemoveField() {
        for (FieldType fieldType : FieldType.values()) {
            String fieldName = "foo";
            switch(fieldType) {
                case OBJECT:
                    schema.addRealmObjectField(fieldName, DOG_SCHEMA);
                    checkAddedAndRemovable(fieldName);
                    break;
                case LIST:
                    schema.addRealmListField(fieldName, DOG_SCHEMA);
                    checkAddedAndRemovable(fieldName);
                    break;
                default:
                    // All simple fields
                    schema.addField(fieldName, fieldType.getType());
                    checkAddedAndRemovable(fieldName);
            }
        }
    }

    // Check that field is actually added and that it can be removed again.
    private void checkAddedAndRemovable(String fieldName) {
        assertTrue(schema.hasField(fieldName));
        schema.removeField(fieldName);
        assertFalse(schema.hasField(fieldName));
    }

    public void testAddField_nameAlreadyExistsThrows() {
        for (SchemaFieldType schemaFieldType : SchemaFieldType.values()) {
            switch (schemaFieldType) {
                case SIMPLE:
                    checkAddFieldTwice(AllJavaTypes.FIELD_STRING, new FieldRunnable() {
                        @Override
                        public void run(String fieldName) {
                            schema.addField(fieldName, String.class);
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
                    fail("Unknown type: " + schemaFieldType);
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
        for (SchemaFieldType schemaFieldType: SchemaFieldType.values()) {
            for (String fieldName : fieldNames) {
                try {
                    switch(schemaFieldType) {
                        case SIMPLE: schema.addField(fieldName, String.class); break;
                        case OBJECT: schema.addRealmObjectField(fieldName, DOG_SCHEMA); break;
                        case LIST: schema.addRealmListField(fieldName, DOG_SCHEMA); break;
                        default:
                            fail("Unknown type: " + schemaFieldType);
                    }
                    fail(schemaFieldType + " didn't throw");
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }

    public void testAddPrimaryKeyFieldModifier_alreadyExistsThrows() {
        for (PrimaryKeyFieldType fieldType : PrimaryKeyFieldType.values()) {
            String fieldName = "foo";
            schema.addField(fieldName, fieldType.getType());
            schema.addPrimaryKey(fieldName);
            try {
                schema.addPrimaryKey(fieldName);
                fail();
            } catch (IllegalStateException ignored) {
                schema.removePrimaryKey();
            }
            schema.removeField(fieldName);
        }
    }

    public void testAddPrimaryKeyFieldModifier_illegalFieldTypeThrows() {
        String fieldName = "foo";
        schema.addField(fieldName, double.class);
        try {
            schema.addPrimaryKey(fieldName);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    public void testAddIndexFieldModifier_illegalFieldTypeThrows() {
        String fieldName = "foo";
        schema.addField(fieldName, double.class);
        try {
            schema.addIndex(fieldName);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    public void testAddIndexFieldModifier_alreadyIndexedThrows() {
        for (IndexFieldType fieldType : IndexFieldType.values()) {
            String fieldName = "foo";
            schema.addField(fieldName, fieldType.getType());
            schema.addIndex(fieldName);
            try {
                schema.addIndex(fieldName);
                fail();
            } catch (IllegalStateException ignored) {
            }
            schema.removeField(fieldName);
        }
    }

    public void testSetRemoveNullable() {
        for (FieldType fieldType : FieldType.values()) {
            String fieldName = "foo";
            switch (fieldType) {
                case OBJECT:
                    // Objects are always nullable and cannot be changed.
                    schema.addRealmObjectField(fieldName, schema);
                    assertTrue(schema.isNullable(fieldName));
                    try {
                        schema.setNullable(fieldName, false);
                        fail();
                    } catch (IllegalArgumentException ignored) {
                    }
                    break;
                case LIST:
                    // Lists are not nullable and cannot be configured to be so.
                    schema.addRealmListField(fieldName, schema);
                    assertFalse(schema.isNullable(fieldName));
                    try {
                        schema.setNullable(fieldName, true);
                        fail();
                    } catch (IllegalArgumentException ignored) {
                    }
                    break;
                default:
                    // All simple types
                    schema.addField(fieldName, fieldType.getType());
                    assertEquals(fieldType.isNullable(), schema.isNullable(fieldName));
                    schema.setNullable(fieldName, !fieldType.isNullable());
                    assertEquals(!fieldType.isNullable(), schema.isNullable(fieldName));
            }
            schema.removeField(fieldName);
        }
    }

    public void testSetRemoveRequired() {
        for (FieldType fieldType : FieldType.values()) {
            String fieldName = "foo";
            switch (fieldType) {
                case OBJECT:
                    // Objects are always nullable and cannot be configured otherwise.
                    schema.addRealmObjectField(fieldName, schema);
                    assertFalse(schema.isRequired((fieldName)));
                    try {
                        schema.setRequired(fieldName, false);
                        fail();
                    } catch (IllegalArgumentException ignored) {
                    }
                    break;
                case LIST:
                    // Lists are always non-nullable and cannot be configured otherwise.
                    schema.addRealmListField(fieldName, schema);
                    assertTrue(schema.isRequired((fieldName)));
                    try {
                        schema.setRequired(fieldName, true);
                        fail();
                    } catch (IllegalArgumentException ignored) {
                    }
                    break;
                default:
                    // All simple types
                    schema.addField(fieldName, fieldType.getType());
                    assertEquals(!fieldType.isNullable(), schema.isRequired(fieldName));
                    schema.setRequired(fieldName, fieldType.isNullable());
                    assertEquals(fieldType.isNullable(), schema.isRequired(fieldName));
            }
            schema.removeField(fieldName);
        }
    }

    public void testSetRemovePrimaryKey() {
        for (PrimaryKeyFieldType fieldType : PrimaryKeyFieldType.values()) {
            String fieldName = "foo";
            schema.addField(fieldName, fieldType.getType());
            schema.addPrimaryKey(fieldName);
            assertTrue(schema.hasPrimaryKey());
            schema.removePrimaryKey();
            assertFalse(schema.hasPrimaryKey());
            schema.removeField(fieldName);
        }
    }

    public void testRemoveNonExistingPrimaryKeyThrows() {
        String fieldName = "foo";
        schema.addField(fieldName, String.class);
        try {
            schema.removePrimaryKey();
            fail();
        } catch (IllegalStateException ignored) {
        }
    }

    public void testSetRemoveIndex() {
        for (IndexFieldType fieldType : IndexFieldType.values()) {
            String fieldName = "foo";
            schema.addField(fieldName, fieldType.getType(), FieldAttribute.INDEXED);
            assertTrue(schema.hasIndex(fieldName));
            schema.removeIndex(fieldName);
            assertFalse(schema.hasIndex(fieldName));
            schema.removeField(fieldName);
        }
    }

    public void testRemoveNonExistingIndexThrows() {
        String fieldName = "foo";
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

        DOG_SCHEMA.forEach(new RealmObjectSchema.Transformer() {
            @Override
            public void apply(DynamicRealmObject obj) {
                obj.setInt("age", obj.getInt("age") + 1);
            }
        });
        assertEquals(5, realm.where("Dog").sum("age").intValue());
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
