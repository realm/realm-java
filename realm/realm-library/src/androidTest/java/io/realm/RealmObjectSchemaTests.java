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
import io.realm.entities.AllJavaTypes;
import io.realm.rule.TestRealmConfigurationFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class RealmObjectSchemaTests {

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private RealmObjectSchema DOG_SCHEMA;
    private DynamicRealm realm;
    private RealmObjectSchema schema;
    private RealmSchema realmSchema;

    @Before
    public void setUp() {
        RealmConfiguration realmConfig = configFactory.createConfiguration();
        Realm.getInstance(realmConfig).close(); // Create Schema
        realm = DynamicRealm.getInstance(realmConfig);
        realmSchema = realm.getSchema();
        DOG_SCHEMA = realmSchema.get("Dog");
        realm.beginTransaction();
        schema = realmSchema.create("NewClass");
    }

    @After
    public void tearDown() {
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

    @Test
    public void addRemoveField() {
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

    @Test
    public void addField_nameAlreadyExistsThrows() {
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


    @Test
    public void addField_illegalFieldNameThrows() {
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

    @Test
    public void requiredFieldAttribute() {
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

    @Test
    public void indexedFieldAttribute() {
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

    @Test
    public void invalidIndexedFieldAttributeThrows() {
        for (InvalidIndexFieldType fieldType : InvalidIndexFieldType.values()) {
            String fieldName = "foo";
            try {
                schema.addField(fieldName, fieldType.getType(), FieldAttribute.INDEXED);
                fail(fieldType + " should not be allowed to be indexed");
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @Test
    public void primaryKeyFieldAttribute() {
        for (PrimaryKeyFieldType fieldType : PrimaryKeyFieldType.values()) {
            String fieldName = "foo";
            schema.addField(fieldName, fieldType.getType(), FieldAttribute.PRIMARY_KEY);
            assertTrue(schema.hasPrimaryKey());
            assertTrue(schema.isPrimaryKey(fieldName));
            assertEquals(fieldName, schema.getPrimaryKey());
            switch (fieldType) {
                case BYTE:
                case SHORT:
                case INT:
                case LONG:
                case STRING:
                    assertTrue(schema.isNullable(fieldName));
                    break;
                default:
                    assertFalse(schema.isNullable(fieldName));
            }
            schema.removeField(fieldName);
        }
    }

    @Test
    public void invalidPrimaryKeyFieldAttributeThrows() {
        for (InvalidPrimaryKeyFieldType fieldType : InvalidPrimaryKeyFieldType.values()) {
            String fieldName = "foo";
            try {
                schema.addField(fieldName, fieldType.getType(), FieldAttribute.PRIMARY_KEY);
                fail(fieldType + " should not be allowed to be a primary key");
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @Test
    public void addPrimaryKeyFieldModifier_alreadyExistsThrows() {
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

    @Test
    public void addPrimaryKeyFieldModifier_illegalFieldTypeThrows() {
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

    @Test
    public void addPrimaryKeyFieldModifier_duplicateValues() {
        for (PrimaryKeyFieldType fieldType : PrimaryKeyFieldType.values()) {
            final String fieldName = "foo";
            schema.addField(fieldName, fieldType.getType());

            // create multiple objects with same values.
            realm.createObject(schema.getClassName());
            realm.createObject(schema.getClassName());

            try {
                schema.addPrimaryKey(fieldName);
                fail();
            } catch (IllegalArgumentException e) {
                // check if message reports correct field name.
                assertTrue(e.getMessage().contains("\"" + fieldName + "\""));
            }
            schema.removeField(fieldName);
        }
    }

    @Test
    public void addIndexFieldModifier_illegalFieldTypeThrows() {
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

    @Test
    public void addIndexFieldModifier_alreadyIndexedThrows() {
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

    @Test
    public void setRemoveNullable() {
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

    @Test
    public void setRemoveRequired() {
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

    @Test
    public void setRemovePrimaryKey() {
        for (PrimaryKeyFieldType fieldType : PrimaryKeyFieldType.values()) {
            String fieldName = "foo";
            schema.addField(fieldName, fieldType.getType());
            schema.addPrimaryKey(fieldName);
            assertTrue(schema.hasPrimaryKey());
            assertTrue(schema.isPrimaryKey(fieldName));
            schema.removePrimaryKey();
            assertFalse(schema.hasPrimaryKey());
            assertFalse(schema.isPrimaryKey(fieldName));
            schema.removeField(fieldName);
        }
    }

    @Test
    public void removeNonExistingPrimaryKeyThrows() {
        String fieldName = "foo";
        schema.addField(fieldName, String.class);

        thrown.expect(IllegalStateException.class);
        schema.removePrimaryKey();
    }

    @Test
    public void setRemoveIndex() {
        for (IndexFieldType fieldType : IndexFieldType.values()) {
            String fieldName = "foo";
            schema.addField(fieldName, fieldType.getType(), FieldAttribute.INDEXED);
            assertTrue(schema.hasIndex(fieldName));
            schema.removeIndex(fieldName);
            assertFalse(schema.hasIndex(fieldName));
            schema.removeField(fieldName);
        }
    }

    @Test
    public void removeNonExistingIndexThrows() {
        String fieldName = "foo";
        schema.addField(fieldName, String.class);

        thrown.expect(IllegalStateException.class);
        schema.removeIndex(fieldName);
    }

    @Test
    public void removeField() {
        String fieldName = "foo";
        schema.addField(fieldName, String.class);
        assertTrue(schema.hasField(fieldName));
        schema.removeField(fieldName);
        assertFalse(schema.hasField(fieldName));
    }

    @Test
    public void removeField_withPrimaryKey() {
        String fieldName = "foo";
        schema.addField(fieldName, String.class, FieldAttribute.PRIMARY_KEY);
        assertTrue(schema.hasField(fieldName));
        assertTrue(schema.hasPrimaryKey());
        assertTrue(schema.isPrimaryKey(fieldName));
        schema.removeField(fieldName);
        assertFalse(schema.hasPrimaryKey());
    }

    @Test
    public void removeField_nonExistingFieldThrows() {
        String fieldName = "foo";

        thrown.expect(IllegalStateException.class);
        schema.removeField(fieldName);
    }

    @Test
    public void renameField() {
        String oldFieldName = "old";
        String newFieldName = "new";
        schema.addField(oldFieldName, String.class);
        assertTrue(schema.hasField(oldFieldName));
        assertFalse(schema.hasField(newFieldName));
        schema.renameField(oldFieldName, newFieldName);
        assertFalse(schema.hasField(oldFieldName));
        assertTrue(schema.hasField(newFieldName));
    }

    @Test
    public void renameField_nonExistingFieldThrows() {
        String oldFieldName = "old";
        String newFieldName = "new";

        thrown.expect(IllegalArgumentException.class);
        schema.renameField(oldFieldName, newFieldName);
    }

    @Test
    public void renameField_toIllegalNameThrows() {
        String oldFieldName = "old";
        String newFieldName = "";
        schema.addField(oldFieldName, String.class);

        thrown.expect(IllegalArgumentException.class);
        schema.renameField(oldFieldName, newFieldName);
    }

    @Test
    public void renameField_withPrimaryKey() {
        String fieldName = "foo";
        schema.addField(fieldName, String.class, FieldAttribute.PRIMARY_KEY);
        assertTrue(schema.hasField(fieldName));
        assertTrue(schema.hasPrimaryKey());
        assertTrue(schema.isPrimaryKey(fieldName));

        schema.renameField(fieldName, "bar");
        assertTrue(schema.hasPrimaryKey());

        assertEquals("bar", schema.getPrimaryKey());
    }

    @Test
    public void setGetClassName() {
        assertEquals("Dog", DOG_SCHEMA.getClassName());
        String newClassName = "Darby";
        DOG_SCHEMA.setClassName(newClassName);
        assertEquals(newClassName, DOG_SCHEMA.getClassName());
        assertTrue(realmSchema.contains(newClassName));
    }

    @Test
    public void transform() {
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

    @Test
    public void transformObjectReferences() {
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
        //noinspection ConstantConditions
        assertEquals("John", realm.where("Dog").findFirst().getObject("owner").getString("name"));
    }

    @Test
    public void getFieldNames() {
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

    @Test
    public void getFieldType() {
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

    @Test(expected = IllegalArgumentException.class)
    public void getFieldType_Throws() {
        schema.getFieldType("I don't exist");
    }

    @Test(expected = IllegalArgumentException.class)
    public void hasIndex_nonExistFieldThrows() {
        schema.hasIndex("I don't exist");
    }

    @Test(expected = IllegalArgumentException.class)
    public void isRequired_nonExistFieldThrows() {
        schema.isRequired("I don't exist");
    }

    @Test(expected = IllegalArgumentException.class)
    public void isNullable_nonExistFieldThrows() {
        schema.isNullable("I don't exist");
    }

    @Test(expected = IllegalArgumentException.class)
    public void isPrimaryKey_nonExistFieldThrows() {
        schema.isPrimaryKey("I don't exist");
    }

    @Test(expected = IllegalStateException.class)
    public void getPrimaryKey_nonExistFieldThrows() {
        schema.getPrimaryKey();
    }

    private interface FieldRunnable {
        void run(String fieldName);
    }
}
