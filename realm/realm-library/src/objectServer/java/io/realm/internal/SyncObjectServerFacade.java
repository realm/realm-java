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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.realm.internal.objectstore.OsApp;
import io.realm.mongodb.App;
import io.realm.RealmConfiguration;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.sync.Sync;
import io.realm.mongodb.User;
import io.realm.mongodb.sync.SyncConfiguration;
import io.realm.exceptions.DownloadingRealmInterruptedException;
import io.realm.exceptions.RealmException;
import io.realm.internal.android.AndroidCapabilities;
import io.realm.internal.jni.JniBsonProtocol;
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
    private static volatile Field osAppField;

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
            User user = syncConfig.getUser();
            App app = user.getApp();
            String rosServerUrl = syncConfig.getServerUrl().toString();
            String rosUserIdentity = user.getId();
            String syncRealmAuthUrl = user.getApp().getConfiguration().getBaseUrl().toString();
            String syncUserRefreshToken = user.getRefreshToken();
            String syncUserAccessToken = user.getAccessToken();
            String deviceId = user.getDeviceId();
            byte sessionStopPolicy = syncConfig.getSessionStopPolicy().getNativeValue();
            String urlPrefix = syncConfig.getUrlPrefix();
            String customAuthorizationHeaderName = app.getConfiguration().getAuthorizationHeaderName();
            Map<String, String> customHeaders = app.getConfiguration().getCustomRequestHeaders();
            long appNativePointer;

            // We cannot get the app native pointer without exposing it in the public API due to
            // how our packages are structured. Instead of polluting the API we use reflection to
            // access it.
            try {
                if (osAppField == null) {
                    synchronized (SyncObjectServerFacade.class) {
                        if (osAppField == null) {
                            Field field = App.class.getDeclaredField("osApp");
                            field.setAccessible(true);
                            osAppField = field;
                        }
                    }
                }
                OsApp osApp = (OsApp) osAppField.get(app);
                appNativePointer = osApp.getNativePtr();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            // TODO Simplify. org.bson serialization only allows writing full documents, so the partition
            //  key is embedded in a document with key 'value' and unwrapped in JNI.
            BsonValue partitionValue = syncConfig.getPartitionValue();
            String encodedPartitionValue;
            switch (partitionValue.getBsonType()) {
                case STRING:
                case OBJECT_ID:
                case INT32:
                case INT64:
                case NULL:
                    encodedPartitionValue = JniBsonProtocol.encode(partitionValue, AppConfiguration.DEFAULT_BSON_CODEC_REGISTRY);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported type: " + partitionValue);
            }

            int i = 0;
            Object[] configObj = new Object[SYNC_CONFIG_OPTIONS];
            configObj[i++] = rosUserIdentity;
            configObj[i++] = rosServerUrl;
            configObj[i++] = syncRealmAuthUrl;
            configObj[i++] = syncUserRefreshToken;
            configObj[i++] = syncUserAccessToken;
            configObj[i++] = deviceId;
            configObj[i++] = sessionStopPolicy;
            configObj[i++] = urlPrefix;
            configObj[i++] = customAuthorizationHeaderName;
            configObj[i++] = customHeaders;
            configObj[i++] = OsRealmConfig.CLIENT_RESYNC_MODE_MANUAL;
            configObj[i++] = encodedPartitionValue;
            configObj[i++] = app.getSync();
            configObj[i++] = appNativePointer;
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
            App app = syncConfig.getUser().getApp();
            app.getSync().getOrCreateSession(syncConfig);
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
                        Method removeSession = Sync.class.getDeclaredMethod("removeSession", SyncConfiguration.class);
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
            App app = syncConfig.getUser().getApp();
            app.getSync().getOrCreateSession(syncConfig);
        }
    }

}
