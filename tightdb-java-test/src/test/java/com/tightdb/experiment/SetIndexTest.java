package com.tightdb.experiment;

import org.testng.annotations.Test;

import com.tightdb.ColumnType;
import com.tightdb.Group;
import com.tightdb.SharedGroup;
import com.tightdb.Table;
import com.tightdb.WriteTransaction;

import java.io.IOException;


// TODO: Add asserts!
// TODO: 

public class SetIndexTest {
    @Test
    public void shouldSetIndexWithoutFailing() {

        //Specify table
        Table table = new Table();
        table.addColumn(ColumnType.STRING, "Name");
        table.addColumn(ColumnType.DOUBLE, "GPA");
        table.addColumn(ColumnType.INTEGER, "Age");
        table.addColumn(ColumnType.STRING, "Nickname");
        
        //Add data
        table.add("cc", 2.5, 24, "Pete");
        table.add("dd", 4.5, 35, "Su");
        table.add("bb", 3.6, 22, "Bri");
        table.add("aa", 3.6, 22, "Chris");
        
        //Set index on column with Strings
        table.setIndex(0);
        
        Group group = new Group();
        Table fromGroup = group.getTable("test");
        fromGroup.addColumn(ColumnType.STRING, "Name");
        fromGroup.addColumn(ColumnType.DOUBLE, "GPA");
        fromGroup.addColumn(ColumnType.INTEGER, "Age");
        fromGroup.addColumn(ColumnType.STRING, "Nickname");
        
        //Add data
        fromGroup.add("cc", 2.5, 24, "Pete");
        fromGroup.add("dd", 4.5, 35, "Su");
        fromGroup.add("bb", 3.6, 22, "Bri");
        fromGroup.add("aa", 3.6, 22, "Chris");
        
        table.setIndex(0);

        SharedGroup sharedGroup = null;
        try {
            sharedGroup = new SharedGroup("testGroup.tightdb");
        } catch (IOException e) {
            e.printStackTrace();
        }
        WriteTransaction wt = sharedGroup.beginWrite();
        
        try{
            Table tab = wt.getTable("table1");
            tab.addColumn(ColumnType.STRING, "Name");
            tab.addColumn(ColumnType.DOUBLE, "GPA");
            tab.addColumn(ColumnType.INTEGER, "Age");
            tab.addColumn(ColumnType.STRING, "Nickname");
            
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
