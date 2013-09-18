package com.tightdb.experiment;


import com.tightdb.ColumnType;
import com.tightdb.Table;
import com.tightdb.TableQuery;
import com.tightdb.TableView;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;


public class MemoryLeakTest {

    @Test
    public void testMemoryManagement() {

        System.out.println("Begin mem test");

        for (int i = 0; i < 1/*00000*/; i++) {

            Table table = new Table();
            table.addColumn(ColumnType.ColumnTypeInt, "myint");
            table.add(i);
            if (true) {
            	TableQuery query = table.where();
            
            	if (false) {
            		TableView view = query.findAll();
            		assertEquals(i, table.getLong(0,0) );
            		view.private_debug_close();
            	}
            	
                query.private_debug_close();
            }
            table.private_debug_close();

        }
        System.out.println("End mem test");
    }
}

//TODO: Prøv i C++
