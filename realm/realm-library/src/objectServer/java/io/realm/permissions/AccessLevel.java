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

import io.realm.PermissionManager;
import io.realm.Realm;
import io.realm.RealmConfiguration;


/**
 * Access levels which can be granted to Realm Mobile Platform users for specific synchronized Realms, using a
 * {@link PermissionRequest}.
 * <p>
 * Note that each access level guarantees all allowed actions provided by less permissive access levels.
 * Specifically, users with write access to a Realm can always read from that Realm, and users with administrative
 * access can always read or write from the Realm. This means that {@code NONE < READ < WRITE < ADMIN}.
 *
 * @see PermissionRequest
 * @see io.realm.PermissionManager#applyPermissions(PermissionRequest, PermissionManager.ApplyPermissionsCallback)
 */
public enum AccessLevel {

    /**
     * The user does not have access to this Realm.
     */
    NONE(false, false, false),

    /**
     * User can only read the contents of the Realm.
     * <p>
     * Users who have read-only access to a Realm should open it using `readOnly()` and
     * `waitForInitialRemoteData()` on the {@link io.realm.SyncConfiguration}. Attempting to directly open the Realm
     * is an error; in this case the Realm must manually be deleted using {@link Realm#deleteRealm(RealmConfiguration)}
     * before being re-opened with the correct configuration.
     * <p>
     * <pre>
     * {@code
     * SyncConfiguration config = new SyncConfiguration(getUser(), getUrl())
     *     .readOnly()
     *     .waitForInitialRemoteData()
     *     .build();
     * }
     * </pre>
     */
    READ(true, false, false),

    /**
     * User can read and write the contents of the Realm.
     */
    WRITE(true, true, false),

    /**
     * User can read, write, and administer the Realm. This includes both granting permissions as well as removing them
     * again.
     */
    ADMIN(true, true, true);

    private final boolean mayRead;
    private final boolean mayWrite;
    private final boolean mayManage;

    AccessLevel(boolean mayRead, boolean mayWrite, boolean mayManage) {
        this.mayRead = mayRead;
        this.mayWrite = mayWrite;
        this.mayManage = mayManage;
    }

    /**
     * Returns {@code true} if the user is allowed to read a Realm, {@code false} if not.
     */
    public boolean mayRead() {
        return mayRead;
    }

    /**
     * Returns {@code true} if the user is allowed to write to the Realm, {@code false} if not.
     */
    public boolean mayWrite() {
        return mayWrite;
    }

    /**
     * Returns {@code true} if the user is allowed to manage the Realm, {@code false} if not.
     * <p>
     * Having this permission, means the user is able to grant permissions to other users as well as remove them
     * again.
     */
    public boolean mayManage() {
        return mayManage;
    }
}
