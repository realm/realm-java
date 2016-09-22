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

import java.util.Arrays;
import java.util.Locale;

/**
 * This class represents a value from the Realm Authentication Server.
 */
public class Token {

    private final String value;
    private final long expiresSec;
    private final Permission[] permissions;
    private final String identity;
    private final String path;

    public static Token from(JSONObject token) throws JSONException {
        String value = token.getString("token");
        JSONObject tokenData = token.getJSONObject("token_data");
        String identity = tokenData.getString("identity");
        String path = tokenData.optString("path");
        long expiresSec = tokenData.getLong("expires");
        Permission[] permissions;
        JSONArray access = tokenData.getJSONArray("access");
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

        return new Token(value, identity, path, expiresSec, permissions);
    }

    public Token(String value, String identity, String path, long expiresSec, Permission[] permissions) {
        this.value = value;
        this.identity = identity;
        this.path = path;
        this.expiresSec = expiresSec;
        if (permissions != null) {
            this.permissions = Arrays.copyOf(permissions, permissions.length);
        } else {
            this.permissions = new Permission[0];
        }
    }

    public String value() {
        return value;
    }

    public String identity() { return identity; }

    public String path() { return path; }

    /**
     * Returns when this token expires. Timestamp is in UTC seconds.
     */
    public long expiresSec() {
        return expiresSec;
    }

    /**
     * Returns when this token expires. Timestamp is in UTC milliseconds.
     */
    public long expiresMs() {
        long expiresMs = expiresSec * 1000;
        if (expiresMs < expiresSec) {
            return Long.MAX_VALUE; // Prevent overflow
        } else {
            return expiresMs;
        }
    }

    public Permission[] permissions() {
        return Arrays.copyOf(permissions, permissions.length);
    }

    public JSONObject toJson() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("token", value);
            JSONObject tokenData = new JSONObject();
            tokenData.put("identity", identity);
            tokenData.put("path", path);
            tokenData.put("expires", expiresSec);
            JSONArray perms = new JSONArray();
            for (int i = 0; i < permissions.length; i++) {
                perms.put(permissions[i].toString().toLowerCase(Locale.US));
            }
            tokenData.put("access", perms);
            obj.put("token_data", tokenData);
            return obj;
        } catch (JSONException e) {
            throw new RuntimeException("Could not convert Token to JSON.", e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Token token = (Token) o;

        if (expiresSec != token.expiresSec) return false;
        if (!value.equals(token.value)) return false;
        if (!Arrays.equals(permissions, token.permissions)) return false;
        if (!identity.equals(token.identity)) return false;
        return path != null ? path.equals(token.path) : token.path == null;
    }

    @Override
    public int hashCode() {
        int result = value.hashCode();
        result = 31 * result + (int) (expiresSec ^ (expiresSec >>> 32));
        result = 31 * result + Arrays.hashCode(permissions);
        result = 31 * result + identity.hashCode();
        result = 31 * result + (path != null ? path.hashCode() : 0);
        return result;
    }

    public enum Permission {
        UNKNOWN,
        UPLOAD,
        DOWNLOAD,
        REFRESH,
        MANAGE;
    }
}
