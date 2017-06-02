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

import java.io.Closeable;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import io.realm.internal.permissions.ManagementModule;
import io.realm.internal.permissions.PermissionModule;
import io.realm.log.RealmLog;

/**
 * FIXME: Better Javadoc
 * Helper class for interacting with Realm permissions.
 * This class has connections to underlying Realms, so all data coming from this class is thread-confined and must be
 * closed after use to avoid leaking resources.
 */
public class PermissionManager implements Closeable {

    // Reference counted cache equivalent to how Realm instances work.
    private static ThreadLocal<PermissionManager> permissionManager = new ThreadLocal<PermissionManager>() {
        @Override
        protected PermissionManager initialValue() {
            return null;
        }
    };

    private static ThreadLocal<Integer> permissionManagerInstanceCounter = new ThreadLocal<Integer>() {
        @Override
        protected Integer initialValue() {
            return 0;
        }
    };

    /**
     * Return a thread confined, reference counted instance of the PermissionManager.
     *
     * @param syncUser user to create the PermissionManager for.
     * @return a thread confined PermissionManager instance for the provided user.
     */
    static synchronized PermissionManager getInstance(SyncUser syncUser) {
        PermissionManager pm = permissionManager.get();
        if (pm == null) {
            pm = new PermissionManager(syncUser);
            permissionManager.set(pm);
        }
        permissionManagerInstanceCounter.set(permissionManagerInstanceCounter.get() + 1);
        return pm;
    }

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

    private final SyncUser user;

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

    // Task list used to queue tasks until the underlying Realms are done opening (or failed doing so).
    private Deque<AsyncTask> delayedTasks = new LinkedList<>();

    // List of tasks that are being processed. Used to keep strong references for listeners to work.
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
    private PermissionManager(final SyncUser user) {
        this.user = user;
        threadId = Thread.currentThread().getId();
        managementRealmConfig = new SyncConfiguration.Builder(
                user, getRealmUrl(RealmType.MANAGEMENT_REALM, user.getAuthenticationUrl()))
                .errorHandler(new SyncSession.ErrorHandler() {
                    @Override
                    public void onError(SyncSession session, ObjectServerError error) {
                        // FIXME: How to handle Client Reset?
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
                        // FIXME: How to handle Client Reset?
                        synchronized (errorLock) {
                            permissionRealmError = error;
                        }
                    }
                })
                .modules(new PermissionModule())
                .waitForInitialRemoteData()
                .readOnly() // FIXME: Something is seriously wrong with the Permission Realm. It doesn't seem to exist on the server. Making it impossible to mark it read only
                .build();
    }

    /**
     * FIXME: Add Javadoc
     *
     * @param callback
     * @return
     */
    public RealmAsyncTask getPermissions(final Callback<RealmResults<Permission>> callback) {
        checkIfValidThread();
        checkCallbackNotNull(callback);
        return addTask(new GetPermissionsAsyncTask(this, callback));
    }

    // Queue the task if the underlying Realms are not ready yet, otherwise
    // start the task by sending it to this thread handler. This is done
    // in order to be able to provide the user with a RealmAsyncTask representation
    // of the work being done.
    private RealmAsyncTask addTask(final AsyncTask task) {
        if (isReady()) {
            activateTask(task);
        } else {
            delayTask(task);
            openRealms();
        }

        return task;
    }

    // Park the task until all underlying Realms are ready
    private void delayTask(AsyncTask task) {
        delayedTasks.add(task);
    }

    // Run any tasks that where delayed while the underlying Realms where being opened.
    // PRECONDITION: Underlying Realms are no longer in the process of being opened.
    private void runDelayedTasks() {
        for (AsyncTask delayedTask : delayedTasks) {
            activateTask(delayedTask);
        }
        delayedTasks.clear();
    }

    // Activate a task. All tasks are controlled by the Handler in order to make it asynchronous.
    // PRECONDITION: Underlying Realms are no longer in the process of being opened.
    private void activateTask(AsyncTask task) {
        activeTasks.add(task);
        handler.post(task);
    }

