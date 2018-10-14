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

import io.realm.RealmObject;
import io.realm.annotations.RealmClass;
import io.realm.internal.annotations.ObjectServer;

/**
 * This class encapsulates the privileges granted a given {@link Role}. These privileges can be
 * applied to either the entire Realm, Classes or individual objects.
 * <p>
 * If no privileges are defined for an individual object, the values {@link ClassPermissions}
 * will be inherited, if no values are defined there, the ones from {@link RealmPermissions} will
 * be used. If no values can be found there, no privileges are granted.
 * <p>
 * Not all privileges are meaningful all levels, e.g. `canCreate` is only meaningful when applied to
 * classes, but it can still be defined at the Realm level. In that case all class permission objects
 * will inherit the value unless they specifically override it. See the individual privileges for the
 * details.
 * <p>
 * When added to either {@link RealmPermissions}, {@link ClassPermissions} or a {@link RealmObject},
 * only one Permission object can exist for that role. If multiple objects are added the behavior
 * is undefined and the Object Server might modify or delete both objects.
 *
 * @see <a href="FIX">Object Level Permissions</a> for an detailed description of the Realm Object
 * Server permission system.
 */
@ObjectServer
@RealmClass(name = "__Permission")
public class Permission extends RealmObject {

    /**
     * Creates a {@link Permission} object in a fluid manner.
     */
    public static class Builder {
        private Role role;
        private boolean canRead = false;
        private boolean canUpdate = false;
        private boolean canDelete = false;
        private boolean canSetPermissions = false;
        private boolean canQuery = false;
        private boolean canCreate = false;
        private boolean canModifySchema = false;

        /**
         * Creates the builder. The default state is that no privileges are enabled.
         *
         * @param role {@link Role} for which these privileges apply.
         */
        public Builder(Role role) {
            this.role = role;
        }

        /**
         * Enables all privileges.
         */
        public Builder allPrivileges() {
            canRead = true;
            canUpdate = true;
            canDelete = true;
            canSetPermissions = true;
            canQuery = true;
            canCreate = true;
            canModifySchema = true;
            return this;
        }

        /**
         * Disables all privileges.
         */
        public Builder noPrivileges() {
            canRead = false;
            canUpdate = false;
            canDelete = false;
            canSetPermissions = false;
            canQuery = false;
            canCreate = false;
            canModifySchema = false;
            return this;
        }

        /**
         * Defines if this role can read from given resource or not.
         *
         * <ol>
         *     <li>
         *         <b>Realm:</b>
         *         The role is allowed to read all objects from the Realm. If {@code false}, the
         *         Realm will appear completely empty to the role, effectively making it inaccessible.
         *     </li>
         *     <li>
         *         <b>Class:</b>
         *         The role is allowed to read the objects of this type and all referenced objects,
         *         even if those objects themselves have set this to {@code false}.
         *         If {@code false}, the role cannot see any object of this type and all queries
         *         against the type will return no results.
         *     </li>
         *     <li>
         *         <b>Object:</b>
         *          Determines if a role is allowed to see the individual object or not.
         *     </li>
         * </ol>
         *
         * @param canRead {@code true} if the role is allowed to read this resource, {@code false} if not.
         */
        public Builder canRead(boolean canRead) {
            this.canRead = canRead;
            return this;
        }

        /**
         * Defines if this role can update the given resource or not.
         *
         * <ol>
         *     <li>
         *         <b>Realm:</b>
         *         If {@code true}, the role is allowed update properties on all objects in the Realm.
         *         This does not include updating permissions nor creating or deleting objects.
         *     </li>
         *     <li>
         *         <b>Class:</b>
         *         If {@code true}, the role is allowed update properties on all objects of this type in
         *         the Realm. This does not include updating permissions nor creating or deleting objects.
         *     </li>
         *     <li>
         *         <b>Object:</b>
         *         If {@code true}, the role is allowed to update properties on the object. This
         *         does not cover updating permissions or deleting the object.
         *     </li>
         * </ol>
         *
         * @param canUpdate {@code true} if the role is allowed to update this resource, {@code false} if not.
         */
        public Builder canUpdate(boolean canUpdate) {
            this.canUpdate = canUpdate;
            return this;
        }

