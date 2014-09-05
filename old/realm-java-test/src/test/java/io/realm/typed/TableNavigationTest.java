package io.realm.typed;

import java.util.Date;

import org.testng.annotations.Test;

import io.realm.test.TestEmployeeQuery;
import io.realm.test.TestEmployeeRow;
import io.realm.test.TestEmployeeTable;
import io.realm.test.TestEmployeeView;

@Test
public class TableNavigationTest extends AbstractNavigationTest {

    private TestEmployeeTable employees;

    public TableNavigationTest() {
        employees = new TestEmployeeTable();

        employees.add("John", "Doe", 10000, true, new byte[] { 1, 2, 3 },
                new Date(), "extra", null);
        employees.add("Johny", "B. Good", 20000, true, new byte[] { 1, 2, 3 },
                new Date(), true, null);
        employees.insert(1, "Nikolche", "Mihajlovski", 30000, false,
                new byte[] { 4, 5 }, new Date(), 1234, null);
    }

    @Override
    protected AbstractTableOrView<TestEmployeeRow, TestEmployeeView, TestEmployeeQuery> getTableOrView() {
        return employees;
    }

}
