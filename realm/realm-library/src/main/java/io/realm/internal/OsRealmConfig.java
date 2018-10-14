/*
 * Copyright 2017 Realm Inc.
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

package io.realm.internal;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import javax.annotation.Nullable;

import io.realm.CompactOnLaunchCallback;
import io.realm.RealmConfiguration;
import io.realm.log.RealmLog;

/**
 * Java wrapper of Object Store's Realm::Config.
 */
public class OsRealmConfig implements NativeObject {

    public enum Durability {
        FULL(0),
        MEM_ONLY(1);

        final int value;

        Durability(int value) {
            this.value = value;
        }
    }

    public enum SchemaMode {
        SCHEMA_MODE_AUTOMATIC(SCHEMA_MODE_VALUE_AUTOMATIC),
        SCHEMA_MODE_IMMUTABLE(SCHEMA_MODE_VALUE_IMMUTABLE),
        SCHEMA_MODE_READONLY(SCHEMA_MODE_VALUE_READONLY),
        SCHEMA_MODE_RESET_FILE(SCHEMA_MODE_VALUE_RESET_FILE),
        SCHEMA_MODE_ADDITIVE(SCHEMA_MODE_VALUE_ADDITIVE),
        SCHEMA_MODE_MANUAL(SCHEMA_MODE_VALUE_MANUAL);

        final byte value;

        SchemaMode(byte value) {
            this.value = value;
        }

        public byte getNativeValue() {
            return value;
        }
    }

    public enum SyncSessionStopPolicy {
        IMMEDIATELY(SYNCSESSION_STOP_POLICY_VALUE_IMMEDIATELY), // Immediately stop the session as soon as all Realms/Sessions go out of scope.
        LIVE_INDEFINITELY(SYNCSESSION_STOP_POLICY_VALUE_LIVE_INDEFINETELY),   // Never stop the session.
        AFTER_CHANGES_UPLOADED(SYNCSESSION_STOP_POLICY_VALUE_AFTER_CHANGES_UPLOADED); // Once all Realms/Sessions go out of scope, wait for uploads to complete and stop.

        final byte value;

        SyncSessionStopPolicy(byte value) {
            this.value = value;
        }

        public byte getNativeValue() {
            return value;
        }
    }

    /**
     * Builder class for creating {@code OsRealmConfig}. The {@code OsRealmConfig} instance should only be created by
     * {@link OsSharedRealm}.
     */
    public static class Builder {
        private RealmConfiguration configuration;
        private OsSchemaInfo schemaInfo = null;
        private OsSharedRealm.MigrationCallback migrationCallback = null;
        private OsSharedRealm.InitializationCallback initializationCallback = null;
        private boolean autoUpdateNotification = false;

        /**
         * Initialize a {@link OsRealmConfig.Builder} with a given {@link RealmConfiguration}.
         */
        public Builder(RealmConfiguration configuration) {
            this.configuration = configuration;
        }

        /**
         * Sets the schema which Object Store initializes the Realm with.
         *
         * @param schemaInfo {@code null} to initialize the Realm in dynamic schema mode. Otherwise Object Store will
         *                   initialize the Realm with given schema and handle migration with it.
         * @return this {@link OsRealmConfig.Builder}.
         */
        public Builder schemaInfo(@Nullable OsSchemaInfo schemaInfo) {
            this.schemaInfo = schemaInfo;
            return this;
        }

        /**
         * Sets the callback when manual migration needed.
         *
         * @param migrationCallback callback to be set.
         * @return this {@link OsRealmConfig.Builder}.
         */
        public Builder migrationCallback(@Nullable OsSharedRealm.MigrationCallback migrationCallback) {
            this.migrationCallback = migrationCallback;
            return this;
        }

        /**
         * Sets the callback which will be called when the Realm is created and the schema has just been initialized.
         *
         * @param initializationCallback the callback to be set.
         * @return this {@link OsRealmConfig.Builder}.
         */
        public Builder initializationCallback(@Nullable OsSharedRealm.InitializationCallback initializationCallback) {
            this.initializationCallback = initializationCallback;
            return this;
        }

        /**
         * Set to {@code false} to disable the background worker thread for producing change notifications. Change
         * notifications are enabled by default.
         *
         * @param autoUpdateNotification {@code false} to disable. {@code true} to enable it.
         * @return this {@link OsRealmConfig.Builder}.
         */
        public Builder autoUpdateNotification(boolean autoUpdateNotification) {
            this.autoUpdateNotification = autoUpdateNotification;
            return this;
        }

