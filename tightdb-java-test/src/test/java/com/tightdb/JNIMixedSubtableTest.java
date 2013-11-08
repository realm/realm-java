package com.tightdb;

import static org.testng.AssertJUnit.*;

import java.util.Date;

import org.testng.annotations.Test;

public class JNIMixedSubtableTest {
    
    
    @Test
    public void getSubtableFromMixedColumnTest() {
        Table table = new Table();

        table.addColumn(ColumnType.INTEGER, "num");
        table.addColumn(ColumnType.MIXED, "mix");
        
        // No rows added yet
        try { Table subtable = table.getSubTable(1, 0); fail("No rows added, index out of bounds"); } catch (ArrayIndexOutOfBoundsException e) { }

        // addEmptyRow() will put Mixed(0) as default value into the mixed column.
        table.addEmptyRow();
        // Getting a subtable on a mixed with a 0 int value should not work
        try { Table subtable = table.getSubTable(1, 0); fail("Mixed contains an int, not a subtable"); } catch (IllegalArgumentException e) { }
        
        // Now we set the Mixed value to a binary
        table.setMixed(1, 0, new Mixed(new byte[] {1,2,3}));
        // Getting a subtable on a mixed with a date value should not work
        try { Table subtable = table.getSubTable(1, 0); fail("Mixed contains an binary, not a subtable"); } catch (IllegalArgumentException e) { }
        
        // Now we set the Mixed value to a bool
        table.setMixed(1, 0, new Mixed(true));
        // Getting a subtable on a mixed with a String value should not work
        try { Table subtable = table.getSubTable(1, 0); fail("Mixed contains a bool, not a subtable"); } catch (IllegalArgumentException e) { }
        
        // Now we set the Mixed value to a date
        table.setMixed(1, 0, new Mixed(new Date()));
        // Getting a subtable on a mixed with a date value should not work
        try { Table subtable = table.getSubTable(1, 0); fail("Mixed contains a date, not a subtable"); } catch (IllegalArgumentException e) { }
        
        // Now we set the Mixed value to a double
        table.setMixed(1, 0, new Mixed(3.0d));
        // Getting a subtable on a mixed with a date value should not work
        try { Table subtable = table.getSubTable(1, 0); fail("Mixed contains a double, not a subtable"); } catch (IllegalArgumentException e) { }
        
        // Now we set the Mixed value to a float
        table.setMixed(1, 0, new Mixed(3.0f));
        // Getting a subtable on a mixed with a date value should not work
        try { Table subtable = table.getSubTable(1, 0); fail("Mixed contains a float, not a subtable"); } catch (IllegalArgumentException e) { }
        
        // Now we set the Mixed value to a int
        table.setMixed(1, 0, new Mixed(300));
        // Getting a subtable on a mixed with a date value should not work
        try { Table subtable = table.getSubTable(1, 0); fail("Mixed contains an int, not a subtable"); } catch (IllegalArgumentException e) { }
        
        // Now we set the Mixed value to a String
        table.setMixed(1, 0, new Mixed("s"));
        // Getting a subtable on a mixed with a String value should not work
        try { Table subtable = table.getSubTable(1, 0); fail("Mixed contains a String, not a subtable"); } catch (IllegalArgumentException e) { }

        // Now we specifically set the Mixed value to a subtable
        table.setMixed(1, 0, new Mixed(ColumnType.TABLE));
        // Getting a subtable on the mixed column is now allowed
        Table subtable = table.getSubTable(1, 0);
    }

    // Test uses TableSpec..
    @Test
    public void shouldCreateSubtableInMixedTypeColumn() {
        Table table = new Table();

        TableSpec tableSpec = new TableSpec();
        tableSpec.addColumn(ColumnType.INTEGER, "num");
        tableSpec.addColumn(ColumnType.MIXED, "mix");
        TableSpec subspec = tableSpec.addSubtableColumn("subtable");
        subspec.addColumn(ColumnType.INTEGER, "num");
        table.updateFromSpec(tableSpec);

        // Shouln't work: no Mixed stored yet
        //Mixed m1 = table.getMixed(1, 0);
        //ColumnType mt = table.getMixedType(1,0);

        // You can't "getSubTable()" unless there is one. And the addEmptyRow will put in a Mixed(0) as default.
        // You now get an exception instead of crash if you try anyway
        {
            table.addEmptyRow();

            try { Table subtable = table.getSubTable(1, 0); fail("Mixed contains 0, not a subtable");  } catch (IllegalArgumentException e) {}
            table.removeLast();
        }

        long ROW = 0;
        // Add empty row - the simple way
        table.addEmptyRow();
        table.setMixed(1, ROW, new Mixed(ColumnType.TABLE));
        assertEquals(1, table.size());
        assertEquals(0, table.getSubTableSize(1, 0));

        // Create schema for the one Mixed cell with a subtable
        Table subtable = table.getSubTable(1, ROW);
        TableSpec subspecMixed = subtable.getTableSpec();
        subspecMixed.addColumn(ColumnType.INTEGER, "num");
        subtable.updateFromSpec(subspecMixed);

        // Insert value in the Mixed subtable
        subtable.add(27);
        subtable.add(273);
        assertEquals(2, subtable.size());
        assertEquals(2, table.getSubTableSize(1, ROW));
        assertEquals(27, subtable.getLong(0, ROW));
        assertEquals(273, subtable.getLong(0, ROW+1));
    }

    @SuppressWarnings("unused")
    @Test
    public void shouldCreateSubtableInMixedTypeColumn2() {
        Table table = new Table();

        TableSpec tableSpec = new TableSpec();
        tableSpec.addColumn(ColumnType.INTEGER, "num");
        tableSpec.addColumn(ColumnType.MIXED, "mix");
        table.updateFromSpec(tableSpec);

        table.addEmptyRow();
        table.setMixed(1, 0, new Mixed(ColumnType.TABLE));

        Table subtable = table.getSubTable(1, 0);
    }

}
