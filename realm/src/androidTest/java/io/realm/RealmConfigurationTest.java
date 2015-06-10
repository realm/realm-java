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
 */

package io.realm;

import android.test.AndroidTestCase;

import java.io.File;
import java.util.Random;

import io.realm.entities.AllTypes;
import io.realm.entities.AllTypesPrimaryKey;
import io.realm.entities.CyclicType;
import io.realm.entities.Dog;
import io.realm.entities.Owner;
import io.realm.exceptions.RealmMigrationNeededException;

public class RealmConfigurationTest extends AndroidTestCase {

    RealmConfiguration defaultConfig;
    Realm realm;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        defaultConfig = new RealmConfiguration.Builder(getContext()).build();
        Realm.deleteRealm(defaultConfig);
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
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testGetNullDefaultInstanceThrows() {
        try {
            Realm.getDefaultInstance();
            fail();
        } catch (NullPointerException expected) {
        }
    }

    public void testGetNullInstance() {
        try {
            Realm.getInstance((RealmConfiguration) null);
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testNullDirThrows() {
        try {
            new RealmConfiguration.Builder((File) null).build();
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testNullNameThrows() {
        try {
            new RealmConfiguration.Builder(getContext()).name(null).build();
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testEmptyNameThrows() {
        try {
            new RealmConfiguration.Builder(getContext()).name("").build();
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testNullKeyThrows() {
        try {
            new RealmConfiguration.Builder(getContext()).encryptionKey(null).build();
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testWrongKeyLengthThrows() {
        byte[][] wrongKeys = new byte[][] {
                new byte[0],
                new byte[RealmConfiguration.KEY_LENGTH - 1],
                new byte[RealmConfiguration.KEY_LENGTH + 1]
        };
        for (byte[] key : wrongKeys) {
            try {
                new RealmConfiguration.Builder(getContext()).encryptionKey(key).build();
                fail("Key with length " + key.length + " should throw an exception");
            } catch (IllegalArgumentException expected) {
            }
        }
    }

    public void testNegativeVersionThrows() {
        try {
            new RealmConfiguration.Builder(getContext()).schemaVersion(-1).build();
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testVersionLessThanDiscVersionThrows() {
        realm = Realm.getInstance(new RealmConfiguration.Builder(getContext()).schemaVersion(42).build());
        realm.close();

        int[] wrongVersions = new int[] { 0, 1, 41 };
        for (int version : wrongVersions) {
            try {
                Realm.getInstance(new RealmConfiguration.Builder(getContext()).schemaVersion(version).build());
                fail("Version " + version + " should throw an exception");
            } catch (IllegalArgumentException expected) {
            }
        }
    }

    public void testVersionEqualWhenSchemaChangesThrows() {
        // Create initial Realm
        RealmConfiguration config = new RealmConfiguration.Builder(getContext()).schemaVersion(42).schema(Dog.class).build();
        Realm.getInstance(config).close();

        // Create new instance with a configuration containing another schema
        try {
            config = new RealmConfiguration.Builder(getContext()).schemaVersion(42).schema(AllTypesPrimaryKey.class).build();
            Realm.getInstance(config);
            fail("A migration should be required");
        } catch (RealmMigrationNeededException expected) {
        }
    }

    public void testCustomSchemaDontIncludeLinkedClasses() {
        RealmConfiguration config = new RealmConfiguration.Builder(getContext()).schema(Dog.class).build();
        realm = Realm.getInstance(config);
        try {
            assertEquals(3, realm.getTable(Owner.class).getColumnCount());
            fail("Owner should to be part of the schema");
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testNullMigrationThrows() {
        try {
            new RealmConfiguration.Builder(getContext()).migration(null).build();
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testSetModulesNullThrows() {
        // Test first argument
        try {
            new RealmConfiguration.Builder(getContext()).setModules(null);
            fail();
        } catch (IllegalArgumentException expected) {
        }

        // Test second argument
        try {
            new RealmConfiguration.Builder(getContext()).setModules(Realm.getDefaultModule(), null, null);
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testSetModulesNonRealmModulesThrows() {
        // Test first argument
        try {
            new RealmConfiguration.Builder(getContext()).setModules(new Object());
            fail();
        } catch (IllegalArgumentException expected) {
        }

        // Test second argument
        try {
            new RealmConfiguration.Builder(getContext()).setModules(Realm.getDefaultModule(), new Object());
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testSetModules() {
        RealmConfiguration realmConfig = new RealmConfiguration.Builder(getContext()).setModules(Realm.getDefaultModule()).build();
        realm = Realm.getInstance(realmConfig);
        assertNotNull(realm.getTable(AllTypes.class));
    }

    public void testSetDefaultConfiguration() {
        Realm.setDefaultConfiguration(defaultConfig);
        realm = Realm.getDefaultInstance();
        assertEquals(realm.getPath(), defaultConfig.getPath());
    }

    public void testGetInstance() {
        realm = Realm.getInstance(defaultConfig);
        assertEquals(realm.getPath(), defaultConfig.getPath());
    }

    public void testStandardSetup() {
        RealmConfiguration config = new RealmConfiguration.Builder(getContext())
                .name("foo.realm")
                .encryptionKey(TestHelper.getRandomKey())
                .schemaVersion(42)
                .migration(new RealmMigration() {
                    @Override
                    public long execute(Realm realm, long version) {
                        return 0; // no-op
                    }
                })
                .deleteRealmIfMigrationNeeded()
                .build();

        Realm.deleteRealm(config);
        realm = Realm.getInstance(config);
        assertTrue(realm.getPath().endsWith("foo.realm"));
        assertEquals(42, realm.getVersion());
    }

    public void testDeleteRealmIfMigration() {
        // Populate v0 of a Realm with an object
        RealmConfiguration config = new RealmConfiguration.Builder(getContext())
                .schema(Dog.class)
                .schemaVersion(0)
                .build();
        Realm.deleteRealm(config);
        realm = Realm.getInstance(config);
        realm.beginTransaction();
        realm.copyToRealm(new Dog("Foo"));
        realm.commitTransaction();
        assertEquals(1, realm.where(Dog.class).count());
        realm.close();

        // Change schema and verify that Realm has been cleared
        config = new RealmConfiguration.Builder(getContext())
                .schema(Owner.class, Dog.class)
                .schemaVersion(1)
                .deleteRealmIfMigrationNeeded()
                .build();
        realm = Realm.getInstance(config);
        assertEquals(0, realm.where(Dog.class).count());
    }

    public void testUpgradeVersionWithNoMigration() {
        realm = Realm.getInstance(defaultConfig);
        assertEquals(0, realm.getVersion());
        realm.close();

        // Version upgrades should always require a migration.
        try {
            realm = Realm.getInstance(new RealmConfiguration.Builder(getContext()).schemaVersion(42).build());
            fail();
        } catch (RealmMigrationNeededException expected) {
        }
    }

    public void testEquals() {
        RealmConfiguration config1 = new RealmConfiguration.Builder(getContext()).build();
        RealmConfiguration config2 = new RealmConfiguration.Builder(getContext()).build();
        assertTrue(config1.equals(config2));
    }

    public void testHashCode() {
        RealmConfiguration config1 = new RealmConfiguration.Builder(getContext()).build();
        RealmConfiguration config2 = new RealmConfiguration.Builder(getContext()).build();
        assertEquals(config1.hashCode(), config2.hashCode());
    }

    public void testEqualConfigurationsReturnCachedRealm() {
        Realm realm1 = Realm.getInstance(getContext());
        Realm realm2 = Realm.getInstance(getContext());
        try {
            assertEquals(realm1, realm2);
        } finally {
            realm1.close();
            realm2.close();
        }
    }

    public void testDifferentVersionsThrows() {
        RealmConfiguration config1 = new RealmConfiguration.Builder(getContext()).schemaVersion(1).build();
        RealmConfiguration config2 = new RealmConfiguration.Builder(getContext()).schemaVersion(2).build();

        Realm realm1 = Realm.getInstance(config1);
        try {
            Realm.getInstance(config2);
            fail();
        } catch (IllegalArgumentException expected) {
        } finally {
            realm1.close();
        }
    }

    public void testDifferentEncryptionKeysThrows() {
        RealmConfiguration config1 = new RealmConfiguration.Builder(getContext()).encryptionKey(TestHelper.getRandomKey()).build();
        RealmConfiguration config2 = new RealmConfiguration.Builder(getContext()).encryptionKey(TestHelper.getRandomKey()).build();

        Realm realm1 = Realm.getInstance(config1);
        try {
            Realm.getInstance(config2);
            fail();
        } catch (IllegalArgumentException expected) {
        } finally {
            realm1.close();
        }
    }

    public void testDifferentSchemasThrows() {
        RealmConfiguration config1 = new RealmConfiguration.Builder(getContext()).schema(AllTypes.class).build();
        RealmConfiguration config2 = new RealmConfiguration.Builder(getContext()).schema(CyclicType.class).build();

        Realm realm1 = Realm.getInstance(config1);
        try {
            Realm.getInstance(config2);
            fail();
        } catch (IllegalArgumentException expected) {
        } finally {
            realm1.close();
        }
    }
}
