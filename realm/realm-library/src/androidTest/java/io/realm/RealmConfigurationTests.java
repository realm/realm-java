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

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.MoreAsserts;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Set;

import io.realm.entities.AllTypes;
import io.realm.entities.AllTypesPrimaryKey;
import io.realm.entities.AnimalModule;
import io.realm.entities.AssetFileModule;
import io.realm.entities.Cat;
import io.realm.entities.CatOwner;
import io.realm.entities.CyclicType;
import io.realm.entities.Dog;
import io.realm.entities.HumanModule;
import io.realm.entities.Owner;
import io.realm.entities.StringAndInt;
import io.realm.entities.StringOnly;
import io.realm.exceptions.RealmException;
import io.realm.exceptions.RealmFileException;
import io.realm.exceptions.RealmMigrationNeededException;
import io.realm.internal.modules.CompositeMediator;
import io.realm.internal.modules.FilterableMediator;
import io.realm.rule.TestRealmConfigurationFactory;
import io.realm.rx.RealmObservableFactory;
import io.realm.rx.RxObservableFactory;
import rx.Observable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
public class RealmConfigurationTests {
    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private Context context;
    private  RealmConfiguration defaultConfig;
    private Realm realm;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getTargetContext();
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
    public void getInstance_nullConfigThrows() {
        try {
            Realm.getInstance((RealmConfiguration) null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void constructBuilder_nullNameThrows() {
        try {
            new RealmConfiguration.Builder(context).name(null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void constructBuilder_emptyNameThrows() {
        try {
            new RealmConfiguration.Builder(context).name("");
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void directory_null() {
        new RealmConfiguration.Builder(context).directory(null);
    }

    @Test
    public void directory_writeProtectedDir() {
        File dir = new File("/");
        thrown.expect(IllegalArgumentException.class);
        new RealmConfiguration.Builder(context).directory(dir);
    }

    @Test
    public void directory_dirIsAFile() throws IOException {
        File dir = configFactory.getRoot();
        File file = new File(dir, "dummyfile");
        assertTrue(file.createNewFile());
        thrown.expect(IllegalArgumentException.class);
        new RealmConfiguration.Builder(context).directory(file);
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
            new RealmConfiguration.Builder(context).encryptionKey(null);
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
                new RealmConfiguration.Builder(context).encryptionKey(key);
                fail("Key with length " + key.length + " should throw an exception");
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @Test
    public void constructBuilder_negativeVersionThrows() {
        try {
            new RealmConfiguration.Builder(context).schemaVersion(-1);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void constructBuilder_versionLessThanDiscVersionThrows() {
        realm = Realm.getInstance(new RealmConfiguration.Builder(context)
                .directory(configFactory.getRoot())
                .schemaVersion(42)
                .build());
        realm.close();

        int[] wrongVersions = new int[] { 0, 1, 41 };
        for (int version : wrongVersions) {
            try {
                realm = Realm.getInstance(new RealmConfiguration.Builder(context)
                        .directory(configFactory.getRoot())
                        .schemaVersion(version)
                        .build());
                fail("Version " + version + " should throw an exception");
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @Test
    public void constructBuilder_versionEqualWhenSchemaChangesThrows() {
        // Creates initial Realm.
        RealmConfiguration config = new RealmConfiguration.Builder(context)
                .directory(configFactory.getRoot())
                .schemaVersion(42)
                .schema(StringOnly.class)
                .build();
        Realm.getInstance(config).close();

        // Creates new instance with a configuration containing another schema.
        try {
            config = new RealmConfiguration.Builder(context)
                    .directory(configFactory.getRoot())
                    .schemaVersion(42)
                    .schema(StringAndInt.class)
                    .build();
            realm = Realm.getInstance(config);
            fail("A migration should be required");
        } catch (RealmMigrationNeededException ignored) {
        }
    }

    // Only Dog is included in the schema definition, but in order to create Dog, the Owner has to be defined as well.
    @Test
    public void schemaDoesNotContainAllDefinedObjectShouldThrow() {
        RealmConfiguration config = new RealmConfiguration.Builder(context)
                .directory(configFactory.getRoot())
                .schema(Dog.class)
                .build();
        thrown.expect(IllegalStateException.class);
        realm = Realm.getInstance(config);
    }

    @Test
    public void migration_nullThrows() {
        try {
            new RealmConfiguration.Builder(context).migration(null).build();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void modules_nonRealmModulesThrows() {
        // Tests first argument.
        try {
            new RealmConfiguration.Builder(context).modules(new Object());
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        // Tests second argument.
        try {
            new RealmConfiguration.Builder(context).modules(Realm.getDefaultModule(), new Object());
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void modules() {
        RealmConfiguration realmConfig = new RealmConfiguration.Builder(context)
                .directory(configFactory.getRoot())
                .modules(Realm.getDefaultModule(), (Object) null)
                .build();
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
        RealmConfiguration config = new RealmConfiguration.Builder(context)
                .directory(configFactory.getRoot())
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
        // Populates v0 of a Realm with an object.
        RealmConfiguration config = new RealmConfiguration.Builder(context)
                .directory(configFactory.getRoot())
                .schema(StringOnly.class)
                .schemaVersion(0)
                .build();
        Realm.deleteRealm(config);
        realm = Realm.getInstance(config);
        realm.beginTransaction();
        realm.copyToRealm(new StringOnly());
        realm.commitTransaction();
        assertEquals(1, realm.where(StringOnly.class).count());
        realm.close();

        // Changes schema and verifies that Realm has been cleared.
        config = new RealmConfiguration.Builder(context)
                .directory(configFactory.getRoot())
                .schema(StringOnly.class, StringAndInt.class)
                .schemaVersion(1)
                .deleteRealmIfMigrationNeeded()
                .build();
        realm = Realm.getInstance(config);
        assertEquals(0, realm.where(StringOnly.class).count());
    }

    @Test
    public void deleteRealmIfMigrationNeeded_failsWhenAssetFileProvided() {
        Context context = InstrumentationRegistry.getInstrumentation().getContext();

        // Has a builder instance to isolate codepath.
        RealmConfiguration.Builder builder = new RealmConfiguration.Builder(context);
        try {
            builder
                .assetFile("asset_file.realm")
                .deleteRealmIfMigrationNeeded();
            fail();
        } catch (IllegalStateException expected) {
            assertEquals("Realm cannot clear its schema when previously configured to use an asset file by calling assetFile().",
                    expected.getMessage());
        }
    }

    @Test
    public void upgradeVersionWithNoMigration() {
        realm = Realm.getInstance(defaultConfig);
        assertEquals(0, realm.getVersion());
        realm.close();

        // Version upgrades should always require a migration.
        try {
            realm = Realm.getInstance(new RealmConfiguration.Builder(context)
                    .directory(configFactory.getRoot())
                    .schemaVersion(42)
                    .build());
            fail();
        } catch (RealmMigrationNeededException expected) {
            // And it should come with a cause.
            assertEquals("Realm on disk need to migrate from v0 to v42", expected.getMessage());
        }
    }

    @Test
    public void equals() {
        RealmConfiguration config1 = new RealmConfiguration.Builder(context).build();
        RealmConfiguration config2 = new RealmConfiguration.Builder(context).build();
        assertTrue(config1.equals(config2));
    }

    @Test
    public void equals_respectReadOnly() {
        RealmConfiguration config1 = new RealmConfiguration.Builder(context).assetFile("foo").build();
        RealmConfiguration config2 = new RealmConfiguration.Builder(context).assetFile("foo").readOnly().build();
        assertFalse(config1.equals(config2));
    }

    @Test
    public void equalsWhenRxJavaUnavailable() {
        // Test for https://github.com/realm/realm-java/issues/2416
        RealmConfiguration config1 = new RealmConfiguration.Builder(context).directory(configFactory.getRoot()).build();
        TestHelper.emulateRxJavaUnavailable(config1);
        RealmConfiguration config2 = new RealmConfiguration.Builder(context).directory(configFactory.getRoot()).build();
        TestHelper.emulateRxJavaUnavailable(config2);
        assertTrue(config1.equals(config2));
    }

    @Test
    public void hashCode_Test() {
        RealmConfiguration config1 = new RealmConfiguration.Builder(context).directory(configFactory.getRoot()).build();
        RealmConfiguration config2 = new RealmConfiguration.Builder(context).directory(configFactory.getRoot()).build();
        assertEquals(config1.hashCode(), config2.hashCode());
    }

    @Test
    public void equals_withCustomModules() {
        RealmConfiguration config1 = new RealmConfiguration.Builder(context)
                .directory(configFactory.getRoot())
                .modules(new HumanModule(), new AnimalModule())
                .build();

        RealmConfiguration config2 = new RealmConfiguration.Builder(context)
                .directory(configFactory.getRoot())
                .modules(new AnimalModule(), new HumanModule())
                .build();

        assertTrue(config1.equals(config2));
    }

    @Test
    public void hashCode_withCustomModules() {
        RealmConfiguration config1 = new RealmConfiguration.Builder(context)
                .directory(configFactory.getRoot())
                .modules(new HumanModule(), new AnimalModule())
                .build();
        RealmConfiguration config2 = new RealmConfiguration.Builder(context)
                .directory(configFactory.getRoot())
                .modules(new AnimalModule(), new HumanModule())
                .build();

        assertEquals(config1.hashCode(), config2.hashCode());
    }

    @Test
    public void hashCode_withDifferentRxObservableFactory() {
        RealmConfiguration config1 = new RealmConfiguration.Builder(context)
                .directory(configFactory.getRoot())
                .rxFactory(new RealmObservableFactory())
                .build();
        RealmConfiguration config2 = new RealmConfiguration.Builder(context)
                .directory(configFactory.getRoot())
                .rxFactory(new RealmObservableFactory() {
                    @Override
                    public int hashCode() {
                        return super.hashCode() + 1;
                    }
                })
                .build();

        assertNotEquals(config1.hashCode(), config2.hashCode());
    }

    @Test
    public void equals_configurationsReturnCachedRealm() {
        Realm realm1 = Realm.getInstance(new RealmConfiguration.Builder(context).directory(configFactory.getRoot()).build());
        Realm realm2 = Realm.getInstance(new RealmConfiguration.Builder(context).directory(configFactory.getRoot()).build());
        try {
            assertEquals(realm1, realm2);
        } finally {
            realm1.close();
            realm2.close();
        }
    }

    @Test
    public void schemaVersion_differentVersionsThrows() {
        RealmConfiguration config1 = new RealmConfiguration.Builder(context).directory(configFactory.getRoot()).schemaVersion(1).build();
        RealmConfiguration config2 = new RealmConfiguration.Builder(context).directory(configFactory.getRoot()).schemaVersion(2).build();

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
        RealmConfiguration config1 = new RealmConfiguration.Builder(context)
                .directory(configFactory.getRoot())
                .encryptionKey(TestHelper.getRandomKey())
                .build();
        RealmConfiguration config2 = new RealmConfiguration.Builder(context)
                .directory(configFactory.getRoot())
                .encryptionKey(TestHelper.getRandomKey())
                .build();

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
        RealmConfiguration config1 = new RealmConfiguration.Builder(context)
                .directory(configFactory.getRoot())
                .schema(StringOnly.class)
                .build();
        RealmConfiguration config2 = new RealmConfiguration.Builder(context)
                .directory(configFactory.getRoot())
                .schema(StringAndInt.class).build();

        Realm realm1 = Realm.getInstance(config1);
        try {
            Realm.getInstance(config2);
            fail();
        } catch (IllegalArgumentException ignored) {
        } finally {
            realm1.close();
        }
    }

    // Creates Realm instances with same name but different durabilities is not allowed.
    @Test
    public void inMemory_differentDurabilityThrows() {
        RealmConfiguration config1 = new RealmConfiguration.Builder(context)
                .directory(configFactory.getRoot())
                .inMemory()
                .build();
        RealmConfiguration config2 = new RealmConfiguration.Builder(context)
                .directory(configFactory.getRoot())
                .build();

        // Creates In-memory Realm first.
        Realm realm1 = Realm.getInstance(config1);
        try {
            // On-disk Realm then. Not allowed!
            Realm.getInstance(config2);
            fail();
        } catch (IllegalArgumentException ignored) {
        } finally {
            realm1.close();
        }

        // Creates on-disk Realm first.
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

    // It is allowed to create multiple Realm with same name but in different directory.
    @Test
    public void constructBuilder_differentDirSameName() throws IOException {
        RealmConfiguration config1 = new RealmConfiguration.Builder(context).directory(configFactory.getRoot()).build();
        RealmConfiguration config2 = new RealmConfiguration.Builder(context).directory(configFactory.newFolder()).build();

        Realm realm1 = Realm.getInstance(config1);
        Realm realm2 = Realm.getInstance(config2);
        realm1.close();
        realm2.close();
    }

    @Test
    public void encryptionKey_keyStorage() throws Exception {
        // Generates a key and uses it in a RealmConfiguration.
        byte[] oldKey = TestHelper.getRandomKey(12345);
        byte[] key = oldKey;
        RealmConfiguration config = new RealmConfiguration.Builder(context)
                .directory(configFactory.getRoot())
                .encryptionKey(key)
                .build();

        // Generates a different key and assigns it to the same variable.
        byte[] newKey = TestHelper.getRandomKey(67890);
        MoreAsserts.assertNotEqual(key, newKey);
        key = newKey;
        MoreAsserts.assertEquals(key, newKey);

        // Ensures that the stored key did not change.
        MoreAsserts.assertEquals(oldKey, config.getEncryptionKey());
    }

    @Test
    public void modelClassesForDefaultMediator() throws Exception {
        assertTrue(defaultConfig.getSchemaMediator() instanceof DefaultRealmModuleMediator);

        final Set<Class<? extends RealmModel>> realmClasses = defaultConfig.getRealmObjectClasses();

        assertTrue(realmClasses.contains(AllTypes.class));

        // Tests returned Set is unmodifiable.
        try {
            realmClasses.add(AllTypes.class);
            fail();
        } catch (UnsupportedOperationException ignored) {
        }
    }

    @Test
    public void modelClasses_forGeneratedMediator() throws Exception {
        final RealmConfiguration config = new RealmConfiguration.Builder(context)
                .directory(configFactory.getRoot())
                .modules(new HumanModule())
                .build();
        assertTrue(config.getSchemaMediator() instanceof HumanModuleMediator);

        final Set<Class<? extends RealmModel>> realmClasses = config.getRealmObjectClasses();

        assertFalse(realmClasses.contains(AllTypes.class));
        assertTrue(realmClasses.contains(CatOwner.class));
        assertFalse(realmClasses.contains(Cat.class));

        // Tests returned Set is unmodifiable.
        try {
            realmClasses.add(AllTypes.class);
            fail();
        } catch (UnsupportedOperationException ignored) {
        }
    }

    @Test
    public void modelClasses_forCompositeMediator() throws Exception {
        final RealmConfiguration config = new RealmConfiguration.Builder(context)
                .directory(configFactory.getRoot())
                .modules(new HumanModule(), new AnimalModule())
                .build();
        assertTrue(config.getSchemaMediator() instanceof CompositeMediator);

        final Set<Class<? extends RealmModel>> realmClasses = config.getRealmObjectClasses();

        assertFalse(realmClasses.contains(AllTypes.class));
        assertTrue(realmClasses.contains(CatOwner.class));
        assertTrue(realmClasses.contains(Cat.class));

        // Tests returned Set is unmodifiable.
        try {
            realmClasses.add(AllTypes.class);
            fail();
        } catch (UnsupportedOperationException ignored) {
        }
    }

    @Test
    public void modelClasses_forFilterableMediator() throws Exception {
        //noinspection unchecked
        final RealmConfiguration config = new RealmConfiguration.Builder(context)
                .directory(configFactory.getRoot())
                .schema(AllTypes.class, CatOwner.class)
                .build();
        assertTrue(config.getSchemaMediator() instanceof FilterableMediator);

        final Set<Class<? extends RealmModel>> realmClasses = config.getRealmObjectClasses();

        assertTrue(realmClasses.contains(AllTypes.class));
        assertTrue(realmClasses.contains(CatOwner.class));
        assertFalse(realmClasses.contains(Cat.class));

        // Tests returned Set is unmodifiable.
        try {
            realmClasses.add(AllTypes.class);
            fail();
        } catch (UnsupportedOperationException ignored) {
        }
    }

    @Test
    public void rxFactory() {
        final RxObservableFactory dummyFactory = new RxObservableFactory() {
            @Override
            public Observable<Realm> from(Realm realm) {
                return null;
            }

            @Override
            public Observable<DynamicRealm> from(DynamicRealm realm) {
                return null;
            }

            @Override
            public <E extends RealmModel> Observable<RealmResults<E>> from(Realm realm, RealmResults<E> results) {
                return null;
            }

            @Override
            public Observable<RealmResults<DynamicRealmObject>> from(DynamicRealm realm, RealmResults<DynamicRealmObject> results) {
                return null;
            }

            @Override
            public <E extends RealmModel> Observable<RealmList<E>> from(Realm realm, RealmList<E> list) {
                return null;
            }

            @Override
            public Observable<RealmList<DynamicRealmObject>> from(DynamicRealm realm, RealmList<DynamicRealmObject> list) {
                return null;
            }

            @Override
            public <E extends RealmModel> Observable<E> from(Realm realm, E object) {
                return null;
            }

            @Override
            public Observable<DynamicRealmObject> from(DynamicRealm realm, DynamicRealmObject object) {
                return null;
            }

            @Override
            public <E extends RealmModel> Observable<RealmQuery<E>> from(Realm realm, RealmQuery<E> query) {
                return null;
            }

            @Override
            public Observable<RealmQuery<DynamicRealmObject>> from(DynamicRealm realm, RealmQuery<DynamicRealmObject> query) {
                return null;
            }
        };

        RealmConfiguration configuration1 = configFactory.createConfigurationBuilder()
                .rxFactory(dummyFactory)
                .build();
        assertTrue(configuration1.getRxFactory() == dummyFactory);

        RealmConfiguration configuration2 = configFactory.createConfigurationBuilder()
                .build();
        assertNotNull(configuration2.getRxFactory());
        assertFalse(configuration2.getRxFactory() == dummyFactory);
    }

    @Test
    public void initialDataTransactionEqual() {
        final Realm.Transaction transaction = new Realm.Transaction() {
            @Override
            public void execute(final Realm realm) {
            }
        };

        RealmConfiguration configuration = configFactory.createConfigurationBuilder()
                .initialData(transaction)
                .build();

        assertEquals(transaction, configuration.getInitialDataTransaction());
    }

    @Test
    public void initialDataTransactionNull() {
        assertNull(defaultConfig.getInitialDataTransaction());

        realm = Realm.getInstance(defaultConfig);
        assertTrue(realm.isEmpty());
    }

    @Test
    public void initialDataTransactionNotNull() {
        // Removes default instance.
        Realm.deleteRealm(defaultConfig);

        RealmConfiguration configuration = configFactory.createConfigurationBuilder()
                .initialData(new Realm.Transaction() {
                    @Override
                    public void execute(final Realm realm) {
                        realm.createObject(AllTypes.class);
                        realm.createObject(Owner.class).setCat(realm.createObject(Cat.class));
                    }
                }).build();

        realm = Realm.getInstance(configuration);

        // First time check for initial data.
        assertEquals(1, realm.where(AllTypes.class).count());
        assertEquals(1, realm.where(Owner.class).count());
        assertEquals(1, realm.where(Cat.class).count());

        realm.beginTransaction();
        realm.delete(AllTypes.class);
        realm.commitTransaction();

        assertEquals(0, realm.where(AllTypes.class).count());

        realm.close();
        realm = Realm.getInstance(configuration);
        // Checks if there is still the same data.
        assertEquals(0, realm.where(AllTypes.class).count());
        assertEquals(1, realm.where(Owner.class).count());
        assertEquals(1, realm.where(Cat.class).count());
    }

    @Test
    public void initialDataTransactionExecutionCount() {
        // Removes default instance.
        Realm.deleteRealm(defaultConfig);

        Realm.Transaction transaction = mock(Realm.Transaction.class);
        RealmConfiguration configuration = configFactory.createConfigurationBuilder()
                .initialData(transaction)
                .build();

        realm = Realm.getInstance(configuration);
        realm.close();
        verify(transaction, times(1)).execute(realm);

        realm = Realm.getInstance(configuration);
        realm.close();
        verify(transaction, never()).execute(realm);
    }

    @Test
    public void initialDataTransactionAssetFile() throws IOException {
        // Removes default instance.
        Realm.deleteRealm(defaultConfig);

        Context context = InstrumentationRegistry.getInstrumentation().getContext();
        configFactory.copyRealmFromAssets(context, "asset_file.realm", Realm.DEFAULT_REALM_NAME);
        assertTrue(new File(configFactory.getRoot(), Realm.DEFAULT_REALM_NAME).exists());

        Realm.Transaction transaction = mock(Realm.Transaction.class);
        RealmConfiguration configuration = configFactory.createConfigurationBuilder()
                .modules(new AssetFileModule())
                .initialData(transaction)
                .build();

        realm = Realm.getInstance(configuration);
        realm.close();
        verify(transaction, never()).execute(realm);
    }

    @Test
    public void assetFileNullAndEmptyFileName() {
        try {
            new RealmConfiguration.Builder(context).assetFile(null).build();
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        try {
            new RealmConfiguration.Builder(context).assetFile("").build();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void assetFileWithInMemoryConfig() {
        // Ensures that there is no data.
        Realm.deleteRealm(new RealmConfiguration.Builder(context).build());

        try {
            new RealmConfiguration.Builder(context).assetFile("asset_file.realm").inMemory().build();
            fail();
        } catch (RealmException ignored) {
        }
    }

    @Test
    public void assetFileFakeFile() {
        // Ensures that there is no data.
        Realm.deleteRealm(new RealmConfiguration.Builder(context).build());

        RealmConfiguration configuration = new RealmConfiguration.Builder(context).assetFile("no_file").build();
        try {
            Realm.getInstance(configuration);
            fail();
        } catch (RealmFileException expected) {
            assertEquals(expected.getKind(), RealmFileException.Kind.ACCESS_ERROR);
        }
    }

    @Test
    public void assetFileValidFile() throws IOException {
        // Ensures that there is no data.
        Realm.deleteRealm(new RealmConfiguration.Builder(context).build());

        RealmConfiguration configuration = new RealmConfiguration
                .Builder(context)
                .modules(new AssetFileModule())
                .assetFile("asset_file.realm")
                .build();
        Realm.deleteRealm(configuration);

        File realmFile = new File(configuration.getPath());
        assertFalse(realmFile.exists());

        realm = Realm.getInstance(configuration);
        assertTrue(realmFile.exists());

        // Asset file has 10 Owners and 10 Cats, checks if data is present.
        assertEquals(10, realm.where(Owner.class).count());
        assertEquals(10, realm.where(Cat.class).count());

        realm.close();

        // Copies original file to another location.
        configFactory.copyRealmFromAssets(context, "asset_file.realm", "asset_file_copy.realm");
        File copyFromAsset = new File(configFactory.getRoot(), "asset_file_copy.realm");
        assertTrue(copyFromAsset.exists());

        Realm.deleteRealm(configuration);
        assertFalse(realmFile.exists());
    }

    @Test
    public void assetFile_failsWhenDeleteRealmIfMigrationNeededConfigured() {
        Context context = InstrumentationRegistry.getInstrumentation().getContext();

        // Has a builder instance to isolate codepath.
        RealmConfiguration.Builder builder = new RealmConfiguration.Builder(context);
        try {
            builder
                    .deleteRealmIfMigrationNeeded()
                    .assetFile("asset_file.realm");
            fail();
        } catch (IllegalStateException expected) {
            assertEquals("Realm cannot use an asset file when previously configured to clear its schema in migration by calling deleteRealmIfMigrationNeeded().",
                    expected.getMessage());
        }
    }

    private static class MigrationWithNoEquals implements RealmMigration {
        @Override
        public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
            // Does nothing.
        }
    }

    @Test
    public void detectMissingEqualsInCustomMigration() {
        RealmConfiguration config1 = configFactory.createConfigurationBuilder().migration(new MigrationWithNoEquals()).build();
        RealmConfiguration config2 = configFactory.createConfigurationBuilder().migration(new MigrationWithNoEquals()).build();

        Realm realm = Realm.getInstance(config1);
        try {
            Realm.getInstance(config2);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("The most likely cause is that equals() and hashCode() are not overridden"));
        } finally {
            realm.close();
        }
    }

    @Test
    public void readOnly_initialTransaction_throws() {
        // Check assetFile(), then initialTransaction();
        RealmConfiguration.Builder config = new RealmConfiguration.Builder()
                .assetFile("foo")
                .readOnly()
                .initialData(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        // Do nothing
                    }
                });

        try {
            config.build();
            fail();
        } catch (IllegalStateException ignored) {
        }
    }

    @Test
    public void readOnly_deleteRealmIfMigrationRequired_throws() {
        try {
            // This test doesn't actually fail on 'deleteRealmIfMigrationNeeded' + 'readOnly' but on
            // 'assetFile' + 'deleteRealmIfMigrationNeed()'. This test is primarely here to prevent this
            // case from accidentally parsing in the future.
            new RealmConfiguration.Builder()
                    .assetFile("foo")
                    .readOnly()
                    .deleteRealmIfMigrationNeeded()
                    .build();
            fail();
        } catch (IllegalStateException ignored) {
        }
    }
}
