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

import io.realm.entities.Cat;
import io.realm.entities.Owner;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


@RunWith(AndroidJUnit4.class)
public class LinkingObjectsDynamicTests {

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    private Realm realm;
    DynamicRealm dynamicRealm;

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
            assertTrue(cat1Owner.getString(Owner.FIELD_NAME).endsWith("_cat1"));
            assertEquals(cat1, cat1Owner.getObject(Owner.FIELD_CAT));
        }

        final DynamicRealmObject cat2 = dynamicRealm.where(Cat.CLASS_NAME).equalTo(Cat.FIELD_NAME, "cat2").findFirst();
        final RealmResults<DynamicRealmObject> cat2Owners = cat2.linkingObjects(Owner.CLASS_NAME, Owner.FIELD_CAT);
        assertNotNull(cat2Owners);
        assertEquals(numOwnersOfCat2, cat2Owners.size());
        for (DynamicRealmObject cat2Owner : cat2Owners) {
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
