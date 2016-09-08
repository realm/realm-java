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

package io.realm.objectserver;

import io.realm.objectserver.android.SharedPrefsUserStore;

/**
 * Interface for describing how a given user object can be persisted and retrieved again.
 *
 * @see SharedPrefsUserStore
 */
public interface UserStore {

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


    /**
     * Interface responsible for handling the result of asynchronously saving or loading the user.
     */
    interface Callback {
        /**
         * User was successfully saved or loaded.
         */
         void onSuccess(User user);

        /**
         * The user could not be saved or loaded.
         */
        void onError(Throwable t);
    }
}
