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

/**
 * This class encapsulates the permissions for a given {@link Role}. These permissions can be
 * applied to either the entire Realm, single classes or individual objects.
 *
 * If multiple permission objects exists for the same Role, then what?
 *
 * FIXME  Best way to expose this API? Builder? Is the builder methods good enough? Some other way?
 */
@RealmClass(name = "__Permission")
public class Permission extends RealmObject {

    /**
     * FIXME
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
         * FIXME
         */
        public Builder(Role role) {
            this.role = role;
        }

        /**
         * FIXME
         */
        public Builder allPrivileges() {
            // FIXME
            return this;
        }

        /**
         * FIXME
         */
        public Builder noPrivileges() {
            // FIXME
            return this;
        }

        /**
         * FIXME
         */
        public Builder canRead(boolean canRead) {
            this.canRead = canRead;
            return this;
        }

        /**
         * FIXME
         */
        public Builder canUpdate(boolean canUpdate) {
            this.canUpdate = canUpdate;
            return this;
        }

        /**
         * FIXME
         */
        public Builder canDelete(boolean canDelete) {
            this.canDelete = canDelete;
            return this;
        }

        /**
         * FIXME
         */
        public Builder canSetPermissions(boolean canSetPermissions) {
            this.canSetPermissions = canSetPermissions;
            return this;
        }

        /**
         * FIXME
         */
        public Builder canQuery(boolean canQuery) {
            this.canQuery = canQuery;
            return this;
        }


        /**
         * FIXME
         */
        public Builder canCreate(boolean canCreate) {
            this.canCreate = canCreate;
            return this;
        }

        /**
         * FIXME
         */
        public Builder canModifySchema(boolean canModifySchema) {
            this.canModifySchema = canModifySchema;
            return this;
        }

        /**
         * FIXME
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
