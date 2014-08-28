package io.realm.tests.typed.entities;


public class UserRealmProxy extends User 
{
    public static final String implName="User";

    final static int idIndex = 0;

    public int getId()
    {
        return (int)row.getLong(idIndex);
    }

    public void setId(int value)
    {
        row.setLong(idIndex, value);
    }

    final static int emailIndex = 1;

    public java.lang.String getEmail()
    {
        return row.getString(emailIndex);
    }

    public void setEmail(java.lang.String value)
    {
        row.setString(emailIndex, value);
    }

    final static int nameIndex = 2;

    public java.lang.String getName()
    {
        return row.getString(nameIndex);
    }

    public void setName(java.lang.String value)
    {
        row.setString(nameIndex, value);
    }

    private static String[] fieldNames = {"id" ,"email" ,"name"};
    public String[] getTableRowNames() {return fieldNames;}

    private static int[] fieldTypes = {0 ,2 ,2};
    public int[] getTableRowTypes() {return fieldTypes;}

    public String getTableName() {return implName;}
}

