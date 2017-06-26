/*
 * Copyright 2017 Realm Inc.
 *
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

import io.realm.PermissionManager;
import io.realm.internal.Util;


/**
 * This class represent the intent of creating a permission offer for a Realm that can be given to other users.
 * Once a permission is successfully created, it will be represented by an {@code offerToken} that can be sent
 * to other users. Once they accept this token using, the permissions covered by this offer will take effect for that
 * user.
 * <p>
 * Permission offers can only be created by users that can manage the Realm(s), to offer is about.
 *
 * @see PermissionManager#makeOffer(PermissionOfferRequest, PermissionManager.Callback)
 * @see PermissionManager#acceptOffer(String, PermissionManager.Callback)
 */
public final class PermissionOfferRequest {

    private final AccessLevel accessLevel;
    private final String url;
    private final Date expireDate;

    /**
     * Creates a request for an permission offer that last until it is manually revoked.
     *
     * @param url specific url to Realm effected this offer encompass all Realms manged
     * by the user making the offer.
     * @param accessLevel the {@link AccessLevel} granted to the user accepting the offer.
     *
     * @see PermissionManager#revokeOffer(String, PermissionManager.Callback)
     */
    public PermissionOfferRequest(String url, AccessLevel accessLevel) {
        this(url, accessLevel, null);
    }

    /**
     * Creates a request for an permission offer that last until it is manually revoked.
     *
     * @param url specific url to Realm effected or {@code *} to indicate that this offer encompass all Realms manged
     * by the user making the offer.
     * @param accessLevel the {@link AccessLevel} granted to the user accepting the offer.
     * @param expireDate the date when this offer expires.
     *
     * @see PermissionManager#revokeOffer(String, PermissionManager.Callback)
     */
    public PermissionOfferRequest(String url, AccessLevel accessLevel, Date expireDate) {
        validateUrl(url);
        validateAccessLevel(accessLevel);
        this.accessLevel = accessLevel;
        this.url = url;
        this.expireDate = expireDate;
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
     * Returns the access level that users will be granted if the request is successful.
     *
     * @return the {@link AccessLevel} users will have once this request is successfully handled.
     */
    public AccessLevel getAccessLevel() {
        return accessLevel;
    }

    /**
     * The Realm URL for which the permissions are granted. {@code *} is returned if the request should match
     * all Realms, for which the user sending the request, has administrative rights.
     *
     * @return the Realm URL for which the permissions should be granted.
     * @see Permission#mayManage()
     */
    public String getUrl() {
        return url;
    }

    /**
     * Returns the timestamp where this offer expires. {@code null} is returned if this offer never expires.
     *
     * @return the timestamp where this offer expires or {@code null} if it doesn't expire.
     */
    public Date getExpireDate() {
        return expireDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        PermissionOfferRequest that = (PermissionOfferRequest) o;

        if (accessLevel != that.accessLevel) { return false; }
        if (!url.equals(that.url)) { return false; }
        return expireDate != null ? expireDate.equals(that.expireDate) : that.expireDate == null;
    }

    @Override
    public int hashCode() {
        int result = accessLevel.hashCode();
        result = 31 * result + url.hashCode();
        result = 31 * result + (expireDate != null ? expireDate.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PermissionOfferRequest{" +
                "accessLevel=" + accessLevel +
                ", url='" + url + '\'' +
                ", expireDate=" + expireDate +
                '}';
    }
}
