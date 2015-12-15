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
import io.realm.annotations.Ignore;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class AnnotationTypes extends RealmObject {

    @PrimaryKey
    private long id;

    @Index
    private String indexString;
    private String notIndexString;

    @Ignore
    private String ignoreString;

    public long getId() {
        return realmGetter$id();
    }

    public void setId(long id) {
        realmSetter$id(id);
    }

    public long realmGetter$id() {
        return id;
    }

    public void realmSetter$id(long id) {
        this.id = id;
    }

    public String getIndexString() {
        return realmGetter$indexString();
    }

    public void setIndexString(String indexString) {
        realmSetter$indexString(indexString);
    }

    public String realmGetter$indexString() {
        return indexString;
    }

    public void realmSetter$indexString(String indexString) {
        this.indexString = indexString;
    }

    public String getNotIndexString() {
        return realmGetter$notIndexString();
    }

    public void setNotIndexString(String notIndexString) {
        realmSetter$notIndexString(notIndexString);
    }

    public String realmGetter$notIndexString() {
        return notIndexString;
    }

    public void realmSetter$notIndexString(String notIndexString) {
        this.notIndexString = notIndexString;
    }

    public String getIgnoreString() {
        return realmGetter$ignoreString();
    }

    public void setIgnoreString(String ignoreString) {
        realmSetter$ignoreString(ignoreString);
    }

    public String realmGetter$ignoreString() {
        return ignoreString;
    }

    public void realmSetter$ignoreString(String ignoreString) {
        this.ignoreString = ignoreString;
    }
}
