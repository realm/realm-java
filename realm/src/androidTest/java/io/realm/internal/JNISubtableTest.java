package io.realm.internal;

import junit.framework.TestCase;

public class JNISubtableTest extends TestCase {

    public void testShouldSynchronizeNestedTables() throws Throwable {
        Group group = new Group();
        Table table = group.getTable("emp");

        TableSpec tableSpec = new TableSpec();
        tableSpec.addColumn(ColumnType.STRING, "name");

        TableSpec subspec = tableSpec.addSubtableColumn("sub");
        subspec.addColumn(ColumnType.INTEGER, "num");

        table.updateFromSpec(tableSpec);

        table.add("Foo", null);
        assertEquals(1, table.size());

        Table subtable1 = table.getSubtable(1, 0);
        subtable1.add(123);
        assertEquals(1, subtable1.size());
        subtable1.close();

        Table subtable2 = table.getSubtable(1, 0);
        assertEquals(1, subtable2.size());
        assertEquals(123, subtable2.getLong(0, 0));

        table.clear();
    }

    public void testShouldInsertNestedTablesNested() {
        Group group = new Group();
        Table table = group.getTable("emp");

        // Define table
        TableSpec tableSpec = new TableSpec();
        tableSpec.addColumn(ColumnType.STRING, "name");

        TableSpec subspec = tableSpec.addSubtableColumn("sub");
        subspec.addColumn(ColumnType.INTEGER, "num");

        tableSpec.addColumn(ColumnType.INTEGER, "Int");
        table.updateFromSpec(tableSpec);

        // Insert values
        table.add("Foo", null, 123456);
        table.getSubtable(1, 0).add(123);
        assertEquals(1, table.getSubtable(1, 0).size());
        assertEquals(123, table.getSubtable(1, 0).getLong(0, 0));

        assertEquals(1, table.size());
    }

    public void testShouldReturnSubtableIfNullIsInsertedAsSubtable() {
        Group group = new Group();
        Table table = group.getTable("emp");

        table.addColumn(ColumnType.STRING, "string");
        long subtableColIndex = table.addColumn(ColumnType.TABLE, "table");

        table.add("val", null);
        assertEquals(0, table.getSubtable(subtableColIndex, 0).getColumnCount());
    }

    public void testGetSubtableOutOfRange() {
        Group group = new Group();
        Table table = group.getTable("emp");

        table.addColumn(ColumnType.TABLE, "table");

        // No rows added
        try {
            table.getSubtable(0, 0);
            fail("rowIndex > available rows.");
        } catch (ArrayIndexOutOfBoundsException e) {
            assertNotNull(e);
        }

        try {
            table.getSubtable(1, 0);
            fail("columnIndex > available columns.");
        } catch (ArrayIndexOutOfBoundsException e) {
            assertNotNull(e);
        }

        table.addEmptyRow();

        try {
            table.getSubtable(1, 0);
            fail("columnIndex > available columns.");
        } catch (ArrayIndexOutOfBoundsException e) {
            assertNotNull(e);
        }
    }

    public void testSubtableSort() {
        Group group = new Group();
        Table table = group.getTable("emp");

        table.addColumn(ColumnType.TABLE, "table");
        TableSchema subSchema = table.getSubtableSchema(0);
        long subtableIntColIndex = subSchema.addColumn(ColumnType.INTEGER, "int col");
        long subtableStringColIndex = subSchema.addColumn(ColumnType.STRING, "string col");

        table.addEmptyRow();

        Table subtable = table.getSubtable(0, 0);
        subtable.add(10, "s");
        subtable.add(100, "ss");
        subtable.add(1000, "sss");

        TableView subView = subtable.where().findAll();
        subView.sort(subtableIntColIndex);

        assertEquals(10, subView.getLong(0, 0));
        assertEquals(100, subView.getLong(0, 1));
        assertEquals(1000, subView.getLong(0, 2));
    }

    public void testAddColumnsToSubtables() {

        // Table definition
        Table persons = new Table();
        persons.addColumn(ColumnType.STRING, "name");
        persons.addColumn(ColumnType.STRING, "email");
        persons.addColumn(ColumnType.TABLE, "addresses");

        // Add a subtable
        TableSchema addresses = persons.getSubtableSchema(2);
        addresses.addColumn(ColumnType.STRING, "street");
        addresses.addColumn(ColumnType.INTEGER, "zipcode");
        addresses.addColumn(ColumnType.TABLE, "phone_numbers");

        TableSchema phone_numbers = addresses.getSubtableSchema(2);
        phone_numbers.addColumn(ColumnType.INTEGER, "number");

        // Inserting data
        persons.add("Mr X", "xx@xxxx.com", new Object[][] {{"X Street", 1234, new Object[][] {{12345678}}}});

        // Assertions
        assertEquals(persons.getColumnName(2), "addresses");
        assertEquals(persons.getSubtable(2, 0).getColumnName(2), "phone_numbers");
        assertEquals(persons.getSubtable(2, 0).getSubtable(2, 0).getColumnName(0), "number");

        assertEquals(persons.getString(1, 0), "xx@xxxx.com");
        assertEquals(persons.getSubtable(2, 0).getString(0, 0), "X Street");
        assertEquals(persons.getSubtable(2, 0).getSubtable(2, 0).getLong(0, 0), 12345678);
    }

