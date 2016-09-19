/*
 * Copyright 2016 Realm Inc.
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

package io.realm.objectserver;

import android.content.Context;

import java.io.File;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.RealmModel;
import io.realm.annotations.RealmModule;
import io.realm.internal.RealmProxyMediator;
import io.realm.internal.SharedRealm;
import io.realm.objectserver.internal.syncpolicy.AutomaticSyncPolicy;
import io.realm.objectserver.internal.syncpolicy.SyncPolicy;
import io.realm.rx.RealmObservableFactory;
import io.realm.rx.RxObservableFactory;

/**
 * An {@link SyncConfiguration} is used to setup a Realm that can be synchronized between devices using the Realm
 * Object Server.
 * <p>
 * A valid {@link User} is required to create a {@link SyncConfiguration}. See {@link Credentials} and
 * {@link User#loginAsync(Credentials, String, User.Callback)} for more information on
 * how to get a user object.
 * <p>
 * A minimal {@link SyncConfiguration} can look like this:
 * <pre>
 * {@code
 * SyncConfiguration config = new SyncConfiguration.Builder(context)
 *   .serverUrl("realm://objectserver.realm.io/~/default")
 *   .user(myUser)
 *   .build();
 * }
 * </pre>
 *
 * Synchronized Realms only support additive migrations which can be detected automatically, so the following
 * builder options are not accessible compared to a normal Realm:
 *
 * <ul>
 *     <li>{@code deleteRealmIfMigrationNeeded()}</li>
 *     <li>{@code schemaVersion(long version)}</li>
 *     <li>{@code migration(Migration)}</li>
 * </ul>
 *
 * Synchronized Realms are created by using {@link Realm#getInstance(RealmConfiguration)} and
 * {@link Realm#getDefaultInstance()} like normal unsynchronized Realms.
 */
public final class SyncConfiguration extends RealmConfiguration {

    private final URI serverUrl;
    private final User user;
    private final SyncPolicy syncPolicy;
    private final Session.ErrorHandler errorHandler;
    private final boolean deleteRealmOnLogout;

    private SyncConfiguration(File directory,
                                String filename,
                                String canonicalPath,
                                String assetFilePath,
                                byte[] key,
                                long schemaVersion,
                                RealmMigration migration,
                                boolean deleteRealmIfMigrationNeeded,
                                SharedRealm.Durability durability,
                                RealmProxyMediator schemaMediator,
                                RxObservableFactory rxFactory,
                                Realm.Transaction initialDataTransaction,
                                WeakReference<Context> context,
                                User user,
                                URI serverUrl,
                                SyncPolicy syncPolicy,
                                Session.ErrorHandler errorHandler,
                                boolean deleteRealmOnLogout
    ) {
        super(directory,
                filename,
                canonicalPath,
                assetFilePath,
                key,
                schemaVersion,
                migration,
                deleteRealmIfMigrationNeeded,
                durability,
                schemaMediator,
                rxFactory,
                initialDataTransaction,
                context
        );

        this.user = user;
        this.serverUrl = serverUrl;
        this.syncPolicy = syncPolicy;
        this.errorHandler = errorHandler;
        this.deleteRealmOnLogout = deleteRealmOnLogout;
    }


    static URI resolveServerUrl(URI serverUrl, String userIdentifier) {
        try {
            return new URI(serverUrl.toString().replace("/~/", "/" + userIdentifier + "/"));
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Could not replace '/~/' with a valid user ID.", e);
        }
    }

