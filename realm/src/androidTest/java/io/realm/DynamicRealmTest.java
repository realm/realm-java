/*
 * Copyright 2015 Realm Inc.
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
 *
 */

package io.realm;


import android.test.AndroidTestCase;

import io.realm.entities.AllTypes;
import io.realm.entities.Owner;
import io.realm.exceptions.RealmException;

public class DynamicRealmTest extends AndroidTestCase {

    public static final String CLASS_ALL_TYPES = "AllTypes";
    private static final String CLASS_OWNER = "Owner";

    private RealmConfiguration defaultConfig;
    private DynamicRealm realm;

    @Override
    public void setUp() {
        defaultConfig = TestHelper.createConfiguration(getContext());
        Realm.deleteRealm(defaultConfig);

        // Initialize schema. DynamicRealm will not do that, so let a normal Realm create the file first.
        Realm.getInstance(defaultConfig).close();
        realm = DynamicRealm.getInstance(defaultConfig);
    }

    @Override
    public void tearDown() {
        if (realm != null) {
            realm.close();
        }
    }

    public void testCreateObject() {
        realm.beginTransaction();
        DynamicRealmObject obj = realm.createObject(CLASS_ALL_TYPES);
        realm.commitTransaction();
        assertTrue(obj.isValid());
    }

    public void testWhere() {
        realm.beginTransaction();
        realm.createObject(CLASS_ALL_TYPES);
        realm.commitTransaction();

        RealmResults<DynamicRealmObject> results = realm.where(CLASS_ALL_TYPES).findAll();
        assertEquals(1, results.size());
    }

    // Test that the SharedGroupManager is not reused across Realm/DynamicRealm on the same thread.
    // This is done by starting a write transaction in one Realm and verifying that none of the data
    // written (but not committed) is available in the other Realm.
    public void testSeparateSharedGroups() {
        Realm typedRealm = Realm.getInstance(defaultConfig);
        DynamicRealm dynamicRealm = DynamicRealm.getInstance(defaultConfig);

        assertEquals(0, typedRealm.where(AllTypes.class).count());
        assertEquals(0, dynamicRealm.where(CLASS_ALL_TYPES).count());

        typedRealm.beginTransaction();
        try {
            typedRealm.createObject(AllTypes.class);
            assertEquals(1, typedRealm.where(AllTypes.class).count());
            assertEquals(0, dynamicRealm.where(CLASS_ALL_TYPES).count());
            typedRealm.cancelTransaction();
        } finally {
            typedRealm.close();
            dynamicRealm.close();
        }
    }

    // Test that Realms can only be deleted after all Typed and Dynamic instances are closed.
    public void testDeleteAfterAllIsClosed() {
        Realm typedRealm = Realm.getInstance(defaultConfig);
        DynamicRealm dynamicRealm = DynamicRealm.getInstance(defaultConfig);

        typedRealm.close();
        try {
            Realm.deleteRealm(defaultConfig);
            fail();
        } catch (IllegalStateException ignored) {
        }

        dynamicRealm.close();
        assertTrue(Realm.deleteRealm(defaultConfig));
    }

    // Test that the closed Realm isn't kept in the Realm instance cache
    public void testRealmCacheIsCleared() {
        Realm typedRealm = Realm.getInstance(defaultConfig);
        DynamicRealm dynamicRealm = DynamicRealm.getInstance(defaultConfig);

        typedRealm.close(); // Still a instance open, but typed Realm cache must still be cleared.
        dynamicRealm.close();

        try {
            // If cache isn't cleared this would crash because of a closed shared group.
            typedRealm = Realm.getInstance(defaultConfig);
            assertEquals(0, typedRealm.where(AllTypes.class).count());
        } finally {
            typedRealm.close();
        }
    }

    // Test that the closed DynamicRealms isn't kept in the DynamicRealm instance cache
    public void testDynamicRealmCacheIsCleared() {
        DynamicRealm dynamicRealm = DynamicRealm.getInstance(defaultConfig);
        Realm typedRealm = Realm.getInstance(defaultConfig);

        dynamicRealm.close(); // Still a instance open, but DynamicRealm cache must still be cleared.
        typedRealm.close();

        try {
            // If cache isn't cleared this would crash because of a closed shared group.
            dynamicRealm = DynamicRealm.getInstance(defaultConfig);
            assertEquals(0, dynamicRealm.getVersion());
        } finally {
            dynamicRealm.close();
        }
    }

    public void testClearInvalidName() {
        realm.beginTransaction();
        realm.clear("I don't exist");
        realm.commitTransaction();
    }

    public void testClearOutsideTransactionThrows() {
        try {
            realm.clear(CLASS_ALL_TYPES);
            fail();
        } catch(IllegalStateException ignored) {
        }
    }

    public void testClear() {
        realm.beginTransaction();
        realm.createObject(CLASS_ALL_TYPES);
        realm.commitTransaction();

        assertEquals(1, realm.where(CLASS_ALL_TYPES).count());
        realm.beginTransaction();
        realm.clear(CLASS_ALL_TYPES);
        realm.commitTransaction();
        assertEquals(0, realm.where(CLASS_ALL_TYPES).count());
    }

    public void testExecuteTransactionNull() {
        realm.executeTransaction(null); // Nothing happens
        assertFalse(realm.hasChanged());
    }

    public void testExecuteTransactionCommit() {
        assertEquals(0, realm.allObjects(CLASS_OWNER).size());
        realm.executeTransaction(new DynamicRealm.Transaction() {
            @Override
            public void execute(DynamicRealm realm) {
                DynamicRealmObject owner = realm.createObject(CLASS_OWNER);
                owner.setString("name", "Owner");
            }
        });
        assertEquals(1, realm.allObjects(CLASS_OWNER).size());
    }

    public void testExecuteTransactionCancel() {
        assertEquals(0, realm.allObjects(CLASS_OWNER).size());
        try {
            realm.executeTransaction(new DynamicRealm.Transaction() {
                @Override
                public void execute(DynamicRealm realm) {
                    DynamicRealmObject owner = realm.createObject(CLASS_OWNER);
                    owner.setString("name", "Owner");
                    throw new RuntimeException("Boom");
                }
            });
        } catch (RealmException ignore) {
        }
        assertEquals(0, realm.allObjects(CLASS_OWNER).size());
    }
}
