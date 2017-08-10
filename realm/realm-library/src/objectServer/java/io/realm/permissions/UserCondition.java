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

package io.realm.permissions;

import io.realm.PermissionManager;
import io.realm.SyncUser;
import io.realm.internal.Util;


/**
 * This class represents a condition for matching users on the Realm Object Server.
 * It is used when a request for changing existing permissions is made.
 *
 * @see PermissionRequest
 * @see io.realm.PermissionManager#applyPermissions(PermissionRequest, PermissionManager.ApplyPermissionsCallback)
 */
public final class UserCondition {

    private final String key;
    private final String value;
    private final MatcherType type;

    /**
     * Creates a condition for matching, exactly, a users username. The comparison is case-sensitive and wildcards are
     * not allowed.
     *
     * @param username exact username to match against.
     */
    public static UserCondition username(String username) {
        if (Util.isEmptyString(username)) {
            throw new IllegalArgumentException("Non-empty 'username' required.");
        }
        return new UserCondition(MatcherType.METADATA, "email", username);
    }

    /**
     * Creates a condition for matching, exactly, a users id.
     *
     * @param userId user id to match against. No wildcards are allowed.
     * @see SyncUser#getIdentity()
     */
    public static UserCondition userId(String userId) {
        if (Util.isEmptyString(userId)) {
            throw new IllegalArgumentException("Non-empty 'userId' required.");
        }
        return new UserCondition(MatcherType.USER_ID, "", userId);
    }

    /**
     * Creates a condition that will match all users with no permissions for the Realm.
     * <p>
     * The {@link AccessLevel} defined alongside this condition, will also be used as the default access level
     * for future new users that might be given access to the Realm.
     *
     * @see PermissionManager#makeOffer(PermissionOffer, PermissionManager.MakeOfferCallback)
     */
    public static UserCondition noExistingPermissions() {
        return userId("*");
    }

    /**
     * Creates a custom permission condition.
     * This will apply the permissions based on a key/value combination in the user's metadata.
     *
     * @param key key to use.
     * @param value value for that field to match.
     */
    public static UserCondition keyValue(String key, String value) {
        if (Util.isEmptyString(key)) {
            throw new IllegalArgumentException("Non-empty 'key' required.");
        }
        if (value == null) {
            throw new IllegalArgumentException("Non-null 'value' required.");
        }
        return new UserCondition(MatcherType.METADATA, key, value);
    }

    private UserCondition(MatcherType type, String key, String value) {
        this.type = type;
        this.key = key;
        this.value = value;
    }

    /**
     * Returns the they in the users metadata that is used for evaluating this condition.
     *
     * @return the key in the users metadata.
     */
    public String getKey() {
        return key;
    }

    /**
     * Returns the value that is used when matching users. The semantics of the value will be different
     * depending on the type of key used.
     *
     * @return the value to searchh for in the users meta data.
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns the type of data this condition matches.
     *
     * @return the type of data this condition matches.
     */
    public MatcherType getType() {
        return type;
    }

    /**
     * Type of matcher this condition represents.
     */
    public enum MatcherType {
        USER_ID,
        METADATA
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        UserCondition that = (UserCondition) o;

        if (!key.equals(that.key)) { return false; }
        return value.equals(that.value);

    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "UserCondition{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
