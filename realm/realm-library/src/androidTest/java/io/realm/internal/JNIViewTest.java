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

import android.test.MoreAsserts;

import junit.framework.TestCase;

import java.util.Date;

import io.realm.RealmFieldType;
import io.realm.Sort;
import io.realm.TestHelper;

@SuppressWarnings("deprecation")
public class JNIViewTest extends TestCase {
    Table t;
    Date date1 = new Date(2010-1900,  1,  5);
    Date date2 = new Date(1999-1900, 12,  1);
    Date date3 = new Date(1990-1900, 12, 24);
    Date date4 = new Date(2010-1900,  1,  4);

    @Override
    public void setUp() {
        //Specify table
        t = new Table();
        t.addColumn(RealmFieldType.STRING, "Name");
        t.addColumn(RealmFieldType.BOOLEAN,   "Study");
        t.addColumn(RealmFieldType.INTEGER,    "Age");
        t.addColumn(RealmFieldType.DATE,   "Birthday");

        //Add data
        t.add("cc", true,  24, date1);
        t.add("dd", false, 35, date2);
        t.add("bb", true,  22, date3);
        t.add("aa", false, 22, date4);

        assertEquals(date1, t.getDate(3, 0));
        assertEquals(date2, t.getDate(3, 1));
        assertEquals(date3, t.getDate(3, 2));
        assertEquals(date4, t.getDate(3, 3));
    }

    public void testUnimplementedMethodsShouldFail() {
        //Get a view containing all rows in table since you can only sort views currently.
        TableView view = t.where().findAll();

        try { view.upperBoundLong(0, 0); fail("Not implemented yet"); } catch (RuntimeException e ) { }
        try { view.lowerBoundLong(0, 0); fail("Not implemented yet"); } catch (RuntimeException e ) { }
      //  try { view.lookup("Some String"); fail("Not implemented yet"); } catch (RuntimeException e ) { }
        try { view.count(0, "Some String"); fail("Not implemented yet"); } catch (RuntimeException e ) { }
    }


    public void testShouldSortViewDate() {
        //Get a view containing all rows in table since you can only sort views currently.
        TableView view = t.where().findAll();

        //Sort without specifying the order, should default to ascending.
        view.sort(3);
        assertEquals(date3, view.getDate(3, 0));
        assertEquals(date2, view.getDate(3, 1));
        assertEquals(date4, view.getDate(3, 2));
        assertEquals(date1, view.getDate(3, 3));
        assertEquals("cc", view.getString(0, 3));
    }


    public void testShouldSortViewIntegers() {
        //Get a view containing all rows in table since you can only sort views currently.
        TableView view = t.where().findAll();

        //Sort without specifying the order, should default to ascending.
        view.sort(2);
        assertEquals(22, view.getLong(2, 0));
        assertEquals(22, view.getLong(2, 1));
        assertEquals(24, view.getLong(2, 2));
        assertEquals(35, view.getLong(2, 3));
        assertEquals("dd", view.getString(0, 3));

        //Sort descending - creating a new view
        view.sort(2, Sort.DESCENDING);
        assertEquals(35, view.getLong(2, 0));
        assertEquals(24, view.getLong(2, 1));
        assertEquals(22, view.getLong(2, 2));
        assertEquals(22, view.getLong(2, 3));
        assertEquals("dd", view.getString(0, 0));

        //Sort ascending.
        TableView view2 = t.where().findAll();
        view2.sort(2, Sort.ASCENDING);
        assertEquals(22, view2.getLong(2, 0));
        assertEquals(22, view2.getLong(2, 1));
        assertEquals(24, view2.getLong(2, 2));
        assertEquals(35, view2.getLong(2, 3));
        assertEquals("dd", view2.getString(0, 3));

        // Check that old view is still the same
        assertEquals(35, view.getLong(2, 0));
        assertEquals(24, view.getLong(2, 1));
        assertEquals(22, view.getLong(2, 2));
        assertEquals(22, view.getLong(2, 3));
        assertEquals("dd", view.getString(0, 0));
    }


