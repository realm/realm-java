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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import io.realm.annotations.RealmModule;
import io.realm.exceptions.RealmException;
import io.realm.internal.RealmProxyMediator;
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

    private static final Object DEFAULT_MODULE;
    private static final RealmProxyMediator DEFAULT_MODULE_MEDIATOR;
    static {
        DEFAULT_MODULE = getDefaultModule();
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
    private final int schemaVersion;
    private final RealmMigration migration;
    private final boolean deleteRealmIfMigrationNeeded;
    private final RealmProxyMediator schemaMediator;

    private RealmConfiguration(Builder builder) {
        this.realmFolder = builder.folder;
        this.realmFileName = builder.fileName;
        this.canonicalPath = Realm.getCanonicalPath(new File(realmFolder, realmFileName));
        this.key = builder.key;
        this.schemaVersion = builder.schemaVersion;
        this.deleteRealmIfMigrationNeeded = builder.deleteRealmIfMigrationNeeded;
        this.migration = builder.migration;
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

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public RealmMigration getMigration() {
        return migration;
    }

    public boolean shouldDeleteRealmIfMigrationNeeded() {
        return deleteRealmIfMigrationNeeded;
    }

    public RealmProxyMediator getSchemaMediator() {
        return schemaMediator;
    }

    public String getPath() {
        return canonicalPath;
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

    // Finds the default module (if there is one)
    private static Object getDefaultModule() {
        String moduleName = "io.realm.DefaultRealmModule";
        Class<?> clazz;
        try {
            clazz = Class.forName(moduleName);
            Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ClassNotFoundException e) {
            return null;
        } catch (InvocationTargetException e) {
            throw new RealmException("Could not create an instance of " + moduleName, e);
        } catch (InstantiationException e) {
            throw new RealmException("Could not create an instance of " + moduleName, e);
        } catch (IllegalAccessException e) {
            throw new RealmException("Could not create an instance of " + moduleName, e);
        }
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
        private int schemaVersion;
        private RealmMigration migration;
        private boolean deleteRealmIfMigrationNeeded;
        private boolean resetRealmBeforeOpening;
        private HashSet<Object> modules = new HashSet<Object>();
        private HashSet<Class<? extends RealmObject>> debugSchema = new HashSet<Class<? extends RealmObject>>();

        /**
         * Creates an instance of the Builder for the RealmConfiguration.
         * The Realm file will be saved in the provided folder.
         *
         * @param folder Folder to save Realm file in. Folder must be writable.
         *
         * @throws {@link IllegalArgumentException} if folder doesn't exists or isn't writable.
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
            this.resetRealmBeforeOpening = false;
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
            if (key.length != 64) {
                throw new IllegalArgumentException("The provided key must be 64 bytes. Yours was: " + key.length);
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
        public Builder schemaVersion(int schemaVersion) {
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
         * <bold>WARNING!</bold> This will result in loss of data.
         */
        public Builder deleteRealmIfMigrationNeeded() {
            this.deleteRealmIfMigrationNeeded = true;
            return this;
        }

        /**
         * Add a {@link RealmModule}s to the existing modules. RealmClasses in the new module is added to the schema
         * for this Realm.
         *
         * @param module {@link RealmModule} to add to this Realms schema.
         *
         * @throws {@link IllegalArgumentException} if module is {@code null} or doesn't have the {@link RealmModule}
         * annotation.
         */
        public Builder addModule(Object module) {
            checkModule(module);
            modules.add(module);
            return this;
        }

        /**
         * Replace the existing module(s) with one or more {@link RealmModule}s. Using this method will replace the
         * current schema for this Realm with the schema defined by the provided modules.
         *
         * @param baseModule
         * @param additionalModules
         *
         * @throws {@link IllegalArgumentException} if any of the modules are {@code null} or doesn't have the
         * {@link RealmModule} annotation.
         */
        public Builder setModules(Object baseModule, Object... additionalModules) {
            modules.clear();
            addModule(baseModule);
            if (additionalModules != null) {
                for (int i = 0; i < additionalModules.length; i++) {
                    Object module = additionalModules[i];
                    checkModule(module);
                    addModule(module);
                }
            }
            return this;
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
            if (module == null) {
                throw new IllegalArgumentException("Provided RealmModule must not be null.");
            }
            if (!module.getClass().isAnnotationPresent(RealmModule.class)) {
                throw new IllegalArgumentException(module.getClass().getCanonicalName() + " is not a RealmModule. " +
                        "Add @RealmModule to the class definition.");
            }
        }
    }
}
