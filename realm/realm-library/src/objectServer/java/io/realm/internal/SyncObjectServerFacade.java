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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import io.realm.RealmConfiguration;
import io.realm.SyncConfiguration;
import io.realm.SyncManager;
import io.realm.SyncSession;
import io.realm.SyncUser;
import io.realm.exceptions.DownloadingRealmInterruptedException;
import io.realm.exceptions.RealmException;
import io.realm.internal.network.NetworkStateReceiver;
import io.realm.internal.sync.permissions.ObjectPermissionsModule;

@SuppressWarnings({"unused", "WeakerAccess"}) // Used through reflection. See ObjectServerFacade
@Keep
public class SyncObjectServerFacade extends ObjectServerFacade {

    private static final String WRONG_TYPE_OF_CONFIGURATION =
            "'configuration' has to be an instance of 'SyncConfiguration'.";
    @SuppressLint("StaticFieldLeak") //
    private static Context applicationContext;
    private static volatile Method removeSessionMethod;

    @Override
    public void init(Context context) {
        // Trying to keep things out the public API is no fun :/
        // Just use reflection on init. It is a one-time method call so should be acceptable.
        //noinspection TryWithIdenticalCatches
        try {
            // FIXME: Reflection can be avoided by moving some functions of SyncManager and ObjectServer out of public
            Class<?> syncManager = Class.forName("io.realm.ObjectServer");
            Method method = syncManager.getDeclaredMethod("init", Context.class);
            method.setAccessible(true);
            method.invoke(null, context);
        } catch (NoSuchMethodException e) {
            throw new RealmException("Could not initialize the Realm Object Server", e);
        } catch (InvocationTargetException e) {
            throw new RealmException("Could not initialize the Realm Object Server", e);
        } catch (IllegalAccessException e) {
            throw new RealmException("Could not initialize the Realm Object Server", e);
        } catch (ClassNotFoundException e) {
            throw new RealmException("Could not initialize the Realm Object Server", e);
        }
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
            SyncUser user = syncConfig.getUser();
            String rosServerUrl = syncConfig.getServerUrl().toString();
            String rosUserIdentity = user.getIdentity();
            String syncRealmAuthUrl = user.getAuthenticationUrl().toString();
            String rosSerializedUser = user.toJson();
            byte sessionStopPolicy = syncConfig.getSessionStopPolicy().getNativeValue();
            String urlPrefix = syncConfig.getUrlPrefix();
            String customAuthorizationHeaderName = SyncManager.getAuthorizationHeaderName(syncConfig.getServerUrl());
            Map<String, String> customHeaders = SyncManager.getCustomRequestHeaders(syncConfig.getServerUrl());
            return new Object[]{
                    rosUserIdentity,
                    rosServerUrl,
                    syncRealmAuthUrl,
                    rosSerializedUser,
                    syncConfig.syncClientValidateSsl(),
                    syncConfig.getServerCertificateFilePath(),
                    sessionStopPolicy,
                    !syncConfig.isFullySynchronizedRealm(),
                    urlPrefix,
                    customAuthorizationHeaderName,
                    customHeaders
            };
        } else {
            return new Object[11];
        }
    }

    public static Context getApplicationContext() {
        return applicationContext;
    }

    @Override
    public void wrapObjectStoreSessionIfRequired(OsRealmConfig config) {
        if (config.getRealmConfiguration() instanceof SyncConfiguration) {
            SyncManager.getOrCreateSession((SyncConfiguration) config.getRealmConfiguration(), config.getResolvedRealmURI());
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
                        Method removeSession = SyncManager.class.getDeclaredMethod("removeSession", SyncConfiguration.class);
                        removeSession.setAccessible(true);
                        removeSessionMethod = removeSession;
                    }
                }
            }
            removeSessionMethod.invoke(null, syncConfig);
        } catch (NoSuchMethodException e) {
            throw new RealmException("Could not lookup method to remove session: " + syncConfig.toString(), e);
        } catch (InvocationTargetException e) {
            throw new RealmException("Could not invoke method to remove session: " + syncConfig.toString(), e);
        } catch (IllegalAccessException e) {
            throw new RealmException("Could not remove session: " + syncConfig.toString(), e);
        }
    }

    @Override
    public void downloadRemoteChanges(RealmConfiguration config) {
        if (config instanceof SyncConfiguration) {
            SyncConfiguration syncConfig = (SyncConfiguration) config;
            if (syncConfig.shouldWaitForInitialRemoteData()) {
                SyncSession session = SyncManager.getSession(syncConfig);
                try {
                    session.downloadAllServerChanges();
                } catch (InterruptedException e) {
                    throw new DownloadingRealmInterruptedException(syncConfig, e);
                }
            }
        }
    }

    @Override
    public boolean wasDownloadInterrupted(Throwable throwable) {
        return (throwable instanceof DownloadingRealmInterruptedException);
    }

    @Override
    public boolean isPartialRealm(RealmConfiguration configuration) {
        if (configuration instanceof SyncConfiguration) {
            SyncConfiguration syncConfig = (SyncConfiguration) configuration;
            return !syncConfig.isFullySynchronizedRealm();
        }
        
        return false;
    }

    @Override
    public void addSupportForObjectLevelPermissions(RealmConfiguration.Builder builder) {
        builder.addModule(new ObjectPermissionsModule());
    }
}
