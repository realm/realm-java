package com.tightdb;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.tightdb.Table.PivotType;
import com.tightdb.test.TestHelper;

public class PivotTest {
    
    
    private long randDate(Random random) {
        GregorianCalendar cal = new GregorianCalendar();
        int year = random.nextInt(110)+1900;
        cal.set(cal.YEAR, year);
        int dayOfYear = random.nextInt(365);
        cal.set(cal.DAY_OF_YEAR, dayOfYear);
        return cal.getTimeInMillis()+random.nextInt(86400000);
    }


    @Test
    public void testPivot(){

        Table data = new Table();

        long ID_COL_INDEX = data.addColumn(ColumnType.INTEGER, "id");
        long STRING_COL_INDEX = data.addColumn(ColumnType.STRING, "randString");
        long INTEGER_COL_INDEX = data.addColumn(ColumnType.INTEGER, "randInteger");
        long FLOAT_COL_INDEX = data.addColumn(ColumnType.FLOAT, "randFloat");
        long DOUBLE_COL_INDEX = data.addColumn(ColumnType.DOUBLE, "randDouble");
        long DATE_COL_INDEX = data.addColumn(ColumnType.DATE, "randDate");
        long BOOLEAN_COL_INDEX = data.addColumn(ColumnType.BOOLEAN, "randBoolean");

        Random random = new Random(7357);

        for(long i = 0; i < 100000; i++) {
            if(i % 100000 == 0){
                System.out.println("Done with : "+ i);
            }
            data.add(i, ""+(char)(65+random.nextInt(25)), random.nextInt(100), random.nextFloat()*100, random.nextDouble()*100, new Date(randDate(random)), (random.nextInt(2) == 0) ? false : true);
        }
        
        Table result = new Table();
        
        
        System.out.println("data size " + data.size());
        
        long current = System.currentTimeMillis();
        data.pivot(STRING_COL_INDEX, INTEGER_COL_INDEX, PivotType.COUNT, result);
        System.out.println("COUNT");
        for (long i=0;i<result.size();i++){
            System.out.println(result.getString(0, i) + " " + result.getLong(1, i));
        }
        
    //   result = new Table();
        
        data.pivot(STRING_COL_INDEX, INTEGER_COL_INDEX, PivotType.SUM, result);
        System.out.println("SUM");
        for (long i=0;i<result.size();i++){
            System.out.println(result.getString(0, i) + " " + result.getLong(1, i));
        }
        
       // result = new Table();
        data.pivot(STRING_COL_INDEX, INTEGER_COL_INDEX, PivotType.AVG, result);
        System.out.println("AVG");
        for (long i=0;i<result.size();i++){
            System.out.println(result.getString(0, i) + " " + result.getLong(1, i));
        }
        
        
        System.out.println("Time for pivot : " + ( System.currentTimeMillis() - current) );
        
        System.out.println("column in results " + result.getColumnCount());
        System.out.println("Names: " + result.getColumnName(0) + result.getColumnName(1));
        System.out.println("rows in results " + result.size());
        assertEquals(true, result.size() > 0);
    }
}
