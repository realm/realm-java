package io.realm.tests.examples.entities;


public class PersonRealmProxy extends Person 
{
    public static final String implName="Person";

    final static int nameIndex = 0;

    public java.lang.String getName()
    {
        return row.getString(nameIndex);
    }

    public void setName(java.lang.String value)
    {
        row.setString(nameIndex, value);
    }

    private static String[] fieldNames = {"name"};
    public String[] getTableRowNames() {return fieldNames;}

    private static int[] fieldTypes = {2};
    public int[] getTableRowTypes() {return fieldTypes;}

    public String getTableName() {return implName;}
}

