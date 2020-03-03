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
package io.realm;

import javax.annotation.Nullable;

import io.realm.internal.objectstore.OsSyncUser;

public class RealmUser {

    private final OsSyncUser osUser;

    /**
     * FIXME
     */
    enum UserType {
        NORMAL("normal"),
        SERVER("server"),
        UNKNOWN("unknown");

        private final String key;

        UserType(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

//    private final UserType userType;
//    private final List<RealmUserIdentity> identities;
//    private final String accessToken;
//    private Map<String, Object> customData;
//    private RealmCredentials.IdentityProvider loggedInIdentityProvider;

    RealmUser(long nativePtr) {
        this.osUser = new OsSyncUser(nativePtr);
    }

    public String getName() {
        return osUser.nativeGetName();
    }

    @Nullable
    public String getEmail() {
        return osUser.getEmail();
    }

    @Nullable
    public String getPictureUrl() {
        return osUser.getPictureUrl();
    }

    @Nullable
    public String getFirstName() {
        return osUser.getFirstName();
    }

    @Nullable
    public String getLastName() {
        return osUser.getLastName();
    }

    @Nullable
    public String getGender() {
        return osUser.getGender();
    }

    @Nullable
    public String getBirthday() {
        return osUser.getBirthday();
    }

    @Nullable
    public Long getMinAge() {
        String minAge = osUser.getMinAge();
        return (minAge == null) ? null : Long.parseLong(minAge);
    }

    @Nullable
    public Long getMaxAge() {
        String maxAge = osUser.getMaxAge();
        return (maxAge == null) ? null : Long.parseLong(maxAge);
    }
//
//    public UserType getUserType() {
//        return userType;
//    }
//
//    public List<RealmUserIdentity> getIdentities() {
//        return identities;
//    }
//
//    public String getAccessToken() {
//        return accessToken;
//    }
//
//    public Map<String, Object> getCustomData() {
//        return customData;
//    }
//
//    public RealmCredentials.IdentityProvider getLoggedInIdentityProvider() {
//        return loggedInIdentityProvider;
//    }

}
