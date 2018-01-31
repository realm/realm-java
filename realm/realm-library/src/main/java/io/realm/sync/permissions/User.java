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

import javax.annotation.Nullable;

import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.SyncUser;
import io.realm.annotations.LinkingObjects;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;
import io.realm.annotations.Required;
import io.realm.internal.annotations.ObjectServer;

/**
 * Class describes a user in the Realm Object Servers Permission system.
 * The Id should be identical to the value from {@link SyncUser#getIdentity()}
 *
 * @see <a href="FIX">Object Level Permissions</a> for an detailed description of the Realm Object
 * Server permission system.
 */
@ObjectServer
@RealmClass(name = "__User")
public class User extends RealmObject {
    @PrimaryKey
    @Required
    private String id;

    @LinkingObjects
    RealmResults<Role> roles = null;

    public User() {
        // Required by Realm
    }

    /**
     * Creates a new user.
     *
     * @param id identify of the user. Should be identitical to {@link SyncUser#getIdentity()}.
     */
    public User(String id) {
        this.id = id;
    }

    /**
     * Returns the identify of this user.
     *
     */
    public String getId() {
        return id;
    }


    /**
     * Returns all {@link Role}s this user has.
     *
     * @return all roles this user has.
     */
    public @Nullable  RealmResults<Role> getRoles() {
        return roles;
    }
}
