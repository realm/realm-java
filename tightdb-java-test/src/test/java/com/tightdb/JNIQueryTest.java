package com.tightdb;

import static org.testng.AssertJUnit.*;

import com.tightdb.test.TestHelper;
import org.testng.annotations.Test;


import java.util.Date;

public class JNIQueryTest {

    Table table;

    void init() {
        table = new Table();
        TableSpec tableSpec = new TableSpec();
        tableSpec.addColumn(ColumnType.LONG, "number");
        tableSpec.addColumn(ColumnType.STRING, "name");
        table.updateFromSpec(tableSpec);

        table.add(10, "A");
        table.add(11, "B");
        table.add(12, "C");
        table.add(13, "B");
        table.add(14, "D");
        table.add(16, "D");
        assertEquals(6, table.size());
    }

    @Test
    public void shouldQuery() {
        init();
        TableQuery query = table.where();

        long cnt = query.equal(1, "D").count();
        assertEquals(2, cnt);

        cnt = query.minimum(0);
        assertEquals(14, cnt);

        cnt = query.maximum(0);
        assertEquals(16, cnt);

        cnt = query.sum(0);
        assertEquals(14+16, cnt);

        double avg = query.average(0);
        assertEquals(15.0, avg);

        // TODO: Add tests with all parameters
    }

    @Test
    public void queryWithWrongDataType() {

        Table table = TestHelper.getTableWithAllColumnTypes();

        // Query the table
        TableQuery query = table.where();

        // Compare strings in non string columns
        for(int i = 0; i <= 8; i++) {
            if(i != 7) {
                try { query.equal(i, "string");                 assert(false); } catch(IllegalArgumentException e) {}
                try { query.notEqual(i, "string");              assert(false); } catch(IllegalArgumentException e) {}
                try { query.beginsWith(i, "string");            assert(false); } catch(IllegalArgumentException e) {}
                try { query.endsWith(i, "string");              assert(false); } catch(IllegalArgumentException e) {}
                try { query.contains(i, "string");              assert(false); } catch(IllegalArgumentException e) {}
            }
        }

        // Compare integer in non integer columns
        for(int i = 0; i <= 8; i++) {
            if(i != 5) {
                try { query.equal(i, 123);                      assert(false); } catch(IllegalArgumentException e) {}
                try { query.notEqual(i, 123);                   assert(false); } catch(IllegalArgumentException e) {}
                try { query.lessThan(i, 123);                   assert(false); } catch(IllegalArgumentException e) {}
                try { query.lessThanOrEqual(i, 123);            assert(false); } catch(IllegalArgumentException e) {}
                try { query.greaterThan(i, 123);                assert(false); } catch(IllegalArgumentException e) {}
                try { query.greaterThanOrEqual(i, 123);         assert(false); } catch(IllegalArgumentException e) {}
                try { query.between(i, 123, 321);               assert(false); } catch(IllegalArgumentException e) {}
            }
        }

        // Compare float in non float columns
        for(int i = 0; i <= 8; i++) {
            if(i != 4) {
                try { query.equal(i, 123F);                     assert(false); } catch(IllegalArgumentException e) {}
                try { query.notEqual(i, 123F);                  assert(false); } catch(IllegalArgumentException e) {}
                try { query.lessThan(i, 123F);                  assert(false); } catch(IllegalArgumentException e) {}
                try { query.lessThanOrEqual(i, 123F);           assert(false); } catch(IllegalArgumentException e) {}
                try { query.greaterThan(i, 123F);               assert(false); } catch(IllegalArgumentException e) {}
                try { query.greaterThanOrEqual(i, 123F);        assert(false); } catch(IllegalArgumentException e) {}
                try { query.between(i, 123F, 321F);             assert(false); } catch(IllegalArgumentException e) {}
            }
        }

        // Compare double in non double columns
        for(int i = 0; i <= 8; i++) {
            if(i != 3) {
                try { query.equal(i, 123D);                     assert(false); } catch(IllegalArgumentException e) {}
                try { query.notEqual(i, 123D);                  assert(false); } catch(IllegalArgumentException e) {}
                try { query.lessThan(i, 123D);                  assert(false); } catch(IllegalArgumentException e) {}
                try { query.lessThanOrEqual(i, 123D);           assert(false); } catch(IllegalArgumentException e) {}
                try { query.greaterThan(i, 123D);               assert(false); } catch(IllegalArgumentException e) {}
                try { query.greaterThanOrEqual(i, 123D);        assert(false); } catch(IllegalArgumentException e) {}
                try { query.between(i, 123D, 321D);             assert(false); } catch(IllegalArgumentException e) {}
            }
        }

        // Compare boolean in non boolean columns
        for(int i = 0; i <= 8; i++) {
            if(i != 1) {
              try { query.equal(i, true);                       assert(false); } catch(IllegalArgumentException e) {}
            }
        }

        // Compare date
        /*
        for(int i = 0; i <= 8; i++) {
            if(i != 2) {
                try { query.equal(i, new Date());                   assert(false); } catch(IllegalArgumentException e) {}
                try { query.lessThan(i, new Date());                assert(false); } catch(IllegalArgumentException e) {}
                try { query.lessThanOrEqual(i, new Date());         assert(false); } catch(IllegalArgumentException e) {}
                try { query.greaterThan(i, new Date());             assert(false); } catch(IllegalArgumentException e) {}
                try { query.greaterThanOrEqual(i, new Date());      assert(false); } catch(IllegalArgumentException e) {}
                try { query.between(i, new Date(), new Date());     assert(false); } catch(IllegalArgumentException e) {}
            }
        }
        */
    }

