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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import io.realm.annotations.RealmModule;
import io.realm.exceptions.RealmException;
import io.realm.internal.RealmProxyMediator;
import io.realm.internal.SharedGroup;
import io.realm.internal.modules.CompositeMediator;
import io.realm.internal.modules.FilterableMediator;

/**
 * A RealmConfiguration is used to setup a specific Realm instance.
 *
 * Instances of a RealmConfiguration can only created by using the {@link io.realm.RealmConfiguration.Builder} and calling
 * its {@link io.realm.RealmConfiguration.Builder#build()} method.
 *
 * A commonly used RealmConfiguration can easily be accessed by first saving it as
 * {@link Realm#setDefaultConfiguration(RealmConfiguration)} and then using {@link io.realm.Realm#getDefaultInstance()}.
 *
 * A minimal configuration can be created using:
 *
 * {@code RealmConfiguration config = new RealmConfiguration.Builder(getContext()).build())}
 *
 * This will create a RealmConfiguration with the following properties
 * - Realm file is called "default.realm"
 * - It is saved in Context.getFilesDir()
 * - It has its schema version set to 0.
 */
public class RealmConfiguration {

    public static final int KEY_LENGTH = 64;

    private static final Object DEFAULT_MODULE;
    private static final RealmProxyMediator DEFAULT_MODULE_MEDIATOR;
    static {
        DEFAULT_MODULE = Realm.getDefaultModule();
        if (DEFAULT_MODULE != null) {
            DEFAULT_MODULE_MEDIATOR = getModuleMediator(DEFAULT_MODULE.getClass().getCanonicalName());
        } else {
            DEFAULT_MODULE_MEDIATOR = null;
        }
    }

    private final File realmFolder;
    private final String realmFileName;
    private final String canonicalPath;
    private final byte[] key;
    private final long schemaVersion;
    private final RealmMigration migration;
    private final boolean deleteRealmIfMigrationNeeded;
    private final SharedGroup.Durability durability;
    private final RealmProxyMediator schemaMediator;

    private RealmConfiguration(Builder builder) {
        this.realmFolder = builder.folder;
        this.realmFileName = builder.fileName;
        this.canonicalPath = Realm.getCanonicalPath(new File(realmFolder, realmFileName));
        this.key = builder.key;
        this.schemaVersion = builder.schemaVersion;
        this.deleteRealmIfMigrationNeeded = builder.deleteRealmIfMigrationNeeded;
        this.migration = builder.migration;
        this.durability = builder.durability;
        this.schemaMediator = createSchemaMediator(builder);
    }

    public File getRealmFolder() {
        return realmFolder;
    }

    public String getRealmFileName() {
        return realmFileName;
    }

    public byte[] getEncryptionKey() {
        return key;
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

    public RealmProxyMediator getSchemaMediator() {
        return schemaMediator;
    }

    public String getPath() {
        return canonicalPath;
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

        return result;
    }

    // Creates the mediator that defines the current schema
    private RealmProxyMediator createSchemaMediator(Builder builder) {

        Set<Object> modules = builder.modules;
        Set<Class<? extends RealmObject>> debugSchema = builder.debugSchema;

        // If using debug schema, use special mediator
        if (debugSchema.size() > 0) {
            return new FilterableMediator(DEFAULT_MODULE_MEDIATOR, debugSchema);
        }

        // If only one module, use that mediator directly
        if (modules.size() == 1) {
            return getModuleMediator(modules.iterator().next().getClass().getCanonicalName());
        }

        // Otherwise combine all mediators
        CompositeMediator mediator = new CompositeMediator();
        for (Object module : modules) {
            mediator.addMediator(getModuleMediator(module.getClass().getCanonicalName()));
        }
        return mediator;
    }

    // Finds the mediator associated with a given module
    private static RealmProxyMediator getModuleMediator(String fullyQualifiedModuleClassName) {
        String[] moduleNameParts = fullyQualifiedModuleClassName.split("\\.");
        String moduleSimpleName = moduleNameParts[moduleNameParts.length - 1];
        String mediatorName = String.format("io.realm.%s%s", moduleSimpleName, "Mediator");
        Class<?> clazz;
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

    /**
     * RealmConfiguration.Builder used to construct instances of a RealmConfiguration in a fluent manner.
     */
    public static class Builder {
        private File folder;
        private String fileName;
        private byte[] key;
        private long schemaVersion;
        private RealmMigration migration;
        private boolean deleteRealmIfMigrationNeeded;
        private SharedGroup.Durability durability;
        private HashSet<Object> modules = new HashSet<Object>();
        private HashSet<Class<? extends RealmObject>> debugSchema = new HashSet<Class<? extends RealmObject>>();

        /**
         * Creates an instance of the Builder for the RealmConfiguration.
         * The Realm file will be saved in the provided folder.
         *
         * @param folder Folder to save Realm file in. Folder must be writable.
         *
         * @throws IllegalArgumentException if folder doesn't exists or isn't writable.
         */
        public Builder(File folder) {
            initializeBuilder(folder);
        }

        /**
         * Creates an instance of the Builder for the RealmConfiguration.
         *
         * This will use the apps own internal directory for storing the Realm file. This does not require any
         * additional permissions. The default location is {@code /data/data/<packagename>/files}, but can
         * change depending on vendor implementations of Android.
         *
         * @param context Android context.
         */
        public Builder(Context context) {
            if (context == null) {
                throw new IllegalArgumentException("A non-null Context must be provided");
            }
            initializeBuilder(context.getFilesDir());
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
            this.key = key;
            return this;
        }

        /**
         * Sets the schema version of the Realm. This must be equal to or higher than the schema version of the existing
         * Realm file, if any. If the schema version is higher than the already existing Realm, a migration is needed.
         *
         * If no migration code is provided, Realm will throw a {@link io.realm.exceptions.RealmMigrationNeededException}.
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
         * upgrade the on-disc schema to the runtime schema, a
         * {@link io.realm.exceptions.RealmMigrationNeededException} will be thrown.
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
            this.durability = SharedGroup.Durability.MEM_ONLY;
            return this;
        }

        /**
         * Replaces the existing module(s) with one or more {@link RealmModule}s. Using this method will replace the
         * current schema for this Realm with the schema defined by the provided modules.
         *
         * A reference to the default Realm module containing all Realm classes in the project (but not dependencies),
         * can be found using {@link Realm#getDefaultModule()}. Combining the schema from the app project and a library
         * dependency is thus done using the following code:
         *
         * {@code builder.setModules(Realm.getDefaultMode(), new MyLibraryModule()); }
         *
         * @param baseModule        First Realm module (required).
         * @param additionalModules Additional Realm modules
         *
         * @throws IllegalArgumentException if any of the modules doesn't have the {@link RealmModule} annotation.
         * @see Realm#getDefaultModule()
         */
        public Builder setModules(Object baseModule, Object... additionalModules) {
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
        Builder schema(Class<? extends RealmObject> firstClass, Class<? extends RealmObject>... additionalClasses) {
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
         * @return The created RealmConfiguration.
         */
        public RealmConfiguration build() {
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
