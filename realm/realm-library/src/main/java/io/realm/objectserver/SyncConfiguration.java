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
import java.net.URI;
import java.net.URISyntaxException;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.objectserver.internal.syncpolicy.AutomaticSyncPolicy;
import io.realm.objectserver.internal.syncpolicy.SyncPolicy;
import io.realm.rx.RxObservableFactory;

/**
 * An {@link SyncConfiguration} is used to setup a Realm that can be synchronized between devices using the Realm
 * Object Server.
 * <p>
 * A valid {@link User} is required to create a SyncConfiguration. See {@link Credentials} and
 * {@link User#loginAsync(Credentials, String, User.Callback)} for more information on
 * how to get a user object.
 * <p>
 * A minimal SyncConfiguration can look like this:
 * <pre>
 * {@code
 * SyncConfiguration config = new SyncConfiguration.Builder(context)
 *   .serverUrl("realm://objectserver.realm.io/~/default")
 *   .user(myUser)
 *   .build();
 * }
 * </pre>
 *
 * Realms created using a {@link SyncConfiguration} is accessed normally using {@link Realm#getInstance(RealmConfiguration)}
 * and can also be stored using {@link Realm#setDefaultConfiguration(RealmConfiguration)}.
 *
 * TODO Need to expand this section I think
 */
public final class SyncConfiguration extends RealmConfiguration {

    private final File realmDirectory;
    private final String realmFileName;
    private final String canonicalPath;
    private final URI serverUrl;
    private final User user;
    private final SyncPolicy syncPolicy;
    private final Session.ErrorHandler errorHandler;

    private SyncConfiguration(Builder builder) {
        super(builder);
        if (builder.serverUrl == null || builder.user == null) {
            throw new IllegalStateException("serverUrl() and user() are both required.");
        }

        // Check if the user has an identifier, if not, it cannot use /~/.
        if (builder.serverUrl.toString().contains("/~/") && builder.user.getIdentifier() == null) {
            throw new IllegalStateException("The serverUrl contained a /~/, but the user does not have an identifier," +
                    " most likely because it hasn't been authenticated yet or have been created directly from an" +
                    " access token. Use a path without /~/.");
        }

        this.user = builder.user;
        this.serverUrl = getFullServerUrl(builder.serverUrl, user.getIdentifier());
        this.syncPolicy = builder.syncPolicy;
        this.errorHandler = builder.errorHandler;

        // Determine location on disk
        // Use the serverUrl + user to create a unique filepath unless it has been explicitly overridden.
        // <rootDir>/<serverPath>/<serverFileNameOrOverriddenFileName>
        File rootDir = builder.overrideDefaultFolder ? super.getRealmDirectory() : builder.defaultFolder;
        String realmPath = getServerPath(serverUrl);
        this.realmDirectory = new File(rootDir, realmPath);
        // Create the folder on disk (if needed)
        if (!realmDirectory.exists() && !realmDirectory.mkdirs()) {
            throw new IllegalStateException("Could not create directory for saving the Realm: " + realmDirectory);
        }
        this.realmFileName = builder.overrideDefaultLocalFileName ? super.getRealmFileName() : builder.defaultLocalFileName;
        this.canonicalPath = getCanonicalPath(new File(realmDirectory, realmFileName));
    }

    static URI getFullServerUrl(URI serverUrl, String userIdentifier) {
        try {
            return new URI(serverUrl.toString().replace("/~/", "/" + userIdentifier + "/"));
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Could not replace '/~/' with a valid user ID.", e);
        }
    }

