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
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.realm.internal.permissions.ManagementModule;
import io.realm.internal.permissions.PermissionChange;
import io.realm.internal.permissions.PermissionModule;
import io.realm.log.RealmLog;
import io.realm.permissions.Permission;
import io.realm.permissions.PermissionRequest;


/**
 * FIXME: Better Javadoc
 * Helper class for interacting with Realm permissions.
 * This class has connections to underlying Realms, so all data coming from this class is thread-confined and must be
 * closed after use to avoid leaking resources.
 */
public class PermissionManager implements Closeable {

    // Reference counted cache equivalent to how Realm instances work.
    private static Map<String, ThreadLocal<Cache>> cache = new HashMap<>();

    private static class Cache {
        public PermissionManager pm = null;
        public Integer instanceCounter = Integer.valueOf(0);
    }

    private static final Object cacheLock = new Object();

    /**
     * Return a thread confined, reference counted instance of the PermissionManager.
     *
     * @param syncUser user to create the PermissionManager for.
     * @return a thread confined PermissionManager instance for the provided user.
     */
    static PermissionManager getInstance(SyncUser syncUser) {
        synchronized (cacheLock) {
            String userId = syncUser.getIdentity();
            ThreadLocal<Cache> threadLocalCache = cache.get(userId);
            if (threadLocalCache == null) {
                threadLocalCache = new ThreadLocal<Cache>() {
                    @Override
                    protected Cache initialValue() {
                        return new Cache();
                    }
                };
                cache.put(userId, threadLocalCache);
            }
            Cache c = threadLocalCache.get();
            if (c.instanceCounter == 0) {
                c.pm = new PermissionManager(syncUser);
            }
            c.instanceCounter++;
            return c.pm;
        }
    }

    private enum RealmType {
        DEFAULT_PERMISSION_REALM("__permission", true),
        PERMISSION_REALM("__permission", false),
        MANAGEMENT_REALM("__management", false);

        private final String name;
        private final boolean globalRealm;

        RealmType(String realmName, boolean globalRealm) {
            this.name = realmName;
            this.globalRealm = globalRealm;
        }

        public String getName() {
            return name;
        }

        public boolean isGlobalRealm() {
            return globalRealm;
        }
    }

    private final SyncUser user;

    // Used to track the lifecycle of the PermissionManager
    private RealmAsyncTask managementRealmOpenTask;
    private RealmAsyncTask permissionRealmOpenTask;
    private RealmAsyncTask defaultPermissionRealmOpenTask;
    private boolean openInProgress = false;
    private boolean closed;

    private final long threadId;
    private Handler handler = new Handler();
    private final SyncConfiguration managementRealmConfig;
    private final SyncConfiguration permissionRealmConfig;
    private final SyncConfiguration defaultPermissionRealmConfig;
    private Realm permissionRealm;
    private Realm managementRealm;
    private Realm defaultPermissionRealm;

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
    private volatile ObjectServerError defaultPermissionRealmError = null;

    // Cached result of the permission query. This will be filled, once the first PermissionAsyncTask has loaded
    // the result.
    private RealmResults<Permission> userPermissions;
    private RealmResults<Permission> defaultPermissions;

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
                // .readOnly() // FIXME: Something is seriously wrong with the Permission Realm. It doesn't seem to exist on the server. Making it impossible to mark it read only
                .build();

