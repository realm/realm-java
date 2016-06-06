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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import io.realm.entities.AllJavaTypes;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class LinkingObjectsTests {

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

    @Test
    public void backlinkFieldName_notFound() {
        // TODO How to test this?
    }

    @Test
    public void backlinkFieldName_typeNotBacklink() {
        // TODO How to test this?
    }

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

    public void queryStartingWithBacklink() {

    }

    public void queryEndingWithBacklink() {

    }

    public void queryBacklinkInMiddle() {

    }

    public void queryOnlyBacklinks() {

    }

    public void setUnmanagedLinkingObjects() {
        AllJavaTypes obj = new AllJavaTypes(1);
        obj.setObjectParents(realm.where(AllJavaTypes.class).findAll());
        assertNotNull(obj.getObjectParents());
        assertEquals(0, obj.getObjectParents().size());
    }

    public void setManagedLinkingObjectsThrows() {
        realm.beginTransaction();
        AllJavaTypes child = realm.createObject(AllJavaTypes.class, 1);
        AllJavaTypes parent = realm.createObject(AllJavaTypes.class, 2);
        parent.setFieldObject(child);

        try {
            // Trying to set @LinkingObjects in managed mode is illegal
            parent.setObjectParents(realm.where(AllJavaTypes.class).findAll());
            fail();
        } catch (UnsupportedOperationException ignored) {
        } finally {
            realm.cancelTransaction();
        }
    }

    public void copyToRealm_ignoreLinkingObjects() {
        AllJavaTypes child = new AllJavaTypes(1);
        AllJavaTypes parent = new AllJavaTypes(2);
        parent.setFieldObject(child);
        child.setObjectParents(realm.where(AllJavaTypes.class).findAll());

        AllJavaTypes managedParent = realm.copyToRealm(parent);

        // Backlinks are null when copied out of Realm.
        assertEquals(2, realm.where(AllJavaTypes.class).count());
        assertEquals(1, managedParent.getFieldObject().getObjectParents().size());
    }

    public void copyFromRealm_ignoreLinkingObjects() {
        realm.beginTransaction();
        AllJavaTypes child = realm.createObject(AllJavaTypes.class, 1);
        AllJavaTypes parent = realm.createObject(AllJavaTypes.class, 2);
        parent.setFieldObject(child);
        realm.commitTransaction();

        AllJavaTypes unmanagedParent = realm.copyFromRealm(parent);

        // Backlinks are null when copied out of Realm.
        assertNull(unmanagedParent.getFieldObject().getObjectParents());
    }

    public void dynamicQuery_fieldNotFound() {

    }

    public void dynamicQuery_typeNotBacklink() {

    }

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
