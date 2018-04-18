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

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import io.realm.entities.AllJavaTypes;
import io.realm.entities.Dog;
import io.realm.entities.NonLatinFieldNames;
import io.realm.internal.Table;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public class RealmObjectSchemaTests {

    private enum ObjectSchemaType {
        MUTABLE, IMMUTABLE
    }

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private RealmObjectSchema DOG_SCHEMA;
    private BaseRealm realm;
    private RealmObjectSchema schema;
    private RealmSchema realmSchema;
    private ObjectSchemaType type;

    @Parameterized.Parameters(name = "{0}")
    public static List<ObjectSchemaType> data() {
        return Arrays.asList(ObjectSchemaType.values());
    }

    public RealmObjectSchemaTests(ObjectSchemaType type) {
        this.type = type;
    }

    @Before
    public void setUp() {
        RealmConfiguration realmConfig = configFactory.createConfiguration();
        Realm.getInstance(realmConfig).close(); // Creates Schema.

        realm = DynamicRealm.getInstance(realmConfig);
        realm.beginTransaction();
        realm.getSchema().create("NewClass");
        realm.commitTransaction();
        realm.close();

        if (type == ObjectSchemaType.MUTABLE)  {
            realm = DynamicRealm.getInstance(realmConfig);
            realmSchema = realm.getSchema();
            DOG_SCHEMA = realmSchema.get("Dog");
            schema = realmSchema.get("NewClass");
        } else {
            realm = Realm.getInstance(realmConfig);
            realmSchema = realm.getSchema();
            DOG_SCHEMA = realmSchema.get("Dog");
            schema = realmSchema.get("Dog");
        }
        realm.beginTransaction();
    }

    @After
    public void tearDown() {
        if (realm.isInTransaction()) {
            realm.cancelTransaction();
        }
        realm.close();
    }

    public enum SchemaFieldType {
        SIMPLE, OBJECT, LIST
    }

    // Enumerate all standard field types
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
        OBJECT(RealmObject.class, false);

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

    // Enumerate all list types
    public enum FieldListType {
        STRING_LIST(String.class, true),
        SHORT_LIST(Short.class, true), PRIMITIVE_SHORT_LIST(short.class, false),
        INT_LIST(Integer.class, true), PRIMITIVE_INT_LIST(int.class, false),
        LONG_LIST(Long.class, true), PRIMITIVE_LONG_LIST(long.class, false),
        BYTE_LIST(Byte.class, true), PRIMITIVE_BYTE_LIST(byte.class, false),
        BOOLEAN_LIST(Boolean.class, true), PRIMITIVE_BOOLEAN_LIST(boolean.class, false),
        FLOAT_LIST(Float.class, true), PRIMITIVE_FLOAT_LIST(float.class, false),
        DOUBLE_LIST(Double.class, true), PRIMITIVE_DOUBLE_LIST(double.class, false),
        BLOB_LIST(byte[].class, true),
        DATE_LIST(Date.class, true),
        LIST(RealmList.class, false); // List of Realm Objects

        final Class<?> clazz;
        final boolean defaultNullable;

        FieldListType(Class<?> clazz, boolean defaultNullable) {
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
        STRING(String.class, true),
        SHORT(Short.class, true), PRIMITIVE_SHORT(short.class, false),
        INT(Integer.class, true), PRIMITIVE_INT(int.class, false),
        LONG(Long.class, true), PRIMITIVE_LONG(long.class, false),
        BYTE(Byte.class, true), PRIMITIVE_BYTE(byte.class, false),
        BOOLEAN(Boolean.class, true), PRIMITIVE_BOOLEAN(boolean.class, false),
        DATE(Date.class, true);

        private final Class<?> clazz;
        private final boolean nullable;

        public Class<?> getType() {
            return clazz;
        }

        public boolean isNullable() {
            return nullable;
        }

        IndexFieldType(Class<?> clazz, boolean nullable) {
            this.clazz = clazz;
            this.nullable = nullable;
        }
    }

    public enum InvalidIndexFieldType {
        FLOAT(Float.class), PRIMITIVE_FLOAT(float.class),
        DOUBLE(Double.class), PRIMITIVE_DOUBLE(double.class),
        BLOB(byte[].class),
        OBJECT(RealmObject.class),
        LIST(RealmList.class);

        private final Class<?> clazz;

        public Class<?> getType() {
            return clazz;
        }

        InvalidIndexFieldType(Class<?> clazz) {
            this.clazz = clazz;
        }
    }

    // TODO These should also be allowed? BOOLEAN, DATE
    public enum PrimaryKeyFieldType {
        STRING(String.class, true),
        SHORT(Short.class, true), PRIMITIVE_SHORT(short.class, false),
        INT(Integer.class, true), PRIMITIVE_INT(int.class, false),
        LONG(Long.class, true), PRIMITIVE_LONG(long.class, false),
        BYTE(Byte.class, true), PRIMITIVE_BYTE(byte.class, false);

        private final Class<?> clazz;
        private final boolean nullable;

        public Class<?> getType() {
            return clazz;
        }

        public boolean isNullable() {
            return nullable;
        }

        PrimaryKeyFieldType(Class<?> clazz, boolean nullable) {
            this.clazz = clazz;
            this.nullable = nullable;
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

        private final Class<?> clazz;

        public Class<?> getType() {
            return clazz;
        }

        InvalidPrimaryKeyFieldType(Class<?> clazz) {
            this.clazz = clazz;
        }
    }

    @Test
    public void addRemoveField() {
        if (type == ObjectSchemaType.IMMUTABLE) {
            try {
                schema.addField("test", int.class);
                fail();
            } catch (UnsupportedOperationException ignore) {
            }
            try {
                schema.addRealmObjectField("test", DOG_SCHEMA);
                fail();
            } catch (UnsupportedOperationException ignore) {
            }
            try {
                schema.addRealmListField("test", DOG_SCHEMA);
                fail();
            } catch (UnsupportedOperationException ignore) {
            }

            try {
                schema.removeField("test");
                fail();
            } catch (UnsupportedOperationException ignore) {
            }
            return;
        }
        String fieldName = "foo";
        for (FieldType fieldType : FieldType.values()) {
            switch (fieldType) {
                case OBJECT:
                    schema.addRealmObjectField(fieldName, DOG_SCHEMA);
                    checkAddedAndRemovable(fieldName);
                    break;
                default:
                    // All simple fields
                    schema.addField(fieldName, fieldType.getType());
                    checkAddedAndRemovable(fieldName);
            }
        }
        for (FieldListType fieldType : FieldListType.values()) {
            switch (fieldType) {
                case LIST:
                    schema.addRealmListField(fieldName, DOG_SCHEMA);
                    checkAddedAndRemovable(fieldName);
                    break;
                default:
                    // All primitive lists
                    schema.addRealmListField(fieldName, fieldType.getType());
                    checkAddedAndRemovable(fieldName);
            }
        }
    }

    // Checks that field is actually added and that it can be removed again.
    private void checkAddedAndRemovable(String fieldName) {
        assertTrue(schema.hasField(fieldName));
        schema.removeField(fieldName);
        assertFalse(schema.hasField(fieldName));
    }

    @Test
    public void addField_nameAlreadyExistsThrows() {
        if (type == ObjectSchemaType.IMMUTABLE) {
            return;
        }
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

    @Test
    public void addField_realmModelThrows() {
        if (type == ObjectSchemaType.IMMUTABLE) {
            return;
        }
        try {
            schema.addField("test", Dog.class);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), CoreMatchers.containsString(
                    "Use 'addRealmObjectField()' instead to add fields that link to other RealmObjects:"));
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
        if (type == ObjectSchemaType.IMMUTABLE) {
            return;
        }
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
        if (type == ObjectSchemaType.IMMUTABLE) {
            return;
        }
        String fieldName = "foo";
        for (FieldType fieldType : FieldType.values()) {
            switch (fieldType) {
                case OBJECT: continue; // Not possible.
                default:
                    // All simple types
                    schema.addField(fieldName, fieldType.getType(), FieldAttribute.REQUIRED);
                    assertTrue(schema.isRequired(fieldName));
                    schema.removeField(fieldName);
            }
        }
        for (FieldListType fieldType : FieldListType.values()) {
            switch(fieldType) {
                case LIST:
                    continue; // Not possible.
                default:
                    // All simple list types
                    schema.addRealmListField(fieldName, fieldType.getType());
                    if (fieldType.isNullable()) {
                        schema.setRequired(fieldName, true);
                    }
                    assertTrue(fieldName + " should be required", schema.isRequired(fieldName));
                    schema.removeField(fieldName);
            }
        }
    }

    @Test
    public void indexedFieldAttribute() {
        if (type == ObjectSchemaType.IMMUTABLE) {
            return;
        }
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
        if (type == ObjectSchemaType.IMMUTABLE) {
            return;
        }
        for (InvalidIndexFieldType fieldType : InvalidIndexFieldType.values()) {
            String fieldName = "foo";
            try {
                schema.addField(fieldName, fieldType.getType(), FieldAttribute.INDEXED);
                fail(fieldType + " should not be allowed to be indexed");
            } catch (IllegalArgumentException ignored) {
            }
        }

        // Probe for all variants of primitive lists
        try {
            schema.addRealmListField("foo", String.class);
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void primaryKeyFieldAttribute() {
        if (type == ObjectSchemaType.IMMUTABLE) {
            return;
        }
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
        if (type == ObjectSchemaType.IMMUTABLE) {
            return;
        }
        for (InvalidPrimaryKeyFieldType fieldType : InvalidPrimaryKeyFieldType.values()) {
            String fieldName = "foo";
            try {
                schema.addField(fieldName, fieldType.getType(), FieldAttribute.PRIMARY_KEY);
                fail(fieldType + " should not be allowed to be a primary key");
            } catch (IllegalArgumentException ignored) {
            }
        }

        try {
            schema.addRealmListField("foo", schema);
        } catch (IllegalArgumentException ignored) {
        }

        // Probe for all variants of primitive lists
        try {
            schema.addRealmListField("foo", String.class);
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void addPrimaryKeyFieldModifier_alreadyExistsThrows() {
        if (type == ObjectSchemaType.IMMUTABLE) {
            return;
        }
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
        if (type == ObjectSchemaType.IMMUTABLE) {
            return;
        }
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
        if (type == ObjectSchemaType.IMMUTABLE) {
            return;
        }
        for (PrimaryKeyFieldType fieldType : PrimaryKeyFieldType.values()) {
            final String fieldName = "foo";
            schema.addField(fieldName, fieldType.getType());

            // Creates multiple objects with same values.
            ((DynamicRealm)realm).createObject(schema.getClassName());
            ((DynamicRealm)realm).createObject(schema.getClassName());

            try {
                schema.addPrimaryKey(fieldName);
                fail();
            } catch (IllegalArgumentException e) {
                // Checks if message reports correct field name.
                assertThat(e.getMessage(), CoreMatchers.containsString(fieldName));
            }
            schema.removeField(fieldName);
        }
    }

    @Test
    public void addIndexFieldModifier_illegalFieldTypeThrows() {
        if (type == ObjectSchemaType.IMMUTABLE) {
            return;
        }
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
        if (type == ObjectSchemaType.IMMUTABLE) {
            return;
        }
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
    public void setNullable_trueAndFalse() {
        if (type == ObjectSchemaType.IMMUTABLE) {
            thrown.expect(UnsupportedOperationException.class);
            schema.setNullable("test", true);
            return;
        }
        String fieldName = "foo";
        for (FieldType fieldType : FieldType.values()) {
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
                default:
                    // All simple types.
                    schema.addField(fieldName, fieldType.getType());
                    assertEquals(fieldType.isNullable(), schema.isNullable(fieldName));
                    schema.setNullable(fieldName, !fieldType.isNullable());
                    assertEquals(!fieldType.isNullable(), schema.isNullable(fieldName));
            }
            schema.removeField(fieldName);
        }
        for (FieldListType fieldType : FieldListType.values()) {
            switch (fieldType) {
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
                    // All simple list types.
                    schema.addRealmListField(fieldName, fieldType.getType());
                    assertEquals("Type: " + fieldType, fieldType.isNullable(), schema.isNullable(fieldName));
                    schema.setNullable(fieldName, !fieldType.isNullable());
                    assertEquals("Type: " + fieldType, !fieldType.isNullable(), schema.isNullable(fieldName));
            }
            schema.removeField(fieldName);
        }
    }

    @Test
    public void setRequired_trueAndFalse() {
        if (type == ObjectSchemaType.IMMUTABLE) {
            thrown.expect(UnsupportedOperationException.class);
            schema.setRequired("test", true);
            return;
        }
        String fieldName = "foo";
        for (FieldType fieldType : FieldType.values()) {
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
                default:
                    // All simple types.
                    schema.addField(fieldName, fieldType.getType());
                    assertEquals(!fieldType.isNullable(), schema.isRequired(fieldName));
                    schema.setRequired(fieldName, fieldType.isNullable());
                    assertEquals(fieldType.isNullable(), schema.isRequired(fieldName));
            }
            schema.removeField(fieldName);
        }
        for (FieldListType fieldType : FieldListType.values()) {
            switch (fieldType) {
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
                    // All simple list types.
                    schema.addRealmListField(fieldName, fieldType.getType());
                    assertEquals(!fieldType.isNullable(), schema.isRequired(fieldName));
                    schema.setRequired(fieldName, fieldType.isNullable());
                    assertEquals(fieldType.isNullable(), schema.isRequired(fieldName));
            }
            schema.removeField(fieldName);
        }
    }

    // When converting a nullable field to required, the null values of the field will be set to the default value
    // according to the field type.
    @Test
    public void setRequired_nullValueBecomesDefaultValue() {
        if (type == ObjectSchemaType.IMMUTABLE) {
            return;
        }
        for (FieldType fieldType : FieldType.values()) {
            String fieldName = fieldType.name();
            switch (fieldType) {
                case OBJECT:
                    // Skip always nullable fields
                    break;
                default:
                    // Skip not-nullable fields .
                    if (!fieldType.isNullable()) {
                        break;
                    }
                    schema.addField(fieldName, fieldType.getType());
                    DynamicRealmObject object = ((DynamicRealm)realm).createObject(schema.getClassName());
                    assertTrue(object.isNull(fieldName));
                    schema.setRequired(fieldName, true);
                    assertFalse(object.isNull(fieldName));
                    if (fieldType == FieldType.BLOB) {
                        assertEquals(0, object.getBlob(fieldName).length);
                    } else if (fieldType == FieldType.BOOLEAN) {
                        assertFalse(object.getBoolean(fieldName));
                    } else if (fieldType == FieldType.STRING) {
                        assertEquals(0, object.getString(fieldName).length());
                    } else if (fieldType == FieldType.FLOAT) {
                        assertEquals(0.0F, object.getFloat(fieldName), 0F);
                    } else if (fieldType == FieldType.DOUBLE) {
                        assertEquals(0.0D, object.getDouble(fieldName), 0D);
                    } else if (fieldType == FieldType.DATE) {
                        assertEquals(new Date(0), object.getDate(fieldName));
                    } else {
                        assertEquals(0, object.getInt(fieldName));
                    }
                    break;
            }
        }
        for (FieldListType fieldType : FieldListType.values()) {
            switch(fieldType) {
                case LIST:
                    // Skip always non-nullable fields.
                    break;
                case STRING_LIST:
                    checkListValueConversionToDefaultValue(String.class, "");
                    break;
                case SHORT_LIST:
                    checkListValueConversionToDefaultValue(Short.class, (short) 0);
                    break;
                case INT_LIST:
                    checkListValueConversionToDefaultValue(Integer.class, 0);
                    break;
                case LONG_LIST:
                    checkListValueConversionToDefaultValue(Long.class, 0L);
                    break;
                case BYTE_LIST:
                    checkListValueConversionToDefaultValue(Byte.class, (byte) 0);
                    break;
                case BOOLEAN_LIST:
                    checkListValueConversionToDefaultValue(Boolean.class, false);
                    break;
                case FLOAT_LIST:
                    checkListValueConversionToDefaultValue(Float.class, 0.0F);
                    break;
                case DOUBLE_LIST:
                    checkListValueConversionToDefaultValue(Double.class, 0.0D);
                    break;
                case BLOB_LIST:
                    checkListValueConversionToDefaultValue(byte[].class, new byte[0]);
                    break;
                case DATE_LIST:
                    checkListValueConversionToDefaultValue(Date.class, new Date(0));
                    break;
                case PRIMITIVE_INT_LIST:
                case PRIMITIVE_LONG_LIST:
                case PRIMITIVE_BYTE_LIST:
                case PRIMITIVE_BOOLEAN_LIST:
                case PRIMITIVE_FLOAT_LIST:
                case PRIMITIVE_DOUBLE_LIST:
                case PRIMITIVE_SHORT_LIST:
                    // Skip not-nullable fields
                    break;
                default:
                    throw new IllegalArgumentException("Unknown type: " + fieldType);
            }
        }
    }

    // Checks that null values in a value list are correctly converted to default values
    // when field is set to required.
    private <E> void checkListValueConversionToDefaultValue(Class<E> type, Object defaultValue) {
        schema.addRealmListField("foo", type);
        DynamicRealmObject obj = ((DynamicRealm) realm).createObject(schema.getClassName());
        RealmList<E> list = new RealmList<>();
        list.add(null);
        obj.setList("foo", list);
        assertNull(obj.getList("foo", type).first());

        // Convert from nullable to required
        schema.setRequired("foo", true);
        if (defaultValue instanceof byte[]) {
            assertArrayEquals((byte[]) defaultValue, (byte[]) obj.getList("foo", type).first());
        } else {
            assertEquals(defaultValue, obj.getList("foo", type).first());
        }

        // Convert back again
        schema.setRequired("foo", false);
        if (defaultValue instanceof byte[]) {
            //noinspection ConstantConditions
            assertArrayEquals((byte[]) defaultValue, (byte[]) obj.getList("foo", type).first());
        } else {
            assertEquals(defaultValue, obj.getList("foo", type).first());
        }

        // Cleanup
        schema.removeField("foo");
    }

    // Special test for making sure that binary data in all forms are transformed correctly
    // when moving between nullable and required states.
    @Test
    public void binaryData_nullabilityConversions() {
        if (type == ObjectSchemaType.IMMUTABLE) {
            return;
        }
        schema.addRealmListField("foo", byte[].class);

        DynamicRealmObject obj = ((DynamicRealm) realm).createObject(schema.getClassName());
        RealmList<byte[]> list = obj.getList("foo", byte[].class);
        assertTrue(list.size() == 0);

        // Initial content (nullable)
        list.add(null);
        list.add(new byte[] {1, 2, 3});
        assertNull(list.get(0));
        assertArrayEquals(new byte[] {1, 2, 3}, list.get(1));

        // Transform to required
        schema.setRequired("foo", true);
        list = obj.getList("foo", byte[].class);
        assertEquals(0, list.get(0).length);
        assertArrayEquals(new byte[] {1, 2, 3}, list.get(1));

        // Transform back to nullable
        schema.setRequired("foo", false);
        list = obj.getList("foo", byte[].class);
        assertEquals(0, list.get(0).length);
        assertArrayEquals(new byte[] {1, 2, 3}, list.get(1));
    }

    @Test
    public void setRequired_true_onPrimaryKeyField_containsNullValues_shouldThrow() {
        if (type == ObjectSchemaType.IMMUTABLE) {
            return;
        }
        for (PrimaryKeyFieldType fieldType : PrimaryKeyFieldType.values()) {
            String className = fieldType.getType().getSimpleName() + "Class";
            String fieldName = "primaryKey";
            schema = realmSchema.create(className);
            if (!fieldType.isNullable()) {
                continue;
            }
            schema.addField(fieldName, fieldType.getType(), FieldAttribute.PRIMARY_KEY);
            DynamicRealmObject object = ((DynamicRealm)realm).createObject(schema.getClassName(), null);
            assertTrue(object.isNull(fieldName));
            try {
                schema.setRequired(fieldName, true);
                fail();
            } catch (IllegalStateException expected) {
                assertThat(expected.getMessage(),
                        CoreMatchers.containsString("The primary key field 'primaryKey' has 'null' values stored."));
            }
            realmSchema.remove(className);
        }
    }

    private void setRequired_onPrimaryKeyField(boolean isRequired) {
        for (PrimaryKeyFieldType fieldType : PrimaryKeyFieldType.values()) {
            String className = fieldType.getType().getSimpleName() + "Class";
            String fieldName = "primaryKey";
            schema = realmSchema.create(className);
            if (!fieldType.isNullable()) {
                continue;
            }
            if (isRequired) {
                schema.addField(fieldName, fieldType.getType(), FieldAttribute.PRIMARY_KEY);
            } else {
                schema.addField(fieldName, fieldType.getType(), FieldAttribute.PRIMARY_KEY, FieldAttribute.REQUIRED);
            }
            ((DynamicRealm)realm).createObject(schema.getClassName(), "1");
            ((DynamicRealm)realm).createObject(schema.getClassName(), "2");
            assertTrue(schema.hasPrimaryKey());
            assertTrue(schema.hasIndex(fieldName));

            schema.setRequired(fieldName, isRequired);
            assertTrue(schema.hasPrimaryKey());
            assertTrue(schema.hasIndex(fieldName));

            RealmResults<DynamicRealmObject> results = ((DynamicRealm)realm).where(className).sort(fieldName).findAll();
            assertEquals(2, results.size());
            if (fieldType == PrimaryKeyFieldType.STRING) {
                assertEquals("1", results.get(0).getString(fieldName));
                assertEquals("2", results.get(1).getString(fieldName));
            } else {
                assertEquals(1, results.get(0).getLong(fieldName));
                assertEquals(2, results.get(1).getLong(fieldName));
            }
            realmSchema.remove(className);
        }
    }

    @Test
    public void setRequired_true_onPrimaryKeyField() {
        if (type == ObjectSchemaType.IMMUTABLE) {
            return;
        }
        setRequired_onPrimaryKeyField(true);
    }

    @Test
    public void setRequired_false_onPrimaryKeyField() {
        if (type == ObjectSchemaType.IMMUTABLE) {
            return;
        }
        setRequired_onPrimaryKeyField(false);
    }

    private void setRequired_onIndexedField(boolean toRequired) {
        String fieldName = "IndexedField";
        for (IndexFieldType fieldType : IndexFieldType.values()) {
            if (!fieldType.isNullable()) {
                continue;
            }
            if (toRequired) {
                schema.addField(fieldName, fieldType.getType(), FieldAttribute.INDEXED);
            } else {
                schema.addField(fieldName, fieldType.getType(), FieldAttribute.INDEXED, FieldAttribute.REQUIRED);
            }
            assertTrue(schema.hasIndex(fieldName));
            schema.setRequired(fieldName, toRequired);
            assertTrue(schema.hasIndex(fieldName));
            schema.removeField(fieldName);
        }
    }

    @Test
    public void setRequired_true_onIndexedField() {
        if (type == ObjectSchemaType.IMMUTABLE) {
            return;
        }
        setRequired_onIndexedField(true);
    }

    @Test
    public void setRequired_false_onIndexedField() {
        if (type == ObjectSchemaType.IMMUTABLE) {
            return;
        }
        setRequired_onIndexedField(false);
    }

    @Test
    public void setPrimaryKey_trueAndFalse() {
        if (type == ObjectSchemaType.IMMUTABLE) {
            try {
                schema.addPrimaryKey("test");
                fail();
            } catch (UnsupportedOperationException ignore){

            }
            try {
                schema.removePrimaryKey();
                fail();
            } catch (UnsupportedOperationException ignore){
            }
            return;
        }
        for (PrimaryKeyFieldType fieldType : PrimaryKeyFieldType.values()) {
            String fieldName = "foo";
            schema.addField(fieldName, fieldType.getType());
            schema.addPrimaryKey(fieldName);
            assertTrue(schema.hasPrimaryKey());
            assertTrue(schema.isPrimaryKey(fieldName));
            assertTrue(schema.hasIndex(fieldName));
            schema.removePrimaryKey();
            assertFalse(schema.hasPrimaryKey());
            assertFalse(schema.isPrimaryKey(fieldName));
            assertFalse(schema.hasIndex(fieldName));
            schema.removeField(fieldName);
        }
    }

    @Test
    public void removeNonExistingPrimaryKeyThrows() {
        if (type == ObjectSchemaType.IMMUTABLE) {
            return;
        }
        String fieldName = "foo";
        schema.addField(fieldName, String.class);

        thrown.expect(IllegalStateException.class);
        schema.removePrimaryKey();
    }

    @Test
    public void setIndex_trueAndFalse() {
        if (type == ObjectSchemaType.IMMUTABLE) {
            try {
                schema.addIndex("test");
                fail();
            } catch (UnsupportedOperationException ignore) {
            }
            try {
                schema.removeIndex("test");
                fail();
            } catch (UnsupportedOperationException ignore) {
            }
            return;
        }
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
        if (type == ObjectSchemaType.IMMUTABLE) {
            return;
        }
        String fieldName = "foo";
        schema.addField(fieldName, String.class);

        thrown.expect(IllegalStateException.class);
        schema.removeIndex(fieldName);
    }

    @Test
    public void removeField() {
        if (type == ObjectSchemaType.IMMUTABLE) {
            thrown.expect(UnsupportedOperationException.class);
            DOG_SCHEMA.removeField(Dog.FIELD_HEIGHT);
            return;
        }
        String fieldName = "foo";
        schema.addField(fieldName, String.class);
        assertTrue(schema.hasField(fieldName));
        schema.removeField(fieldName);
        assertFalse(schema.hasField(fieldName));
    }

    @Test
    public void removeField_withPrimaryKey() {
        if (type == ObjectSchemaType.IMMUTABLE) {
            return;
        }
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
        if (type == ObjectSchemaType.IMMUTABLE) {
            return;
        }
        String fieldName = "foo";

        thrown.expect(IllegalStateException.class);
        schema.removeField(fieldName);
    }

    @Test
    public void renameField() {
        if (type == ObjectSchemaType.IMMUTABLE) {
            thrown.expect(UnsupportedOperationException.class);
            schema.renameField("test", "test1");
            return;
        }
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
        if (type == ObjectSchemaType.IMMUTABLE) {
            return;
        }
        String oldFieldName = "old";
        String newFieldName = "new";

        thrown.expect(IllegalArgumentException.class);
        schema.renameField(oldFieldName, newFieldName);
    }

    @Test
    public void renameField_toIllegalNameThrows() {
        if (type == ObjectSchemaType.IMMUTABLE) {
            return;
        }
        String oldFieldName = "old";
        String newFieldName = "";
        schema.addField(oldFieldName, String.class);

        thrown.expect(IllegalArgumentException.class);
        schema.renameField(oldFieldName, newFieldName);
    }

    @Test
    public void renameField_withPrimaryKey() {
        if (type == ObjectSchemaType.IMMUTABLE) {
            return;
        }
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
        final String[] validClassNames = {
                TestHelper.getRandomString(1),
                "Darby",
                TestHelper.getRandomString(Table.CLASS_NAME_MAX_LENGTH)
        };

        if (type == ObjectSchemaType.IMMUTABLE) {
            thrown.expect(UnsupportedOperationException.class);
            DOG_SCHEMA.setClassName(validClassNames[0]);
            return;
        }

        assertEquals("Dog", DOG_SCHEMA.getClassName());
        for (String validClassName : validClassNames) {
            DOG_SCHEMA.setClassName(validClassName);
            assertEquals(validClassName, DOG_SCHEMA.getClassName());
            assertTrue(realmSchema.contains(validClassName));
        }
    }

    @Test
    public void transform() {
        if (type == ObjectSchemaType.IMMUTABLE) {
            thrown.expect(UnsupportedOperationException.class);
            DOG_SCHEMA.transform(new RealmObjectSchema.Function() {
                @Override
                public void apply(DynamicRealmObject obj) {
                }
            });
            return;
        }
        String className = DOG_SCHEMA.getClassName();
        DynamicRealmObject dog1 = ((DynamicRealm)realm).createObject(className);
        dog1.setInt("age", 1);
        DynamicRealmObject dog2 = ((DynamicRealm)realm).createObject(className);
        dog2.setInt("age", 2);

        DOG_SCHEMA.transform(new RealmObjectSchema.Function() {
            @Override
            public void apply(DynamicRealmObject obj) {
                obj.setInt("age", obj.getInt("age") + 1);
            }
        });
        assertEquals(5, ((DynamicRealm)realm).where("Dog").sum("age").intValue());
    }

    @Test
    public void transformObjectReferences() {
        if (type == ObjectSchemaType.IMMUTABLE) {
            return;
        }
        String className = DOG_SCHEMA.getClassName();
        DynamicRealmObject dog1 = ((DynamicRealm)realm).createObject(className);
        dog1.setInt("age", 1);

        DOG_SCHEMA.transform(new RealmObjectSchema.Function() {
            @Override
            public void apply(DynamicRealmObject dog) {
                DynamicRealmObject owner = ((DynamicRealm)realm).createObject("Owner");
                owner.setString("name", "John");
                dog.setObject("owner", owner);
            }
        });
        //noinspection ConstantConditions
        assertEquals("John", ((DynamicRealm)realm).where("Dog").findFirst().getObject("owner").getString("name"));
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
        schema = realmSchema.get("AllJavaTypes");
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

    @Test
    public void getFieldIndex() {
        final String className = "NoField";
        final String fieldName = "field";
        RealmConfiguration emptyConfig = configFactory.createConfiguration("empty");
        DynamicRealm dynamicRealm = DynamicRealm.getInstance(emptyConfig);
        dynamicRealm.beginTransaction();
        RealmObjectSchema objectSchema = dynamicRealm.getSchema().create(className);

        assertTrue(objectSchema.getFieldIndex(fieldName) < 0);

        objectSchema.addField(fieldName, long.class);
        //noinspection ConstantConditions
        assertTrue(objectSchema.getFieldIndex(fieldName) >= 0);

        objectSchema.removeField(fieldName);
        assertTrue(objectSchema.getFieldIndex(fieldName) < 0);

        dynamicRealm.cancelTransaction();
        dynamicRealm.close();
    }

    @Test
    public void getFieldType_nonLatinName() {
        RealmObjectSchema objSchema = realm.getSchema().get(NonLatinFieldNames.class.getSimpleName());
        assertEquals(RealmFieldType.INTEGER, objSchema.getFieldType(NonLatinFieldNames.FIELD_LONG_GREEK_CHAR));
    }

    @Test
    public void addList_modelClassThrowsWithProperError() {
        if (type == ObjectSchemaType.IMMUTABLE) {
            return;
        }

        try {
            schema.addRealmListField("field", AllJavaTypes.class);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Use 'addRealmListField(String name, RealmObjectSchema schema)' instead"));
        }
    }


    private interface FieldRunnable {
        void run(String fieldName);
    }

    // Tests https://github.com/realm/realm-studio/issues/5899
    @Test
    public void setRequired_keepExistingRowsIfPrimaryKey() {
        if (type == ObjectSchemaType.IMMUTABLE) {
            return;
        }
        DynamicRealm dynRealm = (DynamicRealm) realm;
        String className = "NewClass";
        String fieldName = "field";

        // Check all primary key types
        for (PrimaryKeyFieldType fieldType : PrimaryKeyFieldType.values()) {
            schema.addField(fieldName, fieldType.getType(), FieldAttribute.PRIMARY_KEY); // primary key field

            // Hackish way to add sample data, only treat string differently
            for (int i = 0; i < 5; i++) {
                Object primaryKeyValue = (fieldType.getType() == String.class) ? Integer.toString(i) : i;
                dynRealm.createObject(className, primaryKeyValue);
            }

            // Verify that sample data is intact before swapping nullability state
            String errMsg = String.format(String.format("Count mismatch for FieldType = %s and Nullable = %s", fieldType.getType(), schema.isNullable(fieldName)));
            assertEquals(errMsg, 5, dynRealm.where(className).count());

            // Swap nullability state
            schema.setRequired(fieldName, !schema.isRequired(fieldName));
            errMsg = String.format(String.format("Count mismatch for FieldType = %s and Nullable = %s", fieldType.getType(), schema.isNullable(fieldName)));
            assertEquals(errMsg, 5, dynRealm.where(className).count());

            // Cleanup
            dynRealm.delete(className);
            schema.removeField(fieldName);
        }
    }
}
