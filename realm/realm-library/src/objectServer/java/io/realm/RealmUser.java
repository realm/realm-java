/*
 * Copyright 2020 Realm Inc.
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;

import io.realm.internal.objectstore.OsJavaNetworkTransport;
import io.realm.internal.objectstore.OsSyncUser;
import io.realm.internal.util.Pair;
import io.realm.internal.Util;

/**
 * FIXME
 */
public class RealmUser {

    OsSyncUser osUser;
    private final RealmApp app;

    /**
     * FIXME
     */
    enum UserType {
        NORMAL("normal"),
        SERVER("server"),
        UNKNOWN("unknown");

        private final String key;

        UserType(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

    public enum State {
        LOGGED_IN(OsSyncUser.STATE_LOGGED_IN),
        REMOVED(OsSyncUser.STATE_REMOVED),
        LOGGED_OUT(OsSyncUser.STATE_LOGGED_OUT);

        private final byte nativeValue;

        State(byte nativeValue) {
            this.nativeValue = nativeValue;
        }

        byte getKey() {
            return nativeValue;
        }
    }


    RealmUser(long nativePtr, RealmApp app) {
        this.osUser = new OsSyncUser(nativePtr);
        this.app = app;
    }

    /**
     * FIXME
     * @return
     */
    public String getId() {
        return osUser.getIdentity();
    }

    /**
     * FIXME
     * @return
     */
    public String getName() {
        return osUser.nativeGetName();
    }

    /**
     * FIXME
     * @return
     */
    @Nullable
    public String getEmail() {
        return osUser.getEmail();
    }

    /**
     * FIXME
     * @return
     */
    @Nullable
    public String getPictureUrl() {
        return osUser.getPictureUrl();
    }

    /**
     * FIXME
     * @return
     */
    @Nullable
    public String getFirstName() {
        return osUser.getFirstName();
    }

    /**
     * FIXME
     * @return
     */
    @Nullable
    public String getLastName() {
        return osUser.getLastName();
    }

    /**
     * FIXME
     * @return
     */
    @Nullable
    public String getGender() {
        return osUser.getGender();
    }

    /**
     * FIXME
     * @return
     */
    @Nullable
    public String getBirthday() {
        return osUser.getBirthday();
    }

    /**
     * FIXME
     * @return
     */
    @Nullable
    public Long getMinAge() {
        String minAge = osUser.getMinAge();
        return (minAge == null) ? null : Long.parseLong(minAge);
    }

    /**
     * FIXME
     * @return
     */
    @Nullable
    public Long getMaxAge() {
        String maxAge = osUser.getMaxAge();
        return (maxAge == null) ? null : Long.parseLong(maxAge);
    }

    /**
     * FIXME
     * @return
     */
    public List<RealmUserIdentity> getIdentities() {
        Pair<String, String>[] osIdentities = osUser.getIdentities();
        List<RealmUserIdentity> identities = new ArrayList<>(osIdentities.length);
        for (int i = 0; i < osIdentities.length; i++) {
            Pair<String, String> data = osIdentities[i];
            identities.add(new RealmUserIdentity(data.first, data.second));
        }
        return identities;
    }

    /**
     * FIXME
     * @return
     */
    public String getAccessToken() {
        return osUser.getAccessToken();
    }

    /**
     * FIXME
     * @return
     */
    public String getRefreshToken() {
        return osUser.getRefreshToken();
    }


    /**
     * Returns the {@link RealmApp} this user is associated with.
     *
     * @return the {@link RealmApp} this user is associated with.
     */
    public RealmApp getApp() {
        return app;
    }

    /**
     * Returns the {@link State} the user is in.
     *
     * @return the {@link State} of the user.
     */
    public State getState() {
        byte nativeState = osUser.getState();
        for (State state : State.values()) {
            if (state.nativeValue == nativeState) {
                return state;
            }
        }
        throw new IllegalStateException("Unknown state: " + nativeState);
    }

    /**
     * Log the current user out of the Realm App asynchronously, destroying their server state, unregistering them from the
     * SDK, and removing any synced Realms associated with them from on-disk storage on next app
     * launch.
     * <p>
     * This method should be called whenever the application is committed to not using a user again.
     * Failing to call this method may result in unused files and metadata needlessly taking up space.
     * <p>
     * Once the Realm App has confirmed the logout any registered {@link AuthenticationListener}
     * will be notified and user credentials will be deleted from this device.
     * <p>
     * Logging out anonymous users will remove them immediately instead of marking them as
     * {@link RealmUser.State#LOGGED_OUT}. All other users will be marked as {@link RealmUser.State#LOGGED_OUT}
     * and will still be returned by {@link #allUsers()}.
     *
     * @throws ObjectServerError if an error occurred while trying to log the user out of the Realm
     * App.
     */
    public void logOut() throws ObjectServerError {
        AtomicReference<ObjectServerError> error = new AtomicReference<>(null);
        nativeLogOut(app.nativePtr, osUser.getNativePtr(), new RealmApp.OsJNIVoidResultCallback(error));
        RealmApp.handleResult(null, error);
    }

    /**
     * Log the user out of the Realm App asynchronously, destroying their server state,
     * unregistering them from the SDK, and removing any synced Realms associated with them from
     * on-disk storage on next app launch.
     * <p>
     * This method should be called whenever the application is committed to not using a user again.
     * Failing to call this method may result in unused files and metadata needlessly taking up space.
     * <p>
     * Once the Realm App has confirmed the logout any registered {@link AuthenticationListener}
     * will be notified and user credentials will be deleted from this device.
     * <p>
     * Logging out anonymous users will remove them immediately instead of marking them as
     * {@link RealmUser.State#LOGGED_OUT}. All other users will be marked as {@link RealmUser.State#LOGGED_OUT}
     * and will still be returned by {@link RealmApp#allUsers()}.
     *
     * @param callback callback when logging out has completed or failed. The callback will always
     * happen on the same thread as this method is called on.
     * @throws IllegalStateException if called from a non-looper thread.
     */
    public RealmAsyncTask logOutAsync(RealmApp.Callback<RealmUser> callback) {
        final RealmUser user = this;
        Util.checkLooperThread("Asynchronous log out is only possible from looper threads.");
        return new RealmApp.Request<RealmUser>(SyncManager.NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public RealmUser run() throws ObjectServerError {
                logOut();
                return user;
            }
        }.start();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RealmUser realmUser = (RealmUser) o;

        if (!osUser.equals(realmUser.osUser)) return false;
        return app.equals(realmUser.app);
    }

    @Override
    public int hashCode() {
        int result = osUser.hashCode();
        result = 31 * result + app.hashCode();
        return result;
    }

    private static native void nativeLogOut(long appNativePtr, long userNativePtr, OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback);
}

