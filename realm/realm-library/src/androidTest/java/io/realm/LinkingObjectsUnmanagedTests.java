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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class LinkingObjectsUnmanagedTests {

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

    // In an unmanaged object, the backlinks field can be set
    public void setUnmanagedLinkingObjects() {
        AllJavaTypes obj = new AllJavaTypes(1);
        obj.setObjectParents(realm.where(AllJavaTypes.class).findAll());
        assertNotNull(obj.getObjectParents());
        assertEquals(0, obj.getObjectParents().size());
    }

    // When managed, an object has the existing content of a backlinked field
    // replaced with actual backlinks
    // !!! Should this generate a warning?
    public void copyToRealm_ignoreLinkingObjects() {
        AllJavaTypes child = new AllJavaTypes(1);
        AllJavaTypes parent = new AllJavaTypes(2);
        parent.setFieldObject(child);
        child.setObjectParents(realm.where(AllJavaTypes.class).findAll());
        assertEquals(0, child.getObjectParents().size());

        AllJavaTypes managedParent = realm.copyToRealm(parent);

        assertEquals(2, realm.where(AllJavaTypes.class).count());
        assertEquals(1, managedParent.getFieldObject().getObjectParents().size());
    }

    // When unmanaged, an object's backlinks fields a nulled
    public void copyFromRealm_ignoreLinkingObjects() {
        realm.beginTransaction();
        AllJavaTypes child = realm.createObject(AllJavaTypes.class, 1);
        AllJavaTypes parent = realm.createObject(AllJavaTypes.class, 2);
        parent.setFieldObject(child);
        realm.commitTransaction();
        assertEquals(1, parent.getFieldObject().getObjectParents().size());
        assertEquals(child, parent.getFieldObject().getObjectParents().first());

        AllJavaTypes unmanagedParent = realm.copyFromRealm(parent);

        assertNull(unmanagedParent.getFieldObject().getObjectParents());
    }
}
