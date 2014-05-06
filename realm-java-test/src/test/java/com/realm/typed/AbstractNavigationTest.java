package com.realm.typed;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

import org.testng.annotations.Test;

import com.realm.test.TestEmployeeQuery;
import com.realm.test.TestEmployeeRow;
import com.realm.test.TestEmployeeView;

public abstract class AbstractNavigationTest {

    protected abstract AbstractTableOrView<TestEmployeeRow, TestEmployeeView, TestEmployeeQuery> getTableOrView();

    @Test
    public void shouldNavigateToFirstRecord() {
        TestEmployeeRow first = getTableOrView().first();

        assertEquals(0, first.getPosition());
    }

    @Test
    public void shouldNavigateToLastRecord() {
        TestEmployeeRow last = getTableOrView().last();

        assertEquals(getTableOrView().size() - 1, last.getPosition());
    }

    @Test
    public void shouldNavigateToNextRecord() {
        TestEmployeeRow e = getTableOrView().get(0).next();

        assertEquals(1, e.getPosition());
    }

    @Test
    public void shouldNavigateToPreviousRecord() {
        TestEmployeeRow e = getTableOrView().get(1).previous();

        assertEquals(0, e.getPosition());
    }

    @Test
    public void shouldNavigateAfterSpecifiedRecords() {
        TestEmployeeRow e = getTableOrView().get(0).after(2);

        assertEquals(2, e.getPosition());
    }

    @Test
    public void shouldNavigateBeforeSpecifiedRecords() {
        TestEmployeeRow e = getTableOrView().get(2).before(2);

        assertEquals(0, e.getPosition());
    }

    @Test
    public void shouldReturnNullOnInvalidPosition() {
        assertNull(getTableOrView().get(0).previous());
        assertNull(getTableOrView().last().next());
        assertNull(getTableOrView().get(1).before(2));
        assertNull(getTableOrView().get(2).after(1000));
    }

}
