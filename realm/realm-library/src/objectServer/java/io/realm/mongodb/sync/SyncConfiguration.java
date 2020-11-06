/*
 * Copyright 2020 Realm Inc.
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

package io.realm.mongodb.sync;

import android.content.Context;

import org.bson.BsonInt32;
import org.bson.BsonInt64;
import org.bson.BsonNull;
import org.bson.BsonObjectId;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.bson.types.ObjectId;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.realm.CompactOnLaunchCallback;
import io.realm.DefaultCompactOnLaunchCallback;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.RealmModel;
import io.realm.RealmQuery;
import io.realm.annotations.Beta;
import io.realm.annotations.RealmModule;
import io.realm.coroutines.CoroutinesFactory;
import io.realm.exceptions.RealmException;
import io.realm.internal.OsRealmConfig;
import io.realm.internal.RealmProxyMediator;
import io.realm.internal.Util;
import io.realm.mongodb.App;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;
import io.realm.rx.RealmObservableFactory;
import io.realm.rx.RxObservableFactory;

/**
 * A {@link SyncConfiguration} is used to setup a Realm Database that can be synchronized between
 * devices using MongoDB Realm.
 * <p>
 * A valid {@link User} is required to create a {@link SyncConfiguration}. See
 * {@link Credentials} and {@link App#loginAsync(Credentials, App.Callback)} for
 * more information on how to get a user object.
 * <p>
 * A minimal {@link SyncConfiguration} can be found below.
 * <pre>
 * {@code
 * App app = new App("app-id");
 * User user = app.login(Credentials.anonymous());
 * SyncConfiguration config = SyncConfiguration.defaultConfiguration(user, "partition-value");
 * Realm realm = Realm.getInstance(config);
 * }
 * </pre>
 * <p>
 * Synchronized Realms only support additive migrations which can be detected and performed
 * automatically, so the following builder options are not accessible compared to a normal Realm:
 *
 * <ul>
 *     <li>{@code deleteRealmIfMigrationNeeded()}</li>
 *     <li>{@code migration(Migration)}</li>
 * </ul>
 *
 * Synchronized Realms are created by using {@link Realm#getInstance(RealmConfiguration)} and
 * {@link Realm#getDefaultInstance()} like ordinary unsynchronized Realms.
 *
 * @see <a href="https://docs.realm.io/platform/using-synced-realms/syncing-data">The docs</a> for
 * more information about the two types of synchronization.
 */
@Beta
public class SyncConfiguration extends RealmConfiguration {

    private final URI serverUrl;
    private final User user;
    private final SyncSession.ErrorHandler errorHandler;
    private final SyncSession.ClientResetHandler clientResetHandler;
    private final boolean deleteRealmOnLogout;
    private final boolean waitForInitialData;
    private final long initialDataTimeoutMillis;
    private final OsRealmConfig.SyncSessionStopPolicy sessionStopPolicy;
    @Nullable private final String syncUrlPrefix;
    private final ClientResyncMode clientResyncMode;
    private final BsonValue partitionValue;

    private SyncConfiguration(File realmPath,
                              @Nullable String assetFilePath,
                              @Nullable byte[] key,
                              long schemaVersion,
                              @Nullable RealmMigration migration,
                              boolean deleteRealmIfMigrationNeeded,
                              OsRealmConfig.Durability durability,
                              RealmProxyMediator schemaMediator,
                              @Nullable RxObservableFactory rxFactory,
                              @Nullable CoroutinesFactory coroutinesFactory,
                              @Nullable Realm.Transaction initialDataTransaction,
                              boolean readOnly,
                              long maxNumberOfActiveVersions,
                              boolean allowWritesOnUiThread,
                              boolean allowQueriesOnUiThread,
                              User user,
                              URI serverUrl,
                              SyncSession.ErrorHandler errorHandler,
                              SyncSession.ClientResetHandler clientResetHandler,
                              boolean deleteRealmOnLogout,
                              boolean waitForInitialData,
                              long initialDataTimeoutMillis,
                              OsRealmConfig.SyncSessionStopPolicy sessionStopPolicy,
                              CompactOnLaunchCallback compactOnLaunch,
                              @Nullable String syncUrlPrefix,
                              ClientResyncMode clientResyncMode,
                              BsonValue partitionValue) {
        super(realmPath,
                assetFilePath,
                key,
                schemaVersion,
                migration,
                deleteRealmIfMigrationNeeded,
                durability,
                schemaMediator,
                rxFactory,
                coroutinesFactory,
                initialDataTransaction,
                readOnly,
                compactOnLaunch,
                false,
                maxNumberOfActiveVersions,
                allowWritesOnUiThread,
                allowQueriesOnUiThread
        );

        this.user = user;
        this.serverUrl = serverUrl;
        this.errorHandler = errorHandler;
        this.clientResetHandler = clientResetHandler;
        this.deleteRealmOnLogout = deleteRealmOnLogout;
        this.waitForInitialData = waitForInitialData;
        this.initialDataTimeoutMillis = initialDataTimeoutMillis;
        this.sessionStopPolicy = sessionStopPolicy;
        this.syncUrlPrefix = syncUrlPrefix;
        this.clientResyncMode = clientResyncMode;
        this.partitionValue = partitionValue;
    }