        // Package private because of the OsRealmConfig needs to carry the NativeContext. This should only be called
        // by the OsSharedRealm.
        OsRealmConfig build() {
            return new OsRealmConfig(configuration, autoUpdateNotification, schemaInfo,
                    migrationCallback, initializationCallback);
        }
    }

    private static final byte SCHEMA_MODE_VALUE_AUTOMATIC = 0;
    private static final byte SCHEMA_MODE_VALUE_IMMUTABLE = 1;
    private static final byte SCHEMA_MODE_VALUE_READONLY = 2;
    private static final byte SCHEMA_MODE_VALUE_RESET_FILE = 3;
    private static final byte SCHEMA_MODE_VALUE_ADDITIVE = 4;
    private static final byte SCHEMA_MODE_VALUE_MANUAL = 5;
    private static final byte SYNCSESSION_STOP_POLICY_VALUE_IMMEDIATELY = 0;
    private static final byte SYNCSESSION_STOP_POLICY_VALUE_LIVE_INDEFINETELY = 1;
    private static final byte SYNCSESSION_STOP_POLICY_VALUE_AFTER_CHANGES_UPLOADED = 2;

    private final static long nativeFinalizerPtr = nativeGetFinalizerPtr();

    private final RealmConfiguration realmConfiguration;
    private final URI resolvedRealmURI;
    private final long nativePtr;
    // Every OsSharedRealm instance has to be created from an OsRealmConfig instance. And the OsSharedRealm's NativeContext
    // object will be the same as the context here. This is because of we may create different OsSharedRealm instances
    // with different shared_ptrs which are point to the same SharedGroup object. It could happen when we create
    // OsSharedRealm for migration/initialization callback. The context has to be the same object for those cases for
    // core destructor's thread safety.
    private final NativeContext context = new NativeContext();

    // Hold a ref to callbacks to make sure they won't be GCed before getting called.
    // JNI should only hold a weak ref in the lambda functions.
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final CompactOnLaunchCallback compactOnLaunchCallback;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final OsSharedRealm.MigrationCallback migrationCallback;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final OsSharedRealm.InitializationCallback initializationCallback;

