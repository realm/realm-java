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

import java.security.KeyStoreException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import io.realm.User;
import io.realm.UserStore;
import io.realm.internal.android.crypto.CipherClient;

/**
 * Encrypt and decrypt the token ({@link User}) using Android built in KeyStore capabilities.
 * According to the Android API this picks the right algorithm to perfom the operations.
 * Prior to API 18 there were no AndroidKeyStore API, but the UNIX deamon existed to it's possible
 * with the help of this code: https://github.com/nelenkov/android-keystore.
 *
 * On API &gt; = 18, we generate an AES key to encrypt we then generate and uses the RSA key inside the KeyStore
 * to encrypt the AES key that we store along the encrypted data inside a private {@link android.content.SharedPreferences}.
 *
 * This throws a {@link java.security.KeyStoreException} in case of an error or KeyStore being unvailable (unlocked).
 *
 * See also: io.realm.internal.android.crypto.class.CipherClient
 * @see <a href="https://developer.android.com/training/articles/keystore.html">Android KeyStore</a>
 */
public class SecureUserStore implements UserStore {
    private static final String REALM_OBJECT_SERVER_USERS = "realm_object_server_users";
    private final CipherClient cipherClient;
    private final SharedPreferences sp;
    private User cachedCurrentUser; // Keep a quick reference to the current user

    public SecureUserStore(final Context context) throws KeyStoreException {
        cipherClient = new CipherClient(context);
        sp = context.getSharedPreferences(REALM_OBJECT_SERVER_USERS, Context.MODE_PRIVATE);
    }

    /**
     * Store user as serialised and encrypted (Json), inside the private {@link android.content.SharedPreferences}.
     * @param key the {@link android.content.SharedPreferences} key.
     * @param user we want to save.
     * @return The previous user saved with this key or {@code null} if no user was replaced.
     */
    @Override
    public User put(String key, User user) {
        String previousUser = sp.getString(key, null);
        SharedPreferences.Editor editor = sp.edit();
        String userSerialisedAndEncrypted;
        try {
            userSerialisedAndEncrypted = cipherClient.encrypt(user.toJson());
        } catch (KeyStoreException e) {
            e.printStackTrace();
            return null;
        }
        editor.putString(key, userSerialisedAndEncrypted);
        // Optimistically save. If the user isn't saved due to a process crash it isn't dangerous.
        editor.apply();

        if (UserStore.CURRENT_USER_KEY.equals(key)) {
            cachedCurrentUser = user;
        }
        if (previousUser != null) {
            try {
                String userSerialisedAndDecrypted = cipherClient.decrypt(previousUser);
                return User.fromJson(userSerialisedAndDecrypted);
            } catch (KeyStoreException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Retrieves the {@link User} by decrypting first the serialised Json.
     * @param key the {@link android.content.SharedPreferences} key.
     * @return the {@link User} with the given key.
     */
    @Override
    public User get(String key) {
        if (key.equals(UserStore.CURRENT_USER_KEY) && cachedCurrentUser != null) {
            return cachedCurrentUser;
        }

        String userData = sp.getString(key, "");
        if (userData.equals("")) {
            return null;
        }

        try {
            String userSerialisedAndDecrypted = cipherClient.decrypt(userData);
            User user = User.fromJson(userSerialisedAndDecrypted);
            if (UserStore.CURRENT_USER_KEY.equals(key)) {
                cachedCurrentUser = user;
            }
            return user;
        } catch (KeyStoreException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public User remove(String key) {
        String currentUser = sp.getString(key, null);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, null);
        editor.apply();

        if (UserStore.CURRENT_USER_KEY.equals(key) && cachedCurrentUser != null) {
            cachedCurrentUser = null;
        }

        if (currentUser != null) {
            try {
                String userSerialisedAndDecrypted = cipherClient.decrypt(currentUser);
                return User.fromJson(userSerialisedAndDecrypted);
            } catch (KeyStoreException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public Collection<User> allUsers() {
        Map<String, ?> all = sp.getAll();
        ArrayList<User> users = new ArrayList<User>(all.size());
        for (Object userJson : all.values()) {
            String userSerialisedAndDecrypted = null;
            try {
                userSerialisedAndDecrypted = cipherClient.decrypt((String) userJson);
            } catch (KeyStoreException e) {
                e.printStackTrace();
                // returning null will probably penalise the other Users
            }
            users.add(User.fromJson(userSerialisedAndDecrypted));
        }
        return users;
    }
}