        /**
         * Defines if this role can delete the given resource or not.
         *
         * <ol>
         *     <li>
         *         <b>Realm:</b>
         *         Not applicable.
         *     </li>
         *     <li>
         *         <b>Class:</b>
         *         Not applicable.
         *     </li>
         *     <li>
         *         <b>Object:</b>
         *         If {@code true}, the role is allowed to delete the object.
         *     </li>
         * </ol>
         *
         * @param canDelete {@code true} if the role is allowed to delete this resource, {@code false} if not.
         */
        public Builder canDelete(boolean canDelete) {
            this.canDelete = canDelete;
            return this;
        }

        /**
         * Defines if this role is allowed to change permissions on the given resource.
         * Permissions can only be granted at the same permission level or below. E.g. if set on
         * a Class, it is not possible to change Realm level permissions, but does allow the role to
         * change object level permissions for objects of that type.
         *
         * <ol>
         *     <li>
         *         <b>Realm:</b>
         *         The role is allowed to modify the {@link RealmPermissions} object.
         *     </li>
         *     <li>
         *         <b>Class:</b>
         *         The role is allowed the change the {@link ClassPermissions} object.
         *     </li>
         *     <li>
         *         <b>Object:</b>
         *         The role is allowed to change the permissions on this object.
         *     </li>
         * </ol>
         *
         * @param canSetPermissions {@code true} if the role is allowed to change the permissions for this resource.
         */
        public Builder canSetPermissions(boolean canSetPermissions) {
            this.canSetPermissions = canSetPermissions;
            return this;
        }

        /**
         * Defines if this role is allowed to query the resource or not.
         * <p>
         * Note, that local queries are always possible, but the query result will just be empty.
         *
         * <ol>
         *     <li>
         *         <b>Realm:</b>
         *         Not applicable.
         *     </li>
         *     <li>
         *         <b>Class:</b>
         *         The role is allowed to query objects of this type.
         *     </li>
         *     <li>
         *         <b>Object:</b>
         *         Not applicable.
         *     </li>
         * </ol>
         *
         * @param canQuery {@code true} if the role is allowed to query objects of this type.
         */
        public Builder canQuery(boolean canQuery) {
            this.canQuery = canQuery;
            return this;
        }


        /**
         * Defines if this role is allowed to create objects of this type.
         *
         * <ol>
         *     <li>
         *         <b>Realm:</b>
         *         Not applicable.
         *     </li>
         *     <li>
         *         <b>Class:</b>
         *         If {@code true}, the role is allowed to create objects of this type.
         *     </li>
         *     <li>
         *         <b>Object:</b>
         *         Not applicable.
         *     </li>
         * </ol>
         *
         * @param canCreate {@code true} if the role is allowed to create objects of this type.
         */
        public Builder canCreate(boolean canCreate) {
            this.canCreate = canCreate;
            return this;
        }

        /**
         * Defines if this role is allowed to modify the schema of this resource.
         *
         * <ol>
         *     <li>
         *         <b>Realm:</b>
         *         If {@code true} the role is allowed to create classes in the Realm.
         *     </li>
         *     <li>
         *         <b>Class:</b>
         *         If {@code true}, the role is allowed to add properties to the specified class.
         *     </li>
         *     <li>
         *         <b>Object:</b>
         *         Not applicable.
         *     </li>
         * </ol>
         *
         * @param canModifySchema {@code true} if the role is allowed to modify the schema of this resource.
         */
        public Builder canModifySchema(boolean canModifySchema) {
            this.canModifySchema = canModifySchema;
            return this;
        }

