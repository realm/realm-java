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

import java.util.Collection;

import io.realm.android.SharedPrefsUserStore;
import io.realm.annotations.Beta;

/**
 * @Beta
 * Interface for classes responsible for saving and retrieving Object Server users again.
 * <p>
 * Any implementation of a User Store is expected to not perform lengthy blocking operations as it might
 * be called on the Main Thread. All implementations of this interface should be thread safe.
 *
 * @see SyncManager#setUserStore(UserStore)
 * @see SharedPrefsUserStore
 */
@Beta
public interface UserStore {

    String CURRENT_USER_KEY = "realm$currentUser";

    /**
     * Saves a {@link SyncUser} object under the given key. If another user already exists, it will be replaced.
     *
     * @param key key used to store the User.
     * @param user {@link SyncUser} object to store.
     * @return The previous user saved with this key or {@code null} if no user was replaced.
     *
     */
    SyncUser put(String key, SyncUser user);

    /**
     * Retrieves the {@link SyncUser} with the given key.
     *
     * @param key {@link SyncUser} saved under the given key or {@code null} if no user exists for that key.
     */
    SyncUser get(String key);

    /**
     * Removes the user with the given key from the store.
     *
     * @param key key for the user to remove.
     * @return {@link SyncUser} that was removed or {@code null} if no user matched the key.
     */
    SyncUser remove(String key);

    /**
     * Returns a collection of all users saved in the User store.
     *
     * @return Collection of all users. If no users exist, an empty collection is returned.
     */
    Collection<SyncUser> allUsers();


    /**
     * Removes all saved users.
     */
    void clear();
}
