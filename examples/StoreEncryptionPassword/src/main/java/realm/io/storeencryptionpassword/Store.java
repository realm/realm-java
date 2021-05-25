package realm.io.storeencryptionpassword;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import io.realm.RealmConfiguration;

public class Store {
    private static final String KEYSTORE_PROVIDER_NAME = "AndroidKeyStore";
    private static final String KEY_ALIAS = "realm_key";
    private static final String TRANSFORMATION = KeyProperties.KEY_ALGORITHM_AES
            + "/" + KeyProperties.BLOCK_MODE_CBC
            + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7;
    private static final int AUTH_VALID_DURATION_IN_SECOND = 30;

    private static final ByteOrder ORDER_FOR_ENCRYPTED_DATA = ByteOrder.BIG_ENDIAN;

    private final Activity context;
    private final SecureRandom rng = new SecureRandom();
    private final KeyStore keyStore = prepareKeyStore();

    public Store(Activity context) {
        this.context = context;
    }

    public boolean containsEncryptionKey() {
        try {
            return keyStore.containsAlias(KEY_ALIAS);
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }

    public void unlockKeyStore(int requestCode) {
        final Intent intent = getKeyguardManager().createConfirmDeviceCredentialIntent("Android Keystore System",
                "unlock keystore to decrypt Realm database.");
        context.startActivityForResult(intent, requestCode);
    }

    public boolean onUnlockKeyStoreResult(int result, Intent data) {
        return result == Activity.RESULT_OK;
    }

    public byte[] generateKeyForRealm() {
        final byte[] keyForRealm = new byte[RealmConfiguration.KEY_LENGTH];
        rng.nextBytes(keyForRealm);
        return keyForRealm;
    }

    public void generateKeyInKeystore() {
        final KeyGenerator keyGenerator;
        try {
            keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    KEYSTORE_PROVIDER_NAME);
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException(e);
        }

        final KeyGenParameterSpec keySpec = new KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setUserAuthenticationRequired(true)
                .setUserAuthenticationValidityDurationSeconds(
                        AUTH_VALID_DURATION_IN_SECOND)
                .build();
        try {
            keyGenerator.init(keySpec);
        } catch (InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }
        keyGenerator.generateKey();

    }

    public byte[] encryptAndSaveKeyForRealm(byte[] keyForRealm) {
        final KeyStore ks = prepareKeyStore();
        final Cipher cipher = prepareCipher();

        final byte[] iv;
        final byte[] encryptedKeyForRealm;
        try {
            final SecretKey key = (SecretKey) ks.getKey(KEY_ALIAS, null);
            cipher.init(Cipher.ENCRYPT_MODE, key);

            encryptedKeyForRealm = cipher.doFinal(keyForRealm);
            iv = cipher.getIV();
        } catch (InvalidKeyException | UnrecoverableKeyException | NoSuchAlgorithmException
                | KeyStoreException | BadPaddingException | IllegalBlockSizeException e) {
            throw new RuntimeException("key for encryption is invalid", e);
        }
        final byte[] ivAndEncryptedKey = new byte[Integer.BYTES + iv.length + encryptedKeyForRealm.length];

        final ByteBuffer buffer = ByteBuffer.wrap(ivAndEncryptedKey);
        buffer.order(ORDER_FOR_ENCRYPTED_DATA);
        buffer.putInt(iv.length);
        buffer.put(iv);
        buffer.put(encryptedKeyForRealm);

        SharedPrefUtils.save(context, ivAndEncryptedKey);

        return ivAndEncryptedKey;
    }

    public byte[] decryptKeyForRealm(byte[] ivAndEncryptedKey) {
        final Cipher cipher = prepareCipher();
        final KeyStore keyStore = prepareKeyStore();

        final ByteBuffer buffer = ByteBuffer.wrap(ivAndEncryptedKey);
        buffer.order(ORDER_FOR_ENCRYPTED_DATA);

        final int ivLength = buffer.getInt();
        final byte[] iv = new byte[ivLength];
        final byte[] encryptedKey = new byte[ivAndEncryptedKey.length - Integer.BYTES - ivLength];

        buffer.get(iv);
        buffer.get(encryptedKey);

        try {
            final SecretKey key = (SecretKey) keyStore.getKey(KEY_ALIAS, null);
            final IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);

            return cipher.doFinal(encryptedKey);
        } catch (InvalidKeyException e) {
            throw new RuntimeException("key is invalid.");
        } catch (UnrecoverableKeyException | NoSuchAlgorithmException | BadPaddingException
                | KeyStoreException | IllegalBlockSizeException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] getEncryptedRealmKey() {
        return SharedPrefUtils.load(context);
    }

    private KeyStore prepareKeyStore() {
        try {
            KeyStore ks = KeyStore.getInstance(KEYSTORE_PROVIDER_NAME);
            ks.load(null);
            return ks;
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Cipher prepareCipher() {
        final Cipher cipher;
        try {
            cipher = Cipher.getInstance(TRANSFORMATION);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
        return cipher;
    }

    private KeyguardManager getKeyguardManager() {
        return (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
    }
}
