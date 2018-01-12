/*
 * Copyright 2018 Realm Inc.
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

import io.realm.entities.RelationshipChild;
import io.realm.entities.RelationshipParent;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertEquals;


/**
 * Testing Strong/Weak leaks. This should already be tested in ObjectStore/Core, so just doing
 * smoke test here for the most common scenarios.
 */
@RunWith(AndroidJUnit4.class)
public class RelationshipTests {

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    private RealmConfiguration realmConfig;
    private Realm realm;

    @Before
    public void setUp() {
        realmConfig = configFactory.createConfiguration();
        realm = Realm.getInstance(realmConfig);
        realm.beginTransaction();
    }

    @After
    public void tearDown() {
        if (realm != null && !realm.isClosed()) {
            realm.cancelTransaction();
            realm.close();
        }
    }

    @Test
    public void cascadeDeleteStrongObjectReference() {
        RealmResults<RelationshipChild> children = realm.where(RelationshipChild.class).findAll();

        // Create Strong relationship
        RelationshipChild child = realm.createObject(RelationshipChild.class);
        child.name = "Child";
        RelationshipParent parent = realm.createObject(RelationshipParent.class);
        parent.strongObjectRef = child;

        // Delete parent
        assertEquals(1, children.size());
        parent.deleteFromRealm();
        assertEquals(0, children.size());
    }

    @Test
    public void dontCascadeDeleteWeakObjectReference() {
        RealmResults<RelationshipChild> children = realm.where(RelationshipChild.class).findAll();

        // Create Strong relationship
        RelationshipChild child = realm.createObject(RelationshipChild.class);
        child.name = "Child";
        RelationshipParent parent = realm.createObject(RelationshipParent.class);
        parent.weakObjectRef = child;

        // Delete parent
        assertEquals(1, children.size());
        parent.deleteFromRealm();
        assertEquals(1, children.size());
    }

    @Test
    public void cascadeDeleteStrongListReference() {
        int TEST_SIZE = 10;
        RealmResults<RelationshipChild> children = realm.where(RelationshipChild.class).findAll();

        // Create Strong relationship
        RelationshipParent parent = realm.createObject(RelationshipParent.class);
        for (int i = 0; i < TEST_SIZE; i++) {
            parent.strongListRef.add(new RelationshipChild("child " + i));
        }

        // Delete parent
        assertEquals(TEST_SIZE, children.size());
        parent.deleteFromRealm();
        assertEquals(0, children.size());
    }

    @Test
    public void dontCascadeDeleteWeakListReference() {
        int TEST_SIZE = 10;
        RealmResults<RelationshipChild> children = realm.where(RelationshipChild.class).findAll();

        // Create Strong relationship
        RelationshipParent parent = realm.createObject(RelationshipParent.class);
        for (int i = 0; i < TEST_SIZE; i++) {
            parent.weakListRef.add(new RelationshipChild("child " + i));
        }

        // Delete parent
        assertEquals(TEST_SIZE, children.size());
        parent.deleteFromRealm();
        assertEquals(TEST_SIZE, children.size());
    }

    @Test
    public void cascadeWhenFinalStrongReferenceIsGone() {
        RealmResults<RelationshipChild> children = realm.where(RelationshipChild.class).findAll();

        // Create Strong relationship
        RelationshipChild child = realm.createObject(RelationshipChild.class);
        child.name = "Child";
        RelationshipParent parent1 = realm.createObject(RelationshipParent.class);
        parent1.strongObjectRef = child;
        RelationshipParent parent2 = realm.createObject(RelationshipParent.class);
        parent2.strongObjectRef = child;

        assertEquals(1, children.size());
        parent1.deleteFromRealm();
        assertEquals(1, children.size());
        parent2.deleteFromRealm();
        assertEquals(0, children.size());
    }

}