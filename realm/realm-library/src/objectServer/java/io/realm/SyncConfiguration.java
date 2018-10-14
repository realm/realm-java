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

package io.realm;

import android.content.Context;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import io.reactivex.annotations.Beta;
import io.realm.annotations.RealmModule;
import io.realm.exceptions.RealmException;
import io.realm.internal.OsRealmConfig;
import io.realm.internal.RealmProxyMediator;
import io.realm.internal.Util;
import io.realm.internal.sync.permissions.ObjectPermissionsModule;
import io.realm.log.RealmLog;
import io.realm.rx.RealmObservableFactory;
import io.realm.rx.RxObservableFactory;

/**
 * A {@link SyncConfiguration} is used to setup a Realm that can be synchronized between devices using the Realm
 * Object Server.
 * <p>
 * A valid {@link SyncUser} is required to create a {@link SyncConfiguration}. See {@link SyncCredentials} and
 * {@link SyncUser#logInAsync(SyncCredentials, String, SyncUser.Callback)} for more information on how to get a user object.
 * <p>
 * A minimal {@link SyncConfiguration} can be found below.
 * <pre>
 * {@code
 * SyncUser user = SyncUser.current();
 * String url = "realm://myinstance.cloud.realm.io/default";
 * SyncConfiguration config = new SyncConfiguration.Builder(user, url).build();
 * }
 * </pre>
 *
 * Synchronized Realms come in two forms:
 * <ul>
 *     <li>
 *         <b>Query-based synchronization:</b>
 *         This is the default mode. The Realm will only synchronize data you have queried for.
 *         This means the Realm on the device is initially empty and will gradually fill up as
 *         you start to query for data. This is useful if the server side Realm is too large
 *         to fit on the device or contains data from multiple users. Data synchronized this way
 *         can also be removed from the device again without being deleted on the server.
 *     </li>
 *     <li>
 *         <b>Full synchronization</b>
 *         Enable this mode by setting {@link Builder#fullSynchronization()}. In this mode
 *         the entire Realm is synchronized in the background without having to query for
 *         data first. This means that data generally will be available quicker but should only
 *         be used if the server side Realm is small and doesn't contain data the device is not
 *         allowed to see.
 *     </li>
 * </ul>
 * <p>
 * Synchronized Realms only support additive migrations which can be detected and performed automatically, so
 * the following builder options are not accessible compared to a normal Realm:
 *
 * <ul>
 *     <li>{@code deleteRealmIfMigrationNeeded()}</li>
 *     <li>{@code migration(Migration)}</li>
 * </ul>
 *
 * Synchronized Realms are created by using {@link Realm#getInstance(RealmConfiguration)} and
 * {@link Realm#getDefaultInstance()} like ordinary unsynchronized Realms.
 *
 * @see <a href="https://docs.realm.io/platform/using-synced-realms/syncing-data">The docs</a> for more
 * information about the two types of synchronization.
 */
public class SyncConfiguration extends RealmConfiguration {

    // The FAT file system has limitations of length. Also, not all characters are permitted.
    // https://msdn.microsoft.com/en-us/library/aa365247(VS.85).aspx
    static final int MAX_FULL_PATH_LENGTH = 256;
    static final int MAX_FILE_NAME_LENGTH = 255;
    private static final char[] INVALID_CHARS = {'<', '>', ':', '"', '/', '\\', '|', '?', '*'};
    private final URI serverUrl;
    private final SyncUser user;
    private final SyncSession.ErrorHandler errorHandler;
    private final boolean deleteRealmOnLogout;
    private final boolean syncClientValidateSsl;
    @Nullable private final String serverCertificateAssetName;
    @Nullable private final String serverCertificateFilePath;
    private final boolean waitForInitialData;
    private final OsRealmConfig.SyncSessionStopPolicy sessionStopPolicy;
    private final boolean isPartial;
    @Nullable private final String syncUrlPrefix;

