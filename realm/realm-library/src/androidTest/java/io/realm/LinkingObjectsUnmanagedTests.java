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
import org.junit.runner.RunWith;

import io.realm.entities.AllJavaTypes;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class LinkingObjectsUnmanagedTests {

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

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

    // When unmanaged, an object's backlinks fields are nulled
    @Test
    public void copyFromRealm_ignoreLinkingObjects() {
        realm.beginTransaction();
        AllJavaTypes child = realm.createObject(AllJavaTypes.class, 1);
        AllJavaTypes parent = realm.createObject(AllJavaTypes.class, 2);
        parent.setFieldObject(child);
        realm.commitTransaction();
        assertEquals(1, child.getObjectParents().size());
        assertEquals(parent, child.getObjectParents().first());

        AllJavaTypes unmanagedChild = realm.copyFromRealm(child);
        assertNull(unmanagedChild.getObjectParents());
    }
}