    public void testSubtableAddColumnsCheckNames() {

        // Table definition
        Table persons = new Table();

        persons.addColumn(ColumnType.TABLE, "sub");

        TableSchema addresses = persons.getSubtableSchema(0);
        try {
            addresses.addColumn(ColumnType.STRING, "I am 64 chracters...............................................");
            fail("Only 63 chracters supported");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }

        addresses.addColumn(ColumnType.STRING, "I am 63 chracters..............................................");
    }

    public void testRemoveColumnFromSubtable() {

        // Table definition
        Table persons = new Table();
        persons.addColumn(ColumnType.STRING, "name");
        persons.addColumn(ColumnType.STRING, "email");
        persons.addColumn(ColumnType.TABLE, "addresses");

        TableSchema addresses = persons.getSubtableSchema(2);
        addresses.addColumn(ColumnType.STRING, "street");
        addresses.addColumn(ColumnType.INTEGER, "zipcode");
        addresses.addColumn(ColumnType.TABLE, "phone_numbers");

        TableSchema phone_numbers = addresses.getSubtableSchema(2);
        phone_numbers.addColumn(ColumnType.INTEGER, "number");

        // Inserting data
        persons.add("Mr X", "xx@xxxx.com", new Object[][] {{"X Street", 1234, new Object[][] {{12345678}}}});

        // Assertions
        assertEquals(persons.getSubtable(2, 0).getColumnCount(), 3);
        addresses.removeColumn(1);
        assertEquals(persons.getSubtable(2, 0).getColumnCount(), 2);
    }

    public void testRenameColumnInSubtable() {

        // Table definition
        Table persons = new Table();
        persons.addColumn(ColumnType.STRING, "name");
        persons.addColumn(ColumnType.STRING, "email");
        persons.addColumn(ColumnType.TABLE, "addresses");

        // Define subtable
        TableSchema addresses = persons.getSubtableSchema(2);
        addresses.addColumn(ColumnType.STRING, "street");
        addresses.addColumn(ColumnType.INTEGER, "zipcode");
        addresses.addColumn(ColumnType.TABLE, "phone_numbers");

        TableSchema phone_numbers = addresses.getSubtableSchema(2);
        phone_numbers.addColumn(ColumnType.INTEGER, "number");

        // Inserting data
        persons.add("Mr X", "xx@xxxx.com", new Object[][] {{"X Street", 1234, new Object[][] {{12345678}}}});

        // Assertions
        assertEquals("zipcode", persons.getSubtable(2, 0).getColumnName(1));
        addresses.renameColumn(1, "zip");
        assertEquals("zip", persons.getSubtable(2, 0).getColumnName(1));
    }

    public void testShouldThrowOnGetSubtableDefinitionFromSubtable() {
        // Table definition
        Table persons = new Table();
        persons.addColumn(ColumnType.STRING, "name");
        persons.addColumn(ColumnType.STRING, "email");
        persons.addColumn(ColumnType.TABLE, "addresses");

        TableSchema addresses = persons.getSubtableSchema(2);
        addresses.addColumn(ColumnType.STRING, "street");
        addresses.addColumn(ColumnType.INTEGER, "zipcode");
        addresses.addColumn(ColumnType.TABLE, "phone_numbers");

        TableSchema phone_numbers = addresses.getSubtableSchema(2);
        phone_numbers.addColumn(ColumnType.INTEGER, "number");

        // Inserting data
        persons.add("Mr X", "xx@xxxx.com", new Object[][] {{"X Street", 1234, new Object[][] {{12345678}}}});

        try {
            // Should throw
            persons.getSubtable(2, 0).addColumn(ColumnType.INTEGER, "i");
            fail("expected exception.");
        } catch (UnsupportedOperationException e) {
            assertNotNull(e);
        }

        try {
            // Should throw
            persons.getSubtable(2, 0).getSubtableSchema(2);
            fail("expected exception.");
        } catch (UnsupportedOperationException e) {
            assertNotNull(e);
        }

    }

    // TODO: try on mixed columns - it should work there
}
