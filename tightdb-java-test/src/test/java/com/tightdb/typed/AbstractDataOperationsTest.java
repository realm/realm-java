package com.tightdb.typed;

import static org.testng.AssertJUnit.assertEquals;
import java.util.Date;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.tightdb.Mixed;
import com.tightdb.test.TestEmployeeQuery;
import com.tightdb.test.TestEmployeeRow;
import com.tightdb.test.TestEmployeeTable;
import com.tightdb.test.TestEmployeeView;

public abstract class AbstractDataOperationsTest {

    protected static final String NAME0 = "John";
    protected static final String NAME1 = "Nikolche";
    protected static final String NAME2 = "Johny";
    protected static final String NAME3 = "James";


    protected abstract AbstractTableOrView<TestEmployeeRow, TestEmployeeView, TestEmployeeQuery> getEmployees();

    protected TestEmployeeTable getEmployeeTable() {
        Date myDate = new Date(123456789);
        TestEmployeeTable tbl = new TestEmployeeTable();

        tbl.add(NAME0, "Doe", 10000, true, new byte[] { 1, 2, 3 }, myDate, "Extra!", null);
        tbl.add(NAME2, "B. Good", 10000, true, new byte[] { 1, 2, 3 }, myDate, true, null);
        tbl.insert(1, NAME1, "Mihajlovski", 30000, false, new byte[] { 4, 5 }, myDate, 1234, null);

        Object[][] phones = new Object[][] { { "home", "123-123" }, { "mobile", "456-456" } };
        tbl.add(NAME3, "Bond", 150000, true, new byte[] { 0 }, myDate, "x", phones);
        return tbl;
    }

    @AfterMethod
    public void clear() {
        getEmployees().clear();
    }

    @Test
    public void shouldRetrieveRowsByIndex() {
        assertEquals(NAME0, getEmployees().get(0).getFirstName());
        assertEquals(NAME1, getEmployees().get(1).getFirstName());
        assertEquals(NAME2, getEmployees().get(2).getFirstName());
    }

    @Test
    public void shouldReadCellValue() {
        assertEquals(NAME0, getEmployees().get(0).getFirstName());
    }

    @Test
    public void shouldWriteCellValue() {
        getEmployees().get(0).setFirstName("FOO");
        assertEquals("FOO", getEmployees().get(0).getFirstName());
    }

    @Test
    public void shouldAllowMixedValues() throws IllegalAccessException {
        assertEquals("Extra!", getEmployees().get(0).getExtra().getValue());
        assertEquals("Extra!", getEmployees().get(0).getExtra().getStringValue());

        assertEquals(1234L, getEmployees().get(1).getExtra().getValue());
        assertEquals(1234L, getEmployees().get(1).getExtra().getLongValue());

        assertEquals(true, getEmployees().get(2).getExtra().getValue());
        assertEquals(true, getEmployees().get(2).getExtra().getBooleanValue());

        getEmployees().get(1).setExtra(Mixed.mixedValue("new_value"));
        assertEquals("new_value", getEmployees().get(1).getExtra().getValue());
        assertEquals("new_value", getEmployees().get(1).getExtra()
                .getStringValue());
    }

    @Test
    public void shouldRemoveFirstRow() throws IllegalAccessException {
        // Remove first row
        getEmployees().remove(0);
        assertEquals(NAME1, getEmployees().get(0).getFirstName());
        assertEquals(NAME2, getEmployees().get(1).getFirstName());
        assertEquals(NAME3, getEmployees().get(2).getFirstName());
        assertEquals(3, getEmployees().size());
    }

    @Test
    public void shouldRemoveMiddleRow() throws IllegalAccessException {
        // Remove middle row
        getEmployees().remove(1);
        assertEquals(NAME0, getEmployees().get(0).getFirstName());
        assertEquals(NAME2, getEmployees().get(1).getFirstName());
        assertEquals(NAME3, getEmployees().get(2).getFirstName());
        assertEquals(3, getEmployees().size());
    }

    @Test
    public void shouldRemoveLastRow() throws IllegalAccessException {
        // Remove last row
        getEmployees().remove(3);
        assertEquals(3, getEmployees().size());
        assertEquals(NAME0, getEmployees().get(0).getFirstName());
        assertEquals(NAME1, getEmployees().get(1).getFirstName());
        assertEquals(NAME2, getEmployees().get(2).getFirstName());

        // Remove last row
        getEmployees().removeLast();
        assertEquals(2, getEmployees().size());
        assertEquals(NAME0, getEmployees().get(0).getFirstName());
        assertEquals(NAME1, getEmployees().get(1).getFirstName());
    }

    @Test
    public void shouldExportToJSON() {
        String expected = "[{\"firstName\":\"John\",\"lastName\":\"Doe\",\"salary\":10000,\"driver\":true,\"photo\":\"010203\",\"birthdate\":\"1970-01-02 10:17:36\",\"extra\":\"Extra!\",\"phones\":[]},{\"firstName\":\"Nikolche\",\"lastName\":\"Mihajlovski\",\"salary\":30000,\"driver\":false,\"photo\":\"0405\",\"birthdate\":\"1970-01-02 10:17:36\",\"extra\":1234,\"phones\":[]},{\"firstName\":\"Johny\",\"lastName\":\"B. Good\",\"salary\":10000,\"driver\":true,\"photo\":\"010203\",\"birthdate\":\"1970-01-02 10:17:36\",\"extra\":true,\"phones\":[]},{\"firstName\":\"James\",\"lastName\":\"Bond\",\"salary\":150000,\"driver\":true,\"photo\":\"00\",\"birthdate\":\"1970-01-02 10:17:36\",\"extra\":\"x\",\"phones\":[{\"type\":\"home\",\"number\":\"123-123\"},{\"type\":\"mobile\",\"number\":\"456-456\"}]}]";
        String json = getEmployees().toJson();
        assertEquals(expected, json);
    }
    
    public void shouldPrintData(String header) {
        String expectedTableStr1 = header + ":\n" + 
"    firstName     lastName  salary  driver      photo            birthdate   extra  phones\n"+
"0:  John       Doe           10000    true    3 bytes  1970-01-02 10:17:36  Extra!     [0]\n"+
"1:  Nikolche   Mihajlovski   30000   false    2 bytes  1970-01-02 10:17:36    1234     [0]\n";
        String expectedTableStr2 =
"2:  Johny      B. Good       10000    true    3 bytes  1970-01-02 10:17:36    true     [0]\n"+
"3:  James      Bond         150000    true    1 bytes  1970-01-02 10:17:36  x          [2]\n";
        
        String result = getEmployees().toString(2);
        assertEquals(expectedTableStr1 + "... and 2 more rows (total 4)", result);

        result = getEmployees().toString();
        assertEquals(expectedTableStr1 + expectedTableStr2, result);

        String expectedRowStr = 
                "    firstName     lastName  salary  driver      photo            birthdate   extra  phones\n" +
                "0:  John       Doe           10000    true    3 bytes  1970-01-02 10:17:36  Extra!     [0]\n";
        
        result = getEmployees().first().toString();
        assertEquals(expectedRowStr, result);
        /*
        assertEquals("TestEmployeeTable.birthdate",
                getEmployees().first().getBirthdate().toString());
        assertEquals("TestEmployeeTable.phones",
                getEmployees().first().getPhones().toString());
        */
    }
}
