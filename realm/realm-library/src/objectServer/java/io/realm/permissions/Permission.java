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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.realm.PermissionManager;
import io.realm.RealmObject;
import io.realm.SyncUser;
import io.realm.annotations.Required;


/**
 * This class represents a given set of permissions for one user on one Realm.
 * <p>
 * Permissions can be changed by users with administrative rights using the {@link PermissionManager}.
 *
 * @see SyncUser#getPermissionManager()
 */
public class Permission extends RealmObject {

    @Required
    private String userId;
    @Required
    private String path;
    private boolean mayRead;
    private boolean mayWrite;
    private boolean mayManage;
    @Required
    private Date updatedAt;

    /**
     * Required by Realm. Do not use.
     */
    public Permission() {
        // Required by Realm
    }

    /**
     * Returns the {@link SyncUser#getIdentity()} of the user effected by this permission.˚
     * <p>
     *
     * @return the user effected by this permission.
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
                ", mayRead=" + mayRead +
                ", mayWrite=" + mayWrite +
                ", mayManage=" + mayManage +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
