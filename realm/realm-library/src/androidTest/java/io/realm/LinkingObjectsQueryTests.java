/*
 * Copyright 2017 Realm Inc.
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

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

import io.realm.entities.AllJavaTypes;
import io.realm.entities.NullTypes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@Ignore
@RunWith(AndroidJUnit4.class)
public class LinkingObjectsQueryTests extends QueryTests {

    // All the basic tests for is[Not](Equal|Null) are in RealmQueryTests


    // Query on a field descriptor starting with a backlink
    // Build a simple object graph.
    // The test objects are:
    //             gen1
    //             / \
    //         gen2A gen2B
    //           \\   //
    //            gen3
    //  /  = object ref
    //  // = list ref
    @Test
    public void query_startWithBacklink() {
        realm.beginTransaction();
        AllJavaTypes gen1 = realm.createObject(AllJavaTypes.class, 10);

        AllJavaTypes gen2A = realm.createObject(AllJavaTypes.class, 1);
        gen2A.setFieldObject(gen1);

        AllJavaTypes gen2B = realm.createObject(AllJavaTypes.class, 2);
        gen2B.setFieldObject(gen1);

        AllJavaTypes gen3 = realm.createObject(AllJavaTypes.class, 3);
        RealmList<AllJavaTypes> parents = gen3.getFieldList();
        parents.add(gen2A);
        parents.add(gen2B);

        realm.commitTransaction();

        RealmResults<AllJavaTypes> result = realm.where(AllJavaTypes.class)
                .greaterThan(AllJavaTypes.FIELD_LO_OBJECT + "." + AllJavaTypes.FIELD_OBJECT + "." + AllJavaTypes.FIELD_ID, 1)
                .findAll();
        assertEquals(1, result.size());
        assertTrue(result.contains(gen1));
    }

    // Query on a field descriptor that has a backlink in the middle
    // Build a simple object graph.
    // The test objects are:
    //             gen1
    //             / \
    //         gen2A gen2B
    //           \\   //
    //            gen3
    //  /  = object ref
    //  // = list ref
    @Test
    public void query_backlinkInMiddle() {
        realm.beginTransaction();
        AllJavaTypes gen1 = realm.createObject(AllJavaTypes.class, 10);

        AllJavaTypes gen2A = realm.createObject(AllJavaTypes.class, 1);
        gen2A.setFieldObject(gen1);

        AllJavaTypes gen2B = realm.createObject(AllJavaTypes.class, 2);
        gen2B.setFieldObject(gen1);

        AllJavaTypes gen3 = realm.createObject(AllJavaTypes.class, 3);
        RealmList<AllJavaTypes> parents = gen3.getFieldList();
        parents.add(gen2A);
        parents.add(gen2B);

        realm.commitTransaction();

        // TODO: Explain what this test is doing
        RealmResults<AllJavaTypes> result = realm.where(AllJavaTypes.class)
                .lessThan(AllJavaTypes.FIELD_OBJECT + "." + AllJavaTypes.FIELD_LO_OBJECT + "." + AllJavaTypes.FIELD_ID, 2)
                .findAll();
        assertEquals(1, result.size());
        assertTrue(result.contains(gen2A));
    }

    // Tests isNotNull on link's nullable field.
    @Test
    public void isNull_object() {
        populateTestRealmForNullTests(realm);

        // 1 String
        assertEquals(1, realm.where(NullTypes.class).isNull(
                NullTypes.FIELD_LO_OBJECT + "." + NullTypes.FIELD_STRING_NULL).count());
        // 2 Bytes
        assertEquals(1, realm.where(NullTypes.class).isNull(
                NullTypes.FIELD_LO_OBJECT + "." + NullTypes.FIELD_BYTES_NULL).count());
        // 3 Boolean
        assertEquals(1, realm.where(NullTypes.class).isNull(
                NullTypes.FIELD_LO_OBJECT + "." + NullTypes.FIELD_BOOLEAN_NULL).count());
        // 4 Byte
        assertEquals(1, realm.where(NullTypes.class).isNull(
                NullTypes.FIELD_LO_OBJECT + "." + NullTypes.FIELD_BYTE_NULL).count());
        // 5 Short
        assertEquals(1, realm.where(NullTypes.class).isNull(
                NullTypes.FIELD_LO_OBJECT + "." + NullTypes.FIELD_SHORT_NULL).count());
        // 6 Integer
        assertEquals(1, realm.where(NullTypes.class).isNull(
                NullTypes.FIELD_LO_OBJECT + "." + NullTypes.FIELD_INTEGER_NULL).count());
        // 7 Long
        assertEquals(1, realm.where(NullTypes.class).isNull(
                NullTypes.FIELD_LO_OBJECT + "." + NullTypes.FIELD_LONG_NULL).count());
        // 8 Float
        assertEquals(1, realm.where(NullTypes.class).isNull(
                NullTypes.FIELD_LO_OBJECT + "." + NullTypes.FIELD_FLOAT_NULL).count());
        // 9 Double
        assertEquals(1, realm.where(NullTypes.class).isNull(
                NullTypes.FIELD_LO_OBJECT + "." + NullTypes.FIELD_DOUBLE_NULL).count());
        // 10 Date
        assertEquals(1, realm.where(NullTypes.class).isNull(
                NullTypes.FIELD_LO_OBJECT + "." + NullTypes.FIELD_DATE_NULL).count());
    }

    // Tests isNull on link's nullable field.
    @Test
    public void isNull_list() {
        populateTestRealmForNullTests(realm);

        // 1 String
        assertEquals(1, realm.where(NullTypes.class).isNull(
                NullTypes.FIELD_LO_LIST + "." + NullTypes.FIELD_STRING_NULL).count());
        // 2 Bytes
        assertEquals(1, realm.where(NullTypes.class).isNull(
                NullTypes.FIELD_LO_LIST + "." + NullTypes.FIELD_BYTES_NULL).count());
        // 3 Boolean
        assertEquals(1, realm.where(NullTypes.class).isNull(
                NullTypes.FIELD_LO_LIST + "." + NullTypes.FIELD_BOOLEAN_NULL).count());
        // 4 Byte
        assertEquals(1, realm.where(NullTypes.class).isNull(
                NullTypes.FIELD_LO_LIST + "." + NullTypes.FIELD_BYTE_NULL).count());
        // 5 Short
        assertEquals(1, realm.where(NullTypes.class).isNull(
                NullTypes.FIELD_LO_LIST + "." + NullTypes.FIELD_SHORT_NULL).count());
        // 6 Integer
        assertEquals(1, realm.where(NullTypes.class).isNull(
                NullTypes.FIELD_LO_LIST + "." + NullTypes.FIELD_INTEGER_NULL).count());
        // 7 Long
        assertEquals(1, realm.where(NullTypes.class).isNull(
                NullTypes.FIELD_LO_LIST + "." + NullTypes.FIELD_LONG_NULL).count());
        // 8 Float
        assertEquals(1, realm.where(NullTypes.class).isNull(
                NullTypes.FIELD_LO_LIST + "." + NullTypes.FIELD_FLOAT_NULL).count());
        // 9 Double
        assertEquals(1, realm.where(NullTypes.class).isNull(
                NullTypes.FIELD_LO_LIST + "." + NullTypes.FIELD_DOUBLE_NULL).count());
        // 10 Date
        assertEquals(1, realm.where(NullTypes.class).isNull(
                NullTypes.FIELD_LO_LIST + "." + NullTypes.FIELD_DATE_NULL).count());
    }

    @Test
    public void isNull_unsupported() {
        long result;

        // Tests for other unsupported null types are in RealmQueryTests

        try {
            result = realm.where(NullTypes.class).isNull(NullTypes.FIELD_LO_OBJECT).count();
            fail("isNull should throw on type LINKING_OBJECT(14) targeting an OBJECT");
        } catch (IllegalArgumentException expected) {
            assertEquals("Illegal Argument: LinkingObject from field fieldObjectNull is not nullable.", expected.getMessage());
        }
        try {
            result = realm.where(NullTypes.class).isNull(NullTypes.FIELD_LO_LIST).count();
            fail("isNull should throw on type LINKING_OBJECT(14) targeting a LIST");
        } catch (IllegalArgumentException expected) {
            assertEquals("Illegal Argument: LinkingObject from field fieldListNull is not nullable.", expected.getMessage());
        }
    }

    @Test
    public void isNull_unsupportedLinkedTypes() {
        RealmQuery<NullTypes> result;

        // Tests for other unsupported null types are in RealmQueryTests

        try {
            result = realm.where(NullTypes.class).isNull(NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_LO_OBJECT);
            fail("isNull should throw on nested linked fields (LINKING_OBJECT => OBJECT)");
        } catch (IllegalArgumentException expected) {
            assertEquals("Illegal Argument: LinkingObject from field fieldObjectNull is not nullable.", expected.getMessage());
        }
        try {
            result = realm.where(NullTypes.class).isNull(NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_LO_LIST);
            fail("isNull should throw on nested linked fields (LINKING_OBJECT => LIST)");
        } catch (IllegalArgumentException expected) {
            assertEquals("Illegal Argument: LinkingObject from field fieldListNull is not nullable.", expected.getMessage());
        }
    }

    // Tests isNotNull on link's nullable field.
    @Test
    public void isNotNull_object() {
        populateTestRealmForNullTests(realm);

        // 1 String
        assertEquals(1, realm.where(NullTypes.class).isNotNull(
                NullTypes.FIELD_LO_OBJECT + "." + NullTypes.FIELD_STRING_NULL).count());
        // 2 Bytes
        assertEquals(1, realm.where(NullTypes.class).isNotNull(
                NullTypes.FIELD_LO_OBJECT + "." + NullTypes.FIELD_BYTES_NULL).count());
        // 3 Boolean
        assertEquals(1, realm.where(NullTypes.class).isNotNull(
                NullTypes.FIELD_LO_OBJECT + "." + NullTypes.FIELD_BOOLEAN_NULL).count());
        // 4 Byte
        assertEquals(1, realm.where(NullTypes.class).isNotNull(
                NullTypes.FIELD_LO_OBJECT + "." + NullTypes.FIELD_BYTE_NULL).count());
        // 5 Short
        assertEquals(1, realm.where(NullTypes.class).isNotNull(
                NullTypes.FIELD_LO_OBJECT + "." + NullTypes.FIELD_SHORT_NULL).count());
        // 6 Integer
        assertEquals(1, realm.where(NullTypes.class).isNotNull(
                NullTypes.FIELD_LO_OBJECT + "." + NullTypes.FIELD_INTEGER_NULL).count());
        // 7 Long
        assertEquals(1, realm.where(NullTypes.class).isNotNull(
                NullTypes.FIELD_LO_OBJECT + "." + NullTypes.FIELD_LONG_NULL).count());
        // 8 Float
        assertEquals(1, realm.where(NullTypes.class).isNotNull(
                NullTypes.FIELD_LO_OBJECT + "." + NullTypes.FIELD_FLOAT_NULL).count());
        // 9 Double
        assertEquals(1, realm.where(NullTypes.class).isNotNull(
                NullTypes.FIELD_LO_OBJECT + "." + NullTypes.FIELD_DOUBLE_NULL).count());
        // 10 Date
        assertEquals(1, realm.where(NullTypes.class).isNotNull(
                NullTypes.FIELD_LO_OBJECT + "." + NullTypes.FIELD_DATE_NULL).count());
    }

    // Tests isNotNull on link's nullable field.
    @Test
    public void isNotNull_list() {
        populateTestRealmForNullTests(realm);

        // 1 String
        assertEquals(1, realm.where(NullTypes.class).isNotNull(
                NullTypes.FIELD_LO_LIST + "." + NullTypes.FIELD_STRING_NULL).count());
        // 2 Bytes
        assertEquals(1, realm.where(NullTypes.class).isNotNull(
                NullTypes.FIELD_LO_LIST + "." + NullTypes.FIELD_BYTES_NULL).count());
        // 3 Boolean
        assertEquals(1, realm.where(NullTypes.class).isNotNull(
                NullTypes.FIELD_LO_LIST + "." + NullTypes.FIELD_BOOLEAN_NULL).count());
        // 4 Byte
        assertEquals(1, realm.where(NullTypes.class).isNotNull(
                NullTypes.FIELD_LO_LIST + "." + NullTypes.FIELD_BYTE_NULL).count());
        // 5 Short
        assertEquals(1, realm.where(NullTypes.class).isNotNull(
                NullTypes.FIELD_LO_LIST + "." + NullTypes.FIELD_SHORT_NULL).count());
        // 6 Integer
        assertEquals(1, realm.where(NullTypes.class).isNotNull(
                NullTypes.FIELD_LO_LIST + "." + NullTypes.FIELD_INTEGER_NULL).count());
        // 7 Long
        assertEquals(1, realm.where(NullTypes.class).isNotNull(
                NullTypes.FIELD_LO_LIST + "." + NullTypes.FIELD_LONG_NULL).count());
        // 8 Float
        assertEquals(1, realm.where(NullTypes.class).isNotNull(
                NullTypes.FIELD_LO_LIST + "." + NullTypes.FIELD_FLOAT_NULL).count());
        // 9 Double
        assertEquals(1, realm.where(NullTypes.class).isNotNull(
                NullTypes.FIELD_LO_LIST + "." + NullTypes.FIELD_DOUBLE_NULL).count());
        // 10 Date
        assertEquals(1, realm.where(NullTypes.class).isNotNull(
                NullTypes.FIELD_LO_LIST + "." + NullTypes.FIELD_DATE_NULL).count());
    }

    @Test
    public void isNotNull_unsupported() {
        long result;

        // Tests for other unsupported not null types are in RealmQueryTests

        try {
            result = realm.where(NullTypes.class).isNotNull(NullTypes.FIELD_LO_OBJECT).count();
            fail("isNotNull should throw on type LINKING_OBJECT(14) targeting an OBJECT");
        } catch (IllegalArgumentException expected) {
            assertEquals("Illegal Argument: LinkingObject from field fieldObjectNull is not nullable.", expected.getMessage());
        }
        try {
            result = realm.where(NullTypes.class).isNotNull(NullTypes.FIELD_LO_LIST).count();
            fail("isNotNull should throw on type LINKING_OBJECT(14) targeting a LIST");
        } catch (IllegalArgumentException expected) {
            assertEquals("Illegal Argument: LinkingObject from field fieldListNull is not nullable.", expected.getMessage());
        }
    }

    @Test
    public void isNotNull_unsupportedLinkedTypes() {
        RealmQuery<NullTypes> result;

        // Tests for other unsupported not null types are in RealmQueryTests

        try {
            result = realm.where(NullTypes.class).isNotNull(NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_LO_OBJECT);
            fail("isNotNull should throw on nested linked fields (LINKING_OBJECT => OBJECT)");
        } catch (IllegalArgumentException expected) {
            assertEquals("Illegal Argument: LinkingObject from field fieldObjectNull is not nullable.", expected.getMessage());
        }
        try {
            result = realm.where(NullTypes.class).isNotNull(NullTypes.FIELD_OBJECT_NULL + "." + NullTypes.FIELD_LO_LIST);
            fail("isNotNull should throw on nested linked fields (LINKING_OBJECT => LIST)");
        } catch (IllegalArgumentException expected) {
            assertEquals("Illegal Argument: LinkingObject from field fieldListNull is not nullable.", expected.getMessage());
        }
    }

    @Test
    public void isEmpty() {
        createIsEmptyDataSet(realm);
        for (RealmFieldType type : SUPPORTED_IS_EMPTY_TYPES) {
            switch (type) {
                case LINKING_OBJECTS:
                    assertEquals(1, realm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_LO_OBJECT).count());
                    assertEquals(1, realm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_LO_LIST).count());
                    break;
                default:
                    // tested in RealmQueryTests
            }
        }
    }

    @Test
    public void isEmpty_acrossLink() {
        createIsEmptyDataSet(realm);
        for (RealmFieldType type : SUPPORTED_IS_EMPTY_TYPES) {
            switch (type) {
                case LINKING_OBJECTS:
                    assertEquals(1, realm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_OBJECT + "." + AllJavaTypes.FIELD_LO_OBJECT).count());
                    assertEquals(1, realm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_OBJECT + "." + AllJavaTypes.FIELD_LO_LIST).count());
                    break;
                default:
                    // tested in RealmQueryTests
            }
        }
    }

    @Test
    public void isEmpty_acrossLinkingObjectObjectLink() {
        createIsEmptyDataSet(realm);
        for (RealmFieldType type : SUPPORTED_IS_EMPTY_TYPES) {
            switch (type) {
                case STRING:
                    assertEquals(1, realm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_LO_OBJECT + "." + AllJavaTypes.FIELD_STRING).count());
                    break;
                case BINARY:
                    assertEquals(1, realm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_LO_OBJECT + "." + AllJavaTypes.FIELD_BINARY).count());
                    break;
                case LIST:
                    assertEquals(1, realm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_LO_OBJECT + "." + AllJavaTypes.FIELD_LIST).count());
                    break;
                case LINKING_OBJECTS:
                    assertEquals(1, realm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_LO_OBJECT + "." + AllJavaTypes.FIELD_LO_OBJECT).count());
                    assertEquals(1, realm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_LO_OBJECT + "." + AllJavaTypes.FIELD_LO_LIST).count());
                    break;
                default:
                    fail("Unknown type: " + type);
            }
        }
    }

    @Test
    public void isEmpty_acrossLinkingObjectListLink() {
        createIsEmptyDataSet(realm);
        for (RealmFieldType type : SUPPORTED_IS_EMPTY_TYPES) {
            switch (type) {
                case STRING:
                    assertEquals(1, realm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_LO_LIST + "." + AllJavaTypes.FIELD_STRING).count());
                    break;
                case BINARY:
                    assertEquals(1, realm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_LO_LIST + "." + AllJavaTypes.FIELD_BINARY).count());
                    break;
                case LIST:
                    assertEquals(1, realm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_LO_LIST + "." + AllJavaTypes.FIELD_LIST).count());
                    break;
                case LINKING_OBJECTS:
                    assertEquals(1, realm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_LO_LIST + "." + AllJavaTypes.FIELD_LO_OBJECT).count());
                    assertEquals(1, realm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_LO_LIST + "." + AllJavaTypes.FIELD_LO_LIST).count());
                    break;
                default:
                    fail("Unknown type: " + type);
            }
        }
    }

    @Test
    public void isNotEmpty() {
        createIsNotEmptyDataSet(realm);
        for (RealmFieldType type : SUPPORTED_IS_NOT_EMPTY_TYPES) {
            switch (type) {
                case LINKING_OBJECTS:
                    assertEquals(1, realm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_LO_OBJECT).count());
                    assertEquals(1, realm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_LO_LIST).count());
                    break;
                default:
                    // tested in RealmQueryTests
            }
        }
    }

    @Test
    public void isNotEmpty_acrossLink() {
        createIsNotEmptyDataSet(realm);
        for (RealmFieldType type : SUPPORTED_IS_NOT_EMPTY_TYPES) {
            switch (type) {
                case LINKING_OBJECTS:
                    // tested in LinkingObjectsQueryTests;
                    assertEquals(1, realm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_OBJECT + "." + AllJavaTypes.FIELD_LO_OBJECT).count());
                    assertEquals(1, realm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_OBJECT + "." + AllJavaTypes.FIELD_LO_LIST).count());
                    break;
                default:
                    // tested in RealmQueryTests
            }
        }
    }

    @Test
    public void isNotEmpty_acrossLinkingObjectObjectLink() {
        createIsEmptyDataSet(realm);
        for (RealmFieldType type : SUPPORTED_IS_EMPTY_TYPES) {
            switch (type) {
                case STRING:
                    assertEquals(1, realm.where(AllJavaTypes.class).isNotEmpty(AllJavaTypes.FIELD_LO_OBJECT + "." + AllJavaTypes.FIELD_STRING).count());
                    break;
                case BINARY:
                    assertEquals(1, realm.where(AllJavaTypes.class).isNotEmpty(AllJavaTypes.FIELD_LO_OBJECT + "." + AllJavaTypes.FIELD_BINARY).count());
                    break;
                case LIST:
                    assertEquals(1, realm.where(AllJavaTypes.class).isNotEmpty(AllJavaTypes.FIELD_LO_OBJECT + "." + AllJavaTypes.FIELD_LIST).count());
                    break;
                case LINKING_OBJECTS:
                    assertEquals(1, realm.where(AllJavaTypes.class).isNotEmpty(AllJavaTypes.FIELD_LO_OBJECT + "." + AllJavaTypes.FIELD_LO_OBJECT).count());
                    assertEquals(1, realm.where(AllJavaTypes.class).isNotEmpty(AllJavaTypes.FIELD_LO_OBJECT + "." + AllJavaTypes.FIELD_LO_LIST).count());
                    break;
                default:
                    fail("Unknown type: " + type);
            }
        }
    }

    @Test
    public void isNotEmpty_acrossLinkingObjectListLink() {
        createIsEmptyDataSet(realm);
        for (RealmFieldType type : SUPPORTED_IS_EMPTY_TYPES) {
            switch (type) {
                case STRING:
                    assertEquals(1, realm.where(AllJavaTypes.class).isNotEmpty(AllJavaTypes.FIELD_LO_LIST + "." + AllJavaTypes.FIELD_STRING).count());
                    break;
                case BINARY:
                    assertEquals(1, realm.where(AllJavaTypes.class).isNotEmpty(AllJavaTypes.FIELD_LO_LIST + "." + AllJavaTypes.FIELD_BINARY).count());
                    break;
                case LIST:
                    assertEquals(1, realm.where(AllJavaTypes.class).isNotEmpty(AllJavaTypes.FIELD_LO_LIST + "." + AllJavaTypes.FIELD_LIST).count());
                    break;
                case LINKING_OBJECTS:
                    assertEquals(1, realm.where(AllJavaTypes.class).isNotEmpty(AllJavaTypes.FIELD_LO_LIST + "." + AllJavaTypes.FIELD_LO_OBJECT).count());
                    assertEquals(1, realm.where(AllJavaTypes.class).isNotEmpty(AllJavaTypes.FIELD_LO_LIST + "." + AllJavaTypes.FIELD_LO_LIST).count());
                    break;
                default:
                    fail("Unknown type: " + type);
            }
        }
    }

    // Similar to the version in TestHelper, but with more Backlinks
    // Creates 3 NullTypes objects. The objects are self-referenced (link) in
    // order to test link queries.
    //
    // +-+--------+------+---------+--------+--------------------+
    // | | string | link | numeric | binary | numeric (not null) |
    // +-+--------+------+---------+--------+--------------------+
    // |0| Fish   |    0 |       1 |    {0} |                  1 |
    // |1| null   | null |    null |   null |                  0 |
    // |2| Horse  |    1 |       3 |  {1,2} |                  3 |
    // +-+--------+------+---------+--------+--------------------+
    private void populateTestRealmForNullTests(Realm testRealm) {
        // 1 String
        String[] words = {"Fish", null, "Horse"};
        // 2 Bytes
        byte[][] binaries = {new byte[]{0}, null, new byte[]{1, 2}};
        // 3 Boolean
        Boolean[] booleans = {false, null, true};
        // Numeric fields will be 1, 0/null, 3
        // 10 Date
        Date[] dates = {new Date(0), null, new Date(10000)};
        NullTypes[] nullTypesArray = new NullTypes[3];

        testRealm.beginTransaction();
        for (int i = 0; i < 3; i++) {
            NullTypes nullTypes = new NullTypes();
            nullTypes.setId(i + 1);
            // 1 String
            nullTypes.setFieldStringNull(words[i]);
            if (words[i] != null) {
                nullTypes.setFieldStringNotNull(words[i]);
            }
            // 2 Bytes
            nullTypes.setFieldBytesNull(binaries[i]);
            if (binaries[i] != null) {
                nullTypes.setFieldBytesNotNull(binaries[i]);
            }
            // 3 Boolean
            nullTypes.setFieldBooleanNull(booleans[i]);
            if (booleans[i] != null) {
                nullTypes.setFieldBooleanNotNull(booleans[i]);
            }
            if (i != 1) {
                int n = i + 1;
                // 4 Byte
                nullTypes.setFieldByteNull((byte) n);
                nullTypes.setFieldByteNotNull((byte) n);
                // 5 Short
                nullTypes.setFieldShortNull((short) n);
                nullTypes.setFieldShortNotNull((short) n);
                // 6 Integer
                nullTypes.setFieldIntegerNull(n);
                nullTypes.setFieldIntegerNotNull(n);
                // 7 Long
                nullTypes.setFieldLongNull((long) n);
                nullTypes.setFieldLongNotNull((long) n);
                // 8 Float
                nullTypes.setFieldFloatNull((float) n);
                nullTypes.setFieldFloatNotNull((float) n);
                // 9 Double
                nullTypes.setFieldDoubleNull((double) n);
                nullTypes.setFieldDoubleNotNull((double) n);
            }
            // 10 Date
            nullTypes.setFieldDateNull(dates[i]);
            if (dates[i] != null) {
                nullTypes.setFieldDateNotNull(dates[i]);
            }

            nullTypesArray[i] = testRealm.copyToRealm(nullTypes);
        }
        nullTypesArray[0].setFieldObjectNull(nullTypesArray[0]);
        nullTypesArray[1].setFieldObjectNull(null);
        nullTypesArray[2].getFieldListNull().add(nullTypesArray[1]);
        testRealm.commitTransaction();
    }
}
