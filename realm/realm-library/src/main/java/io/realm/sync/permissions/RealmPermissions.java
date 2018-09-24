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
import io.realm.internal.annotations.ObjectServer;
import io.realm.internal.sync.PermissionHelper;

/**
 * Class describing all permissions related to a given Realm. Permissions attached to this class
 * are treated as the default permissions if not otherwise overridden by {@link ClassPermissions}
 * or object level permissions.
 *
 * @see <a href="FIX">Object Level Permissions</a> for an detailed description of the Realm Object
 * Server permission system.
 */
@ObjectServer
@RealmClass(name = "__Realm")
public class RealmPermissions extends RealmObject {
    @PrimaryKey
    private int id = 0; // Singleton object for the Realm file
    private RealmList<Permission> permissions = new RealmList<>();

    public RealmPermissions() {
        // Required by Realm
    }

    /**
     * Returns all Realm level permissions, i.e. permissions that apply to the Realm as a whole.
     *
     * @return all Realm level permissions
     */
    public RealmList<Permission> getPermissions() {
        return permissions;
    }

    /**
     * Finds the permissions associated with a given {@link Role}. If either the role or the permission
     * object doesn't exists, it will be created.
     * <p>
     * If the {@link Permission} object is created because one didn't exist already, it will be
     * created with all privileges disabled.
     * <p>
     * If the role {@link Role} object is created because one didn't exists, it will be created
     * with no members.
     *
     * @param roleName name of the role to find.
     * @return permission object for the given role.
     * @throws IllegalStateException if this object is not managed by Realm.
     * @throws IllegalStateException if this method is not called inside a write transaction.
     * @throws IllegalArgumentException if a {@code null} or empty
     */
    public Permission findOrCreate(String roleName) {
        // Error handling done in the helper class
        return PermissionHelper.findOrCreatePermissionForRole(this, permissions, roleName);
    }
}
