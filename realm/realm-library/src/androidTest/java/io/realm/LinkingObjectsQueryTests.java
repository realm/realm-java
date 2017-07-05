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

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

import io.realm.entities.AllJavaTypes;
import io.realm.entities.BacklinksTarget;
import io.realm.entities.NullTypes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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

        // row 0: backlink to rows 1 and 2; row 1 link to row 0, included
        // row 1: no backlink, not included
        // row 2: no backlink, not included
        // row 3: no backlink, not included
        // summary: 1 row (gen1)
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

        // row 0: no link, not included
        // row 1: link to row 0, backlink to rows 1 and 2, row 2 has id < 2, included
        // row 2: link to row 0, backlink to rows 1 and 2, row 2 has id < 2, included
        // row 3: no link, not included
        // summary: 2 rows (gen2A and gen2B)
        RealmResults<AllJavaTypes> result = realm.where(AllJavaTypes.class)
                .lessThan(AllJavaTypes.FIELD_OBJECT + "." + AllJavaTypes.FIELD_LO_OBJECT + "." + AllJavaTypes.FIELD_ID, 2)
                .findAll();
        assertEquals(2, result.size());
        assertTrue(result.contains(gen2A));
        assertTrue(result.contains(gen2B));
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
    public void isEmpty_linkingObjects() {
        createIsEmptyDataSet(realm);
        for (RealmFieldType type : SUPPORTED_IS_EMPTY_TYPES) {
            switch (type) {
                case LINKING_OBJECTS:
                    // Row 0: backlink to row 0; not included
                    // Row 1: backlink to row 1; not included
                    // Row 2: no backlink; included
                    assertEquals(1, realm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_LO_OBJECT).count());
                    // Only row 1 has a linklist (and a backlink)
                    assertEquals(1, realm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_LO_LIST).count());
                    break;
                default:
                    // tested in RealmQueryTests
            }
        }
    }

    @Test
    public void isEmpty_multipleModelClasses() {
        createLinkedDataSet(realm);
        assertEquals(1, realm.where(BacklinksTarget.class).isEmpty(BacklinksTarget.FIELD_PARENTS).count());
    }

    @Test(expected = IllegalArgumentException.class)
    public void equalTo_linkingObjectLast() {
        createLinkedDataSet(realm);
        realm.where(BacklinksTarget.class).equalTo(BacklinksTarget.FIELD_PARENTS, "parents");
    }

    @Test
    public void isEmpty_acrossLink() {
        createIsEmptyDataSet(realm);
        for (RealmFieldType type : SUPPORTED_IS_EMPTY_TYPES) {
            switch (type) {
                case LINKING_OBJECTS:
                    // Rows 0 and 1 are not included as they are linked to another row through FIELD_OBJECT
                    // Row 2 is included (no link)
                    assertEquals(1, realm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_OBJECT + "." + AllJavaTypes.FIELD_LO_OBJECT).count());
                    // Row 0 has link to row 0 which has a backlink (list); not included
                    // Row 1 has link to row 1 which has a backlink (list); not included
                    // Row 2 has no link; included
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
                    // Row 0: backlink to row 0, linklist is empty; included
                    // Row 1: backlink to row 1, linklist to row 0; not included
                    // Row 2: no backlink; included
                    assertEquals(2, realm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_LO_OBJECT + "." + AllJavaTypes.FIELD_LIST).count());
                    break;
                case LINKING_OBJECTS:
                    // Both row 0 and 1 have a link/backlink; not included
                    // row 2 has no link/backlink and an empty list; included
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
        assertEquals(3, realm.where(AllJavaTypes.class).findAll().size());
        for (RealmFieldType type : SUPPORTED_IS_EMPTY_TYPES) {
            switch (type) {
                case STRING:
                    // Row 2 included (has no backlink)
                    assertEquals(1, realm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_LO_LIST + "." + AllJavaTypes.FIELD_STRING).count());
                    break;
                case BINARY:
                    // Row 2 included (has no backlink)
                    assertEquals(1, realm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_LO_LIST + "." + AllJavaTypes.FIELD_BINARY).count());
                    break;
                case LIST:
                    assertEquals(1, realm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_LO_LIST + "." + AllJavaTypes.FIELD_LIST).count());
                    break;
                case LINKING_OBJECTS:
                    // Row 0: Backlink (list) to row 1, row 1 backlink to row 1; not included
                    // Row 1: Backlink (list) to row 2, row 2 no backlink; included
                    // Row 2: No backlink (list); included
                    assertEquals(2, realm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_LO_LIST + "." + AllJavaTypes.FIELD_LO_OBJECT).count());

                    // Step 1:
                    //  Row 0 skipped; FIELD_LO_LIST.count > 0
                    //  Row 1 included; FIELD_LO_LIST.count() == 0
                    //
                    // Step 2: now checking Row 2
                    // Row 0 included: goes to Row 1 where FIELD_LO_LIST.count() == 0
                    assertEquals(2, realm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_LO_LIST + "." + AllJavaTypes.FIELD_LO_LIST).count());
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
                    // Row 0 and 1 have a link/backlink so no row is empty
                    assertEquals(0, realm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_LO_OBJECT).count());
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
                    // Row 0 and Row 1 have link/backlink - no empty
                    assertEquals(0, realm.where(AllJavaTypes.class).isEmpty(AllJavaTypes.FIELD_OBJECT + "." + AllJavaTypes.FIELD_LO_OBJECT).count());
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
                    // Row 0: Follow link to row 0, and FIELD_STRING is empty ("")
                    // Row 1: Follow link to row 1, and FIELD_STRING is not empty ("Foo")
                    assertEquals(1, realm.where(AllJavaTypes.class).isNotEmpty(AllJavaTypes.FIELD_LO_OBJECT + "." + AllJavaTypes.FIELD_STRING).count());
                    break;
                case BINARY:
                    assertEquals(1, realm.where(AllJavaTypes.class).isNotEmpty(AllJavaTypes.FIELD_LO_OBJECT + "." + AllJavaTypes.FIELD_BINARY).count());
                    break;
                case LIST:
                    assertEquals(1, realm.where(AllJavaTypes.class).isNotEmpty(AllJavaTypes.FIELD_LO_OBJECT + "." + AllJavaTypes.FIELD_LIST).count());
                    break;
                case LINKING_OBJECTS:
                    // Both row 0 and 1 have a link/backlink
                    assertEquals(2, realm.where(AllJavaTypes.class).isNotEmpty(AllJavaTypes.FIELD_LO_OBJECT + "." + AllJavaTypes.FIELD_LO_OBJECT).count());

                    // Row 0: Backlink to row 0, backlink list to row 1; included
                    // Row 1: Backlink to row 1, backlink list to row 2; included
                    // Row 2: No backlink; not empty
                    assertEquals(2, realm.where(AllJavaTypes.class).isNotEmpty(AllJavaTypes.FIELD_LO_OBJECT + "." + AllJavaTypes.FIELD_LO_LIST).count());
                    break;
                default:
                    fail("Unknown type: " + type);
            }
        }
    }

    @Test
    public void isNotEmpty_acrossLinkingObjectListLink() {
        createIsEmptyDataSet(realm);
        assertEquals(3, realm.where(AllJavaTypes.class).findAll().size());
        for (RealmFieldType type : SUPPORTED_IS_EMPTY_TYPES) {
            switch (type) {
                case STRING:
                    // Row 0: Backlink list to row 1, string not empty ("Foo"); included
                    // Row 1: Backlink list to row 2, string is empty; not included
                    // Row 2: No backlink list; not included
                    assertEquals(1, realm.where(AllJavaTypes.class).isNotEmpty(AllJavaTypes.FIELD_LO_LIST + "." + AllJavaTypes.FIELD_STRING).count());
                    break;
                case BINARY:
                    assertEquals(1, realm.where(AllJavaTypes.class).isNotEmpty(AllJavaTypes.FIELD_LO_LIST + "." + AllJavaTypes.FIELD_BINARY).count());
                    break;
                case LIST:
                    // Row 0: Backlink list to row 1, list to row 0; included
                    // Row 1: Backlink list to row 2, list to row 1; included
                    // Row 2: No backlink list; not included
                    assertEquals(2, realm.where(AllJavaTypes.class).isNotEmpty(AllJavaTypes.FIELD_LO_LIST + "." + AllJavaTypes.FIELD_LIST).count());
                    break;
                case LINKING_OBJECTS:
                    assertEquals(1, realm.where(AllJavaTypes.class).isNotEmpty(AllJavaTypes.FIELD_LO_LIST + "." + AllJavaTypes.FIELD_LO_OBJECT).count());

                    // Row 0: Backlink list to row 1, backlink list to row 2; included
                    // Row 1: Backlink list to row 2, empty backlink list; not included
                    // Row 2: Empty backlink list; not included
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
    // +-+--------+------+---------+--------+--------------------+----------+
    // | | string | link | numeric | binary | numeric (not null) | linklist |
    // +-+--------+------+---------+--------+--------------------+----------+
    // |0| Fish   |    0 |       1 |    {0} |                  1 |      [0] |
    // |1| null   |    2 |    null |   null |                  0 |      [2] |
    // |2| Horse  | null |       3 |  {1,2} |                  3 |     null |
    // +-+--------+------+---------+--------+--------------------+----------+
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
        nullTypesArray[1].setFieldObjectNull(nullTypesArray[2]);
        nullTypesArray[2].setFieldObjectNull(null);

        nullTypesArray[0].getFieldListNull().add(nullTypesArray[1]);
        nullTypesArray[1].getFieldListNull().add(nullTypesArray[2]);
        nullTypesArray[2].getFieldListNull().clear(); // just to be sure
        testRealm.commitTransaction();
    }
}