    private SyncConfiguration(File directory,
                              String filename,
                              String canonicalPath,
                              @Nullable String assetFilePath,
                              @Nullable byte[] key,
                              long schemaVersion,
                              @Nullable RealmMigration migration,
                              boolean deleteRealmIfMigrationNeeded,
                              OsRealmConfig.Durability durability,
                              RealmProxyMediator schemaMediator,
                              @Nullable RxObservableFactory rxFactory,
                              @Nullable Realm.Transaction initialDataTransaction,
                              boolean readOnly,
                              SyncUser user,
                              URI serverUrl,
                              SyncSession.ErrorHandler errorHandler,
                              boolean deleteRealmOnLogout,
                              boolean syncClientValidateSsl,
                              @Nullable String serverCertificateAssetName,
                              @Nullable String serverCertificateFilePath,
                              boolean waitForInitialData,
                              OsRealmConfig.SyncSessionStopPolicy sessionStopPolicy,
                              boolean isPartial,
                              CompactOnLaunchCallback compactOnLaunch,
                              @Nullable String syncUrlPrefix) {
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
                readOnly,
                compactOnLaunch,
                false
        );

        this.user = user;
        this.serverUrl = serverUrl;
        this.errorHandler = errorHandler;
        this.deleteRealmOnLogout = deleteRealmOnLogout;
        this.syncClientValidateSsl = syncClientValidateSsl;
        this.serverCertificateAssetName = serverCertificateAssetName;
        this.serverCertificateFilePath = serverCertificateFilePath;
        this.waitForInitialData = waitForInitialData;
        this.sessionStopPolicy = sessionStopPolicy;
        this.isPartial = isPartial;
        this.syncUrlPrefix = syncUrlPrefix;
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
        return forRecovery(canonicalPath, encryptionKey, schemaMediator);
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

    static RealmConfiguration forRecovery(String canonicalPath, @Nullable byte[] encryptionKey, RealmProxyMediator schemaMediator) {
        return new RealmConfiguration(null,null, canonicalPath,null, encryptionKey, 0,null, false, OsRealmConfig.Durability.FULL, schemaMediator, null, null, true, null, true);
    }

    static URI resolveServerUrl(URI serverUrl, String userIdentifier) {
        try {
            return new URI(serverUrl.toString().replace("/~/", "/" + userIdentifier + "/"));
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Could not replace '/~/' with a valid user ID.", e);
        }
    }

    /**
     * Creates an automatic default configuration based on the the currently logged in user.
     * <p>
     * This configuration will point to the default Realm on the server where the user was
     * authenticated.
     *
     * @throws IllegalStateException if no user are logged in, or multiple users have. Only one should
     * be logged in when calling this method.
     * @return The constructed {@link SyncConfiguration}.
     * @deprecated use {@link SyncUser#getDefaultConfiguration()} instead.
     */
    @Deprecated
    @Beta
    public static SyncConfiguration automatic() {
        SyncUser user = SyncUser.current();
        if (user == null) {
            throw new IllegalStateException("No user was logged in.");
        }
        return user.getDefaultConfiguration();
    }

