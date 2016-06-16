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

import android.content.Context;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import io.realm.annotations.RealmModule;
import io.realm.exceptions.RealmException;
import io.realm.internal.RealmCore;
import io.realm.internal.RealmProxyMediator;
import io.realm.internal.SharedGroup;
import io.realm.internal.modules.CompositeMediator;
import io.realm.internal.modules.FilterableMediator;
import io.realm.rx.RealmObservableFactory;
import io.realm.rx.RxObservableFactory;

/**
 * A RealmConfiguration is used to setup a specific Realm instance.
 * <p>
 * Instances of a RealmConfiguration can only created by using the {@link io.realm.RealmConfiguration.Builder} and calling
 * its {@link io.realm.RealmConfiguration.Builder#build()} method.
 * <p>
 * A commonly used RealmConfiguration can easily be accessed by first saving it as
 * {@link Realm#setDefaultConfiguration(RealmConfiguration)} and then using {@link io.realm.Realm#getDefaultInstance()}.
 * <p>
 * A minimal configuration can be created using:
 * <p>
 * {@code RealmConfiguration config = new RealmConfiguration.Builder(getContext()).build())}
 * <p>
 * This will create a RealmConfiguration with the following properties.
 * <ul>
 * <li>Realm file is called "default.realm"</li>
 * <li>It is saved in Context.getFilesDir()</li>
 * <li>It has its schema version set to 0.</li>
 * </ul>
 */
public final class RealmConfiguration {

    public static final String DEFAULT_REALM_NAME = "default.realm";
    public static final int KEY_LENGTH = 64;

    private static final Object DEFAULT_MODULE;
    private static final RealmProxyMediator DEFAULT_MODULE_MEDIATOR;
    private static Boolean rxJavaAvailable;

    static {
        DEFAULT_MODULE = Realm.getDefaultModule();
        if (DEFAULT_MODULE != null) {
            final RealmProxyMediator mediator = getModuleMediator(DEFAULT_MODULE.getClass().getCanonicalName());
            if (!mediator.transformerApplied()) {
                throw new ExceptionInInitializerError("RealmTransformer doesn't seem to be applied." +
                        " Please update the project configuration to use the Realm Gradle plugin." +
                        " See https://realm.io/news/android-installation-change/");
            }
            DEFAULT_MODULE_MEDIATOR = mediator;
        } else {
            DEFAULT_MODULE_MEDIATOR = null;
        }
    }

    private final File realmFolder;
    private final String realmFileName;
    private final String canonicalPath;
    private final String assetFilePath;
    private final byte[] key;
    private final long schemaVersion;
    private final RealmMigration migration;
    private final boolean deleteRealmIfMigrationNeeded;
    private final SharedGroup.Durability durability;
    private final RealmProxyMediator schemaMediator;
    private final RxObservableFactory rxObservableFactory;
    private final Realm.Transaction initialDataTransaction;
    private final WeakReference<Context> contextWeakRef;

    private RealmConfiguration(Builder builder) {
        this.realmFolder = builder.folder;
        this.realmFileName = builder.fileName;
        this.canonicalPath = Realm.getCanonicalPath(new File(realmFolder, realmFileName));
        this.assetFilePath = builder.assetFilePath;
        this.key = builder.key;
        this.schemaVersion = builder.schemaVersion;
        this.deleteRealmIfMigrationNeeded = builder.deleteRealmIfMigrationNeeded;
        this.migration = builder.migration;
        this.durability = builder.durability;
        this.schemaMediator = createSchemaMediator(builder);
        this.rxObservableFactory = builder.rxFactory;
        this.initialDataTransaction = builder.initialDataTransaction;
        this.contextWeakRef = builder.contextWeakRef;
    }

    public File getRealmFolder() {
        return realmFolder;
    }

    public String getRealmFileName() {
        return realmFileName;
    }

    public byte[] getEncryptionKey() {
        return key == null ? null : Arrays.copyOf(key, key.length);
    }

    public long getSchemaVersion() {
        return schemaVersion;
    }

    public RealmMigration getMigration() {
        return migration;
    }

    public boolean shouldDeleteRealmIfMigrationNeeded() {
        return deleteRealmIfMigrationNeeded;
    }

    public SharedGroup.Durability getDurability() {
        return durability;
    }

    /**
     * Returns the mediator instance of schema which is defined by this configuration.
     *
     * @return the mediator of the schema.
     */
    RealmProxyMediator getSchemaMediator() {
        return schemaMediator;
    }

