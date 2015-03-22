/*
 * Copyright 2014 Realm Inc.
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

import android.test.AndroidTestCase;

import java.io.File;
import java.util.Random;

import io.realm.entities.AllTypesPrimaryKey;
import io.realm.entities.Dog;
import io.realm.entities.Owner;
import io.realm.exceptions.RealmMigrationNeededException;

public class RealmConfigurationTest extends AndroidTestCase {

    RealmConfiguration defaultConfig;
    Realm realm;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        defaultConfig = new RealmConfiguration.Builder(getContext()).create();
        Realm.deleteRealmFile(defaultConfig);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (realm != null) {
            realm.close();
        }
    }

    public void testSetNullDefaultConfigurationThrows() {
        try {
            Realm.setDefaultConfiguration(null);
        } catch (NullPointerException expected) {
            return;
        }
        fail();
    }

    public void testGetNullDefaultInstanceThrows() {
        try {
            Realm.getDefaultInstance();
        } catch (NullPointerException expected) {
            return;
        }
        fail();
    }

    public void testGetNullInstance() {
        try {
            Realm.getInstance((RealmConfiguration) null);
        } catch (NullPointerException expected) {
            return;
        }
        fail();
    }

    public void testNullDirThrows() {
        try {
            new RealmConfiguration.Builder((File) null).create();
        } catch (IllegalArgumentException expected) {
            return;
        }
        fail();
    }

    public void testNullNameThrows() {
        try {
            new RealmConfiguration.Builder(getContext()).name(null).create();
        } catch (IllegalArgumentException expected) {
            return;
        }
        fail();
    }

    public void testEmptyNameThrows() {
        try {
            new RealmConfiguration.Builder(getContext()).name("").create();
        } catch (IllegalArgumentException expected) {
            return;
        }
        fail();
    }

    public void testNullKeyThrows() {
        try {
            new RealmConfiguration.Builder(getContext()).encryptionKey(null).create();
        } catch (IllegalArgumentException expected) {
            return;
        }
        fail();
    }

    public void testWrongKeyLengthThrows() {
        try {
            new RealmConfiguration.Builder(getContext()).encryptionKey(new byte[63]).create();
        } catch (IllegalArgumentException expected) {
            return;
        }
        fail();
    }

    public void testNegativeVersionThrows() {
        try {
            new RealmConfiguration.Builder(getContext()).schemaVersion(-1).create();
        } catch (IllegalArgumentException expected) {
            return;
        }
        fail();
    }

    public void testVersionLessThanDiscVersionThrows() {
        realm = Realm.getInstance(new RealmConfiguration.Builder(getContext()).schemaVersion(42).create());
        realm.close();

        try {
            Realm.getInstance(new RealmConfiguration.Builder(getContext()).schemaVersion(1).create());
        } catch (IllegalArgumentException expected) {
            return;
        }
        fail();
    }

    // TODO Should throw IllegalState instead
    public void testVersionEqualWhenSchemaChangesThrows() {
        realm = Realm.getInstance(new RealmConfiguration.Builder(getContext())
                .deleteRealmBeforeOpening()
                .schemaVersion(42)
                .schema(Dog.class)
                .create());
        realm.close();

        try {
            Realm.getInstance(new RealmConfiguration.Builder(getContext())
                    .schemaVersion(42)
                    .schema(AllTypesPrimaryKey.class)
                    .create());
            fail("A migration should be required");
        } catch (RealmMigrationNeededException expected) {
        }
    }

    public void testCustomSchemaAlsoIncludeLinkedClasses() {
        realm = Realm.getInstance(new RealmConfiguration.Builder(getContext())
                .deleteRealmBeforeOpening()
                .schema(Dog.class)
                .create());
        assertEquals(3, realm.getTable(Owner.class).getColumnCount());
        assertEquals(7, realm.getTable(Dog.class).getColumnCount());
    }

    public void testNullMigrationThrows() {
        try {
            new RealmConfiguration.Builder(getContext()).migration(null).create();
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testSetDefaultConfiguration() {
        Realm.setDefaultConfiguration(defaultConfig);
        realm = Realm.getDefaultInstance();
        assertEquals(realm.getPath(), defaultConfig.getAbsolutePathToRealm());
    }

    public void testGetInstance() {
        realm = Realm.getInstance(defaultConfig);
        assertEquals(realm.getPath(), defaultConfig.getAbsolutePathToRealm());
    }

    public void testStandardSetup() {
        byte[] key = new byte[64];
        new Random().nextBytes(key);
        realm = Realm.getInstance(new RealmConfiguration.Builder(getContext())
                .name("foo.realm")
                .encryptionKey(key)
                .schemaVersion(42)
                .migration(new RealmMigration() {
                    @Override
                    public long execute(Realm realm, long version) {
                        return 0; // no-op
                    }
                })
                .deleteRealmBeforeOpening()
                .deleteRealmIfMigrationNeeded()
                .create());
        assertTrue(realm.getPath().endsWith("foo.realm"));
        assertEquals(42, realm.getVersion());
    }

    public void testDeleteRealmIfMigration() {
        // Populate v0 of a Realm with an object
        RealmConfiguration config = new RealmConfiguration.Builder(getContext())
                .deleteRealmBeforeOpening()
                .schema(Dog.class)
                .schemaVersion(0)
                .create();
        realm = Realm.getInstance(config);
        realm.beginTransaction();
        realm.copyToRealm(new Dog("Foo"));
        realm.commitTransaction();
        assertEquals(1, realm.where(Dog.class).count());
        realm.close();

        // Change schema and verify that Realm has been cleared
        realm = Realm.getInstance(new RealmConfiguration.Builder(getContext())
                .schema(Owner.class, Dog.class)
                .schemaVersion(1)
                .deleteRealmIfMigrationNeeded()
                .create());
        assertEquals(0, realm.where(Dog.class).count());
    }

    public void testDeleteRealmBeforeOpening() {
        RealmConfiguration config = new RealmConfiguration.Builder(getContext()).deleteRealmBeforeOpening().create();
        realm = Realm.getInstance(config);
        realm.beginTransaction();
        realm.copyToRealm(new Dog("Foo"));
        realm.commitTransaction();
        assertEquals(1, realm.where(Dog.class).count());
        realm.close();

        realm = Realm.getInstance(config);
        assertEquals(0, realm.where(Dog.class).count());
    }

    public void testUpgradeVersionWithNoMigrationThrows() {
        realm = Realm.getInstance(defaultConfig);
        assertEquals(0, realm.getVersion());
        realm.close();
        try {
            Realm.getInstance(new RealmConfiguration.Builder(getContext()).schemaVersion(42).create());
            fail("Upgrading to new version without a migration block should fail");
        } catch (RealmMigrationNeededException expected) {
        }
    }
}
