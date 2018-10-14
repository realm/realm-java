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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.realm.internal.OsRealmConfig;
import io.realm.internal.Util;
import io.realm.internal.permissions.BasePermissionApi;
import io.realm.internal.permissions.ManagementModule;
import io.realm.internal.permissions.PermissionChange;
import io.realm.internal.permissions.PermissionModule;
import io.realm.internal.permissions.PermissionOfferResponse;
import io.realm.log.RealmLog;
import io.realm.permissions.Permission;
import io.realm.permissions.PermissionOffer;
import io.realm.permissions.PermissionRequest;


/**
 * Helper class for interacting with Realm Object Server permissions for a {@link SyncUser}.
 * <p>
 * Current functionality supported by this class:
 * <ul>
 *     <li>List users existing permissions.</li>
 *     <li>List default permissions.</li>
 *     <li>Modify permissions for a Realm.</li>
 *     <li>Create a permission offer that can be sent to others.</li>
 *     <li>Accept permission offers sent by other users.</li>
 * </ul>
 * <p>
 * This class depends on underlying Realms, so all data coming from this class is thread-confined and must be
 * closed after use to avoid leaking resources.
 *
 * @see <a href="https://realm.io/docs/java/latest/#access-control">How to work with Access Controls</a>
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
        DEFAULT_PERMISSION_REALM("__wildcardpermissions", true),
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
    final SyncConfiguration managementRealmConfig;
    final SyncConfiguration permissionRealmConfig;
    final SyncConfiguration defaultPermissionRealmConfig;
    private Realm permissionRealm;
    private Realm managementRealm;
    private Realm defaultPermissionRealm;

    // Task list used to queue tasks until the underlying Realms are done opening (or failed doing so).
    private List<PermissionManagerTask> delayedTasks = new ArrayList<>();

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

    // A client reset was encountered in one of the Realms.
    // This has invalidated the PermissionManager and it must be closed as soon as possible.
    // This flag purely used to be able to send a proper error message to users.
    private boolean clientReset = false;


    // Cached result of the permission query. This will be filled, once the first PermissionAsyncTask has loaded
    // the result.
    private RealmResults<Permission> userPermissions;
    private RealmResults<Permission> defaultPermissions;
    private RealmResults<PermissionOffer> offers;

    /**
     * Creates a PermissionManager for the given user.
     *
     * This class is thread confined, so thread safety is not a concern since all internal
     * communication is routed through the original Handler thread.
     *
     * @param user user to create manager for.
     */
    private PermissionManager(SyncUser user) {
        this.user = user;
        threadId = Thread.currentThread().getId();
        managementRealmConfig = user.createConfiguration(getRealmUrl(RealmType.MANAGEMENT_REALM, user.getAuthenticationUrl()))
                .fullSynchronization()
                .errorHandler(new SyncSession.ErrorHandler() {
                    @Override
                    public void onError(SyncSession session, ObjectServerError error) {
                        synchronized (errorLock) {
                            managementRealmError = error;
                        }
                    }
                })
                .modules(new ManagementModule())
                .sessionStopPolicy(OsRealmConfig.SyncSessionStopPolicy.IMMEDIATELY)
                .build();

        permissionRealmConfig = user.createConfiguration(getRealmUrl(RealmType.PERMISSION_REALM, user.getAuthenticationUrl()))
                .fullSynchronization()
                .errorHandler(new SyncSession.ErrorHandler() {
                    @Override
                    public void onError(SyncSession session, ObjectServerError error) {
                        RealmLog.error("Error in __permission:\n" + error.toString());
                        synchronized (errorLock) {
                            permissionRealmError = error;
                        }
                    }
                })
                .modules(new PermissionModule())
                .waitForInitialRemoteData()
                 // .readOnly() Temporarily disabled due to issues with ROS 3.0.0-alpha.X
                .sessionStopPolicy(OsRealmConfig.SyncSessionStopPolicy.IMMEDIATELY)
                .build();

        defaultPermissionRealmConfig = user.createConfiguration(getRealmUrl(RealmType.DEFAULT_PERMISSION_REALM, user.getAuthenticationUrl()))
                .fullSynchronization()
                .errorHandler(new SyncSession.ErrorHandler() {
                    @Override
                    public void onError(SyncSession session, ObjectServerError error) {
                        RealmLog.error("Error in __wildcardpermissions:\n" + error.toString());
                        synchronized (errorLock) {
                            defaultPermissionRealmError = error;
                        }
                    }
                })
                .modules(new PermissionModule())
                .waitForInitialRemoteData()
                .readOnly()
                .sessionStopPolicy(OsRealmConfig.SyncSessionStopPolicy.IMMEDIATELY)
                .build();
    }

    /**
     * Retrieves the list of permissions for all Realms available to this user.
     *
     * @param callback callback notified when the permissions are ready. The returned {@link RealmResults} is a fully
     * live query result, that will be auto-updated like any other {@link RealmResults}.
     * @return {@link RealmAsyncTask} that can be used to cancel the task if needed.
     */
    public RealmAsyncTask getPermissions(PermissionsCallback callback) {
        checkIfValid();
        checkCallbackNotNull(callback);
        return addTask(new GetPermissionsAsyncTask(this, callback));
    }

    /**
     * NOTE: Moved out of the public API until we know for sure how this is going to work.
     *
     * Returns default permissions for all Realms. The default permissions are the ones that will be used if no
     * user specific permissions is in effect.
     *
     * @param callback callback notified when the permissions are ready. The returned {@link RealmResults} is a fully
     * live query result, that will be auto-updated like any other {@link RealmResults}.
     * @return {@link RealmAsyncTask} that can be used to cancel the task if needed.
     */
    public RealmAsyncTask getDefaultPermissions(PermissionsCallback callback) {
        checkIfValid();
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
     * {@link PermissionManager} and can be found using {@link PermissionManager#getPermissions(PermissionsCallback)}.
     *
     * @param request request object describing which permissions to grant and to what Realm(s).
     * @param callback callback when the request either succeeded or failed.
     * @return async task representing the request. This can be used to cancel it if needed.
     */
    public RealmAsyncTask applyPermissions(PermissionRequest request, ApplyPermissionsCallback callback) {
        checkIfValid();
        checkCallbackNotNull(callback);
        return addTask(new ApplyPermissionTask(this, request, callback));
    }

    /**
     * Makes a permission offer to users. The offer is represented by an offer token and the permission changes
     * described in the {@link PermissionOffer} do not take effect until the offer has been accepted by a user
     * calling {@link #acceptOffer(String, AcceptOfferCallback)}.
     * <p>
     * A permission offer can be used as a flexible way of sharing Realms with other users that might not be known at the time
     * of making the offer as well as enabling sharing across other channels like e-mail. If a specific user should be
     * granted access, using {@link #applyPermissions(PermissionRequest, ApplyPermissionsCallback)} will be faster and quicker.
     * <p>
     * An offer can be accepted by multiple users.
     *
     * @param callback callback to be notified with the offer token once it is ready.
     * @return {@link RealmAsyncTask} that can be used to cancel the task if needed.
     * @see <a href="https://realm.io/docs/realm-object-server/#permissions">Permissions description</a> for general
     * documentation.
     * @see <a href="https://realm.io/docs/java/latest/#modifying-permissions">Modifying permissions</a> for a more
     * high level description.
     */
    public RealmAsyncTask makeOffer(PermissionOffer offer, MakeOfferCallback callback) {
        checkIfValid();
        checkCallbackNotNull(callback);
        if (offer.isOfferCreated()) {
            throw new IllegalStateException("Offer has already been created: " + offer);
        }
        return addTask(new MakeOfferAsyncTask(this, offer, callback));
    }

    /**
     * Accepts a permission offer sent by another user. Once this offer is accepted successfully, the permissions
     * described by the token will be granted.
     *
     * @param offerToken token representing the permission offer.
     * @param callback with the permission details that were accepted.
     * @return {@link RealmAsyncTask} that can be used to cancel the task if needed.
     */
    public RealmAsyncTask acceptOffer(String offerToken, AcceptOfferCallback callback) {
        checkIfValid();
        checkCallbackNotNull(callback);
        if (Util.isEmptyString(offerToken)) {
            throw new IllegalArgumentException("Non-empty 'offerToken' required.");
        }
        return addTask(new AcceptOfferAsyncTask(this, offerToken, callback));
    }

    /**
     * Revokes an existing offer. This will prevent any other users from accepting it. Users that already accepted it,
     * will not be affected. Revocation cannot happen until the device has talked to the server. The callback will
     * not be notified until this has happened.
     *
     * @param offerToken token that should be revoked.
     * @return {@link RealmAsyncTask} that can be used to cancel the task if needed.
     */
    public RealmAsyncTask revokeOffer(String offerToken, RevokeOfferCallback callback) {
        checkIfValid();
        checkCallbackNotNull(callback);
        return addTask(new RevokeOfferAsyncTask(this, offerToken, callback));
    }

    /**
     * Returns the list of offers created by this user. These offers can be revoked again by calling
     * {@link #revokeOffer(String, RevokeOfferCallback)} or sent to other users by sending the
     * {@link PermissionOffer#getToken()}.
     *
     * @return {@link RealmAsyncTask} that can be used to cancel the task if needed.
     */
    public RealmAsyncTask getCreatedOffers(OffersCallback callback) {
        checkIfValid();
        checkCallbackNotNull(callback);
        return addTask(new GetOffersAsyncTask(this, callback));
    }

    // Queue the task if the underlying Realms are not ready yet, otherwise
    // start the task by sending it to this thread handler. This is done
    // in order to be able to provide the user with a RealmAsyncTask representation
    // of the work being done.
    private RealmAsyncTask addTask(PermissionManagerTask task) {
        if (isReady()) {
            activateTask(task);
        } else {
            delayTask(task);
            openRealms();
        }

        return task;
    }

    // Park the task until all underlying Realms are ready
    private void delayTask(PermissionManagerTask task) {
        delayedTasks.add(task);
    }

    // Run any tasks that were delayed while the underlying Realms were being opened.
    // PRECONDITION: Underlying Realms are no longer in the process of being opened.
    private void runDelayedTasks() {
        for (PermissionManagerTask delayedTask : delayedTasks) {
            activateTask(delayedTask);
        }
        delayedTasks.clear();
    }

    // Activate a task. All tasks are controlled by the Handler in order to make it asynchronous.
    // PRECONDITION: Underlying Realms are no longer in the process of being opened.
    private void activateTask(PermissionManagerTask task) {
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
                    managementRealmOpenTask = null;
                    checkIfRealmsAreOpenedAndRunDelayedTasks();
                }

                @Override
                public void onError(Throwable exception) {
                    synchronized (errorLock) {
                        managementRealmError = new ObjectServerError(ErrorCode.UNKNOWN, exception);
                        managementRealmOpenTask = null;
                        checkIfRealmsAreOpenedAndRunDelayedTasks();
                    }
                }
            });
            permissionRealmOpenTask = Realm.getInstanceAsync(permissionRealmConfig, new Realm.Callback() {
                @Override
                public void onSuccess(Realm realm) {
                    permissionRealm = realm;
                    permissionRealmOpenTask = null;
                    checkIfRealmsAreOpenedAndRunDelayedTasks();
                }

                @Override
                public void onError(Throwable exception) {
                    synchronized (errorLock) {
                        permissionRealmError = new ObjectServerError(ErrorCode.UNKNOWN, exception);
                        permissionRealmOpenTask = null;
                        checkIfRealmsAreOpenedAndRunDelayedTasks();
                    }
                }
            });
            defaultPermissionRealmOpenTask = Realm.getInstanceAsync(defaultPermissionRealmConfig, new Realm.Callback() {
                @Override
                public void onSuccess(Realm realm) {
                    defaultPermissionRealm = realm;
                    defaultPermissionRealmOpenTask = null;
                    checkIfRealmsAreOpenedAndRunDelayedTasks();
                }

                @Override
                public void onError(Throwable exception) {
                    synchronized (errorLock) {
                        defaultPermissionRealmError = new ObjectServerError(ErrorCode.UNKNOWN, exception);
                        defaultPermissionRealmOpenTask = null;
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

    private void checkCallbackNotNull(PermissionManagerBaseCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Non-null 'callback' required.");
        }
    }

    private boolean isReady() {
        return managementRealm != null && permissionRealm != null; // && defaultPermissionRealm != null;
    }

    private void checkIfValid() {
        // Checks if we are in thread that created the PermissionManager.
        if (threadId != Thread.currentThread().getId()) {
            throw new IllegalStateException("PermissionManager was accessed from the wrong thread. It can only be " +
                                            "accessed on the thread it was created on.");
        }

        if (closed) {
            throw new IllegalStateException("PermissionManager has been closed. No further actions are possible.");
        }
    }

    /**
     * Closes the PermissionManager as well as any underlying Realms.
     * Any active tasks in progress will be canceled.
     */
    @Override
    public void close() {
        checkIfValid();

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
        closed = true;
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
    }

    /**
     * Checks if this PermissionManager is closed or not. If it is closed, all methods will report back an error.
     *
     * @return {@code true} if the PermissionManager is closed, {@code false} if it is still open.
     */
    public boolean isClosed() {
        // Don't use `checkIfValid()` as it throws because closed might be false.
        if (threadId != Thread.currentThread().getId()) {
            throw new IllegalStateException("PermissionManager was accessed from the wrong thread. It can only be " +
                    "accessed on the thread it was created on.");
        }
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
    private class GetPermissionsAsyncTask extends PermissionManagerTask<RealmResults<Permission>> {

        private final PermissionsCallback callback;
        // Prevent permissions from being GC'ed until fully loaded.
        private RealmResults<Permission> loadingPermissions;

        GetPermissionsAsyncTask(PermissionManager permissionManager, PermissionsCallback callback) {
            super(permissionManager, callback);
            this.callback = callback;
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
                        RealmLog.error(String.format("1stCallback: Size: %s, Permissions: %s", loadedPermissions.size(), Arrays.toString(loadedPermissions.toArray())));
                        // Don't report ready until both __permission and __management Realm are there
                        if (loadedPermissions.size() > 1) {
                            loadingPermissions.removeChangeListener(this);
                            loadingPermissions = null;
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

        void notifyCallbackWithSuccess(RealmResults<Permission> permissions) {
            try {
                callback.onSuccess(permissions);
            } finally {
                activeTasks.remove(this);
            }
        }
    }

    // Task responsible for loading the Default Permissions result and returning it to the user.
    // The Permission result is not considered available until the query has completed.
    private class GetDefaultPermissionsAsyncTask extends PermissionManagerTask<RealmResults<Permission>> {

        private final PermissionsCallback callback;
        // Prevent permissions from being GC'ed until fully loaded.
        private RealmResults<Permission> loadingPermissions;

        GetDefaultPermissionsAsyncTask(PermissionManager permissionManager, PermissionsCallback callback) {
            super(permissionManager, callback);
            this.callback = callback;
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
                loadingPermissions = defaultPermissionRealm.where(Permission.class).findAllAsync();
                loadingPermissions.addChangeListener(new RealmChangeListener <RealmResults<Permission>>() {
                    @Override
                    public void onChange(RealmResults <Permission> loadedPermissions) {
                        // Wildcard permissions should contain 1 Realm as the default, namely __wildcardpermissions
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

        void notifyCallbackWithSuccess(RealmResults<Permission> permissions) {
            try {
               callback.onSuccess(permissions);
            } finally {
                activeTasks.remove(this);
            }
        }
    }

    // Class encapsulating setting a Permission by writing a PermissionChange and waiting for it to
    // be processed.
    private class ApplyPermissionTask extends PermissionManagerTask<Void> {

        private final PermissionChange unmanagedChangeRequest;
        private final ApplyPermissionsCallback callback;
        private final String changeRequestId;
        private PermissionChange managedChangeRequest;
        private RealmAsyncTask transactionTask;

        public ApplyPermissionTask(PermissionManager manager, PermissionRequest request, ApplyPermissionsCallback callback) {
            super(manager, callback);
            this.unmanagedChangeRequest = PermissionChange.fromRequest(request);
            this.changeRequestId = unmanagedChangeRequest.getId();
            this.callback = callback;
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
                    RealmObject.addChangeListener(managedChangeRequest, new RealmChangeListener<PermissionChange>() {
                        @Override
                        public void onChange(PermissionChange permissionChange) {
                            if (checkAndReportInvalidState()) {
                                RealmObject.removeChangeListener(managedChangeRequest, this);
                                return;
                            }
                            handleServerStatusChanges(permissionChange, new Runnable() {
                                @Override
                                public void run() {
                                    notifyCallbackWithSuccess();
                                }
                            });
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
                    notifyCallbackWithError(new ObjectServerError(ErrorCode.UNKNOWN, error));
                }
            };

            // Run
            transactionTask = managementRealm.executeTransactionAsync(transaction, onSuccess, onError);
        }

        void notifyCallbackWithSuccess() {
            try {
                callback.onSuccess();
            } finally {
                activeTasks.remove(this);
            }
        }

        @Override
        public void cancel() {
            super.cancel();
            if (transactionTask != null) {
                cancel();
            }
        }
    }

    private class MakeOfferAsyncTask extends PermissionManagerTask<String> {

        private final PermissionOffer unmanagedOffer;
        private final String offerId;
        private final MakeOfferCallback callback;
        private PermissionOffer managedOffer;
        private RealmAsyncTask transactionTask;

        public MakeOfferAsyncTask(PermissionManager permissionManager, PermissionOffer offer, MakeOfferCallback callback) {
            super(permissionManager, callback);
            this.unmanagedOffer = offer;
            this.offerId = offer.getId();
            this.callback = callback;
        }

        @Override
        public void run() {
            if (checkAndReportInvalidState()) {
                return;
            }

            // Save PermissionOffer object. It will be synchronized to the server where it will be processed.
            Realm.Transaction transaction = new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    if (checkAndReportInvalidState()) { return; }
                    realm.insertOrUpdate(unmanagedOffer);
                }
            };

            // If the PermissionOffer was successfully written to Realm, we need to wait for it to be processed.
            // Register a ChangeListener on the object and wait for the proper response code, which can then be
            // converted to a proper response to the user.
            Realm.Transaction.OnSuccess onSuccess = new Realm.Transaction.OnSuccess() {
                @Override
                public void onSuccess() {
                    if (checkAndReportInvalidState()) { return; }

                    // Find PermissionChange object we just added
                    // Wait for it to be processed
                    managedOffer = managementRealm.where(PermissionOffer.class).equalTo("id", offerId).findFirstAsync();
                    RealmObject.addChangeListener(managedOffer, new RealmChangeListener<PermissionOffer>() {
                        @Override
                        public void onChange(final PermissionOffer permissionOffer) {
                            if (checkAndReportInvalidState()) {
                                RealmObject.removeChangeListener(managedOffer, this);
                                return;
                            }
                            handleServerStatusChanges(permissionOffer, new Runnable() {
                                @Override
                                public void run() {
                                    notifyCallbackWithSuccess(permissionOffer.getToken());
                                }
                            });
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
                    notifyCallbackWithError(new ObjectServerError(ErrorCode.UNKNOWN, error));
                }
            };

            // Run
            transactionTask = managementRealm.executeTransactionAsync(transaction, onSuccess, onError);
        }

        void notifyCallbackWithSuccess(String token) {
            try {
                callback.onSuccess(token);
            } finally {
                activeTasks.remove(this);
            }
        }

        @Override
        public void cancel() {
            super.cancel();
            if (transactionTask != null) {
                transactionTask.cancel();
                transactionTask = null;
            }
        }
    }

    private class AcceptOfferAsyncTask extends PermissionManagerTask<Permission> {

        private final PermissionOfferResponse unmanagedResponse;
        private final String responseId;
        private final AcceptOfferCallback callback;
        private PermissionOfferResponse managedResponse;
        private RealmAsyncTask transactionTask;
        public RealmResults<Permission> grantedPermissionResults;

        public AcceptOfferAsyncTask(PermissionManager permissionManager, String offerToken, AcceptOfferCallback callback) {
            super(permissionManager, callback);
            this.unmanagedResponse = new PermissionOfferResponse(offerToken);
            this.responseId = unmanagedResponse.getId();
            this.callback = callback;
        }

        @Override
        public void run() {
            if (checkAndReportInvalidState()) {
                return;
            }

            // Save response object. It will be synchronized to the server where it will be processed.
            Realm.Transaction transaction = new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    if (checkAndReportInvalidState()) { return; }
                    realm.insertOrUpdate(unmanagedResponse);
                }
            };

            // If the response was successfully written to Realm, we need to wait for it to be processed.
            // Register a ChangeListener on the object and wait for the proper response code, which can then be
            // converted to a proper response to the user.
            Realm.Transaction.OnSuccess onSuccess = new Realm.Transaction.OnSuccess() {
                @Override
                public void onSuccess() {
                    if (checkAndReportInvalidState()) { return; }

                    // Find PermissionOffer object we just added
                    // Wait for it to be processed
                    managedResponse = managementRealm.where(PermissionOfferResponse.class).equalTo("id", responseId).findFirstAsync();
                    RealmObject.addChangeListener(managedResponse, new RealmChangeListener<PermissionOfferResponse>() {
                        @Override
                        public void onChange(final PermissionOfferResponse response) {
                            if (checkAndReportInvalidState()) {
                                RealmObject.removeChangeListener(managedResponse, this);
                                return;
                            }
                            handleServerStatusChanges(response, new Runnable() {
                                @Override
                                public void run() {
                                    grantedPermissionResults = permissionRealm.where(Permission.class).equalTo("path", response.getPath()).findAllAsync();
                                    grantedPermissionResults.addChangeListener(new RealmChangeListener<RealmResults<Permission>>() {
                                        @Override
                                        public void onChange(RealmResults<Permission> permissions) {
                                            if (!permissions.isEmpty()) {
                                                grantedPermissionResults.removeChangeListener(this);
                                                //noinspection ConstantConditions
                                                notifyCallbackWithSuccess(managedResponse.getRealmUrl(), permissions.first());
                                            }
                                        }
                                    });
                                }
                            });
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
                    notifyCallbackWithError(new ObjectServerError(ErrorCode.UNKNOWN, error));
                }
            };

            // Run
            transactionTask = managementRealm.executeTransactionAsync(transaction, onSuccess, onError);
        }

        void notifyCallbackWithSuccess(String url, Permission permission) {
            try {
                callback.onSuccess(url, permission);
            } finally {
                activeTasks.remove(this);
            }
        }

        @Override
        public void cancel() {
            super.cancel();
            if (transactionTask != null) {
                transactionTask.cancel();
                transactionTask = null;
            }
        }
    }

    // Class encapsulating all async tasks exposed by the PermissionManager.
    // Made package protected instead of private to facilitate testing
    // IMPORTANT:
    // - All subclasses are responsible for removing themselves from the activeTaskList when done.
    // - All callbacks should start by checking `if (checkAndReportInvalidState()) { return; }`
    //   This will abort the task if it was canceled or failed. It will also remove the task from the activeTaskList.
    abstract static class PermissionManagerTask<T> implements RealmAsyncTask, Runnable {

        private final PermissionManagerBaseCallback callback;
        private final PermissionManager permissionManager;
        private volatile boolean canceled = false;
        private static final String ERROR_MESSAGE_CLIENT_RESET = "The PermissionManager " +
                "has been invalidated due to a server conflict. No further tasks can be scheduled. " +
                "The app needs to be restarted to allow the PermissionManager to work again.";

        public PermissionManagerTask(PermissionManager permissionManager, PermissionManagerBaseCallback callback) {
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
            if (isCancelled()) {
                permissionManager.activeTasks.remove(this);
                return true;
            }
            // Closed check need to work around thread confinement
            if (permissionManager.closed) {
                ObjectServerError error = new ObjectServerError(ErrorCode.UNKNOWN,
                        new IllegalStateException("PermissionManager has been closed"));
                notifyCallbackWithError(error); // This will remove the task from the task list
                return true;
            }
            if (permissionManager.clientReset) {
                ObjectServerError error = new ObjectServerError(ErrorCode.CLIENT_RESET, ERROR_MESSAGE_CLIENT_RESET);
                notifyCallbackWithError(error);
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
            if (!permissionErrorHappened && !managementErrorHappened) {// && !defaultPermissionErrorHappened) {
                return false;
            }

            // Handle Client Reset if it happened in any of the Realms.
            // A Client Reset is a fatal error for the PermissionManager, so all current and
            // future tasks will exit as soon as possible after this event happened and report it
            // through the error callback. Only action a user can take is to close the
            // PermissionManager and re-open it again. Some data might be lost (like permission
            // offers not yet processed). This is currently unavoidable.
            // TODO: Eventually we might be able to recover the permission manager from this event
            // but it will require some serious task management as we would need to do a full
            // close, reschedule all tasks, and re-open behind users back. This is out of scope for
            // now.
            if (managementErrorHappened && managementError instanceof ClientResetRequiredError) {
                ClientResetRequiredError cr = (ClientResetRequiredError) managementError;
                permissionManager.managementRealm.close();
                cr.executeClientReset();
                permissionManager.clientReset = true;
            }

            if (permissionErrorHappened && permissionError instanceof ClientResetRequiredError) {
                ClientResetRequiredError cr = (ClientResetRequiredError) permissionError;
                permissionManager.permissionRealm.close();
                cr.executeClientReset();
                permissionManager.clientReset = true;
            }

            if (defaultPermissionErrorHappened && defaultPermissionError instanceof ClientResetRequiredError) {
                ClientResetRequiredError cr = (ClientResetRequiredError) defaultPermissionError;
                permissionManager.defaultPermissionRealm.close();
                cr.executeClientReset();
                permissionManager.clientReset = true;
            }

            // Handle errors
            Map<String, ObjectServerError> errors = new LinkedHashMap<>();
            if (permissionManager.clientReset) {
                errors.put("ClientReset", new ObjectServerError(ErrorCode.CLIENT_RESET, ERROR_MESSAGE_CLIENT_RESET));
            } else {
                if (managementErrorHappened) { errors.put("Management Realm", managementError); }
                if (permissionErrorHappened) { errors.put("Permission Realm", permissionError); }
                if (defaultPermissionErrorHappened) { errors.put("Default Permission Realm", defaultPermissionError); }
            }
            notifyCallbackWithError(combineRealmErrors(errors)); // This will remove the task from the task list

            return true;
        }

        /**
         * Handle the status change from ROS and either call error or success callbacks.
         */
        protected void handleServerStatusChanges(BasePermissionApi obj, Runnable onSuccessDelegate) {
            Integer statusCode = obj.getStatusCode();
            if (statusCode != null) {
                RealmObject.removeAllChangeListeners(obj);
                if (statusCode > 0) {
                    ErrorCode errorCode = ErrorCode.fromInt(statusCode);
                    String errorMsg = obj.getStatusMessage();
                    ObjectServerError error = new ObjectServerError(errorCode, errorMsg);
                    notifyCallbackWithError(error);
                } else if (statusCode == 0) {
                    onSuccessDelegate.run();
                } else {
                    ErrorCode errorCode = ErrorCode.UNKNOWN;
                    String errorMsg = "Illegal status code: " + statusCode;
                    ObjectServerError error = new ObjectServerError(errorCode, errorMsg);
                    notifyCallbackWithError(error);
                }
            }
        }

        protected final void notifyCallbackWithError(ObjectServerError e) {
            RealmLog.debug("Error happened in PermissionManager for %s: %s",
                    permissionManager.user.getIdentity(), e.toString());
            try {
                callback.onError(e);
            } finally {
                permissionManager.activeTasks.remove(this);
            }
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

    // Task responsible for loading the Permissions result and returning it to the user.
    // The Permission result is not considered available until the query has completed.
    private class GetOffersAsyncTask extends PermissionManagerTask<RealmResults<Permission>> {

        private final OffersCallback callback;
        // Prevent permissions from being GC'ed until fully loaded.
        private RealmResults<PermissionOffer> loadingOffers;

        GetOffersAsyncTask(PermissionManager permissionManager, OffersCallback callback) {
            super(permissionManager, callback);
            this.callback = callback;
        }

        @Override
        public void run() {
            if (checkAndReportInvalidState()) { return; }
            if (offers != null) {
                notifyCallbackWithSuccess(offers);
            } else {
                // We only want offers that have been created.
                loadingOffers = managementRealm.where(PermissionOffer.class)
                        .equalTo("statusCode", 0)
                        .findAllAsync();
                loadingOffers.addChangeListener(new RealmChangeListener<RealmResults<PermissionOffer>>() {
                    @Override
                    public void onChange(RealmResults<PermissionOffer> loadedOffers) {
                        loadedOffers.removeChangeListener(this);
                        if (checkAndReportInvalidState()) { return; }
                        if (offers == null) {
                            offers = loadedOffers;
                        }
                        notifyCallbackWithSuccess(offers);
                    }
                });
            }
        }

        void notifyCallbackWithSuccess(RealmResults<PermissionOffer> permissions) {
            try {
                callback.onSuccess(permissions);
            } finally {
                activeTasks.remove(this);
            }
        }
    }

    private class RevokeOfferAsyncTask extends PermissionManagerTask<Permission> {

        private final String offerToken;
        private final RevokeOfferCallback callback;
        private RealmResults<PermissionOffer> matchingOffers;

        public RevokeOfferAsyncTask(PermissionManager permissionManager, String offerToken, RevokeOfferCallback callback) {
            super(permissionManager, callback);
            this.offerToken = offerToken;
            this.callback = callback;
        }

        @Override
        public void run() {
            if (checkAndReportInvalidState()) {
                return;
            }
            matchingOffers = managementRealm.where(PermissionOffer.class)
                    .equalTo("token", offerToken)
                    .findAllAsync();
            matchingOffers.addChangeListener(new RealmChangeListener<RealmResults<PermissionOffer>>() {
                @Override
                public void onChange(final RealmResults<PermissionOffer> offers) {
                    if (checkAndReportInvalidState()) { return; }
                    if (!offers.isEmpty()) {
                        managementRealm.executeTransactionAsync(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                if (checkAndReportInvalidState()) { return; }
                                // Make 100% sure the offer is still in the Realm.
                                // It could have been deleted between querying for it and the
                                // transaction running. We will still call OnSuccess if the
                                // offer was removed by someone else.
                                RealmResults<PermissionOffer> offers = realm.where(PermissionOffer.class)
                                        .equalTo("token", offerToken)
                                        .findAll();
                                if (!offers.isEmpty()) {
                                    offers.deleteAllFromRealm();
                                }
                            }
                        }, new Realm.Transaction.OnSuccess() {
                            @Override
                            public void onSuccess() {
                                // Don't notify user about success before changes have been uploaded to the server.
                                matchingOffers.removeAllChangeListeners();
                                if (checkAndReportInvalidState()) { return; }
                                final SyncSession session = SyncManager.getSession(managementRealmConfig);
                                session.addUploadProgressListener(ProgressMode.CURRENT_CHANGES, new ProgressListener() {
                                    @Override
                                    public void onChange(Progress progress) {
                                        if (progress.isTransferComplete()) {
                                            session.removeProgressListener(this);
                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (checkAndReportInvalidState()) { return; }
                                                    notifyCallbackWithSuccess();
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        }, new Realm.Transaction.OnError() {
                            @Override
                            public void onError(Throwable error) {
                                matchingOffers.removeAllChangeListeners();
                                notifyCallbackWithError(new ObjectServerError(ErrorCode.UNKNOWN, error));

                            }
                        });
                    }
                }
            });
        }

        void notifyCallbackWithSuccess() {
            try {
                callback.onSuccess();
            } finally {
                activeTasks.remove(this);
            }
        }
    }

    private interface PermissionManagerBaseCallback {
        /**
         * Called if an error happened while executing the task. The PermissionManager uses different underlying Realms,
         * and this error will report errors from all of these Realms combining them as best as possible.
         * <p>
         * This means that if all Realms fail with the same error code, {@link ObjectServerError#getErrorCode()} will
         * return that error code. If the underlying Realms fail for different reasons, {@link ErrorCode#UNKNOWN} will
         * be returned. {@link ObjectServerError#getErrorMessage()} will always contain the full description of errors
         * including the specific error code for each underlying Realm that failed.
         *
         * @param error error object describing what happened.
         */
        void onError(ObjectServerError error);
    }

    /**
     * Callback used when loading a set of permissions.
     */
    public interface PermissionsCallback extends PermissionManagerBaseCallback {
        /**
         * Called when all known permissions are successfully loaded.
         * <p>
         * These permissions will continue to synchronize with the server in the background. Register a
         * {@link RealmChangeListener} to be notified about any further changes.
         *
         * @param permissions The set of currently known permissions.
         */
        void onSuccess(RealmResults<Permission> permissions);
    }

    /**
     * Callback used when modifying or creating new permissions.
     */
    public interface ApplyPermissionsCallback extends PermissionManagerBaseCallback {
        /**
         * Called when the permissions where successfully modified.
         */
        void onSuccess();
    }

    /**
     * Callback used when making a permission offer for other users.
     */
    public interface MakeOfferCallback extends PermissionManagerBaseCallback {
        /**
         * Called when the offer was successfully created.
         *
         * @param offerToken token representing the offer that can be sent to other users.
         */
        void onSuccess(String offerToken);
    }

    /**
     * Callback used when accepting a permission offer.
     */
    public interface AcceptOfferCallback extends PermissionManagerBaseCallback {
        /**
         * Called when the offer was successfully accepted. This means that this user can now access this Realm.
         *
         * @param realmUrl The url pointing to the Realm for which the offer was created.
         * @param permission The permissions granted.
         */
        void onSuccess(String realmUrl, Permission permission);
    }

    /**
     * Callback used when loading the list of {@link PermissionOffer}'s created by the user.
     */
    public interface OffersCallback extends PermissionManagerBaseCallback {
        /**
         * Called when all known offers are successfully loaded.
         * <p>
         * These offers will continue to synchronize with the server in the background. Register a
         * {@link RealmChangeListener} to be notified about any further changes.
         *
         * @param offers The set of currently known offers.
         */
        void onSuccess(RealmResults<PermissionOffer> offers);
    }

    /**
     * Callback used when revoking an existing offer.
     */
    public interface RevokeOfferCallback extends PermissionManagerBaseCallback {
        /**
         * Called when the offer was successfully revoked successfully modified.
         */
        void onSuccess();
    }

}
