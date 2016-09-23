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

package io.realm.internal.android.crypto.api_18;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.security.KeyPairGeneratorSpec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPairGenerator;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Calendar;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.x500.X500Principal;


import io.realm.internal.android.crypto.CipherFactory;
import io.realm.internal.android.crypto.SyncCrypto;
import io.realm.internal.android.crypto.misc.Base64;
import io.realm.internal.android.crypto.misc.PRNGFixes;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Implements {@link SyncCrypto} methods for API 18 (after the Android KeyStore public API).
 */
public class SyncCryptoApi18Impl implements SyncCrypto {
    private java.security.KeyStore keyStore;
    private Context context;
    private String alias = "Realm";
    private static String X500Principal = "CN=Sync, O=Realm";
    private final static String DELIMITER = "]";

    public static final String UNLOCK_ACTION = "com.android.credentials.UNLOCK";

    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";

    public SyncCryptoApi18Impl (Context context) throws KeyStoreException {
        PRNGFixes.apply();
        this.context = context;
        try {
            keyStore = java.security.KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null);
        } catch (KeyStoreException e) {
            e.printStackTrace();
            throw new KeyStoreException(e);
        } catch (CertificateException e) {
            e.printStackTrace();
            throw new KeyStoreException(e);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new KeyStoreException(e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new KeyStoreException(e);
        }
    }

    @Override
    public String encrypt(String plainText) throws KeyStoreException {
        try {
            SecretKey key = generateAESKey();
            byte[] encrypted = encryptedUsingAESKey(key, plainText);
            byte[] encryptedKey = encryptAESKeyUsingRSA(key);
            // append with AES enc with RSA
            return String.format("%s%s%s", Base64.to(encryptedKey), DELIMITER,
                    Base64.to(encrypted));
        } catch (Exception e) {
            throw new KeyStoreException(e);
        }
    }

    @Override
    public String decrypt(String cipherText) throws KeyStoreException {
        try {
            String[] fields = cipherText.split(DELIMITER);
            if (fields.length != 2) {
                throw new IllegalArgumentException("Invalid encrypted text format");
            }

            byte[] aesEncWithRSA = Base64.from(fields[0]);
            byte[] encToken = Base64.from(fields[1]);

            // decrypt AES using RSA
            SecretKey key = decrypytAESKeyUsingRSA(aesEncWithRSA);

            // decrypt Token using decrypted AES
            return decryptedUsingAESKey(key, encToken);
        } catch (Exception e) {
            throw new KeyStoreException(e);
        }
    }

    @Override
    public boolean is_keystore_unlocked() throws KeyStoreException {
        try {
            Class<?> keyStoreClass = Class.forName("android.security.KeyStore");
            Method getInstanceMethod = keyStoreClass.getMethod("getInstance");
            Object invoke = getInstanceMethod.invoke(null);

            Method isUnlockedMethod = keyStoreClass.getMethod("isUnlocked");
            boolean isUnlocked = (boolean)isUnlockedMethod.invoke(invoke);
            return isUnlocked;
        } catch (ClassNotFoundException e) {
            throw new KeyStoreException(e);
        } catch (NoSuchMethodException e) {
            throw new KeyStoreException(e);
        } catch (IllegalAccessException e) {
            throw new KeyStoreException(e);
        } catch (InvocationTargetException e) {
            throw new KeyStoreException(e);
        }
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

    @TargetApi(18)
    public void create_key() throws KeyStoreException {
        try {
            // Create new key.
            // Avoid a known bug in Api 23 where we need names in KeyStore to be unique
            // http://stackoverflow.com/questions/23977407/android-4-3-keystore-chain-null-while-trying-to-retrieve-keys
            if (keyStore.containsAlias(alias)) {
                keyStore.deleteEntry(alias);
            }
            Calendar start = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            end.add(Calendar.YEAR, 1);
            KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
                    .setAlias(alias)
                    .setSubject(new X500Principal(X500Principal))
                    .setSerialNumber(BigInteger.ONE)
                    .setStartDate(start.getTime())
                    .setEndDate(end.getTime())
                    .build();
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA",
                    "AndroidKeyStore");
            generator.initialize(spec);
            generator.generateKeyPair();
        } catch (Exception e) {
            throw new KeyStoreException(e);
        }
    }

