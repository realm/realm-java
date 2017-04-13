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
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.Locale;

import io.realm.entities.AllJavaTypes;
import io.realm.entities.BacklinksSource;
import io.realm.entities.BacklinksTarget;
import io.realm.entities.Cat;
import io.realm.entities.Owner;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


@RunWith(AndroidJUnit4.class)
public class LinkingObjectsDynamicTests {

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

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
    public void linkingObjects_ClassIsNull() throws Exception {
        dynamicRealm.beginTransaction();
        final DynamicRealmObject object = dynamicRealm.createObject(AllJavaTypes.CLASS_NAME, 1L);
        dynamicRealm.commitTransaction();

        try {
            object.linkingObjects(null, AllJavaTypes.FIELD_INT);
            fail();
        } catch (IllegalArgumentException expected) {
            final Field messageField = StandardRealmSchema.class.getDeclaredField("EMPTY_STRING_MSG");
            messageField.setAccessible(true);
            assertEquals(messageField.get(null), expected.getMessage());
        }
    }

    @Test
    public void linkingObjects_FieldIsNull() throws Exception {
        dynamicRealm.beginTransaction();
        final DynamicRealmObject object = dynamicRealm.createObject(AllJavaTypes.CLASS_NAME, 1L);
        dynamicRealm.commitTransaction();

        try {
            object.linkingObjects(AllJavaTypes.CLASS_NAME, null);
            fail();
        } catch (IllegalArgumentException expected) {
            assertEquals("Column name can not be null.", expected.getMessage());
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
                        // skip unsupported types
                    case UNSUPPORTED_TABLE: // fall-through
                    case UNSUPPORTED_MIXED: // fall-through
                    case UNSUPPORTED_DATE:
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
                    default:
                        fail("unknown type: " + fieldType);
                        break;
                }
                fail();
            } catch (IllegalArgumentException expected) {
                assertTrue(expected.getMessage().startsWith("Unexpected field type"));
            }
        }
    }

    @Test
    public void linkingObjects_definedInModel() {
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
    public void linkingObjects_notDefinedInModel() {
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
