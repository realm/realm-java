package com.tightdb.experiment;

import org.testng.annotations.Test;

import com.tightdb.ColumnType;
import com.tightdb.Group;
import com.tightdb.SharedGroup;
import com.tightdb.Table;
import com.tightdb.WriteTransaction;

public class SetIndexTest {
    @Test
    public void shouldSetIndexWithoutFailing() {

        //Specify table
        Table table = new Table();
        table.addColumn(ColumnType.ColumnTypeString, "Name");
        table.addColumn(ColumnType.ColumnTypeDouble, "GPA");
        table.addColumn(ColumnType.ColumnTypeInt, "Age");
        table.addColumn(ColumnType.ColumnTypeString, "Nickname");
        
        //Add data
        table.add("cc", 2.5, 24, "Pete");
        table.add("dd", 4.5, 35, "Su");
        table.add("bb", 3.6, 22, "Bri");
        table.add("aa", 3.6, 22, "Chris");
        
        //Set index on column with Strings
        table.setIndex(0);
        
        Group group = new Group();
        Table fromGroup = group.getTable("test");
        fromGroup.addColumn(ColumnType.ColumnTypeString, "Name");
        fromGroup.addColumn(ColumnType.ColumnTypeDouble, "GPA");
        fromGroup.addColumn(ColumnType.ColumnTypeInt, "Age");
        fromGroup.addColumn(ColumnType.ColumnTypeString, "Nickname");
        
        //Add data
        fromGroup.add("cc", 2.5, 24, "Pete");
        fromGroup.add("dd", 4.5, 35, "Su");
        fromGroup.add("bb", 3.6, 22, "Bri");
        fromGroup.add("aa", 3.6, 22, "Chris");
        
        table.setIndex(0);
        
        SharedGroup sharedGroup = new SharedGroup("testGroup.tightdb");
        WriteTransaction wt = sharedGroup.beginWrite();
        
        try{
            Table tab = wt.getTable("table1");
            tab.addColumn(ColumnType.ColumnTypeString, "Name");
            tab.addColumn(ColumnType.ColumnTypeDouble, "GPA");
            tab.addColumn(ColumnType.ColumnTypeInt, "Age");
            tab.addColumn(ColumnType.ColumnTypeString, "Nickname");
            
            //Add data
            tab.add("cc", 2.5, 24, "Pete");
            tab.add("dd", 4.5, 35, "Su");
            tab.add("bb", 3.6, 22, "Bri");
            tab.add("aa", 3.6, 22, "Chris");
            
            wt.commit();
            
        } catch(Throwable error){
            wt.rollback();
        }
    }
}
