/*
 * Copyright 2014 Realm Inc.
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

package io.realm.examples.encryptionexample;

import android.app.Activity;
import android.os.Bundle;
import android.security.KeyPairGeneratorSpec;
import android.util.Log;

import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.util.Calendar;
import java.util.GregorianCalendar;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.x500.X500Principal;

import io.realm.Realm;

public class EncryptionExampleActivity extends Activity {

    public static final String TAG = EncryptionExampleActivity.class.getName();

    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Open the Realm with encryption enabled
        // Throws UnsupportedOperator if not using a copy of Realm with encryption enabled
        try {
            realm = Realm.getInstance(this, getKey());
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Everything continues to work as normal except for that the file is encrypted on disk
        realm.beginTransaction();
        Person person = realm.createObject(Person.class);
        person.setName("Happy Person");
        person.setAge(14);
        realm.commitTransaction();

        person = realm.where(Person.class).findFirst();
        Log.i(TAG, String.format("Person name: %s", person.getName()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close(); // Remember to close Realm when done.
    }


    // Get the application's 256-bit AES key
    private byte[] getKey() throws GeneralSecurityException, IOException, java.io.IOException {
        // As of 4.3, Android has a secure per-application key store, but it can't store symmetric keys
        // As a result, we use it to store a public/private keypair which is used to encrypt the
        // symmetric key which is stored in a file in the application context

        byte[] keyData;
        try {
            File file = new File(getFilesDir(), Realm.DEFAULT_REALM_NAME + ".key");
            keyData = new byte[256];
            FileInputStream stream = new FileInputStream(file);
            try {
                int read = stream.read(keyData);
                if (read != keyData.length) {
                    keyData = null;
                }
            }
            finally {
                stream.close();
            }
        } catch (java.io.IOException e) {
            // Generate a new key if reading the existing one failed for any reason
            keyData = null;
        }

        KeyPair keyPair = getKeyPair();
        final Cipher cipher = Cipher.getInstance("RSA/NONE/PKCS1Padding");

        // We have an existing secret key, so decrypt and return it
        if (keyData != null) {
            cipher.init(Cipher.UNWRAP_MODE, keyPair.getPrivate());
            return cipher.unwrap(keyData, "AES", Cipher.SECRET_KEY).getEncoded();
        }

        // We need to generate a new secret key
        keyData = new byte[32];
        new SecureRandom().nextBytes(keyData);

        cipher.init(Cipher.WRAP_MODE, keyPair.getPublic());

        // Save the secret key to the file
        File file = new File(getFilesDir(), Realm.DEFAULT_REALM_NAME + ".key");
        FileOutputStream stream = new FileOutputStream(file);
        try {
            stream.write(cipher.wrap(new SecretKeySpec(keyData, "AES")));
        } finally {
            stream.close();
        }

        // Delete any existing default Realm since we won't be able to open it with the new key
        Realm.deleteRealmFile(this);

        return keyData;
    }

    // Get the keypair from the keystore if it exists, or create a new one if not
    private KeyPair getKeyPair() throws GeneralSecurityException, IOException, java.io.IOException {
        final KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);

        if (!keyStore.containsAlias(TAG)) {
            generateKeyPair(TAG);
        }

        final KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry)keyStore.getEntry(TAG, null);
        return new KeyPair(entry.getCertificate().getPublicKey(), entry.getPrivateKey());
    }

    // Generate a new long-lived keypair
    private void generateKeyPair(String alias) throws GeneralSecurityException {
        final Calendar start = new GregorianCalendar();
        final Calendar end = new GregorianCalendar();
        end.add(Calendar.YEAR, 100);
        final KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(this)
            .setAlias(alias)
            .setSubject(new X500Principal("CN=" + alias))
            .setSerialNumber(BigInteger.ONE)
            .setStartDate(start.getTime())
            .setEndDate(end.getTime())
            .build();

        final KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
        gen.initialize(spec);
        gen.generateKeyPair();
    }
}
