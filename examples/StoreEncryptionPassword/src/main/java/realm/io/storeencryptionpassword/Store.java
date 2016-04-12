package realm.io.storeencryptionpassword;

/**
 * Created by Nabil on 12/04/2016.
 */
public interface Store {
    boolean isKeystorePresent();
    void generateKeystore();
    void unlockKeyStore();

    byte[] generateAesKey();
    void encryptAndSaveAESKey(byte[] aes);
    byte[] decryptAesKey();

    byte[] getRealmKey();
}