    /**
     * Returns a {@link RealmConfiguration} appropriate to open a read-only, non-synced Realm to recover any pending changes.
     * This is useful when trying to open a backup/recovery Realm (after a client reset).
     *
     * @param canonicalPath the absolute path to the Realm file defined by this configuration.
     * @param encryptionKey the key used to encrypt/decrypt the Realm file.
     * @param modules if specified it will restricts Realm schema to the provided module.
     * @return RealmConfiguration that can be used offline
     */
    public static RealmConfiguration forRecovery(String canonicalPath, @Nullable byte[] encryptionKey, @Nullable Object... modules) {
        HashSet<Object> validatedModules = new HashSet<>();
        if (modules != null && modules.length > 0) {
            for (Object module : modules) {
                if (!module.getClass().isAnnotationPresent(RealmModule.class)) {
                    throw new IllegalArgumentException(module.getClass().getCanonicalName() + " is not a RealmModule. " +
                            "Add @RealmModule to the class definition.");
                }
                validatedModules.add(module);
            }
        } else {
            if (Realm.getDefaultModule() != null) {
                validatedModules.add(Realm.getDefaultModule());
            }
        }

        RealmProxyMediator schemaMediator = createSchemaMediator(validatedModules, Collections.<Class<? extends RealmModel>>emptySet());
        return RealmConfiguration.forRecovery(canonicalPath, encryptionKey, schemaMediator);
    }

    RealmConfiguration forErrorRecovery(String canonicalPath) {
        return RealmConfiguration.forRecovery(canonicalPath, getEncryptionKey(), getSchemaMediator());
    }

    /**
     * Returns a default configuration for the given user and partition value.
     *
     * @param user The user that will be used for accessing the Realm App.
     * @param partitionValue The partition value identifying the remote Realm that will be synchronized.
     * @return the default configuration for the given user and partition value.
     */
    @Beta
    public static SyncConfiguration defaultConfig(User user, @Nullable String partitionValue) {
        return new SyncConfiguration.Builder(user, partitionValue).build();
    }

    /**
     * Returns a default configuration for the given user and partition value.
     *
     * @param user The user that will be used for accessing the Realm App.
     * @param partitionValue The partition value identifying the remote Realm that will be synchronized.
     * @return the default configuration for the given user and partition value.
     */
    @Beta
    public static SyncConfiguration defaultConfig(User user, @Nullable Long partitionValue) {
        return new SyncConfiguration.Builder(user, partitionValue).build();
    }

    /**
     * Returns a default configuration for the given user and partition value.
     *
     * @param user The user that will be used for accessing the Realm App.
     * @param partitionValue The partition value identifying the remote Realm that will be synchronized.
     * @return the default configuration for the given user and partition value.
     */
    @Beta
    public static SyncConfiguration defaultConfig(User user, @Nullable Integer partitionValue) {
        return new SyncConfiguration.Builder(user, partitionValue).build();
    }

    /**
     * Returns a default configuration for the given user and partition value.
     *
     * @param user The user that will be used for accessing the Realm App.
     * @param partitionValue The partition value identifying the remote Realm that will be synchronized.
     * @return the default configuration for the given user and partition value.
     */
    @Beta
    public static SyncConfiguration defaultConfig(User user, @Nullable ObjectId partitionValue) {
        return new SyncConfiguration.Builder(user, partitionValue).build();
    }

    /**
     * Returns a {@link RealmConfiguration} appropriate to open a read-only, non-synced Realm to recover any pending changes.
     * This is useful when trying to open a backup/recovery Realm (after a client reset).
     *
     * Note: This will use the default Realm module (composed of all {@link RealmModel}), and
     * assume no encryption should be used as well.
     *
     * @param canonicalPath the absolute path to the Realm file defined by this configuration.
     * @return RealmConfiguration that can be used offline
     */
    public static RealmConfiguration forRecovery(String canonicalPath) {
        return forRecovery(canonicalPath, null);
    }

    @Override
    protected Realm.Transaction getInitialDataTransaction() {
        return super.getInitialDataTransaction();
    }

