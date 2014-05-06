package com.realm.typed;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Iterator;

import org.testng.annotations.Test;

import com.realm.test.EmployeesFixture;
import com.realm.test.TestEmployeeRow;
import com.realm.test.TestPhoneRow;

public class TableCursorColumnTest extends AbstractTest {

    @Test
    public void shouldProvideConvenienceMethods() {
        TestEmployeeRow employee = employees.last();

        // 2 predefined records in the "phones" sub-table should exist
        assertEquals(2, employee.getPhones().size());
        assertFalse(employee.getPhones().isEmpty());

        // make sure the 2 predefined records in the "phones" sub-table match
        checkCursorValues(EmployeesFixture.PHONES[2][0],
                employee.getPhones().first());
        checkCursorValues(EmployeesFixture.PHONES[2][0], employee.getPhones().get(0));
        checkCursorValues(EmployeesFixture.PHONES[2][1], employee.getPhones().last());
        checkCursorValues(EmployeesFixture.PHONES[2][1], employee.getPhones().get(1));

        // check the iteration through the predefined records
        Iterator<TestPhoneRow> it = employee.getPhones().iterator();
        checkIterator(it, EmployeesFixture.PHONES[2]);

        // clear the phones sub-table
        employee.getPhones().clear();

        // check there are no more records in the "phones" sub-table
        assertEquals(0, employee.getPhones().size());
        assertTrue(employee.getPhones().isEmpty());
    }

}
