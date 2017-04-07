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
package io.realm.entities;

import io.realm.RealmObject;

public class BacklinksWrongTypeSource extends RealmObject {
    private Integer childId;

    private BacklinksWrongTypeTarget child;

    public Integer getChildId() {
        return childId;
    }

    public void setChildId(Integer childId) {
        this.childId = childId;
    }

    public BacklinksWrongTypeTarget getChild() {
        return child;
    }

    public void setChild(BacklinksWrongTypeTarget child) {
        this.child = child;
    }
}
