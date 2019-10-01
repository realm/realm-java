/*
 * Copyright 2017 Realm Inc.
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

package io.realm.permissions;

import java.util.Date;

import javax.annotation.Nullable;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.realm.SyncUser;


/**
 * This class represents the given set of permissions provided to a user for the Realm identified by
 * {@link #path}.
 * <p>
 * Permissions can be changed by users with administrative rights using {@link SyncUser#applyPermissions(PermissionRequest)}.
 */
public final class Permission {

    @Nullable private final String userId;
    private final String path;
    private final AccessLevel accessLevel;
    private final boolean mayRead;
    private final boolean mayWrite;
    private final boolean mayManage;
    private final Date updatedAt;

    public Permission(@Nullable String userId, String path, AccessLevel accessLevel, boolean mayRead, boolean mayWrite, boolean mayManage, Date updatedAt) {
        this.userId = userId;
        this.path = path;
        this.accessLevel = accessLevel;
        this.mayRead = mayRead;
        this.mayWrite = mayWrite;
        this.mayManage = mayManage;
        this.updatedAt = (Date) updatedAt.clone();
    }

    /**
     * Returns the {@link SyncUser#getIdentity()} of the user effected by this permission or
     * {@code null} if this permissions applies to all users.
     *
     * @return the user(s) effected by this permission.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Returns the path to the Realm on the server effected by this permission. This is not the full URL.
     *
     * @return the path to the Realm this permission object refers to.
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns the access level granted by this permission.
     *
     * @return access level granted by this permission.
     */
    public AccessLevel getAccessLevel() {
        return accessLevel;
    }

    /**
     * Checks whether or not the user defined by this permission is allowed to read the Realm defined by
     * {@link #getPath()}.
     *
     * @return {@code true} if this permission grant read permissions to the Realm, {@code false} if not.
     */
    public boolean mayRead() {
        return mayRead;
    }

    /**
     * Checks whether or not the user defined by this permission is allowed to write to the Realm defined by
     * {@link #getPath()}.
     *
     * @return {@code true} if this permission grant write permissions to the Realm, {@code false} if not.
     */
    public boolean mayWrite() {
        return mayWrite;
    }

    /**
     * Checks whether or not the user defined by this permission is allowed to manage access to the Realm defined
     * by {@link #getPath()}. Having this permission enable those users to add or remove permissions from
     * other users, including the one who granted it.
     *
     * @return {@code true} if this permission grant administrative rights to the Realm, {@code false} if not.
     */
    public boolean mayManage() {
        return mayManage;
    }

    /**
     * Returns the timestamp for when this permission object was last updated.
     *
     * @return the timestamp for when this permission was last updated.
     */
    @SuppressFBWarnings({"EI_EXPOSE_REP"})
    public Date getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return "Permission{" +
                "userId='" + userId + '\'' +
                ", path='" + path + '\'' +
                ", accessLevel=" + accessLevel +
                ", mayRead=" + mayRead +
                ", mayWrite=" + mayWrite +
                ", mayManage=" + mayManage +
                ", updatedAt=" + updatedAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Permission that = (Permission) o;

        if (mayRead != that.mayRead) return false;
        if (mayWrite != that.mayWrite) return false;
        if (mayManage != that.mayManage) return false;
        if (userId != null ? !userId.equals(that.userId) : that.userId != null) return false;
        if (!path.equals(that.path)) return false;
        if (accessLevel != that.accessLevel) return false;
        return updatedAt.equals(that.updatedAt);
    }

    @Override
    public int hashCode() {
        int result = userId != null ? userId.hashCode() : 0;
        result = 31 * result + path.hashCode();
        result = 31 * result + accessLevel.hashCode();
        result = 31 * result + (mayRead ? 1 : 0);
        result = 31 * result + (mayWrite ? 1 : 0);
        result = 31 * result + (mayManage ? 1 : 0);
        result = 31 * result + updatedAt.hashCode();
        return result;
    }
}
