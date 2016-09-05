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

package io.realm.internal.objectserver;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * This class represents a value from the Realm Authentication Server.
 */
public class Token {

    private final String value;
    private final long expiresMs;
    private final Permission[] permissions;

    public static Token from(JSONObject token) throws JSONException {
        String value = token.getString("token");
        long expiresSec = token.getLong("expires");
        Permission[] permissions;
        JSONArray access = token.getJSONArray("access");
        if (access != null) {
            permissions = new Permission[access.length()];
            for (int i = 0; i < access.length(); i++) {
                try {
                    permissions[i] = Permission.valueOf(access.getString(i));
                } catch (IllegalArgumentException e) {
                    permissions[i] = Permission.UNKNOWN;
                }
            }
        } else {
            permissions = new Permission[0];
        }

        return new Token(value, expiresSec, permissions);
    }

    public Token(String value, long expiresSec, Permission... permissions) {
        this.value = value;
        this.expiresMs = TimeUnit.MILLISECONDS.convert(expiresSec, TimeUnit.SECONDS);
        this.permissions = permissions;
    }

    public String value() {
        return value;
    }

    /**
     * Return the timestamp for when this Token expires. Timestamp is milliseconds UTC.
     */
    public long expires() {
        return expiresMs;
    }

    public Permission[] permissions() {
        return permissions;
    }

    public String toJson() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("token", value);
            obj.put("expiresMs", expiresMs);
            JSONArray perms = new JSONArray();
            for (int i = 0; i < permissions.length; i++) {
                perms.put(permissions[i].toString().toLowerCase(Locale.US));
            }
            obj.put("access", perms);
            return obj.toString();
        } catch (JSONException e) {
            throw new RuntimeException("Could not convert Token to JSON.", e);
        }
    }

    public enum Permission {
        UNKNOWN,
        UPLOAD,
        DOWNLOAD,
        REFRESH,
        MANAGE;
    }
}
