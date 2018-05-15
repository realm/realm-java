/*
 * Copyright 2017 Realm Inc.
 *
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

import java.net.URI;
import java.net.URISyntaxException;

import io.realm.PermissionManager;
import io.realm.internal.Util;


/**
 * This class represents the intent of giving a set of permissions to some users for some Realm(s).
 * <p>
 * If the request is successful, a {@link io.realm.permissions.Permission} entry will be added to each affected users
 * {@link PermissionManager}, where it can be fetched using
 * {@link PermissionManager#getPermissions(PermissionManager.PermissionsCallback)}
 *
 * @see PermissionManager#applyPermissions(PermissionRequest, PermissionManager.ApplyPermissionsCallback)
 * @see PermissionManager#getPermissions(PermissionManager.PermissionsCallback)
 */
public final class PermissionRequest {

    private final AccessLevel accessLevel;
    private final UserCondition condition;
    private final String url;

    /**
     * Creates a description of a set of permissions granted to some users for some Realms.
     *
     * @param realmUrl the Realm URL whose permissions settings should be changed. Use {@code *} to change the
     * permissions of all Realms managed by the user sending this request. The user that wants to grant these permissions
     * must have administrative rights to those Realms.
     *
     * @param condition the conditions used to match which users are effected.
     * @param accessLevel the {@link AccessLevel} to grant matching users. Setting the access level is absolute i.e., it
     * may revoke permissions for users that previously had a higher access level. To revoke all permissions, use
     * {@link AccessLevel#NONE}.
     *
     */
    public PermissionRequest(UserCondition condition, String realmUrl, AccessLevel accessLevel) {
        checkCondition(condition);
        checkUrl(realmUrl);
        checkAccessLevel(accessLevel);
        this.condition = condition;
        this.accessLevel = accessLevel;
        this.url = realmUrl;
    }

    private void checkUrl(String url) {
        if (Util.isEmptyString(url)) {
            throw new IllegalArgumentException("Non-empty 'realmUrl' required.");
        }

        if (url.equals("*")) {
            return; // Special case for selecting all URL's
        }

        try {
            // Validate basic syntax.
            new URI(url);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid 'realmUrl'.", e);
        }
    }

    private void checkCondition(UserCondition condition) {
        if (condition == null) {
            throw new IllegalArgumentException("Non-null 'condition' required.");
        }
    }

    private void checkAccessLevel(AccessLevel accessLevel) {
        if (accessLevel == null) {
            throw new IllegalArgumentException("Non-null 'accessLevel' required.");
        }
    }

    /**
     * Returns the access level that users will be granted if the request is successful.
     *
     * @return the {@link AccessLevel} users will have once this request is successfully handled.
     */
    public AccessLevel getAccessLevel() {
        return accessLevel;
    }

    /**
     * Returns the {@link UserCondition} used to match users. Those users that match will be granted the the
     * {@link AccessLevel} defined by {@link #getAccessLevel()}.
     *
     * @return the condition used to match users.
     */
    public UserCondition getCondition() {
        return condition;
    }

    /**
     * The Realm URL for which the permissions are granted. {@code *} is returned if the request should match
     * all Realms, for which the user sending the request, has administrative rights.
     *
     * @return the Realm URL for which the permissions should be granted.
     * @see io.realm.permissions.Permission#mayManage()
     */
    public String getUrl() {
        return url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        PermissionRequest that = (PermissionRequest) o;

        if (accessLevel != that.accessLevel) { return false; }
        if (!condition.equals(that.condition)) { return false; }
        return url.equals(that.url);

    }

    @Override
    public int hashCode() {
        int result = accessLevel.hashCode();
        result = 31 * result + condition.hashCode();
        result = 31 * result + url.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "PermissionRequest{" +
                "accessLevel=" + accessLevel +
                ", condition=" + condition +
                ", url='" + url + '\'' +
                '}';
    }
}
