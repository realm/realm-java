package io.realm.tests.typed.entities;

import io.realm.tests.typed.entities.*;

public class User_PROXY extends User 
{
    public static final String implName="User";
    public int getId()
    {
        return (int)realmGetRow().getLong(realmGetRow().getColumnIndex("id"));
    }

    public void setId(int value)
    {
        realmGetRow().setLong(realmGetRow().getColumnIndex("id"), value);
    }

    public java.lang.String getEmail()
    {
        return realmGetRow().getString(realmGetRow().getColumnIndex("email"));
    }

    public void setEmail(java.lang.String value)
    {
        realmGetRow().setString(realmGetRow().getColumnIndex("email"), value);
    }

    public java.lang.String getName()
    {
        return realmGetRow().getString(realmGetRow().getColumnIndex("name"));
    }

    public void setName(java.lang.String value)
    {
        realmGetRow().setString(realmGetRow().getColumnIndex("name"), value);
    }

    public static String[] fieldNames = {"id" ,"email" ,"name"};
    public String[] getTableRowNames() {return fieldNames;}

    public static int[] fieldTypes = {0 ,2 ,2};
    public int[] getTableRowTypes() {return fieldTypes;}

    public String getTableName() {return implName;}
}

