package io.realm.typed.entities;

import java.util.Date;

import io.realm.typed.RealmObject;

public class AllColumns extends RealmObject {

    private String ColumnString;
    private long ColumnLong;
    private float ColumnFloat;
    private double ColumnDouble;
    private boolean ColumnBoolean;
    private Date ColumnDate;
    private byte[] ColumnBinary;
    private User ColumnRealmObject;


    public String getColumnString() {
        return ColumnString;
    }

    public void setColumnString(String columnString) {
        ColumnString = columnString;
    }

    public long getColumnLong() {
        return ColumnLong;
    }

    public void setColumnLong(long columnLong) {
        ColumnLong = columnLong;
    }

    public float getColumnFloat() {
        return ColumnFloat;
    }

    public void setColumnFloat(float columnFloat) {
        ColumnFloat = columnFloat;
    }

    public double getColumnDouble() {
        return ColumnDouble;
    }

    public void setColumnDouble(double columnDouble) {
        ColumnDouble = columnDouble;
    }

    public boolean isColumnBoolean() {
        return ColumnBoolean;
    }

    public void setColumnBoolean(boolean columnBoolean) {
        ColumnBoolean = columnBoolean;
    }

    public Date getColumnDate() {
        return ColumnDate;
    }

    public void setColumnDate(Date columnDate) {
        ColumnDate = columnDate;
    }

    public byte[] getColumnBinary() {
        return ColumnBinary;
    }

    public void setColumnBinary(byte[] columnBinary) {
        ColumnBinary = columnBinary;
    }

    public User getColumnRealmObject() {
        return ColumnRealmObject;
    }

    public void setColumnRealmObject(User columnRealmObject) {
        ColumnRealmObject = columnRealmObject;
    }
}
