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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.objectserver.credentials.Credentials;
import io.realm.objectserver.credentials.CredentialsHandler;
import io.realm.objectserver.session.Session;
import io.realm.objectserver.syncpolicy.AutomaticSyncPolicy;
import io.realm.objectserver.syncpolicy.SyncPolicy;
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
public final class ObjectServerConfiguration extends RealmConfiguration {

    private final String remoteUrl;
    private final List<Credentials> credentials;
    private final List<CredentialsHandler> credentialsHandlers;
    private final RealmConfiguration realmConfig;
    private final boolean autoConnect;
    private final SyncPolicy syncPolicy;
    private final long heartbeatRateMs;

    private ObjectServerConfiguration(Builder builder) {
        super(builder);
        this.realmConfig = builder.realmConfig;
        this.remoteUrl = builder.remoteRealmUrl;
        this.credentials = builder.credentials;
        this.credentialsHandlers = builder.credentialsHandlers;
        this.autoConnect = builder.autoConnect;
        this.syncPolicy = builder.syncPolicy;
        this.heartbeatRateMs = builder.heartBeatRateMs;
    }

    /**
     * Returns the server side URL used to sync this Realm across devices.
     *
     * @return URL of the Realm Sync server.
     */
    public String getRemoteUrl() {
        return remoteUrl;
    }

    @Override
    public boolean equals(Object obj) {
        return true; // TODO;
    }

    @Override
    public int hashCode() {
        return 31; // TODO
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        // TODO
        return stringBuilder.toString();
    }

    public SyncPolicy getSyncPolicy() {
        return syncPolicy;
    }

    public List<Credentials> getCredentials() {
        return credentials;
    }

    public List<CredentialsHandler> getCredentialHanders() {
        return credentialsHandlers;
    }

    /**
     * ReplicationConfiguration.Builder used to construct instances of a ReplicationConfiguration in a fluent manner.
     */
    public static final class Builder extends RealmConfiguration.Builder {

        private RealmConfiguration realmConfig;
        private String remoteRealmUrl;
        private final List<Credentials> credentials = new ArrayList<Credentials>();
        private final List<CredentialsHandler> credentialsHandlers = new ArrayList<CredentialsHandler>();
        private boolean autoConnect = true;
        private SyncPolicy syncPolicy = new AutomaticSyncPolicy();
        private long heartBeatRateMs = TimeUnit.SECONDS.toMillis(280);

        /**
         * {@inheritDoc}
         */
        public Builder(Context context) {
            super(context);
        }

