package com.tightdb.typed;

import static org.testng.AssertJUnit.assertEquals;

import com.tightdb.Table;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.tightdb.test.TestNumbersTable;
import com.tightdb.test.TestNumbersView;

public class NumbersTest {

    private TestNumbersTable numbers;
    private TestNumbersView view;

    @BeforeMethod
    public void init() {
        numbers = new TestNumbersTable();

        numbers.add(10000, 10000.1f, 10000.1d);
        numbers.add(10000, 10000.1f, 10000.1d);
        numbers.insert(1, 30000, 30000.6f, 30000.6d);

        assertEquals(3, numbers.size());
        
        view = numbers.where().findAll();
        
        assertEquals(3, view.size());

    }

    @Test
    public void shouldMatchFloats() {
        assertEquals(1, numbers.floatNum.equal(30000.6f).findAll().size());
        assertEquals(1, numbers.floatNum.eq(30000.6f).findAll().size());

        assertEquals(2, numbers.floatNum.notEqual(30000.6f).findAll().size());
        assertEquals(2, numbers.floatNum.neq(30000.6f).findAll().size());

        assertEquals(2, numbers.floatNum.lessThan(30000.6f).findAll().size());
        assertEquals(2, numbers.floatNum.lt(30000.6f).findAll().size());

        assertEquals(3, numbers.floatNum.lessThanOrEqual(30000.6f).findAll().size());
        assertEquals(3, numbers.floatNum.lte(30000.6f).findAll().size());

        assertEquals(3, numbers.floatNum.greaterThan(5000).findAll().size());
        assertEquals(3, numbers.floatNum.gt(5000).findAll().size());

        assertEquals(3, numbers.floatNum.greaterThanOrEqual(10000.1f).findAll().size());
        assertEquals(3, numbers.floatNum.gte(10000.1f).findAll().size());

        assertEquals(2, numbers.floatNum.between(5000, 15000).findAll().size());
    }

    @Test
    public void shouldMatchDoubles() {
        assertEquals(1, numbers.doubleNum.equal(30000.6).findAll().size());
        assertEquals(1, numbers.doubleNum.eq(30000.6).findAll().size());

        assertEquals(2, numbers.doubleNum.notEqual(30000.6).findAll().size());
        assertEquals(2, numbers.doubleNum.neq(30000.6).findAll().size());

        assertEquals(2, numbers.doubleNum.lessThan(30000.6).findAll().size());
        assertEquals(2, numbers.doubleNum.lt(30000.6).findAll().size());

        assertEquals(3, numbers.doubleNum.lessThanOrEqual(30000.6).findAll().size());
        assertEquals(3, numbers.doubleNum.lte(30000.6).findAll().size());

        assertEquals(3, numbers.doubleNum.greaterThan(5000).findAll().size());
        assertEquals(3, numbers.doubleNum.gt(5000).findAll().size());

        assertEquals(3, numbers.doubleNum.greaterThanOrEqual(10000.1).findAll().size());
        assertEquals(3, numbers.doubleNum.gte(10000.1).findAll().size());

        assertEquals(2, numbers.doubleNum.between(5000, 15000).findAll().size());
    }

    @Test
    public void shouldAggregateFloats() {
        assertEquals(10000.1f, numbers.floatNum.minimum());
        assertEquals(10000.1f, numbers.floatNum.minimum(0, 1, Table.INFINITE)); // first
        assertEquals(30000.6f, numbers.floatNum.minimum(1, 2, Table.INFINITE)); // second
        assertEquals(10000.1f, numbers.floatNum.minimum(0, 2, Table.INFINITE)); // 1st & 2nd

        assertEquals(30000.6f, numbers.floatNum.maximum());
        assertEquals(10000.1f, numbers.floatNum.maximum(0, 1, Table.INFINITE)); // first
        assertEquals(30000.6f, numbers.floatNum.maximum(1, 2, Table.INFINITE)); // second
        assertEquals(30000.6f, numbers.floatNum.maximum(0, 2, Table.INFINITE)); // 1st & 2nd

        assertEquals(50000.8d, numbers.floatNum.sum(), 0.01);
        assertEquals(10000.1d, numbers.floatNum.sum(0, 1, Table.INFINITE), 0.01); // first
        assertEquals(30000.6d, numbers.floatNum.sum(1, 2, Table.INFINITE), 0.01); // second
        assertEquals(40000.7d, numbers.floatNum.sum(0, 2, Table.INFINITE), 0.01); // 1st & 2nd

        assertEquals(50000.8d/3, numbers.floatNum.average(), 0.01);
        assertEquals(30000.6d, numbers.floatNum.average(1, 2, Table.INFINITE), 0.01); // second
        assertEquals(40000.7d/2, numbers.floatNum.average(0, 2, Table.INFINITE), 0.01); // 1st & 2nd
        assertEquals(10000.1d, numbers.floatNum.average(0, 1, Table.INFINITE), 0.01); // first
    }

