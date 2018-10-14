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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.annotation.Nullable;

import io.realm.annotations.RealmModule;
import io.realm.exceptions.RealmException;
import io.realm.exceptions.RealmFileException;
import io.realm.internal.OsRealmConfig;
import io.realm.internal.RealmCore;
import io.realm.internal.RealmProxyMediator;
import io.realm.internal.Util;
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
 * {@code RealmConfiguration config = new RealmConfiguration.Builder().build()}
 * <p>
 * This will create a RealmConfiguration with the following properties.
 * <ul>
 * <li>Realm file is called "default.realm"</li>
 * <li>It is saved in Context.getFilesDir()</li>
 * <li>It has its schema version set to 0.</li>
 * </ul>
 */
public class RealmConfiguration {

    public static final String DEFAULT_REALM_NAME = "default.realm";
    public static final int KEY_LENGTH = 64;

    private static final Object DEFAULT_MODULE;
    protected static final RealmProxyMediator DEFAULT_MODULE_MEDIATOR;
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

    private final File realmDirectory;
    private final String realmFileName;
    private final String canonicalPath;
    private final String assetFilePath;
    private final byte[] key;
    private final long schemaVersion;
    private final RealmMigration migration;
    private final boolean deleteRealmIfMigrationNeeded;
    private final OsRealmConfig.Durability durability;
    private final RealmProxyMediator schemaMediator;
    private final RxObservableFactory rxObservableFactory;
    private final Realm.Transaction initialDataTransaction;
    private final boolean readOnly;
    private final CompactOnLaunchCallback compactOnLaunch;
    /**
     * Whether this RealmConfiguration is intended to open a
     * recovery Realm produced after an offline/online client reset.
     */
    private final boolean isRecoveryConfiguration;

    // We need to enumerate all parameters since SyncConfiguration and RealmConfiguration supports different
    // subsets of them.
    protected RealmConfiguration(@Nullable File realmDirectory,
            @Nullable String realmFileName,
            String canonicalPath,
            @Nullable String assetFilePath,
            @Nullable byte[] key,
            long schemaVersion,
            @Nullable RealmMigration migration,
            boolean deleteRealmIfMigrationNeeded,
            OsRealmConfig.Durability durability,
            RealmProxyMediator schemaMediator,
            @Nullable RxObservableFactory rxObservableFactory,
            @Nullable Realm.Transaction initialDataTransaction,
            boolean readOnly,
            @Nullable CompactOnLaunchCallback compactOnLaunch,
            boolean isRecoveryConfiguration) {
        this.realmDirectory = realmDirectory;
        this.realmFileName = realmFileName;
        this.canonicalPath = canonicalPath;
        this.assetFilePath = assetFilePath;
        this.key = key;
        this.schemaVersion = schemaVersion;
        this.migration = migration;
        this.deleteRealmIfMigrationNeeded = deleteRealmIfMigrationNeeded;
        this.durability = durability;
        this.schemaMediator = schemaMediator;
        this.rxObservableFactory = rxObservableFactory;
        this.initialDataTransaction = initialDataTransaction;
        this.readOnly = readOnly;
        this.compactOnLaunch = compactOnLaunch;
        this.isRecoveryConfiguration = isRecoveryConfiguration;
    }

