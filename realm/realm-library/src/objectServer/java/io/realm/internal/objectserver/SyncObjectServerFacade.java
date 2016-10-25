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

package io.realm.internal.objectserver;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import io.realm.RealmConfiguration;
import io.realm.SyncSession;
import io.realm.SyncConfiguration;
import io.realm.SyncManager;
import io.realm.exceptions.RealmException;
import io.realm.internal.Keep;
import io.realm.internal.ObjectServerFacade;
import io.realm.internal.network.NetworkStateReceiver;

@SuppressWarnings({"unused", "WeakerAccess"}) // Used through reflection. See ObjectServerFacade
@Keep
public class SyncObjectServerFacade extends ObjectServerFacade {

    private static final String WRONG_TYPE_OF_CONFIGURATION =
            "'configuration' has to be an instance of 'SyncConfiguration'.";
    @SuppressLint("StaticFieldLeak") //
    private static Context applicationContext;

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
    public void notifyCommit(RealmConfiguration configuration, long lastSnapshotVersion) {
        if (configuration instanceof SyncConfiguration) {
            SyncSession publicSession = SyncManager.getSession((SyncConfiguration) configuration);
            ObjectServerSession session = SessionStore.getPrivateSession(publicSession);
            session.notifyCommit(lastSnapshotVersion);
        } else {
            throw new IllegalArgumentException(WRONG_TYPE_OF_CONFIGURATION);
        }
    }

    @Override
    public void realmClosed(RealmConfiguration configuration) {
        if (configuration instanceof SyncConfiguration) {
            SyncSession publicSession = SyncManager.getSession((SyncConfiguration) configuration);
            ObjectServerSession session = SessionStore.getPrivateSession(publicSession);
            session.getSyncPolicy().onRealmClosed(session);
        } else {
            throw new IllegalArgumentException(WRONG_TYPE_OF_CONFIGURATION);
        }
    }

    @Override
    public void realmOpened(RealmConfiguration configuration) {
        if (configuration instanceof SyncConfiguration) {
            SyncSession publicSession = SyncManager.getSession((SyncConfiguration) configuration);
            ObjectServerSession session = SessionStore.getPrivateSession(publicSession);
            session.getSyncPolicy().onRealmOpened(session);
        } else {
            throw new IllegalArgumentException(WRONG_TYPE_OF_CONFIGURATION);
        }
    }

    @Override
    public String[] getUserAndServerUrl(RealmConfiguration config) {
        if (config instanceof SyncConfiguration) {
            SyncConfiguration syncConfig = (SyncConfiguration) config;
            String rosServerUrl = syncConfig.getServerUrl().toString();
            String rosUserToken = syncConfig.getUser().getAccessToken();
            return new String[]{rosServerUrl, rosUserToken};
        } else {
            return new String[2];
        }
    }

    static Context getApplicationContext() {
        return applicationContext;
    }
}
