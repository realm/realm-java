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

package io.realm.android;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import io.realm.SyncUser;
import io.realm.UserStore;

/**
 * A User Store backed by a SharedPreferences file.
 */
public class SharedPrefsUserStore implements UserStore {

    private final SharedPreferences sp;
    private SyncUser cachedCurrentUser; // Keep a quick reference to the current user

    public SharedPrefsUserStore(Context context) {
        sp = context.getSharedPreferences("realm_object_server_users", Context.MODE_PRIVATE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SyncUser put(String key, SyncUser user) {
        String previousUser = sp.getString(key, null);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, user.toJson());
        // Optimistically save. If the user isn't saved due to a process crash it isn't dangerous.
        editor.apply();

        if (UserStore.CURRENT_USER_KEY.equals(key)) {
            cachedCurrentUser = user;
        }

        if (previousUser != null) {
            return SyncUser.fromJson(previousUser);
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SyncUser get(String key) {
        if (UserStore.CURRENT_USER_KEY.equals(key) && cachedCurrentUser != null) {
            return cachedCurrentUser;
        }

        String userData = sp.getString(key, "");
        if (userData.equals("")) {
            return null;
        }

        SyncUser user = SyncUser.fromJson(userData);
        if (UserStore.CURRENT_USER_KEY.equals(key)) {
            cachedCurrentUser = user;
        }
        return user;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SyncUser remove(String key) {
        String currentUser = sp.getString(key, null);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, null);
        editor.apply();

        if (UserStore.CURRENT_USER_KEY.equals(key) && cachedCurrentUser != null) {
            cachedCurrentUser = null;
        }

        if (currentUser != null) {
            return SyncUser.fromJson(currentUser);
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<SyncUser> allUsers() {
        Map<String, ?> all = sp.getAll();
        ArrayList<SyncUser> users = new ArrayList<SyncUser>(all.size());
        for (Object userJson : all.values()) {
            users.add(SyncUser.fromJson((String) userJson));
        }
        return users;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        Set<String> all = sp.getAll().keySet();
        SharedPreferences.Editor editor = sp.edit();
        for (String key : all) {
            editor.remove(key);
        }
        editor.apply();
    }
}
