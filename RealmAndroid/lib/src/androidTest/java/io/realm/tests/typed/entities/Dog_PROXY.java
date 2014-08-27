package io.realm.tests.typed.entities;

import io.realm.tests.typed.entities.*;

public class Dog_PROXY extends Dog 
{
    public static final String implName="Dog";
    public java.lang.String getName()
    {
        return realmGetRow().getString(realmGetRow().getColumnIndex("name"));
    }

    public void setName(java.lang.String value)
    {
        realmGetRow().setString(realmGetRow().getColumnIndex("name"), value);
    }

    public static String[] fieldNames = {"name"};
    public String[] getTableRowNames() {return fieldNames;}

    public static int[] fieldTypes = {2};
    public int[] getTableRowTypes() {return fieldTypes;}

    public String getTableName() {return implName;}
}

