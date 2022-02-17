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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class UserProfile {
    private final User user;

    UserProfile(User user) {
        this.user = user;
    }

    /**
     * Returns the name of the user.
     *
     * @return the name of the user.
     */
    @Nullable
    public String getName() {
        return user.osUser.nativeGetName();
    }

    /**
     * Returns the email address of the user.
     *
     * @return the email address of the user or null if there is no email address associated with the user.
     * address.
     */
    @Nullable
    public String getEmail() {
        return user.osUser.getEmail();
    }

    /**
     * Returns the picture URL of the user.
     *
     * @return the picture URL of the user or null if there is no picture URL associated with the user.
     */
    @Nullable
    public String getPictureUrl() {
        return user.osUser.getPictureUrl();
    }

    /**
     * Return the first name of the user.
     *
     * @return the first name of the user or null if there is no first name associated with the user.
     */
    @Nullable
    public String getFirstName() {
        return user.osUser.getFirstName();
    }

    /**
     * Return the last name of the user.
     *
     * @return the last name of the user or null if there is no last name associated with the user.
     */
    @Nullable
    public String getLastName() {
        return user.osUser.getLastName();
    }

    /**
     * Returns the gender of the user.
     *
     * @return the gender of the user or null if there is no gender associated with the user.
     */
    @Nullable
    public String getGender() {
        return user.osUser.getGender();
    }

    /**
     * Returns the birthday of the user.
     *
     * @return the birthday of the user or null if there is no birthday associated with the user.
     */
    @Nullable
    public String getBirthday() {
        return user.osUser.getBirthday();
    }

    /**
     * Returns the minimum age of the user.
     *
     * @return the minimum age of the user or null if there is no minimum age associated with the user.
     */
    @Nullable
    public Long getMinAge() {
        String minAge = user.osUser.getMinAge();
        return (minAge == null) ? null : Long.parseLong(minAge);
    }

    /**
     * Returns the maximum age of the user.
     *
     * @return the maximum age of the user or null if there is no maximum age associated with the user.
     */
    @Nullable
    public Long getMaxAge() {
        String maxAge = user.osUser.getMaxAge();
        return (maxAge == null) ? null : Long.parseLong(maxAge);
    }

    /**
     * Returns the {@link User} that this instance in associated with.
     *
     * @return The {@link User} that this instance in associated with.
     */
    public User getUser() {
        return user;
    }

    @Override
    public String toString() {
        return "Profile{" +
                "name='" + getName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", pictureUrl='" + getPictureUrl() + '\'' +
                ", firstName='" + getFirstName() + '\'' +
                ", lastName='" + getLastName() + '\'' +
                ", gender='" + getGender() + '\'' +
                ", birthday='" + getBirthday() + '\'' +
                ", minAge=" + getMinAge() +
                ", maxAge=" + getMaxAge() +
                '}';
    }

    @SuppressFBWarnings("NP_METHOD_PARAMETER_TIGHTENS_ANNOTATION")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserProfile profile = (UserProfile) o;

        return user.equals(profile.user);
    }

    @Override
    public int hashCode() {
        return user.hashCode();
    }
}
