package io.realm.typed;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import io.realm.DefineTable;
import org.testng.annotations.Test;

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
