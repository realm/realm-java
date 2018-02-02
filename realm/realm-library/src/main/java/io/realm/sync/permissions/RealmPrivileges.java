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
 * FIXME: Javadoc
 * Thread confined object representing the current level of access to either the Realm, Class
 * or individual objects.
 *
 */
@ObjectServer
public final class RealmPrivileges {

    private boolean canRead;
    private boolean canUpdate;
    private boolean canDelete;
    private boolean canSetPermissions;
    private boolean canQuery;
    private boolean canCreate;
    private boolean canModifySchema;

    public RealmPrivileges(long privileges) {
        this.canRead = (privileges & (1 << 0)) != 0;
        this.canUpdate = (privileges & (1 << 1)) != 0;
        this.canDelete = (privileges & (1 << 2)) != 0;
        this.canSetPermissions = (privileges & (1 << 3)) != 0;
        this.canQuery = (privileges & (1 << 4)) != 0;
        this.canCreate = (privileges & (1 << 5)) != 0;
        this.canModifySchema = (privileges & (1 << 6)) != 0;
    }

    /**
     * FIXME
     *
     * @return
     */
    public boolean canRead() {
        return canRead;
    }

    /**
     * FIXME
     *
     * @return
     */
    public boolean canUpdate() {
        return canUpdate;
    };


    /**
     * FIXME
     *
     * @return
     */
    public boolean canDelete() {
        return canDelete;
    }

    /**
     * FIXME
     *
     * @return
     */
    public boolean canSetPermissions() {
        return canSetPermissions;
    };


    /**
     * FIXME
     *
     * @return
     */
    public boolean canQuery() {
        return canQuery;
    }

    /**
     * FIXME
     *
     * @return
     */
    public boolean canCreate() {
        return canCreate;
    }

    /**
     * FIXME
     *
     * @return
     */
    public boolean canModifySchema() {
        return canModifySchema;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RealmPrivileges that = (RealmPrivileges) o;

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
