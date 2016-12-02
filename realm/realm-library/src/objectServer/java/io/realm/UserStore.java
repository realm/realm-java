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

import io.realm.annotations.Beta;

/**
 * @Beta
 * Interface for classes responsible for saving and retrieving Object Server users again.
 * <p>
 * Any implementation of a User Store is expected to not perform lengthy blocking operations as it might
 * be called on the Main Thread. All implementations of this interface should be thread safe.
 *
 * @see SyncManager#setUserStore(UserStore)
 * @see RealmFileUserStore
 */
@Beta
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
     *
     * For now, current User cannot be called if more that one valid, logged in user
     * exists, it will throw an exception.
     */
    //TODO when ObjectStore integration of SyncManager is completed & multiple
    //     users are allowed, consider passing the User identity to lookup apply
    //     the operation to a particular user.
    SyncUser get();

    /**
     * Removes the current user from the store.
     */
    //TODO when ObjectStore integration of SyncManager is completed & multiple
    //     users are allowed, consider passing the User identity to lookup apply
    //     the operation to a particular user.
    void remove();

    /**
     * Returns a collection of all users saved in the User store.
     *
     * @return Collection of all users. If no users exist, an empty collection is returned.
     */
    Collection<SyncUser> allUsers();
}
