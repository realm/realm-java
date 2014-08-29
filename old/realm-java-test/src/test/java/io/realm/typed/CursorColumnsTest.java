package io.realm.typed;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.nio.ByteBuffer;
import java.util.Date;

import org.testng.annotations.Test;

import io.realm.ColumnType;
import io.realm.Mixed;
import io.realm.test.EmployeesFixture;
import io.realm.test.TestEmployeeQuery;
import io.realm.test.TestEmployeeRow;
import io.realm.test.TestEmployeeView;

public class CursorColumnsTest extends AbstractTest {

    @Test
    public void shouldGetCorrectColumnValues() throws IllegalAccessException {
        TestEmployeeRow employee0 = employees.first();
        checkCursor(EmployeesFixture.EMPLOYEES[0], employee0);

        TestEmployeeRow employee1 = employees.get(1);
        checkCursor(EmployeesFixture.EMPLOYEES[1], employee1);

        TestEmployeeRow employee2 = employee1.next();
        checkCursor(EmployeesFixture.EMPLOYEES[2], employee2);
    }

    @Test
    public void shouldSetAndGetCorrectColumnValues() {
        checkSetAndGetCorrectColumnValues(employees);
        checkSetAndGetCorrectColumnValues(employeesView);
    }

    private void checkSetAndGetCorrectColumnValues(
            AbstractTableOrView<TestEmployeeRow, TestEmployeeView, TestEmployeeQuery> empls) {
        TestEmployeeRow employee0 = empls.first();
        checkCursor(EmployeesFixture.EMPLOYEES[0], employee0);

        updateEmployee(employee0, EmployeesFixture.EMPLOYEES[2]);
        checkCursor(EmployeesFixture.EMPLOYEES[2], employee0);

        updateEmployee(employee0, EmployeesFixture.EMPLOYEES[1]);
        checkCursor(EmployeesFixture.EMPLOYEES[1], employee0);
        checkCursor(EmployeesFixture.EMPLOYEES[1], empls.first());
    }

    @Test
    public void shouldSetAndGetMixedValues() throws Exception {
        checkSetAndGetMixedValues(employees);
        checkSetAndGetMixedValues(employeesView);
    }

    private void checkSetAndGetMixedValues(
            AbstractTableOrView<TestEmployeeRow, TestEmployeeView, TestEmployeeQuery> empls)
            throws Exception {
        TestEmployeeRow employee = empls.first();

        employee.setExtra(new Mixed(true));
        assertEquals(true, employee.getExtra().getBooleanValue());
        assertEquals(ColumnType.BOOLEAN, employee.getExtra().getType());

        byte[] arr = { 1, 3, 5 };
        employee.setExtra(new Mixed(arr));
        // FIXME: shouldn't be BINARY_TYPE_BYTE_ARRAY an expected type here?
        assertEquals(Mixed.BINARY_TYPE_BYTE_BUFFER, employee.getExtra().getBinaryType());
        assertEquals(ByteBuffer.wrap(arr), employee.getExtra().getBinaryValue());
        assertEquals(ColumnType.BINARY, employee.getExtra().getType());



        Date date = new Date(6547);
        employee.setExtra(new Mixed(date));
        assertEquals(date, employee.getExtra().getDateValue());
        assertEquals(ColumnType.DATE, employee.getExtra().getType());

        long num = 135L;
        employee.setExtra(new Mixed(num));
        assertEquals(num, employee.getExtra().getLongValue());
        assertEquals(ColumnType.INTEGER, employee.getExtra().getType());

        Mixed mixed = Mixed.mixedValue("mixed");
        employee.setExtra(mixed);
        assertEquals(mixed, employee.getExtra());
        assertEquals(ColumnType.STRING, employee.getExtra().getType());

        employee.setExtra(new Mixed("abc"));
        assertEquals("abc", employee.getExtra().getStringValue());
        assertEquals(ColumnType.STRING, employee.getExtra().getType());
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void shouldntSetTableValue() {
        // the "set" operation is not supported yet for sub-table columns
        employees.first().setPhones(employees.last().getPhones());
    }

    public void shouldProvideReadableValue() {
        TestEmployeeRow employee = employees.first();

        assertNotNull(employee.getFirstName());
        assertNotNull(employee.getLastName());
        assertNotNull(employee.getSalary());
        assertNotNull(employee.getDriver());
        assertNotNull(employee.getPhoto());
        assertNotNull(employee.getBirthdate());
        assertNotNull(employee.getExtra());
        assertNotNull(employee.getPhones());
    }

}