    // Open both underlying Realms asynchronously. Once they are both ready, all tasks added in the meantime are
    // started. If the Realms failed to open correctly any error will be reported through the `Callback.onError` callback.
    private void openRealms() {
        if (!openInProgress) {
            openInProgress = true;
            managementRealmOpenTask = Realm.getInstanceAsync(managementRealmConfig, new Realm.Callback() {
                @Override
                public void onSuccess(Realm realm) {
                    managementRealm = realm;
                    checkIfRealmsAreOpenedAndRunDelayedTasks();
                }

                @Override
                public void onError(Throwable exception) {
                    synchronized (errorLock) {
                        managementRealmError = new ObjectServerError(ErrorCode.UNKNOWN, exception);
                        checkIfRealmsAreOpenedAndRunDelayedTasks();
                    }
                }
            });
            permissionRealmOpenTask = Realm.getInstanceAsync(permissionRealmConfig, new Realm.Callback() {
                @Override
                public void onSuccess(Realm realm) {
                    permissionRealm = realm;
                    checkIfRealmsAreOpenedAndRunDelayedTasks();
                }

                @Override
                public void onError(Throwable exception) {
                    synchronized (errorLock) {
                        permissionRealmError = new ObjectServerError(ErrorCode.UNKNOWN, exception);
                        checkIfRealmsAreOpenedAndRunDelayedTasks();
                    }
                }
            });
        }
    }

    private void checkIfRealmsAreOpenedAndRunDelayedTasks() {
        synchronized (errorLock) {
            if ((permissionRealm != null || permissionRealmError != null)
                && (managementRealm != null || managementRealmError != null)) {
                openInProgress = false;
                runDelayedTasks();
            }
        }
    }

