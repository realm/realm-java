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

package io.realm.entities.pojo;

import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.annotations.RealmClass;

@RealmClass
public class RealmModelWithRealmListOfRealmModel implements RealmModel {
    private RealmList<AllTypesRealmModel> columnRealmList;

    public RealmList<AllTypesRealmModel> getColumnRealmList() {
        return columnRealmList;
    }

    public void setColumnRealmList(RealmList<AllTypesRealmModel> columnRealmList) {
        this.columnRealmList = columnRealmList;
    }
}