    /**
     * Returns the transaction instance with initial data.
     *
     * @return the initial data transaction.
     */
    Realm.Transaction getInitialDataTransaction() {
        return initialDataTransaction;
    }

    /**
     * Indicates if there is available asset file for copy action.
     *
     * @return {@code true} if there is asset file, {@code false} otherwise.
     */
    boolean hasAssetFile() {
        return !TextUtils.isEmpty(assetFilePath);
    }

    /**
     * Returns input stream object to the Realm asset file.
     *
     * @return input stream to the asset file.
     * @throws IOException if copying the file fails.
     */
    InputStream getAssetFile() throws IOException {
        Context context = contextWeakRef.get();
        if (context != null) {
            return context.getAssets().open(assetFilePath);
        } else {
            throw new IllegalArgumentException("Context should not be null. Use Application Context instead of Activity Context.");
        }
    }

    /**
     * Returns the unmodifiable {@link Set} of model classes that make up the schema for this Realm.
     *
     * @return unmodifiable {@link Set} of model classes.
     */
    public Set<Class<? extends RealmModel>> getRealmObjectClasses() {
        return schemaMediator.getModelClasses();
    }

    public String getPath() {
        return canonicalPath;
    }

    /**
     * Returns the {@link RxObservableFactory} that is used to create Rx Observables from Realm objects.
     *
     * @throws UnsupportedOperationException if the required RxJava framework is not on the classpath.
     * @return the factory instance used to create Rx Observables.
     */
    public RxObservableFactory getRxFactory() {
        // Since RxJava doesn't exist, rxObservableFactory is not initialized.
        if (rxObservableFactory == null) {
            throw new UnsupportedOperationException("RxJava seems to be missing from the classpath. " +
                    "Remember to add it as a compile dependency." +
                    " See https://realm.io/docs/java/latest/#rxjava for more details.");
        }
        return rxObservableFactory;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        RealmConfiguration that = (RealmConfiguration) obj;

        if (schemaVersion != that.schemaVersion) return false;
        if (deleteRealmIfMigrationNeeded != that.deleteRealmIfMigrationNeeded) return false;
        if (!realmFolder.equals(that.realmFolder)) return false;
        if (!realmFileName.equals(that.realmFileName)) return false;
        if (!canonicalPath.equals(that.canonicalPath)) return false;
        if (!Arrays.equals(key, that.key)) return false;
        if (!durability.equals(that.durability)) return false;
        if (migration != null ? !migration.equals(that.migration) : that.migration != null) return false;
        //noinspection SimplifiableIfStatement
        if (rxObservableFactory != null ? !rxObservableFactory.equals(that.rxObservableFactory) : that.rxObservableFactory != null) return false;
        if (initialDataTransaction != null ? !initialDataTransaction.equals(that.initialDataTransaction) : that.initialDataTransaction != null) return false;
        return schemaMediator.equals(that.schemaMediator);
    }

    @Override
    public int hashCode() {
        int result = realmFolder.hashCode();
        result = 31 * result + realmFileName.hashCode();
        result = 31 * result + canonicalPath.hashCode();
        result = 31 * result + (key != null ? Arrays.hashCode(key) : 0);
        result = 31 * result + (int)schemaVersion;
        result = 31 * result + (migration != null ? migration.hashCode() : 0);
        result = 31 * result + (deleteRealmIfMigrationNeeded ? 1 : 0);
        result = 31 * result + schemaMediator.hashCode();
        result = 31 * result + durability.hashCode();
        result = 31 * result + (rxObservableFactory != null ? rxObservableFactory.hashCode() : 0);
        result = 31 * result + (initialDataTransaction != null ? initialDataTransaction.hashCode() : 0);

        return result;
    }

    // Creates the mediator that defines the current schema
    private RealmProxyMediator createSchemaMediator(Builder builder) {

        Set<Object> modules = builder.modules;
        Set<Class<? extends RealmModel>> debugSchema = builder.debugSchema;

        // If using debug schema, use special mediator
        if (debugSchema.size() > 0) {
            return new FilterableMediator(DEFAULT_MODULE_MEDIATOR, debugSchema);
        }

        // If only one module, use that mediator directly
        if (modules.size() == 1) {
            return getModuleMediator(modules.iterator().next().getClass().getCanonicalName());
        }

        // Otherwise combine all mediators
        RealmProxyMediator[] mediators = new RealmProxyMediator[modules.size()];
        int i = 0;
        for (Object module : modules) {
            mediators[i] = getModuleMediator(module.getClass().getCanonicalName());
            i++;
        }
        return new CompositeMediator(mediators);
    }

