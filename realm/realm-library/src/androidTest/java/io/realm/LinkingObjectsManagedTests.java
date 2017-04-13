/*
 * Copyright 2016 Realm Inc.
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

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.entities.AllJavaTypes;
import io.realm.entities.BacklinksMissingFieldSourceModule;
import io.realm.entities.BacklinksMissingFieldTargetModule;
import io.realm.entities.BacklinksSource;
import io.realm.entities.BacklinksTarget;
import io.realm.entities.BacklinksWrongTypeSourceModule;
import io.realm.entities.BacklinksWrongTypeTargetModule;
import io.realm.exceptions.RealmException;
import io.realm.exceptions.RealmMigrationNeededException;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.RunTestInLooperThread;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class LinkingObjectsManagedTests {
    private interface PostConditions {
        void run(Realm realm);
    }

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();
    @Rule
    public final RunInLooperThread looperThread = new RunInLooperThread();
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private Realm realm;
    private Context context;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getContext();

        RealmConfiguration realmConfig = configFactory.createConfiguration();
        realm = Realm.getInstance(realmConfig);
    }

    @After
    public void tearDown() {
        if (realm != null) {
            realm.close();
        }
    }

    // Setting the linked object field creates the correct backlink
    @Test
    public void basic_singleBacklinkObject() {
        realm.beginTransaction();
        AllJavaTypes child = realm.createObject(AllJavaTypes.class, 1);
        AllJavaTypes parent = realm.createObject(AllJavaTypes.class, 2);
        parent.setFieldObject(child);
        realm.commitTransaction();

        assertEquals(1, child.getObjectParents().size());
        assertTrue(child.getObjectParents().contains(parent));
    }

    // Setting a linked list field creates the correct backlink
    @Test
    public void basic_singleBacklinkList() {
        realm.beginTransaction();
        AllJavaTypes child = realm.createObject(AllJavaTypes.class, 1);
        AllJavaTypes parent = realm.createObject(AllJavaTypes.class, 2);
        parent.getFieldList().add(child);
        realm.commitTransaction();

        assertEquals(1, child.getListParents().size());
        assertTrue(child.getListParents().contains(parent));
    }

    // Setting multiple object links creates multiple backlinks
    @Test
    public void basic_multipleBacklinksObject() {
        realm.beginTransaction();
        AllJavaTypes child = realm.createObject(AllJavaTypes.class, 1);
        AllJavaTypes parent1 = realm.createObject(AllJavaTypes.class, 2);
        AllJavaTypes parent2 = realm.createObject(AllJavaTypes.class, 3);
        parent1.setFieldObject(child);
        parent2.setFieldObject(child);
        realm.commitTransaction();
        assertEquals(2, child.getObjectParents().size());
    }

    // Setting multiple list links creates multiple backlinks
    @Test
    public void basic_multipleBacklinksList() {
        realm.beginTransaction();
        AllJavaTypes child = realm.createObject(AllJavaTypes.class, 1);
        AllJavaTypes parent1 = realm.createObject(AllJavaTypes.class, 2);
        AllJavaTypes parent2 = realm.createObject(AllJavaTypes.class, 3);
        parent1.getFieldList().add(child);
        parent2.getFieldList().add(child);
        realm.commitTransaction();
        assertEquals(2, child.getListParents().size());
    }

    // Adding multiple list links creates multiple backlinks,
    // even if the links are to a single object
    @Test
    public void basic_multipleReferencesFromParentList() {
        realm.beginTransaction();
        AllJavaTypes child = realm.createObject(AllJavaTypes.class, 1);
        AllJavaTypes parent = realm.createObject(AllJavaTypes.class, 2);
        parent.getFieldList().add(child);
        parent.getFieldList().add(child);
        realm.commitTransaction();

        // One entry for each reference, so two references from a LinkList will
        // result in two backlinks.
        assertEquals(2, child.getListParents().size());
        assertEquals(parent, child.getListParents().first());
        assertEquals(parent, child.getListParents().last());
    }

    // This test reproduces https://github.com/realm/realm-java/issues/4487
    @Test
    public void issue4487_checkIfTableIsCorrect() {
        realm.beginTransaction();
        final BacklinksTarget target = realm.createObject(BacklinksTarget.class);
        target.setId(1);
        final BacklinksSource source = realm.createObject(BacklinksSource.class);
        source.setChild(target);
        realm.commitTransaction();

        final RealmResults<BacklinksSource> parents = target.getParents();
        final BacklinksSource sourceFromBacklinks = parents.first();

        assertEquals(source, sourceFromBacklinks);
    }

    // A listener registered on the backlinked object should not be called after the listener is removed
    @Test
    @RunTestInLooperThread
    public void notification_notSentAfterUnregisterListenerModelObject() {
        final Realm looperThreadRealm = looperThread.realm;

        looperThreadRealm.beginTransaction();
        AllJavaTypes child = looperThreadRealm.createObject(AllJavaTypes.class, 10);
        looperThreadRealm.commitTransaction();

        RealmChangeListener<AllJavaTypes> listener = new RealmChangeListener<AllJavaTypes>() {
            @Override
            public void onChange(AllJavaTypes object) {
                fail("Not expecting notification after unregister");
            }
        };
        child.addChangeListener(listener);
        child.removeChangeListener(listener);

        looperThreadRealm.beginTransaction();
        AllJavaTypes parent = looperThreadRealm.createObject(AllJavaTypes.class, 1);
        parent.setFieldObject(child);
        looperThreadRealm.commitTransaction();

        verifyPostConditions(
            looperThreadRealm,
            new PostConditions() {
                @Override
                public void run(Realm realm) {
                    assertEquals(2, looperThreadRealm.where(AllJavaTypes.class).findAll().size());
                }
            },
            child, parent);
    }

    // A listener registered on the backlinked field should be called when a commit adds a backlink
    @Test
    @RunTestInLooperThread
    public void notification_onCommitRealmResults() {
        final Realm looperThreadRealm = looperThread.realm;

        looperThreadRealm.beginTransaction();
        AllJavaTypes child = looperThreadRealm.createObject(AllJavaTypes.class, 10);
        looperThreadRealm.commitTransaction();

        final AtomicInteger counter = new AtomicInteger(0);
        RealmChangeListener<RealmResults<AllJavaTypes>> listener = new RealmChangeListener<RealmResults<AllJavaTypes>>() {
            @Override
            public void onChange(RealmResults<AllJavaTypes> object) {
                counter.incrementAndGet();
            }
        };
        child.getObjectParents().addChangeListener(listener);

        looperThreadRealm.beginTransaction();
        AllJavaTypes parent = looperThreadRealm.createObject(AllJavaTypes.class, 1);
        parent.setFieldObject(child);
        looperThreadRealm.commitTransaction();

        verifyPostConditions(
            looperThreadRealm,
            new PostConditions() {
                @Override
                public void run(Realm realm) {
                    assertEquals(2, looperThreadRealm.where(AllJavaTypes.class).findAll().size());
                    assertEquals(1, counter.get());
                }
            },
            child, parent);
    }

    // A listener registered on the backlinked field should not be called after the listener is removed
    @Test
    @RunTestInLooperThread
    public void notification_notSentAfterUnregisterListenerRealmResults() {
        final Realm looperThreadRealm = looperThread.realm;

        looperThreadRealm.beginTransaction();
        AllJavaTypes child = looperThreadRealm.createObject(AllJavaTypes.class, 10);
        looperThreadRealm.commitTransaction();

        RealmChangeListener<RealmResults<AllJavaTypes>> listener = new RealmChangeListener<RealmResults<AllJavaTypes>>() {
            @Override
            public void onChange(RealmResults<AllJavaTypes> object) {
                fail("Not expecting notification after unregister");
            }
        };
        RealmResults<AllJavaTypes> objParents = child.getObjectParents();
        objParents.addChangeListener(listener);
        objParents.removeChangeListener(listener);

        looperThreadRealm.beginTransaction();
        AllJavaTypes parent = looperThreadRealm.createObject(AllJavaTypes.class, 1);
        parent.setFieldObject(child);
        looperThreadRealm.commitTransaction();

        verifyPostConditions(
            looperThreadRealm,
            new PostConditions() {
                @Override
                public void run(Realm realm) {
                    assertEquals(2, looperThreadRealm.where(AllJavaTypes.class).findAll().size());
                }
            },
            child, parent);
    }

    // A listener registered on the backlinked object should be called when a backlinked object is deleted
    @Test
    @RunTestInLooperThread
    public void notification_onDeleteRealmResults() {
        final Realm looperThreadRealm = looperThread.realm;

        looperThreadRealm.beginTransaction();
        AllJavaTypes child = looperThreadRealm.createObject(AllJavaTypes.class, 10);
        AllJavaTypes parent = looperThreadRealm.createObject(AllJavaTypes.class, 1);
        parent.setFieldObject(child);
        looperThreadRealm.commitTransaction();

        final AtomicInteger counter = new AtomicInteger(0);
        RealmChangeListener<RealmResults<AllJavaTypes>> listener = new RealmChangeListener<RealmResults<AllJavaTypes>>() {
            @Override
            public void onChange(RealmResults<AllJavaTypes> object) {
                counter.incrementAndGet();
            }
        };
        child.getObjectParents().addChangeListener(listener);

        looperThreadRealm.beginTransaction();
        looperThreadRealm.where(AllJavaTypes.class).equalTo("fieldId", 1).findAll().deleteAllFromRealm();
        looperThreadRealm.commitTransaction();

        verifyPostConditions(
            looperThreadRealm,
            new PostConditions() {
                @Override
                public void run(Realm realm) {
                    assertEquals(1, looperThreadRealm.where(AllJavaTypes.class).findAll().size());
                    assertEquals(1, counter.get());
                }
            },
            child, parent);
    }

    // A listener registered on the backlinked object should not called for an unrelated change
    @Test
    @RunTestInLooperThread
    public void notification_notSentOnUnrelatedChangeRealmResults() {
        final Realm looperThreadRealm = looperThread.realm;

        looperThreadRealm.beginTransaction();
        AllJavaTypes child = looperThreadRealm.createObject(AllJavaTypes.class, 10);
        AllJavaTypes parent = looperThreadRealm.createObject(AllJavaTypes.class, 1);
        looperThreadRealm.commitTransaction();

        RealmChangeListener<RealmResults<AllJavaTypes>> listener = new RealmChangeListener<RealmResults<AllJavaTypes>>() {
            @Override
            public void onChange(RealmResults<AllJavaTypes> object) {
                fail("Not expecting notification after unregister");
            }
        };
        child.getObjectParents().addChangeListener(listener);

        looperThreadRealm.beginTransaction();
        looperThreadRealm.where(AllJavaTypes.class).equalTo("fieldId", 1).findAll().deleteAllFromRealm();
        looperThreadRealm.commitTransaction();

        verifyPostConditions(
            looperThreadRealm,
            new PostConditions() {
                @Override
                public void run(Realm realm) {
                    assertEquals(1, looperThreadRealm.where(AllJavaTypes.class).findAll().size());
                }
            },
            child, parent);
    }

    // Fields annotated with @LinkingObjects should not be affected by JSON updates
    @Test
    public void json_updateObject() {
        realm.beginTransaction();
        AllJavaTypes child = realm.createObject(AllJavaTypes.class, 1);
        AllJavaTypes parent = realm.createObject(AllJavaTypes.class, 2);
        parent.setFieldObject(child);
        realm.commitTransaction();

        RealmResults<AllJavaTypes> parents = child.getObjectParents();
        assertNotNull(parents);
        assertEquals(1, parents.size());
        assertTrue(parents.contains(parent));

        realm.beginTransaction();
        try {
            realm.createOrUpdateAllFromJson(AllJavaTypes.class, "[{ \"fieldId\" : 1, \"objectParents\" : null }]");
        } catch (RealmException e) {
            fail("Failed loading JSON" + e);
        }
        realm.commitTransaction();

        parents = child.getObjectParents();
        assertNotNull(parents);
        assertEquals(1, parents.size());
        assertTrue(parents.contains(parent));
    }

    // Fields annotated with @LinkingObjects should not be affected by JSON updates
    @Test
    public void json_updateList() {
        realm.beginTransaction();
        AllJavaTypes child = realm.createObject(AllJavaTypes.class, 1);
        AllJavaTypes parent = realm.createObject(AllJavaTypes.class, 2);
        parent.getFieldList().add(child);
        realm.commitTransaction();

        RealmResults<AllJavaTypes> parents = child.getListParents();
        assertNotNull(parents);
        assertEquals(1, parents.size());
        assertTrue(parents.contains(parent));

        realm.beginTransaction();
        try {
            realm.createOrUpdateAllFromJson(AllJavaTypes.class, "[{ \"fieldId\" : 1, \"listParents\" : null }]");
        } catch (RealmException e) {
            fail("Failed loading JSON" + e);
        }
        realm.commitTransaction();

        parents = child.getListParents();
        assertNotNull(parents);
        assertEquals(1, parents.size());
        assertTrue(parents.contains(parent));
    }

    /**
     * Table validation should fail if the backinked column already exists in the target table.
     * The realm `backlinks-fieldInUse.realm` contains the classes `BacklinksSource` and `BacklinksTarget`
     * except that in the definition of `BacklinksTarget`, the field parent is defined as:
     * <pre>
     * {@code
     *     private RealmList<BacklinksSource> parents;
     * }
     * </pre>
     */
    @Test
    public void migration_backlinkedFieldInUse() {
        final String realmName = "backlinks-fieldInUse.realm";

        RealmConfiguration realmConfig = configFactory.createConfigurationBuilder()
            .name(realmName)
            .schema(BacklinksSource.class, BacklinksTarget.class)
            .build();

        try {
            configFactory.copyRealmFromAssets(context, realmName, realmName);

            Realm localRealm = Realm.getInstance(realmConfig);
            localRealm.close();
            fail("A migration should have been required");
        } catch (IOException e) {
            fail("Failed copying realm");
        } catch (RealmMigrationNeededException expected) {
            assertTrue(expected.getMessage().contains("Field count is"));
        } finally {
            Realm.deleteRealm(realmConfig);
        }
    }

    /**
     * Table validation should fail if the backinked column points to a non-existent class.
     * The realm `backlinks-missingSourceClass.realm` contains two tables very like those
     * defined by `BacklinksSource` and `BacklinksTarget`.  In it, though, the source class
     * is named XXXBacklinksSource, like so:
     * <pre>
     * {@code
     * @LinkingObjects("child")
     *     private final RealmResults<XXXBacklinksSource> parents = null;
     * }
     * </pre>
     * If the both classes were used in the configuration, the test would fail because of the
     * missing class.  Since the configuration contains only the single class `BacklinksTarget`,
     * basic validation passes.  Backlink validation, however, should fail, seeking the
     * `BacklinksSource` table.
     */
    @Test
    public void migration_backlinkedSourceClassDoesntExist() throws IOException {
        final String realmName = "backlinks-missingSourceClass.realm";

        RealmConfiguration realmConfig = configFactory.createConfigurationBuilder()
            .name(realmName)
            .schema(BacklinksTarget.class)
            .build();

        try {
            configFactory.copyRealmFromAssets(context, realmName, realmName);

            Realm localRealm = Realm.getInstance(realmConfig);
            localRealm.close();
            fail("A migration should have been required");
        } catch (IOException e) {
            fail("Failed copying realm");
        } catch (RealmMigrationNeededException expected) {
            assertTrue(expected.getMessage().contains("Cannot find source class"));
        } finally {
            Realm.deleteRealm(realmConfig);
        }
    }

    /**
     * Table validation should fail if the backlinked column points to a non-existent field in the source class.
     * This test is quite a chore to construct!
     * The realm `backlinks-missingSourceField.realm` was constructed with classes `BacklinksMissingFieldTarget`
     * and `BacklinksMissingFieldSource`.  They are identical in their definitions to `BacklinkSource` and
     * `BacklinkTarget` except for their names.  The library `backlinks-missing-field-source.jar` contains
     * the class `BacklinksMissingFieldSource` and all of its annotation generated code.  The library
     * `backlinks-missing-field-target.jar` however, contains a version of `BacklinksMissingFieldTarget` that
     * was compiled with its backlink field referring to a field in `BacklinksMissingFieldSource`, called
     * `xxxchild`.  Clearly, in order to compile successfully, the definition of `BacklinksMissingFieldSource`
     * had to be changed accordingly.  The modified version, however, is *NOT* the version that is in
     * `backlinks-missing-field-source.jar`!
     * So, now, the proxy in `backlinks-missing-field-source.jar` will correctly validate the its table
     * (it generated it!). Similarly, the proxy in `backlinks-missing-field-target.jar` will successfully
     * validate its table.  If we have been living clean lives, though, the validator for
     * `BacklinksMissingFieldTarget` should notice that there is no field named `BacklinksMissingFieldSource.xxxchild`.
     */
    @Test
    public void migration_backlinkedSourceFieldDoesntExist() {
        final String realmName = "backlinks-missingSourceField.realm";

        RealmConfiguration realmConfig = configFactory.createConfigurationBuilder()
            .name(realmName)
            .modules(new BacklinksMissingFieldSourceModule(), new BacklinksMissingFieldTargetModule())
            .build();

        try {
            configFactory.copyRealmFromAssets(context, realmName, realmName);

            Realm localRealm = Realm.getInstance(realmConfig);
            localRealm.close();
            fail("A migration should have been required");
        } catch (IOException e) {
            fail("Failed copying realm");
        } catch (RealmMigrationNeededException expected) {
            assertTrue(expected.getMessage().contains("Cannot find source field"));
        } finally {
            Realm.deleteRealm(realmConfig);
        }
    }

    /**
     * Table validation should fail if the backinked column points to a field of the wrong type.
     * This test is built in almost exactly the way as was `migration_backlinkedSourceFieldDoesntExist`
     * The realm `backlinks-sourceFieldWrongType.realm` was constructed with classes `BacklinksWrongTypeTarget`
     * and `BacklinksWrongTypeSource`.  Again, these two classes are nearly identical in their counterparts
     * `BacklinkSource` and `BacklinkTarget` except for their names.  Unlike `BacklinkSource`,
     * `BacklinksWrongTypeSource` has two fields, `child` and `childId`. The first is exactly as it is in
     * `BacklinkSource`, the second is of type `Integer`.  To construct `backlinks-wrong-type-target.jar`
     * I reversed the names of the two fields in `BacklinkSource`, and made then adjusted `parents` in
     * `BacklinkTarget` to point to `childId`.
     * All of the proxies in in the two jars should correctly validate their tables.  The backlink validation
     * for `BacklinksWrongTypeTarget` should notice, though, that its `parents` field points to an object
     * of the wrong type, `Integer`, instead of `BacklinksWrongTypeSource`.
     */
    @Test
    public void migration_backlinkedSourceFieldWrongType() {
        final String realmName = "backlinks-sourceFieldWrongType.realm";

        RealmConfiguration realmConfig = configFactory.createConfigurationBuilder()
            .name(realmName)
            .modules(new BacklinksWrongTypeSourceModule(), new BacklinksWrongTypeTargetModule())
            .build();

        try {
            configFactory.copyRealmFromAssets(context, realmName, realmName);

            Realm localRealm = Realm.getInstance(realmConfig);
            localRealm.close();
            fail("A migration should have been required");
        } catch (IOException e) {
            fail("Failed copying realm");
        } catch (RealmMigrationNeededException expected) {
            assertTrue(expected.getMessage().contains("is not a RealmObject type"));
        } finally {
            Realm.deleteRealm(realmConfig);
        }
    }

    // Distinct works for backlinks
    @Test
    public void query_multipleReferencesWithDistinct() {
        realm.beginTransaction();
        AllJavaTypes child = realm.createObject(AllJavaTypes.class, 1);
        AllJavaTypes parent = realm.createObject(AllJavaTypes.class, 2);
        parent.getFieldList().add(child);
        parent.getFieldList().add(child);
        realm.commitTransaction();

        assertEquals(2, child.getListParents().size());

        RealmResults<AllJavaTypes> distinctParents = child.getListParents().where().distinct("fieldId");
        assertEquals(1, distinctParents.size());
        assertTrue(child.getListParents().contains(parent));
    }

    // Query on a field descriptor starting with a backlink
    // The test objects are:
    //             gen1
    //             / \
    //         gen2A gen2B
    //           \\   //
    //            gen3
    //  /  = object ref
    //  // = list ref
    @Test
    @Ignore
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
            .greaterThan("objectParents.fieldId", 1)
            .findAll();
        assertEquals(1, result.size());
        assertTrue(result.contains(gen2B));
    }

    // Query on a field descriptor that ends with a backlink
    // The test objects are:
    //             gen1
    //             / \
    //         gen2A gen2B
    //           \\   //
    //            gen3
    //  /  = object ref
    //  // = list ref
    @Test
    @Ignore
    public void query_endWithBacklink() {
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
            .isNotNull("objectParents.listParents")
            .findAll();
        assertEquals(2, result.size());
        assertTrue(result.contains(gen2A));
        assertTrue(result.contains(gen2B));
    }

    // Query on a field descriptor that has a backlink in the middle
    // The test objects are:
    //             gen1
    //             / \
    //         gen2A gen2B
    //           \\   //
    //            gen3
    //  /  = object ref
    //  // = list ref
    @Test
    @Ignore
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

        RealmResults<AllJavaTypes> result = realm.where(AllJavaTypes.class)
            .lessThan("objectParents.listParents.fieldId", 4)
            .findAll();
        assertEquals(2, result.size());
    }

    // Based on a quick conversation with Christian Melchior and Mark Rowe,
    // it appears that notifications are enqueued, briefly, on a non-Java
    // thread.  That makes their delivery onto the looper thread unpredictable.
    // Fortunately, it appears that beginning a transaction forces the delivery of
    // any outstanding notifications.
    // The closure passed to this method will be run *after* the body of the test
    // completes, and *after* the notifications have been delivered.  Because the
    // test method has been popped off the stack any objects referenced only from
    // the stack are subject to GC.  To hang on to them, until the test completes,
    // just pass them to the final vararg to this method.
    // @zaki50 has some evidence that notifications are delivered on a commit.
    // If that is the case, we may be able to eliminate the ugly begin-commit
    // that is the prologue to this method.
    private void verifyPostConditions(final Realm realm, final PostConditions test, final Object... refs) {
        realm.beginTransaction();
        realm.commitTransaction();

        // Runnable is guaranteed to be enqueued on the Looper queue, after the notifications
        looperThread.keepStrongReference.addAll(Arrays.asList(refs));
        looperThread.postRunnable(
            new Runnable() {
                @Override
                public void run() {
                    test.run(realm);
                    looperThread.testComplete();
                }
            });
    }
}

