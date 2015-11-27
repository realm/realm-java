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
import android.test.MoreAsserts;

import java.io.File;
import java.util.Set;

import io.realm.entities.AllTypes;
import io.realm.entities.AllTypesPrimaryKey;
import io.realm.entities.AnimalModule;
import io.realm.entities.Cat;
import io.realm.entities.CatOwner;
import io.realm.entities.CyclicType;
import io.realm.entities.Dog;
import io.realm.entities.HumanModule;
import io.realm.entities.Owner;
import io.realm.exceptions.RealmMigrationNeededException;
import io.realm.internal.modules.CompositeMediator;
import io.realm.internal.modules.FilterableMediator;

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
        } catch (IllegalArgumentException ignored) {
        }
    }

    public void testGetNullDefaultInstanceThrows() {
        try {
            Realm.getDefaultInstance();
            fail();
        } catch (NullPointerException ignored) {
        }
    }

    public void testGetNullInstance() {
        try {
            Realm.getInstance((RealmConfiguration) null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    public void testNullDirThrows() {
        try {
            new RealmConfiguration.Builder((File) null).build();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    public void testGetInstanceCreateSubFoldersThrows() {
        File folder = new File(getContext().getFilesDir().getAbsolutePath() + "/subfolder1/subfolder2/");
        try {
            new RealmConfiguration.Builder(folder).build();
            fail("Assuming that subfolders are created automatically should fail");
        } catch (IllegalArgumentException ignored) {
        }
    }

    public void testNullNameThrows() {
        try {
            new RealmConfiguration.Builder(getContext()).name(null).build();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    public void testEmptyNameThrows() {
        try {
            new RealmConfiguration.Builder(getContext()).name("").build();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    public void testInstanceIdForHashCollision() {
        // Ea.hashCode() == FB.hashCode()
        RealmConfiguration configA = TestHelper.createConfiguration(getContext(), "Ea");
        RealmConfiguration configB = TestHelper.createConfiguration(getContext(), "FB");
        Realm.deleteRealm(configA);
        Realm.deleteRealm(configB);

        Realm r1 = Realm.getInstance(configA);
        Realm r2 = Realm.getInstance(configB);
        assertNotSame(r1, r2);
        r1.close();
        r2.close();
    }

    public void testNullKeyThrows() {
        try {
            new RealmConfiguration.Builder(getContext()).encryptionKey(null).build();
            fail();
        } catch (IllegalArgumentException ignored) {
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
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public void testNegativeVersionThrows() {
        try {
            new RealmConfiguration.Builder(getContext()).schemaVersion(-1).build();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    public void testVersionLessThanDiscVersionThrows() {
        realm = Realm.getInstance(new RealmConfiguration.Builder(getContext()).schemaVersion(42).build());
        realm.close();

        int[] wrongVersions = new int[] { 0, 1, 41 };
        for (int version : wrongVersions) {
            try {
                realm = Realm.getInstance(new RealmConfiguration.Builder(getContext()).schemaVersion(version).build());
                fail("Version " + version + " should throw an exception");
            } catch (IllegalArgumentException ignored) {
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
            realm = Realm.getInstance(config);
            fail("A migration should be required");
        } catch (RealmMigrationNeededException ignored) {
        }
    }

    public void testCustomSchemaDontIncludeLinkedClasses() {
        RealmConfiguration config = new RealmConfiguration.Builder(getContext()).schema(Dog.class).build();
        realm = Realm.getInstance(config);
        try {
            assertEquals(3, realm.getTable(Owner.class).getColumnCount());
            fail("Owner should to be part of the schema");
        } catch (IllegalArgumentException ignored) {
        }
    }

    public void testNullMigrationThrows() {
        try {
            new RealmConfiguration.Builder(getContext()).migration(null).build();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    public void testSetModulesNonRealmModulesThrows() {
        // Test first argument
        try {
            new RealmConfiguration.Builder(getContext()).setModules(new Object());
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        // Test second argument
        try {
            new RealmConfiguration.Builder(getContext()).setModules(Realm.getDefaultModule(), new Object());
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    public void testSetModules() {
        RealmConfiguration realmConfig = new RealmConfiguration.Builder(getContext()).setModules(Realm.getDefaultModule(), (Object) null).build();
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
                    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                        // no-op
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
        } catch (RealmMigrationNeededException ignored) {
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

    public void testEqualsWithCustomModules() {
        RealmConfiguration config1 = new RealmConfiguration.Builder(getContext())
                .setModules(new HumanModule(), new AnimalModule())
                .build();

        RealmConfiguration config2 = new RealmConfiguration.Builder(getContext())
                .setModules(new AnimalModule(), new HumanModule())
                .build();

        assertTrue(config1.equals(config2));
    }

    public void testHashCodeWithCustomModules() {
        RealmConfiguration config1 = new RealmConfiguration.Builder(getContext())
                .setModules(new HumanModule(), new AnimalModule())
                .build();
        RealmConfiguration config2 = new RealmConfiguration.Builder(getContext())
                .setModules(new AnimalModule(), new HumanModule())
                .build();

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
        } catch (IllegalArgumentException ignored) {
        } finally {
            realm1.close();
        }
    }

    public void testDifferentEncryptionKeysThrows() {
        RealmConfiguration config1 = new RealmConfiguration.Builder(getContext()).encryptionKey(TestHelper.getRandomKey()).build();
        RealmConfiguration config2 = new RealmConfiguration.Builder(getContext()).encryptionKey(TestHelper.getRandomKey()).build();

        Realm realm1 = Realm.getInstance(config1);
        try {
            realm = Realm.getInstance(config2);
            fail();
        } catch (IllegalArgumentException ignored) {
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
        } catch (IllegalArgumentException ignored) {
        } finally {
            realm1.close();
        }
    }

    // Creating Realm instances with same name but different durabilities is not allowed.
    public void testDifferentDurabilityThrows() {
        RealmConfiguration config1 = new RealmConfiguration.Builder(getContext()).inMemory().build();
        RealmConfiguration config2 = new RealmConfiguration.Builder(getContext()).build();

        // Create In-memory Realm first.
        Realm realm1 = Realm.getInstance(config1);
        try {
            // On-disk Realm then. Not allowed!
            Realm.getInstance(config2);
            fail();
        } catch (IllegalArgumentException ignored) {
        } finally {
            realm1.close();
        }

        // Create on-disk Realm first.
        realm1 = Realm.getInstance(config2);
        try {
            // In-memory Realm then. Not allowed!
            Realm.getInstance(config1);
            fail();
        } catch (IllegalArgumentException ignored) {
        } finally {
            realm1.close();
        }
    }

    // It is allowed to create multiple Realm with same name but in different directory
    public void testDifferentDirSameName() {
        RealmConfiguration config1 = new RealmConfiguration.Builder(getContext()).build();
        RealmConfiguration config2 = new RealmConfiguration.Builder(getContext().getCacheDir()).build();

        Realm.deleteRealm(config1);
        Realm.deleteRealm(config2);

        Realm realm1 = Realm.getInstance(config1);
        Realm realm2 = Realm.getInstance(config2);
        realm1.close();
        realm2.close();
    }

    public void testKeyStorage() throws Exception {
        // Generate a key and use it in a RealmConfiguration
        byte[] oldKey = TestHelper.getRandomKey(12345);
        byte[] key = oldKey;
        RealmConfiguration config = new RealmConfiguration.Builder(getContext()).encryptionKey(key).build();

        // Generate a different key and assign it to the same variable
        byte[] newKey = TestHelper.getRandomKey(67890);
        MoreAsserts.assertNotEqual(key, newKey);
        key = newKey;
        MoreAsserts.assertEquals(key, newKey);

        // Ensure that the stored key did not change
        MoreAsserts.assertEquals(oldKey, config.getEncryptionKey());
    }

    public void testModelClassesForDefaultMediator() throws Exception {
        assertTrue(defaultConfig.getSchemaMediator() instanceof DefaultRealmModuleMediator);

        final Set<Class<? extends RealmObject>> realmClasses = defaultConfig.getRealmObjectClasses();

        assertTrue(realmClasses.contains(AllTypes.class));

        // tests returned Set is unmodifiable.
        try {
            realmClasses.add(AllTypes.class);
            fail();
        } catch (UnsupportedOperationException ignored) {
        }
    }

    public void testModelClassesForGeneratedMediator() throws Exception {
        final RealmConfiguration config = new RealmConfiguration.Builder(getContext())
                .setModules(new HumanModule()).build();
        assertTrue(config.getSchemaMediator() instanceof HumanModuleMediator);

        final Set<Class<? extends RealmObject>> realmClasses = config.getRealmObjectClasses();

        assertFalse(realmClasses.contains(AllTypes.class));
        assertTrue(realmClasses.contains(CatOwner.class));
        assertFalse(realmClasses.contains(Cat.class));

        // tests returned Set is unmodifiable.
        try {
            realmClasses.add(AllTypes.class);
            fail();
        } catch (UnsupportedOperationException ignored) {
        }
    }

    public void testModelClassesForCompositeMediator() throws Exception {
        final RealmConfiguration config = new RealmConfiguration.Builder(getContext())
                .setModules(new HumanModule(), new AnimalModule()).build();
        assertTrue(config.getSchemaMediator() instanceof CompositeMediator);

        final Set<Class<? extends RealmObject>> realmClasses = config.getRealmObjectClasses();

        assertFalse(realmClasses.contains(AllTypes.class));
        assertTrue(realmClasses.contains(CatOwner.class));
        assertTrue(realmClasses.contains(Cat.class));

        // tests returned Set is unmodifiable.
        try {
            realmClasses.add(AllTypes.class);
            fail();
        } catch (UnsupportedOperationException ignored) {
        }
    }

    public void testModelClassesForFilterableMediator() throws Exception {
        //noinspection unchecked
        final RealmConfiguration config = new RealmConfiguration.Builder(getContext())
                .schema(AllTypes.class, CatOwner.class).build();
        assertTrue(config.getSchemaMediator() instanceof FilterableMediator);

        final Set<Class<? extends RealmObject>> realmClasses = config.getRealmObjectClasses();

        assertTrue(realmClasses.contains(AllTypes.class));
        assertTrue(realmClasses.contains(CatOwner.class));
        assertFalse(realmClasses.contains(Cat.class));

        // tests returned Set is unmodifiable.
        try {
            realmClasses.add(AllTypes.class);
            fail();
        } catch (UnsupportedOperationException ignored) {
        }
    }
}
