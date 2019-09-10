/*
 * Copyright 2017 Realm Inc.
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

package io.realm.permissions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import javax.annotation.Nonnull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.realm.SyncUser;
import io.realm.internal.android.JsonUtils;


///**
// * This class represents a given set of permissions for one user on one Realm.
// * <p>
// * Permissions can be changed by users with administrative rights using the {@link PermissionManager}.
// *
// * @see SyncUser#getPermissionManager()
// */
public class Permission {

    private String userId;
    @Nonnull
    private String path;
    private AccessLevel accessLevel;
    private boolean mayRead;
    private boolean mayWrite;
    private boolean mayManage;
    @Nonnull
    private Date updatedAt;

    public Permission(String userId, String path, AccessLevel accessLevel, boolean mayRead, boolean mayWrite, boolean mayManage, Date updatedAt) {
        this.userId = userId;
        this.path = path;
        this.accessLevel = accessLevel;
        this.mayRead = mayRead;
        this.mayWrite = mayWrite;
        this.mayManage = mayManage;
        this.updatedAt = updatedAt;
    }

    /**
     * Converts a Json object from the Realm Object Server to a Java Permission object.
     * @throws JSONException if the JSON was malformed.
     */
    public static Permission fromJson(JSONObject permission) throws JSONException {
        /* Example:
         * {"permissions":[
         *  { "path":"/__wildcardpermissions",
         *    "accessLevel": "read",
         *    "realmOwnerId": null,
         *    "updatedAt": "2019-09-06T06:51:02.532Z",
         *    "updatedById":null,
         *    "userId":null}
         *    ]}
         */
        String userId = (permission.isNull("userId")) ? null : permission.getString("userId");
        String path = permission.getString("path");
        AccessLevel accessLevel;
        switch(permission.getString("accessLevel")) {
            case "read":
                accessLevel = AccessLevel.READ;
                break;
            case "write":
                accessLevel = AccessLevel.WRITE;
                break;
            case "admin":
                accessLevel = AccessLevel.ADMIN;
                break;
            default:
                throw new IllegalArgumentException("Unsupported access level: " + permission.getString("accessLevel"));
        }
        boolean mayRead = accessLevel.mayRead();
        boolean mayWrite = accessLevel.mayWrite();
        boolean mayManage = accessLevel.mayManage();
        Date updatedAt = JsonUtils.stringToDate(permission.getString("updatedAt"));
        return new Permission(userId, path, accessLevel, mayRead, mayWrite, mayManage, updatedAt);
    }

    /**
     * Returns the {@link SyncUser#getIdentity()} of the user effected by this permission.˚
     * <p>
     *
     * @return the user effected by this permission.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Returns the path to the Realm on the server effected by this permission. This is not the full URL.
     *
     * @return the path to the Realm this permission object refers to.
     */
    public String getPath() {
        return path;
    }

    /**
     * Checks whether or not the user defined by this permission is allowed to read the Realm defined by
     * {@link #getPath()}.
     *
     * @return {@code true} if this permission grant read permissions to the Realm, {@code false} if not.
     */
    public boolean mayRead() {
        return mayRead;
    }

    /**
     * Checks whether or not the user defined by this permission is allowed to write to the Realm defined by
     * {@link #getPath()}.
     *
     * @return {@code true} if this permission grant write permissions to the Realm, {@code false} if not.
     */
    public boolean mayWrite() {
        return mayWrite;
    }

    /**
     * Checks whether or not the user defined by this permission is allowed to manage access to the Realm defined
     * by {@link #getPath()}. Having this permission enable those users to add or remove permissions from
     * other users, including the one who granted it.
     *
     * @return {@code true} if this permission grant administrative rights to the Realm, {@code false} if not.
     */
    public boolean mayManage() {
        return mayManage;
    }

    /**
     * Returns the timestamp for when this permission object was last updated.
     *
     * @return the timestamp for when this permission was last updated.
     */
    @SuppressFBWarnings({"EI_EXPOSE_REP"})
    public Date getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return "Permission{" +
                "userId='" + userId + '\'' +
                ", path='" + path + '\'' +
                ", mayRead=" + mayRead +
                ", mayWrite=" + mayWrite +
                ", mayManage=" + mayManage +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
