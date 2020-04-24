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

package io.realm.internal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import org.bson.BsonValue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.realm.RealmApp;
import io.realm.RealmConfiguration;
import io.realm.RealmUser;
import io.realm.SyncConfiguration;
import io.realm.RealmSync;
import io.realm.exceptions.DownloadingRealmInterruptedException;
import io.realm.exceptions.RealmException;
import io.realm.internal.android.AndroidCapabilities;
import io.realm.internal.network.NetworkStateReceiver;
import io.realm.internal.objectstore.OsAsyncOpenTask;

@SuppressWarnings({"unused", "WeakerAccess"}) // Used through reflection. See ObjectServerFacade
@Keep
public class SyncObjectServerFacade extends ObjectServerFacade {

    private static final String WRONG_TYPE_OF_CONFIGURATION =
            "'configuration' has to be an instance of 'SyncConfiguration'.";
    @SuppressLint("StaticFieldLeak") //
    private static Context applicationContext;
    private static volatile Method removeSessionMethod;

    @Override
    public void initialize(Context context, String userAgent) {
        if (applicationContext == null) {
            applicationContext = context;
            applicationContext.registerReceiver(new NetworkStateReceiver(),
                    new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    @Override
    public void realmClosed(RealmConfiguration configuration) {
        // Last Thread using the specified configuration is closed
        // delete the wrapped Java session
        if (configuration instanceof SyncConfiguration) {
            SyncConfiguration syncConfig = (SyncConfiguration) configuration;
            invokeRemoveSession(syncConfig);
        } else {
            throw new IllegalArgumentException(WRONG_TYPE_OF_CONFIGURATION);
        }
    }

    @Override
    public Object[] getSyncConfigurationOptions(RealmConfiguration config) {
        if (config instanceof SyncConfiguration) {
            SyncConfiguration syncConfig = (SyncConfiguration) config;
            RealmUser user = syncConfig.getUser();
            RealmApp app = user.getApp();
            String rosServerUrl = syncConfig.getServerUrl().toString();
            String rosUserIdentity = user.getId();
            String syncRealmAuthUrl = user.getApp().getConfiguration().getBaseUrl().toString();
            String syncUserRefreshToken = user.getRefreshToken();
            String syncUserAccessToken = user.getAccessToken();
            byte sessionStopPolicy = syncConfig.getSessionStopPolicy().getNativeValue();
            String urlPrefix = syncConfig.getUrlPrefix();
            String customAuthorizationHeaderName = app.getConfiguration().getAuthorizationHeaderName();
            Map<String, String> customHeaders = app.getConfiguration().getCustomRequestHeaders();

            // Temporary work-around for serializing supported bson values
            BsonValue val = syncConfig.getPartitionValue();
            String partitionValue = null;
            if (val.isString()) {
                partitionValue = "\"" + val.asString().getValue() + "\"";
            } else if (val.isInt32()) {
                partitionValue = "{ \"$bsonInt\" : " + val.asInt32().intValue() + " }";
            } else if (val.isInt64()) {
                partitionValue = "{ \"$bsonLong\" : " + val.asInt64().longValue() + " }";
            } else if (val.isObjectId()) {
                partitionValue = "{ \"$oid\" : " + val.asObjectId().toString() + " }";
            } else {
                throw new IllegalArgumentException("Unsupported type: " + val);
            }
            Object[] configObj = new Object[SYNC_CONFIG_OPTIONS];
            configObj[0] = rosUserIdentity;
            configObj[1] = rosServerUrl;
            configObj[2] = syncRealmAuthUrl;
            configObj[3] = syncUserRefreshToken;
            configObj[4] = syncUserAccessToken;
            configObj[5] = syncConfig.syncClientValidateSsl();
            configObj[6] = syncConfig.getServerCertificateFilePath();
            configObj[7] = sessionStopPolicy;
            configObj[8] = urlPrefix;
            configObj[9] = customAuthorizationHeaderName;
            configObj[10] = customHeaders;
            configObj[11] = OsRealmConfig.CLIENT_RESYNC_MODE_MANUAL;
            configObj[12] = partitionValue;
            configObj[13] = app.getSync();
            return configObj;
        } else {
            return new Object[SYNC_CONFIG_OPTIONS];
        }
    }

    public static Context getApplicationContext() {
        return applicationContext;
    }

    @Override
    public void wrapObjectStoreSessionIfRequired(OsRealmConfig config) {
        if (config.getRealmConfiguration() instanceof SyncConfiguration) {
            SyncConfiguration syncConfig = (SyncConfiguration) config.getRealmConfiguration();
            RealmApp app = syncConfig.getUser().getApp();
            app.getSync().getOrCreateSession(syncConfig);
        }
    }

    @Override
    public String getSyncServerCertificateAssetName(RealmConfiguration configuration) {
        if (configuration instanceof SyncConfiguration) {
            SyncConfiguration syncConfig = (SyncConfiguration) configuration;
            return syncConfig.getServerCertificateAssetName();
        } else {
            throw new IllegalArgumentException(WRONG_TYPE_OF_CONFIGURATION);
        }
    }

    @Override
    public String getSyncServerCertificateFilePath(RealmConfiguration configuration) {
        if (configuration instanceof SyncConfiguration) {
            SyncConfiguration syncConfig = (SyncConfiguration) configuration;
            return syncConfig.getServerCertificateFilePath();
        } else {
            throw new IllegalArgumentException(WRONG_TYPE_OF_CONFIGURATION);
        }
    }

    //FIXME remove this reflection call once we redesign the SyncManager to separate interface
    //      from implementation to avoid issue like exposing internal method like SyncManager#removeSession
    //      or SyncSession#close. This happens because SyncObjectServerFacade is internal, whereas
    //      SyncManager#removeSession or SyncSession#close are package private & should not be public.
    private void invokeRemoveSession(SyncConfiguration syncConfig) {
        try {
            if (removeSessionMethod == null) {
                synchronized (SyncObjectServerFacade.class) {
                    if (removeSessionMethod == null) {
                        Method removeSession = RealmSync.class.getDeclaredMethod("removeSession", SyncConfiguration.class);
                        removeSession.setAccessible(true);
                        removeSessionMethod = removeSession;
                    }
                }
            }
            removeSessionMethod.invoke(syncConfig.getUser().getApp().getSync(), syncConfig);
        } catch (NoSuchMethodException e) {
            throw new RealmException("Could not lookup method to remove session: " + syncConfig.toString(), e);
        } catch (InvocationTargetException e) {
            throw new RealmException("Could not invoke method to remove session: " + syncConfig.toString(), e);
        } catch (IllegalAccessException e) {
            throw new RealmException("Could not remove session: " + syncConfig.toString(), e);
        }
    }

    @Override
    public void downloadInitialRemoteChanges(RealmConfiguration config) {
        if (config instanceof SyncConfiguration) {
            SyncConfiguration syncConfig = (SyncConfiguration) config;
            if (syncConfig.shouldWaitForInitialRemoteData()) {
                if (new AndroidCapabilities().isMainThread()) {
                    throw new IllegalStateException("waitForInitialRemoteData() cannot be used synchronously on the main thread. Use Realm.getInstanceAsync() instead.");
                }
                downloadInitialFullRealm(syncConfig);
            }
        }
    }

    private void downloadInitialFullRealm(SyncConfiguration syncConfig) {
        OsAsyncOpenTask task = new OsAsyncOpenTask(new OsRealmConfig.Builder(syncConfig).build());
        try {
            task.start(syncConfig.getInitialRemoteDataTimeout(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new DownloadingRealmInterruptedException(syncConfig, e);
        }
    }

    @Override
    public boolean wasDownloadInterrupted(Throwable throwable) {
        return (throwable instanceof DownloadingRealmInterruptedException);
    }

    @Override
    public void createNativeSyncSession(RealmConfiguration configuration) {
        if (configuration instanceof SyncConfiguration) {
            SyncConfiguration syncConfig = (SyncConfiguration) configuration;
            RealmApp app = syncConfig.getUser().getApp();
            app.getSync().getOrCreateSession(syncConfig);
        }
    }

}
