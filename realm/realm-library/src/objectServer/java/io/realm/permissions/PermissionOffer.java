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

import java.util.Date;
import java.util.UUID;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * This model is used for offering permission changes to other users.
 * It should be used in conjunction with an {@link io.realm.SyncUser}'s management Realm.
 *
 * @see <a href="https://realm.io/docs/realm-object-server/#permissions">Permissions description</a> for general
 * documentation.
 */
public class PermissionOffer extends RealmObject {

    // Base fields
    @PrimaryKey
    @Required
    private String id = UUID.randomUUID().toString();
    @Required
    private Date createdAt = new Date();
    @Required
    private Date updatedAt = new Date();
    private Integer statusCode; // nil=not processed, 0=success, >0=error
    private String statusMessage;

    // Offer fields
    @Index
    private String token;
    @Required
    private String realmUrl;
    private boolean mayRead;
    private boolean mayWrite;
    private boolean mayManage;
    private Date expiresAt;

    public PermissionOffer() {
        // No args constructor required by Realm
    }

    /**
     * Construct a permission offer object used to offer permission changes to other users.
     *
     * @param url The URL to the Realm on which to apply these permission changes to, once the offer is accepted.
     * @param mayRead Grant or revoke read access.
     * @param mayWrite Grant or revoked read-write access.
     * @param mayManage Grant or revoke administrative access.
     * @param expiresAt When this token will expire and become invalid. Pass {@code null} if this offer should not expire.
     */
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public PermissionOffer(String url, boolean mayRead, boolean mayWrite, boolean mayManage, Date expiresAt) {
        if (url == null) {
            throw new IllegalArgumentException("Non-null 'url' required.");
        }
        this.realmUrl = url;
        this.mayRead = mayRead;
        this.mayWrite= mayWrite;
        this.mayManage = mayManage;
        this.expiresAt = expiresAt;
    }

    public String getId() {
        return id;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public Date getCreatedAt() {
        return createdAt;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public Date getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Returns the status code for this change.
     *
     * @return {@code null} if not yet processed. {@code 0} if successful, {@code >0} if an error happened. See {@link #getStatusMessage()}.
     */
    public Integer getStatusCode() {
        return statusCode;
    }

    /**
     * Check if the request was successfully handled by the Realm Object Server.
     *
     * @return {@code true} if request was handled successfully. {@code false} if not. See {@link #getStatusMessage()}
     *         for the full error message.
     */
    public boolean isSuccessful() {
        return statusCode != null && statusCode == 0;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public String getToken() {
        return token;
    }

    public String getRealmUrl() {
        return realmUrl;
    }

    public boolean isMayRead() {
        return mayRead;
    }

    public boolean isMayWrite() {
        return mayWrite;
    }

    public boolean isMayManage() {
        return mayManage;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public Date getExpiresAt() {
        return expiresAt;
    }
}
