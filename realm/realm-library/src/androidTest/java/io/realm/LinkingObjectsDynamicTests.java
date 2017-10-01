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

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.util.Locale;

import io.realm.entities.AllJavaTypes;
import io.realm.entities.BacklinksSource;
import io.realm.entities.BacklinksTarget;
import io.realm.entities.Cat;
import io.realm.entities.Owner;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.RunTestInLooperThread;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


@RunWith(AndroidJUnit4.class)
public class LinkingObjectsDynamicTests {

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    @Rule
    public final RunInLooperThread looperThread = new RunInLooperThread();
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private Realm realm;
    private DynamicRealm dynamicRealm;

    @Before
    public void setUp() {
        RealmConfiguration realmConfig = configFactory.createConfiguration();
        realm = Realm.getInstance(realmConfig);
        dynamicRealm = DynamicRealm.getInstance(realmConfig);
    }

    @After
    public void tearDown() {
        if (realm != null) {
            realm.close();
        }

        if (dynamicRealm != null) {
            dynamicRealm.close();
        }
    }

    @Test
    public void linkingObjects_classIsNull() throws Exception {
        dynamicRealm.beginTransaction();
        final DynamicRealmObject object = dynamicRealm.createObject(AllJavaTypes.CLASS_NAME, 1L);
        dynamicRealm.commitTransaction();

        try {
            object.linkingObjects(null, AllJavaTypes.FIELD_INT);
            fail();
        } catch (IllegalArgumentException expected) {
            assertEquals(RealmSchema.EMPTY_STRING_MSG, expected.getMessage());
        }
    }

    @Test
    public void linkingObjects_fieldIsNull() throws Exception {
        dynamicRealm.beginTransaction();
        final DynamicRealmObject object = dynamicRealm.createObject(AllJavaTypes.CLASS_NAME, 1L);
        dynamicRealm.commitTransaction();

        try {
            object.linkingObjects(AllJavaTypes.CLASS_NAME, null);
            fail();
        } catch (IllegalArgumentException expected) {
            assertEquals("Non-null 'srcFieldName' required.", expected.getMessage());
        }
    }