    @Test
    public void columnIndexOutOfBounds() {
        Table table = TestHelper.getTableWithAllColumnTypes();

        // Query the table
        TableQuery query = table.where();

        try { query.minimum(0);                 assert(false); } catch(IllegalArgumentException e) {}
        try { query.minimumFloat(0);            assert(false); } catch(IllegalArgumentException e) {}
        try { query.minimumDouble(0);           assert(false); } catch(IllegalArgumentException e) {}
        try { query.minimum(1);                 assert(false); } catch(IllegalArgumentException e) {}
        try { query.minimumFloat(1);            assert(false); } catch(IllegalArgumentException e) {}
        try { query.minimumDouble(1);           assert(false); } catch(IllegalArgumentException e) {}
        try { query.minimum(2);                 assert(false); } catch(IllegalArgumentException e) {}
        try { query.minimumFloat(2);            assert(false); } catch(IllegalArgumentException e) {}
        try { query.minimumDouble(2);           assert(false); } catch(IllegalArgumentException e) {}
        try { query.minimum(6);                 assert(false); } catch(IllegalArgumentException e) {}
        try { query.minimumFloat(6);            assert(false); } catch(IllegalArgumentException e) {}
        try { query.minimumDouble(6);           assert(false); } catch(IllegalArgumentException e) {}
        try { query.minimum(7);                 assert(false); } catch(IllegalArgumentException e) {}
        try { query.minimumFloat(7);            assert(false); } catch(IllegalArgumentException e) {}
        try { query.minimumDouble(7);           assert(false); } catch(IllegalArgumentException e) {}
        try { query.minimum(8);                 assert(false); } catch(IllegalArgumentException e) {}
        try { query.minimumFloat(8);            assert(false); } catch(IllegalArgumentException e) {}
        try { query.minimumDouble(8);           assert(false); } catch(IllegalArgumentException e) {}

        try { query.maximum(0);                 assert(false); } catch(IllegalArgumentException e) {}
        try { query.maximumFloat(0);            assert(false); } catch(IllegalArgumentException e) {}
        try { query.maximumDouble(0);           assert(false); } catch(IllegalArgumentException e) {}
        try { query.maximum(1);                 assert(false); } catch(IllegalArgumentException e) {}
        try { query.maximumFloat(1);            assert(false); } catch(IllegalArgumentException e) {}
        try { query.maximumDouble(1);         	assert(false); } catch(IllegalArgumentException e) {}
        try { query.maximum(2);                 assert(false); } catch(IllegalArgumentException e) {}
        try { query.maximumFloat(2);            assert(false); } catch(IllegalArgumentException e) {}
        try { query.maximumDouble(2);           assert(false); } catch(IllegalArgumentException e) {}
        try { query.maximum(6);                 assert(false); } catch(IllegalArgumentException e) {}
        try { query.maximumFloat(6);            assert(false); } catch(IllegalArgumentException e) {}
        try { query.maximumDouble(6);         	assert(false); } catch(IllegalArgumentException e) {}
        try { query.maximum(7);                 assert(false); } catch(IllegalArgumentException e) {}
        try { query.maximumFloat(7);            assert(false); } catch(IllegalArgumentException e) {}
        try { query.maximumDouble(7);           assert(false); } catch(IllegalArgumentException e) {}
        try { query.maximum(8);                 assert(false); } catch(IllegalArgumentException e) {}
        try { query.maximumFloat(8);            assert(false); } catch(IllegalArgumentException e) {}
        try { query.maximumDouble(8);           assert(false); } catch(IllegalArgumentException e) {}

        try { query.sum(0);                     assert(false); } catch(IllegalArgumentException e) {}
        try { query.sumFloat(0);                assert(false); } catch(IllegalArgumentException e) {}
        try { query.sumDouble(0);               assert(false); } catch(IllegalArgumentException e) {}
        try { query.sum(1);                     assert(false); } catch(IllegalArgumentException e) {}
        try { query.sumFloat(1);                assert(false); } catch(IllegalArgumentException e) {}
        try { query.sumDouble(1);               assert(false); } catch(IllegalArgumentException e) {}
        try { query.sum(2);                     assert(false); } catch(IllegalArgumentException e) {}
        try { query.sumFloat(2);                assert(false); } catch(IllegalArgumentException e) {}
        try { query.sumDouble(2);               assert(false); } catch(IllegalArgumentException e) {}
        try { query.sum(6);                     assert(false); } catch(IllegalArgumentException e) {}
        try { query.sumFloat(6);                assert(false); } catch(IllegalArgumentException e) {}
        try { query.sumDouble(6);               assert(false); } catch(IllegalArgumentException e) {}
        try { query.sum(7);                     assert(false); } catch(IllegalArgumentException e) {}
        try { query.sumFloat(7);                assert(false); } catch(IllegalArgumentException e) {}
        try { query.sumDouble(7);               assert(false); } catch(IllegalArgumentException e) {}
        try { query.sum(8);                     assert(false); } catch(IllegalArgumentException e) {}
        try { query.sumFloat(8);                assert(false); } catch(IllegalArgumentException e) {}
        try { query.sumDouble(8);               assert(false); } catch(IllegalArgumentException e) {}

        try { query.average(0);                 assert(false); } catch(IllegalArgumentException e) {}
        try { query.averageFloat(0);            assert(false); } catch(IllegalArgumentException e) {}
        try { query.averageDouble(0);           assert(false); } catch(IllegalArgumentException e) {}
        try { query.average(1);                 assert(false); } catch(IllegalArgumentException e) {}
        try { query.averageFloat(1);            assert(false); } catch(IllegalArgumentException e) {}
        try { query.averageDouble(1);           assert(false); } catch(IllegalArgumentException e) {}
        try { query.average(2);                 assert(false); } catch(IllegalArgumentException e) {}
        try { query.averageFloat(2);            assert(false); } catch(IllegalArgumentException e) {}
        try { query.averageDouble(2);           assert(false); } catch(IllegalArgumentException e) {}
        try { query.average(6);                 assert(false); } catch(IllegalArgumentException e) {}
        try { query.averageFloat(6);            assert(false); } catch(IllegalArgumentException e) {}
        try { query.averageDouble(6);           assert(false); } catch(IllegalArgumentException e) {}
        try { query.average(7);                 assert(false); } catch(IllegalArgumentException e) {}
        try { query.averageFloat(7);            assert(false); } catch(IllegalArgumentException e) {}
        try { query.averageDouble(7);           assert(false); } catch(IllegalArgumentException e) {}
        try { query.average(8);                 assert(false); } catch(IllegalArgumentException e) {}
        try { query.averageFloat(8);            assert(false); } catch(IllegalArgumentException e) {}
        try { query.averageDouble(8);           assert(false); } catch(IllegalArgumentException e) {}
        // Out of bounds for string
        try { query.equal(9, "string");                 assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.notEqual(9, "string");              assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.beginsWith(9, "string");            assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.endsWith(9, "string");              assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.contains(9, "string");              assert(false); } catch(ArrayIndexOutOfBoundsException e) {}


        // Out of bounds for integer
        try { query.equal(9, 123);                      assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.notEqual(9, 123);                   assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.lessThan(9, 123);                   assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.lessThanOrEqual(9, 123);            assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.greaterThan(9, 123);                assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.greaterThanOrEqual(9, 123);         assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.between(9, 123, 321);               assert(false); } catch(ArrayIndexOutOfBoundsException e) {}


        // Out of bounds for float
        try { query.equal(9, 123F);                     assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.notEqual(9, 123F);                  assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.lessThan(9, 123F);                  assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.lessThanOrEqual(9, 123F);           assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.greaterThan(9, 123F);               assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.greaterThanOrEqual(9, 123F);        assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.between(9, 123F, 321F);             assert(false); } catch(ArrayIndexOutOfBoundsException e) {}


        // Out of bounds for double
        try { query.equal(9, 123D);                     assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.notEqual(9, 123D);                  assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.lessThan(9, 123D);                  assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.lessThanOrEqual(9, 123D);           assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.greaterThan(9, 123D);               assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.greaterThanOrEqual(9, 123D);        assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.between(9, 123D, 321D);             assert(false); } catch(ArrayIndexOutOfBoundsException e) {}


        // Out of bounds for boolean
        try { query.equal(9, true);                       assert(false); } catch(ArrayIndexOutOfBoundsException e) {}

    }

}
