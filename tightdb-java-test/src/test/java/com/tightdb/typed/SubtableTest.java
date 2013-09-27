package com.tightdb.typed;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.tightdb.test.EmployeesFixture;
import com.tightdb.test.TestEmployeeRow;
import com.tightdb.test.TestPhoneQuery;
import com.tightdb.test.TestPhoneRow;
import com.tightdb.test.TestPhoneTable;
import com.tightdb.test.TestPhoneView;

public class SubtableTest extends AbstractTest {

    @Test
    public void shouldSaveSubtableChanges() {
        TestEmployeeRow employee = employees.get(0);

        // check the basic operations
        TestPhoneTable phones1 = employee.getPhones();
        assertEquals(1, phones1.size());

        phones1.add("mobile", "111");
        assertEquals(2, phones1.size());

        TestPhoneTable phones2 = employee.getPhones();
        assertEquals(2, phones2.size());

        phones2.add("mobile", "222");
        assertEquals(3, phones2.size());

        phones2.insert(1, "home", "333");
        assertEquals(4, phones2.size());

        TestPhoneTable phones3 = employee.getPhones();
        assertEquals(2, phones3.type.eq("mobile").count());
        assertEquals(2, phones3.type.eq("home").count());

        assertEquals(1, phones3.number.eq("111").count());
        assertEquals(1, phones3.number.eq("123").count());
        assertEquals(0, phones3.number.eq("xxx").count());
        
        // check the search operations
        TestPhoneQuery phoneQuery = phones3.where().number.eq("111").number
                .neq("wrong").type.eq("mobile").type.neq("wrong");
        assertEquals(1, phoneQuery.count());

        TestPhoneView all = phoneQuery.findAll();
        assertEquals(1, all.size());
        checkPhone(all.get(0), "mobile", "111");

        checkPhone(phoneQuery.findFirst(), "mobile", "111");
        checkPhone(phoneQuery.findLast(), "mobile", "111");
        checkPhone(phoneQuery.findNext(), "mobile", "111");
        assertEquals(null, phoneQuery.findNext());

        // make sure the other sub-tables and independent and were not changed
        assertEquals(EmployeesFixture.PHONES[1].length, employees.get(1)
                .getPhones().size());
        assertEquals(EmployeesFixture.PHONES[2].length, employees.get(2)
                .getPhones().size());

        // check the clear operation on the query
        phoneQuery.clear();
        assertEquals(3, phones1.size());

        // check the clear operation
        phones3.clear();
        assertEquals(0, phones1.size());
        assertEquals(0, phones2.size());
        assertEquals(0, phones3.size());

        employees.clear();
    }

    private void checkPhone(TestPhoneRow phone, String type, String number) {
        assertEquals(type, phone.getType());
        assertEquals(number, phone.getNumber());
        assertEquals(type, phone.getType());
        assertEquals(number, phone.getNumber());
    }

    @Test
    public void shouldInvalidateWhenParentTableIsCleared() {
        TestEmployeeRow employee = employees.get(0);
        TestPhoneTable phones = employee.getPhones();
        assertTrue(phones.isValid());

        employees.clear();
        assertFalse(phones.isValid());
    }

    @Test
    public void shouldInvalidateOnRemovedRecordParentTable() {
        TestEmployeeRow employee = employees.get(0);
        TestPhoneTable phones = employee.getPhones();
        assertTrue(phones.isValid());

        employees.remove(2);
        assertFalse(phones.isValid());
    }

}