    public File getRealmDirectory() {
        return realmDirectory;
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

    public OsRealmConfig.Durability getDurability() {
        return durability;
    }

    /**
     * Returns the mediator instance of schema which is defined by this configuration.
     *
     * @return the mediator of the schema.
     */
    // Protected for testing with mockito.
    protected RealmProxyMediator getSchemaMediator() {
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
        return !Util.isEmptyString(assetFilePath);
    }

    /**
     * Returns the path to the Realm asset file.
     *
     * @return path to the asset file relative to the asset directory.
     */
    String getAssetFilePath() {
        return assetFilePath;
    }

    /**
     * Returns a callback to determine if the Realm file should be compacted before being returned to the user.
     *
     * @return a callback called when opening a Realm for the first time during the life of a process to determine if
     * it should be compacted before being returned to the user. It is passed the total file size (data + free space)
     * and the total bytes used by data in the file.
     */
    public CompactOnLaunchCallback getCompactOnLaunchCallback() {
        return compactOnLaunch;
    }

    /**
     * Returns the unmodifiable {@link Set} of model classes that make up the schema for this Realm.
     *
     * @return unmodifiable {@link Set} of model classes.
     */
    public Set<Class<? extends RealmModel>> getRealmObjectClasses() {
        return schemaMediator.getModelClasses();
    }

    /**
     * Returns the absolute path to where the Realm file will be saved.
     *
     * @return the absolute path to the Realm file defined by this configuration.
     */
    public String getPath() {
        return canonicalPath;
    }

    /**
     * Checks if the Realm file defined by this configuration already exists.
     * <p>
     * WARNING: This method is just a point-in-time check. Unless protected by external synchronization another
     * thread or process might have created or deleted the Realm file right after this method has returned.
     *
     * @return {@code true} if the Realm file exists, {@code false} otherwise.
     */
    boolean realmExists() {
        return new File(canonicalPath).exists();
    }

    /**
     * Returns the {@link RxObservableFactory} that is used to create Rx Observables from Realm objects.
     *
     * @return the factory instance used to create Rx Observables.
     * @throws UnsupportedOperationException if the required RxJava framework is not on the classpath.
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

    /**
     * Returns whether this Realm is read-only or not. Read-only Realms cannot be modified and will throw an
     * {@link IllegalStateException} if {@link Realm#beginTransaction()} is called on it.
     *
     * @return {@code true} if this Realm is read only, {@code false} if not.
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * @return {@code true} if this configuration is intended to open a backup Realm (as a result of a client reset).
     * @see <a href="https://realm.io/docs/java/latest/api/io/realm/ClientResetRequiredError.html">ClientResetRequiredError</a>
     */
    public boolean isRecoveryConfiguration() {
        return isRecoveryConfiguration;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) { return true; }
        if (obj == null || getClass() != obj.getClass()) { return false; }

        RealmConfiguration that = (RealmConfiguration) obj;

        if (schemaVersion != that.schemaVersion) { return false; }
        if (deleteRealmIfMigrationNeeded != that.deleteRealmIfMigrationNeeded) { return false; }
        if (readOnly != that.readOnly) { return false; }
        if (isRecoveryConfiguration != that.isRecoveryConfiguration) { return false; }
        if (realmDirectory != null ? !realmDirectory.equals(that.realmDirectory) : that.realmDirectory != null) {
            return false;
        }
        if (realmFileName != null ? !realmFileName.equals(that.realmFileName) : that.realmFileName != null) {
            return false;
        }
        if (!canonicalPath.equals(that.canonicalPath)) { return false; }
        if (assetFilePath != null ? !assetFilePath.equals(that.assetFilePath) : that.assetFilePath != null) {
            return false;
        }
        if (!Arrays.equals(key, that.key)) { return false; }
        if (migration != null ? !migration.equals(that.migration) : that.migration != null) {
            return false;
        }
        if (durability != that.durability) { return false; }
        if (!schemaMediator.equals(that.schemaMediator)) { return false; }
        if (rxObservableFactory != null ? !rxObservableFactory.equals(that.rxObservableFactory) : that.rxObservableFactory != null) {
            return false;
        }
        if (initialDataTransaction != null ? !initialDataTransaction.equals(that.initialDataTransaction) : that.initialDataTransaction != null) {
            return false;
        }
        return compactOnLaunch != null ? compactOnLaunch.equals(that.compactOnLaunch) : that.compactOnLaunch == null;
    }

