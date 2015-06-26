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

import io.realm.dynamic.RealmModifier;
import io.realm.dynamic.RealmObjectSchema;
import io.realm.dynamic.RealmSchema;
import io.realm.entities.AllJavaTypes;

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
        String[] fieldNames = new String[] { null, "", "foo.bar" };
        for (PrimaryKeyFieldType fieldType : PrimaryKeyFieldType.values()) {
            for (String fieldName : fieldNames) {
                switch(fieldType) {
                    case STRING: schema.addString(fieldName); break;
                    case SHORT: schema.addShort(fieldName); break;
                    case INT: schema.addInt(fieldName); break;
                    case LONG: schema.addLong(fieldName); break;
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
                } catch (IllegalArgumentException ignored) {
                }
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
        String[] fieldNames = new String[] { null, "", "foo.bar" };
        for (PrimaryKeyFieldType fieldType : PrimaryKeyFieldType.values()) {
            for (String fieldName : fieldNames) {
                switch(fieldType) {
                    case STRING: schema.addString(fieldName); break;
                    case SHORT: schema.addShort(fieldName); break;
                    case INT: schema.addInt(fieldName); break;
                    case LONG: schema.addLong(fieldName); break;
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
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }

    public void testAddNullableFieldModifier_fieldNotNullableThrows() {

    }

    public void testAddNonNullableFieldModifier_fieldNotNonNullableThrows() {

    }

    public void testRemovePrimaryKey() {

    }

    public void testRemoveNonExistingPrimaryKeyThrows() {

    }

    public void testRemoveIndex() {

    }

    public void testRemoveNonExistingIndexThrows() {

    }

    public void testRemoveField() {

    }

    public void testRemoveNonExistingFieldThrows() {

    }

    public void testRenameField() {

    }

    public void testRenameNonExistingFieldThrows() {

    }

    public void testRenameFieldToIllegalNameThrows() {

    }

    private interface FieldRunnable {
        void run(String fieldName);
    }





    // DIFFERENT APPROACH - Each type tested as a whole
//    public void testStringField() {
//        String fieldName = AllJavaTypes.FIELD_STRING;
//        String newFieldName = "new";
//
//        // Add
//        schema.addString(fieldName);
//        checkAddedAndRemovable(fieldName);
//
//        // Rename
//        schema.addString(fieldName);
//        schema.renameField(fieldName, newFieldName);
//        checkAddedAndRemovable(newFieldName);
//
//        // Indexed
//        schema.addString(fieldName, EnumSet.of(RealmModifier.INDEXED));
//        assertTrue(schema.hasIndex(fieldName));
//        schema.removeIndex(fieldName);
//        checkAddedAndRemovable(fieldName);
//
//        // Primary key
//    }
//
//
//

}
