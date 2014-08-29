package io.realm.typed;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import io.realm.test.TestEmployeeQuery;
import io.realm.test.TestEmployeeRow;
import io.realm.test.TestEmployeeTable;
import io.realm.test.TestEmployeeView;
import io.realm.test.TestPhoneTable;

@Test
public class TableDataOperationsTest extends AbstractDataOperationsTest {

    private TestEmployeeTable employees;

    @Override
    protected AbstractTableOrView<TestEmployeeRow, TestEmployeeView, TestEmployeeQuery> getEmployees() {
        return employees;
    }

    @BeforeMethod
    public void init() {
        employees = getEmployeeTable();
    }

    private void setAndTestValue(long val) {
        employees.get(1).setSalary(val);
        assertEquals(val, employees.get(1).getSalary());
    }

    @Test
    public void shouldPrint() {
        super.shouldPrintData("TestEmployeeTable");
    }

    @Test
    public void shouldStoreValues() {
        setAndTestValue(Integer.MAX_VALUE);
        setAndTestValue(Integer.MIN_VALUE);

        setAndTestValue(Long.MAX_VALUE);
        setAndTestValue(Long.MIN_VALUE);
    }

    @Test
    public void shouldConstructSubtableInline() {
        TestPhoneTable phones = employees.last().getPhones();
        assertEquals(2, phones.size());

        assertEquals("home", phones.get(0).getType());
        assertEquals("123-123", phones.get(0).getNumber());

        assertEquals("mobile", phones.get(1).getType());
        assertEquals("456-456", phones.get(1).getNumber());
    }


    @Test
    public void shouldDeleteAllButLast() {
        employees.moveLastOver(2);
        employees.moveLastOver(1);
        employees.moveLastOver(0);
        assertEquals("Bond", employees.get(0).getLastName());
        TestPhoneTable phones2 = employees.last().getPhones();
        assertEquals(2, phones2.size());
        assertEquals(1, employees.size());

        try {
            employees.moveLastOver(0);
            // should not allow the last to be removed
            assert(false);
        } catch (Exception e) {
        }

    }
}