        /**
         * Creates the unmanaged {@link Permission} object.
         */
        public Permission build() {
            return new Permission(
                    role,
                    canRead,
                    canUpdate,
                    canDelete,
                    canSetPermissions,
                    canQuery,
                    canCreate,
                    canModifySchema
            );
        }
    }

    private Role role;
    private boolean canRead;
    private boolean canUpdate;
    private boolean canDelete;
    private boolean canSetPermissions;
    private boolean canQuery;
    private boolean canCreate;
    private boolean canModifySchema;

    public Permission() {
        // Required by Realm
    }

    /**
     * Creates a set of privileges for the given role.
     */
    public Permission(Role role) {
        this.role = role;
    }

    /**
     * Creates a set of privileges for the given role.
     */
    private Permission(Role role, boolean canRead, boolean canUpdate, boolean canDelete, boolean canSetPermissions, boolean canQuery, boolean canCreate, boolean canModifySchema) {
        this.role = role;
        this.canRead = canRead;
        this.canUpdate = canUpdate;
        this.canDelete = canDelete;
        this.canSetPermissions = canSetPermissions;
        this.canQuery = canQuery;
        this.canCreate = canCreate;
        this.canModifySchema = canModifySchema;
    }

    /**
     * Returns the role these privileges apply to.
     *
     * @return the role these privileges apply to.
     */
    public Role getRole() {
        return role;
    }

    /**
     * Returns {@code true} if the role is allowed to read the resource, {@code false} if not.
     */
    public boolean canRead() {
        return canRead;
    }

    /**
     * Defines if this role can read from given resource or not.
     *
     * <ol>
     *     <li>
     *         <b>Realm:</b>
     *         The role is allowed to read all objects from the Realm. If {@code false}, the
     *         Realm will appear completely empty to the role, effectively making it inaccessible.
     *     </li>
     *     <li>
     *         <b>Class:</b>
     *         The role is allowed to read the objects of this type and all referenced objects,
     *         even if those objects themselves have set this to {@code false}.
     *         If {@code false}, the role cannot see any object of this type and all queries
     *         against the type will return no results.
     *     </li>
     *     <li>
     *         <b>Object:</b>
     *          Determines if a role is allowed to see the individual object or not.
     *     </li>
     * </ol>
     *
     * @param canRead {@code true} if the role is allowed to read this resource, {@code false} if not.
     */
    public void setCanRead(boolean canRead) {
        this.canRead = canRead;
    }

    /**
     * Returns {@code true} if the role is allowed to update the resource, {@code false} if not.
     */
    public boolean canUpdate() {
        return canUpdate;
    }

    /**
     * Defines if this role can update the given resource or not.
     *
     * <ol>
     *     <li>
     *         <b>Realm:</b>
     *         If {@code true}, the role is allowed update properties on all objects in the Realm.
     *         This does not include updating permissions nor creating or deleting objects.
     *     </li>
     *     <li>
     *         <b>Class:</b>
     *         If {@code true}, the role is allowed update properties on all objects of this type in
     *         the Realm. This does not include updating permissions nor creating or deleting objects.
     *     </li>
     *     <li>
     *         <b>Object:</b>
     *         If {@code true}, the role is allowed to update properties on the object. This
     *         does not cover updating permissions or deleting the object.
     *     </li>
     * </ol>
     *
     * @param canUpdate {@code true} if the role is allowed to update this resource, {@code false} if not.
     */
    public void setCanUpdate(boolean canUpdate) {
        this.canUpdate = canUpdate;
    }

    /**
     * Returns {@code true} if the role is allowed to delete the object , {@code false} if not.
     */
    public boolean canDelete() {
        return canDelete;
    }

