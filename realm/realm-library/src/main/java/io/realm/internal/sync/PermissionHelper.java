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
package io.realm.internal.sync;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.internal.annotations.ObjectServer;
import io.realm.sync.permissions.Permission;
import io.realm.sync.permissions.Role;

/**
 * Helper class for working with fine-grained permissions
 */
@ObjectServer
public class PermissionHelper {

    /**
     * Finds or creates the permission object for a given role. Creating objects if they cannot
     * be found.
     *
     * @param container RealmObject containg the permission objects
     * @param permissions the list of permissions
     * @param roleName the role to search for
     * @return
     */
    public static Permission findOrCreatePermissionForRole(RealmObject container, RealmList<Permission> permissions, String roleName) {
        if (!container.isManaged()) {
            throw new IllegalStateException("'findOrCreate()' can only be called on managed objects.");
        }
        Realm realm = container.getRealm();
        if (!realm.isInTransaction()) {
            throw new IllegalStateException("'findOrCreate()' can only be called inside a write transaction.");
        }

        // Find existing permission object or create new one
        Permission permission = permissions.where().equalTo("role.name", roleName).findFirst();
        if (permission == null) {

            // Find existing role or create new one
            Role role = realm.where(Role.class).equalTo("name", roleName).findFirst();
            if (role == null) {
                role = realm.createObject(Role.class, roleName);
            }

            permission = realm.copyToRealm(new Permission.Builder(role).noPrivileges().build());
            permissions.add(permission);
        }

        return permission;
    }
}
