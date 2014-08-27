package io.realm.tests.typed.entities;

import io.realm.tests.typed.entities.*;

public class AllColumns_PROXY extends AllColumns 
{
    public static final String implName="AllColumns";
    public java.util.Date getColumnDate()
    {
        return realmGetRow().getDate(realmGetRow().getColumnIndex("columnDate"));
    }

    public void setColumnDate(java.util.Date value)
    {
        realmGetRow().setDate(realmGetRow().getColumnIndex("columnDate"), value);
    }

    public double getColumnDouble()
    {
        return realmGetRow().getDouble(realmGetRow().getColumnIndex("columnDouble"));
    }

    public void setColumnDouble(double value)
    {
        realmGetRow().setDouble(realmGetRow().getColumnIndex("columnDouble"), value);
    }

    public long getColumnLong()
    {
        return realmGetRow().getLong(realmGetRow().getColumnIndex("columnLong"));
    }

    public void setColumnLong(long value)
    {
        realmGetRow().setLong(realmGetRow().getColumnIndex("columnLong"), value);
    }

    public boolean getColumnBoolean()
    {
        return realmGetRow().getBoolean(realmGetRow().getColumnIndex("columnBoolean"));
    }

    public void setColumnBoolean(boolean value)
    {
        realmGetRow().setBoolean(realmGetRow().getColumnIndex("columnBoolean"), value);
    }

    public java.lang.String getColumnString()
    {
        return realmGetRow().getString(realmGetRow().getColumnIndex("columnString"));
    }

    public void setColumnString(java.lang.String value)
    {
        realmGetRow().setString(realmGetRow().getColumnIndex("columnString"), value);
    }

    public float getColumnFloat()
    {
        return realmGetRow().getFloat(realmGetRow().getColumnIndex("columnFloat"));
    }

    public void setColumnFloat(float value)
    {
        realmGetRow().setFloat(realmGetRow().getColumnIndex("columnFloat"), value);
    }

    public static String[] fieldNames = {"columnDate" ,"columnDouble" ,"columnLong" ,"columnBoolean" ,"columnString" ,"columnFloat"};
    public String[] getTableRowNames() {return fieldNames;}

    public static int[] fieldTypes = {7 ,10 ,0 ,1 ,2 ,9};
    public int[] getTableRowTypes() {return fieldTypes;}

    public String getTableName() {return implName;}
}

