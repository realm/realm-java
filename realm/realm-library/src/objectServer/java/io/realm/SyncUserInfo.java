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

import java.util.Collections;
import java.util.Map;

import io.realm.internal.network.LookupUserIdResponse;

/**
 * POJO representing information about a user that was retrieved from a user lookup call.
 * @see SyncUser#retrieveInfoForUser(String, String)
 */

public class SyncUserInfo {
    private final String identity;
    private final boolean isAdmin;
    private final Map<String, String> metadata;
    private final Map<String, String> accounts;

    private SyncUserInfo(String identity, boolean isAdmin, Map<String, String> metadata, Map<String, String> accounts) {
        this.identity = identity;
        this.isAdmin = isAdmin;
        this.metadata = Collections.unmodifiableMap(metadata);
        this.accounts = Collections.unmodifiableMap(accounts);
    }

    static SyncUserInfo fromLookupUserIdResponse(LookupUserIdResponse response) {
        return new SyncUserInfo(response.getUserId(), response.isAdmin(), response.getMetadata(), response.getAccounts());
    }

    /**
     * @return the identity issued to this user by the Realm Object Server.
     */
    public String getIdentity() {
        return identity;
    }

    /**
     * @return whether the user is flagged on the Realm Object Server as an administrator.
     */
    public boolean isAdmin() {
        return isAdmin;
    }

    /**
     * Returns the metadata associated with the user. The metadata is a generic key/value map with
     * the only restriction that a key must be non-empty.
     *
     * @return the metadata associated with this user.
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * Returns the accounts associated with this user. The map returned is a map of {@link SyncCredentials.IdentityProvider}
     * and the providerId used in that provider.
     * <p>
     * Example being {@code ("password", "my@email.com") }, if the user created an account using the standard account creation
     * supported by the Realm Object Server.
     * <p>
     * A user can have multiple accounts associated with it.
     *
     * @return the accounts associated with the user.
     */
    public Map<String, String> getAccounts() { return accounts; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SyncUserInfo that = (SyncUserInfo) o;

        if (isAdmin != that.isAdmin) return false;
        if (!identity.equals(that.identity)) return false;
        return metadata.equals(that.metadata);
    }

    @Override
    public int hashCode() {
        int result = identity.hashCode();
        result = 31 * result + (isAdmin ? 1 : 0);
        result = 31 * result + metadata.hashCode();
        return result;
    }
}
