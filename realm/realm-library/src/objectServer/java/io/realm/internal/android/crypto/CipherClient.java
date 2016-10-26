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

import android.content.Context;

import java.security.KeyStoreException;

import io.realm.SyncUser;

/**
 * A Helper to use the crypto API, it allows encryption/decryption and has methods to help test if the KeyStore is locked and help unlocked it.
 * This hides the complexity of different Android API to achieve those operations.
 *
 * This support Android API 9 and forwards.
 * This cipher uses the KeyStore provided by Android, hence we to need to be sure that the KeyStore is available
 * before doing any {@link #encrypt(String)}/{@link #decrypt(String)} by calling {@link #isKeystoreUnlocked()} then
 * {@link #unlockKeystore()}, note that the latter will open the system {@link android.app.Activity} to set a passowrd/PIN/Pattern required
 * to unlock the sceen & the KeyStore.
 */
public class CipherClient {
    private SyncCrypto syncCrypto;

    public CipherClient(Context context) throws KeyStoreException {
        syncCrypto = SyncCryptoFactory.get(context);
    }

    /**
     * Takes some plain text {@link String} and return the encrypted version
     * of this {@link String} using the Android Key Store.
     *
     * @param user represents the Token of a {@link SyncUser}.
     * @return the encrypted Token.
     * @throws KeyStoreException in case the Key Store is locked or other error.
     */
    public String encrypt(String user) throws KeyStoreException {
        if (syncCrypto.is_keystore_unlocked()) {
            try {
                syncCrypto.create_key();
                String encrypted = syncCrypto.encrypt(user);
                return encrypted;
            } catch (KeyStoreException ex) {
                throw new KeyStoreException(ex);
            }
        } else {
            throw new KeyStoreException("Trying to use SecureUserStore without an unlocked KeyStore");
        }
    }

    /**
     * Takes a previously {@link #encrypt(String)} to decrypted it
     * using the Android Key Store.
     *
     * @param user_encrypted represents the encrypted Token of a {@link SyncUser}.
     * @return the decrypted Token.
     * @throws KeyStoreException in case the KeyStore is locked or other error.
     */
    public String decrypt(String user_encrypted) throws KeyStoreException {
        if (syncCrypto.is_keystore_unlocked()) {
            try {
                String decrypted = syncCrypto.decrypt(user_encrypted);
                return decrypted;
            } catch (KeyStoreException ex) {
                throw new KeyStoreException(ex);
            }
        } else {
            throw new KeyStoreException("Trying to use SecureUserStore without an unlocked KeyStore");
        }
    }


    /**
     * Checks whether the Android KeyStore is available.
     * This should be called before {@link #encrypt(String)} or {@link #decrypt(String)} as those need the KeyStore unlocked.
      * @return {@code true} if the Android KeyStore in unlocked.
     * @throws KeyStoreException in case of error.
     */
    public boolean isKeystoreUnlocked () throws KeyStoreException {
        return syncCrypto.is_keystore_unlocked();
    }

    /**
     * Helps unlock the KeyStore this will launch the appropriate {@link android.content.Intent}
     * to start the platform system {@link android.app.Activity} to create/unlock the KeyStore.
     *
     * @throws KeyStoreException in case of error.
     */
    public void unlockKeystore () throws KeyStoreException {
        syncCrypto.unlock_keystore();
    }
}
