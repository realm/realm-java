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

package io.realm.internal.android.crypto.api_legacy;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.KeyStoreException;
import java.security.SecureRandom;
import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


import io.realm.internal.android.crypto.CipherFactory;
import io.realm.internal.android.crypto.SyncCrypto;
import io.realm.internal.android.crypto.misc.Base64;
import io.realm.internal.android.crypto.misc.PRNGFixes;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Implements {@link SyncCrypto} methods for API 9 to 18 (pre Android KeyStore public API).
 */
public class SyncCryptoLegacy implements SyncCrypto {
    private Context context;
    private SecretKey key;
    private String alias = "Realm";
    private int mError = NO_ERROR;
    private SecureRandom random = new SecureRandom();

    private static final String UNLOCK_ACTION = "android.credentials.UNLOCK";

    // ResponseCodes
    private static final int NO_ERROR = 1;
    private static final int LOCKED = 2;
    private static final int UNINITIALIZED = 3;
    private static final int PROTOCOL_ERROR = 5;

    // States
    private enum State {
        UNLOCKED, LOCKED, UNINITIALIZED
    };

    private static final LocalSocketAddress sAddress = new LocalSocketAddress(
            "keystore", LocalSocketAddress.Namespace.RESERVED);
    private final static int KEY_LENGTH = 256;
    private final static String DELIMITER = "]";

    public SyncCryptoLegacy (Context context) throws KeyStoreException {
        PRNGFixes.apply();
        this.context = context;
    }

    @Override
    public String encrypt(String plainText) throws KeyStoreException {
        try {
            Cipher cipher = CipherFactory.get();

            byte[] iv = generateIv(cipher.getBlockSize());
            IvParameterSpec ivParams = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, ivParams);
            byte[] cipherText = cipher.doFinal(plainText.getBytes("UTF-8"));

            return String.format("%s%s%s", Base64.to(iv), DELIMITER,
                    Base64.to(cipherText));
        } catch (GeneralSecurityException e) {
            throw new KeyStoreException(e);
        } catch (UnsupportedEncodingException e) {
            throw new KeyStoreException(e);
        }
    }

    @Override
    public String decrypt(String cipherText) throws KeyStoreException {
        byte[] keyBytes = get(alias);
        if (keyBytes == null) {
            return null;
        }
        SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");

        try {
            String[] fields = cipherText.split(DELIMITER);
            if (fields.length != 2) {
                throw new IllegalArgumentException("Invalid encrypted text format");
            }

            byte[] iv = Base64.from(fields[0]);
            byte[] cipherBytes = Base64.from(fields[1]);
            Cipher cipher = CipherFactory.get();
            IvParameterSpec ivParams = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, key, ivParams);
            byte[] plaintext = cipher.doFinal(cipherBytes);
            return new String(plaintext, "UTF-8");
        } catch (GeneralSecurityException e) {
            throw new KeyStoreException(e);
        } catch (UnsupportedEncodingException e) {
            throw new KeyStoreException(e);
        }
    }

    @Override
    public boolean is_keystore_unlocked() throws KeyStoreException {
        return state() == State.UNLOCKED;
    }

    @Override
    public void unlock_keystore() throws KeyStoreException {
        try {
            Intent intent = new Intent(UNLOCK_ACTION);
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            throw new KeyStoreException(e);
        }
    }

    @Override
    public void create_key() throws KeyStoreException {
        try {
            KeyGenerator kg = KeyGenerator.getInstance("AES");
            kg.init(KEY_LENGTH);
            key = kg.generateKey();

            boolean success = put(getBytes(alias), key.getEncoded());
            if (!success) {
                throw new KeyStoreException("Keystore error");
            }
        } catch (Exception e) {
            throw new KeyStoreException(e);
        }
    }

    private State state() throws KeyStoreException {
        execute('t');
        switch (mError) {
            case NO_ERROR:
                return State.UNLOCKED;
            case LOCKED:
                return State.LOCKED;
            case UNINITIALIZED:
                return State.UNINITIALIZED;
            default:
                throw new KeyStoreException("" + mError);
        }
    }

    private byte[] get(byte[] key) {
        ArrayList<byte[]> values = execute('g', key);
        return (values == null || values.isEmpty()) ? null : values.get(0);
    }

    private byte[] get(String key) {
        return get(getBytes(key));
    }

    private boolean put(byte[] key, byte[] value) {
        execute('i', key, value);
        return mError == NO_ERROR;
    }

    private ArrayList<byte[]> execute(int code, byte[]... parameters) {
        mError = PROTOCOL_ERROR;

        for (byte[] parameter : parameters) {
            if (parameter == null || parameter.length > 65535) {
                return null;
            }
        }

        LocalSocket socket = new LocalSocket();
        try {
            socket.connect(sAddress);

            OutputStream out = socket.getOutputStream();
            out.write(code);
            for (byte[] parameter : parameters) {
                out.write(parameter.length >> 8);
                out.write(parameter.length);
                out.write(parameter);
            }
            out.flush();
            socket.shutdownOutput();

            InputStream in = socket.getInputStream();
            if ((code = in.read()) != NO_ERROR) {
                if (code != -1) {
                    mError = code;
                }
                return null;
            }

            ArrayList<byte[]> values = new ArrayList<byte[]>();
            while (true) {
                int i, j;
                if ((i = in.read()) == -1) {
                    break;
                }
                if ((j = in.read()) == -1) {
                    return null;
                }
                byte[] value = new byte[i << 8 | j];
                for (i = 0; i < value.length; i += j) {
                    if ((j = in.read(value, i, value.length - i)) == -1) {
                        return null;
                    }
                }
                values.add(value);
            }
            mError = NO_ERROR;
            return values;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
        return null;
    }

    private static byte[] getBytes(String string) {
        try {
            return string.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] generateIv(int length) {
        byte[] b = new byte[length];
        random.nextBytes(b);
        return b;
    }
}
