package com.tightdb;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.tightdb.TableOrView.PivotType;

public class PivotTest {

    Table t = new Table();
    long colIndexSex;
    long colIndexAge;
    long colIndexHired;
    
    @BeforeTest
    public void setup(){
    
        colIndexSex = t.addColumn(ColumnType.STRING, "sex");
        colIndexAge = t.addColumn(ColumnType.INTEGER, "age");
        colIndexHired = t.addColumn(ColumnType.BOOLEAN, "hired");
        
        for (long i=0;i<5000000;i++){
            String sex = i % 2 == 0 ? "Male" : "Female";
            t.add(sex, 20 + (i%20), true);
        }
    }
    
    @Test
    public void testPivotTable(){
        
        Table resultCount = t.pivot(colIndexSex, colIndexAge, PivotType.COUNT);
        assertEquals(2, resultCount.size());
        assertEquals(2500000, resultCount.getLong(1, 0));
        assertEquals(2500000, resultCount.getLong(1, 1));
        
        Table resultMin = t.pivot(colIndexSex, colIndexAge, PivotType.MIN);
        assertEquals(20, resultMin.getLong(1, 0));
        assertEquals(21, resultMin.getLong(1, 1));
        
        Table resultMax = t.pivot(colIndexSex, colIndexAge, PivotType.MAX);
        assertEquals(38, resultMax.getLong(1, 0));
        assertEquals(39, resultMax.getLong(1, 1));
        
        try { t.pivot(colIndexHired, colIndexAge, PivotType.SUM); fail("Group by not a String column"); } catch (UnsupportedOperationException e) { }
        try { t.pivot(colIndexSex, colIndexHired, PivotType.SUM); fail("Aggregation not an int column"); } catch (UnsupportedOperationException e) { }
    }
    
    
    @Test
    public void testPivotTableView(){
        
        TableView view = t.getSortedView(colIndexAge);
        
        Table resultCount = view.pivot(colIndexSex, colIndexAge, PivotType.COUNT);
        assertEquals(2, resultCount.size());
        assertEquals(2500000, resultCount.getLong(1, 0));
        assertEquals(2500000, resultCount.getLong(1, 1));
        
        Table resultMin = view.pivot(colIndexSex, colIndexAge, PivotType.MIN);
        assertEquals(20, resultMin.getLong(1, 0));
        assertEquals(21, resultMin.getLong(1, 1));
        
        Table resultMax = view.pivot(colIndexSex, colIndexAge, PivotType.MAX);
        assertEquals(38, resultMax.getLong(1, 0));
        assertEquals(39, resultMax.getLong(1, 1));
        
        
        try { view.pivot(colIndexHired, colIndexAge, PivotType.SUM); fail("Group by not a String column"); } catch (UnsupportedOperationException e) { }
        try { view.pivot(colIndexSex, colIndexHired, PivotType.SUM); fail("Aggregation not an int column"); } catch (UnsupportedOperationException e) { }
    }
}
