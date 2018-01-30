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
@RealmClass(name = "__Class")
public class ClassPermissions extends RealmObject {

    @PrimaryKey
    @Required
    private String name; // Name of the class in the schema
    private RealmList<Permission> permissions = new RealmList<>();
    private RealmList<Permission> defaultObjectPermissions = new RealmList<>();

    /**
     * FIXME
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * FIXME
     * @return
     */
    public RealmList<Permission> getPermissions() {
        return permissions;
    }

    /**
     * FIXME
     * @return
     */
    public RealmList<Permission> getDefaultObjectPermissions() {
        return defaultObjectPermissions;
    }
}
