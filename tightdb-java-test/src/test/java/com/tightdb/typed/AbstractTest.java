package com.tightdb.typed;

import static com.tightdb.test.EmployeesFixture.EMPLOYEES;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.nio.ByteBuffer;
import java.util.Iterator;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import com.tightdb.ColumnType;
import com.tightdb.Mixed;
import com.tightdb.test.EmployeeData;
import com.tightdb.test.PhoneData;
import com.tightdb.test.TestEmployeeRow;
import com.tightdb.test.TestEmployeeTable;
import com.tightdb.test.TestEmployeeView;
import com.tightdb.test.TestPhoneRow;
import com.tightdb.typed.AbstractColumn;

public abstract class AbstractTest {

    protected static final String[] EXPECTED_COLUMNS = { "firstName",
            "lastName", "salary", "driver", "photo", "birthdate", "extra",
            "phones" };

    protected static final ColumnType[] EXPECTED_COLUMN_TYPE = {
            ColumnType.STRING, ColumnType.STRING,
            ColumnType.INTEGER, ColumnType.BOOLEAN,
            ColumnType.BINARY, ColumnType.DATE,
            ColumnType.MIXED, ColumnType.TABLE };

    protected TestEmployeeTable employees;

    protected TestEmployeeView employeesView;

    @BeforeMethod
    public void init() {
        employees = new TestEmployeeTable();

        addEmployee(employees, EMPLOYEES[0]);
        addEmployee(employees, EMPLOYEES[2]);
        insertEmployee(employees, 1, EMPLOYEES[1]);
        assertEquals(3, employees.size());

        TestEmployeeTable employeesTbl = new TestEmployeeTable();
        addEmployee(employeesTbl, EMPLOYEES[0]);
        addEmployee(employeesTbl, EMPLOYEES[2]);
        insertEmployee(employeesTbl, 1, EMPLOYEES[1]);
        employeesView = employeesTbl.where().findAll();
    }

    @AfterMethod
    public void clear() {
        employees.clear();
        assertEquals(0, employees.size());
        employeesView.clear();
    }

    protected void addEmployee(TestEmployeeTable employees, EmployeeData emp) {
        TestEmployeeRow e = employees.add(emp.firstName, emp.lastName,
                emp.salary, emp.driver, emp.photo, emp.birthdate, emp.extra, null);
        addPhones(emp, e);
    }

    protected void insertEmployee(TestEmployeeTable employees, long pos,
            EmployeeData emp) {
        TestEmployeeRow e = employees.insert(pos, emp.firstName, emp.lastName,
                emp.salary, emp.driver, emp.photo, emp.birthdate, emp.extra, null);
        addPhones(emp, e);
    }

    private void addPhones(EmployeeData emp, TestEmployeeRow e) {
        for (PhoneData phone : emp.phones) {
            e.phones.get().add(phone.type, phone.number);
        }
    }

    protected void updateEmployee(TestEmployeeRow employee, EmployeeData data) {
        employee.firstName.set(data.firstName);
        employee.lastName.set(data.lastName);
        employee.salary.set(data.salary);
        employee.driver.set(data.driver);
        // FIXME: NOTE: This is just a hack. photo.set should take a byte[] as
        // parameter.
        // using wrap() doesn't create a Direct allocated buffer as expected.
        ByteBuffer buf = ByteBuffer.allocateDirect(data.photo.length);
        buf.put(data.photo);
        employee.photo.set(buf);
        // employee.photo.set(ByteBuffer.wrap(data.photo));
        employee.birthdate.set(data.birthdate);
        employee.extra.set(Mixed.mixedValue(data.extra));
    }

    protected void checkCursorValues(EmployeeData expected,
            TestEmployeeRow employee) {
        try {
            assertEquals(expected.firstName, employee.firstName.get());
            assertEquals(expected.lastName, employee.lastName.get());
            assertEquals(expected.salary, employee.salary.get().longValue());
            assertEquals(expected.driver, employee.driver.get().booleanValue());
            assertEquals(ByteBuffer.wrap(expected.photo), employee.photo.get());
            assertEquals(expected.birthdate.getTime()/1000, employee.birthdate.get().getTime()/1000);
            assertEquals(Mixed.mixedValue(expected.extra), employee.extra.get());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void checkCursorValues(PhoneData expected, TestPhoneRow phone) {
        assertEquals(expected.type, phone.type.get());
        assertEquals(expected.number, phone.number.get());
    }

    protected void checkCursorColumns(TestEmployeeRow employee) {
        try {
            AbstractColumn<?, ?, ?, ?>[] columns = employee.columns();
            assertEquals(EXPECTED_COLUMNS.length, columns.length);

            for (int i = 0; i < columns.length; i++) {
                AbstractColumn<?, ?, ?, ?> column = columns[i];
                assertEquals(EXPECTED_COLUMNS[i], column.getName());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void checkCursor(EmployeeData expected, TestEmployeeRow employee) {
        checkCursorValues(expected, employee);
        checkCursorColumns(employee);
    }

    protected void checkIterator(Iterator<TestEmployeeRow> it,
            EmployeeData[] employeeData) {
        for (int i = 0; i < employeeData.length; i++) {
            checkIteratorOnRemove(it);
            assertTrue(it.hasNext());
            checkCursorValues(employeeData[i], it.next());
        }
        checkIteratorOnRemove(it);
    }

    private void checkIteratorOnRemove(Iterator<?> it) {
        try {
            it.remove();
        } catch (UnsupportedOperationException e) {
            return;
        }
        fail("Expected unsupported 'remove' operation!");
    }

    protected void checkIterator(Iterator<TestPhoneRow> it,
            PhoneData[] phoneData) {
        for (int i = 0; i < phoneData.length; i++) {
            checkIteratorOnRemove(it);
            assertTrue(it.hasNext());
            checkCursorValues(phoneData[i], it.next());
        }
        checkIteratorOnRemove(it);
    }

}