    /**
     * Creates an automatic default configuration for the provided user.
     * <p>
     * This configuration will point to the default Realm on the server where the user was
     * authenticated.
     *
     * @throws IllegalArgumentException if no user was provided or the user isn't valid.
     * @return The constructed {@link SyncConfiguration}.
     * @deprecated use {@link SyncUser#getDefaultConfiguration()} instead.
     */
    @Deprecated
    @Beta
    public static SyncConfiguration automatic(SyncUser user) {
        if (user == null) {
            throw new IllegalArgumentException("Non-null 'user' required.");
        }
        if (!user.isValid()) {
            throw new IllegalArgumentException("User is no logger valid.  Log the user in again.");
        }
        return user.getDefaultConfiguration();
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
        if (syncClientValidateSsl != that.syncClientValidateSsl) return false;
        if (!serverUrl.equals(that.serverUrl)) return false;
        if (!user.equals(that.user)) return false;
        if (!errorHandler.equals(that.errorHandler)) return false;
        if (serverCertificateAssetName != null ? !serverCertificateAssetName.equals(that.serverCertificateAssetName) : that.serverCertificateAssetName != null) return false;
        if (serverCertificateFilePath != null ? !serverCertificateFilePath.equals(that.serverCertificateFilePath) : that.serverCertificateFilePath != null) return false;
        if (waitForInitialData != that.waitForInitialData) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + serverUrl.hashCode();
        result = 31 * result + user.hashCode();
        result = 31 * result + errorHandler.hashCode();
        result = 31 * result + (deleteRealmOnLogout ? 1 : 0);
        result = 31 * result + (syncClientValidateSsl ? 1 : 0);
        result = 31 * result + (serverCertificateAssetName != null ? serverCertificateAssetName.hashCode() : 0);
        result = 31 * result + (serverCertificateFilePath != null ? serverCertificateFilePath.hashCode() : 0);
        result = 31 * result + (waitForInitialData ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(super.toString());
        stringBuilder.append("\n");
        stringBuilder.append("serverUrl: " + serverUrl);
        stringBuilder.append("\n");
        stringBuilder.append("user: " + user);
        stringBuilder.append("\n");
        stringBuilder.append("errorHandler: " + errorHandler);
        stringBuilder.append("\n");
        stringBuilder.append("deleteRealmOnLogout: " + deleteRealmOnLogout);
        stringBuilder.append("\n");
        stringBuilder.append("waitForInitialRemoteData: " + waitForInitialData);
        return stringBuilder.toString();
    }

    /**
     * Returns the user.
     *
     * @return the user.
     */
    public SyncUser getUser() {
        return user;
    }

    /**
     * Returns the fully disambiguated URI for the remote Realm i.e., the {@code /~/} placeholder has been replaced
     * by the proper user ID.
     *
     * @return {@link URI} identifying the remote Realm this local Realm is synchronized with.
     */
    public URI getServerUrl() {
        return serverUrl;
    }

    public SyncSession.ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    /**
     * Returns {@code true} if the Realm file must be deleted once the {@link SyncUser} owning it logs out.
     *
     * @return {@code true} if the Realm file must be deleted if the {@link SyncUser} logs out. {@code false} if the file
     *         is allowed to remain behind.
     */
    public boolean shouldDeleteRealmOnLogout() {
        return deleteRealmOnLogout;
    }

    /**
     * Returns the name of certificate stored under the {@code assets}, to be used to validate
     * the TLS connection to the Realm Object Server.
     *
     * @return name of the certificate to be copied from the {@code assets}.
     * @see #getServerCertificateFilePath()
     */
    @Nullable
    public String getServerCertificateAssetName() {
        return serverCertificateAssetName;
    }

    /**
     * Returns the name of the certificate copied from {@code assets} into internal storage, so it
     * can be used to validate the TLS connection to the Realm Object Server.
     *
     * @return absolute path to the certificate.
     * @see #getServerCertificateAssetName()
     */
    @Nullable
    public String getServerCertificateFilePath() {
        return serverCertificateFilePath;
    }

    /**
     * Whether the Realm Object Server certificate should be validated in order
     * to establish a valid TLS connection.
     *
     * @return {@code true} to validate the remote certificate, or {@code false} to bypass certificate validation.
     */
    public boolean syncClientValidateSsl() {
        return syncClientValidateSsl;
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

    @Override
    boolean isSyncConfiguration() {
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
     * Whether this configuration is for a query-based Realm.
     * <p>
     * Query-based synchronization allows a synchronized Realm to be opened in such a way that
     * only objects queried by the user are synchronized to the device.
     *
     * @return {@code true} to open a query-based Realm {@code false} otherwise.
     * @deprecated use {@link #isFullySynchronizedRealm()} instead.
     */
    @Deprecated
    public boolean isPartialRealm() {
        return isPartial;
    }

    /**
     * Returns whether this configuration is for a fully synchronized Realm or not.
     *
     * @see Builder#fullSynchronization() for more details.
     */
    public boolean isFullySynchronizedRealm() {
        return !isPartial;
    }

    /**
     * Returns the url prefix used when establishing a sync connection to the Realm Object Server.
     */
    @Nullable
    public String getUrlPrefix() {
        return syncUrlPrefix;
    }

    /**
     * Builder used to construct instances of a SyncConfiguration in a fluent manner.
     */
    public static final class Builder  {

        private File directory;
        private boolean overrideDefaultFolder = false;
        private String fileName;
        private boolean overrideDefaultLocalFileName = false;
        @Nullable
        private byte[] key;
        private long schemaVersion = 0;
        private HashSet<Object> modules = new HashSet<Object>();
        private HashSet<Class<? extends RealmModel>> debugSchema = new HashSet<Class<? extends RealmModel>>();
        @Nullable
        private RxObservableFactory rxFactory;
        @Nullable
        private Realm.Transaction initialDataTransaction;
        private File defaultFolder;
        private String defaultLocalFileName;
        private OsRealmConfig.Durability durability = OsRealmConfig.Durability.FULL;
        private final Pattern pattern = Pattern.compile("^[A-Za-z0-9_\\-\\.]+$"); // for checking serverUrl
        private boolean readOnly = false;
        private boolean waitForServerChanges = false;
        // sync specific
        private boolean deleteRealmOnLogout = false;
        private URI serverUrl;
        private SyncUser user = null;
        private SyncSession.ErrorHandler errorHandler = SyncManager.defaultSessionErrorHandler;
        private boolean syncClientValidateSsl = true;
        @Nullable
        private String serverCertificateAssetName;
        @Nullable
        private String serverCertificateFilePath;
        private OsRealmConfig.SyncSessionStopPolicy sessionStopPolicy = OsRealmConfig.SyncSessionStopPolicy.AFTER_CHANGES_UPLOADED;
        private boolean isPartial = true; // Partial Synchronization is enabled by default
        private CompactOnLaunchCallback compactOnLaunch;
        private String syncUrlPrefix = null;

        /**
         * Creates an instance of the Builder for the SyncConfiguration. This SyncConfiguration
         * will be for a fully synchronized Realm.
         * <p>
         * Opening a synchronized Realm requires a valid user and an unique URI that identifies that Realm. In URIs,
         * {@code /~/} can be used as a placeholder for a user ID in case the Realm should only be available to one
         * user e.g., {@code "realm://objectserver.realm.io/~/default"}.
         * <p>
         * The URL cannot end with {@code .realm}, {@code .realm.lock} or {@code .realm.management}.
         * <p>
         * The {@code /~/} will automatically be replaced with the user ID when creating the {@link SyncConfiguration}.
         * <p>
         * Moreover, the URI defines the local location on disk. The default location of a synchronized Realm file is
         * {@code /data/data/<packageName>/files/realm-object-server/<user-id>/<last-path-segment>}, but this behavior
         * can be overwritten using {@link #name(String)} and {@link #directory(File)}.
         * <p>
         * Many Android devices are using FAT32 file systems. FAT32 file systems have a limitation that
         * file names cannot be longer than 255 characters. Moreover, the entire URI should not exceed 256 characters.
         * If file name and underlying path are too long to handle for FAT32, a shorter unique name will be generated.
         * See also @{link https://msdn.microsoft.com/en-us/library/aa365247(VS.85).aspx}.
         *
         * @param user the user for this Realm. An authenticated {@link SyncUser} is required to open any Realm managed
         *             by a Realm Object Server.
         * @param uri URI identifying the Realm. If only a path like {@code /~/default} is given, the configuration will
         *            assume the file is located on the same server returned by {@link SyncUser#getAuthenticationUrl()}.
         *
         * @see SyncUser#isValid()
         * @deprecated Use {@link SyncUser#createConfiguration(String)} instead.
         */
        @Deprecated
        public Builder(SyncUser user, String uri) {
            this(BaseRealm.applicationContext, user, uri);
            fullSynchronization();
        }

        Builder(Context context, SyncUser user, String url) {
            //noinspection ConstantConditions
            if (context == null) {
                throw new IllegalStateException("Call `Realm.init(Context)` before creating a SyncConfiguration");
            }
            this.defaultFolder = new File(context.getFilesDir(), "realm-object-server");
            if (Realm.getDefaultModule() != null) {
                this.modules.add(Realm.getDefaultModule());
            }

            validateAndSet(user);
            validateAndSet(url);
        }

        private void validateAndSet(SyncUser user) {
            //noinspection ConstantConditions
            if (user == null) {
                throw new IllegalArgumentException("Non-null `user` required.");
            }
            if (!user.isValid()) {
                throw new IllegalArgumentException("User not authenticated or authentication expired.");
            }
            this.user = user;
        }

        private void validateAndSet(String uri) {
            //noinspection ConstantConditions
            if (uri == null) {
                throw new IllegalArgumentException("Non-null 'uri' required.");
            }

            try {
                serverUrl = new URI(uri);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Invalid URI: " + uri, e);
            }

            try {
                // Automatically set scheme based on auth server if not set or wrongly set
                String serverScheme = serverUrl.getScheme();
                if (serverScheme == null) {
                    String authProtocol = user.getAuthenticationUrl().getProtocol();
                    if (authProtocol.equalsIgnoreCase("https")) {
                        serverScheme = "realms";
                    } else {
                        serverScheme = "realm";
                    }
                } else if (serverScheme.equalsIgnoreCase("http")) {
                    serverScheme = "realm";
                } else if (serverScheme.equalsIgnoreCase("https")) {
                    serverScheme = "realms";
                }

                // Automatically set host if one wasn't defined
                String host = serverUrl.getHost();
                if (host == null) {
                    host = user.getAuthenticationUrl().getHost();
                }

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
                throw new IllegalArgumentException("Invalid URI: " + uri, e);
            }

            // Detect last path segment as it is the default file name
            String path = serverUrl.getPath();
            if (path == null) {
                throw new IllegalArgumentException("Invalid URI: " + uri);
            }

            String[] pathSegments = path.split("/");
            for (int i = 1; i < pathSegments.length; i++) {
                String segment = pathSegments[i];
                if (segment.equals("~")) {
                    continue;
                }
                if (segment.equals("..") || segment.equals(".")) {
                    throw new IllegalArgumentException("The URI has an invalid segment: " + segment);
                }
                Matcher m = pattern.matcher(segment);
                if (!m.matches()) {
                    throw new IllegalArgumentException("The URI must only contain characters 0-9, a-z, A-Z, ., _, and -: " + segment);
                }
            }

            this.defaultLocalFileName = pathSegments[pathSegments.length - 1];

            // Validate filename
            // TODO Lift this restriction on the Object Server
            if (defaultLocalFileName.endsWith(".realm")
                    || defaultLocalFileName.endsWith(".realm.lock")
                    || defaultLocalFileName.endsWith(".realm.management")) {
                throw new IllegalArgumentException("The URI must not end with '.realm', '.realm.lock' or '.realm.management: " + uri);
            }
        }

        /**
         * Sets the local file name for the Realm.
         * This will override the default name defined by the Realm URL.
         *
         * @param filename name of the local file on disk.
         * @throws IllegalArgumentException if file name is {@code null} or empty.
         */
        public Builder name(String filename) {
            //noinspection ConstantConditions
            if (filename == null || filename.isEmpty()) {
                throw new IllegalArgumentException("A non-empty filename must be provided");
            }
            this.fileName = filename;
            this.overrideDefaultLocalFileName = true;
            return this;
        }

        /**
         * Sets the local root directory where synchronized Realm files can be saved.
         * <p>
         * Synchronized Realms will not be saved directly in the provided directory, but instead in a
         * subfolder that matches the path defined by Realm URI. As Realm server URIs are unique
         * this means that multiple users can save their Realms on disk without the risk of them overwriting
         * each other files.
         * <p>
         * The default location is {@code context.getFilesDir()}.
         *
         * @param directory directory on disk where the Realm file can be saved.
         * @throws IllegalArgumentException if the directory is not valid.
         */
        public Builder directory(File directory) {
            //noinspection ConstantConditions
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
         * Sets the {@value io.realm.RealmConfiguration#KEY_LENGTH} bytes key used to encrypt and decrypt the Realm file.
         *
         * @param key the encryption key.
         * @throws IllegalArgumentException if key is invalid.
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
         * Sets the error handler used by this configuration. This will override any handler set by calling
         * {@link SyncManager#setDefaultSessionErrorHandler(SyncSession.ErrorHandler)}.
         * <p>
         * Only errors not handled by the defined {@code SyncPolicy} will be reported to this error handler.
         *
         * @param errorHandler error handler used to report back errors when communicating with the Realm Object Server.
         * @throws IllegalArgumentException if {@code null} is given as an error handler.
         */
        public Builder errorHandler(SyncSession.ErrorHandler errorHandler) {
            //noinspection ConstantConditions
            if (errorHandler == null) {
                throw new IllegalArgumentException("Non-null 'errorHandler' required.");
            }
            this.errorHandler = errorHandler;
            return this;
        }

        /**
         * Provides the trusted root certificate(s) authority (CA) in {@code PEM} format, that should be used to
         * validate the TLS connections to the Realm Object Server.
         * <p>
         * The file should be stored under {@code assets}, it will be copied at runtime into the internal storage.
         * <p>
         * Note: This is similar to passing the parameter {@code CAfile} to {@code SSL_CTX_load_verify_locations},
         *       Therefore it is recommended to include only the root CA you trust, and not the entire list of root CA
         *       as this file will be loaded at runtime.
         *
         *       It is your responsibility to download and verify the correct {@code PEM} for the root CA you trust.
         *       An existing list by Mozilla exist that could be used https://mozillacaprogram.secure.force.com/CA/IncludedCACertificateReportPEMCSV
         *
         * @param filename the path under {@code assets} to the root CA.
         * @see <a href="https://www.openssl.org/docs/man1.0.2/ssl/SSL_CTX_load_verify_locations.html">SSL_CTX_load_verify_locations</a>
         */
        public Builder trustedRootCA(String filename) {
            //noinspection ConstantConditions
            if (filename == null || filename.isEmpty()) {
                throw new IllegalArgumentException("A non-empty filename must be provided");
            }
            this.serverCertificateAssetName = filename;
            return this;
        }

        /**
         * This will disable TLS certificate verification for the remote Realm Object Server.
         * It is not recommended to use this in production.
         * <p>
         * This might be useful in non-production environments where you use a self-signed certificate
         * for testing.
         */
        public Builder disableSSLVerification() {
            this.syncClientValidateSsl = false;
            return this;
        }

        /*
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
         * Setting this will open a query-based Realm.
         *
         * @see #isPartialRealm()
         * @deprecated Use {@link SyncUser#createConfiguration(String)} instead.
         */
        @Deprecated
        public SyncConfiguration.Builder partialRealm() {
            this.isPartial = true;
            return this;
        }

        /**
         * Define this Realm as a fully synchronized Realm.
         * <p>
         * Full synchronization, unlike the default query-based synchronization, will transparently
         * synchronize the entire Realm without needing to query for the data. This option is
         * useful if the serverside Realm is small and all the data in the Realm should be
         * available to the user.
         */
        public SyncConfiguration.Builder fullSynchronization() {
            this.isPartial = false;
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
         * The prefix that is prepended to the path in the HTTP request that initiates a sync
         * connection to the Realm Object Server. The value specified must match the server’s
         * configuration otherwise the device will not be able to create a connection. If no value
         * is specified then the default {@code /realm-sync} path is used.
         *
         * @param urlPrefix The prefix to append to the sync connection url.
         * @see <a href="https://docs.realm.io/platform/guides/learn-realm-sync-and-integrate-with-a-proxy#adding-a-custom-proxy">Adding a custom proxy</a>
         */
        public SyncConfiguration.Builder urlPrefix(String urlPrefix) {
            if (Util.isEmptyString(urlPrefix)) {
                throw new IllegalArgumentException("Non-empty 'urlPrefix' required");
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

            // Check if the user has an identifier, if not, it cannot use /~/.
            if (serverUrl.toString().contains("/~/") && user.getIdentity() == null) {
                throw new IllegalStateException("The serverUrl contains a /~/, but the user does not have an identity." +
                        " Most likely it hasn't been authenticated yet or has been created directly from an" +
                        " access token. Use a path without /~/.");
            }

            if (rxFactory == null && isRxJavaAvailable()) {
                rxFactory = new RealmObservableFactory();
            }

            // Determine location on disk
            // Use the serverUrl + user to create a unique filepath unless it has been explicitly overridden.
            // <rootDir>/<userIdentifier>/<serverPath>/<serverFileNameOrOverriddenFileName>
            URI resolvedServerUrl = resolveServerUrl(serverUrl, user.getIdentity());
            File rootDir = overrideDefaultFolder ? directory : defaultFolder;
            String realmPathFromRootDir = user.getIdentity() + "/" + getServerPath(resolvedServerUrl);
            File realmFileDirectory = new File(rootDir, realmPathFromRootDir);

            String realmFileName = overrideDefaultLocalFileName ? fileName : defaultLocalFileName;
            String fullPathName = realmFileDirectory.getAbsolutePath() + File.pathSeparator + realmFileName;
            // full path must not exceed 256 characters (on FAT)
            if (fullPathName.length() > MAX_FULL_PATH_LENGTH) {
                // path is too long, so we make the file name shorter
                realmFileName = MD5(realmFileName);
                fullPathName = realmFileDirectory.getAbsolutePath() + File.pathSeparator + realmFileName;
                if (fullPathName.length() > MAX_FULL_PATH_LENGTH) {
                    // use rootDir/userIdentify as directory instead as it is shorter
                    realmFileDirectory = new File(rootDir, user.getIdentity());
                    fullPathName = realmFileDirectory.getAbsolutePath() + File.pathSeparator + realmFileName;
                    if (fullPathName.length() > MAX_FULL_PATH_LENGTH) { // we are out of ideas
                        throw new IllegalStateException(String.format(Locale.US,
                                "Full path name must not exceed %d characters: %s",
                                MAX_FULL_PATH_LENGTH, fullPathName));
                    }
                }
            }

            if (realmFileName.length() > MAX_FILE_NAME_LENGTH) {
                throw new IllegalStateException(String.format(Locale.US,
                        "File name exceed %d characters: %d", MAX_FILE_NAME_LENGTH,
                        realmFileName.length()));
            }

            // substitute invalid characters
            for (char c : INVALID_CHARS) {
                realmFileName = realmFileName.replace(c, '_');
            }

            // Create the folder on disk (if needed)
            if (!realmFileDirectory.exists() && !realmFileDirectory.mkdirs()) {
                throw new IllegalStateException("Could not create directory for saving the Realm: " + realmFileDirectory);
            }

            if (!Util.isEmptyString(serverCertificateAssetName)) {
                if (syncClientValidateSsl) {
                    // Create the path where the serverCertificateAssetName will be copied
                    // so we can supply it to the Sync client.
                    // using getRealmDirectory avoid file collision between same filename from different users (Realms)
                    String fileName = serverCertificateAssetName.substring(serverCertificateAssetName.lastIndexOf(File.separatorChar) + 1);
                    serverCertificateFilePath = new File(realmFileDirectory, fileName).getAbsolutePath();
                } else {
                    RealmLog.warn("SSL Verification is disabled, the provided server certificate will not be used.");
                }
            }

            // If query based sync is enabled, also add support for Object Level Permissions
            if (isPartial) {
                addModule(new ObjectPermissionsModule());
            }

            return new SyncConfiguration(
                    // Realm Configuration options
                    realmFileDirectory,
                    realmFileName,
                    getCanonicalPath(new File(realmFileDirectory, realmFileName)),
                    null, // assetFile not supported by Sync. See https://github.com/realm/realm-sync/issues/241
                    key,
                    schemaVersion,
                    null, // Custom migrations not supported
                    false, // MigrationNeededException is never thrown
                    durability,
                    createSchemaMediator(modules, debugSchema),
                    rxFactory,
                    initialDataTransaction,
                    readOnly,

                    // Sync Configuration specific
                    user,
                    resolvedServerUrl,
                    errorHandler,
                    deleteRealmOnLogout,
                    syncClientValidateSsl,
                    serverCertificateAssetName,
                    serverCertificateFilePath,
                    waitForServerChanges,
                    sessionStopPolicy,
                    isPartial,
                    compactOnLaunch,
                    syncUrlPrefix
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
