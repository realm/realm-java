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

package io.realm.internal.android.crypto.ciper;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.NoSuchPaddingException;

/**
 * Return a {@link javax.crypto.Cipher} that works for the API 18.
 */
public class CipherJB {
    public static javax.crypto.Cipher get() throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException {
        return javax.crypto.Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidOpenSSL");
    }
}