    private void checkCallbackNotNull(Callback<?> callback) {
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

        // Multiple instances open, just decrement the reference count
        Integer instanceCount = permissionManagerInstanceCounter.get();
        if (instanceCount > 1) {
            permissionManagerInstanceCounter.set(instanceCount - 1);
            return;
        }

        // Only one instance open. Do a full close
        permissionManagerInstanceCounter.set(0);
        permissionManager.set(null);
        delayedTasks.clear();

        // If Realms are still being opened, abort that task
        if (managementRealmOpenTask != null) {
            managementRealmOpenTask.cancel();
            managementRealmOpenTask = null;
        }
        if (permissionRealmOpenTask != null) {
            permissionRealmOpenTask.cancel();
            permissionRealmOpenTask = null;
        }

        // If Realms are opened. Close them.
        if (managementRealm != null) {
            managementRealm.close();
        }
        if (permissionRealm != null) {
            permissionRealm.close();
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
        if (managementRealm != null || permissionRealm != null) {
            RealmLog.warn("PermissionManager was not correctly closed before being finalized.");
            close();
        }
        super.finalize();
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
    // The Permission result is not considered available until the query has completed.
    private class GetPermissionsAsyncTask extends AsyncTask<RealmResults<Permission>> {

        // Prevent permissions from being GC'ed until fully loaded.
        private RealmResults<Permission> loadingPermissions;

        GetPermissionsAsyncTask(PermissionManager permissionManager, Callback<RealmResults<Permission>> callback) {
            super(permissionManager, callback);
        }

        @Override
        public void run() {
            if (checkAndReportInvalidState()) { return; }
            if (permissions != null) {
                // Permissions already loaded
                notifyCallbackWithSuccess(permissions);
            } else {
                // Start loading permissions.
                // TODO Right now multiple getPermission() calls will result in multiple
                // queries being executed. The first one to return will be the one returned
                // by all callbacks.
                loadingPermissions = permissionRealm.where(Permission.class).findAllAsync();
                loadingPermissions.addChangeListener(new RealmChangeListener <RealmResults<Permission>>() {
                    @Override
                    public void onChange(RealmResults <Permission> loadedPermissions) {
                        loadingPermissions.removeChangeListener(this);
                        if (checkAndReportInvalidState()) { return; }
                        if (permissions == null) {
                            permissions = loadedPermissions;
                        }
                        notifyCallbackWithSuccess(permissions);
                    }
                });
            }
        }
    }

    // Class encapsulating all async tasks exposed by the PermissionManager.
    // All subclasses are responsible for removing themselves from the activeTaskList when done.
    // Made package protected instead of private to facilitate testing
    abstract static class AsyncTask<T> implements RealmAsyncTask, Runnable {

        private final Callback<T> callback;
        private final PermissionManager permissionManager;
        private volatile boolean canceled = false;

        public AsyncTask(PermissionManager permissionManager, Callback<T> callback) {
            this.callback = callback;
            this.permissionManager = permissionManager;
        }

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

        /**
         * Checks if we are in a state where we are not allowed to continue executing.
         * If an invalid state is encountered, it will be reported to the error callback.
         *
         * This method will return {@code true} if an invalid state was encountered, {@code false}
         * if it looks ok to continue.

         * @return {@code true} if in a invalid state, {@code false} if in a valid one.
         */
        protected final boolean checkAndReportInvalidState() {
            if (isCancelled()) { return true; }
            if (permissionManager.isClosed()) {
                ObjectServerError error = new ObjectServerError(ErrorCode.UNKNOWN,
                        new IllegalStateException("PermissionManager has been closed"));
                notifyCallbackError(error);
                return true;
            }

            // We are juggling two different Realms. If only one fail, expose that error directly.
            // Otherwise try to sensible join the two error messages before returning it to the user.
            // TODO: Should we expose the underlying Realm errors directly? What else would make sense?
            boolean managementErrorHappened;
            boolean permissionErrorHappened;
            ObjectServerError managementError;
            ObjectServerError permissionError;
            synchronized (permissionManager.errorLock) {
                // Only hold lock while making a safe copy of current error state
                managementErrorHappened = (permissionManager.managementRealmError != null);
                permissionErrorHappened = (permissionManager.permissionRealmError != null);
                managementError = permissionManager.managementRealmError;
                permissionError = permissionManager.permissionRealmError;
            }

            if (permissionErrorHappened && !managementErrorHappened) {
                notifyCallbackError(permissionError);
                return true;
            }

            if (managementErrorHappened && !permissionErrorHappened) {
                notifyCallbackError(managementError);
                return true;
            }

            if (permissionErrorHappened && managementErrorHappened) {
                notifyCallbackError(combineRealmErrors(managementError, permissionError));
                return true;
            }

            // Everything seems valid
            return false;

        }

        protected final void notifyCallbackWithSuccess(T result) {
            callback.onSuccess(result);
            permissionManager.activeTasks.remove(this);
        }

        protected final void notifyCallbackError(ObjectServerError e) {
            RealmLog.debug("Error happened in PermissionManager for %s: %s",
                    permissionManager.user.getIdentity(), e.toString());
            callback.onError(e);
            permissionManager.activeTasks.remove(this);
        }

        // Combine error messages. If they have the same ErrorCode, it will be re-used, otherwise
        // we are forced to report back UNKNOWN as error code. The real error codes
        // will be always part of the exception message.
        private ObjectServerError combineRealmErrors(ObjectServerError managementError, ObjectServerError
                permissionError) {

            String errorMsg = combineErrorMessage(managementError, permissionError);
            ErrorCode errorCode = (managementError.getErrorCode() == permissionError.getErrorCode())
                    ? managementError.getErrorCode()
                    : ErrorCode.UNKNOWN;

            return new ObjectServerError(errorCode, errorMsg);
        }

        // Combine the text based error message from two ObjectServerErrrors.
        private String combineErrorMessage(ObjectServerError managementError,
                                           ObjectServerError permissionError) {
            StringBuilder errorMsg = new StringBuilder("Multiple errors occurred: ");
            errorMsg.append('\n');
            errorMsg.append("Management Realm:");
            errorMsg.append('\n');
            errorMsg.append(managementError.toString());
            errorMsg.append('\n');
            errorMsg.append("Permission Realm:");
            errorMsg.append('\n');
            errorMsg.append(permissionError.toString());
            return errorMsg.toString();
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