    // Finds the mediator associated with a given module
    private static RealmProxyMediator getModuleMediator(String fullyQualifiedModuleClassName) {
        String[] moduleNameParts = fullyQualifiedModuleClassName.split("\\.");
        String moduleSimpleName = moduleNameParts[moduleNameParts.length - 1];
        String mediatorName = String.format("io.realm.%s%s", moduleSimpleName, "Mediator");
        Class<?> clazz;
        //noinspection TryWithIdenticalCatches
        try {
            clazz = Class.forName(mediatorName);
            Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
            constructor.setAccessible(true);
            return (RealmProxyMediator) constructor.newInstance();
        } catch (ClassNotFoundException e) {
            throw new RealmException("Could not find " + mediatorName, e);
        } catch (InvocationTargetException e) {
            throw new RealmException("Could not create an instance of " + mediatorName, e);
        } catch (InstantiationException e) {
            throw new RealmException("Could not create an instance of " + mediatorName, e);
        } catch (IllegalAccessException e) {
            throw new RealmException("Could not create an instance of " + mediatorName, e);
        }
    }

    @Override
    public String toString() {
        //noinspection StringBufferReplaceableByString
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("realmFolder: ").append(realmFolder.toString());
        stringBuilder.append("\n");
        stringBuilder.append("realmFileName : ").append(realmFileName);
        stringBuilder.append("\n");
        stringBuilder.append("canonicalPath: ").append(canonicalPath);
        stringBuilder.append("\n");
        stringBuilder.append("key: ").append("[length: ").append(key == null ? 0 : KEY_LENGTH).append("]");
        stringBuilder.append("\n");
        stringBuilder.append("schemaVersion: ").append(Long.toString(schemaVersion));
        stringBuilder.append("\n");
        stringBuilder.append("migration: ").append(migration);
        stringBuilder.append("\n");
        stringBuilder.append("deleteRealmIfMigrationNeeded: ").append(deleteRealmIfMigrationNeeded);
        stringBuilder.append("\n");
        stringBuilder.append("durability: ").append(durability);
        stringBuilder.append("\n");
        stringBuilder.append("schemaMediator: ").append(schemaMediator);

        return stringBuilder.toString();
    }

    /**
     * Checks if RxJava is can be loaded.
     *
     * @return {@code true} if RxJava dependency exist, {@code false} otherwise.
     */
    private static synchronized boolean isRxJavaAvailable() {
        if (rxJavaAvailable == null) {
            try {
                Class.forName("rx.Observable");
                rxJavaAvailable = true;
            } catch (ClassNotFoundException ignore) {
                rxJavaAvailable = false;
            }
        }
        return rxJavaAvailable;
    }

    /**
     * RealmConfiguration.Builder used to construct instances of a RealmConfiguration in a fluent manner.
     */
    public static final class Builder {
        private File folder;
        private String fileName;
        private String assetFilePath;
        private byte[] key;
        private long schemaVersion;
        private RealmMigration migration;
        private boolean deleteRealmIfMigrationNeeded;
        private SharedGroup.Durability durability;
        private HashSet<Object> modules = new HashSet<Object>();
        private HashSet<Class<? extends RealmModel>> debugSchema = new HashSet<Class<? extends RealmModel>>();
        private WeakReference<Context> contextWeakRef;
        private RxObservableFactory rxFactory;
        private Realm.Transaction initialDataTransaction;

        /**
         * Creates an instance of the Builder for the RealmConfiguration.
         * The Realm file will be saved in the provided folder.
         *
         * @param folder the folder to save Realm file in. Folder must be writable.
         * @throws IllegalArgumentException if folder doesn't exist or isn't writable.
         * @deprecated Please use {@link #Builder(Context, File)} instead.
         */
        @Deprecated
        public Builder(File folder) {
            RealmCore.loadLibrary();
            initializeBuilder(folder);
        }

