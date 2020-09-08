/*
 * Copyright 2020 Realm Inc.
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
package io.realm.mongodb;

import io.realm.annotations.Beta;

/**
 * Each User is represented by 1 or more identities each defined by an
 * {@link Credentials.Provider}.
 *
 * This class represents the identity defined by a specific provider.
 */
@Beta
public class UserIdentity {

    private final String userId;
    private final String providerId;
    private final Credentials.Provider provider;

    UserIdentity(String id, String providerId) {
        this.userId = id;
        this.providerId = providerId;
        this.provider = Credentials.Provider.fromId(providerId);
    }

    /**
     * Returns a unique identifier for this identity.
     *
     * @return a unique identifier for this identifier.
     */
    public String getId() {
        return userId;
    }

    /**
     * Returns the provider defining this identity.
     *
     * @return
     */
    public Credentials.Provider getProvider() {
        return provider;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserIdentity that = (UserIdentity) o;

        if (!userId.equals(that.userId)) return false;
        if (!providerId.equals(that.providerId)) return false;
        return provider == that.provider;
    }

    @Override
    public int hashCode() {
        int result = userId.hashCode();
        result = 31 * result + providerId.hashCode();
        result = 31 * result + provider.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "UserIdentity{" +
                "userId='" + userId + '\'' +
                ", providerId='" + providerId + '\'' +
                '}';
    }
}
