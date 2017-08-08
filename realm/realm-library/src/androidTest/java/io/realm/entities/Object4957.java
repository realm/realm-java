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

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

// Object to reproduce issue https://github.com/realm/realm-java/issues/4957 .
public class Object4957 extends RealmObject {
    @PrimaryKey
    private int id;
    // Don't change the order of the field declaration! The order is the key to reproduce the original bug.
    private Object4957 child;
    private RealmList<Object4957> childList = new RealmList<Object4957>();

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setChild(Object4957 child) {
        this.child = child;
    }

    public RealmList<Object4957> getChildList() {
        return childList;
    }

    public Object4957 getChild() {
        return child;
    }
}
