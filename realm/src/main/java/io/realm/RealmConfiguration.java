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

import android.content.Context;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import io.realm.internal.migration.SetVersionNumberMigration;

/**
 * A RealmConfiguration is used to setup a specific Realm instance.
 *
 * Instances of a RealmConfiguration can only created by using the {@link io.realm.RealmConfiguration.Builder} and calling
 * its {@link io.realm.RealmConfiguration.Builder#create()} method.
 *
 * A commonly used RealmConfiguration can be easily accessed by first saving it as
 * {@link Realm#setDefaultConfiguration(RealmConfiguration)} and then using {@link io.realm.Realm#getDefaultInstance()}.
 *
 * A minimal configuration can be created using:
 *
 * {@code RealmConfiguration config = new RealmConfiguration.Builder(getContext().create())}
 *
 * This will create a RealmConfiguration with the following properties
 * - Realm file is called "default.realm"
 * - It is saved in Context.getFilesDir()
 * - It has it's version set to 0.
 */
public class RealmConfiguration {

    private final File realmDir;
    private final String realmName;
    private final byte[] key;
    private final int version;
    private final RealmMigration migration;
    private final boolean deleteRealmIfMigrationNeeded;
    private final boolean deleteRealmBeforeOpening;
    private final Set<Class<? extends RealmObject>> schema;

    private RealmConfiguration(Builder builder) {
        this.realmDir = builder.folder;
        this.realmName = builder.fileName;
        this.key = builder.key;
        this.version = builder.version;
        this.deleteRealmIfMigrationNeeded = builder.deleteRealmIfMigrationNeeded;
        this.deleteRealmBeforeOpening = builder.deleteRealmBeforeOpening;
        this.migration = (builder.migration != null) ? builder.migration : new SetVersionNumberMigration(version);
        this.schema = builder.schema;
    }

    public File getFileDir() {
        return realmDir;
    }

    public String getFileName() {
        return realmName;
    }

    public byte[] getKey() {
        return key;
    }

    public int getVersion() {
        return version;
    }

    public RealmMigration getMigration() {
        return migration;
    }

    public boolean shouldDeleteRealmIfMigrationNeeded() {
        return deleteRealmIfMigrationNeeded;
    }

    public boolean shouldDeleteRealmBeforeOpening() {
        return deleteRealmBeforeOpening;
    }

    public Set<Class<? extends RealmObject>> getSchema() {
        return schema;
    }

    public String getAbsolutePathToRealm() {
        return new File(realmDir, realmName).getAbsolutePath();
    }

    /**
     * RealmConfiguration.Builder used to construct instances of a RealmConfiguration in a fluent manner.
     */
    public static class Builder {
        private File folder = null;
        private String fileName = "default.realm";
        private byte[] key = null;
        private int version = 0;
        private RealmMigration migration = null;
        private boolean deleteRealmIfMigrationNeeded = false;
        private boolean deleteRealmBeforeOpening = false;
        private Set<Class<? extends RealmObject>> schema = new HashSet<Class<? extends RealmObject>>();

        /**
         * Create an instance of the Builder for the RealmConfiguration.
         * The Realm file in the provided folder.
         */
        public Builder(File writeableFolder) {
            if (folder == null || !folder.isDirectory()) {
                throw new IllegalArgumentException(("An existing folder must be provided. Yours was " + (folder != null ? folder.getAbsolutePath() : "null")));
            }
            this.folder = writeableFolder;
        }

        /**
         * Create an instance of the Builder for the RealmConfiguration.
         * This will use the Apps own internal directory for storing the Realm file. This does not require any
         * additional permissions. The default location is {@code /data/data/<packagename>/files}, but can
         * change depending on vendor implementations of Android.
         */
        public Builder(Context context) {
            if (context == null) {
                throw new IllegalArgumentException("A non-null Context must be provided");
            }
            this.folder = context.getFilesDir();
        }

        /**
         * Set the filename for the Realm.
         */
        public Builder name(String filename) {
            if (filename == null || filename.isEmpty()) {
                throw new IllegalArgumentException("A non-empty filename must be provided");
            }

            this.fileName = filename;
            return this;
        }

        /**
         * Set the 64 bit key used to encrypt and decrypt the Realm file.
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
         * Set the version of the Realm. This must be equal to or higher than the version of any existing Realm file.
         * If the version is higher than an already existing Realm, a migration is needed.
         *
         * If no migration code is provided, Realm will compare the on-disc schema of the Realm with the
         * {@link io.realm.RealmObject}'s defined.
         *
         * - If they match, the version number will automatically be increased to the new version.
         * - If not, a {@link io.realm.exceptions.RealmMigrationNeededException} will be thrown. This behavior can be
         *   overridden by using {@link #deleteRealmIfMigrationNeeded()}.
         *
         * @see #migration(RealmMigration)
         */
        public Builder version(int version) {
            if (version < 0) {
                throw new IllegalArgumentException("Realm version numbers must be 0 (zero) or higher. Yours was: " + version);
            }
            this.version = version;
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
         * Setting this will change the behavior of migrations. If a
         * {@link io.realm.exceptions.RealmMigrationNeededException} should be thrown, instead the on-disc
         * Realm will be cleared and recreated with the new Realm schema.
         *
         * <bold>WARNING!</bold> This will result in loss of data.
         */
        public Builder deleteRealmIfMigrationNeeded() {
            this.deleteRealmIfMigrationNeeded = true;
            return this;
        }

        /**
         * Setting this will cause any previous existing Realm file on the disc to be deleted before a new instance is
         * opened. As Realm instances are reference counted, the Realm file will only be deleted if the reference count
         * is zero, ie. the first time {@link io.realm.Realm#getInstance(RealmConfiguration)} is called when starting
         * the app or after all instances has been closed using {@link Realm#close()} and then reopening the Realm.
         *
         * <bold>WARNING!</bold> This will result in loss of data.
         *
         * @see {@link io.realm.Realm}
         */
        public Builder deleteRealmBeforeOpening() {
            this.deleteRealmBeforeOpening = true;
            return this;
        }

        /**
         * Package private method. Only available for testing until Migrations introduces RealmModules. This restricts
         * the Realm schema to only consist of the provided classes.
         */
        Builder schema(Class<? extends RealmObject>... schemaClass) {
            schema = new HashSet<Class<? extends RealmObject>>();
            if (schemaClass != null) {
                Collections.addAll(schema, schemaClass);
            }
            return this;
        }

        /**
         * Creates the RealmConfiguration based on the builder parameters.
         * @return The created RealmConfiguration.
         */
        public RealmConfiguration create() {
            return new RealmConfiguration(this);
        }
    }
}
