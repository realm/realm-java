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

import io.realm.RealmModel;
import io.realm.internal.ManagableObject;
import io.realm.internal.annotations.ObjectServer;

/**
 * FIXME: Javadoc
 * Thread confined object representing the current level of access to either the Realm, Class
 * or individual objects.
 *
 */
@ObjectServer
public class RealmPrivileges implements ManagableObject {


    public static RealmPrivileges fromObject(RealmModel model) {
        return null;
    }

    RealmPrivileges(ClassPermissions realmPermissions) {

    }


    RealmPrivileges(RealmPermissions realmPermissions) {

    }

    /**
     * FIXME
     *
     * @return
     */
    public boolean canRead() {
        return false;
    }

    /**
     * FIXME
     *
     * @return
     */
    public boolean canUpdate() {
        return false;
    };


    /**
     * FIXME
     *
     * @return
     */
    public boolean canDelete() {
        return false;
    }

    /**
     * FIXME
     *
     * @return
     */
    public boolean canSetPermissions() {
        return false;
    };


    /**
     * FIXME
     *
     * @return
     */
    boolean canQuery() {
        return false;
    }

    /**
     * FIXME
     *
     * @return
     */
    boolean canCreate() {
        return false;
    }

    /**
     * FIXME
     *
     * @return
     */
    boolean canModifySchema() {
        return false;
    }


    @Override
    public boolean isManaged() {
        return false;
    }

    @Override
    public boolean isValid() {
        return false;
    }
}
