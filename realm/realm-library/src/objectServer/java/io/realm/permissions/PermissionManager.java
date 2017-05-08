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

package io.realm.permissions;

import android.os.Handler;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

import io.realm.ErrorCode;
import io.realm.ObjectServerError;
import io.realm.Realm;
import io.realm.RealmAsyncTask;
import io.realm.RealmResults;
import io.realm.SyncConfiguration;
import io.realm.SyncSession;
import io.realm.SyncUser;
import io.realm.log.RealmLog;


/**
 * FIXME: Better Javadoc
 * Helper class for interacting with Realm permissions.
 * This class has connections to underlying Realms, so all data comming from this class is thread-confined and must be
 * closed after use to avoid leaking resources.
 *
 * All methods in this class
 */
public class PermissionManager implements Closeable {

    private enum RealmType {
        PERMISSION_REALM("__permission"),
        MANAGEMENT_REALM("__management");

        private final String name;

        RealmType(String realmName) {
            this.name = realmName;
        }

        public String getName() {
            return name;
        }
    }

    private final long threadId;
    private Handler handler = new Handler();
    private final SyncConfiguration managementRealmConfig;
    private final SyncConfiguration permissionRealmConfig;
    private boolean openInProgress = false;
    private Realm permissionRealm;
    private Realm managementRealm;
    // Task list used to keep tasks alive until all Realms are available
    private Deque<Callable> delayedTasks = new LinkedList<>();

    // Keep strong reference to live Realm Objects to prevent them from being GC'ed while the PermissionManager is
    // running. This also works as a object cache
    private ObjectServerError permissionRealmError;
    private ObjectServerError managementRealmError;
    private RealmResults<Permission> permissions;

    /**
     * Creates a PermissionManager for the given user.
     *
     * Implementation notes: This class is thread safe since all public methods have thread confined checks in them
     * and all internal communication is routed through the original Handler thread (required by Realm's notifications).
     *
     * @param user user to create manager for.
     */
    PermissionManager(final SyncUser user) {
        threadId = Thread.currentThread().getId();
        managementRealmConfig = new SyncConfiguration.Builder(
                user, getRealmUrl(RealmType.MANAGEMENT_REALM, user.getAuthenticationUrl()))
                .errorHandler(new SyncSession.ErrorHandler() {
                    @Override
                    public void onError(SyncSession session, ObjectServerError error) {
                        if (error.getErrorCode() == ErrorCode.CLIENT_RESET) {
                            RealmLog.error("Client Reset required for user's management Realm: " + user.toString());
                        } else {
                            RealmLog.error(String.format("Unexpected error with %s's management Realm: %s",
                                    user.getIdentity(),
                                    error.toString()));
                        }
                    }
                })
                .modules(new ManagementModule())
                .build();

        permissionRealmConfig = new SyncConfiguration.Builder(
                user, getRealmUrl(RealmType.PERMISSION_REALM, user.getAuthenticationUrl()))
                .errorHandler(new SyncSession.ErrorHandler() {
                    @Override
                    public void onError(SyncSession session, ObjectServerError error) {
                        if (error.getErrorCode() == ErrorCode.CLIENT_RESET) {
                            RealmLog.error("Client Reset required for user's permission Realm: " + user.toString());
                        } else {
                            RealmLog.error(String.format("Unexpected error with %s's management Realm: %s",
                                    user.getIdentity(),
                                    error.toString()));
                        }
                    }
                })
                .modules(new PermissionModule())
                .build();
    }
//
    public RealmAsyncTask getPermissionsAsync(final Callback<RealmResults<Permission>> callback) {
        checkIfValidThread();
        checkCallbackNotNull(callback);
        return addTask(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                if (permissions != null) {
                    callback.onSuccess(permissions);
                } else {
                    permissions = permissionRealm.where(Permission.class).findAllAsync();
                }
                return null;
            }
        });
//
//
//        return permissionRealm.where(Permission.class).findAll();
    }

    private RealmAsyncTask addTask(Callable<?> task) {
        delayedTasks.add(task);
        if (isReady()) {
            runTaskQueue();
        } else {
            openRealms();
        }
    }

    private void runTaskQueue() {
        // Delay execution so we can return a RealmAsyncTask to users
        handler.post(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    private void openRealms() {
        if (openInProgress) {
            return;
        } else {
            managementRealm = Realm.getInstance(managementRealmConfig);
            permissionRealm = Realm.getInstance(permissionRealmConfig);
        }
    }

    private void checkCallbackNotNull(Callback<RealmResults<Permission>> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Non-null 'callback' required.");
        }
    }

    //
//    RealmAsyncTask setPermissionAsync(Permission permission, Callback callback) {
//        return null;
//    }
//
//    RealmAsyncTask deletePermissionAsync(Permission permission, Callback callback) {
//        return null;
//    }
//
    private boolean isReady() {
        return managementRealm != null && permissionRealm != null;
    }

    private void checkIfValidThread() {
        // Checks if we are in thread that created the PermissionManager.
        if (threadId != Thread.currentThread().getId()) {
            throw new IllegalStateException("PermissionManager was accessed from the wrong thread. It can only be " +
                    "accessed on the thread it was created on.");
        }
    }


    @Override
    public void close() throws IOException {
        if (managementRealm != null) {
            managementRealm.close();
            managementRealm = null;
        }
        if (permissionRealm != null) {
            permissionRealm.close();
            permissionRealm = null;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (managementRealm != null || permissionRealm != null) {
            RealmLog.error("PermissionManager was not correctly closed before being finalized.");
            close();
        }
    }

    // Creates the URL to the permission/management Realm based on the authentication URL.
    private static String getRealmUrl(RealmType type, URL authUrl) {
        String scheme = "realm";
        if (authUrl.getProtocol().equalsIgnoreCase("https")) {
            scheme = "realms";
        }
        try {
            return new URI(scheme, authUrl.getUserInfo(), authUrl.getHost(), authUrl.getPort(),
                    "/~/" + type.getName(), null, null).toString();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Could not create URL to the management Realm", e);
        }
    }

    private static class PermissionAsyncTask implements RealmAsyncTask {

        @Override
        public void cancel() {

        }

        @Override
        public boolean isCancelled() {
            return false;
        }
    }



    public interface Callback<T> {
        void onSuccess(T t);
        void onError(ObjectServerError error);
    }

}
