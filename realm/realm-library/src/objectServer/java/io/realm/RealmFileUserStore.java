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

/**
 * A User Store backed by a Realm file to store user.
 */
public class RealmFileUserStore implements UserStore {

    /**
     * {@inheritDoc}
     */
    @Override
    public void put(SyncUser user) {
        String userJson = user.toJson();
        // create or update token (userJson) using identity
        nativeUpdateOrCreateUser(user.getIdentity(), userJson, user.getSyncUser().getAuthenticationUrl().toString(), user.isAdmin());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SyncUser getCurrent() {
        String userJson = nativeGetCurrentUser();
        return toSyncUserOrNull(userJson);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SyncUser get(String identity) {
        String userJson = nativeGetUser(identity);
        return toSyncUserOrNull(userJson);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(String identity) {
        nativeLogoutUser(identity);
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

    private static SyncUser toSyncUserOrNull(String userJson) {
        if (userJson == null) {
            return null;
        }
        return SyncUser.fromJson(userJson);
    }

    // returns json data (token) of the current logged in user
    protected static native String nativeGetCurrentUser();

    // returns json data (token) of the specified user
    protected static native String nativeGetUser(String identity);

    protected static native String[] nativeGetAllUsers();

    protected static native void nativeUpdateOrCreateUser(String identity, String jsonToken, String url, boolean isAdmin);

    protected static native void nativeLogoutUser(String identity);
}