        /**
         * Creates an instance of the Builder for the RealmConfiguration.
         * <p>
         * This will use the app's own internal directory for storing the Realm file. This does not require any
         * additional permissions. The default location is {@code /data/data/<packagename>/files}, but can
         * change depending on vendor implementations of Android.
         *
         * @param context an Android context.
         */
        public Builder(Context context) {
            if (context == null) {
                throw new IllegalArgumentException("A non-null Context must be provided");
            }
            RealmCore.loadLibrary(context);
            initializeBuilder(context.getFilesDir());
        }

        /**
         * Creates an instance of the Builder for the RealmConfiguration.
         * <p>
         * The Realm file will be saved in the provided folder, and it might require additional permissions.
         *
         * @param folder the folder to save Realm file in. Folder must be writable.
         * @throws IllegalArgumentException if folder doesn't exist or isn't writable.
         */
        public Builder(Context context, File folder) {
            if (context == null) {
                throw new IllegalArgumentException("A non-null Context must be provided");
            }
            RealmCore.loadLibrary(context);
            initializeBuilder(folder);
        }

        // Setup builder in its initial state
        private void initializeBuilder(File folder) {
            if (folder == null || !folder.isDirectory()) {
                throw new IllegalArgumentException(("An existing folder must be provided. " +
                        "Yours was " + (folder != null ? folder.getAbsolutePath() : "null")));
            }
            if (!folder.canWrite()) {
                throw new IllegalArgumentException("Folder is not writable: " + folder.getAbsolutePath());
            }

            this.folder = folder;
            this.fileName = Realm.DEFAULT_REALM_NAME;
            this.key = null;
            this.schemaVersion = 0;
            this.migration = null;
            this.deleteRealmIfMigrationNeeded = false;
            this.durability = SharedGroup.Durability.FULL;
            if (DEFAULT_MODULE != null) {
                this.modules.add(DEFAULT_MODULE);
            }
        }

        /**
         * Sets the filename for the Realm.
         */
        public Builder name(String filename) {
            if (filename == null || filename.isEmpty()) {
                throw new IllegalArgumentException("A non-empty filename must be provided");
            }

            this.fileName = filename;
            return this;
        }

        /**
         * Sets the 64 bit key used to encrypt and decrypt the Realm file.
         */
        public Builder encryptionKey(byte[] key) {
            if (key == null) {
                throw new IllegalArgumentException("A non-null key must be provided");
            }
            if (key.length != KEY_LENGTH) {
                throw new IllegalArgumentException(String.format("The provided key must be %s bytes. Yours was: %s",
                        KEY_LENGTH, key.length));
            }
            this.key = Arrays.copyOf(key, key.length);
            return this;
        }

        /**
         * Sets the schema version of the Realm. This must be equal to or higher than the schema version of the existing
         * Realm file, if any. If the schema version is higher than the already existing Realm, a migration is needed.
         * <p>
         * If no migration code is provided, Realm will throw a
         * {@link io.realm.exceptions.RealmMigrationNeededException}.
         *
         * @see #migration(RealmMigration)
         */
        public Builder schemaVersion(long schemaVersion) {
            if (schemaVersion < 0) {
                throw new IllegalArgumentException("Realm schema version numbers must be 0 (zero) or higher. Yours was: " + schemaVersion);
            }
            this.schemaVersion = schemaVersion;
            return this;
        }

        /**
         * Sets the {@link io.realm.RealmMigration} to be run if a migration is needed. If this migration fails to
         * upgrade the on-disc schema to the runtime schema, a {@link io.realm.exceptions.RealmMigrationNeededException}
         * will be thrown.
         */
        public Builder migration(RealmMigration migration) {
            if (migration == null) {
                throw new IllegalArgumentException("A non-null migration must be provided");
            }
            this.migration = migration;
            return this;
        }

        /**
         * Setting this will change the behavior of how migration exceptions are handled. Instead of throwing a
         * {@link io.realm.exceptions.RealmMigrationNeededException} the on-disc Realm will be cleared and recreated
         * with the new Realm schema.
         *
         * <b>WARNING!</b> This will result in loss of data.
         */
        public Builder deleteRealmIfMigrationNeeded() {
            this.deleteRealmIfMigrationNeeded = true;
            return this;
        }

        /**
         * Setting this will create an in-memory Realm instead of saving it to disk. In-memory Realms might still use
         * disk space if memory is running low, but all files created by an in-memory Realm will be deleted when the
         * Realm is closed.
         * <p>
         * Note that because in-memory Realms are not persisted, you must be sure to hold on to at least one non-closed
         * reference to the in-memory Realm object with the specific name as long as you want the data to last.
         */
        public Builder inMemory() {
            if (!TextUtils.isEmpty(assetFilePath)) {
                throw new RealmException("Realm can not use in-memory configuration if asset file is present.");
            }

            this.durability = SharedGroup.Durability.MEM_ONLY;

            return this;
        }