        defaultPermissionRealmConfig = new SyncConfiguration.Builder(
                user, getRealmUrl(RealmType.DEFAULT_PERMISSION_REALM, user.getAuthenticationUrl()))
                .errorHandler(new SyncSession.ErrorHandler() {
                    @Override
                    public void onError(SyncSession session, ObjectServerError error) {
                        // FIXME: How to handle Client Reset?
                        synchronized (errorLock) {
                            defaultPermissionRealmError = error;
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
    public RealmAsyncTask getPermissions(final Callback<RealmResults<Permission>> callback) {
        checkIfValidThread();
        checkCallbackNotNull(callback);
        return addTask(new GetPermissionsAsyncTask(this, callback));
    }

    /**
     * Returns default permissions for all Realms. The default permissions are the ones that will be used if no
     * user specific permissions is in effect.
     *
     * @param callback callback notified when the permissions are ready. The returned {@link RealmResults} is a fully
     * live query result, that will be auto-updated like any other {@link RealmResults}.
     * @return {@link RealmAsyncTask} that can be used to cancel the task if needed.
     */
    public RealmAsyncTask getDefaultPermissions(final Callback<RealmResults<Permission>> callback) {
        checkIfValidThread();
        checkCallbackNotNull(callback);
        return addTask(new GetDefaultPermissionsAsyncTask(this, callback));
    }

    /**
     * Applies a given set of permissions to a Realm.
     * <p>
     * A {@link PermissionRequest} object encapsulates a description of which users are granted what
     * {@link io.realm.permissions.AccessLevel}s for which Realm(s).
     * <p>
     * Once the request is successfully handled, a {@link Permission} entry is created in each user's
     * {@link PermissionManager} and can be found using {@link PermissionManager#getPermissions(Callback)}.
     *
     * @param request request object describing which permissions to grant and to what Realm(s).
     * @param callback callback when the request either succeeded or failed.
     * @return async task representing the request. This can be used to cancel it if needed.
     */
    public RealmAsyncTask applyPermissions(PermissionRequest request, final Callback<Void> callback) {
        checkIfValidThread();
        checkCallbackNotNull(callback);
        return addTask(new ApplyPermissionTask(this, request, callback));
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

    // Run any tasks that were delayed while the underlying Realms were being opened.
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

    // Open all underlying Realms asynchronously. Once they are all ready, all tasks added in the meantime are
    // started. Any error will be reported through the `Callback.onError` callback if the Realms failed to open
    // correctly.
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
            defaultPermissionRealmOpenTask = Realm.getInstanceAsync(defaultPermissionRealmConfig, new Realm.Callback() {
                @Override
                public void onSuccess(Realm realm) {
                    defaultPermissionRealm = realm;
                    checkIfRealmsAreOpenedAndRunDelayedTasks();
                }

                @Override
                public void onError(Throwable exception) {
                    synchronized (errorLock) {
                        defaultPermissionRealmError = new ObjectServerError(ErrorCode.UNKNOWN, exception);
                        checkIfRealmsAreOpenedAndRunDelayedTasks();
                    }
                }
            });
        }
    }

    private void checkIfRealmsAreOpenedAndRunDelayedTasks() {
        synchronized (errorLock) {
            if ((permissionRealm != null || permissionRealmError != null)
                && (defaultPermissionRealm != null || defaultPermissionRealmError != null)
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
        return managementRealm != null && permissionRealm != null && defaultPermissionRealm != null;
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
        synchronized (cacheLock) {
            Cache cache = PermissionManager.cache.get(user.getIdentity()).get();
            if (cache.instanceCounter > 1) {
                cache.instanceCounter--;
                return;
            }

            // Only one instance open. Do a full close
            cache.instanceCounter = 0;
            cache.pm = null;
        }
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
        if (defaultPermissionRealmOpenTask != null) {
            defaultPermissionRealmOpenTask.cancel();
            defaultPermissionRealmOpenTask = null;
        }

        // If Realms are opened. Close them.
        if (managementRealm != null) {
            managementRealm.close();
        }
        if (permissionRealm != null) {
            permissionRealm.close();
        }
        if (defaultPermissionRealm != null) {
            defaultPermissionRealm.close();
        }
        closed = true;
    }

    /**
     * Checks if this PermissionManager is closed or not. If it is closed, all methods will report back an error.
     *
     * @return {@code true} if the PermissionManager is closed, {@code false} if it is still open.
     */
    public boolean isClosed() {
        checkIfValidThread();
        return closed;
    }

    @Override
    protected void finalize() throws Throwable {
        if (!closed) {
            RealmLog.warn("PermissionManager was not correctly closed before being finalized.");
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
            String path = (type.isGlobalRealm() ? "/" : "/~/") + type.getName();
            return new URI(scheme, authUrl.getUserInfo(), authUrl.getHost(), authUrl.getPort(), path, null, null).toString();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Could not create URL to the " + type + " Realm", e);
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
            if (userPermissions != null) {
                // Permissions already loaded
                notifyCallbackWithSuccess(userPermissions);
            } else {
                // TODO Right now multiple getPermission() calls will result in multiple
                // queries being executed. The first one to return will be the one returned
                // by all callbacks.
                loadingPermissions = permissionRealm.where(Permission.class).findAllAsync();
                loadingPermissions.addChangeListener(new RealmChangeListener <RealmResults<Permission>>() {
                    @Override
                    public void onChange(RealmResults <Permission> loadedPermissions) {
                        // FIXME Wait until both the __permission and __management Realm are available
                        if (loadedPermissions.size() > 0) {
                            loadingPermissions.removeChangeListener(this);
                            if (checkAndReportInvalidState()) { return; }
                            if (userPermissions == null) {
                                userPermissions = loadedPermissions;
                            }
                            notifyCallbackWithSuccess(userPermissions);
                        }
                    }
                });
            }
        }
    }

    // Task responsible for loading the Default Permissions result and returning it to the user.
    // The Permission result is not considered available until the query has completed.
    private class GetDefaultPermissionsAsyncTask extends AsyncTask<RealmResults<Permission>> {

        // Prevent permissions from being GC'ed until fully loaded.
        private RealmResults<Permission> loadingPermissions;

        GetDefaultPermissionsAsyncTask(PermissionManager permissionManager, Callback<RealmResults<Permission>> callback) {
            super(permissionManager, callback);
        }

        @Override
        public void run() {
            if (checkAndReportInvalidState()) { return; }
            if (defaultPermissions != null) {
                notifyCallbackWithSuccess(defaultPermissions);
            } else {
                // Start loading permissions.
                // TODO Right now multiple getPermission() calls will result in multiple
                // queries being executed. The first one to return will be the one returned
                // by all callbacks.
                loadingPermissions = permissionRealm.where(Permission.class).findAllAsync();
                loadingPermissions.addChangeListener(new RealmChangeListener <RealmResults<Permission>>() {
                    @Override
                    public void onChange(RealmResults <Permission> loadedPermissions) {
                        if (loadedPermissions.size() > 0) {
                            loadingPermissions.removeChangeListener(this);
                            if (checkAndReportInvalidState()) { return; }
                            if (defaultPermissions == null) {
                                defaultPermissions = loadedPermissions;
                            }
                            notifyCallbackWithSuccess(defaultPermissions);
                        }
                    }
                });
            }
        }
    }

    // Class encapsulating setting a Permission by writing a PermissionChange and waiting for it to
    // be processed.
    private class ApplyPermissionTask extends AsyncTask<Void> {

        private final PermissionChange unmanagedChangeRequest;
        private String changeRequestId;
        private PermissionChange managedChangeRequest;
        private RealmAsyncTask transactionTask;

        public ApplyPermissionTask(PermissionManager manager, PermissionRequest request, Callback<Void> callback) {
            super(manager, callback);
            this.unmanagedChangeRequest = PermissionChange.fromRequest(request);
            this.changeRequestId = unmanagedChangeRequest.getId();
        }

        @Override
        public void run() {
            if (checkAndReportInvalidState()) {
                return;
            }

            // Save PermissionChange object. It will be synchronized to the server where it will be processed.
            Realm.Transaction transaction = new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    if (checkAndReportInvalidState()) { return; }
                    realm.insertOrUpdate(unmanagedChangeRequest);
                }
            };

            // If the PermissionChange was successfully written to Realm, we need to wait for it to be processed.
            // Register a ChangeListener on the object and wait for the proper response code, which can then be
            // converted to a proper response to the user.
            Realm.Transaction.OnSuccess onSuccess = new Realm.Transaction.OnSuccess() {
                @Override
                public void onSuccess() {
                    if (checkAndReportInvalidState()) { return; }

                    // Find PermissionChange object we just added
                    managedChangeRequest = managementRealm.where(PermissionChange.class)
                            .equalTo("id", changeRequestId)
                            .findFirstAsync();

                    // Wait for it to be processed
                    managedChangeRequest.addChangeListener(new RealmChangeListener<PermissionChange>() {
                        @Override
                        public void onChange(PermissionChange permissionChange) {
                            if (checkAndReportInvalidState()) {
                                managedChangeRequest.removeChangeListener(this);
                                return;
                            }
                            Integer statusCode = permissionChange.getStatusCode();
                            if (statusCode != null) {
                                if (statusCode > 0) {
                                    ErrorCode errorCode = ErrorCode.fromInt(statusCode);
                                    String errorMsg = permissionChange.getStatusMessage();
                                    ObjectServerError error = new ObjectServerError(errorCode, errorMsg);
                                    notifyCallbackError(error);
                                } else if (statusCode == 0) {
                                    notifyCallbackWithSuccess(null);
                                } else {
                                    ErrorCode errorCode = ErrorCode.UNKNOWN;
                                    String errorMsg = "Illegal status code: " + statusCode;
                                    ObjectServerError error = new ObjectServerError(errorCode, errorMsg);
                                    notifyCallbackError(error);
                                }
                            }
                        }
                    });
                }
            };

            // Critical error: The PermissionChange could not be written to the Realm.
            // Report it back to the user.
            Realm.Transaction.OnError onError = new Realm.Transaction.OnError() {
                @Override
                public void onError(Throwable error) {
                    if (checkAndReportInvalidState()) { return; }
                    notifyCallbackError(new ObjectServerError(ErrorCode.UNKNOWN, error));
                }
            };

            // Run
            transactionTask = managementRealm.executeTransactionAsync(transaction, onSuccess, onError);
        }

        @Override
        public void cancel() {
            super.cancel();
            if (transactionTask != null) {
                cancel();
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
            // Closed check need to work around thread confinement
            if (permissionManager.closed) {
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
            boolean defaultPermissionErrorHappened;
            ObjectServerError managementError;
            ObjectServerError permissionError;
            ObjectServerError defaultPermissionError;
            synchronized (permissionManager.errorLock) {
                // Only hold lock while making a safe copy of current error state
                managementErrorHappened = (permissionManager.managementRealmError != null);
                permissionErrorHappened = (permissionManager.permissionRealmError != null);
                defaultPermissionErrorHappened = (permissionManager.defaultPermissionRealmError != null);
                managementError = permissionManager.managementRealmError;
                permissionError = permissionManager.permissionRealmError;
                defaultPermissionError = permissionManager.defaultPermissionRealmError;
            }

            // Everything seems valid
            if (!permissionErrorHappened && !managementErrorHappened && !defaultPermissionErrorHappened) {
                return false;
            }

            // Handle errors
            Map<String, ObjectServerError> errors = new LinkedHashMap<>();
            if (managementErrorHappened) { errors.put("Management Realm", managementError); }
            if (permissionErrorHappened) { errors.put("Permission Realm", permissionError); }
            if (defaultPermissionErrorHappened) { errors.put("Default Permission Realm", defaultPermissionError); }
            notifyCallbackError(combineRealmErrors(errors));
            return true;
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
        private ObjectServerError combineRealmErrors(Map<String, ObjectServerError> errors) {

            String errorMsg = combineErrorMessage(errors);
            ErrorCode errorCode = combineErrorCodes(errors);

            return new ObjectServerError(errorCode, errorMsg);
        }

        // Combine the text based error message from two ObjectServerErrrors.
        private String combineErrorMessage(Map<String, ObjectServerError> errors) {
            boolean multipleErrors = errors.size() > 1;
            StringBuilder errorMsg = new StringBuilder(multipleErrors ? "Multiple errors occurred: " : "Error occurred in Realm: ");
            for (Map.Entry<String, ObjectServerError> entry : errors.entrySet()) {
                errorMsg.append('\n');
                errorMsg.append(entry.getKey());
                errorMsg.append('\n');
                errorMsg.append(entry.getValue().toString());
            }
            return errorMsg.toString();
        }

        private ErrorCode combineErrorCodes(Map<String, ObjectServerError> errors) {
            ErrorCode finalErrorCode = null;
            for (ObjectServerError error : errors.values()) {
                ErrorCode errorCode = error.getErrorCode();
                if (finalErrorCode == null) {
                    finalErrorCode = errorCode;
                    continue;
                }
                if (errorCode == finalErrorCode) {
                    continue;
                }

                // Multiple error codes. No good way to report this.
                // The real error codes will still be in the error text.
                finalErrorCode = ErrorCode.UNKNOWN;
                break;
            }
            return finalErrorCode;
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
