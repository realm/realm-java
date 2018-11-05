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

import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;
import io.realm.annotations.Required;
import io.realm.internal.annotations.ObjectServer;
import io.realm.internal.sync.PermissionHelper;

/**
 * Class describing all permissions related to a given Realm model class. These permissions will
 * be inherited by any concrete objects of the given type.
 * <p>
 * If a class level permission grants a privilege, it is still possible for individual objects
 * to revoke them again, i.e. it is possible for the class level permission to grant general read
 * access, while the individual objects are still able to revoke them.
 * <p>
 * The opposite is not true, so if a privilege is not granted at the class level, it can never
 * be granted at the object level, no matter what kind of permissions are set there.
 *
 * @see <a href="FIX">Object Level Permissions</a> for an detailed description of the Realm Object
 * Server permission system.
 */
@ObjectServer
@RealmClass(name = "__Class")
public class ClassPermissions extends RealmObject {

    @PrimaryKey
    @Required
    private String name; // Name of the class in the schema
    private RealmList<Permission> permissions = new RealmList<>();

    @Ignore
    Class<? extends RealmModel> modelClassRef;

    public ClassPermissions() {
        // Required by Realm
    }

    /**
     * Creates permissions for the given Realm model class. Only one {@code ClassPermissions} object
     * can exist pr Realm model class.
     *
     * @param clazz class to create permissions.
     */
    public ClassPermissions(Class<? extends RealmModel> clazz) {
        //noinspection ConstantConditions
        if (clazz == null) {
            throw new IllegalArgumentException("Non-null 'clazz' required.");
        }
        modelClassRef = clazz;
        name = clazz.getSimpleName();
    }

    /**
     * Returns the name of the class these permissions apply to. If this object is unmanaged
     * this name returned will be the simple name of the Java class. If the object is managed
     * it will be the internal name Realm uses to represent the class.
     *
     * @return the name of the class these permissions apply to.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns all Class level permissions for the class defined by {@link #getName()}. This is the
     * default set of permissions for the class unless otherwise re-defined by object level
     * permissions.
     *
     * @return all Class level permissions
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
     * If the the {@link Role} object is created because one didn't exists, it will be created
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
