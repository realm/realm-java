package io.realm;

public class MigrationAddedFieldInMiddle extends RealmObject {

    private int firstField;
    private boolean secondField;
    private int newField; // Should be added by a migration from field_added_migration.realm
    private String thirdField;

    public int getFirstField() {
        return firstField;
    }

    public void setFirstField(int firstField) {
        this.firstField = firstField;
    }

    public boolean isSecondField() {
        return secondField;
    }

    public void setSecondField(boolean secondField) {
        this.secondField = secondField;
    }

    public String getThirdField() {
        return thirdField;
    }

    public void setThirdField(String thirdField) {
        this.thirdField = thirdField;
    }

    public int getNewField() {
        return newField;
    }

    public void setNewField(int newField) {
        this.newField = newField;
    }
}

