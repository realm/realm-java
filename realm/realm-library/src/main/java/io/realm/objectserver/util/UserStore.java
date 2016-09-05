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

package io.realm.objectserver.util;

import io.realm.objectserver.User;

/**
 * Interface for describing how a given user object can be persisted and retrieved again.
 *
 * @see SharedPrefsUserStore
 */
public interface UserStore {

    /**
     * Returns the user that have been set by calling `{@link #setCurrentUser(User)}.
     *
     * This user is not persisted across app restarts.
     *
     * @return the current {@link User} object or {@code null} if no user has been set as the current user.
     */
    User getCurrentUser();

    /**
     * Helper method for easily saving a user. Can be retrieved back using {@link #getCurrentUser()}.
     * This user will not be persisted if the process dies.
     *
     * @param user {@link User} to set as the current user.
     */
    void setCurrentUser(User user);

    /**
     * Saves a User object under the given key. If another user already exists, it will be replaced.
     *
     * @param key Key used to store the User. The same key is used to retrieve it again
     * @param user User object to store.
     */
    boolean save(String key, User user);

    /**
     * Saves a User object under the given key. If another user already exists, it will be replaced.
     *
     * @param key
     * @param user
     */
    void saveAsync(String key, User user);

    /**
     * TODO
     * @param key
     * @param user
     */
    void saveASync(String key, User user, Callback callback);

    /**
     * TODO
     * @param key
     */
    User load(String key);

    /**
     * TODO
     * @param key
     */
    void loadAsync(String key, Callback callback);



    interface Callback {
        /**
         * User was successfully saved.
         */
         void onSuccess(User user);

        /**
         * The user could not be saved.
         */
        void onError();
    }
}
