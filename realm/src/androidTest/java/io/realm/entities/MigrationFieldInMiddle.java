package io.realm.entities;

import io.realm.RealmObject;

public class MigrationFieldInMiddle extends RealmObject {

    private int firstField;
    // private boolean secondField; This field is present in field_removed_migration.realm
    private String thirdField;

    public int getFirstField() {
        return firstField;
    }

    public void setFirstField(int firstField) {
        this.firstField = firstField;
    }

    public String getThirdField() {
        return thirdField;
    }

    public void setThirdField(String thirdField) {
        this.thirdField = thirdField;
    }
}
