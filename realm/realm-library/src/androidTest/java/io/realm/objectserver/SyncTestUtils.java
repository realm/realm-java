package io.realm.objectserver;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.UUID;

import io.realm.objectserver.internal.Token;

public class SyncTestUtils {

    public static User createTestUser() {
        return createTestUser(Long.MAX_VALUE);
    }

    public static User createTestUser(long expires) {
        JSONObject obj = new JSONObject();
        try {
            JSONObject token = new JSONObject();
            token.put("token", UUID.randomUUID().toString());
            JSONObject tokenData = new JSONObject();
            JSONArray perms = new JSONArray(); // Grant all permissions
            for (int i = 0; i < Token.Permission.values().length; i++) {
                perms.put(Token.Permission.values()[i].toString().toLowerCase(Locale.US));
            }
            tokenData.put("identity", UUID.randomUUID().toString());
            tokenData.put("path", null);
            tokenData.put("expires", expires);
            tokenData.put("access", perms);
            token.put("token_data", tokenData);
            obj.put("refreshToken", token);
            obj.put("authUrl", "http://dummy.org/auth");
            return User.fromJson(obj.toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
