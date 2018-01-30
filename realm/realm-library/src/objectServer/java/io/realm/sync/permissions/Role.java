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

/**
 * FIXME
 */
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
     * FIXME
     *
     * @param name
     */
    public Role(String name) {
        this.name = name;
    }

    /**
     * FIXME
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * FIXME
     *
     * @return
     */
    public RealmList<User> getMembers() {
        return members;
    }
}
