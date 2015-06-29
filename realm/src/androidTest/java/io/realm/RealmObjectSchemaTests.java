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

import java.lang.reflect.Modifier;
import java.util.EnumSet;

import io.realm.dynamic.RealmModifier;
import io.realm.dynamic.RealmObjectSchema;
import io.realm.dynamic.RealmSchema;
import io.realm.entities.AllJavaTypes;
import io.realm.exceptions.RealmException;

public class RealmObjectSchemaTests extends AndroidTestCase {

    private RealmObjectSchema DOG_SCHEMA;
    private Realm realm;
    private RealmObjectSchema schema;
    private RealmSchema realmSchema;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        RealmConfiguration emptyRealm = new RealmConfiguration.Builder(getContext()).build();
        Realm.deleteRealm(emptyRealm);
        realm = Realm.getInstance(emptyRealm);
        realmSchema = realm.getSchema();
        DOG_SCHEMA = realmSchema.getClass("Dog");
        realm.beginTransaction();
        schema = realmSchema.addClass("NewClass");
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
        STRING // TODO Enable these once added by @mc: SHORT, INT, LONG, BOOLEAN, BYTE
    }

    public enum PrimaryKeyFieldType {
        STRING, SHORT, INT, LONG // TODO Enable these once added by @mc: BOOLEAN, BYTE, DATE
    }

    public enum NullableFieldType {
        OBJECT, LIST
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
                    schema.addString(fieldName);
                    checkAddedAndRemovable(fieldName);
                    break;
                case SHORT:
                    fieldName = AllJavaTypes.FIELD_SHORT;
                    schema.addShort(fieldName);
                    checkAddedAndRemovable(fieldName);
                    break;
                case INT:
                    fieldName = AllJavaTypes.FIELD_INT;
                    schema.addInt(fieldName);
                    checkAddedAndRemovable(fieldName);
                    break;
                case LONG:
                    fieldName = AllJavaTypes.FIELD_LONG;
                    schema.addLong(fieldName);
                    checkAddedAndRemovable(fieldName);
                    break;
                case BYTE:
                    fieldName = AllJavaTypes.FIELD_BYTE;
                    schema.addByte(fieldName);
                    checkAddedAndRemovable(fieldName);
                    break;
                case BOOLEAN:
                    fieldName = AllJavaTypes.FIELD_BOOLEAN;
                    schema.addBoolean(fieldName);
                    checkAddedAndRemovable(fieldName);
                    break;
                case FLOAT:
                    fieldName = AllJavaTypes.FIELD_FLOAT;
                    schema.addFloat(fieldName);
                    checkAddedAndRemovable(fieldName);
                    break;
                case DOUBLE:
                    fieldName = AllJavaTypes.FIELD_DOUBLE;
                    schema.addDouble(fieldName);
                    checkAddedAndRemovable(fieldName);
                    break;
                case BLOB:
                    fieldName = AllJavaTypes.FIELD_BLOB;
                    schema.addBlob(fieldName);
                    checkAddedAndRemovable(fieldName);
                    break;
                case DATE:
                    fieldName = AllJavaTypes.FIELD_DATE;
                    schema.addDate(fieldName);
                    checkAddedAndRemovable(fieldName);
                    break;
                case OBJECT:
                    fieldName = AllJavaTypes.FIELD_OBJECT;
                    schema.addObject(fieldName, DOG_SCHEMA);
                    checkAddedAndRemovable(fieldName);
                    break;
                case LIST:
                    fieldName = AllJavaTypes.FIELD_LIST;
                    schema.addObject(fieldName, DOG_SCHEMA);
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
                    schema.addString(fieldName, EnumSet.of(RealmModifier.INDEXED));
                    checkAddedAndRemovable(fieldName);
                    break;
//                case SHORT:
//                    fieldName = AllJavaTypes.FIELD_SHORT;
//                    schema.addShort(fieldName, EnumSet.of(RealmModifier.INDEXED));
//                    checkAddedAndRemovable(fieldName);
//                    break;
//                case INT:
//                    fieldName = AllJavaTypes.FIELD_INT;
//                    schema.addInt(fieldName, EnumSet.of(RealmModifier.INDEXED));
//                    checkAddedAndRemovable(fieldName);
//                    break;
//                case LONG:
//                    fieldName = AllJavaTypes.FIELD_LONG;
//                    schema.addLong(fieldName, EnumSet.of(RealmModifier.INDEXED));
//                    checkAddedAndRemovable(fieldName);
//                    break;
//                case BYTE:
//                    fieldName = AllJavaTypes.FIELD_BYTE;
//                    schema.addByte(fieldName, EnumSet.of(RealmModifier.INDEXED));
//                    checkAddedAndRemovable(fieldName);
//                    break;
//                case BOOLEAN:
//                    fieldName = AllJavaTypes.FIELD_BOOLEAN;
//                    schema.addBoolean(fieldName, EnumSet.of(RealmModifier.INDEXED));
//                    checkAddedAndRemovable(fieldName);
//                    break;
                default:
                    fail();
            }
        }
    }

    public void testAddRemovePrimaryKeyField() {
        for (PrimaryKeyFieldType fieldType : PrimaryKeyFieldType.values()) {
            String fieldName;
            switch (fieldType) {
                case STRING:
                    fieldName = AllJavaTypes.FIELD_STRING;
                    schema.addString(fieldName, EnumSet.of(RealmModifier.PRIMARY_KEY));
                    checkAddedAndRemovable(fieldName);
                    break;
                case SHORT:
                    fieldName = AllJavaTypes.FIELD_SHORT;
                    schema.addShort(fieldName, EnumSet.of(RealmModifier.PRIMARY_KEY));
                    checkAddedAndRemovable(fieldName);
                    break;
                case INT:
                    fieldName = AllJavaTypes.FIELD_INT;
                    schema.addInt(fieldName, EnumSet.of(RealmModifier.PRIMARY_KEY));
                    checkAddedAndRemovable(fieldName);
                    break;
                case LONG:
                    fieldName = AllJavaTypes.FIELD_LONG;
                    schema.addLong(fieldName, EnumSet.of(RealmModifier.PRIMARY_KEY));
                    checkAddedAndRemovable(fieldName);
                    break;
//                case BYTE:
//                    fieldName = AllJavaTypes.FIELD_BYTE;
//                    schema.addByte(fieldName, EnumSet.of(RealmModifier.PRIMARY_KEY));
//                    checkAddedAndRemovable(fieldName);
//                    break;
//                case BOOLEAN:
//                    fieldName = AllJavaTypes.FIELD_BOOLEAN;
//                    schema.addBoolean(fieldName, EnumSet.of(RealmModifier.INDEXED));
//                    checkAddedAndRemovable(fieldName);
//                    break;
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
                            schema.addString(fieldName);
                        }
                    });
                    break;
                case SHORT:
                    checkAddFieldTwice(AllJavaTypes.FIELD_SHORT, new FieldRunnable() {
                        @Override
                        public void run(String fieldName) {
                            schema.addShort(fieldName);
                        }
                    });
                    break;
                case INT:
                    checkAddFieldTwice(AllJavaTypes.FIELD_INT, new FieldRunnable() {
                        @Override
                        public void run(String fieldName) {
                            schema.addInt(fieldName);
                        }
                    });
                    break;
                case LONG:
                    checkAddFieldTwice(AllJavaTypes.FIELD_LONG, new FieldRunnable() {
                        @Override
                        public void run(String fieldName) {
                            schema.addLong(fieldName);
                        }
                    });
                    break;
                case BYTE:
                    checkAddFieldTwice(AllJavaTypes.FIELD_BYTE, new FieldRunnable() {
                        @Override
                        public void run(String fieldName) {
                            schema.addByte(fieldName);
                        }
                    });
                    break;
                case BOOLEAN:
                    checkAddFieldTwice(AllJavaTypes.FIELD_BOOLEAN, new FieldRunnable() {
                        @Override
                        public void run(String fieldName) {
                            schema.addBoolean(fieldName);
                        }
                    });
                    break;
                case FLOAT:
                    checkAddFieldTwice(AllJavaTypes.FIELD_FLOAT, new FieldRunnable() {
                        @Override
                        public void run(String fieldName) {
                            schema.addFloat(fieldName);
                        }
                    });
                    break;
                case DOUBLE:
                    checkAddFieldTwice(AllJavaTypes.FIELD_DOUBLE, new FieldRunnable() {
                        @Override
                        public void run(String fieldName) {
                            schema.addDouble(fieldName);
                        }
                    });
                    break;
                case BLOB:
                    checkAddFieldTwice(AllJavaTypes.FIELD_BLOB, new FieldRunnable() {
                        @Override
                        public void run(String fieldName) {
                            schema.addBlob(fieldName);
                        }
                    });
                    break;
                case DATE:
                    checkAddFieldTwice(AllJavaTypes.FIELD_DATE, new FieldRunnable() {
                        @Override
                        public void run(String fieldName) {
                            schema.addDate(fieldName);
                        }
                    });
                    break;
                case OBJECT:
                    checkAddFieldTwice(AllJavaTypes.FIELD_OBJECT, new FieldRunnable() {
                        @Override
                        public void run(String fieldName) {
                            schema.addObject(fieldName, DOG_SCHEMA);
                        }
                    });
                    break;
                case LIST:
                    checkAddFieldTwice(AllJavaTypes.FIELD_LIST, new FieldRunnable() {
                        @Override
                        public void run(String fieldName) {
                            schema.addList(fieldName, DOG_SCHEMA);
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
        String[] fieldNames = new String[] { null, "", "foo.bar" };
        for (FieldType fieldType : FieldType.values()) {
            for (String fieldName : fieldNames) {
                try {
                    switch(fieldType) {
                        case STRING: schema.addString(fieldName); break;
                        case SHORT: schema.addShort(fieldName); break;
                        case INT: schema.addInt(fieldName); break;
                        case LONG: schema.addLong(fieldName); break;
                        case BYTE: schema.addByte(fieldName); break;
                        case BOOLEAN: schema.addBoolean(fieldName); break;
                        case FLOAT: schema.addFloat(fieldName); break;
                        case DOUBLE: schema.addDouble(fieldName); break;
                        case BLOB: schema.addBlob(fieldName); break;
                        case DATE: schema.addDate(fieldName); break;
                        case OBJECT: schema.addObject(fieldName, DOG_SCHEMA); break;
                        case LIST: schema.addList(fieldName, DOG_SCHEMA); break;
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
                    schema.addString(fieldName);
                    break;
                case SHORT:
                    fieldName = AllJavaTypes.FIELD_SHORT;
                    schema.addShort(fieldName);
                    break;
                case INT:
                    fieldName = AllJavaTypes.FIELD_INT;
                    schema.addInt(fieldName);
                    break;
                case LONG:
                    fieldName = AllJavaTypes.FIELD_LONG;
                    schema.addLong(fieldName);
                    break;
//                        case BYTE: schema.addByte(fieldName); break;
//                        case BOOLEAN: schema.addBoolean(fieldName); break;
//                        case DATE: schema.addDate(fieldName); break;
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
        schema.addDouble(fieldName);
        try {
            schema.addPrimaryKey(fieldName);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    public void testAddIndexFieldModifier_illegalFieldTypeThrows() {
        String fieldName = AllJavaTypes.FIELD_DOUBLE;
        schema.addDouble(fieldName);
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
                    schema.addString(fieldName);
                    break;
//                case SHORT:
//                    fieldName = AllJavaTypes.FIELD_SHORT;
//                    schema.addShort(fieldName);
//                    break;
//                case INT:
//                    fieldName = AllJavaTypes.FIELD_INT;
//                    schema.addInt(fieldName);
//                    break;
//                case LONG:
//                    fieldName = AllJavaTypes.FIELD_LONG;
//                    schema.addLong(fieldName);
//                    break;
//                case BOOLEAN:
//                    fieldName = AllJavaTypes.FIELD_BOOLEAN;
//                    schema.addBoolean(fieldName);
//                    break;
//                case DATE:
//                    fieldName = AllJavaTypes.FIELD_DATE;
//                    schema.addDate(fieldName);
//                    break;
                default:
                    fail();
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
                    case STRING: schema.addString(AllJavaTypes.FIELD_STRING, modifiers); break;
                    case SHORT: schema.addShort(AllJavaTypes.FIELD_SHORT, modifiers); break;
                    case INT: schema.addInt(AllJavaTypes.FIELD_INT, modifiers); break;
                    case LONG: schema.addLong(AllJavaTypes.FIELD_LONG, modifiers); break;
                    case BYTE: schema.addByte(AllJavaTypes.FIELD_BYTE, modifiers); break;
                    case BOOLEAN: schema.addBoolean(AllJavaTypes.FIELD_BOOLEAN, modifiers); break;
                    case FLOAT: schema.addFloat(AllJavaTypes.FIELD_FLOAT, modifiers); break;
                    case DOUBLE: schema.addDouble(AllJavaTypes.FIELD_DOUBLE, modifiers); break;
                    case BLOB: schema.addBlob(AllJavaTypes.FIELD_BLOB, modifiers); break;
                    case DATE: schema.addDate(AllJavaTypes.FIELD_DATE, modifiers); break;
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
            EnumSet<RealmModifier> modifiers = EnumSet.of(RealmModifier.NON_NULLABLE);
            try {
                switch (fieldType) {
                    case OBJECT:
                        schema.addObject(AllJavaTypes.FIELD_OBJECT, DOG_SCHEMA);
                        schema.setNotNullable(AllJavaTypes.FIELD_OBJECT);
                        break;
                    case LIST:
                        schema.addList(AllJavaTypes.FIELD_LIST, DOG_SCHEMA);
                        schema.setNotNullable(AllJavaTypes.FIELD_LIST);
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
        schema.addString(fieldName);
        schema.addPrimaryKey(fieldName);
        assertTrue(schema.hasPrimaryKey());
        schema.removePrimaryKey();
        assertFalse(schema.hasPrimaryKey());
    }

    public void testRemoveNonExistingPrimaryKeyThrows() {
        String fieldName = AllJavaTypes.FIELD_STRING;
        schema.addString(fieldName);
        try {
            schema.removePrimaryKey();
            fail();
        } catch (IllegalStateException ignored) {
        }
    }

    public void testRemoveIndex() {
        String fieldName = AllJavaTypes.FIELD_STRING;
        schema.addString(fieldName, EnumSet.of(RealmModifier.INDEXED));
        assertTrue(schema.hasIndex(fieldName));
        schema.removeIndex(fieldName);
        assertFalse(schema.hasIndex(fieldName));
    }

    public void testRemoveNonExistingIndexThrows() {
        String fieldName = AllJavaTypes.FIELD_STRING;
        schema.addString(fieldName);
        try {
            schema.removeIndex(fieldName);
            fail();
        } catch (IllegalStateException ignored) {
        }
    }

    public void testRemoveField() {
        String fieldName = AllJavaTypes.FIELD_STRING;
        schema.addString(fieldName);
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
        schema.addString(oldFieldName);
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
        schema.addString(oldFieldName);
        try {
            schema.renameField(oldFieldName, newFieldName);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    private interface FieldRunnable {
        void run(String fieldName);
    }
}
