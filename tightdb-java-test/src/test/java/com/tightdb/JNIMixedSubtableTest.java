package com.tightdb;

import static org.testng.AssertJUnit.*;

import org.testng.annotations.Test;

public class JNIMixedSubtableTest {

    @Test
    public void shouldCreateSubtableInMixedTypeColumn() {
        Table table = new Table();

        TableSpec tableSpec = new TableSpec();
        tableSpec.addColumn(ColumnType.ColumnTypeInt, "num");
        tableSpec.addColumn(ColumnType.ColumnTypeMixed, "mix");
        TableSpec subspec = tableSpec.addSubtableColumn("subtable");
        subspec.addColumn(ColumnType.ColumnTypeInt, "num");
        table.updateFromSpec(tableSpec);

        // Shouln't work: no Mixed stored yet
        //Mixed m1 = table.getMixed(1, 0);
        //ColumnType mt = table.getMixedType(1,0);
        
        // You can't "getSubTable()" unless there is one. And the addEmptyRow will put in a Mixed(0) as default.
        // You now get an exception instead of crash if you try anyway
        {
            table.addEmptyRow();
            
            boolean gotException = false;
            try {
                @SuppressWarnings("unused")
                Table subtable = table.getSubTable(1, 0);
            } catch (IllegalArgumentException e) {
                gotException = true;
            }
            assertEquals(true, gotException);
            table.removeLast();
        }
        
        long ROW = 0;
        // Add empty row - the simple way
        table.addEmptyRow();
        table.setMixed(1, ROW, new Mixed(ColumnType.ColumnTypeTable));
        assertEquals(1, table.size());
        assertEquals(0, table.getSubTableSize(1, 0));
        
        // Create schema for the one Mixed cell with a subtable
        Table subtable = table.getSubTable(1, ROW);
        TableSpec subspecMixed = subtable.getTableSpec();
        subspecMixed.addColumn(ColumnType.ColumnTypeInt, "num");
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
        tableSpec.addColumn(ColumnType.ColumnTypeInt, "num");
        tableSpec.addColumn(ColumnType.ColumnTypeMixed, "mix");
        table.updateFromSpec(tableSpec);

        table.addEmptyRow();
        table.setMixed(1, 0, new Mixed(ColumnType.ColumnTypeTable));
        
        Table subtable = table.getSubTable(1, 0);
    }

}
