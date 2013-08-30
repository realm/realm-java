package com.tightdb.examples.quickbenchmark;

import java.util.Date;

import com.tightdb.ColumnType;
import com.tightdb.Table;

public class InsertPerformance {
    
    public static void main(String[] args) {
        
        Table t = new Table();
        
        t.addColumn(ColumnType.ColumnTypeString, "String");
        t.addColumn(ColumnType.ColumnTypeBool, "Bool");
        t.addColumn(ColumnType.ColumnTypeInt, "Long");
        t.addColumn(ColumnType.ColumnTypeDate, "Date");
        
        Long timer = System.currentTimeMillis();
        
        System.out.println("Performance test for inserting values in table:");
        
        for (int i=0;i<50000000;i++){
            
            t.add("String", false, 4000L, new Date());
            
            if (i % 1000000 == 0 && i > 0){
                System.out.println(i + " split time: " +  (System.currentTimeMillis() - timer));
            }
        }
        
        System.out.println("Total time in miliseconds: " + (System.currentTimeMillis() - timer));
    }
}
