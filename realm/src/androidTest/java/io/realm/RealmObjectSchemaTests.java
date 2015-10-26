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

import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.entities.AllJavaTypes;
import io.realm.exceptions.RealmException;

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
        DOG_SCHEMA = realmSchema.getClass("Dog");
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

    public enum NullableFieldType {
        OBJECT, LIST // FIXME Add Nullable support
    }

    public enum NonNullableFieldType {
        STRING, SHORT, INT, LONG, BYTE, BOOLEAN, FLOAT, DOUBLE, BLOB, DATE
    }

    public void testAddRemoveField() {
        for (FieldType fieldType : FieldType.values()) {
            String fieldName;
            switch(fieldType) {
                case STRING:
                    fieldName = AllJavaTypes.FIELD_STRING;
                    schema.addStringField(fieldName);
                    checkAddedAndRemovable(fieldName);
                    break;
                case SHORT:
                    fieldName = AllJavaTypes.FIELD_SHORT;
                    schema.addShortField(fieldName);
                    checkAddedAndRemovable(fieldName);
                    break;
                case INT:
                    fieldName = AllJavaTypes.FIELD_INT;
                    schema.addIntField(fieldName);
                    checkAddedAndRemovable(fieldName);
                    break;
                case LONG:
                    fieldName = AllJavaTypes.FIELD_LONG;
                    schema.addLongField(fieldName);
                    checkAddedAndRemovable(fieldName);
                    break;
                case BYTE:
                    fieldName = AllJavaTypes.FIELD_BYTE;
                    schema.addByteField(fieldName);
                    checkAddedAndRemovable(fieldName);
                    break;
                case BOOLEAN:
                    fieldName = AllJavaTypes.FIELD_BOOLEAN;
                    schema.addBooleanField(fieldName);
                    checkAddedAndRemovable(fieldName);
                    break;
                case FLOAT:
                    fieldName = AllJavaTypes.FIELD_FLOAT;
                    schema.addFloatField(fieldName);
                    checkAddedAndRemovable(fieldName);
                    break;
                case DOUBLE:
                    fieldName = AllJavaTypes.FIELD_DOUBLE;
                    schema.addDoubleField(fieldName);
                    checkAddedAndRemovable(fieldName);
                    break;
                case BLOB:
                    fieldName = AllJavaTypes.FIELD_BINARY;
                    schema.addBlobField(fieldName);
                    checkAddedAndRemovable(fieldName);
                    break;
                case DATE:
                    fieldName = AllJavaTypes.FIELD_DATE;
                    schema.addDateField(fieldName);
                    checkAddedAndRemovable(fieldName);
                    break;
                case OBJECT:
                    fieldName = AllJavaTypes.FIELD_OBJECT;
                    schema.addObjectField(fieldName, DOG_SCHEMA);
                    checkAddedAndRemovable(fieldName);
                    break;
                case LIST:
                    fieldName = AllJavaTypes.FIELD_LIST;
                    schema.addObjectField(fieldName, DOG_SCHEMA);
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
                    schema.addStringField(fieldName, EnumSet.of(RealmModifier.INDEXED));
                    checkAddedAndRemovable(fieldName);
                    break;
                case SHORT:
                    fieldName = AllJavaTypes.FIELD_SHORT;
                    schema.addShortField(fieldName, EnumSet.of(RealmModifier.INDEXED));
                    checkAddedAndRemovable(fieldName);
                    break;
                case INT:
                    fieldName = AllJavaTypes.FIELD_INT;
                    schema.addIntField(fieldName, EnumSet.of(RealmModifier.INDEXED));
                    checkAddedAndRemovable(fieldName);
                    break;
                case LONG:
                    fieldName = AllJavaTypes.FIELD_LONG;
                    schema.addLongField(fieldName, EnumSet.of(RealmModifier.INDEXED));
                    checkAddedAndRemovable(fieldName);
                    break;
                case BYTE:
                    fieldName = AllJavaTypes.FIELD_BYTE;
                    schema.addByteField(fieldName, EnumSet.of(RealmModifier.INDEXED));
                    checkAddedAndRemovable(fieldName);
                    break;
                case BOOLEAN:
                    fieldName = AllJavaTypes.FIELD_BOOLEAN;
                    schema.addBooleanField(fieldName, EnumSet.of(RealmModifier.INDEXED));
                    checkAddedAndRemovable(fieldName);
                    break;
                case DATE:
                    fieldName = AllJavaTypes.FIELD_DATE;
                    schema.addDateField(fieldName, EnumSet.of(RealmModifier.INDEXED));
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
                    schema.addStringField(fieldName, EnumSet.of(RealmModifier.PRIMARY_KEY));
                    checkAddedAndRemovable(fieldName);
                    break;
                case SHORT:
                    fieldName = AllJavaTypes.FIELD_SHORT;
                    schema.addShortField(fieldName, EnumSet.of(RealmModifier.PRIMARY_KEY));
                    checkAddedAndRemovable(fieldName);
                    break;
                case INT:
                    fieldName = AllJavaTypes.FIELD_INT;
                    schema.addIntField(fieldName, EnumSet.of(RealmModifier.PRIMARY_KEY));
                    checkAddedAndRemovable(fieldName);
                    break;
                case LONG:
                    fieldName = AllJavaTypes.FIELD_LONG;
                    schema.addLongField(fieldName, EnumSet.of(RealmModifier.PRIMARY_KEY));
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
                            schema.addStringField(fieldName);
                        }
                    });
                    break;
                case SHORT:
                    checkAddFieldTwice(AllJavaTypes.FIELD_SHORT, new FieldRunnable() {
                        @Override
                        public void run(String fieldName) {
                            schema.addShortField(fieldName);
                        }
                    });
                    break;
                case INT:
                    checkAddFieldTwice(AllJavaTypes.FIELD_INT, new FieldRunnable() {
                        @Override
                        public void run(String fieldName) {
                            schema.addIntField(fieldName);
                        }
                    });
                    break;
                case LONG:
                    checkAddFieldTwice(AllJavaTypes.FIELD_LONG, new FieldRunnable() {
                        @Override
                        public void run(String fieldName) {
                            schema.addLongField(fieldName);
                        }
                    });
                    break;
                case BYTE:
                    checkAddFieldTwice(AllJavaTypes.FIELD_BYTE, new FieldRunnable() {
                        @Override
                        public void run(String fieldName) {
                            schema.addByteField(fieldName);
                        }
                    });
                    break;
                case BOOLEAN:
                    checkAddFieldTwice(AllJavaTypes.FIELD_BOOLEAN, new FieldRunnable() {
                        @Override
                        public void run(String fieldName) {
                            schema.addBooleanField(fieldName);
                        }
                    });
                    break;
                case FLOAT:
                    checkAddFieldTwice(AllJavaTypes.FIELD_FLOAT, new FieldRunnable() {
                        @Override
                        public void run(String fieldName) {
                            schema.addFloatField(fieldName);
                        }
                    });
                    break;
                case DOUBLE:
                    checkAddFieldTwice(AllJavaTypes.FIELD_DOUBLE, new FieldRunnable() {
                        @Override
                        public void run(String fieldName) {
                            schema.addDoubleField(fieldName);
                        }
                    });
                    break;
                case BLOB:
                    checkAddFieldTwice(AllJavaTypes.FIELD_BINARY, new FieldRunnable() {
                        @Override
                        public void run(String fieldName) {
                            schema.addBlobField(fieldName);
                        }
                    });
                    break;
                case DATE:
                    checkAddFieldTwice(AllJavaTypes.FIELD_DATE, new FieldRunnable() {
                        @Override
                        public void run(String fieldName) {
                            schema.addDateField(fieldName);
                        }
                    });
                    break;
                case OBJECT:
                    checkAddFieldTwice(AllJavaTypes.FIELD_OBJECT, new FieldRunnable() {
                        @Override
                        public void run(String fieldName) {
                            schema.addObjectField(fieldName, DOG_SCHEMA);
                        }
                    });
                    break;
                case LIST:
                    checkAddFieldTwice(AllJavaTypes.FIELD_LIST, new FieldRunnable() {
                        @Override
                        public void run(String fieldName) {
                            schema.addListField(fieldName, DOG_SCHEMA);
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
                        case STRING: schema.addStringField(fieldName); break;
                        case SHORT: schema.addShortField(fieldName); break;
                        case INT: schema.addIntField(fieldName); break;
                        case LONG: schema.addLongField(fieldName); break;
                        case BYTE: schema.addByteField(fieldName); break;
                        case BOOLEAN: schema.addBooleanField(fieldName); break;
                        case FLOAT: schema.addFloatField(fieldName); break;
                        case DOUBLE: schema.addDoubleField(fieldName); break;
                        case BLOB: schema.addBlobField(fieldName); break;
                        case DATE: schema.addDateField(fieldName); break;
                        case OBJECT: schema.addObjectField(fieldName, DOG_SCHEMA); break;
                        case LIST: schema.addListField(fieldName, DOG_SCHEMA); break;
                        default:
                            fail();
                    }
                    fail();
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
                    schema.addStringField(fieldName);
                    break;
                case SHORT:
                    fieldName = AllJavaTypes.FIELD_SHORT;
                    schema.addShortField(fieldName);
                    break;
                case INT:
                    fieldName = AllJavaTypes.FIELD_INT;
                    schema.addIntField(fieldName);
                    break;
                case LONG:
                    fieldName = AllJavaTypes.FIELD_LONG;
                    schema.addLongField(fieldName);
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
        schema.addDoubleField(fieldName);
        try {
            schema.addPrimaryKey(fieldName);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    public void testAddIndexFieldModifier_illegalFieldTypeThrows() {
        String fieldName = AllJavaTypes.FIELD_DOUBLE;
        schema.addDoubleField(fieldName);
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
                    schema.addStringField(fieldName);
                    break;
                case SHORT:
                    fieldName = AllJavaTypes.FIELD_SHORT;
                    schema.addShortField(fieldName);
                    break;
                case INT:
                    fieldName = AllJavaTypes.FIELD_INT;
                    schema.addIntField(fieldName);
                    break;
                case LONG:
                    fieldName = AllJavaTypes.FIELD_LONG;
                    schema.addLongField(fieldName);
                    break;
                case BOOLEAN:
                    fieldName = AllJavaTypes.FIELD_BOOLEAN;
                    schema.addBooleanField(fieldName);
                    break;
                case DATE:
                    fieldName = AllJavaTypes.FIELD_DATE;
                    schema.addDateField(fieldName);
                    break;
                case BYTE:
                    fieldName = AllJavaTypes.FIELD_BYTE;
                    schema.addByteField(fieldName);
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

    public void testAddNullableFieldModifier_fieldNotNullableThrows() {
        for (NonNullableFieldType fieldType : NonNullableFieldType.values()) {
            EnumSet<RealmModifier> modifiers = EnumSet.of(RealmModifier.NULLABLE);
            try {
                switch (fieldType) {
                    case STRING: schema.addStringField(AllJavaTypes.FIELD_STRING, modifiers); break;
                    case SHORT: schema.addShortField(AllJavaTypes.FIELD_SHORT, modifiers); break;
                    case INT: schema.addIntField(AllJavaTypes.FIELD_INT, modifiers); break;
                    case LONG: schema.addLongField(AllJavaTypes.FIELD_LONG, modifiers); break;
                    case BYTE: schema.addByteField(AllJavaTypes.FIELD_BYTE, modifiers); break;
                    case BOOLEAN: schema.addBooleanField(AllJavaTypes.FIELD_BOOLEAN, modifiers); break;
                    case FLOAT: schema.addFloatField(AllJavaTypes.FIELD_FLOAT, modifiers); break;
                    case DOUBLE: schema.addDoubleField(AllJavaTypes.FIELD_DOUBLE, modifiers); break;
                    case BLOB: schema.addBlobField(AllJavaTypes.FIELD_BINARY, modifiers); break;
                    case DATE: schema.addDateField(AllJavaTypes.FIELD_DATE, modifiers); break;
                    default:
                        fail();
                }
                fail();
            } catch (RealmException ignored) {
                // This should fail until we add Null support. Disable the API completely if we are going to release
                // this before Null.
            }
        }
    }

    public void testAddNonNullableFieldModifier_fieldNotNonNullableThrows() {
        for (NullableFieldType fieldType : NullableFieldType.values()) {
            EnumSet<RealmModifier> modifiers = EnumSet.of(RealmModifier.REQUIRED);
            try {
                switch (fieldType) {
                    case OBJECT:
                        schema.addObjectField(AllJavaTypes.FIELD_OBJECT, DOG_SCHEMA);
                        schema.setNullable(AllJavaTypes.FIELD_OBJECT, false);
                        break;
                    case LIST:
                        schema.addListField(AllJavaTypes.FIELD_LIST, DOG_SCHEMA);
                        schema.setNullable(AllJavaTypes.FIELD_LIST, false);
                        break;
                    default:
                        fail();
                }
                fail();
            } catch (RealmException ignored) {
                // This should fail until we add Null support. Disable the API completely if we are going to release
                // this before Null.
            }
        }
    }

    public void testRemovePrimaryKey() {
        String fieldName = AllJavaTypes.FIELD_STRING;
        schema.addStringField(fieldName);
        schema.addPrimaryKey(fieldName);
        assertTrue(schema.hasPrimaryKey());
        schema.removePrimaryKey();
        assertFalse(schema.hasPrimaryKey());
    }

    public void testRemoveNonExistingPrimaryKeyThrows() {
        String fieldName = AllJavaTypes.FIELD_STRING;
        schema.addStringField(fieldName);
        try {
            schema.removePrimaryKey();
            fail();
        } catch (IllegalStateException ignored) {
        }
    }

    public void testRemoveIndex() {
        String fieldName = AllJavaTypes.FIELD_STRING;
        schema.addStringField(fieldName, EnumSet.of(RealmModifier.INDEXED));
        assertTrue(schema.hasIndex(fieldName));
        schema.removeIndex(fieldName);
        assertFalse(schema.hasIndex(fieldName));
    }

    public void testRemoveNonExistingIndexThrows() {
        String fieldName = AllJavaTypes.FIELD_STRING;
        schema.addStringField(fieldName);
        try {
            schema.removeIndex(fieldName);
            fail();
        } catch (IllegalStateException ignored) {
        }
    }

    public void testRemoveField() {
        String fieldName = AllJavaTypes.FIELD_STRING;
        schema.addStringField(fieldName);
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
        schema.addStringField(oldFieldName);
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
        schema.addStringField(oldFieldName);
        try {
            schema.renameField(oldFieldName, newFieldName);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    public void testCreateObject() {
        DynamicRealmObject obj = realm.createObject(DOG_SCHEMA.getClassName());
        assertEquals("Dog", obj.getType());
    }

    public void testCreateObjectWithPrimaryKey() {
        DOG_SCHEMA.addPrimaryKey("name");
        DynamicRealmObject dog = realm.createObject(DOG_SCHEMA.getClassName(), "Foo");
        assertEquals("Foo", dog.getString("name"));
    }

    public void testCreateObjectWithIllegalPrimaryKeyValueThrows() {
        DOG_SCHEMA.addPrimaryKey("name");
        try {
            realm.createObject(DOG_SCHEMA.getClassName(), 42);
            fail();
        } catch (IllegalArgumentException expected) {
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
        DOG_SCHEMA.forEach(new RealmObjectSchema.Iterator() {
            @Override
            public void next(DynamicRealmObject obj) {
                totalAge.addAndGet(obj.getInt("age"));
            }
        });
        assertEquals(3, totalAge.get());
    }

    private interface FieldRunnable {
        void run(String fieldName);
    }
}