    // Extract the full server path, minus the file name
    private static String getServerPath(URI serverUrl) {
        String path = serverUrl.getPath();
        int endIndex = path.lastIndexOf("/");
        if (endIndex == -1 ) {
            return path;
        } else if (endIndex == 0) {
            return path.substring(1);
        } else {
            return path.substring(1, endIndex); // Also strip leading /
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        SyncConfiguration that = (SyncConfiguration) o;

        if (deleteRealmOnLogout != that.deleteRealmOnLogout) return false;
        if (!serverUrl.equals(that.serverUrl)) return false;
        if (!user.equals(that.user)) return false;
        if (!syncPolicy.equals(that.syncPolicy)) return false;
        return errorHandler.equals(that.errorHandler);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + serverUrl.hashCode();
        result = 31 * result + user.hashCode();
        result = 31 * result + syncPolicy.hashCode();
        result = 31 * result + errorHandler.hashCode();
        result = 31 * result + (deleteRealmOnLogout ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        // TODO
        return stringBuilder.toString();
    }

    // Keeping this package protected for now. The API might still be subject to change.
    SyncPolicy getSyncPolicy() {
        return syncPolicy;
    }

    public User getUser() {
        return user;
    }

    /**
     * Returns the fully disambiguated URI for the remote Realm, i.e. any {@code /~/} placeholder has been replaced
     * by the proper user ID.
     *
     * @return {@link URI} identifying the remote Realm this local Realm is synchronized with.
     */
    public URI getServerUrl() {
        return serverUrl;
    }

    public Session.ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    /**
     * Returns {@code true} if the Realm file must be deleted once the {@link User} owning it logs out.
     *
     * @return {@code true} if the Realm file must be deleted if the {@link User} logs out. {@code false} if the file
     *         is allowed to remain behind.
     */
    public boolean shouldDeleteRealmOnLogout() {
        return deleteRealmOnLogout;
    }

    /**
     * Builder used to construct instances of a SyncConfiguration in a fluent manner.
     */
    public static final class Builder  {

        private Context context;
        private File directory;
        private boolean overrideDefaultFolder = false;
        private String fileName;
        private boolean overrideDefaultLocalFileName = false;
        private byte[] key;
        private HashSet<Object> modules = new HashSet<Object>();
        private HashSet<Class<? extends RealmModel>> debugSchema = new HashSet<Class<? extends RealmModel>>();
        private RxObservableFactory rxFactory;
        private Realm.Transaction initialDataTransaction;
        private URI serverUrl;
        private User user = null;
        private SyncPolicy syncPolicy = new AutomaticSyncPolicy();
        private Session.ErrorHandler errorHandler = SyncManager.defaultSessionErrorHandler;
        private File defaultFolder;
        private String defaultLocalFileName;
        private SharedRealm.Durability durability = SharedRealm.Durability.FULL;
        private boolean deleteRealmOnLogout = false;

        /**
         * Creates an instance of the Builder for the SyncConfiguration.
         * <p>
         * This will use the app's own internal directory for storing the Realm file. This does not require any
         * additional permissions. The default location is {@code /data/data/<packagename>/files/realm-object-server},
         * but can change depending on vendor implementations of Android.
         *
         * @param context the Android application context.
         */
        public Builder(Context context) {
            this.context = context;
            this.defaultFolder = new File(context.getFilesDir(), "realm-object-server");
            if (Realm.getDefaultModule() != null) {
                this.modules.add(Realm.getDefaultModule());
            }
        }

        /**
         * Sets the local filename for the Realm.
         * This will override the default name defined by the {@link #serverUrl(String)}
         *
         * @param filename name of the local file on disk.
         */
        public Builder name(String filename) {
            if (filename == null || filename.isEmpty()) {
                throw new IllegalArgumentException("A non-empty filename must be provided");
            }
            this.fileName = filename;
            this.overrideDefaultLocalFileName = true;
            return this;
        }

        /**
         * Sets the local root directory where synchronized Realm files can be saved.
         *
         * Synchronized Realms will not be saved directly in the provided directory, but instead in a
         * subfolder that matches the path defined by {@link #serverUrl(String)}. As Realm server URLs are unique
         * this means that multiple users can save their Realms on disk without the risk of them overriding each other.
         *
         * The default location is {@code context.getFilesDir()}.
         *
         * @param directory directory on disk where the Realm file can be saved.
         * @throws IllegalArgumentException if the directory is not valid.
         */
        public Builder directory(File directory) {
            if (directory == null) {
                throw new IllegalArgumentException("Non-null 'directory' required.");
            }
            if (directory.isFile()) {
                throw new IllegalArgumentException("'directory' is a file, not a directory: " +
                        directory.getAbsolutePath() + ".");
            }
            if (!directory.exists() && !directory.mkdirs()) {
                throw new IllegalArgumentException("Could not create the specified directory: " +
                        directory.getAbsolutePath() + ".");
            }
            if (!directory.canWrite()) {
                throw new IllegalArgumentException("Realm directory is not writable: " +
                        directory.getAbsolutePath() + ".");
            }
            this.directory = directory;
            overrideDefaultFolder = true;
            return this;
        }

        /**
         * Sets the 64 bit key used to encrypt and decrypt the Realm file.
         * Sets the {@value io.realm.RealmConfiguration#KEY_LENGTH} bytes key used to encrypt and decrypt the Realm file.
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
         * @throws IllegalArgumentException if any of the modules don't have the {@link RealmModule} annotation.
         * @see Realm#getDefaultModule()
         */
        public Builder modules(Object baseModule, Object... additionalModules) {
            modules.clear();
            addModule(baseModule);
            if (additionalModules != null) {
                for (Object module : additionalModules) {
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
         * when database file is created or while migrating the data when
         * {@link RealmConfiguration.Builder#deleteRealmIfMigrationNeeded()} is set.
         *
         * @param transaction transaction to execute.
         */
        public Builder initialData(Realm.Transaction transaction) {
            initialDataTransaction = transaction;
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
            this.durability = SharedRealm.Durability.MEM_ONLY;
            return this;
        }

        /**
         * Enable server side synchronization for this Realm. The name should be a unique URL that identifies the Realm.
         * {@code /~/} can be used as a placeholder for a user ID in case the Realm should only be available to one
         * user, e.g. {@code "realm://objectserver.realm.io/~/default"}
         *
         * The `/~/` will automatically be replaced with the user ID when creating the {@link SyncConfiguration}.
         *
         * The URL also defines the local location on the device. The default location of a synchronized Realm file is
         * {@code /data/data/<packageName>/files/realm-object-server/<user-id>/<last-path-segment>}.
         *
         * This behaviour can be overwritten using {@link #name(String)} and {@link #directory(File)}.
         *
         * @param url URL identifying the Realm.
         * @throws IllegalArgumentException if the URL is not valid.
         */
        public Builder serverUrl(String url) {
            if (url == null) {
                throw new IllegalArgumentException("Non-null 'url' required.");
            }
            try {
                this.serverUrl = new URI(url);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Invalid url: " + url, e);
            }

            // Detect last path segment as it is the default file name
            String path = serverUrl.getPath();
            if (path == null) {
                throw new IllegalArgumentException("Invalid url: " + url);
            }

            String[] pathSegments = path.split("/");
            this.defaultLocalFileName = pathSegments[pathSegments.length - 1];

            // Validate filename
            // TODO Lift this restriction on the Object Server
            if (defaultLocalFileName.endsWith(".realm")) {
                throw new IllegalArgumentException("The URL must not end with '.realm': " + url);
            }

            return this;
        }

        /**
         * Set the user for this Realm. An authenticated {@link User} is required to open any Realm managed by a
         * Realm Object Server.
         *
         * @param user {@link User} who wants to access this Realm.
         */
        public Builder user(User user) {
            if (user == null) {
                throw new IllegalArgumentException("Non-null `user` required.");
            }
            if (!user.isValid()) {
                throw new IllegalArgumentException("User not authenticated or authentication expired.");
            }
            this.user = user;
            return this;
        }

        /**
         * Sets the {@link SyncPolicy} used to control when changes should be synchronized with the remote Realm.
         * The default policy is {@link AutomaticSyncPolicy}.
         *
         * @param syncPolicy policy to use.
         *
         * @see Session
         */
        Builder syncPolicy(SyncPolicy syncPolicy) {
            // Package protected until SyncPolicy API is more stable.
            this.syncPolicy = syncPolicy;
            return this;
        }

        /**
         * Sets the error handler used by this configuration. This will override any handler set by calling
         * {@link SyncManager#setDefaultSessionErrorHandler(Session.ErrorHandler)}.
         *
         * Only errors not handled by the defined {@code SyncPolicy} will be reported to this error handler.
         *
         * @param errorHandler error handler used to report back errors when communicating with the Realm Object Server.
         * @throws IllegalArgumentException if {@code null} is given as an error handler.
         */
        public Builder errorHandler(Session.ErrorHandler errorHandler) {
            if (errorHandler == null) {
                throw new IllegalArgumentException("Non-null 'errorHandler' required.");
            }
            this.errorHandler = errorHandler;
            return this;
        }

        /**
         * Setting this will cause the local Realm file used to synchronize changes to be deleted if the {@link User}
         * defined by {@link #user(User)} logs out from the device using {@link User#logout()}.
         *
         * The default behaviour is that the Realm file is allowed to stay behind, making it faster for users to log in
         * again and have access to their data faster.
         */
        public Builder deleteRealmOnLogout() {
            this.deleteRealmOnLogout = true;
            return this;
        }

        /**
         * Creates the RealmConfiguration based on the builder parameters.
         *
         * @return the created {@link SyncConfiguration}.
         */
        public SyncConfiguration build() {
            if (serverUrl == null || user == null) {
                throw new IllegalStateException("serverUrl() and user() are both required.");
            }

            // Check if the user has an identifier, if not, it cannot use /~/.
            if (serverUrl.toString().contains("/~/") && user.getIdentity() == null) {
                throw new IllegalStateException("The serverUrl contains a /~/, but the user does not have an identity." +
                        " Most likely it hasn't been authenticated yet or has been created directly from an" +
                        " access token. Use a path without /~/.");
            }

            // Determine location on disk
            // Use the serverUrl + user to create a unique filepath unless it has been explicitly overridden.
            // <rootDir>/<serverPath>/<serverFileNameOrOverriddenFileName>
            URI resolvedServerUrl = resolveServerUrl(serverUrl, user.getIdentity());
            File rootDir = overrideDefaultFolder ? directory : defaultFolder;
            String realmPathFromRootDir = getServerPath(resolvedServerUrl);
            File realmFileDirectory = new File(rootDir, realmPathFromRootDir);
            // Create the folder on disk (if needed)
            if (!realmFileDirectory.exists() && !realmFileDirectory.mkdirs()) {
                throw new IllegalStateException("Could not create directory for saving the Realm: " + realmFileDirectory);
            }
            String realmFileName = overrideDefaultLocalFileName ? fileName : defaultLocalFileName;

            return new SyncConfiguration(
                    // Realm Configuration options
                    realmFileDirectory,
                    realmFileName,
                    getCanonicalPath(new File(realmFileDirectory, realmFileName)),
                    null, // assetFile not supported by Sync. See https://github.com/realm/realm-sync/issues/241
                    key,
                    -1, // Schema version not supported
                    null, // Custom migrations not supported
                    false, // MigrationNeededException is never thrown
                    durability,
                    createSchemaMediator(modules, debugSchema),
                    rxFactory,
                    initialDataTransaction,
                    new WeakReference<Context>(context),

                    // Sync Configuration specific
                    user,
                    resolvedServerUrl,
                    syncPolicy,
                    errorHandler,
                    deleteRealmOnLogout
            );
        }

        private void addModule(Object module) {
            if (module != null) {
                checkModule(module);
                modules.add(module);
            }
        }

        private void checkModule(Object module) {
            if (!module.getClass().isAnnotationPresent(RealmModule.class)) {
                throw new IllegalArgumentException(module.getClass().getCanonicalName() + " is not a RealmModule. " +
                        "Add @RealmModule to the class definition.");
            }
        }
    }
}
