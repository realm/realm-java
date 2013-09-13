package com.tightdb.experiment;


import com.tightdb.Table;
import com.tightdb.TableQuery;
import com.tightdb.TableView;
import org.testng.annotations.Test;

public class MemoryTest {

    @Test
    public void testMemoryManagement() {

        System.out.println("Begin mem test");

        for(int i = 0; i < 10000; i++) {

            Table table = new Table();

            TableQuery query = table.where();

            @SuppressWarnings("unused")
			TableView view = query.findAll();

            // We must make a public debug version of this, to be able to run a memory analyser and get leak detection
            //query.close();

            table.close();

        }

        System.out.println("End mem test");
    }

}

