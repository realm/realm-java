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
import io.realm.internal.Table;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
        for (FieldType fieldType : FieldType.values()) {
            String fieldName = "foo";
            switch (fieldType) {
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
        for (FieldType fieldType : FieldType.values()) {
            String fieldName = "foo";
            switch (fieldType) {
                case OBJECT: continue; // Not possible.
                case LIST: continue; // Not possible.
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
                assertTrue(e.getMessage().contains("\"" + fieldName + "\""));
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
    public void setRemoveNullable() {
        if (type == ObjectSchemaType.IMMUTABLE) {
            thrown.expect(UnsupportedOperationException.class);
            schema.setNullable("test", true);
            return;
        }
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
                    // All simple types.
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
        if (type == ObjectSchemaType.IMMUTABLE) {
            thrown.expect(UnsupportedOperationException.class);
            schema.setRequired("test", true);
            return;
        }
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
                    // All simple types.
                    schema.addField(fieldName, fieldType.getType());
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
                case LIST:
                    // Skip always nullable fields.
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

            RealmResults<DynamicRealmObject> results = ((DynamicRealm)realm).where(className).findAllSorted(fieldName);
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
    public void setRemovePrimaryKey() {
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
    public void setRemoveIndex() {
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

    private interface FieldRunnable {
        void run(String fieldName);
    }
}
