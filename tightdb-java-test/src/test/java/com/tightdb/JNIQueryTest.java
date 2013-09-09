package com.tightdb;

import static org.testng.AssertJUnit.*;
import org.testng.annotations.Test;

import java.util.Date;

public class JNIQueryTest {

    Table table;

    void init() {
        table = new Table();
        TableSpec tableSpec = new TableSpec();
        tableSpec.addColumn(ColumnType.ColumnTypeInt, "number");
        tableSpec.addColumn(ColumnType.ColumnTypeString, "name");
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
    public void EqualWithWrongDataType() {

        Table table = new Table();

        table.addColumn(ColumnType.ColumnTypeBinary, "binary");     // 0
        table.addColumn(ColumnType.ColumnTypeBool, "boolean");      // 1
        table.addColumn(ColumnType.ColumnTypeDate, "date");         // 2
        table.addColumn(ColumnType.ColumnTypeDouble, "double");     // 3
        table.addColumn(ColumnType.ColumnTypeFloat, "float");       // 4
        table.addColumn(ColumnType.ColumnTypeInt, "long");          // 5
        table.addColumn(ColumnType.ColumnTypeMixed, "mixed");       // 6
        table.addColumn(ColumnType.ColumnTypeString, "string");     // 7
        table.addColumn(ColumnType.ColumnTypeTable, "table");       // 8

        // Query the table
        TableQuery query = table.where();

        // Compare strings in non string columns
        for(int i = 0; i <= 8; i++) {
            if(i != 7) {
                try { query.equal(0, "string");                 assert(false); } catch(IllegalArgumentException e) {}
                try { query.notEqual(0, "string");              assert(false); } catch(IllegalArgumentException e) {}
                try { query.beginsWith(0, "string");            assert(false); } catch(IllegalArgumentException e) {}
                try { query.endsWith(0, "string");              assert(false); } catch(IllegalArgumentException e) {}
                try { query.contains(0, "string");              assert(false); } catch(IllegalArgumentException e) {}
            }
        }

        // Compare integer in non integer columns
        for(int i = 0; i <= 8; i++) {
            if(i != 5) {
                try { query.equal(0, 123);                      assert(false); } catch(IllegalArgumentException e) {}
                try { query.notEqual(0, 123);                   assert(false); } catch(IllegalArgumentException e) {}
                try { query.lessThan(0, 123);                   assert(false); } catch(IllegalArgumentException e) {}
                try { query.lessThanOrEqual(0, 123);            assert(false); } catch(IllegalArgumentException e) {}
                try { query.greaterThan(0, 123);                assert(false); } catch(IllegalArgumentException e) {}
                try { query.greaterThanOrEqual(0, 123);         assert(false); } catch(IllegalArgumentException e) {}
                try { query.between(0, 123, 321);               assert(false); } catch(IllegalArgumentException e) {}
            }
        }

        // Compare float in non float columns
        for(int i = 0; i <= 8; i++) {
            if(i != 4) {
                try { query.equal(0, 123F);                     assert(false); } catch(IllegalArgumentException e) {}
                try { query.notEqual(0, 123F);                  assert(false); } catch(IllegalArgumentException e) {}
                try { query.lessThan(0, 123F);                  assert(false); } catch(IllegalArgumentException e) {}
                try { query.lessThanOrEqual(0, 123F);           assert(false); } catch(IllegalArgumentException e) {}
                try { query.greaterThan(0, 123F);               assert(false); } catch(IllegalArgumentException e) {}
                try { query.greaterThanOrEqual(0, 123F);        assert(false); } catch(IllegalArgumentException e) {}
                try { query.between(0, 123F, 321F);             assert(false); } catch(IllegalArgumentException e) {}
            }
        }

        // Compare double in non double columns
        for(int i = 0; i <= 8; i++) {
            if(i != 3) {
                try { query.equal(i, 123D);                     assert(false); } catch(IllegalArgumentException e) {}
                try { query.notEqual(0, 123D);                  assert(false); } catch(IllegalArgumentException e) {}
                try { query.lessThan(0, 123D);                  assert(false); } catch(IllegalArgumentException e) {}
                try { query.lessThanOrEqual(0, 123D);           assert(false); } catch(IllegalArgumentException e) {}
                try { query.greaterThan(0, 123D);               assert(false); } catch(IllegalArgumentException e) {}
                try { query.greaterThanOrEqual(0, 123D);        assert(false); } catch(IllegalArgumentException e) {}
                try { query.between(0, 123D, 321D);             assert(false); } catch(IllegalArgumentException e) {}
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
            if(i != 0) {
                try { query.equal(i, new Date());                   assert(false); } catch(IllegalArgumentException e) {}
                try { query.lessThan(0, new Date());                assert(false); } catch(IllegalArgumentException e) {}
                try { query.lessThanOrEqual(0, new Date());         assert(false); } catch(IllegalArgumentException e) {}
                try { query.greaterThan(0, new Date());             assert(false); } catch(IllegalArgumentException e) {}
                try { query.greaterThanOrEqual(0, new Date());      assert(false); } catch(IllegalArgumentException e) {}
                try { query.between(0, new Date(), new Date());     assert(false); } catch(IllegalArgumentException e) {}
            }
        }
        */
    }

}
