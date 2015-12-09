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

    public enum InvalidIndexFieldType {
        FLOAT(Float.class), PRIMITIVE_FLOAT(float.class),
        DOUBLE(Double.class), PRIMITIVE_DOUBLE(double.class),
        BLOB(byte[].class),
        OBJECT(RealmObject.class),
        LIST(RealmList.class);

        Class<?> clazz;

        public Class<?> getType() {
            return clazz;
        }

        InvalidIndexFieldType(Class<?> clazz) {
            this.clazz = clazz;
        }
    }

    // TODO These should also be allowed? BOOLEAN, DATE
    public enum PrimaryKeyFieldType {
        STRING(String.class),
        SHORT(Short.class), PRIMITIVE_SHORT(short.class),
        INT(Integer.class), PRIMITIVE_INT(int.class),
        LONG(Long.class), PRIMITIVE_LONG(long.class),
        BYTE(Byte.class), PRIMITIVE_BYTE(byte.class);

        Class<?> clazz;

        public Class<?> getType() {
            return clazz;
        }

        PrimaryKeyFieldType(Class<?> clazz) {
            this.clazz = clazz;
        }
    }

    public enum InvalidPrimaryKeyFieldType {
        BOOLEAN(Boolean.class), PRIMITIVE_BOOLEAN(boolean.class),
        FLOAT(Float.class), PRIMITIVE_FLOAT(float.class),
        DOUBLE(Double.class), PRIMITIVE_DOUBLE(double.class),
        BLOB(byte[].class),
        DATE(Date.class),
        OBJECT(RealmObject.class),
        LIST(RealmList.class);

        Class<?> clazz;

        public Class<?> getType() {
            return clazz;
        }

        InvalidPrimaryKeyFieldType(Class<?> clazz) {
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
        for (SchemaFieldType schemaFieldType : SchemaFieldType.values()) {
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

    public void testRequiredFieldAttribute() {
        for (FieldType fieldType : FieldType.values()) {
            String fieldName = "foo";
            switch (fieldType) {
                case OBJECT: continue; // Not possible
                case LIST: continue; // Not possible
                default:
                    // All simple types
                    schema.addField(fieldName, fieldType.getType(), FieldAttribute.REQUIRED);
                    assertTrue(schema.isRequired(fieldName));
                    schema.removeField(fieldName);
            }
        }
    }

    public void testIndexedFieldAttribute() {
        for (IndexFieldType fieldType : IndexFieldType.values()) {
            String fieldName = "foo";
            switch (fieldType) {
                default:
                    schema.addField(fieldName, fieldType.getType(), FieldAttribute.INDEXED);
                    assertTrue(fieldType + " failed", schema.hasIndex(fieldName));
            }
            schema.removeField(fieldName);
        }
    }

    public void testInvalidIndexedFieldAttributeThrows() {
        for (InvalidIndexFieldType fieldType : InvalidIndexFieldType.values()) {
            String fieldName = "foo";
            try {
                schema.addField(fieldName, fieldType.getType(), FieldAttribute.INDEXED);
                fail(fieldType + " should not be allowed to be indexed");
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public void testPrimaryKeyFieldAttribute() {
        for (PrimaryKeyFieldType fieldType : PrimaryKeyFieldType.values()) {
            String fieldName = "foo";
            schema.addField(fieldName, fieldType.getType(), FieldAttribute.PRIMARY_KEY);
            assertTrue(schema.hasPrimaryKey());
            schema.removeField(fieldName);
        }
    }

    public void testInvalidPrimaryKeyFieldAttributeThrows() {
        for (InvalidPrimaryKeyFieldType fieldType : InvalidPrimaryKeyFieldType.values()) {
            String fieldName = "foo";
            try {
                schema.addField(fieldName, fieldType.getType(), FieldAttribute.PRIMARY_KEY);
                fail(fieldType + " should not be allowed to be a primary key");
            } catch (IllegalArgumentException ignored) {
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
        for (InvalidPrimaryKeyFieldType fieldType : InvalidPrimaryKeyFieldType.values()) {
            switch (fieldType) {
                case OBJECT: schema.addRealmObjectField(fieldName, DOG_SCHEMA); break;
                case LIST: schema.addRealmListField(fieldName, DOG_SCHEMA); break;
                default: schema.addField(fieldName, fieldType.getType());
            }
            try {
                schema.addPrimaryKey(fieldName);
                fail(fieldType + " should not be a legal primary key");
            } catch (IllegalArgumentException ignored) {
            }
            schema.removeField(fieldName);
        }
    }

    public void testAddIndexFieldModifier_illegalFieldTypeThrows() {
        String fieldName = "foo";
        for (InvalidIndexFieldType fieldType : InvalidIndexFieldType.values()) {
            switch (fieldType) {
                case OBJECT: schema.addRealmObjectField(fieldName, DOG_SCHEMA); break;
                case LIST: schema.addRealmListField(fieldName, DOG_SCHEMA); break;
                default: schema.addField(fieldName, fieldType.getType());
            }
            try {
                schema.addIndex(fieldName);
                fail(fieldType + " should not be allowed to be indexed.");
            } catch (IllegalArgumentException ignored) {
            }
            schema.removeField(fieldName);
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
        String fieldName = "foo";
        schema.addField(fieldName, String.class);
        assertTrue(schema.hasField(fieldName));
        schema.removeField(fieldName);
        assertFalse(schema.hasField(fieldName));
    }

    public void testRemoveFieldWithPrimaryKey() {
        String fieldName = "foo";
        schema.addField(fieldName, String.class, FieldAttribute.PRIMARY_KEY);
        assertTrue(schema.hasField(fieldName));
        assertTrue(schema.hasPrimaryKey());
        schema.removeField(fieldName);
        assertFalse(schema.hasPrimaryKey());
    }

    public void testRemoveNonExistingFieldThrows() {
        String fieldName = "foo";
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

    public void testTransform() {
        String className = DOG_SCHEMA.getClassName();
        DynamicRealmObject dog1 = realm.createObject(className);
        dog1.setInt("age", 1);
        DynamicRealmObject dog2 = realm.createObject(className);
        dog2.setInt("age", 2);

        DOG_SCHEMA.transform(new RealmObjectSchema.Function() {
            @Override
            public void apply(DynamicRealmObject obj) {
                obj.setInt("age", obj.getInt("age") + 1);
            }
        });
        assertEquals(5, realm.where("Dog").sum("age").intValue());
    }

    public void testTransformObjectReferences() {
        String className = DOG_SCHEMA.getClassName();
        DynamicRealmObject dog1 = realm.createObject(className);
        dog1.setInt("age", 1);

        DOG_SCHEMA.transform(new RealmObjectSchema.Function() {
            @Override
            public void apply(DynamicRealmObject dog) {
                DynamicRealmObject owner = realm.createObject("Owner");
                owner.setString("name", "John");
                dog.setObject("owner", owner);
            }
        });
        assertEquals("John", realm.where("Dog").findFirst().getObject("owner").getString("name"));
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

    public void testGetFieldType() {
        schema = realmSchema.getSchemaForClass("AllJavaTypes");
        assertEquals(RealmFieldType.STRING, schema.getFieldType(AllJavaTypes.FIELD_STRING));
        assertEquals(RealmFieldType.BINARY, schema.getFieldType(AllJavaTypes.FIELD_BINARY));
        assertEquals(RealmFieldType.BOOLEAN, schema.getFieldType(AllJavaTypes.FIELD_BOOLEAN));
        assertEquals(RealmFieldType.DATE, schema.getFieldType(AllJavaTypes.FIELD_DATE));
        assertEquals(RealmFieldType.DOUBLE, schema.getFieldType(AllJavaTypes.FIELD_DOUBLE));
        assertEquals(RealmFieldType.FLOAT, schema.getFieldType(AllJavaTypes.FIELD_FLOAT));
        assertEquals(RealmFieldType.OBJECT, schema.getFieldType(AllJavaTypes.FIELD_OBJECT));
        assertEquals(RealmFieldType.LIST, schema.getFieldType(AllJavaTypes.FIELD_LIST));
        assertEquals(RealmFieldType.INTEGER, schema.getFieldType(AllJavaTypes.FIELD_BYTE));
        assertEquals(RealmFieldType.INTEGER, schema.getFieldType(AllJavaTypes.FIELD_SHORT));
        assertEquals(RealmFieldType.INTEGER, schema.getFieldType(AllJavaTypes.FIELD_INT));
        assertEquals(RealmFieldType.INTEGER, schema.getFieldType(AllJavaTypes.FIELD_LONG));
    }

    public void testGetFieldTypeThrows() {
        try {
            schema.getFieldType("I don't exists");
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    private interface FieldRunnable {
        void run(String fieldName);
    }
}