    public void testSetBinary() {

        Table table = new Table();
        table.addColumn(RealmFieldType.BINARY, "binary");

        byte[] arr1 = new byte[] {1,2,3};
        table.add(new Object[]{arr1});
        MoreAsserts.assertEquals(arr1, table.getBinaryByteArray(0, 0));

        TableView view = table.where().findAll();

        byte[] arr2 = new byte[] {1,2,3, 4, 5};

        view.setBinaryByteArray(0, 0, arr2, false);

        MoreAsserts.assertEquals(arr2, view.getBinaryByteArray(0, 0));
    }

    public void testSortOnNonexistingColumn() {
        TableView view = t.where().findAll();

        try { view.sort(-1); fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { view.sort(-100); fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { view.sort(100); fail("Column is 100, column does not exist"); } catch (ArrayIndexOutOfBoundsException e) { }
    }


    public void testFindFirstNonExisting() {
        Table tt = TestHelper.getTableWithAllColumnTypes();
        tt.add(new byte[]{1,2,3}, true, new Date(1384423149761l), 4.5d, 5.7f, 100, "string");
        TableView v = tt.where().findAll();

        assertEquals(-1, v.findFirstBoolean(1, false));
        //FIXME: enable when find_first_timestamp() is implemented: assertEquals(-1, v.findFirstDate(2, new Date(138442314986l)));
        assertEquals(-1, v.findFirstDouble(3, 1.0d));
        assertEquals(-1, v.findFirstFloat(4, 1.0f));
        assertEquals(-1, v.findFirstLong(5, 50));
    }


    public void testGetValuesFromNonExistingColumn() {
        Table table = TestHelper.getTableWithAllColumnTypes();
        TableView view = table.where().findAll();

        try { view.getBinaryByteArray(-1, 0);   fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { view.getBinaryByteArray(-10, 0);  fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { view.getBinaryByteArray(100, 0);  fail("Column does not exist"); } catch (ArrayIndexOutOfBoundsException e) { }

        try { view.getBoolean(-1, 0);           fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { view.getBoolean(-10, 0);          fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { view.getBoolean(100, 0);          fail("Column does not exist"); } catch (ArrayIndexOutOfBoundsException e) { }

        try { view.getDate(-1, 0);              fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { view.getDate(-10, 0);             fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { view.getDate(100, 0);             fail("Column does not exist"); } catch (ArrayIndexOutOfBoundsException e) { }

        try { view.getDouble(-1, 0);            fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { view.getDouble(-10, 0);           fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { view.getDouble(100, 0);           fail("Column does not exist"); } catch (ArrayIndexOutOfBoundsException e) { }

        try { view.getFloat(-1, 0);             fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { view.getFloat(-10, 0);            fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { view.getFloat(100, 0);            fail("Column does not exist"); } catch (ArrayIndexOutOfBoundsException e) { }

        try { view.getLong(-1, 0);              fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { view.getLong(-10, 0);             fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { view.getLong(100, 0);             fail("Column does not exist"); } catch (ArrayIndexOutOfBoundsException e) { }

        try { view.getString(-1, 0);            fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { view.getString(-10, 0);           fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { view.getString(100, 0);           fail("Column does not exist"); } catch (ArrayIndexOutOfBoundsException e) { }
    }


    public void testGetSourceRow() {
        Table t = new Table();
        t.addColumn(RealmFieldType.STRING, "");
        t.addColumn(RealmFieldType.INTEGER, "");
        t.addColumn(RealmFieldType.BOOLEAN, "");

        t.add("1", 1, true);
        t.add("2", 2, true);
        t.add("3", 3, false);
        t.add("4", 5, false);

        TableView v = t.where().equalTo(new long[]{2}, false).findAll();

        assertEquals(2, v.getSourceRowIndex(0));
        assertEquals(3, v.getSourceRowIndex(1));

        // Out of bound
        try { assertEquals(0, v.getSourceRowIndex(2));      fail("index ot of bounds"); } catch (IndexOutOfBoundsException e) { }
        try { assertEquals(0, v.getSourceRowIndex(100));    fail("index ot of bounds"); } catch (IndexOutOfBoundsException e) { }
        try { assertEquals(0, v.getSourceRowIndex(-1));     fail("index ot of bounds"); } catch (IndexOutOfBoundsException e) { }
        try { assertEquals(0, v.getSourceRowIndex(-100));   fail("index ot of bounds"); } catch (IndexOutOfBoundsException e) { }
    }


    public void testGetSourceRowNoRows() {
        Table t = new Table();
        t.addColumn(RealmFieldType.STRING, "");
        t.addColumn(RealmFieldType.INTEGER, "");
        t.addColumn(RealmFieldType.BOOLEAN, "");
        // No data is added
        TableView v = t.where().findAll();

        // Out of bound
        try { assertEquals(0, v.getSourceRowIndex(0));      fail("index ot of bounds"); } catch (IndexOutOfBoundsException e) { }
        try { assertEquals(0, v.getSourceRowIndex(1));      fail("index ot of bounds"); } catch (IndexOutOfBoundsException e) { }
    }


    public void testGetSourceRowEmptyTable() {
        Table t = new Table();
        // No columns
        TableView v = t.where().findAll();

        // Out of bound
        try { assertEquals(0, v.getSourceRowIndex(0));      fail("index ot of bounds"); } catch (IndexOutOfBoundsException e) { }
        try { assertEquals(0, v.getSourceRowIndex(1));      fail("index ot of bounds"); } catch (IndexOutOfBoundsException e) { }
    }


    public void testShouldSortViewBool() {
        //Get a view containing all rows in table since you can only sort views currently.
        TableView view = t.where().findAll();

        //Sort without specifying the order, should default to ascending.
        view.sort(1);
        assertEquals(false, view.getBoolean(1, 0));
        assertEquals(false, view.getBoolean(1, 1));
        assertEquals(true, view.getBoolean(1, 2));
        assertEquals(true, view.getBoolean(1, 3));
        assertEquals("bb", view.getString(0, 3));
    }

    public void testShouldSearchByColumnValue() {
        Table table = new Table();
        table.addColumn(RealmFieldType.STRING, "name");

        table.add("Foo");
        table.add("Bar");

        TableQuery query = table.where();
        TableView view = query.findAll(0, table.size(), Integer.MAX_VALUE);
        assertEquals(2, view.size());

        view.findAllString(0, "Foo");
    }

    public void testShouldQueryInView() {
        Table table = new Table();
        table.addColumn(RealmFieldType.STRING, "name");

        table.add("A1");
        table.add("B");
        table.add("A2");
        table.add("B");
        table.add("A3");
        table.add("B");
        table.add("A3");

        TableQuery query = table.where();
        TableView view = query.beginsWith(new long[]{0}, "A").findAll(0, table.size(), Table.INFINITE);
        assertEquals(4, view.size());

        TableQuery query2 = table.where();
        TableView view2 = query2.tableview(view).contains(new long[]{0}, "3").findAll();
        assertEquals(2, view2.size());
    }

    public void testGetNonExistingColumn() {
        Table t = new Table();
        t.addColumn(RealmFieldType.INTEGER, "int");
        TableView view = t.where().findAll();
        assertEquals(-1, view.getColumnIndex("non-existing column"));
    }

    public void testGetNullColumn() {
        Table t = new Table();
        t.addColumn(RealmFieldType.INTEGER, "");
        TableView view = t.where().findAll();
        try { view.getColumnIndex(null); fail("Getting null column"); } catch(IllegalArgumentException e) { }
    }


    public void testViewToString() {
        Table t = new Table();
        t.addColumn(RealmFieldType.STRING, "stringCol");
        t.addColumn(RealmFieldType.INTEGER, "intCol");
        t.addColumn(RealmFieldType.BOOLEAN, "boolCol");

        t.add("s1", 1, true);
        t.add("s2", 2, false);

        TableView view = t.where().findAll();

        String expected = "The TableView contains 3 columns: stringCol, intCol, boolCol. And 2 rows.";

        assertEquals(expected, view.toString());
    }

    void accessingViewOk(TableView view)
    {
        view.size();
        view.isEmpty();
        view.getLong(0, 0);
        view.getColumnCount();
        view.getColumnName(0);
        view.getColumnIndex("");
        view.getColumnType(0);
        view.averageLong(0);
        view.maximumLong(0);
        view.minimumLong(0);
        view.sumLong(0);
        view.findAllLong(0, 2);
        view.findFirstLong(0, 2);
        view.where();
        view.toJson();
        view.toString();
    }

    void accessingViewMustThrow(TableView view)
    {
        try { view.size();              assert(false); } catch (IllegalStateException e) {}
        try { view.isEmpty();           assert(false); } catch (IllegalStateException e) {}
        try { view.getLong(0,0);        assert(false); } catch (IllegalStateException e) {}
        try { view.getColumnCount();    assert(false); } catch (IllegalStateException e) {}
        try { view.getColumnName(0);    assert(false); } catch (IllegalStateException e) {}
        try { view.getColumnIndex("");  assert(false); } catch (IllegalStateException e) {}
        try { view.getColumnType(0);    assert(false); } catch (IllegalStateException e) {}
        try { view.averageLong(0);      assert(false); } catch (IllegalStateException e) {}
        try { view.maximumLong(0);      assert(false); } catch (IllegalStateException e) {}
        try { view.minimumLong(0);      assert(false); } catch (IllegalStateException e) {}
        try { view.sumLong(0);          assert(false); } catch (IllegalStateException e) {}
        try { view.findAllLong(0, 2);   assert(false); } catch (IllegalStateException e) {}
        try { view.findFirstLong(0, 2); assert(false); } catch (IllegalStateException e) {}
        try { view.where();             assert(false); } catch (IllegalStateException e) {}
        try { view.toJson();            assert(false); } catch (IllegalStateException e) {}
        try { view.toString();          assert(false); } catch (IllegalStateException e) {}
    }

    public void testViewShouldInvalidate() {
        Table t = new Table();
        t.addColumn(RealmFieldType.INTEGER, "intCol");
        t.add(1);
        t.add(2);
        t.add(3);

        TableView view = t.where().equalTo(new long[]{0}, 2).findAll();
        // access view is ok.
        assertEquals(1, view.size());

        // access view after change in value is ok
        t.setLong(0, 0, 3, false);
        accessingViewOk(view);

        // access view after additions to table must fail
        t.add(4);
        accessingViewMustThrow(view);

        // recreate view to access again
        view = t.where().equalTo(new long[]{0}, 2).findAll();
        accessingViewOk(view);

        // Removing any row in Table should invalidate view
        t.remove(3);
        accessingViewMustThrow(view);
    }

    public void testMaximumDate() {

        Table table = new Table();
        table.addColumn(RealmFieldType.DATE, "date");

        table.add(new Date(0));
        table.add(new Date(10000));
        table.add(new Date(1000));

        TableView view = table.where().findAll();

        assertEquals(new Date(10000), view.maximumDate(0));

    }

    public void testMinimumDate() {

        Table table = new Table();
        table.addColumn(RealmFieldType.DATE, "date");

        table.add(new Date(10000));
        table.add(new Date(0));
        table.add(new Date(1000));

        TableView view = table.where().findAll();

        assertEquals(new Date(0), view.minimumDate(0));

    }
}
