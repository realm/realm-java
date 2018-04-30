/*
 * Copyright 2018 Realm Inc.
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
package io.realm.sync.permissions;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;
import io.realm.annotations.Required;
import io.realm.internal.Util;
import io.realm.internal.annotations.ObjectServer;

/**
 * A role describes a function or area of authority in the Realm Object Server permission system.
 * Multiple users can have the same role and a role can be assigned different permissions.
 *
 * @see <a href="FIX">Object Level Permissions</a> for an detailed description of the Realm Object
 * Server permission system.
 */
@ObjectServer
@RealmClass(name = "__Role")
public class Role extends RealmObject {
    @PrimaryKey
    @Required
    private String name;
    private RealmList<PermissionUser> members = new RealmList<>();

    public Role() {
        // Required by Realm;
    }

    /**
     * Creates a new named role. The name must be unique.
     *
     * @param name a unique name for the role.
     */
    public Role(String name) {
        this.name = name;
    }

    /**
     * Returns the name of this role.
     *
     * @return name of this role.
     */
    public String getName() {
        return name;
    }

    /**
     * Adds a member to this Role. Must be done from within a write transaction.
     *
     * @param userId userid of the SyncUser.
     * @throws IllegalStateException if not in a write transaction.
     * @throws IllegalArgumentException if {@code null} or empty {@code userId} is provided.
     */
    public void addMember(String userId) {
        if (isManaged()) {
            if (Util.isEmptyString(userId)) {
                throw new IllegalArgumentException("Non-empty 'userId' required");
            }
            Realm realm = getRealm();
            PermissionUser user = realm.where(PermissionUser.class).equalTo("id", userId).findFirst();
            if (user == null) {
                user = realm.createObject(PermissionUser.class, userId);
            }
            members.add(user);

        } else {
            throw new IllegalStateException("Can not add a member to a non managed Role");
        }
    }

    /**
     * Removes a member from this Role. Must be done from within a write transaction.
     *
     * @param userId userid of the SyncUser to remove.
     * @return {@code true} if the user could be removed, {@code false} if not.
     * @throws IllegalStateException if not in a write transaction.
     */
    public boolean removeMember(String userId) {
        PermissionUser user = getRealm().where(PermissionUser.class).equalTo("id", userId).findFirst();
        if (user != null) {
            return members.remove(user);
        } else {
            return false;
        }
    }

    /**
     * Checks if the provided user has this role.
     *
     * @param userId user to check
     * @return {@code true} if the user has this role, {@code false} if not.
     */
    public boolean hasMember(String userId) {
        return members.where().equalTo("id", userId).count() > 0;
    }

    /**
     * Returns the list of {@link PermissionUser} within this role.
     *
     * @return list of members associated with this role.
     */
    public RealmList<PermissionUser> getMembers() {
        return members;
    }
}