    @Test
    public void linkingObjects_nonExistentClass() {
        dynamicRealm.beginTransaction();
        final DynamicRealmObject object = dynamicRealm.createObject(AllJavaTypes.CLASS_NAME, 1L);
        dynamicRealm.commitTransaction();

        try {
            object.linkingObjects("ThisClassDoesNotExist", AllJavaTypes.FIELD_INT);
            fail();
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().startsWith("Class not found"));
        }
    }

    @Test
    public void linkingObjects_nonExistentField() {
        dynamicRealm.beginTransaction();
        final DynamicRealmObject object = dynamicRealm.createObject(AllJavaTypes.CLASS_NAME, 1L);
        dynamicRealm.commitTransaction();

        try {
            object.linkingObjects(AllJavaTypes.CLASS_NAME, "fieldNotExist");
            fail();
        } catch (IllegalArgumentException expected) {
            final String expectedMessage = String.format(Locale.ENGLISH,
                    "Field name '%s' does not exist on schema for '%s'",
                    "fieldNotExist", AllJavaTypes.CLASS_NAME);
            assertEquals(expectedMessage, expected.getMessage());
        }
    }

    @Test
    public void linkingObjects_ignoredExistentField() {
        dynamicRealm.beginTransaction();
        final DynamicRealmObject object = dynamicRealm.createObject(AllJavaTypes.CLASS_NAME, 1L);
        dynamicRealm.commitTransaction();

        try {
            object.linkingObjects(AllJavaTypes.CLASS_NAME, AllJavaTypes.FIELD_IGNORED);
            fail();
        } catch (IllegalArgumentException expected) {
            final String expectedMessage = String.format(Locale.ENGLISH,
                    "Field name '%s' does not exist on schema for '%s'",
                    AllJavaTypes.FIELD_IGNORED, AllJavaTypes.CLASS_NAME);
            assertEquals(expectedMessage, expected.getMessage());
        }
    }

    @Test
    public void linkingObjects_linkQueryNotSupported() throws Exception {
        dynamicRealm.beginTransaction();
        final DynamicRealmObject object = dynamicRealm.createObject(AllJavaTypes.CLASS_NAME, 1L);
        dynamicRealm.commitTransaction();

        try {
            object.linkingObjects(AllJavaTypes.CLASS_NAME, AllJavaTypes.FIELD_OBJECT + "." + AllJavaTypes.FIELD_OBJECT);
            fail();
        } catch (IllegalArgumentException expected) {
            assertEquals(DynamicRealmObject.MSG_LINK_QUERY_NOT_SUPPORTED, expected.getMessage());
        }
    }

    @Test
    public void linkingObjects_invalidFieldType() {
        dynamicRealm.beginTransaction();
        final DynamicRealmObject object = dynamicRealm.createObject(AllJavaTypes.CLASS_NAME, 1L);
        dynamicRealm.commitTransaction();

        for (RealmFieldType fieldType : RealmFieldType.values()) {
            try {
                switch (fieldType) {
                    // skip valid types
                    case OBJECT: // fall-through
                    case LIST:
                        continue;
                    // skip special case
                    case LINKING_OBJECTS:
                        continue;
                    case INTEGER:
                        object.linkingObjects(AllJavaTypes.CLASS_NAME, AllJavaTypes.FIELD_INT);
                        break;
                    case BOOLEAN:
                        object.linkingObjects(AllJavaTypes.CLASS_NAME, AllJavaTypes.FIELD_BOOLEAN);
                        break;
                    case STRING:
                        object.linkingObjects(AllJavaTypes.CLASS_NAME, AllJavaTypes.FIELD_STRING);
                        break;
                    case BINARY:
                        object.linkingObjects(AllJavaTypes.CLASS_NAME, AllJavaTypes.FIELD_BINARY);
                        break;
                    case DATE:
                        object.linkingObjects(AllJavaTypes.CLASS_NAME, AllJavaTypes.FIELD_DATE);
                        break;
                    case FLOAT:
                        object.linkingObjects(AllJavaTypes.CLASS_NAME, AllJavaTypes.FIELD_FLOAT);
                        break;
                    case DOUBLE:
                        object.linkingObjects(AllJavaTypes.CLASS_NAME, AllJavaTypes.FIELD_DOUBLE);
                        break;
                    case INTEGER_LIST:
                        // FIXME zaki50 enable this once Primitive List is implemented
                        //object.linkingObjects(AllJavaTypes.CLASS_NAME, AllJavaTypes.FIELD_INT_LIST);
                        //break;
                        throw new IllegalArgumentException("Unexpected field type");
                    case BOOLEAN_LIST:
                        // FIXME zaki50 enable this once Primitive List is implemented
                        //object.linkingObjects(AllJavaTypes.CLASS_NAME, AllJavaTypes.FIELD_BOOLEAN_LIST);
                        //break;
                        throw new IllegalArgumentException("Unexpected field type");
                    case STRING_LIST:
                        // FIXME zaki50 enable this once Primitive List is implemented
                        //object.linkingObjects(AllJavaTypes.CLASS_NAME, AllJavaTypes.FIELD_STRING_LIST);
                        //break;
                        throw new IllegalArgumentException("Unexpected field type");
                    case BINARY_LIST:
                        // FIXME zaki50 enable this once Primitive List is implemented
                        //object.linkingObjects(AllJavaTypes.CLASS_NAME, AllJavaTypes.FIELD_BINARY_LIST);
                        //break;
                        throw new IllegalArgumentException("Unexpected field type");
                    case DATE_LIST:
                        // FIXME zaki50 enable this once Primitive List is implemented
                        //object.linkingObjects(AllJavaTypes.CLASS_NAME, AllJavaTypes.FIELD_DATE_LIST);
                        //break;
                        throw new IllegalArgumentException("Unexpected field type");
                    case FLOAT_LIST:
                        // FIXME zaki50 enable this once Primitive List is implemented
                        //object.linkingObjects(AllJavaTypes.CLASS_NAME, AllJavaTypes.FIELD_FLOAT_LIST);
                        //break;
                        throw new IllegalArgumentException("Unexpected field type");
                    case DOUBLE_LIST:
                        // FIXME zaki50 enable this once Primitive List is implemented
                        //object.linkingObjects(AllJavaTypes.CLASS_NAME, AllJavaTypes.FIELD_DOUBLE_LIST);
                        //break;
                        throw new IllegalArgumentException("Unexpected field type");
                    default:
                        fail("unknown type: " + fieldType);
                        break;
                }
                fail();
            } catch (IllegalArgumentException expected) {
                assertTrue(expected.getMessage().startsWith("Unexpected field type"));
            }
        }

        // Linking Object fields are implicit and do not exist.
        for (String field : new String[] {AllJavaTypes.FIELD_LO_OBJECT, AllJavaTypes.FIELD_LO_LIST}) {
            try {
                object.linkingObjects(AllJavaTypes.CLASS_NAME, field);
                fail();
            } catch (IllegalArgumentException expected) {
                assertTrue(expected.getMessage().contains("does not exist"));
            }
        }
    }

    @Test
    public void linkingObjects_linkedByOBJECT_backlinksDefinedInModel() {
        final int numSourceOfTarget1 = 3;
        final int numSourceOfTarget2 = 2;
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                final BacklinksTarget target1 = realm.createObject(BacklinksTarget.class);
                target1.setId(1);

                // create sources of target1
                for (int i = 0; i < numSourceOfTarget1; i++) {
                    final BacklinksSource source = realm.createObject(BacklinksSource.class);
                    source.setName("source" + i + "_target1");
                    source.setChild(target1);
                }

                final BacklinksTarget target2 = realm.createObject(BacklinksTarget.class);
                target2.setId(2);

                // create sources of target2
                for (int i = 0; i < numSourceOfTarget2; i++) {
                    final BacklinksSource source = realm.createObject(BacklinksSource.class);
                    source.setName("source" + i + "_target2");
                    source.setChild(target2);
                }

                // target3 has no owner
                final BacklinksTarget target3 = realm.createObject(BacklinksTarget.class);
                target3.setId(3);
            }
        });

        final DynamicRealmObject target1 = dynamicRealm.where(BacklinksTarget.CLASS_NAME).equalTo(BacklinksTarget.FIELD_ID, 1).findFirst();
        final RealmResults<DynamicRealmObject> target1Sources = target1.linkingObjects(BacklinksSource.CLASS_NAME, BacklinksSource.FIELD_CHILD);
        assertNotNull(target1Sources);
        assertEquals(numSourceOfTarget1, target1Sources.size());
        for (DynamicRealmObject target1Source : target1Sources) {
            assertEquals(BacklinksSource.CLASS_NAME, target1Source.getType());
            assertTrue(target1Source.getString(BacklinksSource.FIELD_NAME).endsWith("_target1"));
            assertEquals(target1, target1Source.getObject(BacklinksSource.FIELD_CHILD));
        }

        final DynamicRealmObject target2 = dynamicRealm.where(BacklinksTarget.CLASS_NAME).equalTo(BacklinksTarget.FIELD_ID, 2).findFirst();
        final RealmResults<DynamicRealmObject> target2Sources = target2.linkingObjects(BacklinksSource.CLASS_NAME, BacklinksSource.FIELD_CHILD);
        assertNotNull(target2Sources);
        assertEquals(numSourceOfTarget2, target2Sources.size());
        for (DynamicRealmObject target2Source : target2Sources) {
            assertEquals(BacklinksSource.CLASS_NAME, target2Source.getType());
            assertTrue(target2Source.getString(BacklinksSource.FIELD_NAME).endsWith("_target2"));
            assertEquals(target2, target2Source.getObject(BacklinksSource.FIELD_CHILD));
        }

        final DynamicRealmObject target3 = dynamicRealm.where(BacklinksTarget.CLASS_NAME).equalTo(BacklinksTarget.FIELD_ID, 3).findFirst();
        final RealmResults<DynamicRealmObject> target3Sources = target3.linkingObjects(BacklinksSource.CLASS_NAME, BacklinksSource.FIELD_CHILD);
        assertNotNull(target3Sources);
        assertTrue(target3Sources.isEmpty());
    }

    @Test
    public void linkingObjects_linkedByOBJECT_backlinksNotDefinedInModel() {
        final int numOwnersOfCat1 = 3;
        final int numOwnersOfCat2 = 2;
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                final Cat cat1 = realm.createObject(Cat.class);
                cat1.setName("cat1");

                // create owners of cat1
                for (int i = 0; i < numOwnersOfCat1; i++) {
                    final Owner owner = realm.createObject(Owner.class);
                    owner.setName("owner" + i + "_cat1");
                    owner.setCat(cat1);
                }

                final Cat cat2 = realm.createObject(Cat.class);
                cat2.setName("cat2");

                // create owners of cat2
                for (int i = 0; i < numOwnersOfCat2; i++) {
                    final Owner owner = realm.createObject(Owner.class);
                    owner.setName("owner" + i + "_cat2");
                    owner.setCat(cat2);
                }

                // cat3 has no owner
                final Cat cat3 = realm.createObject(Cat.class);
                cat3.setName("cat3");
            }
        });

        final DynamicRealmObject cat1 = dynamicRealm.where(Cat.CLASS_NAME).equalTo(Cat.FIELD_NAME, "cat1").findFirst();
        final RealmResults<DynamicRealmObject> cat1Owners = cat1.linkingObjects(Owner.CLASS_NAME, Owner.FIELD_CAT);
        assertNotNull(cat1Owners);
        assertEquals(numOwnersOfCat1, cat1Owners.size());
        for (DynamicRealmObject cat1Owner : cat1Owners) {
            assertEquals(Owner.CLASS_NAME, cat1Owner.getType());
            assertTrue(cat1Owner.getString(Owner.FIELD_NAME).endsWith("_cat1"));
            assertEquals(cat1, cat1Owner.getObject(Owner.FIELD_CAT));
        }

        final DynamicRealmObject cat2 = dynamicRealm.where(Cat.CLASS_NAME).equalTo(Cat.FIELD_NAME, "cat2").findFirst();
        final RealmResults<DynamicRealmObject> cat2Owners = cat2.linkingObjects(Owner.CLASS_NAME, Owner.FIELD_CAT);
        assertNotNull(cat2Owners);
        assertEquals(numOwnersOfCat2, cat2Owners.size());
        for (DynamicRealmObject cat2Owner : cat2Owners) {
            assertEquals(Owner.CLASS_NAME, cat2Owner.getType());
            assertTrue(cat2Owner.getString(Owner.FIELD_NAME).endsWith("_cat2"));
            assertEquals(cat2, cat2Owner.getObject(Owner.FIELD_CAT));
        }

        final DynamicRealmObject cat3 = dynamicRealm.where(Cat.CLASS_NAME).equalTo(Cat.FIELD_NAME, "cat3").findFirst();
        final RealmResults<DynamicRealmObject> cat3Owners = cat3.linkingObjects(Owner.CLASS_NAME, Owner.FIELD_CAT);
        assertNotNull(cat3Owners);
        assertTrue(cat3Owners.isEmpty());
    }

    @Test
    public void linkingObjects_linkedByLIST() {
        //           source100          source200        source300
        //            //    \\          \\ || //
        //        target1   target2     target2
        //
        //  // = list ref
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                final AllJavaTypes target1 = realm.createObject(AllJavaTypes.class, 1L);
                final AllJavaTypes target2 = realm.createObject(AllJavaTypes.class, 2L);
                final AllJavaTypes target3 = realm.createObject(AllJavaTypes.class, 3L);

                final AllJavaTypes source100 = realm.createObject(AllJavaTypes.class, 100L);
                source100.getFieldList().add(target1);
                source100.getFieldList().add(target2);

                // list contains three target2s
                final AllJavaTypes source200 = realm.createObject(AllJavaTypes.class, 200L);
                source200.getFieldList().add(target2);
                source200.getFieldList().add(target2);
                source200.getFieldList().add(target2);
            }
        });

        final DynamicRealmObject target1 = dynamicRealm.where(AllJavaTypes.CLASS_NAME).equalTo(AllJavaTypes.FIELD_ID, 1L).findFirst();
        final DynamicRealmObject target2 = dynamicRealm.where(AllJavaTypes.CLASS_NAME).equalTo(AllJavaTypes.FIELD_ID, 2L).findFirst();
        final DynamicRealmObject target3 = dynamicRealm.where(AllJavaTypes.CLASS_NAME).equalTo(AllJavaTypes.FIELD_ID, 3L).findFirst();

        // tests sources of target1
        final RealmResults<DynamicRealmObject> target1Sources = target1.linkingObjects(AllJavaTypes.CLASS_NAME, AllJavaTypes.FIELD_LIST);
        assertNotNull(target1Sources);
        assertEquals(1, target1Sources.size());
        assertEquals(AllJavaTypes.CLASS_NAME, target1Sources.first().getType());
        assertEquals(100L, target1Sources.first().getLong(AllJavaTypes.FIELD_ID));
        assertTrue(target1Sources.first().getList(AllJavaTypes.FIELD_LIST).contains(target1));
        assertTrue(target1Sources.first().getList(AllJavaTypes.FIELD_LIST).contains(target2));
        assertFalse(target1Sources.first().getList(AllJavaTypes.FIELD_LIST).contains(target3));

        // tests sources of target2
        final RealmResults<DynamicRealmObject> target2Sources = target2.linkingObjects(AllJavaTypes.CLASS_NAME, AllJavaTypes.FIELD_LIST);
        assertNotNull(target2Sources);
        // if a source (in this test, source200) contains multiple references to a target in one RealmList, those must not be aggregated.
        assertEquals(4, target2Sources.size());
        boolean source100Found = false;
        boolean source200Found = false;
        for (DynamicRealmObject target2Source : target2Sources) {
            final long idValue = target2Source.getLong(AllJavaTypes.FIELD_ID);
            if (idValue == 100L) {
                source100Found = true;
            } else if (idValue == 200L) {
                source200Found = true;
            } else {
                fail("unexpected id value: " + idValue);
            }

            assertEquals(AllJavaTypes.CLASS_NAME, target2Source.getType());
            assertTrue(target2Source.getList(AllJavaTypes.FIELD_LIST).contains(target2));
            assertFalse(target2Source.getList(AllJavaTypes.FIELD_LIST).contains(target3));
        }
        assertTrue(source100Found);
        assertTrue(source200Found);

        // tests sources of target3
        final RealmResults<DynamicRealmObject> target3Sources = target3.linkingObjects(AllJavaTypes.CLASS_NAME, AllJavaTypes.FIELD_LIST);
        assertNotNull(target3Sources);
        assertTrue(target3Sources.isEmpty());

        dynamicRealm.executeTransaction(new DynamicRealm.Transaction() {
            @Override
            public void execute(DynamicRealm realm) {
                final DynamicRealmObject source200 = dynamicRealm.where(AllJavaTypes.CLASS_NAME).equalTo(AllJavaTypes.FIELD_ID, 200L).findFirst();
                // remove last reference in the list
                source200.getList(AllJavaTypes.FIELD_LIST).remove(2);
            }
        });

        // backlinks are also updated
        assertEquals(3, target2Sources.size());
    }

    @Test
    @RunTestInLooperThread
    public void linkingObjects_IllegalStateException_ifNotYetLoaded() {
        final Realm realm = looperThread.getRealm();

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                final BacklinksTarget target1 = realm.createObject(BacklinksTarget.class);
                target1.setId(1);

                final BacklinksSource source = realm.createObject(BacklinksSource.class);
                source.setChild(target1);
            }
        });

        final DynamicRealm dynamicRealm = DynamicRealm.getInstance(looperThread.getConfiguration());
        try {
            final DynamicRealmObject targetAsync = dynamicRealm.where(BacklinksTarget.CLASS_NAME)
                    .equalTo(BacklinksTarget.FIELD_ID, 1L).findFirstAsync();
            // precondition
            assertFalse(targetAsync.isLoaded());

            thrown.expect(IllegalStateException.class);
            targetAsync.linkingObjects(BacklinksSource.CLASS_NAME, BacklinksSource.FIELD_CHILD);
        } finally {
            dynamicRealm.close();
        }
    }

    @Test
    @RunTestInLooperThread
    public void linkingObjects_IllegalStateException_ifDeleted() {
        final Realm realm = looperThread.getRealm();

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                final BacklinksTarget target1 = realm.createObject(BacklinksTarget.class);
                target1.setId(1);

                final BacklinksSource source = realm.createObject(BacklinksSource.class);
                source.setChild(target1);
            }
        });

        final DynamicRealm dynamicRealm = DynamicRealm.getInstance(looperThread.getConfiguration());
        try {
            final DynamicRealmObject target = dynamicRealm.where(BacklinksTarget.CLASS_NAME)
                    .equalTo(BacklinksTarget.FIELD_ID, 1L).findFirst();

            dynamicRealm.executeTransaction(new DynamicRealm.Transaction() {
                @Override
                public void execute(DynamicRealm realm) {
                    target.deleteFromRealm();
                }
            });

            // precondition
            assertFalse(target.isValid());

            thrown.expect(IllegalStateException.class);
            target.linkingObjects(BacklinksSource.CLASS_NAME, BacklinksSource.FIELD_CHILD);
        } finally {
            dynamicRealm.close();
        }
    }

    @Test
    public void dynamicQuery_invalidSyntax() {
        String[] invalidBacklinks = new String[] {
                "linkingObject(x",
                "linkingObject(x.y",
                "linkingObject(x.y)",
                "linkingObject(x.y).",
                "linkingObject(x.y)..z",
                "linkingObject(x.y).linkingObjects(x1.y1).z"
        };
    }
}
