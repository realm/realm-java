/*
 * Copyright 2014 Realm Inc.
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

package io.realm.typed;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.util.Iterator;

import io.realm.ColumnType;
import io.realm.Mixed;
import io.realm.test.EmployeesFixture;
import io.realm.test.PhoneData;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import io.realm.test.EmployeeData;
import io.realm.test.TestEmployeeRow;
import io.realm.test.TestEmployeeTable;
import io.realm.test.TestEmployeeView;
import io.realm.test.TestPhoneRow;

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

        addEmployee(employees, EmployeesFixture.EMPLOYEES[0]);
        addEmployee(employees, EmployeesFixture.EMPLOYEES[2]);
        insertEmployee(employees, 1, EmployeesFixture.EMPLOYEES[1]);
        assertEquals(3, employees.size());

        TestEmployeeTable employeesTbl = new TestEmployeeTable();
        addEmployee(employeesTbl, EmployeesFixture.EMPLOYEES[0]);
        addEmployee(employeesTbl, EmployeesFixture.EMPLOYEES[2]);
        insertEmployee(employeesTbl, 1, EmployeesFixture.EMPLOYEES[1]);
        employeesView = employeesTbl.where().findAll();
    }

    @AfterMethod
    public void clear() {
        employees.clear();
        assertEquals(0, employees.size());
        employeesView.clear();
        employees = null;
        employeesView = null;
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
            e.getPhones().add(phone.type, phone.number);
        }
    }

    protected void updateEmployee(TestEmployeeRow employee, EmployeeData data) {
        employee.setFirstName(data.firstName);
        employee.setLastName(data.lastName);
        employee.setSalary(data.salary);
        employee.setDriver(data.driver);
        employee.setPhoto(data.photo);
        // employee.photo.set(ByteBuffer.wrap(data.photo));
        employee.setBirthdate(data.birthdate);
        employee.setExtra(Mixed.mixedValue(data.extra));
    }

    protected void checkCursorValues(EmployeeData expected,
            TestEmployeeRow employee) {
        try {
            assertEquals(expected.firstName, employee.getFirstName());
            assertEquals(expected.lastName, employee.getLastName());
            assertEquals(expected.salary, employee.getSalary());
            assertEquals(expected.driver, employee.getDriver());
            assertEquals(expected.photo, employee.getPhoto());
            assertEquals(expected.birthdate.getTime()/1000, employee.getBirthdate().getTime()/1000);
            assertEquals(Mixed.mixedValue(expected.extra), employee.getExtra());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void checkCursorValues(PhoneData expected, TestPhoneRow phone) {
        assertEquals(expected.type, phone.getType());
        assertEquals(expected.number, phone.getNumber());
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