    // Extract the full server path, minus the file name
    private static String getServerPath(User user, URI serverUrl) {
        // FIXME Add support for partion key
        // Current scheme is <rootDir>/<appId>/<userId>/default.realm or
        // Current scheme is <rootDir>/<appId>/<userId>/<hashedPartionKey>/default.realm
        return user.getApp().getConfiguration().getAppId() + "/" + user.getId(); // TODO Check that it doesn't contain invalid filesystem chars
    }

    @SuppressFBWarnings("NP_METHOD_PARAMETER_TIGHTENS_ANNOTATION")
    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        SyncConfiguration that = (SyncConfiguration) o;

        if (deleteRealmOnLogout != that.deleteRealmOnLogout) return false;
        if (waitForInitialData != that.waitForInitialData) return false;
        if (initialDataTimeoutMillis != that.initialDataTimeoutMillis) return false;
        if (!serverUrl.equals(that.serverUrl)) return false;
        if (!user.equals(that.user)) return false;
        if (!errorHandler.equals(that.errorHandler)) return false;
        if (sessionStopPolicy != that.sessionStopPolicy) return false;
        if (syncUrlPrefix != null ? !syncUrlPrefix.equals(that.syncUrlPrefix) : that.syncUrlPrefix != null)
            return false;
        if (clientResyncMode != that.clientResyncMode) return false;
        return partitionValue.equals(that.partitionValue);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + serverUrl.hashCode();
        result = 31 * result + user.hashCode();
        result = 31 * result + errorHandler.hashCode();
        result = 31 * result + (deleteRealmOnLogout ? 1 : 0);
        result = 31 * result + (waitForInitialData ? 1 : 0);
        result = 31 * result + (int) (initialDataTimeoutMillis ^ (initialDataTimeoutMillis >>> 32));
        result = 31 * result + sessionStopPolicy.hashCode();
        result = 31 * result + (syncUrlPrefix != null ? syncUrlPrefix.hashCode() : 0);
        result = 31 * result + clientResyncMode.hashCode();
        result = 31 * result + partitionValue.hashCode();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append("\n");
        sb.append("serverUrl: ").append(serverUrl);
        sb.append("\n");
        sb.append("user: ").append(user);
        sb.append("\n");
        sb.append("errorHandler: ").append(errorHandler);
        sb.append("\n");
        sb.append("deleteRealmOnLogout: ").append(deleteRealmOnLogout);
        sb.append("\n");
        sb.append("waitForInitialData: ").append(waitForInitialData);
        sb.append("\n");
        sb.append("initialDataTimeoutMillis: ").append(initialDataTimeoutMillis);
        sb.append("\n");
        sb.append("sessionStopPolicy: ").append(sessionStopPolicy);
        sb.append("\n");
        sb.append("syncUrlPrefix: ").append(syncUrlPrefix);
        sb.append("\n");
        sb.append("clientResyncMode: ").append(clientResyncMode);
        sb.append("\n");
        sb.append("partitionValue: ").append(partitionValue);
        return sb.toString();
    }

    /**
     * Returns the user.
     *
     * @return the user.
     */
    public User getUser() {
        return user;
    }

    /**
     * Returns the server URI for the remote MongoDB Realm the local Realm is synchronizing with.
     *
     * @return {@link URI} identifying the MongoDB Realm this local Realm is synchronized with.
     */
    public URI getServerUrl() {
        return serverUrl;
    }

    /**
     * Returns the error handler for this <i>SyncConfiguration</i>.
     *
     * @return the error handler.
     */
    public SyncSession.ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    /**
     * Returns the Client Reset handler for this <i>SyncConfiguration</i>.
     *
     * @return the Client Reset handler.
     */
    public SyncSession.ClientResetHandler getClientResetHandler() {
        return clientResetHandler;
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
     * Returns {@code true} if the Realm will download all known changes from the remote server before being opened the
     * first time.
     *
     * @return {@code true} if all remote changes will be downloaded before the Realm can be opened. {@code false} if
     * the Realm can be opened immediately.
     */
    public boolean shouldWaitForInitialRemoteData() {
        return waitForInitialData;
    }

    /**
     * Returns the timeout defined when downloading any initial data the first time the Realm is opened.
     * <p>
     * This value is only applicable if {@link #shouldWaitForInitialRemoteData()} returns {@code true}.
     *
     * @return the time Realm will wait for all changes to be downloaded before it is aborted and an exception is thrown.
     * @see SyncConfiguration.Builder#waitForInitialRemoteData(long, TimeUnit)
     */
    public long getInitialRemoteDataTimeout(TimeUnit unit) {
        return unit.convert(initialDataTimeoutMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    protected boolean isSyncConfiguration() {
        return true;
    }

    /**
     * NOTE: Only for internal usage. May change without warning.
     *
     * Returns the stop policy for the session for this Realm once the Realm has been closed.
     *
     * @return the stop policy used by the session once the Realm is closed.
     */
    public OsRealmConfig.SyncSessionStopPolicy getSessionStopPolicy() {
        return sessionStopPolicy;
    }

    /**
     * Returns the url prefix used when establishing a sync connection to the Realm Object Server.
     */
    @Nullable
    public String getUrlPrefix() {
        return syncUrlPrefix;
    }

    /**
     * Returns what happens in case of a Client Resync.
     */
    ClientResyncMode getClientResyncMode() {
        return clientResyncMode;
    }

    /**
     * Returns the value this Realm is partitioned on. The partition key is a property defined in
     * MongoDB Realm. All classes with a property with this value will be synchronized to the
     * Realm.
     *
     * @return the value being used by MongoDB Realm to partition the server side MongoDB Database
     * into Realms that can be synchronized independently.
     */
    public BsonValue getPartitionValue() {
        return partitionValue;
    }

    @Override
    protected boolean realmExists() {
        return super.realmExists();
    }

    /**
     * Builder used to construct instances of a SyncConfiguration in a fluent manner.
     */
    public static final class Builder  {

        @Nullable
        private byte[] key;
        private long schemaVersion = 0;
        private HashSet<Object> modules = new HashSet<Object>();
        private HashSet<Class<? extends RealmModel>> debugSchema = new HashSet<Class<? extends RealmModel>>();
        @Nullable
        private RxObservableFactory rxFactory;
        @Nullable
        private CoroutinesFactory coroutinesFactory;
        @Nullable
        private Realm.Transaction initialDataTransaction;
        @Nullable
        private String filename;
        private OsRealmConfig.Durability durability = OsRealmConfig.Durability.FULL;
        private boolean readOnly = false;
        private boolean waitForServerChanges = false;
        private long initialDataTimeoutMillis = Long.MAX_VALUE;
        // sync specific
        private boolean deleteRealmOnLogout = false;
        private URI serverUrl;
        private User user = null;
        private SyncSession.ErrorHandler errorHandler;
        private SyncSession.ClientResetHandler clientResetHandler;
        private OsRealmConfig.SyncSessionStopPolicy sessionStopPolicy = OsRealmConfig.SyncSessionStopPolicy.AFTER_CHANGES_UPLOADED;
        private CompactOnLaunchCallback compactOnLaunch;
        private String syncUrlPrefix = null;
        @Nullable // null means the user hasn't explicitly set one. An appropriate default is chosen when calling build()
        private ClientResyncMode clientResyncMode = null;
        private long maxNumberOfActiveVersions = Long.MAX_VALUE;
        private boolean allowWritesOnUiThread;
        private boolean allowQueriesOnUiThread;
        private final BsonValue partitionValue;

        /**
         * Creates an instance of the builder for a <i>SyncConfiguration</i> with the given user
         * and partition value.
         *
         * @param user The user that will be used for accessing the Realm App.
         * @param partitionValue The partition value identifying the remote Realm that will be synchronized.
         */
        public Builder(User user, @Nullable String partitionValue) {
            this(user, (partitionValue == null? new BsonNull() : new BsonString(partitionValue)));
        }

        /**
         * Creates an instance of the builder for a <i>SyncConfiguration</i> with the given user
         * and partition value.
         *
         * @param user The user that will be used for accessing the Realm App.
         * @param partitionValue The partition value identifying the remote Realm that will be synchronized.
         */
        public Builder(User user, @Nullable ObjectId partitionValue) {
            this(user, (partitionValue == null? new BsonNull() : new BsonObjectId(partitionValue)));
        }

        /**
         * Creates an instance of the builder for a <i>SyncConfiguration</i> with the given user
         * and partition value.
         *
         * @param user The user that will be used for accessing the Realm App.
         * @param partitionValue The partition value identifying the remote Realm that will be synchronized.
         */
        public Builder(User user, @Nullable Integer partitionValue) {
            this(user, (partitionValue == null? new BsonNull() : new BsonInt32(partitionValue)));
        }

        /**
         * Creates an instance of the builder for a <i>SyncConfiguration</i> with the given user
         * and partition value.
         *
         * @param user The user that will be used for accessing the Realm App.
         * @param partitionValue The partition value identifying the remote Realm that will be synchronized.
         */
        public Builder(User user, @Nullable Long partitionValue) {
            this(user, (partitionValue == null? new BsonNull() : new BsonInt64(partitionValue)));
        }

        /**
         * Builder used to construct instances of a SyncConfiguration in a fluent manner.
         *
         * @param user the user opening the Realm on the server.
         * @param partitionValue te value this Realm is partitioned on. The partition key is a
         * property defined in MongoDB Realm. All classes with a property with this value will be
         * synchronized to the Realm.
         * @see <a href="FIXME">Link to docs about partions</a>
         */
        Builder(User user, BsonValue partitionValue) {
            Context context = Realm.getApplicationContext();
            if (context == null) {
                throw new IllegalStateException("Call `Realm.init(Context)` before creating a SyncConfiguration");
            }
            Util.checkNull(user, "user");
            Util.checkNull(partitionValue, "partitionValue");
            validateAndSet(user);
            validateAndSet(user.getApp().getConfiguration().getBaseUrl());
            this.partitionValue = partitionValue;
            if (Realm.getDefaultModule() != null) {
                this.modules.add(Realm.getDefaultModule());
            }
            this.errorHandler = user.getApp().getConfiguration().getDefaultErrorHandler();
            this.clientResetHandler = user.getApp().getConfiguration().getDefaultClientResetHandler();
            this.allowQueriesOnUiThread = true;
            this.allowWritesOnUiThread = false;
        }

        private void validateAndSet(User user) {
            //noinspection ConstantConditions
            if (user == null) {
                throw new IllegalArgumentException("Non-null `user` required.");
            }
            if (!user.isLoggedIn()) {
                throw new IllegalArgumentException("User not authenticated or authentication expired.");
            }
            this.user = user;
        }

        private void validateAndSet(URL baseUrl ) {
            try {
                serverUrl = new URI(baseUrl.toString());
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Invalid URI: " + baseUrl.toString(), e);
            }

            try {
                // Automatically set scheme based on auth server if not set or wrongly set
                String serverScheme = serverUrl.getScheme();
                if (serverScheme == null || serverScheme.equalsIgnoreCase("http")) {
                    serverScheme = "ws";
                } else if (serverScheme.equalsIgnoreCase("https")) {
                    serverScheme = "wss";
                }

                // Automatically set host if one wasn't defined
                String host = serverUrl.getHost();

                // Convert relative paths to absolute if required
                String path = serverUrl.getPath();
                if (path != null && !path.startsWith("/")) {
                    path = "/" + path;
                }

                serverUrl = new URI(serverScheme,
                        serverUrl.getUserInfo(),
                        host,
                        serverUrl.getPort(),
                        (path != null) ? path.replace(host + "/", "") : null, // Remove host if it accidentially was interpreted as a path segment
                        serverUrl.getQuery(),
                        serverUrl.getRawFragment());

            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Invalid URI: " + baseUrl, e);
            }
        }

        /**
         * FIXME: Make public once https://github.com/realm/realm-object-store/pull/1049 is merged.
         *
         * Sets the filename for the Realm file on this device.
         */
        Builder name(String filename) {
            //noinspection ConstantConditions
            if (filename == null || filename.isEmpty()) {
                throw new IllegalArgumentException("A non-empty filename must be provided");
            }

            // Strip `.realm` suffix as it will be appended by Object Store later
            if (filename.endsWith(".realm")) {
                if (filename.length() == 6) {
                    throw new IllegalArgumentException("'.realm' is not a valid filename");
                } else {
                    filename = filename.substring(0, filename.length() - 6);
                }
            }

            this.filename = filename;
            return this;
        }

        /**
         * Sets the {@value io.realm.Realm#ENCRYPTION_KEY_LENGTH} bytes key used to encrypt and decrypt the Realm file.
         *
         * @param key the encryption key.
         * @throws IllegalArgumentException if key is invalid.
         */
        public Builder encryptionKey(byte[] key) {
            //noinspection ConstantConditions
            if (key == null) {
                throw new IllegalArgumentException("A non-null key must be provided");
            }
            if (key.length != Realm.ENCRYPTION_KEY_LENGTH) {
                throw new IllegalArgumentException(String.format(Locale.US,
                        "The provided key must be %s bytes. Yours was: %s",
                        Realm.ENCRYPTION_KEY_LENGTH, key.length));
            }
            this.key = Arrays.copyOf(key, key.length);
            return this;
        }

        /**
         * DEBUG method. This restricts the Realm schema to only consist of the provided classes without having to
         * create a module. These classes must be available in the default module. Calling this will remove any
         * previously configured modules.
         */
        SyncConfiguration.Builder schema(Class<? extends RealmModel> firstClass, Class<? extends RealmModel>... additionalClasses) {
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
         * DEBUG method. This makes it possible to define different policies for when a session should be stopped when
         * the Realm is closed.
         *
         * @param policy how a session for a Realm should behave when the Realm is closed.
         */
        SyncConfiguration.Builder sessionStopPolicy(OsRealmConfig.SyncSessionStopPolicy policy) {
            sessionStopPolicy = policy;
            return this;
        }


        /**
         * Sets the schema version of the Realm.
         * <p>
         * Synced Realms only support additive schema changes which can be applied without requiring a manual
         * migration. The schema version will only be used as an indication to the underlying storage layer to remove
         * or add indexes. These will be recalculated if the provided schema version differ from the version in the
         * Realm file.
         *
         * <b>WARNING:</b> There is no guarantee that the value inserted here is the same returned by {@link Realm#getVersion()}.
         * Due to the nature of synced Realms, the value can both be higher and lower.
         * <ul>
         *     <li>It will be lower if another client with a lesser {@code schemaVersion} connected to the server for
         *         the first time after this schemaVersion was used.
     *         </li>
         *     <li>It will be higher if another client with a higher {@code schemaVersion} connected to the server after
         *         this Realm was created.
     *         </li>
         * </ul>
         *
         * @param schemaVersion the schema version.
         * @throws IllegalArgumentException if schema version is invalid.
         */
        public Builder schemaVersion(long schemaVersion) {
            if (schemaVersion < 0) {
                throw new IllegalArgumentException("Realm schema version numbers must be 0 (zero) or higher. Yours was: " + schemaVersion);
            }
            this.schemaVersion = schemaVersion;
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
            //noinspection ConstantConditions
            if (additionalModules != null) {
                for (Object module : additionalModules) {
                    addModule(module);
                }
            }
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
         * @param modules list of modules tthe first Realm module (required).
         * @throws IllegalArgumentException if any of the modules don't have the {@link RealmModule} annotation.
         * @see Realm#getDefaultModule()
         */
        public Builder modules(Iterable<Object> modules) {
            this.modules.clear();
            if (modules != null) {
                for (Object module : modules) {
                    addModule(module);
                }
            }
            return this;
        }

        /**
         * Adds a module to the already defined modules.
         */
        public Builder addModule(Object module) {
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
         * FIXME
         * @param factory
         * @return
         */
        public Builder coroutinesFactory(CoroutinesFactory factory) {
            coroutinesFactory = factory;
            return this;
        }

        /**
         * Sets the initial data in {@link io.realm.Realm}. This transaction will be executed only the first time
         * the Realm file is opened (created) or while migrating the data if
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
            this.durability = OsRealmConfig.Durability.MEM_ONLY;
            return this;
        }

        /**
         * Sets the error handler used by this configuration.
         * <p>
         * Only errors not handled by the defined {@code SyncPolicy} will be reported to this error handler.
         *
         * @param errorHandler error handler used to report back errors when communicating with the Realm Object Server.
         * @throws IllegalArgumentException if {@code null} is given as an error handler.
         */
        public Builder errorHandler(SyncSession.ErrorHandler errorHandler) {
            Util.checkNull(errorHandler, "handler");
            this.errorHandler = errorHandler;
            return this;
        }

        /**
         * Sets the handler for when a Client Reset occurs. If no handler is set, and error is
         * logged when a Client Reset occurs.
         *
         * @param handler custom handler in case of a Client Reset.
         */
        public Builder clientResetHandler(SyncSession.ClientResetHandler handler) {
            Util.checkNull(handler, "handler");
            this.clientResetHandler = handler;
            return this;
        }

        /**
         * Setting this will cause the Realm to download all known changes from the server the first time a Realm is
         * opened. The Realm will not open until all the data has been downloaded. This means that if a device is
         * offline the Realm will not open.
         * <p>
         * Since downloading all changes can be an lengthy operation that might block the UI thread, Realms with this
         * setting enabled should only be opened on background threads or with
         * {@link Realm#getInstanceAsync(RealmConfiguration, Realm.Callback)} on the UI thread.
         * <p>
         * This check is only enforced the first time a Realm is created. If you otherwise want to make sure a Realm
         * has the latest changes, use {@link SyncSession#downloadAllServerChanges()}.
         */
        public Builder waitForInitialRemoteData() {
            this.waitForServerChanges = true;
            this.initialDataTimeoutMillis = Long.MAX_VALUE;
            return this;
        }

        /**
         * Setting this will cause the Realm to download all known changes from the server the first time a Realm is
         * opened. The Realm will not open until all the data has been downloaded. This means that if a device is
         * offline the Realm will not open.
         * <p>
         * Since downloading all changes can be an lengthy operation that might block the UI thread, Realms with this
         * setting enabled should only be opened on background threads or with
         * {@link Realm#getInstanceAsync(RealmConfiguration, Realm.Callback)} on the UI thread.
         * <p>
         * This check is only enforced the first time a Realm is created. If you otherwise want to make sure a Realm
         * has the latest changes, use {@link SyncSession#downloadAllServerChanges()}.
         *
         * @param timeout how long to wait for the download to complete before an {@link io.realm.exceptions.DownloadingRealmInterruptedException} is thrown.
         * @param unit the unit of time used to define the timeout.
         */
        public Builder waitForInitialRemoteData(long timeout, TimeUnit unit) {
            if (timeout < 0) {
                throw new IllegalArgumentException("'timeout' must be >= 0. It was: " + timeout);
            }
            //noinspection ConstantConditions
            if (unit == null) {
                throw new IllegalArgumentException("Non-null 'unit' required");
            }
            this.waitForServerChanges = true;
            this.initialDataTimeoutMillis = unit.toMillis(timeout);
            return this;
        }

        /**
         * Setting this will cause the Realm to become read only and all write transactions made against this Realm will
         * fail with an {@link IllegalStateException}.
         * <p>
         * This in particular mean that {@link #initialData(Realm.Transaction)} will not work in combination with a
         * read only Realm and setting this will result in a {@link IllegalStateException} being thrown.
         * </p>
         * Marking a Realm as read only only applies to the Realm in this process. Other processes and devices can still
         * write to the Realm.
         */
        public SyncConfiguration.Builder readOnly() {
            this.readOnly = true;
            return this;
        }

        /**
         * Setting this will cause Realm to compact the Realm file if the Realm file has grown too large and a
         * significant amount of space can be recovered. See {@link DefaultCompactOnLaunchCallback} for details.
         */
        public SyncConfiguration.Builder compactOnLaunch() {
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
        public SyncConfiguration.Builder compactOnLaunch(CompactOnLaunchCallback compactOnLaunch) {
            //noinspection ConstantConditions
            if (compactOnLaunch == null) {
                throw new IllegalArgumentException("A non-null compactOnLaunch must be provided");
            }
            this.compactOnLaunch = compactOnLaunch;
            return this;
        }

        /**
         * The prefix that is prepended to the path in the WebSocket request that initiates a sync
         * connection to MongoDB Realm. The value specified must match the server’s configuration
         * otherwise the device will not be able to create a connection. This value is optional
         * and should only be set if a specific firewall rule requires it.
         *
         * @param urlPrefix The prefix to append to the sync connection url.
         * @see <a href="https://docs.realm.io/platform/guides/learn-realm-sync-and-integrate-with-a-proxy#adding-a-custom-proxy">Adding a custom proxy</a>
         */
        public SyncConfiguration.Builder urlPrefix(String urlPrefix) {
            if (Util.isEmptyString(urlPrefix)) {
                throw new IllegalArgumentException("Non-empty 'urlPrefix' required");
            }
            if (urlPrefix.endsWith("/")) {
                urlPrefix = urlPrefix.substring(0, Math.min(0, urlPrefix.length() - 2));
            }
            this.syncUrlPrefix = urlPrefix;
            return this;
        }

        private String MD5(String in) {
            try {
                MessageDigest digest = MessageDigest.getInstance("MD5");
                byte[] buf = digest.digest(in.getBytes("UTF-8"));
                StringBuilder builder = new StringBuilder();
                for (byte b : buf) {
                    builder.append(String.format(Locale.US, "%02X", b));
                }
                return builder.toString();
            } catch (NoSuchAlgorithmException e) {
                throw new RealmException(e.getMessage());
            } catch (UnsupportedEncodingException e) {
                throw new RealmException(e.getMessage());
            }
        }

        /**
         * Setting this will cause the local Realm file used to synchronize changes to be deleted if the {@link SyncUser}
         * owning this Realm logs out from the device using {@link SyncUser#logOut()}.
         * <p>
         * The default behavior is that the Realm file is allowed to stay behind, making it possible for users to log
         * in again and have access to their data faster.
         */
        /* FIXME: Disable this API since we cannot support it without https://github.com/realm/realm-core/issues/2165
        public Builder deleteRealmOnLogout() {
            this.deleteRealmOnLogout = true;
            return this;
        }
        */

        /**
         * TODO: Removed from the public API until MongoDB Realm correctly supports anything byt MANUAL mode again.
         *
         * Configure the behavior in case of a Client Resync.
         * <p>
         * The default mode is {@link ClientResyncMode#RECOVER_LOCAL_REALM}.
         *
         * @param mode what should happen when a Client Resync happens
         * @see ClientResyncMode for more information about what a Client Resync is.
         */
        Builder clientResyncMode(ClientResyncMode mode) {
            //noinspection ConstantConditions
            if (mode == null) {
                throw new IllegalArgumentException("Non-null 'mode' required.");
            }
            clientResyncMode = mode;
            return this;
        }

        /**
         * Sets the maximum number of live versions in the Realm file before an {@link IllegalStateException} is thrown when
         * attempting to write more data.
         * <p>
         * Realm is capable of concurrently handling many different versions of Realm objects. This can happen if you
         * have a Realm open on many different threads or are freezing objects while data is being written to the file.
         * <p>
         * Under normal circumstances this is not a problem, but if the number of active versions grow too large, it will
         * have a negative effect on the filesize on disk. Setting this parameters can therefore be used to prevent uses of
         * Realm that can result in very large Realms.
         * <p>
         * Note, the version number will also increase when changes from other devices are integrated on this device,
         * so the number of active versions will also depend on what other devices writing to the same Realm are doing.
         *
         * @param number the maximum number of active versions before an exception is thrown.
         * @see <a href="https://realm.io/docs/java/latest/#faq-large-realm-file-size">FAQ</a>
         */
        public Builder maxNumberOfActiveVersions(long number) {
            this.maxNumberOfActiveVersions = number;
            return this;
        }

        /**
         * Sets whether or not calls to {@link Realm#executeTransaction} are allowed from the UI thread.
         * <p>
         * <b>WARNING: Realm does not allow synchronous transactions to be run on the main thread unless users explicitly opt in
         * with this method.</b> We recommend diverting calls to {@code executeTransaction} to non-UI threads or, alternatively,
         * using {@link Realm#executeTransactionAsync}.
         */
        public Builder allowWritesOnUiThread(boolean allowWritesOnUiThread) {
            this.allowWritesOnUiThread = allowWritesOnUiThread;
            return this;
        }

        /**
         * Sets whether or not {@code RealmQueries} are allowed from the UI thread.
         * <p>
         * By default Realm allows queries on the main thread. However, by doing so your application may experience a drop of
         * frames or even ANRs. We recommend diverting queries to non-UI threads or, alternatively, using
         * {@link RealmQuery#findAllAsync()} or {@link RealmQuery#findFirstAsync()}.
         */
        public Builder allowQueriesOnUiThread(boolean allowQueriesOnUiThread) {
            this.allowQueriesOnUiThread = allowQueriesOnUiThread;
            return this;
        }

        /**
         * Creates the RealmConfiguration based on the builder parameters.
         *
         * @return the created {@link SyncConfiguration}.
         * @throws IllegalStateException if the configuration parameters are invalid or inconsistent.
         */
        public SyncConfiguration build() {
            if (serverUrl == null || user == null) {
                throw new IllegalStateException("serverUrl() and user() are both required.");
            }

            // Check that readOnly() was applied to legal configuration. Right now it should only be allowd if
            // an assetFile is configured
            if (readOnly) {
                if (initialDataTransaction != null) {
                    throw new IllegalStateException("This Realm is marked as read-only. " +
                            "Read-only Realms cannot use initialData(Realm.Transaction).");
                }
                if (!waitForServerChanges) {
                    throw new IllegalStateException("A read-only Realms must be provided by some source. " +
                            "'waitForInitialRemoteData()' wasn't enabled which is currently the only supported source.");
                }
            }

            // Set the default Client Resync Mode based on the current type of Realm.
            // Eventually RECOVER_LOCAL_REALM should be the default for all types.
            // FIXME: We should add support back for this.
            if (clientResyncMode == null) {
                clientResyncMode = ClientResyncMode.MANUAL;
            }

            if (rxFactory == null && Util.isRxJavaAvailable()) {
                rxFactory = new RealmObservableFactory(true);
            }

            URI resolvedServerUrl = serverUrl;
            syncUrlPrefix = String.format("/api/client/v2.0/app/%s/realm-sync", user.getApp().getConfiguration().getAppId());

            String absolutePathForRealm = user.getApp().getSync().getAbsolutePathForRealm(user.getId(), partitionValue, filename);
            File realmFile = new File(absolutePathForRealm);

            return new SyncConfiguration(
                    realmFile,
                    null, // assetFile not supported by Sync. See https://github.com/realm/realm-sync/issues/241
                    key,
                    schemaVersion,
                    null, // Custom migrations not supported
                    false, // MigrationNeededException is never thrown
                    durability,
                    createSchemaMediator(modules, debugSchema),
                    rxFactory,
                    coroutinesFactory,
                    initialDataTransaction,
                    readOnly,
                    maxNumberOfActiveVersions,
                    allowWritesOnUiThread,
                    allowQueriesOnUiThread,

                    // Sync Configuration specific
                    user,
                    resolvedServerUrl,
                    errorHandler,
                    clientResetHandler,
                    deleteRealmOnLogout,
                    waitForServerChanges,
                    initialDataTimeoutMillis,
                    sessionStopPolicy,
                    compactOnLaunch,
                    syncUrlPrefix,
                    clientResyncMode,
                    partitionValue
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
