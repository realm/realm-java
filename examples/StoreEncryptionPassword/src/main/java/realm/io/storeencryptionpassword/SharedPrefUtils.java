package realm.io.storeencryptionpassword;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

public final class SharedPrefUtils {
    private static final String PREF_NAME = "realm_key";
    private static final String KEY = "iv_and_encrypted_key";

    public static void save(Context context, byte[] ivAndEncryptedKey) {
        getPreference(context).edit()
                .putString(KEY, encode(ivAndEncryptedKey))
                .apply();
    }

    public static byte[] load(Context context) {
        final SharedPreferences pref = getPreference(context);

        final String ivAndEncryptedKey = pref.getString(KEY, null);
        if (ivAndEncryptedKey == null) {
            return null;
        }

        return decode(ivAndEncryptedKey);
    }

    private static String encode(byte[] data) {
        if (data == null) {
            return null;
        }
        return Base64.encodeToString(data, Base64.NO_WRAP);
    }

    private static byte[] decode(String encodedData) {
        if (encodedData == null) {
            return null;
        }
        return Base64.decode(encodedData, Base64.DEFAULT);
    }

    private static SharedPreferences getPreference(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
}