    /**
     * Defines if this role can delete the given resource or not.
     *
     * <ol>
     *     <li>
     *         <b>Realm:</b>
     *         Not applicable.
     *     </li>
     *     <li>
     *         <b>Class:</b>
     *         Not applicable.
     *     </li>
     *     <li>
     *         <b>Object:</b>
     *         If {@code true}, the role is allowed to delete the object.
     *     </li>
     * </ol>
     *
     * @param canDelete {@code true} if the role is allowed to delete this resource, {@code false} if not.
     */
    public void setCanDelete(boolean canDelete) {
        this.canDelete = canDelete;
    }

    /**
     * Returns {@code true} if this this role is allowed to change permissions on the given resource.
     */
    public boolean canSetPermissions() {
        return canSetPermissions;
    }

    /**
     * Defines if this role is allowed to change permissions on the given resource.
     * Permissions can only be granted at the same permission level or below. E.g. if set on
     * a Class, it is not possible to change Realm level permissions, but does allow the role to
     * change object level permissions for objects of that type.
     *
     * <ol>
     *     <li>
     *         <b>Realm:</b>
     *         The role is allowed to modify the {@link RealmPermissions} object.
     *     </li>
     *     <li>
     *         <b>Class:</b>
     *         The role is allowed the change the {@link ClassPermissions} object.
     *     </li>
     *     <li>
     *         <b>Object:</b>
     *         The role is allowed to change the permissions on this object.
     *     </li>
     * </ol>
     *
     * @param canSetPermissions {@code true} if the role is allowed to change the permissions for this resource.
     */
    public void setCanSetPermissions(boolean canSetPermissions) {
        this.canSetPermissions = canSetPermissions;
    }

    /**
     * Returns {@code true} if the role is allowed to query the resource, {@code false} if not.
     */
    public boolean canQuery() {
        return canQuery;
    }

    /**
     * Defines if this role is allowed to query the resource or not.
     * <p>
     * Note, that local queries are always possible, but the query result will just be empty.
     *
     * <ol>
     *     <li>
     *         <b>Realm:</b>
     *         Not applicable.
     *     </li>
     *     <li>
     *         <b>Class:</b>
     *         The role is allowed to query objects of this type.
     *     </li>
     *     <li>
     *         <b>Object:</b>
     *         Not applicable.
     *     </li>
     * </ol>
     *
     * @param canQuery {@code true} if the role is allowed to query objects of this type.
     */
    public void setCanQuery(boolean canQuery) {
        this.canQuery = canQuery;
    }

    /**
     * Returns {@code true} if the role is allowed to create objects, {@code false} if not.
     */
    public boolean canCreate() {
        return canCreate;
    }

    /**
     * Defines if this role is allowed to create objects of this type.
     *
     * <ol>
     *     <li>
     *         <b>Realm:</b>
     *         Not applicable.
     *     </li>
     *     <li>
     *         <b>Class:</b>
     *         If {@code true}, the role is allowed to create objects of this type.
     *     </li>
     *     <li>
     *         <b>Object:</b>
     *         Not applicable.
     *     </li>
     * </ol>
     *
     * @param canCreate {@code true} if the role is allowed to create objects of this type.
     */
    public void setCanCreate(boolean canCreate) {
        this.canCreate = canCreate;
    }

    /**
     * Returns {@code true} if the role is allowed to modify the schema of the resource,
     * {@code false} if not.
     */
    public boolean canModifySchema() {
        return canModifySchema;
    }

    /**
     * Defines if this role is allowed to modify the schema of this resource.
     *
     * <ol>
     *     <li>
     *         <b>Realm:</b>
     *         If {@code true} the role is allowed to create classes in the Realm.
     *     </li>
     *     <li>
     *         <b>Class:</b>
     *         If {@code true}, the role is allowed to add properties to the specified class.
     *     </li>
     *     <li>
     *         <b>Object:</b>
     *         Not applicable.
     *     </li>
     * </ol>
     *
     * @param canModifySchema {@code true} if the role is allowed to modify the schema of this resource.
     */
    public void setCanModifySchema(boolean canModifySchema) {
        this.canModifySchema = canModifySchema;
    }
}
