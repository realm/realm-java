/*
 * Copyright 2016 Realm Inc.
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

package some.test;

import io.realm.RealmObject;


import io.realm.RealmObject;

public class ConflictingFieldName extends RealmObject {
    private String realm;
    private String row;
    private String listeners;
    private String pendingQuery;
    private String isCompleted;
    private String currentTableVersion;

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public String getRow() {
        return row;
    }

    public void setRow(String row) {
        this.row = row;
    }

    public String getListeners() {
        return listeners;
    }

    public void setListeners(String listeners) {
        this.listeners = listeners;
    }

    public String getPendingQuery() {
        return pendingQuery;
    }

    public void setPendingQuery(String pendingQuery) {
        this.pendingQuery = pendingQuery;
    }

    public String getIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(String isCompleted) {
        this.isCompleted = isCompleted;
    }

    public String getCurrentTableVersion() {
        return currentTableVersion;
    }

    public void setCurrentTableVersion(String currentTableVersion) {
        this.currentTableVersion = currentTableVersion;
    }
}
