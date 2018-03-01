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

import io.realm.internal.annotations.ObjectServer;

/**
 * This object combines all privileges granted on the Class by all Roles which the
 * current User is a member of into the final privileges which will be enforced by
 * the server.
 *
 * The privilege calculation is done locally using cached data, and inherently may
 * be stale. It is possible that this method may indicate that an operation is
 * permitted but the server will still reject it if permission is revoked before
 * the changes have been integrated on the server. If this happens, the server will
 * automatically revoke any illegal operations.
 *
 * Non-synchronized Realms always have permission to perform all operations.
 */
@ObjectServer
public final class ClassPrivileges {

    private boolean canRead;
    private boolean canUpdate;
    private boolean canDelete;
    private boolean canSetPermissions;
    private boolean canQuery;
    private boolean canCreate;
    private boolean canModifySchema;

    public ClassPrivileges(long privileges) {
        this.canRead = (privileges & (1 << 0)) != 0;
        this.canUpdate = (privileges & (1 << 1)) != 0;
        this.canDelete = (privileges & (1 << 2)) != 0;
        this.canSetPermissions = (privileges & (1 << 3)) != 0;
        this.canQuery = (privileges & (1 << 4)) != 0;
        this.canCreate = (privileges & (1 << 5)) != 0;
        this.canModifySchema = (privileges & (1 << 6)) != 0;
    }

    /**
     * Returns whether or not the user can read objects of this type.
     * <p>
     * If {@code false}, the current User is not permitted to see objects of this type, and
     + attempting to query this class will always return empty results.
     + <p>
     + Note that Read permissions are transitive, and so it may be possible to read an
     + object which the user does not directly have Read permissions for by following a
     + link to it from an object they do have Read permissions for. This does not apply
     + to any of the other permission types.
     *
     * @return {@code true} if the user can read objects of the given type, {@code false} if not.
     */
    public boolean canRead() {
        return canRead;
    }

    /**
     * Returns whether or not the user can update objects of the given type.
     * <p>
     * If {@code true}, the user is allowed to update properties on all objects of this type in
     * the Realm. This does not include updating permissions nor creating or deleting objects.
     *
     * @return {@code true} if the user can update objects of the given type, {@code false} if not.
     */
    public boolean canUpdate() {
        return canUpdate;
    };

    /**
     * Returns whether or not the user can change the {@link ClassPermissions} object representing
     * the given class. See this clas for further details.
     *
     * @return {@code true} if the user can modify the {@link ClassPermissions} object for the given
     * class, {@code false} if not.
     * @see ClassPermissions
     */
    public boolean canSetPermissions() {
        return canSetPermissions;
    };

    /**
     * Returns whether or not the user can query the given class.
     * <p>
     * If this returns {@code false}, queries can still be run, but they will always return the
     * empty result. This can be useful to prevent people from querying leaf objects in a tree
     * structure and force them to only access objects through some parent objects that reference
     * them.
     *
     * @return {@code true} if the user can query the given class, {@code false} if not.
     */
    public boolean canQuery() {
        return canQuery;
    }

    /**
     * Returns whether or not this user is allowed to create objects of this type.
     *
     * @return {@code true} if the user can create objects of this type, {@code false} if not.
     */
    public boolean canCreate() {
        return canCreate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClassPrivileges that = (ClassPrivileges) o;

        if (canRead != that.canRead) return false;
        if (canUpdate != that.canUpdate) return false;
        if (canDelete != that.canDelete) return false;
        if (canSetPermissions != that.canSetPermissions) return false;
        if (canQuery != that.canQuery) return false;
        if (canCreate != that.canCreate) return false;
        return canModifySchema == that.canModifySchema;
    }

    @Override
    public int hashCode() {
        int result = (canRead ? 1 : 0);
        result = 31 * result + (canUpdate ? 1 : 0);
        result = 31 * result + (canDelete ? 1 : 0);
        result = 31 * result + (canSetPermissions ? 1 : 0);
        result = 31 * result + (canQuery ? 1 : 0);
        result = 31 * result + (canCreate ? 1 : 0);
        result = 31 * result + (canModifySchema ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "RealmPrivileges{" +
                "canRead=" + canRead +
                ", canUpdate=" + canUpdate +
                ", canDelete=" + canDelete +
                ", canSetPermissions=" + canSetPermissions +
                ", canQuery=" + canQuery +
                ", canCreate=" + canCreate +
                ", canModifySchema=" + canModifySchema +
                '}';
    }
}
