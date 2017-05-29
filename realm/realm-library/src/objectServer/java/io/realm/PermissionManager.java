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

package io.realm;

import android.os.Handler;
import android.os.SystemClock;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import io.realm.ErrorCode;
import io.realm.ObjectServerError;
import io.realm.Realm;
import io.realm.RealmAsyncTask;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.SyncConfiguration;
import io.realm.SyncSession;
import io.realm.SyncUser;
import io.realm.log.RealmLog;
import io.realm.permissions.ManagementModule;
import io.realm.permissions.Permission;
import io.realm.permissions.PermissionModule;


/**
 * FIXME: Better Javadoc
 * Helper class for interacting with Realm permissions.
 * This class has connections to underlying Realms, so all data coming from this class is thread-confined and must be
 * closed after use to avoid leaking resources.
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

    // Used to track the lifecycle of the PermissionManager
    private RealmAsyncTask managementRealmOpenTask;
    private RealmAsyncTask permissionRealmOpenTask;
    private boolean openInProgress = false;
    private boolean closed;

    private final long threadId;
    private Handler handler = new Handler();
    private final SyncConfiguration managementRealmConfig;
    private final SyncConfiguration permissionRealmConfig;
    private Realm permissionRealm;
    private Realm managementRealm;

    // Task list used to queue tasks until the underlying Realms are done opening (or fail doing so).
    private Deque<PermissionManagerAsyncTask> delayedTasks = new LinkedList<>();

    // List of tasks that are running. Used to keep strong references for listeners to work.
    // The task must remove itself from this list once it either completes
    // or fails.
    private List<RealmAsyncTask> activeTasks = new ArrayList<>();

    // Object Server Errors might be reported on another thread than the one running this PermissionManager
    // In order to prevent race conditions, all blocks of code that read/write these errors should do
    // so while holding the errorLock
    private final Object errorLock = new Object();
    private volatile ObjectServerError permissionRealmError = null;
    private volatile ObjectServerError managementRealmError = null;

    // Cached result of the permission query. This will be filled, once the first PermissionAsyncTask has loaded
    // the result.
    private RealmResults<Permission> permissions;

    /**
     * FIXME Javadoc
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
                        synchronized (errorLock) {
                            managementRealmError = error;
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
                        synchronized (errorLock) {
                            permissionRealmError = error;
                        }
                    }
                })
                .modules(new PermissionModule())
                .waitForInitialRemoteData()
                .readOnly()
                .build();
    }

    /**
     * FIXME: Add Javadoc
     *
     * @param callback
     * @return
     */
    public RealmAsyncTask getPermissionsAsync(final Callback<RealmResults<Permission>> callback) {
        checkIfValidThread();
        checkCallbackNotNull(callback);
        return addTask(new PermissionsAsyncTask(callback));
    }

    // Queue the task if the underlying Realms are not ready yet, otherwise
    // start the task by sending it to this thread handler. This is done
    // in order to be able to provide the user with a RealmAsyncTask representation
    // of the work being done.
    private RealmAsyncTask addTask(final PermissionManagerAsyncTask task) {
        if (isReady()) {
            activateTask(task);
        } else {
            delayTask(task);
            openRealms();
        }

        return task;
    }

    // Park the task until all underlying Realms are ready
    private void delayTask(PermissionManagerAsyncTask task) {
        delayedTasks.add(task);
    }

    // Run any tasks that where delayed while the underlying Realms where being opened.
    // PRECONDITION: Underlying Realms are no longer in the process of being opened.
    private void runDelayedTasks() {
        for (PermissionManagerAsyncTask delayedTask : delayedTasks) {
            activateTask(delayedTask);
        }
        delayedTasks.clear();
    }

    // Activate a task. All tasks are controlled by the Handler in order to make it asynchronous.
    // PRECONDITION: Underlying Realms are no longer in the process of being opened.
    private void activateTask(PermissionManagerAsyncTask task) {
        activeTasks.add(task);
        handler.post(task);
    }

    // Open both underlying Realms asynchronously. Once they are both ready, all tasks added in the meantime are
    // started. If the Realms failed to open correctly any error will be reported through the `Callback.onError` callback.
    private void openRealms() {
        if (openInProgress) {
            return;
        } else {
            openInProgress = true;
            managementRealmOpenTask = Realm.getInstanceAsync(managementRealmConfig, new Realm.Callback() {
                @Override
                public void onSuccess(Realm realm) {
                    managementRealm = realm;
                    checkIfRealmsAreOpened();
                }

                @Override
                public void onError(Throwable exception) {
                    synchronized (errorLock) {
                        managementRealmError = new ObjectServerError(ErrorCode.UNKNOWN, exception);
                    }
                    checkIfRealmsAreOpened();
                }
            });
            permissionRealmOpenTask = Realm.getInstanceAsync(permissionRealmConfig, new Realm.Callback() {
                @Override
                public void onSuccess(Realm realm) {
                    permissionRealm = realm;
                    checkIfRealmsAreOpened();
                }

                @Override
                public void onError(Throwable exception) {
                    synchronized (errorLock) {
                        permissionRealmError = new ObjectServerError(ErrorCode.UNKNOWN, exception);
                    }
                    checkIfRealmsAreOpened();
                }
            });
        }
    }

    private void checkIfRealmsAreOpened() {
        synchronized (errorLock) {
            if ((permissionRealm != null || permissionRealmError != null)
                    && (managementRealm != null || managementRealmError != null)) {
                openInProgress = false;
                runDelayedTasks();
            }
        }
    }

    private void checkCallbackNotNull(Callback<RealmResults<Permission>> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Non-null 'callback' required.");
        }
    }

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

    /**
     * FIXME Add Javadoc
     */
    @Override
    public void close() {
        checkIfValidThread();
        // If Realms are still being opened, abort that task
        if (openInProgress) {
            if (managementRealmOpenTask != null) {
                managementRealmOpenTask.cancel();
                managementRealmOpenTask = null;
            }
            if (permissionRealmOpenTask != null) {
                permissionRealmOpenTask.cancel();
                permissionRealmOpenTask = null;
            }
        } else {
            if (managementRealm != null) {
                managementRealm.close();
            }
            if (permissionRealm != null) {
                permissionRealm.close();
            }
        }
        closed = true;
    }

    /**
     * Checks if this PermissionManager is closed or not. If it is closed, all methods will report back an error
     *
     * @return {@code true} if the PermissionManager is closed, {@code false} if it is still open.
     */
    public boolean isClosed() {
        checkIfValidThread();
        return closed;
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
            throw new IllegalArgumentException("Could not create URL to the " + type + " + Realm", e);
        }
    }

    // Task responsible for loading the Permissions result and returning it to the user.
    // The Permission result is not considered available until the query has complated.
    private class PermissionsAsyncTask extends PermissionManagerAsyncTask {

        private final Callback<RealmResults<Permission>> callback;
        private RealmResults<Permission> loadingPermissons; // Prevent permissions from being GC'ed until fully loaded.

        PermissionsAsyncTask(Callback<RealmResults<Permission>> callback) {
            this.callback = callback;
        }

        @Override
        public void run() {
            if (isCancelled()) { return; }
            if (permissions != null || isClosed()) {
                notifyCallbackWithResult();
            } else {
                synchronized (errorLock) {
                    if (permissionRealmError != null) {
                        callback.onError(permissionRealmError);
                    } else {
                        loadingPermissons = permissionRealm.where(Permission.class).findAllAsync();
                        loadingPermissons.addChangeListener(new RealmChangeListener<RealmResults<Permission>>() {
                            @Override
                            public void onChange(RealmResults<Permission> loadedPermissions) {
                                loadingPermissons.removeChangeListener(this);
                                if (isCancelled()) { return; }
                                if (permissions == null) {
                                    permissions = loadedPermissions;
                                }
                                notifyCallbackWithResult();
                            }
                        });
                    }
                }
            }
        }

        private void notifyCallbackWithResult() {
            synchronized (errorLock) {
                if (isClosed()) {
                    IllegalStateException reason = new IllegalStateException("PermissionManager has been closed.");
                    callback.onError(new ObjectServerError(ErrorCode.UNKNOWN, reason));
                } else if (permissionRealmError != null) {
                    callback.onError(permissionRealmError);
                } else {
                    callback.onSuccess(permissions);
                }
            }
            activeTasks.remove(this);
        }
    }

    // Class encapsulating all async tasks exposed by the PermissionManager.
    // All subclasses are responsible for removing themselves from the activeTaskList when done.
    private abstract static class PermissionManagerAsyncTask implements RealmAsyncTask, Runnable {

        private volatile boolean canceled = false;

        @Override
        public abstract void run();

        @Override
        public void cancel() {
            canceled = true;
        }

        @Override
        public boolean isCancelled() {
            return canceled;
        }
    }

    /**
     * Callback used when an asynchronous task is complete.
     *
     * @param <T> the result in case of a success or {@link Void} if no result is available.
     */
    public interface Callback<T> {
        void onSuccess(T t);
        void onError(ObjectServerError error);
    }
}
