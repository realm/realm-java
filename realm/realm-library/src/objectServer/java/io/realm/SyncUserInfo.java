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

package io.realm;

import io.realm.internal.network.LookupUserIdResponse;

/**
 * POJO representing information about a user that was retrieved from a user lookup call.
 * @see SyncUser#retrieveInfoForUser(String, String)
 */

public class SyncUserInfo {
    private final String provider;
    private final String providerUserIdentity;
    private final String identity;
    private final boolean isAdmin;

    private SyncUserInfo(String provider, String providerUserIdentity, String identity, boolean isAdmin) {
        this.provider = provider;
        this.providerUserIdentity = providerUserIdentity;
        this.identity = identity;
        this.isAdmin = isAdmin;
    }

    static SyncUserInfo fromLookupUserIdResponse(LookupUserIdResponse response) {
        return new SyncUserInfo(response.getProvider(), response.getProviderId(), response.getUserId(), response.isAdmin());
    }

    /**
     * @return identity providers {@link io.realm.SyncCredentials.IdentityProvider} which manages the user represented by this user info instance.
     */
    public String getProvider() {
        return provider;
    }

    /**
     * @return The username or identity issued to this user by the authentication provider.
     */
    public String getProviderUserIdentity() {
        return providerUserIdentity;
    }

    /**
     * @return The identity issued to this user by the Realm Object Server.
     */
    public String getIdentity() {
        return identity;
    }

    /**
     * @return Whether the user is flagged on the Realm Object Server as an administrator.
     */
    public boolean isAdmin() {
        return isAdmin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SyncUserInfo that = (SyncUserInfo) o;

        if (isAdmin != that.isAdmin) return false;
        if (!provider.equals(that.provider)) return false;
        if (!providerUserIdentity.equals(that.providerUserIdentity)) return false;
        return identity.equals(that.identity);
    }

    @Override
    public int hashCode() {
        int result = provider.hashCode();
        result = 31 * result + providerUserIdentity.hashCode();
        result = 31 * result + identity.hashCode();
        result = 31 * result + (isAdmin ? 1 : 0);
        return result;
    }
}