    private OsRealmConfig(final RealmConfiguration config,
                          boolean autoUpdateNotification,
                          @Nullable OsSchemaInfo schemaInfo,
                          @Nullable OsSharedRealm.MigrationCallback migrationCallback,
                          @Nullable OsSharedRealm.InitializationCallback initializationCallback) {
        this.realmConfiguration = config;
        this.nativePtr = nativeCreate(config.getPath(), false, true);
        NativeContext.dummyContext.addReference(this);

        // Retrieve Sync settings first. We need syncRealmUrl to identify if this is a SyncConfig
        Object[] syncConfigurationOptions = ObjectServerFacade.getSyncFacadeIfPossible().getSyncConfigurationOptions(realmConfiguration);
        String syncUserIdentifier = (String) syncConfigurationOptions[0];
        String syncRealmUrl = (String) syncConfigurationOptions[1];
        String syncRealmAuthUrl = (String) syncConfigurationOptions[2];
        String syncRefreshToken = (String) syncConfigurationOptions[3];
        boolean syncClientValidateSsl = (Boolean.TRUE.equals(syncConfigurationOptions[4]));
        String syncSslTrustCertificatePath = (String) syncConfigurationOptions[5];
        Byte sessionStopPolicy = (Byte) syncConfigurationOptions[6];
        boolean isPartial = (Boolean.TRUE.equals(syncConfigurationOptions[7]));
        String urlPrefix = (String)(syncConfigurationOptions[8]);
        String customAuthorizationHeaderName = (String)(syncConfigurationOptions[9]);

        // Convert the headers into a String array to make it easier to send through JNI
        // [key1, value1, key2, value2, ...]
        //noinspection unchecked
        Map<String, String> customHeadersMap = (Map<String, String>) (syncConfigurationOptions[10]);
        String[] customHeaders = new String[customHeadersMap != null ? customHeadersMap.size() * 2 : 0];
        if (customHeadersMap != null) {
            int i = 0;
            for (Map.Entry<String, String> entry : customHeadersMap.entrySet()) {
                customHeaders[i] = entry.getKey();
                customHeaders[i + 1] = entry.getValue();
                i = i + 2;
            }
        }

        // Set encryption key
        byte[] key = config.getEncryptionKey();
        if (key != null) {
            nativeSetEncryptionKey(nativePtr, key);
        }

        // Set durability
        nativeSetInMemory(nativePtr, config.getDurability() == Durability.MEM_ONLY);

        // Set auto update notification
        nativeEnableChangeNotification(nativePtr, autoUpdateNotification);

        // Set schema related params.
        SchemaMode schemaMode = SchemaMode.SCHEMA_MODE_MANUAL;
        if (config.isRecoveryConfiguration()) {
            schemaMode = SchemaMode.SCHEMA_MODE_IMMUTABLE;
        } else if (config.isReadOnly()) {
            schemaMode = SchemaMode.SCHEMA_MODE_READONLY;
        } else if (syncRealmUrl != null) {
            schemaMode = SchemaMode.SCHEMA_MODE_ADDITIVE;
        } else if (config.shouldDeleteRealmIfMigrationNeeded()) {
            schemaMode = SchemaMode.SCHEMA_MODE_RESET_FILE;
        }
        final long schemaVersion = config.getSchemaVersion();
        final long nativeSchemaPtr = schemaInfo == null ? 0 : schemaInfo.getNativePtr();
        this.migrationCallback = migrationCallback;
        nativeSetSchemaConfig(nativePtr, schemaMode.getNativeValue(), schemaVersion, nativeSchemaPtr, migrationCallback);

        // Compact on launch
        this.compactOnLaunchCallback = config.getCompactOnLaunchCallback();
        if (compactOnLaunchCallback != null) {
            nativeSetCompactOnLaunchCallback(nativePtr, compactOnLaunchCallback);
        }

        // Initial data transaction
        this.initializationCallback = initializationCallback;
        if (initializationCallback != null) {
            nativeSetInitializationCallback(nativePtr, initializationCallback);
        }

        URI resolvedRealmURI  = null;
        // Set sync config
        if (syncRealmUrl != null) {
            String resolvedSyncRealmUrl = nativeCreateAndSetSyncConfig(
                    nativePtr,
                    syncRealmUrl,
                    syncRealmAuthUrl,
                    syncUserIdentifier,
                    syncRefreshToken,
                    isPartial,
                    sessionStopPolicy,
                    urlPrefix,
                    customAuthorizationHeaderName,
                    customHeaders);
            try {
                resolvedRealmURI = new URI(resolvedSyncRealmUrl);
            } catch (URISyntaxException e) {
                RealmLog.error(e, "Cannot create a URI from the Realm URL address");
            }
            nativeSetSyncConfigSslSettings(nativePtr, syncClientValidateSsl, syncSslTrustCertificatePath);
        }
        this.resolvedRealmURI = resolvedRealmURI;
    }

    @Override
    public long getNativePtr() {
        return nativePtr;
    }

    @Override
    public long getNativeFinalizerPtr() {
        return nativeFinalizerPtr;
    }

    public RealmConfiguration getRealmConfiguration() {
        return realmConfiguration;
    }

    public URI getResolvedRealmURI() {
        return resolvedRealmURI;
    }

    NativeContext getContext() {
        return context;
    }

    private static native long nativeCreate(String path, boolean enableCache, boolean enableFormatUpdate);

    private static native void nativeSetEncryptionKey(long nativePtr, byte[] key);

    private static native void nativeSetInMemory(long nativePtr, boolean inMem);

    private native void nativeSetSchemaConfig(long nativePtr, byte schemaMode, long schemaVersion,
                                              long schemaInfoPtr,
                                              @Nullable OsSharedRealm.MigrationCallback migrationCallback);

    private static native void nativeSetCompactOnLaunchCallback(long nativePtr, CompactOnLaunchCallback callback);

    private native void nativeSetInitializationCallback(long nativePtr, OsSharedRealm.InitializationCallback callback);

    private static native void nativeEnableChangeNotification(long nativePtr, boolean enableNotification);

    private static native String nativeCreateAndSetSyncConfig(long nativePtr, String syncRealmUrl, String authUrl,
                                                              String userId, String refreshToken, boolean isPartial,
                                                              byte sessionStopPolicy, String urlPrefix,
                                                              String customAuthorizationHeaderName,
                                                              String[] customHeaders);

    private static native void nativeSetSyncConfigSslSettings(long nativePtr,
                                                              boolean validateSsl, String trustCertificatePath);

    private static native long nativeGetFinalizerPtr();
}
