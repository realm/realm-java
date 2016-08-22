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
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.objectserver.session.Session;
import io.realm.objectserver.syncpolicy.AutomaticSyncPolicy;
import io.realm.objectserver.syncpolicy.SyncPolicy;
import io.realm.rx.RxObservableFactory;

/**
 * An {@link SyncConfiguration} is used to setup a replicated .
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
public final class SyncConfiguration extends RealmConfiguration {

    private final String serverUrl;
    private final User user;
    private final boolean autoConnect;
    private final SyncPolicy syncPolicy;
    private final long heartbeatRateMs;

    private SyncConfiguration(Builder builder) {
        super(builder);
        this.serverUrl = builder.serverUrl;
        this.user = builder.user;
        this.autoConnect = builder.autoConnect;
        this.syncPolicy = builder.syncPolicy;
        this.heartbeatRateMs = builder.heartBeatRateMs;
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

    public User getUser() {
        return user;
    }

    public boolean isAutoConnectEnabled() {
        return autoConnect;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    /**
     * ReplicationConfiguration.Builder used to construct instances of a ReplicationConfiguration in a fluent manner.
     */
    public static final class Builder extends RealmConfiguration.Builder {

        private String serverUrl;
        private User user = null;
        private boolean autoConnect = true;
        private SyncPolicy syncPolicy = new AutomaticSyncPolicy();
        private long heartBeatRateMs = TimeUnit.SECONDS.toMillis(280);
        private Session.ErrorHandler errorHandler;
        private Session.ErrorHandler eventHandler;

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
        public Builder name(String name) {
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
        public SyncConfiguration.Builder schemaVersion(long schemaVersion) {
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
         * Enable server side synchronization for this Realm. The name should be a unique URL that identifies this Realm.
         * {@code /~/} can be used as a placeholder for a user id in case the Realm should only be available to one
         * user, e.g. {@code "realm://objectserver.relam.io/~/default.realm"}
         *
         * @param url URL identifying the Realm.
         */
        public Builder serverUrl(String url) {
            // FIXME Validate URL
            this.serverUrl = url;
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
            this.user = user;
            return this;
        }

        /**
         * If this is set. Realm will automatically handle connections to the Realm Object Server as part of the normal
         * Realm lifecycle.
         *
         * Specifically this means that the connection will be established when the first local Realm is opened and
         * closed again after it has been closed and all changes locally have been sent to the server.
         *
         * If this is set to {@code false}, the connection must manually be established using
         * {@link SyncManager#connect(SyncConfiguration)}.
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
         * @see SyncConfiguration.Builder#autoConnect(boolean)
         * @see SyncManager#connect(SyncConfiguration)
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
         * @return the created {@link SyncConfiguration}.
         */
        public SyncConfiguration build() {
            return new SyncConfiguration(this);
        }

        public Builder errorHandler(Session.ErrorHandler errorHandler) {
            this.errorHandler = errorHandler;
            return this;
        }

        public Builder eventHandler(Session.EventHandler sessionEvents) {
            return this;
        }
    }
}