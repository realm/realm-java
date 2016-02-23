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

import android.support.test.runner.AndroidJUnit4;
import android.test.MoreAsserts;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
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
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class RealmConfigurationTests {
    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    RealmConfiguration defaultConfig;
    Realm realm;

    @Before
    public void setUp() {
        defaultConfig = configFactory.createConfiguration();
    }

    @After
    public void tearDown() throws Exception {
        if (realm != null) {
            realm.close();
        }
    }

    private void clearDefaultConfiguration() throws NoSuchFieldException, IllegalAccessException {
        final Field field = Realm.class.getDeclaredField("defaultConfiguration");
        field.setAccessible(true);
        field.set(null, null);
    }

    @Test
    public void setDefaultConfiguration_nullThrows() throws NoSuchFieldException, IllegalAccessException {
        clearDefaultConfiguration();
        try {
            Realm.setDefaultConfiguration(null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void getDefaultInstance_nullThrows() throws NoSuchFieldException, IllegalAccessException {
        clearDefaultConfiguration();
        try {
            Realm.getDefaultInstance();
            fail();
        } catch (NullPointerException ignored) {
        }
    }

    @Test
    public void getInstance_nullConfigThrows() {
        try {
            Realm.getInstance((RealmConfiguration) null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void constructBuilder_nullDirThrows() {
        try {
            new RealmConfiguration.Builder((File) null).build();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void constructBuilder_createSubFoldersThrows() {
        File folder = new File(configFactory.getRoot() + "/subfolder1/subfolder2/");
        try {
            new RealmConfiguration.Builder(folder).build();
            fail("Assuming that sub folders are created automatically should fail.");
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void constructBuilder_nullNameThrows() {
        try {
            new RealmConfiguration.Builder(configFactory.getRoot()).name(null).build();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void constructBuilder_emptyNameThrows() {
        try {
            new RealmConfiguration.Builder(configFactory.getRoot()).name("").build();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void getInstance_idForHashCollision() {
        // Ea.hashCode() == FB.hashCode()
        RealmConfiguration configA = configFactory.createConfiguration("Ea");
        RealmConfiguration configB = configFactory.createConfiguration("FB");

        Realm r1 = Realm.getInstance(configA);
        Realm r2 = Realm.getInstance(configB);
        assertNotSame(r1, r2);
        r1.close();
        r2.close();
    }

    @Test
    public void constructBuilder_nullKeyThrows() {
        try {
            new RealmConfiguration.Builder(configFactory.getRoot()).encryptionKey(null).build();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void constructBuilder_wrongKeyLengthThrows() {
        byte[][] wrongKeys = new byte[][] {
                new byte[0],
                new byte[RealmConfiguration.KEY_LENGTH - 1],
                new byte[RealmConfiguration.KEY_LENGTH + 1]
        };
        for (byte[] key : wrongKeys) {
            try {
                new RealmConfiguration.Builder(configFactory.getRoot()).encryptionKey(key).build();
                fail("Key with length " + key.length + " should throw an exception");
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @Test
    public void constructBuilder_negativeVersionThrows() {
        try {
            new RealmConfiguration.Builder(configFactory.getRoot()).schemaVersion(-1).build();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void constructBuilder_versionLessThanDiscVersionThrows() {
        realm = Realm.getInstance(new RealmConfiguration.Builder(configFactory.getRoot()).schemaVersion(42).build());
        realm.close();

        int[] wrongVersions = new int[] { 0, 1, 41 };
        for (int version : wrongVersions) {
            try {
                realm = Realm.getInstance(new RealmConfiguration.Builder(configFactory.getRoot())
                        .schemaVersion(version).build());
                fail("Version " + version + " should throw an exception");
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @Test
    public void constructBuilder_versionEqualWhenSchemaChangesThrows() {
        // Create initial Realm
        RealmConfiguration config = new RealmConfiguration.Builder(configFactory.getRoot())
                .schemaVersion(42).schema(Dog.class).build();
        Realm.getInstance(config).close();

        // Create new instance with a configuration containing another schema
        try {
            config = new RealmConfiguration.Builder(configFactory.getRoot())
                    .schemaVersion(42).schema(AllTypesPrimaryKey.class).build();
            realm = Realm.getInstance(config);
            fail("A migration should be required");
        } catch (RealmMigrationNeededException ignored) {
        }
    }

    @Test
    public void customSchemaDontIncludeLinkedClasses() {
        RealmConfiguration config = new RealmConfiguration.Builder(configFactory.getRoot()).schema(Dog.class).build();
        realm = Realm.getInstance(config);
        try {
            assertEquals(3, realm.getTable(Owner.class).getColumnCount());
            fail("Owner should to be part of the schema");
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void migration_nullThrows() {
        try {
            new RealmConfiguration.Builder(configFactory.getRoot()).migration(null).build();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void setModules_nonRealmModulesThrows() {
        // Test first argument
        try {
            new RealmConfiguration.Builder(configFactory.getRoot()).setModules(new Object());
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        // Test second argument
        try {
            new RealmConfiguration.Builder(configFactory.getRoot()).setModules(Realm.getDefaultModule(), new Object());
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void setModules() {
        RealmConfiguration realmConfig = new RealmConfiguration.Builder(configFactory.getRoot())
                .setModules(Realm.getDefaultModule(), (Object) null).build();
        realm = Realm.getInstance(realmConfig);
        assertNotNull(realm.getTable(AllTypes.class));
    }

    @Test
    public void setDefaultConfiguration() throws NoSuchFieldException, IllegalAccessException {
        clearDefaultConfiguration();
        Realm.setDefaultConfiguration(defaultConfig);
        realm = Realm.getDefaultInstance();
        assertEquals(realm.getPath(), defaultConfig.getPath());
    }

    @Test
    public void getInstance() {
        realm = Realm.getInstance(defaultConfig);
        assertEquals(realm.getPath(), defaultConfig.getPath());
    }

    @Test
    public void standardSetup() {
        RealmConfiguration config = new RealmConfiguration.Builder(configFactory.getRoot())
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

    @Test
    public void deleteRealmIfMigrationNeeded() {
        // Populate v0 of a Realm with an object
        RealmConfiguration config = new RealmConfiguration.Builder(configFactory.getRoot())
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
        config = new RealmConfiguration.Builder(configFactory.getRoot())
                .schema(Owner.class, Dog.class)
                .schemaVersion(1)
                .deleteRealmIfMigrationNeeded()
                .build();
        realm = Realm.getInstance(config);
        assertEquals(0, realm.where(Dog.class).count());
    }

    @Test
    public void upgradeVersionWithNoMigration() {
        realm = Realm.getInstance(defaultConfig);
        assertEquals(0, realm.getVersion());
        realm.close();

        // Version upgrades should always require a migration.
        try {
            realm = Realm.getInstance(new RealmConfiguration.Builder(configFactory.getRoot())
                    .schemaVersion(42).build());
            fail();
        } catch (RealmMigrationNeededException ignored) {
        }
    }

    @Test
    public void equals() {
        RealmConfiguration config1 = new RealmConfiguration.Builder(configFactory.getRoot()).build();
        RealmConfiguration config2 = new RealmConfiguration.Builder(configFactory.getRoot()).build();
        assertTrue(config1.equals(config2));
    }

    @Test
    public void hashCode_Test() {
        RealmConfiguration config1 = new RealmConfiguration.Builder(configFactory.getRoot()).build();
        RealmConfiguration config2 = new RealmConfiguration.Builder(configFactory.getRoot()).build();
        assertEquals(config1.hashCode(), config2.hashCode());
    }

    @Test
    public void equals_withCustomModules() {
        RealmConfiguration config1 = new RealmConfiguration.Builder(configFactory.getRoot())
                .setModules(new HumanModule(), new AnimalModule())
                .build();

        RealmConfiguration config2 = new RealmConfiguration.Builder(configFactory.getRoot())
                .setModules(new AnimalModule(), new HumanModule())
                .build();

        assertTrue(config1.equals(config2));
    }

    @Test
    public void hashCode_withCustomModules() {
        RealmConfiguration config1 = new RealmConfiguration.Builder(configFactory.getRoot())
                .setModules(new HumanModule(), new AnimalModule())
                .build();
        RealmConfiguration config2 = new RealmConfiguration.Builder(configFactory.getRoot())
                .setModules(new AnimalModule(), new HumanModule())
                .build();

        assertEquals(config1.hashCode(), config2.hashCode());
    }

    @Test
    public void equals_configurationsReturnCachedRealm() {
        Realm realm1 = Realm.getInstance(new RealmConfiguration.Builder(configFactory.getRoot()).build());
        Realm realm2 = Realm.getInstance(new RealmConfiguration.Builder(configFactory.getRoot()).build());
        try {
            assertEquals(realm1, realm2);
        } finally {
            realm1.close();
            realm2.close();
        }
    }

    @Test
    public void schemaVersion_differentVersionsThrows() {
        RealmConfiguration config1 = new RealmConfiguration.Builder(configFactory.getRoot()).schemaVersion(1).build();
        RealmConfiguration config2 = new RealmConfiguration.Builder(configFactory.getRoot()).schemaVersion(2).build();

        Realm realm1 = Realm.getInstance(config1);
        try {
            Realm.getInstance(config2);
            fail();
        } catch (IllegalArgumentException ignored) {
        } finally {
            realm1.close();
        }
    }

    @Test
    public void encryptionKey_differentEncryptionKeysThrows() {
        RealmConfiguration config1 = new RealmConfiguration.Builder(configFactory.getRoot())
                .encryptionKey(TestHelper.getRandomKey()).build();
        RealmConfiguration config2 = new RealmConfiguration.Builder(configFactory.getRoot())
                .encryptionKey(TestHelper.getRandomKey()).build();

        Realm realm1 = Realm.getInstance(config1);
        try {
            realm = Realm.getInstance(config2);
            fail();
        } catch (IllegalArgumentException ignored) {
        } finally {
            realm1.close();
        }
    }

    @Test
    public void schema_differentSchemasThrows() {
        RealmConfiguration config1 = new RealmConfiguration.Builder(configFactory.getRoot())
                .schema(AllTypes.class).build();
        RealmConfiguration config2 = new RealmConfiguration.Builder(configFactory.getRoot())
                .schema(CyclicType.class).build();

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
    @Test
    public void inMemory_differentDurabilityThrows() {
        RealmConfiguration config1 = new RealmConfiguration.Builder(configFactory.getRoot()).inMemory().build();
        RealmConfiguration config2 = new RealmConfiguration.Builder(configFactory.getRoot()).build();

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
    @Test
    public void constructBuilder_differentDirSameName() throws IOException {
        RealmConfiguration config1 = new RealmConfiguration.Builder(configFactory.getRoot()).build();
        RealmConfiguration config2 = new RealmConfiguration.Builder(configFactory.newFolder()).build();

        Realm realm1 = Realm.getInstance(config1);
        Realm realm2 = Realm.getInstance(config2);
        realm1.close();
        realm2.close();
    }

    @Test
    public void encryptionKey_keyStorage() throws Exception {
        // Generate a key and use it in a RealmConfiguration
        byte[] oldKey = TestHelper.getRandomKey(12345);
        byte[] key = oldKey;
        RealmConfiguration config = new RealmConfiguration.Builder(configFactory.getRoot()).encryptionKey(key).build();

        // Generate a different key and assign it to the same variable
        byte[] newKey = TestHelper.getRandomKey(67890);
        MoreAsserts.assertNotEqual(key, newKey);
        key = newKey;
        MoreAsserts.assertEquals(key, newKey);

        // Ensure that the stored key did not change
        MoreAsserts.assertEquals(oldKey, config.getEncryptionKey());
    }

    @Test
    public void modelClassesForDefaultMediator() throws Exception {
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

    @Test
    public void modelClasses_forGeneratedMediator() throws Exception {
        final RealmConfiguration config = new RealmConfiguration.Builder(configFactory.getRoot())
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

    @Test
    public void modelClasses_forCompositeMediator() throws Exception {
        final RealmConfiguration config = new RealmConfiguration.Builder(configFactory.getRoot())
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

    @Test
    public void modelClasses_forFilterableMediator() throws Exception {
        //noinspection unchecked
        final RealmConfiguration config = new RealmConfiguration.Builder(configFactory.getRoot())
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
