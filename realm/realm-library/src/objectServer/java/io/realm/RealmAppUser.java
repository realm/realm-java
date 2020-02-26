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

import java.util.List;
import java.util.Map;

public class RealmAppUser {

    /**
     * FIXME
     */
    enum UserType {
        NORMAL("normal"),
        SERVER("server"),
        UNKNOWN("unknown");

        private String key;

        UserType(String key) {
            this.key = key;
        }
    }

    private final String name;
    private final String email;
    private final String pictureUrl;
    private final String firstName;
    private final String lastName;
    private final String gender; // TODO Enum? Values provided here?
    private final String birthday; // String? Should probably return Date
    private final String minAge; // Should be Int?
    private final String maxAge; // Should be Int?

    private final UserType userType;
    private final List<RealmAppUserIdentity> identities;
    private final String accessToken;
    private Map<String, Object> customData;
    private RealmAppCredentials.IdentityProvider loggedInIdentityProvider;

    private RealmAppUser(String name, String email, String pictureUrl, String firstName, String lastName, String gender, String birthday, String minAge, String maxAge, UserType userType, List<RealmAppUserIdentity> identities, String accessToken, Map<String, Object> customData, RealmAppCredentials.IdentityProvider loggedInIdentityProvider) {
        this.name = name;
        this.email = email;
        this.pictureUrl = pictureUrl;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.birthday = birthday;
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.userType = userType;
        this.identities = identities;
        this.accessToken = accessToken;
        this.customData = customData;
        this.loggedInIdentityProvider = loggedInIdentityProvider;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getGender() {
        return gender;
    }

    public String getBirthday() {
        return birthday;
    }

    public String getMinAge() {
        return minAge;
    }

    public String getMaxAge() {
        return maxAge;
    }

    public UserType getUserType() {
        return userType;
    }

    public List<RealmAppUserIdentity> getIdentities() {
        return identities;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public Map<String, Object> getCustomData() {
        return customData;
    }

    public RealmAppCredentials.IdentityProvider getLoggedInIdentityProvider() {
        return loggedInIdentityProvider;
    }

    // FIXME equals/hashcode/toString
}
