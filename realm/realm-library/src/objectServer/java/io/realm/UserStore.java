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

/**
 * Interface for classes responsible for saving and retrieving Object Server users again.
 * <p>
 * Any implementation of a User Store is expected to not perform lengthy blocking operations as it might
 * be called on the Main Thread. All implementations of this interface should be thread safe.
 *
 * @see SyncManager#setUserStore(UserStore)
 * @see RealmFileUserStore
 */
public interface UserStore {

    /**
     * Saves a {@link SyncUser} object. If another user already exists, it will be replaced.
     *  {@link SyncUser#getIdentity()} is used as a unique identifier of a given {@link SyncUser}.
     *
     * @param user {@link SyncUser} object to store.
     */
    void put(SyncUser user);

    /**
     * Retrieves the current {@link SyncUser}.
     * <p>
     * This method will throw an exception if more than one valid, logged in users exist.
     * @return {@link SyncUser} object or {@code null} if not found.
     */
    SyncUser getCurrent();

    /**
     * Retrieves specified {@link SyncUser}.
     *
     * @param identity identity of the user.
     * @return {@link SyncUser} object or {@code null} if not found.
     */
    SyncUser get(String identity);

    /**
     * Removes the user from the store.
     * <p>
     * If the user is not found, this method does nothing.
     *
     * @param identity identity of the user.
     */
    void remove(String identity);

    /**
     * Returns a collection of all users saved in the User store.
     *
     * @return Collection of all users. If no users exist, an empty collection is returned.
     */
    Collection<SyncUser> allUsers();
}
