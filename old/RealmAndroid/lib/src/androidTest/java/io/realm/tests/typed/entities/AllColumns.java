package io.realm.tests.typed.entities;
import java.util.Date;

@io.realm.base.RealmClass
public class AllColumns extends io.realm.typed.RealmObject {

    private String columnString;
    private long columnLong;
    private Long columnLLong;
    private float columnFloat;
    private double columnDouble;
    private boolean columnBoolean;
    private Date columnDate;
    private Integer integerNumber;
    private int intNumber;

    //private byte columnBinary;
    //private User columnRealmObject;


    public boolean getColumnBoolean() {
        return columnBoolean;
    }

    public void setColumnBoolean(boolean value) {
        columnBoolean = value;
    }

    public java.util.Date getColumnDate() {
        return columnDate;
    }

    public void setColumnDate(java.util.Date value) {
        columnDate = value;
    }

    public double getColumnDouble() {
        return columnDouble;
    }

    public void setColumnDouble(double value) {
        columnDouble = value;
    }

    public float getColumnFloat() {
        return columnFloat;
    }

    public void setColumnFloat(float value) {
        columnFloat = value;
    }

    public Long getColumnLLong() {
        return columnLLong;
    }

    public void setColumnLLong(Long value) {
        columnLLong = value;
    }

    public long getColumnLong() {
        return columnLong;
    }

    public void setColumnLong(long value) {
        columnLong = value;
    }

    public String getColumnString() {
        return columnString;
    }

    public void setColumnString(String value) {
        columnString = value;
    }

    public int getIntNumber() {
        return intNumber;
    }

    public void setIntNumber(int value) {
        intNumber = value;
    }


    public Integer getIntegerNumber() {
        return integerNumber;
    }

    public void setIntegerNumber(Integer value) {
        integerNumber = value;
    }



}
