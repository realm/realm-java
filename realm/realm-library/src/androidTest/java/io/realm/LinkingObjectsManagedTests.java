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

import io.realm.entities.AllJavaTypes;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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

    // In a managed object, the backlinks field cannot be set
    public void setManagedLinkingObjectsThrows() {
        realm.beginTransaction();
        AllJavaTypes child = realm.createObject(AllJavaTypes.class, 1);
        AllJavaTypes parent = realm.createObject(AllJavaTypes.class, 2);
        parent.setFieldObject(child);

        try {
            // Trying to set @LinkingObjects in managed mode is illegal
            parent.setObjectParents(realm.where(AllJavaTypes.class).findAll());
            fail();
        }
        catch (UnsupportedOperationException ignored) {
        }
        finally {
            realm.cancelTransaction();
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
        assertEquals(parent, child.getObjectParents().first());
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
        assertEquals(parent, child.getListParents().first());
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

    // Query on a field descriptor starting with a backlink
    @Test
    @Ignore
    public void queryStartingWithBacklink() {
        realm.beginTransaction();
        AllJavaTypes child = realm.createObject(AllJavaTypes.class, 10);

        AllJavaTypes parent1 = realm.createObject(AllJavaTypes.class, 1);
        parent1.setFieldObject(child);

        AllJavaTypes parent2 = realm.createObject(AllJavaTypes.class, 2);
        parent2.setFieldObject(child);

        AllJavaTypes parent3 = realm.createObject(AllJavaTypes.class, 3);
        parent3.setFieldObject(child);
        realm.commitTransaction();

        RealmResults<AllJavaTypes> result = realm.where(AllJavaTypes.class)
            .greaterThan("objectParents.fieldId", 1)
            .findAll();
        assertEquals(2, result.size());
        assertFalse(result.contains(parent1));
        assertTrue(result.contains(parent2));
        assertTrue(result.contains(parent3));
    }

    // Query on a field descriptor that has a backlink in the middle
    @Test
    @Ignore
    public void queryBacklinkInMiddle() {
        realm.beginTransaction();
        AllJavaTypes child = realm.createObject(AllJavaTypes.class, 1);

        AllJavaTypes parent1 = realm.createObject(AllJavaTypes.class, 2);
        parent1.setFieldObject(child);

        AllJavaTypes parent2 = realm.createObject(AllJavaTypes.class, 3);
        parent2.setFieldObject(child);

        AllJavaTypes parent3 = realm.createObject(AllJavaTypes.class, 4);
        parent3.setFieldObject(child);
        realm.commitTransaction();

        RealmResults<AllJavaTypes> result = realm.where(AllJavaTypes.class)
            .greaterThan("fieldObject.objectParents.fieldId", 1)
            .findAll();
        assertEquals(2, result.size());
        assertFalse(result.contains(parent1));
        assertTrue(result.contains(parent2));
        assertTrue(result.contains(parent3));
    }

    // Query on a field descriptor that ends with a backlink
    @Test
    public void queryEndingWithBacklink() {
        //equalTo("selectedFieldParents.selectedFieldParents")
    }

    // Query on a field descriptor containing mulitple backlinks
    @Test
    @Ignore
    public void queryMultipleBacklinks() {
        realm.beginTransaction();
        AllJavaTypes child = realm.createObject(AllJavaTypes.class, 1);

        AllJavaTypes parent1 = realm.createObject(AllJavaTypes.class, 2);
        parent1.setFieldObject(child);

        AllJavaTypes parent2 = realm.createObject(AllJavaTypes.class, 3);
        parent2.setFieldObject(child);

        AllJavaTypes parent3 = realm.createObject(AllJavaTypes.class, 4);
        parent3.setFieldObject(child);
        realm.commitTransaction();

        RealmResults<AllJavaTypes> result = realm.where(AllJavaTypes.class)
            .greaterThan("objectParents.objectParents.fieldId", 1)
            .findAll();
        assertEquals(2, result.size());
        assertFalse(result.contains(parent1));
        assertTrue(result.contains(parent2));
        assertTrue(result.contains(parent3));
    }

    // A newly added notification callback should be called immediately for an object
    // that has acquired a backlink
    @Test
    public void notifcationSentInitially() {

    }

    // A notification callback should be called on a commit that adds a backlink
    @Test
    public void notifcationOnCommit() {

    }

    // A notification callback should be called when a backlinked object is deleted
    @Test
    public void notifcationOnDelete() {

    }

    // A notification callback should not be called for unrelated changes on the same object
    @Test
    public void notifcationNotSentOnUnrelatedChange() {

    }

    // ???
    @Test
    public void notifcationSentOnlyForRefresh() {
    }
}

