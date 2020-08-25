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

import javax.annotation.Nullable;

import io.realm.internal.objectstore.OsSyncUser;

public class Profile {
    private final OsSyncUser osUser;

    public Profile(OsSyncUser osUser) {
        this.osUser = osUser;
    }

    /**
     * Returns the name of the user.
     *
     * @return the name of the user.
     */
    @Nullable
    public String getName() {
        return osUser.nativeGetName();
    }

    /**
     * Returns the email address of the user.
     *
     * @return the email address of the user or null if there is no email address associated with the user.
     * address.
     */
    @Nullable
    public String getEmail() {
        return osUser.getEmail();
    }

    /**
     * Returns the picture URL of the user.
     *
     * @return the picture URL of the user or null if there is no picture URL associated with the user.
     */
    @Nullable
    public String getPictureUrl() {
        return osUser.getPictureUrl();
    }

    /**
     * Return the first name of the user.
     *
     * @return the first name of the user or null if there is no first name associated with the user.
     */
    @Nullable
    public String getFirstName() {
        return osUser.getFirstName();
    }

    /**
     * Return the last name of the user.
     *
     * @return the last name of the user or null if there is no last name associated with the user.
     */
    @Nullable
    public String getLastName() {
        return osUser.getLastName();
    }

    /**
     * Returns the gender of the user.
     *
     * @return the gender of the user or null if there is no gender associated with the user.
     */
    @Nullable
    public String getGender() {
        return osUser.getGender();
    }

    /**
     * Returns the birthday of the user.
     *
     * @return the birthday of the user or null if there is no birthday associated with the user.
     */
    @Nullable
    public String getBirthday() {
        return osUser.getBirthday();
    }

    /**
     * Returns the minimum age of the user.
     *
     * @return the minimum age of the user or null if there is no minimum age associated with the user.
     */
    @Nullable
    public Long getMinAge() {
        String minAge = osUser.getMinAge();
        return (minAge == null) ? null : Long.parseLong(minAge);
    }

    /**
     * Returns the maximum age of the user.
     *
     * @return the maximum age of the user or null if there is no maximum age associated with the user.
     */
    @Nullable
    public Long getMaxAge() {
        String maxAge = osUser.getMaxAge();
        return (maxAge == null) ? null : Long.parseLong(maxAge);
    }
}
