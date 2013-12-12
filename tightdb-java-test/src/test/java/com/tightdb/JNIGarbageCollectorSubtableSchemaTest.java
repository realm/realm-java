package com.tightdb;

import static org.testng.AssertJUnit.*;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;


// Tables get detached

public class JNIGarbageCollectorSubtableSchemaTest {

    private Table t;


    public void test1(long count){
        t = new Table();

        t.addColumn(ColumnType.TABLE, "table");
        t.addEmptyRow();

        List<TableSchema> tables = new ArrayList<TableSchema>();

        for (long i=0;i<count;i++){
            t.addEmptyRow();
            tables.add(t.getSubtableSchema(0));
        }
        
        t.close();
    }

    public void test2(long count){
        t = new Table();

        t.addColumn(ColumnType.TABLE, "table");
        t.addEmptyRow();

        for (long i=0;i<count;i++){
            t.addEmptyRow();

            TableSchema schema = t.getSubtableSchema(0);
            schema.toString();
        }
        
        t.close();
    }

    public void test3(long count){
        t = new Table();

        t.addColumn(ColumnType.TABLE, "table");
        t.addEmptyRow();

        for (long i=0;i<count;i++){
            t.addEmptyRow();

            TableSchema schema = t.getSubtableSchema(0);
            schema.toString();
            //schema.close();
        }
        
        t.close();
    }


    @Test
    public void testGetSubtable(){

        long count = 1000;
        long loop = 1000;

        for (int i=0;i<loop;i++){
            test1(count);
            test2(count);
            test3(count);
        }
    }
}
