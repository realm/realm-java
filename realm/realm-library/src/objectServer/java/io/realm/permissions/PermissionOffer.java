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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.realm.internal.Util;


///**
// * This class represents a permission offer for a Realm that can be given to other users.
// * When an offer is successfully created, it will be represented by an {@code offerToken} that can be sent
// * to other users. Once they accept this token, the permissions covered by this offer will take effect for that
// * user.
// * <p>
// * Permission offers can only be created by users that can manage the Realm, the offer is about.
// *
// * @see PermissionManager#makePermissionsOffer(PermissionOffer, PermissionManager.MakeOfferCallback)
// * @see PermissionManager#acceptOffer(String, PermissionManager.AcceptOfferCallback)
// * @see <a href="https://realm.io/docs/realm-object-server/#permissions">Permissions description</a> for general
// * documentation.
// */

public class PermissionOffer {

    @Nonnull
    private Date createdAt = new Date();

    // Offer fields
    private String userId;
    private String token;
    @Nonnull
    private String realmUrl;
    @Nonnull
    private AccessLevel accessLevel;
    private Date expiresAt;

    /**
     * Creates a request for an permission offer that last until it is manually revoked.
     *
     * @param url specific url to Realm effected this offer encompasses all Realms manged by the user making the offer.
     * @param accessLevel the {@link AccessLevel} granted to the user accepting the offer.
     *
     * @see Sync#revokeOffer(String, PermissionManager.RevokeOfferCallback)
     */
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public PermissionOffer(String url, AccessLevel accessLevel) {
        //noinspection ConstantConditions
        this(url, accessLevel, null);
    }

    /**
     * Creates a request for a permission offer that last until it is manually revoked.
     *
     * @param url specific url to Realm effected. The user sending the offer must have manage rights to this Realm.
     * @param accessLevel the {@link AccessLevel} granted to the user accepting the offer.
     * @param expiresAt the date and time when this offer expires. If {@code null} is provided the offer never expires.
     *
     *
     * @see PermissionManager#revokeOffer(String, PermissionManager.RevokeOfferCallback)
     */
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public PermissionOffer(String url, AccessLevel accessLevel, @Nullable Date expiresAt) {
        validateUrl(url);
        validateAccessLevel(accessLevel);
        this.accessLevel = accessLevel;
        this.realmUrl = url;
        //noinspection ConstantConditions
        this.expiresAt = (expiresAt != null) ? (Date) expiresAt.clone() : null;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public PermissionOffer(String path, AccessLevel accessLevel, Date expiresAt, Date createdAt, String userId, String token) {
        this.realmUrl = path;
        this.accessLevel = accessLevel;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.userId = userId;
        this.token = token;
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
     * Returns the timestamp when this offer was created.
     *
     * @return the timstamp when this offer was created.
     */
    @SuppressFBWarnings("EI_EXPOSE_REP")
    public Date getCreatedAt() {
        return createdAt;
    }

    /**
     * Checks if the request was successfully handled by the Realm Object Server.
     *
     * @return {@code true} if the request was handled successfully. {@code false} if not. See {@link #getStatusMessage()}
     *         for the full error message.
     */
    public boolean isOfferCreated() {
        return !Util.isEmptyString(token);
    }

    /**
     * Returns the offer token if this offer was successfully created.
     *
     * @return the offer token or {@code null} if the offer wasn't created yet.
     */
    @Nullable
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
        return accessLevel.mayRead();
    }

    /**
     * Returns whether or not the user accepting this offer is granted write permission.
     *
     * @return {@code true} if the user accepting this offer is granted write permission, {@code false} if not.
     */
    public boolean mayWrite() {
        return accessLevel.mayWrite();
    }

    /**
     * Returns whether or not the user accepting this offer is granted manage permission. This will allow this user
     * to also grant or remove permission for other users on this Realm.
     *
     * @return {@code true} if the user accepting this offer is granted mange permission, {@code false} if not.
     */
    public boolean mayManage() {
        return accessLevel.mayManage();
    }

    /**
     * FIXME
     * @return
     */
    public AccessLevel getAccessLevel() {
        return accessLevel;
    }

    /**
     * Returns when this offer expires. {@code null} is returned if this offer never expires.
     *
     * @return the date when this offer expires or {@code null} if it never expires.
     */
    @SuppressFBWarnings("EI_EXPOSE_REP")
    @Nullable
    public Date getExpiresAt() {
        return expiresAt;
    }

    @Override
    public String toString() {
        return "PermissionOffer{" +
                "userId='" + userId + '\'' +
                ", createdAt=" + createdAt +
                ", token='" + token + '\'' +
                ", realmUrl='" + realmUrl + '\'' +
                ", mayRead=" + accessLevel.mayRead() +
                ", mayWrite=" + accessLevel.mayWrite() +
                ", mayManage=" + accessLevel.mayManage() +
                ", expiresAt=" + expiresAt +
                '}';
    }
}
