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
package io.realm.internal.permissions;

import java.util.Date;
import java.util.UUID;

import javax.annotation.Nullable;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;
import io.realm.annotations.Required;
import io.realm.permissions.AccessLevel;
import io.realm.permissions.UserCondition;
import io.realm.permissions.PermissionRequest;


/**
 * This class is used for requesting changes to a Realm's permissions.
 *
 * @see <a href="https://realm.io/docs/realm-object-server/#permissions">Controlling Permissions</a>
 */
@RealmClass
public class PermissionChange implements BasePermissionApi {

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

    private String metadataKey;
    private String metadataValue;
    private String metadataNameSpace;
    private Boolean mayRead = false;
    private Boolean mayWrite = false;
    private Boolean mayManage = false;

    /**
     * Maps between a PermissionRequest and a PermissionChange object.
     *
     * @param request request to map to a PermissionChange.
     */
    public static PermissionChange fromRequest(PermissionRequest request) {
        // PRE-CONDITION: All input are verified to be valid from the perspective of the Client.
        UserCondition condition = request.getCondition();
        AccessLevel level = request.getAccessLevel();
        String realmUrl = request.getUrl();

        String userId = "";
        String metadataKey = null;
        String metadataValue = null;
        switch (condition.getType()) {
            case USER_ID:
                userId = condition.getValue();
                break;
            case METADATA:
                metadataKey = condition.getKey();
                metadataValue = condition.getValue();
                break;
        }

        return new PermissionChange(realmUrl, userId, metadataKey, metadataValue, level.mayRead(), level.mayWrite(),
                level.mayManage());
    }

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
    public PermissionChange(String realmUrl, String userId,
            @Nullable Boolean mayRead, @Nullable Boolean mayWrite, @Nullable Boolean mayManage) {
        this.realmUrl = realmUrl;
        this.userId = userId;
        this.mayRead = mayRead;
        this.mayWrite = mayWrite;
        this.mayManage = mayManage;
    }

    public PermissionChange(String realmUrl, String userId, String metadataKey, String metadataValue, Boolean mayRead,
            Boolean mayWrite, Boolean mayManage) {
        this.realmUrl = realmUrl;
        this.userId = userId;
        this.metadataKey = metadataKey;
        this.metadataValue = metadataValue;
        this.mayRead = mayRead;
        this.mayWrite = mayWrite;
        this.mayManage = mayManage;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    @SuppressFBWarnings("EI_EXPOSE_REP")
    public Date getCreatedAt() {
        return createdAt;
    }

    @Override
    @SuppressFBWarnings("EI_EXPOSE_REP")
    public Date getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Returns the status code for this change.
     *
     * @return {@code null} if not yet processed. {@code 0} if successful, {@code >0} if an error happened. See {@link #getStatusMessage()}.
     */
    @Override
    @Nullable
    public Integer getStatusCode() {
        return statusCode;
    }

    @Override
    @Nullable
    public String getStatusMessage() {
        return statusMessage;
    }

    public String getRealmUrl() {
        return realmUrl;
    }

    public String getUserId() {
        return userId;
    }

    @Nullable
    public Boolean mayRead() {
        return mayRead;
    }

    @Nullable
    public Boolean mayWrite() {
        return mayWrite;
    }

    @Nullable
    public Boolean mayManage() {
        return mayManage;
    }

    public String getMetadataKey() {
        return metadataKey;
    }

    public String getMetadataValue() {
        return metadataValue;
    }
}
