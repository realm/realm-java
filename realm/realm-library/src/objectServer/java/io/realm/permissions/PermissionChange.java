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
package io.realm.permissions;

import java.util.Date;
import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * This class is used for requesting changes to a Realm's permissions.
 *
 * @see <a href="https://realm.io/docs/realm-object-server/#permissions">Controlling Permissions</a>
 */
public class PermissionChange extends RealmObject {

    // Base fields
    @PrimaryKey
    @Required
    private String id = UUID.randomUUID().toString();
    @Required
    private Date createdAt = new Date();
    @Required
    private Date updatedAt = new Date();
    private Integer statusCode = null; // null=not processed, 0=success, >0=error
    private String statusMessage;

    @Required
    private String realmUrl;
    @Required
    private String userId;
    private Boolean mayRead = false;
    private Boolean mayWrite = false;
    private Boolean mayManage = false;

    public PermissionChange() {
        // Default constructor required by Realm
    }

    /**
     * Construct a Permission Change Object.
     *
     * @param realmUrl Realm to change permissions for. Use {@code *} to change the permissions of all Realms.
     * @param userId User or users to effect. Use {@code *} to change the permissions for all users.
     * @param mayRead Define read access. {@code true} or {@code false} to request this new value. {@code null} to
     *                keep current value.
     * @param mayWrite Define write access. {@code true} or {@code false} to request this new value. {@code null} to
     *                 keep current value.
     * @param mayManage Define manage access. {@code true} or {@code false} to request this new value. {@code null} to
     *                  keep current value.
     *
     * @see <a href="https://realm.io/docs/realm-object-server/#permissions">Controlling Permissions</a>
     */
    public PermissionChange(String realmUrl, String userId, Boolean mayRead, Boolean mayWrite, Boolean mayManage) {
        this.realmUrl = realmUrl;
        this.userId = userId;
        this.mayRead = mayRead;
        this.mayWrite = mayWrite;
        this.mayManage = mayManage;
    }

    public String getId() {
        return id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Returns the status code for this change.
     *
     * @return {@code null} if not yet processed. {@code 0} if successfull, {@code >0} if an error happened. See {@link #getStatusMessage()}.
     */
    public Integer getStatusCode() {
        return statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public String getRealmUrl() {
        return realmUrl;
    }

    public String getUserId() {
        return userId;
    }

    public Boolean mayRead() {
        return mayRead;
    }

    public Boolean mayWrite() {
        return mayWrite;
    }

    public Boolean mayManage() {
        return mayManage;
    }
}
