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

import android.os.Build;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import io.realm.internal.android.crypto.ciper.CipherJB;
import io.realm.internal.android.crypto.ciper.CipherLegacy;
import io.realm.internal.android.crypto.ciper.CipherMM;


/**
 * Return an appropriate {@link Cipher} given the version of Android.
 * Ex: on API 23 OpenSSL is replaced by BoringSSL.
 */
public class CipherFactory {

    private static final boolean IS_JB43 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
    private static final boolean IS_MM = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    private static final boolean IS_GINGERBREAD = Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;

    public static Cipher get() throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException {
        if (IS_MM) {
            return CipherMM.get();
        } else if (IS_JB43) {
            return CipherJB.get();
        } else if (IS_GINGERBREAD) {
             return CipherLegacy.get();
        } else  {
            throw new IllegalArgumentException("Not supported yet");
        }
    }
}
