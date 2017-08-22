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

package io.realm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nullable;


/**
 * A User Store backed by a Realm file to store users.
 */
public class RealmFileUserStore implements UserStore {

    /**
     * {@inheritDoc}
     */
    @Override
    public void put(SyncUser user) {
        String userJson = user.toJson();
        // create or update token (userJson) using identity
        nativeUpdateOrCreateUser(user.getIdentity(), userJson, user.getAuthenticationUrl().toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public SyncUser getCurrent() {
        String userJson = nativeGetCurrentUser();
        return toSyncUserOrNull(userJson);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public SyncUser get(String identity, String authUrl) {
        String userJson = nativeGetUser(identity, authUrl);
        return toSyncUserOrNull(userJson);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(String identity, String authUrl) {
        nativeLogoutUser(identity, authUrl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<SyncUser> allUsers() {
        String[] allUsers = nativeGetAllUsers();
        if (allUsers != null && allUsers.length > 0) {
            ArrayList<SyncUser> users = new ArrayList<SyncUser>(allUsers.length);
            for (String userJson : allUsers) {
                users.add(SyncUser.fromJson(userJson));
            }
            return users;
        }
        return Collections.emptyList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isActive(String identity, String authenticationUrl) {
        return nativeIsActive(identity, authenticationUrl);
    }

    @Nullable
    private static SyncUser toSyncUserOrNull(@Nullable String userJson) {
        if (userJson == null) {
            return null;
        }
        return SyncUser.fromJson(userJson);
    }

    // returns json data (token) of the current logged in user
    protected static native String nativeGetCurrentUser();

    // returns json data (token) of the specified user
    @Nullable
    protected static native String nativeGetUser(String identity, String authUrl);

    protected static native String[] nativeGetAllUsers();

    protected static native void nativeUpdateOrCreateUser(String identity, String jsonToken, String url);

    protected static native void nativeLogoutUser(String identity, String authUrl);

    protected static native boolean nativeIsActive(String identity, String authUrl);
}
