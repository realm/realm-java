/*
 * Copyright 2019 Realm Inc.
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
package io.realm.entities;

import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;
import io.realm.annotations.RealmField;

@RealmClass(name = "backlink_override_name")
public class BacklinkWithOverridenNames extends RealmObject {

    @PrimaryKey
    public String id;

    @RealmField(name = "forward_link")
    public BacklinkWithOverridenNames child;

    @LinkingObjects("child")
    public final RealmResults<BacklinkWithOverridenNames> parents = null;

    public BacklinkWithOverridenNames() {

    }

    public BacklinkWithOverridenNames(String id) {
        this.id = id;
    }
}
