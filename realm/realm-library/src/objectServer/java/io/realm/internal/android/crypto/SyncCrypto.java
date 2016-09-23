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

package io.realm.internal.android.crypto;

import java.security.KeyStoreException;

/**
 * Define methods that Android API should expose regardless of the API version.
 */
public interface SyncCrypto {
    String encrypt(String plainText) throws KeyStoreException;
    String decrypt(String cipherText) throws KeyStoreException;
    void create_key() throws KeyStoreException;

    // User is responsible of unlocking the keystore, we expose these methods as
    // a helper.
    boolean is_keystore_unlocked() throws KeyStoreException;
    void unlock_keystore() throws KeyStoreException;
}
