package com.tightdb.typed;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.util.Date;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.tightdb.DefineTable;
import com.tightdb.Group;
import com.tightdb.Mixed;
import com.tightdb.test.TestEmployeeTable;
import com.tightdb.test.TestEmployeeRow;

public class TableDefinitionTest {


    @DefineTable
    class Car {
        String s1;
        int position; // Old generated parameter in generated classes
        int rowIndex; // _rowIndex is used in the generated classes
    }


    @Test
    public void checkTable() {

        // Just make sure table can be created and used
        CarTable myCar = new CarTable();
        myCar.addEmptyRow();
    }
}
