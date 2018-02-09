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
 * This object describes the privileges granted the currently logged in user for either the Realm,
 * a specific Realm Model Class or an Object. Some of the privileges are not applicable at the
 * different levels, so see the individual privileges for the details.
 * <p>
 * It is obtained by calling either {@link Realm#getPrivileges()}, {@link Realm#getPrivileges(Class)}
 * or {@link Realm#getPrivileges(RealmModel)} .
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
     * Returns whether or not the user can read the given resource.
     *
     * <ol>
     *     <li>
     *         <b>Realm:</b>
     *         If {@code true}, the user is allowed to read all objects and classes from the Realm.
     *         If {@code false}, the Realm will appear completely empty , effectively making it
     *         inaccessible.
     *     </li>
     *     <li>
     *         <b>Class:</b>
     *         If {@code true}, the role is allowed read the objects of this type and all referenced
     *         objects, even if those objects have set this to {@code false}. If {@code false}, the
     *         user cannot see any object of this type and all queries against the type will return
     *         no results.
     *     </li>
     *     <li>
     *         <b>Object:</b>
     *         If {@code true}, the user can see the object, if {@code false} he/she cannot.
     *     </li>
     * </ol>
     *
     * @return {@code true} if the user can read the resource, {@code false} if not.
     */
    public boolean canRead() {
        return canRead;
    }

    /**
     * Returns whether or not the user can update the given resource.
     *
     * <ol>
     *     <li>
     *         <b>Realm:</b>
     *         If {@code true}, the user is allowed update properties on all objects in the Realm.
     *         This does not include updating permissions nor creating or deleting objects.
     *     </li>
     *     <li>
     *         <b>Class:</b>
     *         If {@code true}, the user is allowed update properties on all objects of this type in
     *         the Realm. This does not include updating permissions nor creating or deleting objects.
     *     </li>
     *     <li>
     *         <b>Object:</b>
     *         If {@code true}, the user is allowed to update properties on the object. This
     *         does not cover updating permissions or deleting the object.
     *     </li>
     * </ol>
     *
     * @return {@code true} if the user can update the resource, {@code false} if not.
     */
    public boolean canUpdate() {
        return canUpdate;
    };


    /**
     * Returns whether or not the user can delete the given resource.
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
     *         If {@code true}, the role is allowed to delete the object. {@code false} if not.
     *     </li>
     * </ol>
     *
     * @return {@code true} if the user can delete the resource, {@code false} if not.
     */
    public boolean canDelete() {
        return canDelete;
    }

    /**
     * Returns whether or not the user can change permissions on the given resource.
     *
     * <ol>
     *     <li>
     *         <b>Realm:</b>
     *         If {@code true} the user is allowed to modify the {@link RealmPermissions} object for
     *         this Realm.
     *     </li>
     *     <li>
     *         <b>Class:</b>
     *         if {@code true} the user is allowed to modify the {@link ClassPermissions} object
     *         representing the Realm Model class.
     *     </li>
     *     <li>
     *         <b>Object:</b>
     *         if {@code true} the user is allowed to modify the custom permissions property
     *         defined on the object.
     *     </li>
     * </ol>
     *
     * @return {@code true} if the user can modify the permissions on the resource, {@code false} if not.
     */
    public boolean canSetPermissions() {
        return canSetPermissions;
    };


    /**
     * Returns whether or not the user can query the given resource.
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
     *         If {@code true} the user is allowed to query objects of this type.
     *     </li>
     *     <li>
     *         <b>Object:</b>
     *         Not applicable.
     *     </li>
     * </ol>
     *
     * @return {@code true} if the user can query the resource, {@code false} if not.
     */
    public boolean canQuery() {
        return canQuery;
    }

    /**
     * Returns whether or not this role is allowed to create objects of this type.
     *
     * <ol>
     *     <li>
     *         <b>Realm:</b>
     *         Not applicable.
     *     </li>
     *     <li>
     *         <b>Class:</b>
     *         If {@code true}, the user is allowed to create objects of this type.
     *     </li>
     *     <li>
     *         <b>Object:</b>
     *         Not applicable.
     *     </li>
     * </ol>
     *
     * @return {@code true} if the user can create objects, {@code false} if not.
     */
    public boolean canCreate() {
        return canCreate;
    }

    /**
     * Returns whether or not the user can modify the schema of the given resource.
     *
     * <ol>
     *     <li>
     *         <b>Realm:</b>
     *         If {@code true} the user is allowed to create classes in the Realm.
     *     </li>
     *     <li>
     *         <b>Class:</b>
     *         If {@code true}, the user is allowed to add properties to the given class.
     *     </li>
     *     <li>
     *         <b>Object:</b>
     *         Not applicable.
     *     </li>
     * </ol>
     *
     * @return {@code true} if the user can modify the schema of the given resource, {@code false} if not.
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
