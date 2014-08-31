package io.realm.tests.typed.entities;


public class AllColumnsRealmProxy extends AllColumns 
{
    public static final String implName="AllColumns";

    final static int columnDateIndex = 0;

    public java.util.Date getColumnDate()
    {
        return row.getDate(columnDateIndex);
    }

    public void setColumnDate(java.util.Date value)
    {
        row.setDate(columnDateIndex, value);
    }

    final static int columnDoubleIndex = 1;

    public double getColumnDouble()
    {
        return row.getDouble(columnDoubleIndex);
    }

    public void setColumnDouble(double value)
    {
        row.setDouble(columnDoubleIndex, value);
    }

    final static int columnBooleanIndex = 2;

    public boolean getColumnBoolean()
    {
        return row.getBoolean(columnBooleanIndex);
    }

    public void setColumnBoolean(boolean value)
    {
        row.setBoolean(columnBooleanIndex, value);
    }

    final static int columnStringIndex = 3;

    public java.lang.String getColumnString()
    {
        return row.getString(columnStringIndex);
    }

    public void setColumnString(java.lang.String value)
    {
        row.setString(columnStringIndex, value);
    }

    final static int columnFloatIndex = 4;

    public float getColumnFloat()
    {
        return row.getFloat(columnFloatIndex);
    }

    public void setColumnFloat(float value)
    {
        row.setFloat(columnFloatIndex, value);
    }

    private static String[] fieldNames = {"columnDate" ,"columnDouble" ,"columnBoolean" ,"columnString" ,"columnFloat"};
    public String[] getTableRowNames() {return fieldNames;}

    private static int[] fieldTypes = {7 ,10 ,1 ,2 ,9};
    public int[] getTableRowTypes() {return fieldTypes;}

    public String getTableName() {return implName;}
}