    @Override
    public int hashCode() {
        int result = realmDirectory != null ? realmDirectory.hashCode() : 0;
        result = 31 * result + (realmFileName != null ? realmFileName.hashCode() : 0);
        result = 31 * result + canonicalPath.hashCode();
        result = 31 * result + (assetFilePath != null ? assetFilePath.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(key);
        result = 31 * result + (int) (schemaVersion ^ (schemaVersion >>> 32));
        result = 31 * result + (migration != null ? migration.hashCode() : 0);
        result = 31 * result + (deleteRealmIfMigrationNeeded ? 1 : 0);
        result = 31 * result + durability.hashCode();
        result = 31 * result + schemaMediator.hashCode();
        result = 31 * result + (rxObservableFactory != null ? rxObservableFactory.hashCode() : 0);
        result = 31 * result + (initialDataTransaction != null ? initialDataTransaction.hashCode() : 0);
        result = 31 * result + (readOnly ? 1 : 0);
        result = 31 * result + (compactOnLaunch != null ? compactOnLaunch.hashCode() : 0);
        result = 31 * result + (isRecoveryConfiguration ? 1 : 0);
        return result;
    }

    // Creates the mediator that defines the current schema.
    protected static RealmProxyMediator createSchemaMediator(Set<Object> modules,
            Set<Class<? extends RealmModel>> debugSchema) {

        // If using debug schema, uses special mediator.
        if (debugSchema.size() > 0) {
            return new FilterableMediator(DEFAULT_MODULE_MEDIATOR, debugSchema);
        }

        // If only one module, uses that mediator directly.
        if (modules.size() == 1) {
            return getModuleMediator(modules.iterator().next().getClass().getCanonicalName());
        }

        // Otherwise combines all mediators.
        RealmProxyMediator[] mediators = new RealmProxyMediator[modules.size()];
        int i = 0;
        for (Object module : modules) {
            mediators[i] = getModuleMediator(module.getClass().getCanonicalName());
            i++;
        }
        return new CompositeMediator(mediators);
    }

    // Finds the mediator associated with a given module.
    private static RealmProxyMediator getModuleMediator(String fullyQualifiedModuleClassName) {
        String[] moduleNameParts = fullyQualifiedModuleClassName.split("\\.");
        String moduleSimpleName = moduleNameParts[moduleNameParts.length - 1];
        String mediatorName = String.format(Locale.US, "io.realm.%s%s", moduleSimpleName, "Mediator");
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
        stringBuilder.append("realmDirectory: ").append(realmDirectory != null ? realmDirectory.toString() : "");
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
        stringBuilder.append("\n");
        stringBuilder.append("readOnly: ").append(readOnly);
        stringBuilder.append("\n");
        stringBuilder.append("compactOnLaunch: ").append(compactOnLaunch);

        return stringBuilder.toString();
    }

    /**
     * Checks if RxJava is can be loaded.
     *
     * @return {@code true} if RxJava dependency exist, {@code false} otherwise.
     */
    @SuppressWarnings("LiteralClassName")
    static synchronized boolean isRxJavaAvailable() {
        if (rxJavaAvailable == null) {
            try {
                Class.forName("io.reactivex.Flowable");
                rxJavaAvailable = true;
            } catch (ClassNotFoundException ignore) {
                rxJavaAvailable = false;
            }
        }
        return rxJavaAvailable;
    }

    // Gets the canonical path for a given file.
    protected static String getCanonicalPath(File realmFile) {
        try {
            return realmFile.getCanonicalPath();
        } catch (IOException e) {
            throw new RealmFileException(RealmFileException.Kind.ACCESS_ERROR,
                    "Could not resolve the canonical path to the Realm file: " + realmFile.getAbsolutePath(),
                    e);
        }
    }

    // Checks if this configuration is a SyncConfiguration instance.
    boolean isSyncConfiguration() {
        return false;
    }

    /**
     * RealmConfiguration.Builder used to construct instances of a RealmConfiguration in a fluent manner.
     */
    public static class Builder {
        // IMPORTANT: When adding any new methods to this class also add them to SyncConfiguration.
        private File directory;
        private String fileName;
        private String assetFilePath;
        private byte[] key;
        private long schemaVersion;
        private RealmMigration migration;
        private boolean deleteRealmIfMigrationNeeded;
        private OsRealmConfig.Durability durability;
        private HashSet<Object> modules = new HashSet<Object>();
        private HashSet<Class<? extends RealmModel>> debugSchema = new HashSet<Class<? extends RealmModel>>();
        private RxObservableFactory rxFactory;
        private Realm.Transaction initialDataTransaction;
        private boolean readOnly;
        private CompactOnLaunchCallback compactOnLaunch;

        /**
         * Creates an instance of the Builder for the RealmConfiguration.
         * <p>
         * This will use the app's own internal directory for storing the Realm file. This does not require any
         * additional permissions. The default location is {@code /data/data/<packagename>/files}, but can
         * change depending on vendor implementations of Android.
         */
        public Builder() {
            this(BaseRealm.applicationContext);
        }

        Builder(Context context) {
            //noinspection ConstantConditions
            if (context == null) {
                throw new IllegalStateException("Call `Realm.init(Context)` before creating a RealmConfiguration");
            }
            RealmCore.loadLibrary(context);
            initializeBuilder(context);
        }

        // Setups builder in its initial state.
        private void initializeBuilder(Context context) {
            this.directory = context.getFilesDir();
            this.fileName = Realm.DEFAULT_REALM_NAME;
            this.key = null;
            this.schemaVersion = 0;
            this.migration = null;
            this.deleteRealmIfMigrationNeeded = false;
            this.durability = OsRealmConfig.Durability.FULL;
            this.readOnly = false;
            this.compactOnLaunch = null;
            if (DEFAULT_MODULE != null) {
                this.modules.add(DEFAULT_MODULE);
            }
        }

        /**
         * Sets the filename for the Realm file.
         */
        public Builder name(String filename) {
            //noinspection ConstantConditions
            if (filename == null || filename.isEmpty()) {
                throw new IllegalArgumentException("A non-empty filename must be provided");
            }

            this.fileName = filename;
            return this;
        }

        /**
         * Specifies the directory where the Realm file will be saved. The default value is {@code context.getFilesDir()}.
         * If the directory does not exist, it will be created.
         *
         * @param directory the directory to save the Realm file in. Directory must be writable.
         * @throws IllegalArgumentException if {@code directory} is null, not writable or a file.
         */
        public Builder directory(File directory) {
            //noinspection ConstantConditions
            if (directory == null) {
                throw new IllegalArgumentException("Non-null 'dir' required.");
            }
            if (directory.isFile()) {
                throw new IllegalArgumentException("'dir' is a file, not a directory: " + directory.getAbsolutePath() + ".");
            }
            if (!directory.exists() && !directory.mkdirs()) {
                throw new IllegalArgumentException("Could not create the specified directory: " + directory.getAbsolutePath() + ".");
            }
            if (!directory.canWrite()) {
                throw new IllegalArgumentException("Realm directory is not writable: " + directory.getAbsolutePath() + ".");
            }
            this.directory = directory;
            return this;
        }

        /**
         * Sets the 64 byte key used to encrypt and decrypt the Realm file.
         * Sets the {@value io.realm.RealmConfiguration#KEY_LENGTH} bytes key used to encrypt and decrypt the Realm file.
         */
        public Builder encryptionKey(byte[] key) {
            //noinspection ConstantConditions
            if (key == null) {
                throw new IllegalArgumentException("A non-null key must be provided");
            }
            if (key.length != KEY_LENGTH) {
                throw new IllegalArgumentException(String.format(Locale.US,
                        "The provided key must be %s bytes. Yours was: %s",
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
            //noinspection ConstantConditions
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
         * <p>
         * <p>This cannot be configured to have an asset file at the same time by calling
         * {@link #assetFile(String)} as the provided asset file will be deleted in migrations.
         * <p>
         * <p><b>WARNING!</b> This will result in loss of data.
         *
         * @throws IllegalStateException if configured to use an asset file by calling {@link #assetFile(String)} previously.
         */
        public Builder deleteRealmIfMigrationNeeded() {
            if (this.assetFilePath != null && this.assetFilePath.length() != 0) {
                throw new IllegalStateException("Realm cannot clear its schema when previously configured to use an asset file by calling assetFile().");
            }

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
            if (!Util.isEmptyString(assetFilePath)) {
                throw new RealmException("Realm can not use in-memory configuration if asset file is present.");
            }

            this.durability = OsRealmConfig.Durability.MEM_ONLY;

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
         *
         * @param baseModule the first Realm module (required).
         * @param additionalModules the additional Realm modules
         * @throws IllegalArgumentException if any of the modules doesn't have the {@link RealmModule} annotation.
         * @see Realm#getDefaultModule()
         */
        public Builder modules(Object baseModule, Object... additionalModules) {
            modules.clear();
            addModule(baseModule);
            //noinspection ConstantConditions
            if (additionalModules != null) {
                for (int i = 0; i < additionalModules.length; i++) {
                    Object module = additionalModules[i];
                    addModule(module);
                }
            }
            return this;
        }

        /**
         * FIXME: Temporary visible
         * DEBUG method. Will add a module unconditionally.
         *
         * Adds a module to already defined modules.
         */
        public final Builder addModule(Object module) {
            //noinspection ConstantConditions
            if (module != null) {
                checkModule(module);
                modules.add(module);
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
         * the Realm file will be copied from the provided asset file and used instead.
         * <p>
         * This cannot be combined with {@link #deleteRealmIfMigrationNeeded()} as doing so would just result in the
         * copied file being deleted.
         * <p>
         * WARNING: This could potentially be a lengthy operation and should ideally be done on a background thread.
         *
         * @param assetFile path to the asset database file.
         * @throws IllegalStateException if this is configured to clear its schema by calling {@link #deleteRealmIfMigrationNeeded()}.
         */
        public Builder assetFile(String assetFile) {
            if (Util.isEmptyString(assetFile)) {
                throw new IllegalArgumentException("A non-empty asset file path must be provided");
            }
            if (durability == OsRealmConfig.Durability.MEM_ONLY) {
                throw new RealmException("Realm can not use in-memory configuration if asset file is present.");
            }
            if (this.deleteRealmIfMigrationNeeded) {
                throw new IllegalStateException("Realm cannot use an asset file when previously configured to clear its schema in migration by calling deleteRealmIfMigrationNeeded().");
            }
            this.assetFilePath = assetFile;

            return this;
        }

        /**
         * Setting this will cause the Realm to become read only and all write transactions made against this Realm will
         * fail with an {@link IllegalStateException}.
         * <p>
         * This in particular mean that {@link #initialData(Realm.Transaction)} will not work in combination with a
         * read only Realm and setting this will result in a {@link IllegalStateException} being thrown.
         * </p>
         * Marking a Realm as read only only applies to the Realm in this process. Other processes can still
         * write to the Realm.
         */
        public Builder readOnly() {
            this.readOnly = true;
            return this;
        }

        /**
         * Setting this will cause Realm to compact the Realm file if the Realm file has grown too large and a
         * significant amount of space can be recovered. See {@link DefaultCompactOnLaunchCallback} for details.
         */
        public Builder compactOnLaunch() {
            return compactOnLaunch(new DefaultCompactOnLaunchCallback());
        }

        /**
         * Sets this to determine if the Realm file should be compacted before returned to the user. It is passed the
         * total file size (data + free space) and the bytes used by data in the file.
         *
         * @param compactOnLaunch a callback called when opening a Realm for the first time during the life of a process
         *                        to determine if it should be compacted before being returned to the user. It is passed
         *                        the total file size (data + free space) and the bytes used by data in the file.
         */
        public Builder compactOnLaunch(CompactOnLaunchCallback compactOnLaunch) {
            //noinspection ConstantConditions
            if (compactOnLaunch == null) {
                throw new IllegalArgumentException("A non-null compactOnLaunch must be provided");
            }
            this.compactOnLaunch = compactOnLaunch;
            return this;
        }

        /**
         * DEBUG method. This restricts the Realm schema to only consist of the provided classes without having to
         * create a module. These classes must be available in the default module. Calling this will remove any
         * previously configured modules.
         */
        final Builder schema(Class<? extends RealmModel> firstClass, Class<? extends RealmModel>... additionalClasses) {
            //noinspection ConstantConditions
            if (firstClass == null) {
                throw new IllegalArgumentException("A non-null class must be provided");
            }
            modules.clear();
            modules.add(DEFAULT_MODULE_MEDIATOR);
            debugSchema.add(firstClass);
            //noinspection ConstantConditions
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
            // Check that readOnly() was applied to legal configuration. Right now it should only be allowed if
            // an assetFile is configured
            if (readOnly) {
                if (initialDataTransaction != null) {
                    throw new IllegalStateException("This Realm is marked as read-only. Read-only Realms cannot use initialData(Realm.Transaction).");
                }
                if (assetFilePath == null) {
                    throw new IllegalStateException("Only Realms provided using 'assetFile(path)' can be marked read-only. No such Realm was provided.");
                }
                if (deleteRealmIfMigrationNeeded) {
                    throw new IllegalStateException("'deleteRealmIfMigrationNeeded()' and read-only Realms cannot be combined");
                }
                if (compactOnLaunch != null) {
                    throw new IllegalStateException("'compactOnLaunch()' and read-only Realms cannot be combined");
                }
            }

            if (rxFactory == null && isRxJavaAvailable()) {
                rxFactory = new RealmObservableFactory();
            }


            return new RealmConfiguration(directory,
                    fileName,
                    getCanonicalPath(new File(directory, fileName)),
                    assetFilePath,
                    key,
                    schemaVersion,
                    migration,
                    deleteRealmIfMigrationNeeded,
                    durability,
                    createSchemaMediator(modules, debugSchema),
                    rxFactory,
                    initialDataTransaction,
                    readOnly,
                    compactOnLaunch,
                    false
            );
        }

        private void checkModule(Object module) {
            if (!module.getClass().isAnnotationPresent(RealmModule.class)) {
                throw new IllegalArgumentException(module.getClass().getCanonicalName() + " is not a RealmModule. " +
                        "Add @RealmModule to the class definition.");
            }
        }
    }
}
