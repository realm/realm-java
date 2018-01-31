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

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;
import io.realm.annotations.Required;
import io.realm.internal.annotations.ObjectServer;

/**
 * A role describes a function or area of authority in the Realm Object Server permission system.
 * Multiple users can have the same role and a role can be assigned different permissions.
 *
 * @see <a href="FIX">Object Level Permissions</a> for an detailed description of the Realm Object
 * Server permission system.
 */
@ObjectServer
@RealmClass(name = "__Role")
public class Role extends RealmObject {
    @PrimaryKey
    @Required
    private String name;
    private RealmList<User> members = new RealmList<>();

    public Role() {
        // Required by Realm;
    }

    /**
     * Creates a new named role. The name must be unique.
     *
     * @param name a unique name for the role.
     */
    public Role(String name) {
        this.name = name;
    }

    /**
     * Returns the name of this role.
     *
     * @return name of this role.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns all users with this role.
     *
     * @return all users with the given role.
     */
    public RealmList<User> getMembers() {
        return members;
    }
}
