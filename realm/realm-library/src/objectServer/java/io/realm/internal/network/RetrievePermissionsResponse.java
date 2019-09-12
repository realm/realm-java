/*
 * Copyright 2019 Realm Inc.
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
package io.realm.internal.network;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.ErrorCode;
import io.realm.ObjectServerError;
import io.realm.internal.android.JsonUtils;
import io.realm.log.RealmLog;
import io.realm.permissions.AccessLevel;
import io.realm.permissions.Permission;
import okhttp3.Response;

/**
 * Class wrapping the response from `GET /permissions`
 */
public class RetrievePermissionsResponse extends AuthServerResponse {

    private final List<Permission> permissions = new ArrayList<>();

    /**
     * Helper method for creating the proper response. This method will set the appropriate error
     * depending on any HTTP response codes or I/O errors.
     *
     * @param response the server response.
     * @return the user lookup response.
     */
    static RetrievePermissionsResponse from(Response response) {
        String serverResponse;
        try {
            serverResponse = response.body().string();
        } catch (IOException e) {
            ObjectServerError error = new ObjectServerError(ErrorCode.IO_EXCEPTION, e);
            return new RetrievePermissionsResponse(error);
        }
        if (!response.isSuccessful()) {
            return new RetrievePermissionsResponse(AuthServerResponse.createError(serverResponse, response.code()));
        } else {
            return new RetrievePermissionsResponse(serverResponse);
        }
    }

    /**
     * Helper method for creating a failed response.
     */
    public static RetrievePermissionsResponse from(ObjectServerError objectServerError) {
        return new RetrievePermissionsResponse(objectServerError);
    }

    /**
     * Helper method for creating a failed response from an {@link Exception}.
     */
    public static RetrievePermissionsResponse from(Exception exception) {
        return RetrievePermissionsResponse.from(new ObjectServerError(ErrorCode.fromException(exception), exception));
    }

    private RetrievePermissionsResponse(ObjectServerError error) {
        RealmLog.debug("LookupUserIdResponse - Error: %s", error);
        setError(error);
        this.error = error;
    }

    private RetrievePermissionsResponse(String serverResponse) {
        RealmLog.debug("RetrievePermissionsResponse - Success: %s", serverResponse);
        try {
            JSONObject obj = new JSONObject(serverResponse);
            JSONArray array = obj.getJSONArray("permissions");
            for (int i = 0; i < array.length(); i++) {
                JSONObject permission = array.getJSONObject(i);
                String userId = (permission.isNull("userId")) ? null : permission.getString("userId");
                String path = permission.getString("path");
                AccessLevel accessLevel = AccessLevel.fromKey(permission.getString("accessLevel"));
                boolean mayRead = accessLevel.mayRead();
                boolean mayWrite = accessLevel.mayWrite();
                boolean mayManage = accessLevel.mayManage();
                Date updatedAt = JsonUtils.stringToDate(permission.getString("updatedAt"));
                permissions.add(new Permission(userId, path, accessLevel, mayRead, mayWrite, mayManage, updatedAt));
            }
        } catch (JSONException e) {
            error = new ObjectServerError(ErrorCode.JSON_EXCEPTION, e);
        }
    }

    public List<Permission> getPermissions() {
        return permissions;
    }
}