    // Extract the full server path, minus the file name
    private String getServerPath(URI serverUrl) {
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

        if (realmDirectory != null ? !realmDirectory.equals(that.realmDirectory) : that.realmDirectory != null) return false;
        if (realmFileName != null ? !realmFileName.equals(that.realmFileName) : that.realmFileName != null) return false;
        if (canonicalPath != null ? !canonicalPath.equals(that.canonicalPath) : that.canonicalPath != null) return false;
        if (serverUrl != null ? !serverUrl.equals(that.serverUrl) : that.serverUrl != null) return false;
        if (user != null ? !user.equals(that.user) : that.user != null) return false;
        if (syncPolicy != null ? !syncPolicy.equals(that.syncPolicy) : that.syncPolicy != null) return false;
        return errorHandler != null ? errorHandler.equals(that.errorHandler) : that.errorHandler == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (realmDirectory != null ? realmDirectory.hashCode() : 0);
        result = 31 * result + (realmFileName != null ? realmFileName.hashCode() : 0);
        result = 31 * result + (canonicalPath != null ? canonicalPath.hashCode() : 0);
        result = 31 * result + (serverUrl != null ? serverUrl.hashCode() : 0);
        result = 31 * result + (user != null ? user.hashCode() : 0);
        result = 31 * result + (syncPolicy != null ? syncPolicy.hashCode() : 0);
        result = 31 * result + (errorHandler != null ? errorHandler.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        // TODO
        return stringBuilder.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getRealmDirectory() {
        return this.realmDirectory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRealmFileName() {
        return this.realmFileName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPath() {
        return this.canonicalPath;
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
     * ReplicationConfiguration.Builder used to construct instances of a ReplicationConfiguration in a fluent manner.
     */
    public static final class Builder extends RealmConfiguration.Builder {

        private Context context;
        private URI serverUrl;
        private User user = null;
        private SyncPolicy syncPolicy = new AutomaticSyncPolicy();
        private Session.ErrorHandler errorHandler = SyncManager.defaultSessionErrorHandler;
        private boolean overrideDefaultFolder = false;
        private boolean overrideDefaultLocalFileName = false;
        private File defaultFolder;
        private String defaultLocalFileName;

        /**
         * {@inheritDoc}
         */
        public Builder(Context context) {
            super(context);
            this.context = context;
        }

        /**
         * Sets the local filename for the Realm.
         * This will override the default name defined by the {@link #serverUrl(String)}
         *
         * @param name name of the local file on disk.
         */
        @Override
        public Builder name(String name) {
            super.name(name);
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
         * @param dir directory on disk where the Realm file can be saved.
         * @throws IllegalArgumentException if the directory is not valid.
         */
        public Builder directory(File dir) {
            super.directory(dir);
            overrideDefaultFolder = true;
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Builder encryptionKey(byte[] key) {
            super.encryptionKey(key);
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Builder schemaVersion(long schemaVersion) {
            super.schemaVersion(schemaVersion);
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Builder deleteRealmIfMigrationNeeded() {
            super.deleteRealmIfMigrationNeeded();
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public SyncConfiguration.Builder inMemory() {
            super.inMemory();
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Builder modules(Object baseModule, Object... additionalModules) {
            super.modules(baseModule, additionalModules);
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public SyncConfiguration.Builder rxFactory(RxObservableFactory factory) {
            super.rxFactory(factory);
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Builder initialData(Realm.Transaction transaction) {
            super.initialData(transaction);
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Builder assetFile(String assetFile) {
            super.assetFile(assetFile);
            return this;
        }

        /**
         * Manual migrations are not supported (yet) for Realms that can be synced using the Realm Object Server
         * Only additive changes are allowed, and these will be detected and applied automatically.
         *
         * @throws IllegalArgumentException always.
         */
        @Override
        public Builder migration(RealmMigration migration) {
            throw new IllegalArgumentException("Migrations are not supported for Realms that can be synchronized using the Realm Mobile Platform");
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
            if (!user.isAuthenticated()) {
                throw new IllegalArgumentException("User not authenticated or authentication expired. User ID: " + user.getIdentifier());
            }

            this.defaultFolder = new File(context.getFilesDir(), "realm-object-server");
            this.user = user;
            return this;
        }

        /**
         * Sets the sync policy used to control when changes should be synchronized with the remote Realm.
         * The default policy is {@link AutomaticSyncPolicy}.
         *
         * @param syncPolicy policy to use.
         *
         * @see Session
         */
        Builder syncPolicy(SyncPolicy syncPolicy) {
            // TODO: Decide if we should launch with this as package protected since the sync notification API might
            // change quite a bit.
            this.syncPolicy = syncPolicy;
            return this;
        }

        /**
         * Sets the error handler used by this configuration.
         * This will override any handler set by calling {@link SyncManager#setDefaultSessionErrorHandler(Session.ErrorHandler)}.
         *
         * Only errors not handled by the defined {@link SyncPolicy} will be reported to this error handler.
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
         * Creates the RealmConfiguration based on the builder parameters.
         *
         * @return the created {@link SyncConfiguration}.
         */
        public SyncConfiguration build() {
            return new SyncConfiguration(this);
        }
    }
}