        /**
         * {@inheritDoc}
         */
        public Builder(Context context, File folder) {
            super(context, folder);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Builder name(String filename) {
            super.name(filename);
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
        public ObjectServerConfiguration.Builder schemaVersion(long schemaVersion) {
            super.schemaVersion(schemaVersion);
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public RealmConfiguration.Builder deleteRealmIfMigrationNeeded() {
            super.deleteRealmIfMigrationNeeded();
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public RealmConfiguration.Builder inMemory() {
            super.inMemory();
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public RealmConfiguration.Builder modules(Object baseModule, Object... additionalModules) {
            super.modules(baseModule, additionalModules);
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public RealmConfiguration.Builder rxFactory(RxObservableFactory factory) {
            super.rxFactory(factory);
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public RealmConfiguration.Builder initialData(Realm.Transaction transaction) {
            super.initialData(transaction);
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public RealmConfiguration.Builder assetFile(Context context, String assetFile) {
            super.assetFile(context, assetFile);
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
         * Enable server side synchronization for this Realm. The URL should point to an endpoint exposed by a
         * Realm Object Server.
         *
         * @param realmUrl URL identifying the remote Realm.
         */
        public Builder remoteRealm(String realmUrl) {
            remoteRealmUrl = realmUrl;
            return this;
        }

        /**
         * Sets the {@link Credentials} used when authenticating access to the remote Realm.
         * Use this methods if you acquired the credentials up-front as part of a custom login flow.
         *
         * Multiple credentials can be added. Each will be tried in turn, but the first one to be verified will be used
         * as a basis for determining which permissions are available.
         *
         * @param credentials Credentials used to verify access and permissions granted for the remote Realm.
         * @see <a href="TODO_LINK">Realm Object Server Credentials</a>
         */
        public Builder credentials(Credentials credentials) {
            if (credentials == null) {
                throw new IllegalArgumentException("Non-empty 'credentials' is required");
            }

            this.credentials.add(credentials);
            return this;
        }

        /**
         * Sets an {@link CredentialsHandler} that will be called whenever the Realm Object Server
         * needs to authenticate access to a remote Realm.
         *
         * This callback will always happen on the UI thread. It is then up to the receiving code to acquire these
         * credentials and give them back using {@link Session#setCredentials(Credentials)}.
         *
         * Multiple credential handlers can be added. Each will be tried in turn, but the first one to be verified will
         * be used as a basis for determining which permissions are available.
         *
         * @param credentialsHandler the Handler called when credentials are missing.
         * @see <a href="TODO_LINK">Realm Object Server Credentials</a>
         */
        public Builder credentials(CredentialsHandler credentialsHandler) {
            if (credentialsHandler == null) {
                throw new IllegalArgumentException("'credentialsHandler' is required.");
            }

            this.credentialsHandlers.add(credentialsHandler);
            return this;
        }

        /**
         * If this is set. Realm will automatically handle connections to the Realm Object Server as part of the normal
         * Realm lifecycle.
         *
         * Specifically this means that the connection will be established when the first local Realm is opened and
         * closed again after it has been closed and all changes locally and remotely have been synchronized.
         *
         * If this is to {@code false}, the connection must manually be established using
         * {@link ObjectServer#connect(ObjectServerConfiguration)}.
         *
         * The default value is {@code true}.
         */
        public Builder autoConnect(boolean autoConnect) {
            this.autoConnect = autoConnect;
            return this;
        }

        /**
         * Sets the sync policy used to control when changes should be synchronized with the remote Realm.
         * The {@link SyncPolicy} only takes effect after a Realm have been <i>bound</i> to the remote Realm.
         *
         * The default value policy is a {@link AutomaticSyncPolicy}.
         *
         * TODO Think about how a sync policy could also control if _any_ connection is made, not just sync changes.
         * TODO Does the core SyncClient support starting/stopping sync yet?
         *
         * @param syncPolicy
         * @return
         *
         * @see ObjectServerConfiguration.Builder#autoConnect(boolean)
         * @see ObjectServer#connect(ObjectServerConfiguration)
         */
        public Builder syncPolicy(SyncPolicy syncPolicy) {
            this.syncPolicy = syncPolicy;
            return this;
        }

        /**
         * Sets the heartbeat used by the Object Server. The heartbeat is primarily used to determine if an device is
         * online or present, but it is also used by the underlying protocol to keep the connection to the Object
         * Server alive. A live connection is important if changes must be synchronized as fast as possible.
         *
         * For apps that want to display if another user is "present", the maximum error margin is also the heart rate.
         * Setting a fast heartbeat can negatively effect users battery.
         *
         * The default value is 280 seconds.
         *
         * @param heartRate H
         * @param unit the unit of time
         *
         * @see <a href="TODO_LINK">User precense and heart rate</a>
         */
        public Builder heartbeat(long heartRate, TimeUnit unit) {
            this.heartBeatRateMs = TimeUnit.MILLISECONDS.convert(heartRate, unit);
            return this;
        }

//        /**
//         * Sets the error handler used by this configuration.
//         * This will override any handler set by calling {@link RealmObjectServer#setGlobalErrorHandler(SyncErrorHandler)}.
//         *
//         * @param errorHandler error handler used to report back errors when communicating with the Realm Object Server.
//         */
//        public Builder errorHandler(SyncErrorHandler errorHandler) {
//            return this;
//        }
//
//        /**
//         * Sets the log level used by this configuration.
//         * This will override any handler set by calling {@link RealmObjectServer#setGlobalErrorHandler(SyncErrorHandler)}.
//         *
//         * @param logLevel the about of inerror handler used to report back errors when communicating with the Realm Object Server.
//         */
//        public Builder logLevel(SyncLogLevel logLevel) {
//            // TODO What is this used for?
//            return this;
//        }
//
//        public Builder changeListener(SyncEventHandler changeListener) {
//
//        }

        /**
         * Creates the RealmConfiguration based on the builder parameters.
         *
         * @return the created {@link ObjectServerConfiguration}.
         */
        public ObjectServerConfiguration build() {
            return new ObjectServerConfiguration(this);
        }
    }
}