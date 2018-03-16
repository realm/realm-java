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
import io.realm.RealmModel;
import io.realm.internal.annotations.ObjectServer;

/**
 * This object combines all privileges granted on a Realm object by all Roles which the
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
public final class ObjectPrivileges {

    private boolean canRead;
    private boolean canUpdate;
    private boolean canDelete;
    private boolean canSetPermissions;
    private boolean canQuery;
    private boolean canCreate;
    private boolean canModifySchema;

    public ObjectPrivileges(long privileges) {
        this.canRead = (privileges & (1 << 0)) != 0;
        this.canUpdate = (privileges & (1 << 1)) != 0;
        this.canDelete = (privileges & (1 << 2)) != 0;
        this.canSetPermissions = (privileges & (1 << 3)) != 0;
        this.canQuery = (privileges & (1 << 4)) != 0;
        this.canCreate = (privileges & (1 << 5)) != 0;
        this.canModifySchema = (privileges & (1 << 6)) != 0;
    }

    /**
     * Returns whether or not the user can see/read the object.
     *
     * @return {@code true} if the user can read the object, {@code false} if not.
     */
    public boolean canRead() {
        return canRead;
    }

    /**
     * Returns whether or not the user can update fields on the object. This does not
     * include deleting (see {@link #canDelete()} nor if permissions can be updated (see
     * {@link #canSetPermissions()}).
     *
     * @return {@code true} if the user can update fields on the object, {@code false} if not.
     */
    public boolean canUpdate() {
        return canUpdate;
    };


    /**
     * Returns whether or not the user can delete the object.
     *
     * @return {@code true} if the user can delete the object, {@code false} if not.
     */
    public boolean canDelete() {
        return canDelete;
    }

    /**
     * Returns whether or not the user can change permissions on the object through its custom
     * permission field (A field of the type {@code RealmList<Permission>}).
     *
     * @return {@code true} if the user can modify the permissions on the object, {@code false} if not.
     */
    public boolean canSetPermissions() {
        return canSetPermissions;
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ObjectPrivileges that = (ObjectPrivileges) o;

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
