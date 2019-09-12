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
package io.realm.internal.permissions;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.realm.permissions.AccessLevel;
import io.realm.permissions.PermissionOffer;


/**
 * This model is used to apply permission changes defined in the permission offer
 * object represented by the specified token, which was created by another user's
 * {@link PermissionOffer} object.
 *
 * It should be used in conjunction with an {@link io.realm.SyncUser}'s management Realm.
 *
 * @see <a href="https://realm.io/docs/realm-object-server/#permissions">Permissions description</a> for general
 * documentation.
 */
public final class PermissionOfferResponse {

    @Nonnull private final String userId;
    @Nonnull private final Date createdAt;
    private final Date expiresAt;
    @Nonnull private final String token;
    @Nonnull private final String realmUrl;
    @Nonnull private final AccessLevel accessLevel;

    public PermissionOfferResponse(String path, Date expiresAt, AccessLevel accessLevel, Date createdAt, String userId, String token) {
        this.realmUrl = path;
        this.expiresAt = (expiresAt != null) ? (Date) expiresAt.clone() : null;
        this.accessLevel = accessLevel;
        this.createdAt = (Date) createdAt.clone();
        this.userId = userId;
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public Date getCreatedAt() {
        return createdAt;
    }

    public String getToken() {
        return token;
    }

    @Nullable
    public String getRealmUrl() {
        return realmUrl;
    }

    public String getPath() {
        try {
            return new URI(realmUrl).getPath();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