    private SecretKey generateAESKey() throws NoSuchAlgorithmException {
        // Generate a 256-bit key
        final int outputKeyLength = 256;

        SecureRandom secureRandom = new SecureRandom();
        // Do *not* seed secureRandom! Automatically seeded from system entropy.
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(outputKeyLength, secureRandom);
        SecretKey key = keyGenerator.generateKey();
        return key;
    }

    private byte[] encryptedUsingAESKey(SecretKey key, String plainText) throws KeyStoreException {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(plainText.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new KeyStoreException(e);
        } catch (NoSuchPaddingException e) {
            throw new KeyStoreException(e);
        } catch (BadPaddingException e) {
            throw new KeyStoreException(e);
        } catch (UnsupportedEncodingException e) {
            throw new KeyStoreException(e);
        } catch (IllegalBlockSizeException e) {
            throw new KeyStoreException(e);
        } catch (InvalidKeyException e) {
            throw new KeyStoreException(e);
        }
    }

    private String decryptedUsingAESKey(SecretKey key, byte[] cipherText) throws KeyStoreException {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] encrypted = cipher.doFinal(cipherText);
            return new String(encrypted, "UTF-8");
        } catch (NoSuchAlgorithmException e) {
            throw new KeyStoreException(e);
        } catch (NoSuchPaddingException e) {
            throw new KeyStoreException(e);
        } catch (BadPaddingException e) {
            throw new KeyStoreException(e);
        } catch (UnsupportedEncodingException e) {
            throw new KeyStoreException(e);
        } catch (IllegalBlockSizeException e) {
            throw new KeyStoreException(e);
        } catch (InvalidKeyException e) {
            throw new KeyStoreException(e);
        }
    }

    private byte[] encryptAESKeyUsingRSA(SecretKey key) throws KeyStoreException {
        try {
            java.security.KeyStore.PrivateKeyEntry privateKeyEntry = (java.security.KeyStore.PrivateKeyEntry) keyStore.getEntry(alias, null);
            RSAPublicKey publicKey = (RSAPublicKey) privateKeyEntry.getCertificate().getPublicKey();

            Cipher cipher = CipherFactory.get();

            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, cipher);
            cipherOutputStream.write(key.getEncoded());
            cipherOutputStream.close();

            return outputStream.toByteArray();
        } catch (NoSuchPaddingException e) {
            throw new KeyStoreException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new KeyStoreException(e);
        } catch (NoSuchProviderException e) {
            throw new KeyStoreException(e);
        } catch (InvalidKeyException e) {
            throw new KeyStoreException(e);
        } catch (KeyStoreException e) {
            throw new KeyStoreException(e);
        } catch (UnrecoverableEntryException e) {
            throw new KeyStoreException(e);
        } catch (IOException e) {
            throw new KeyStoreException(e);
        }
    }

    private SecretKeySpec decrypytAESKeyUsingRSA(byte[] aesEncKey) throws KeyStoreException {
        try {
            java.security.KeyStore.PrivateKeyEntry privateKeyEntry = (java.security.KeyStore.PrivateKeyEntry) keyStore.getEntry(alias, null);
            Cipher cipher = CipherFactory.get();
            cipher.init(Cipher.DECRYPT_MODE, privateKeyEntry.getPrivateKey());
            CipherInputStream cipherInputStream = new CipherInputStream(new ByteArrayInputStream(aesEncKey), cipher);

            ArrayList<Byte> values = new ArrayList<>();
            int nextByte;
            while ((nextByte = cipherInputStream.read()) != -1) {
                values.add((byte)nextByte);
            }

            final byte[] bytes = new byte[values.size()];
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = values.get(i).byteValue();
            }

            SecretKeySpec originalKey = new SecretKeySpec(bytes, "AES");
            return originalKey;
        } catch (NoSuchPaddingException e) {
            throw new KeyStoreException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new KeyStoreException(e);
        } catch (NoSuchProviderException e) {
            throw new KeyStoreException(e);
        } catch (UnsupportedEncodingException e) {
            throw new KeyStoreException(e);
        } catch (IOException e) {
            throw new KeyStoreException(e);
        } catch (InvalidKeyException e) {
            throw new KeyStoreException(e);
        } catch (UnrecoverableEntryException e) {
            throw new KeyStoreException(e);
        } catch (KeyStoreException e) {
            throw new KeyStoreException(e);
        }
    }
}