        /**
         * Replaces the existing module(s) with one or more {@link RealmModule}s. Using this method will replace the
         * current schema for this Realm with the schema defined by the provided modules.
         * <p>
         * A reference to the default Realm module containing all Realm classes in the project (but not dependencies),
         * can be found using {@link Realm#getDefaultModule()}. Combining the schema from the app project and a library
         * dependency is thus done using the following code:
         * <p>
         * {@code builder.modules(Realm.getDefaultMode(), new MyLibraryModule()); }
         * <p>
         * @param baseModule the first Realm module (required).
         * @param additionalModules the additional Realm modules
         * @throws IllegalArgumentException if any of the modules doesn't have the {@link RealmModule} annotation.
         * @see Realm#getDefaultModule()
         */
        public Builder modules(Object baseModule, Object... additionalModules) {
            modules.clear();
            addModule(baseModule);
            if (additionalModules != null) {
                for (int i = 0; i < additionalModules.length; i++) {
                    Object module = additionalModules[i];
                    addModule(module);
                }
            }
            return this;
        }

        /**
         * Sets the {@link RxObservableFactory} used to create Rx Observables from Realm objects.
         * The default factory is {@link RealmObservableFactory}.
         *
         * @param factory factory to use.
         */
        public Builder rxFactory(RxObservableFactory factory) {
            rxFactory = factory;
            return this;
        }

        /**
         * Sets the initial data in {@link io.realm.Realm}. This transaction will be executed only for the first time
         * when database file is created or while migrating the data when {@link Builder#deleteRealmIfMigrationNeeded()} is set.
         *
         * @param transaction transaction to execute.
         */
        public Builder initialData(Realm.Transaction transaction) {
            initialDataTransaction = transaction;
            return this;
        }

        /**
         * Copies the Realm file from the given asset file path.
         * <p>
         * When opening the Realm for the first time, instead of creating an empty file,
         * the Realm file will be copied from the provided assets file and used instead.
         * <p>
         * WARNING: This could potentially be a lengthy operation and should ideally be done on a background thread.
         *
         * @param context Android application context.
         * @param assetFile path to the asset database file.
         */
        public Builder assetFile(Context context, final String assetFile) {
            if (context == null) {
                throw new IllegalArgumentException("A non-null Context must be provided");
            }
            if (TextUtils.isEmpty(assetFile)) {
                throw new IllegalArgumentException("A non-empty asset file path must be provided");
            }
            if (durability == SharedGroup.Durability.MEM_ONLY) {
                throw new RealmException("Realm can not use in-memory configuration if asset file is present.");
            }

            this.contextWeakRef = new WeakReference<>(context);
            this.assetFilePath = assetFile;

            return this;
        }

        private void addModule(Object module) {
            if (module != null) {
                checkModule(module);
                modules.add(module);
            }
        }

        /**
         * DEBUG method. This restricts the Realm schema to only consist of the provided classes without having to
         * create a module. These classes must be available in the default module. Calling this will remove any
         * previously configured modules.
         */
        Builder schema(Class<? extends RealmModel> firstClass, Class<? extends RealmModel>... additionalClasses) {
            if (firstClass == null) {
                throw new IllegalArgumentException("A non-null class must be provided");
            }
            modules.clear();
            modules.add(DEFAULT_MODULE_MEDIATOR);
            debugSchema.add(firstClass);
            if (additionalClasses != null) {
                Collections.addAll(debugSchema, additionalClasses);
            }

            return this;
        }

        /**
         * Creates the RealmConfiguration based on the builder parameters.
         *
         * @return the created {@link RealmConfiguration}.
         */
        public RealmConfiguration build() {
            if (rxFactory == null && isRxJavaAvailable()) {
                rxFactory = new RealmObservableFactory();
            }
            return new RealmConfiguration(this);
        }

        private void checkModule(Object module) {
            if (!module.getClass().isAnnotationPresent(RealmModule.class)) {
                throw new IllegalArgumentException(module.getClass().getCanonicalName() + " is not a RealmModule. " +
                        "Add @RealmModule to the class definition.");
            }
        }
    }
}
