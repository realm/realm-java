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

import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicInteger;

import io.realm.entities.AllJavaTypes;
import io.realm.exceptions.RealmException;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.RunTestInLooperThread;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class LinkingObjectsManagedTests {

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();
    @Rule
    public final ExpectedException thrown = ExpectedException.none();
    @Rule
    public final RunInLooperThread looperThread = new RunInLooperThread();

    private Realm realm;

    @Before
    public void setUp() {
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
    public void singleBacklink_link() {
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
    public void singleBacklink_linkList() {
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
    public void multipleBacklinks_link() {
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
    public void multipleBacklinks_linkList() {
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
    // !!!FIXME: DOCUMENT THIS
    @Test
    public void multipleReferencesFromParentList() {
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

    // Fields annotated with @LinkingObjects should not be affected by JSON updates
    @Test
    public void jsonUpdate_object() {
        realm.beginTransaction();
        AllJavaTypes child = realm.createObject(AllJavaTypes.class, 1);
        AllJavaTypes parent = realm.createObject(AllJavaTypes.class, 2);
        parent.setFieldObject(child);
        realm.commitTransaction();

        realm.beginTransaction();
        try {
            realm.createOrUpdateAllFromJson(AllJavaTypes.class, "[{ \"fieldId\" : 1, \"objectParents\" : null }]");
            fail("Expected attempt to load the @LinkingObjects 'objectParents' field to fail");
        } catch (RealmException ignore) {
        }
        realm.commitTransaction();

        RealmResults<AllJavaTypes> parents = child.getObjectParents();
        assertNotNull(parents);
        assertEquals(1, child.getObjectParents().size());
        assertTrue(child.getObjectParents().contains(parent));
    }

    // Fields annotated with @LinkingObjects should not be affected by JSON updates
    @Test
    public void jsonUpdate_list() {
        realm.beginTransaction();
        AllJavaTypes child = realm.createObject(AllJavaTypes.class, 1);
        AllJavaTypes parent = realm.createObject(AllJavaTypes.class, 2);
        parent.getFieldList().add(child);
        realm.commitTransaction();

        realm.beginTransaction();
        try {
            realm.createOrUpdateAllFromJson(AllJavaTypes.class, "[{ \"fieldId\" : 1, \"listParents\" : null }]");
            fail("Expected attempt to load the @LinkingObjects 'listParents' field to fail");
        } catch (RealmException ignore) {
        }
        realm.commitTransaction();

        RealmResults<AllJavaTypes> parents = child.getListParents();
        assertNotNull(parents);
        assertEquals(1, child.getListParents().size());
        assertTrue(child.getListParents().contains(parent));
    }

    // Distinct works for backlinks
    @Test
    public void multipleReferencesWithDistinct() {
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
    public void queryStartingWithBacklink() {
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
    public void queryEndingWithBacklink() {
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
    public void queryBacklinkInMiddle() {
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

    // A notification callback should be called on a commit that adds a backlink
    @Test
    @RunTestInLooperThread
    public void notificationOnCommit() {
        Realm ltRealm = looperThread.realm;

        ltRealm.beginTransaction();
        AllJavaTypes gen1 = ltRealm.createObject(AllJavaTypes.class, 10);
        ltRealm.commitTransaction();

        final AtomicInteger counter = new AtomicInteger(0);
        RealmChangeListener<AllJavaTypes> listener = new RealmChangeListener<AllJavaTypes>() {
            @Override
            public void onChange(AllJavaTypes object) {
                counter.incrementAndGet();
                looperThread.testComplete();
            }
        };
        gen1.addChangeListener(listener);

        ltRealm.beginTransaction();

        AllJavaTypes gen2A = ltRealm.createObject(AllJavaTypes.class, 1);
        gen2A.setFieldObject(gen1);
        ltRealm.commitTransaction();

        assertEquals(1, counter.get());
        assertEquals(2, ltRealm.where(AllJavaTypes.class).findAll().size());
    }

    // A notification callback should be called when a backlinked object is deleted
    @Test
    @RunTestInLooperThread
    public void notificationOnDelete() {
        Realm ltRealm = looperThread.realm;

        ltRealm.beginTransaction();
        AllJavaTypes gen1 = ltRealm.createObject(AllJavaTypes.class, 10);
        AllJavaTypes gen2A = ltRealm.createObject(AllJavaTypes.class, 1);
        gen2A.setFieldObject(gen1);
        ltRealm.commitTransaction();

        final AtomicInteger counter = new AtomicInteger(0);
        RealmChangeListener<AllJavaTypes> listener = new RealmChangeListener<AllJavaTypes>() {
            @Override
            public void onChange(AllJavaTypes object) {
                counter.incrementAndGet();
                looperThread.testComplete();
            }
        };
        gen1.addChangeListener(listener);

        ltRealm.beginTransaction();
        ltRealm.where(AllJavaTypes.class).equalTo("fieldId", 1).findAll().deleteAllFromRealm();
        ltRealm.commitTransaction();

        assertEquals(1, ltRealm.where(AllJavaTypes.class).findAll().size());
        assertEquals(1, counter.get());
    }

    // A notification callback should not be called for unrelated changes on the same object
    @Test
    @RunTestInLooperThread
    public void notificationNotSentOnUnrelatedChange() {
        Realm ltRealm = looperThread.realm;

        ltRealm.beginTransaction();
        AllJavaTypes gen1 = ltRealm.createObject(AllJavaTypes.class, 10);
        AllJavaTypes gen2A = ltRealm.createObject(AllJavaTypes.class, 1);
        ltRealm.commitTransaction();

        final AtomicInteger counter = new AtomicInteger(0);
        RealmChangeListener<AllJavaTypes> listener = new RealmChangeListener<AllJavaTypes>() {
            @Override
            public void onChange(AllJavaTypes object) {
                counter.incrementAndGet();
                looperThread.testComplete();
            }
        };
        gen1.addChangeListener(listener);

        ltRealm.beginTransaction();
        ltRealm.where(AllJavaTypes.class).equalTo("fieldId", 1).findAll().deleteAllFromRealm();
        ltRealm.commitTransaction();

        assertEquals(1, ltRealm.where(AllJavaTypes.class).findAll().size());
        assertEquals(1, counter.get());
    }

    // !!!FIXME: what is this test supposed to do?
    @Test
    public void notificationSentOnlyForRefresh() {
    }

    // Table validation should fail if the backinked column exists in the target table
    @Test
    public void backlinkedFieldInUse() {
    }

    // Table validation should fail if the backinked column points to a non-existent class
    @Test
    public void backlinkedSourceClassDoesntExist() {
    }

    // Table validation should fail if the backinked column points to a non-existent field
    @Test
    public void backlinkedSourceFieldDoesntExist() {
    }

    // Table validation should fail if the backinked column points to a field of the wrong type
    @Test
    public void backlinkedSourceFieldWrongType() {
    }
}

