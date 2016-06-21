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

package io.realm.entities;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class PrimaryKeyWithNoPrimaryKeyObjectRelation extends RealmObject {
    @PrimaryKey
    private String columnString;

    private AllTypes columnRealmObjectNoPK;

    private int columnInt = 8;

    public String getColumnString() {
        return columnString;
    }

    public void setColumnString(String columnString) {
        this.columnString = columnString;
    }

    public AllTypes getColumnRealmObjectNoPK() {
        return columnRealmObjectNoPK;
    }

    public void setColumnRealmObjectNoPK(AllTypes columnRealmObjectNoPK) {
        this.columnRealmObjectNoPK = columnRealmObjectNoPK;
    }

    public int getColumnInt() {
        return columnInt;
    }

    public void setColumnInt(int columnInt) {
        this.columnInt = columnInt;
    }
}
