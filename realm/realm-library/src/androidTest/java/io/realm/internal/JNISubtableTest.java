/*
 * Copyright 2015 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.realm.internal;

import junit.framework.TestCase;

import io.realm.RealmFieldType;

public class JNISubtableTest extends TestCase {

    public void testShouldSynchronizeNestedTables() throws Throwable {
        Group group = new Group();
        Table table = group.getTable("emp");

        TableSpec tableSpec = new TableSpec();
        tableSpec.addColumn(RealmFieldType.STRING, "name");

        TableSpec subspec = tableSpec.addSubtableColumn("sub");
        subspec.addColumn(RealmFieldType.INTEGER, "num");

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
        tableSpec.addColumn(RealmFieldType.STRING, "name");

        TableSpec subspec = tableSpec.addSubtableColumn("sub");
        subspec.addColumn(RealmFieldType.INTEGER, "num");

        tableSpec.addColumn(RealmFieldType.INTEGER, "Int");
        table.updateFromSpec(tableSpec);

        // Insert values
        table.add("Foo", null, 123456);
        table.getSubtable(1, 0).add(123);
        assertEquals(1, table.getSubtable(1, 0).size());
        assertEquals(123, table.getSubtable(1, 0).getLong(0,0));

        assertEquals(1, table.size());
    }

    public void testShouldReturnSubtableIfNullIsInsertedAsSubtable() {
        Group group = new Group();
        Table table = group.getTable("emp");

        table.addColumn(RealmFieldType.STRING, "string");
        long subtableColIndex = table.addColumn(RealmFieldType.UNSUPPORTED_TABLE, "table");

        table.add("val", null);
        assertEquals(0,  table.getSubtable(subtableColIndex, 0).getColumnCount());
    }

    public void testGetSubtableOutOfRange() {
        Group group = new Group();
        Table table = group.getTable("emp");

        table.addColumn(RealmFieldType.UNSUPPORTED_TABLE, "table");

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

        table.addColumn(RealmFieldType.UNSUPPORTED_TABLE, "table");
        TableSchema subSchema = table.getSubtableSchema(0);
        long subtableIntColIndex = subSchema.addColumn(RealmFieldType.INTEGER, "int col");
        long subtableStringColIndex = subSchema.addColumn(RealmFieldType.STRING, "string col");

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
        persons.addColumn(RealmFieldType.STRING, "name");
        persons.addColumn(RealmFieldType.STRING, "email");
        persons.addColumn(RealmFieldType.UNSUPPORTED_TABLE, "addresses");

        // Add a subtable
        TableSchema addresses = persons.getSubtableSchema(2);
        addresses.addColumn(RealmFieldType.STRING, "street");
        addresses.addColumn(RealmFieldType.INTEGER, "zipcode");
        addresses.addColumn(RealmFieldType.UNSUPPORTED_TABLE, "phone_numbers");

        TableSchema phone_numbers = addresses.getSubtableSchema(2);
        phone_numbers.addColumn(RealmFieldType.INTEGER, "number");

        // Inserting data
        persons.add("Mr X", "xx@xxxx.com", new Object[][] {{ "X Street", 1234, new Object[][] {{ 12345678 }} }});

        // Assertions
        assertEquals(persons.getColumnName(2), "addresses");
        assertEquals(persons.getSubtable(2,0).getColumnName(2), "phone_numbers");
        assertEquals(persons.getSubtable(2,0).getSubtable(2,0).getColumnName(0), "number");

        assertEquals(persons.getString(1,0), "xx@xxxx.com");
        assertEquals(persons.getSubtable(2,0).getString(0,0), "X Street");
        assertEquals(persons.getSubtable(2,0).getSubtable(2,0).getLong(0,0), 12345678);
    }


    public void testSubtableAddColumnsCheckNames() {

        // Table definition
        Table persons = new Table();

        persons.addColumn(RealmFieldType.UNSUPPORTED_TABLE, "sub");

        TableSchema addresses = persons.getSubtableSchema(0);
        try {
            addresses.addColumn(RealmFieldType.STRING, "I am 64 characters..............................................");
            fail("Only 63 characters supported"); }
        catch (IllegalArgumentException e) {
            assertNotNull(e);
        }

        addresses.addColumn(RealmFieldType.STRING, "I am 63 characters.............................................");
    }

    public void testRemoveColumnFromSubtable() {

        // Table definition
        Table persons = new Table();
        persons.addColumn(RealmFieldType.STRING, "name");
        persons.addColumn(RealmFieldType.STRING, "email");
        persons.addColumn(RealmFieldType.UNSUPPORTED_TABLE, "addresses");

        TableSchema addresses = persons.getSubtableSchema(2);
        addresses.addColumn(RealmFieldType.STRING, "street");
        addresses.addColumn(RealmFieldType.INTEGER, "zipcode");
        addresses.addColumn(RealmFieldType.UNSUPPORTED_TABLE, "phone_numbers");

        TableSchema phone_numbers = addresses.getSubtableSchema(2);
        phone_numbers.addColumn(RealmFieldType.INTEGER, "number");

        // Inserting data
        persons.add("Mr X", "xx@xxxx.com", new Object[][] {{ "X Street", 1234, new Object[][] {{ 12345678 }} }});

        // Assertions
        assertEquals(persons.getSubtable(2,0).getColumnCount(), 3);
        addresses.removeColumn(1);
        assertEquals(persons.getSubtable(2,0).getColumnCount(), 2);
    }

    public void testRenameColumnInSubtable() {

        // Table definition
        Table persons = new Table();
        persons.addColumn(RealmFieldType.STRING, "name");
        persons.addColumn(RealmFieldType.STRING, "email");
        persons.addColumn(RealmFieldType.UNSUPPORTED_TABLE, "addresses");

        // Define subtable
        TableSchema addresses = persons.getSubtableSchema(2);
        addresses.addColumn(RealmFieldType.STRING, "street");
        addresses.addColumn(RealmFieldType.INTEGER, "zipcode");
        addresses.addColumn(RealmFieldType.UNSUPPORTED_TABLE, "phone_numbers");

        TableSchema phone_numbers = addresses.getSubtableSchema(2);
        phone_numbers.addColumn(RealmFieldType.INTEGER, "number");

        // Inserting data
        persons.add("Mr X", "xx@xxxx.com", new Object[][] {{ "X Street", 1234, new Object[][] {{ 12345678 }} }});

        // Assertions
        assertEquals("zipcode", persons.getSubtable(2,0).getColumnName(1));
        addresses.renameColumn(1, "zip");
        assertEquals("zip", persons.getSubtable(2,0).getColumnName(1));
    }

    public void testShouldThrowOnGetSubtableDefinitionFromSubtable() {
        // Table definition
        Table persons = new Table();
        persons.addColumn(RealmFieldType.STRING, "name");
        persons.addColumn(RealmFieldType.STRING, "email");
        persons.addColumn(RealmFieldType.UNSUPPORTED_TABLE, "addresses");

        TableSchema addresses = persons.getSubtableSchema(2);
        addresses.addColumn(RealmFieldType.STRING, "street");
        addresses.addColumn(RealmFieldType.INTEGER, "zipcode");
        addresses.addColumn(RealmFieldType.UNSUPPORTED_TABLE, "phone_numbers");

        TableSchema phone_numbers = addresses.getSubtableSchema(2);
        phone_numbers.addColumn(RealmFieldType.INTEGER, "number");

        // Inserting data
        persons.add("Mr X", "xx@xxxx.com", new Object[][] {{ "X Street", 1234, new Object[][] {{ 12345678 }} }});

        try {
            // Should throw
            persons.getSubtable(2,0).addColumn(RealmFieldType.INTEGER, "i");
            fail("expected exception.");
        } catch (UnsupportedOperationException e) {
            assertNotNull(e);
        }

        try {
            // Should throw
            persons.getSubtable(2,0).getSubtableSchema(2);
            fail("expected exception.");
        } catch (UnsupportedOperationException e) {
            assertNotNull(e);
        }

    }

    // TODO: try on mixed columns - it should work there
}
