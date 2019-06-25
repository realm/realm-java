package io.realm.entities;


import java.util.Objects;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Nickolay Semendyaev on 09.07.2017.
 */
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
