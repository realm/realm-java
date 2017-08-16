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
 */

public class SyncUserInfo {//TODO use getter for Javadoc
    /**
     * identity providers {@link io.realm.SyncCredentials.IdentityProvider} which manages the user represented by this user info instance.
     */
    private final String provider;
    /**
     * The username or identity issued to this user by the authentication provider.
     */
    private final String providerUserIdentity;
    /**
     * The identity issued to this user by the Realm Object Server.
     */
    private final String identity;
    /**
     * Whether the user is flagged on the Realm Object Server as an administrator.
     */
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

    public String getProvider() {
        return provider;
    }

    public String getProviderUserIdentity() {
        return providerUserIdentity;
    }

    public String getIdentity() {
        return identity;
    }

    public boolean isAdmin() {
        return isAdmin;
    }
}
