/*
 * Copyright 2014 Realm Inc.
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

public class AnnotationNameConventions extends RealmObject {
    private long id_object;
    private long object_id;
    private boolean isObject;
    private boolean hasObject;
    private long mObject;

    public long getId_object() {
        return id_object;
    }

    public void setId_object(long id_object) {
        this.id_object = id_object;
    }

    public long getObject_id() {
        return object_id;
    }

    public void setObject_id(long object_id) {
        this.object_id = object_id;
    }

    public boolean isObject() {
        return isObject;
    }

    public void setObject(boolean isObject) {
        this.isObject = isObject;
    }

    public boolean isHasObject() {
        return hasObject;
    }

    public void setHasObject(boolean hasObject) {
        this.hasObject = hasObject;
    }

    public long getmObject() {
        return mObject;
    }

    public void setmObject(long mObject) {
        this.mObject = mObject;
    }
}
