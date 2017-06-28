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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.UUID;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.realm.PermissionManager;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;
import io.realm.annotations.Required;
import io.realm.internal.Util;
import io.realm.internal.permissions.BasePermissionApi;


/**
 * This class represent a permission offer for a Realm that can be given to other users.
 * Once a offer is successfully created, it will be represented by an {@code offerToken} that can be sent
 * to other users. Once they accept this token using, the permissions covered by this offer will take effect for that
 * user.
 * <p>
 * Permission offers can only be created by users that can manage the Realm, to offer is about.
 *
 * @see PermissionManager#makeOffer(PermissionOffer, PermissionManager.Callback)
 * @see PermissionManager#acceptOffer(String, PermissionManager.Callback)
 * @see <a href="https://realm.io/docs/realm-object-server/#permissions">Permissions description</a> for general
 * documentation.
 */

@RealmClass
public class PermissionOffer implements BasePermissionApi {

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

    /**
     * Constructor required by Realm. Should not be used.
     */
    public PermissionOffer() {
        // No args constructor required by Realm
    }

    /**
     * Creates a request for an permission offer that last until it is manually revoked.
     *
     * @param url specific url to Realm effected this offer encompass all Realms manged by the user making the offer.
     * @param accessLevel the {@link AccessLevel} granted to the user accepting the offer.
     *
     * @see PermissionManager#revokeOffer(String, PermissionManager.Callback)
     */
    public PermissionOffer(String url, AccessLevel accessLevel) {
        this(url, accessLevel, null);
    }

    /**
     * Creates a request for an permission offer that last until it is manually revoked.
     *
     * @param url specific url to Realm effected. The user sending the offer must have manage rights to this Realm.
     * @param accessLevel the {@link AccessLevel} granted to the user accepting the offer.
     * @param expiresAt the date when this offer expires.
     *
     * @see PermissionManager#revokeOffer(String, PermissionManager.Callback)
     */
    public PermissionOffer(String url, AccessLevel accessLevel, Date expiresAt) {
        validateUrl(url);
        validateAccessLevel(accessLevel);
        this.mayRead = accessLevel.mayRead();
        this.mayWrite = accessLevel.mayWrite();
        this.mayManage = accessLevel.mayManage();
        this.realmUrl = url;
        this.expiresAt = (expiresAt != null) ? (Date) expiresAt.clone() : null;
    }

    private void validateUrl(String url) {
        if (Util.isEmptyString(url)) {
            throw new IllegalArgumentException("Non-empty 'realmUrl' required.");
        }

        try {
            // Validate basic syntax.
            new URI(url);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid 'realmUrl'.", e);
        }
    }

    private void validateAccessLevel(AccessLevel accessLevel) {
        if (accessLevel == null) {
            throw new IllegalArgumentException("Non-null 'accessLevel' required.");
        }
    }

    /**
     * Returns the id uniquely identifying this offer.
     *
     * @return the id uniquely identifying this offer.
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * Returns the timestamp when this offer was created.
     *
     * @return the timstamp when this offer was created.
     */
    @Override
    @SuppressFBWarnings("EI_EXPOSE_REP")
    public Date getCreatedAt() {
        return createdAt;
    }

    /**
     * Returns the timestamp this offer was last updated.
     *
     * @return the timestamp when this offer was last updated.
     */
    @Override
    @SuppressFBWarnings("EI_EXPOSE_REP")
    public Date getUpdatedAt() {
        return updatedAt;
    }


    /**
     * Returns the server status code for this change.
     *
     * @return {@code null} if not yet processed. {@code 0} if successful, {@code >0} if an error happened.
     * See {@link #getStatusMessage()}.
     */
    @Override
    public Integer getStatusCode() {
        return statusCode;
    }

    /**
     * Returns the servers status message, if an error occurred. Otherwise it will return {@code null}.
     *
     * @return The servers status message in case of an error, {@code null} otherwise.
     */
    @Override
    public String getStatusMessage() {
        return statusMessage;
    }

    /**
     * Check if the request was successfully handled by the Realm Object Server.
     *
     * @return {@code true} if request was handled successfully. {@code false} if not. See {@link #getStatusMessage()}
     *         for the full error message.
     */
    public boolean isOfferCreated() {
        return statusCode != null && statusCode == 0;
    }

    /**
     * Returns the offer token if this offer was successfully created.
     *
     * @return the offer token or {@code null} if the offer wasn't created yet.
     */
    public String getToken() {
        return token;
    }

    /**
     * Returns the Realm URL for which the permissions are granted.
     *
     * @return the Realm URL for which the permissions should be granted.
     */
    public String getRealmUrl() {
        return realmUrl;
    }

    /**
     * Returns whether or not the user accepting this offer is granted read permission.
     *
     * @return {@code true} if the user accepting this offer is granted read permission, {@code false} if not.
     */
    public boolean mayRead() {
        return mayRead;
    }

    /**
     * Returns whether or not the user accepting this offer is granted write permission.
     *
     * @return {@code true} if the user accepting this offer is granted write permission, {@code false} if not.
     */
    public boolean mayWrite() {
        return mayWrite;
    }

    /**
     * Returns whether or not the user accepting this offer is granted manage permission. This will allow this user
     * to also grant or remove permission for other users on this Realm.
     *
     * @return {@code true} if the user accepting this offer is granted mange permission, {@code false} if not.
     */
    public boolean mayManage() {
        return mayManage;
    }

    /**
     * Returns when this offer expires. {@code null} is returned if this offer never expires.
     *
     * @return the date when this offer expires or {@code null} if it never expires.
     */
    @SuppressFBWarnings("EI_EXPOSE_REP")
    public Date getExpiresAt() {
        return expiresAt;
    }

    @Override
    public String toString() {
        return "PermissionOffer{" +
                "id='" + id + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", statusCode=" + statusCode +
                ", statusMessage='" + statusMessage + '\'' +
                ", token='" + token + '\'' +
                ", realmUrl='" + realmUrl + '\'' +
                ", mayRead=" + mayRead +
                ", mayWrite=" + mayWrite +
                ", mayManage=" + mayManage +
                ", expiresAt=" + expiresAt +
                '}';
    }
}
