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

import java.util.Objects;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Parent6522 extends RealmObject {
    @PrimaryKey
    private Integer guid;
    private RealmList<Child6522> reportValues;

    public Parent6522() {
    }

    public Integer getGuid() {
        return guid;
    }

    public void setGuid(Integer guid) {
        this.guid = guid;
    }

    public RealmList<Child6522> getReportValues() {
        return reportValues;
    }

    public void setReportValues(RealmList<Child6522> reportValues) {
        this.reportValues = reportValues;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Parent6522 report = (Parent6522) o;
        return Objects.equals(guid, report.guid) &&
                Objects.equals(reportValues, report.reportValues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(guid, reportValues);
    }
}