    @Test
    public void shouldAggregateDoubles() {
        assertEquals(10000.1d, numbers.doubleNum.minimum());
        assertEquals(10000.1d, numbers.doubleNum.minimum(0, 1, Table.INFINITE)); // first
        assertEquals(30000.6d, numbers.doubleNum.minimum(1, 2, Table.INFINITE)); // second
        assertEquals(10000.1d, numbers.doubleNum.minimum(0, 2, Table.INFINITE)); // 1st & 2nd

        assertEquals(30000.6d, numbers.doubleNum.maximum());
        assertEquals(10000.1d, numbers.doubleNum.maximum(0, 1, Table.INFINITE)); // first
        assertEquals(30000.6d, numbers.doubleNum.maximum(1, 2, Table.INFINITE)); // second
        assertEquals(30000.6d, numbers.doubleNum.maximum(0, 2, Table.INFINITE)); // 1st & 2nd

        assertEquals(50000.8d, numbers.doubleNum.sum(), 0.01);
        assertEquals(10000.1d, numbers.doubleNum.sum(0, 1, Table.INFINITE)); // first
        assertEquals(30000.6d, numbers.doubleNum.sum(1, 2, Table.INFINITE)); // second
        assertEquals(40000.7d, numbers.doubleNum.sum(0, 2, Table.INFINITE)); // 1st & 2nd

        assertEquals(50000.8d/3, numbers.doubleNum.average(), 0.01);
        assertEquals(30000.6d, numbers.doubleNum.average(1, 2, Table.INFINITE)); // second
        assertEquals(40000.7d/2, numbers.doubleNum.average(0, 2, Table.INFINITE)); // 1st & 2nd
        assertEquals(10000.1d, numbers.doubleNum.average(0, 1, Table.INFINITE)); // first
    }
    
    
    @Test
    public void shouldAggregateIntegersOnView() {
        assertEquals(10000, view.longNum.minimum());
        assertEquals(30000, view.longNum.maximum());
        assertEquals(50000, view.longNum.sum());
        assertEquals(50000d / 3, view.longNum.average());
    }
    
    
    @Test
    public void shouldAggregateFloatsOnView() {
        assertEquals(10000.1f, view.floatNum.minimum());
        assertEquals(30000.6f, view.floatNum.maximum());
        assertEquals(50000.8d, view.floatNum.sum(), 0.01);
        assertEquals(50000.8d/3, view.floatNum.average(), 0.01);
    }
    
    @Test
    public void shouldAggregateDoublesOnView() {
        assertEquals(10000.1d, view.doubleNum.minimum());
        assertEquals(30000.6d, view.doubleNum.maximum());
        assertEquals(50000.8d, view.doubleNum.sum(), 0.01);
        assertEquals(50000.8d/3, view.doubleNum.average(), 0.01);
        
    }
    
    
    @Test
    public void searchValuesOnView() {
        assertEquals(2 , view.doubleNum.findAll(10000.1d).size() );
        assertEquals(2 , view.floatNum.findAll(10000.1f).size() );

        assertEquals(1 , view.doubleNum.findFirst(30000.6d).getPosition());
        assertEquals(1 , view.floatNum.findFirst(30000.6f).getPosition() );
    }
    
    
    
    
    @Test
    public void viewShouldAggregatesLong() {
        assertEquals(50000d / 3, view.longNum.average());
        assertEquals(50000.8d / 3, view.doubleNum.average(), 0.000001);
        assertEquals(50000.8d / 3, view.floatNum.average(), 0.01);


    }

}
