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
 * applied to either the entire Realm, Classes or individual objects, but not all privileges
 * are used at all levels. See the individual privileges for the exact details.
 * </p>
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
         * Disables all privileges.
         */
        public Builder noPrivileges() {
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
         * Define if this role can read from given resource.
         * FIXME: Describe exact semantics for Realm/Class/Object
         *
         * @param canRead {@code true} if the role is allowed to read this resource, {@code false} if not.
         */
        public Builder canRead(boolean canRead) {
            this.canRead = canRead;
            return this;
        }

        /**
         * Defines if the role can make changes (but not delete) a resource.
         * FIXME: Describe exact semantics for Realm/Class/Object

         * @param canUpdate {@code true} if the role is allowed to update this resource, {@code false} if not.
         */
        public Builder canUpdate(boolean canUpdate) {
            this.canUpdate = canUpdate;
            return this;
        }

        /**
         * Defines if the role can delete a resource.
         * FIXME: Describe exact semantics for Realm/Class/Object
         *
         * @param canDelete {@code true} if the role is allowed to delete this resource, {@code false} if not.
         */
        public Builder canDelete(boolean canDelete) {
            this.canDelete = canDelete;
            return this;
        }

        /**
         * Defines if the role is allowed to change the permissions on this resource.
         * FIXME: Describe exact semantics for Realm/Class/Object
         *
         * @param canSetPermissions {@code true} if the role is allowed to change the permissions for this resource, {@code false} if not.
         */
        public Builder canSetPermissions(boolean canSetPermissions) {
            this.canSetPermissions = canSetPermissions;
            return this;
        }

        /**
         * Defines if the role is allowed to query this resource.
         * FIXME: Describe exact semantics for Realm/Class/Object
         *
         * @param canQuery {@code true} if the role is allowed to query this resource, {@code false} if not.
         */
        public Builder canQuery(boolean canQuery) {
            this.canQuery = canQuery;
            return this;
        }


        /**
         * Defines if the role is allowed to creates child resources.
         *
         * FIXME: Describe exact semantics for Realm/Class/Object
         *
         * @param canCreate {@code true} if the role is allowed to query this resource, {@code false} if not.
         */
        public Builder canCreate(boolean canCreate) {
            this.canCreate = canCreate;
            return this;
        }

        /**
         * Defines if the role is allowed to modify the schema for this resource.
         *
         * FIXME: Describe exact semantics for Realm/Class/Object
         *
         * @param canModifySchema {@code true} if the role is allowed to query this resource, {@code false} if not.
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
     * FIXME
     */
    public Role getRole() {
        return role;
    }

    /**
     * FIXME
     */
    public void setRole(Role role) {
        this.role = role;
    }

    /**
     * FIXME
     */
    public boolean canRead() {
        return canRead;
    }

    /**
     * FIXME
     */
    public void setCanRead(boolean canRead) {
        this.canRead = canRead;
    }

    /**
     * FIXME
     */
    public boolean canUpdate() {
        return canUpdate;
    }

    /**
     * FIXME
     */
    public void setCanUpdate(boolean canUpdate) {
        this.canUpdate = canUpdate;
    }

    /**
     * FIXME
     */
    public boolean canDelete() {
        return canDelete;
    }

    /**
     * FIXME
     */
    public void canDelete(boolean canDelete) {
        this.canDelete = canDelete;
    }

    /**
     * FIXME
     */
    public boolean canSetPermissions() {
        return canSetPermissions;
    }

    /**
     * FIXME
     */
    public void canSetPermissions(boolean canSetPermissions) {
        this.canSetPermissions = canSetPermissions;
    }

    /**
     * FIXME
     */
    public boolean canQuery() {
        return canQuery;
    }

    /**
     * FIXME
     */
    public void setCanQuery(boolean canQuery) {
        this.canQuery = canQuery;
    }

    /**
     * FIXME
     */
    public boolean canCreate() {
        return canCreate;
    }

    /**
     * FIXME
     */
    public void setCanCreate(boolean canCreate) {
        this.canCreate = canCreate;
    }

    /**
     * FIXME
     */
    public boolean canModifySchema() {
        return canModifySchema;
    }

    /**
     * FIXME
     */
    public void setCanModifySchema(boolean canModifySchema) {
        this.canModifySchema = canModifySchema;
    }
}
