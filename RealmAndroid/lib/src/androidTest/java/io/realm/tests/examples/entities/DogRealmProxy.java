package io.realm.tests.examples.entities;


public class DogRealmProxy extends Dog 
{
    public static final String implName="Dog";

    final static int nameIndex = 0;

    public java.lang.String getName()
    {
        return row.getString(nameIndex);
    }

    public void setName(java.lang.String value)
    {
        row.setString(nameIndex, value);
    }

    final static int ageIndex = 1;

    public int getAge()
    {
        return (int)row.getLong(ageIndex);
    }

    public void setAge(int value)
    {
        row.setLong(ageIndex, value);
    }

    private static String[] fieldNames = {"name" ,"age"};
    public String[] getTableRowNames() {return fieldNames;}

    private static int[] fieldTypes = {2 ,0};
    public int[] getTableRowTypes() {return fieldTypes;}

    public String getTableName() {return implName;}
}

