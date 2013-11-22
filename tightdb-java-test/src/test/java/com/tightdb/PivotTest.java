package com.tightdb;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.tightdb.TableOrView.PivotType;
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

    Table data = new Table();
    private Long ID_COL_INDEX = null;
    private Long STRING_COL_INDEX = null;
    private Long INTEGER_COL_INDEX = null;
    private Long FLOAT_COL_INDEX = null;
    private Long DOUBLE_COL_INDEX = null;
    private Long DATE_COL_INDEX = null;
    private Long BOOLEAN_COL_INDEX = null;


    @BeforeTest
    public void generateData() {

        ID_COL_INDEX = data.addColumn(ColumnType.INTEGER, "id");
        STRING_COL_INDEX = data.addColumn(ColumnType.STRING, "randString");
        INTEGER_COL_INDEX = data.addColumn(ColumnType.INTEGER, "randInteger");
        FLOAT_COL_INDEX = data.addColumn(ColumnType.FLOAT, "randFloat");
        DOUBLE_COL_INDEX = data.addColumn(ColumnType.DOUBLE, "randDouble");
        DATE_COL_INDEX = data.addColumn(ColumnType.DATE, "randDate");
        BOOLEAN_COL_INDEX = data.addColumn(ColumnType.BOOLEAN, "randBoolean");

        Random random = new Random(7357);

        for(long i = 0; i < 100000; i++) {
            if(i % 100000 == 0){
                System.out.println("Done with : "+ i);
            }
            data.add(i, ""+(char)(65+random.nextInt(25)), random.nextInt(100), random.nextFloat()*100, random.nextDouble()*100, new Date(randDate(random)), (random.nextInt(2) == 0) ? false : true);
        }
    }


    @Test
    public void testPivotTable(){


        long current = System.currentTimeMillis();
        Table result = data.pivot(STRING_COL_INDEX, INTEGER_COL_INDEX, PivotType.COUNT);
        /*System.out.println("COUNT");
        for (long i=0;i<result.size();i++){
            System.out.println(result.getString(0, i) + " " + result.getLong(1, i));
        }
        result = data.pivot(STRING_COL_INDEX, INTEGER_COL_INDEX, PivotType.SUM);
        System.out.println("SUM");
        for (long i=0;i<result.size();i++){
            System.out.println(result.getString(0, i) + " " + result.getLong(1, i));
        }
        result = data.pivot(STRING_COL_INDEX, INTEGER_COL_INDEX, PivotType.AVG);
        System.out.println("AVG");
        for (long i=0;i<result.size();i++){
            System.out.println(result.getString(0, i) + " " + result.getLong(1, i));
        }
        result = data.pivot(STRING_COL_INDEX, INTEGER_COL_INDEX, PivotType.MIN);
        System.out.println("MIN");
        for (long i=0;i<result.size();i++){
            System.out.println(result.getString(0, i) + " " + result.getLong(1, i));
        }
        
        result = data.pivot(STRING_COL_INDEX, INTEGER_COL_INDEX, PivotType.MAX);
        System.out.println("MAX");
        for (long i=0;i<result.size();i++){
            System.out.println(result.getString(0, i) + " " + result.getLong(1, i));
        }

        System.out.println("Time for pivot : " + ( System.currentTimeMillis() - current) );
        System.out.println("column in results " + result.getColumnCount());
        System.out.println("Names: " + result.getColumnName(0) + result.getColumnName(1));
        System.out.println("rows in results " + result.size());*/
        assertEquals(true, result.size() > 0);
    }

    @Test
    public void testPivotTableView(){

        // Smallest value is 0
        TableView dataView = data.where().lessThan(INTEGER_COL_INDEX, 20).findAll();
        Table res = dataView.pivot(STRING_COL_INDEX, INTEGER_COL_INDEX, PivotType.MIN);
        assertEquals(0, res.minimumInt(1));
        
        // Largest value is 19
        dataView = data.where().lessThan(INTEGER_COL_INDEX, 20).findAll();
        res = dataView.pivot(STRING_COL_INDEX, INTEGER_COL_INDEX, PivotType.MAX);
        assertEquals(19, res.maximumInt(1));
        
        // Largest and smallest value is 20
        dataView = data.where().equalTo(INTEGER_COL_INDEX, 20).findAll();
        res = dataView.pivot(STRING_COL_INDEX, INTEGER_COL_INDEX, PivotType.MAX);
        assertEquals(20, res.minimumInt(1));
        assertEquals(20, res.maximumInt(1));
        res = dataView.pivot(STRING_COL_INDEX, INTEGER_COL_INDEX, PivotType.MIN);
        assertEquals(20, res.minimumInt(1));
        assertEquals(20, res.maximumInt(1));
        
        
        res = dataView.pivot(STRING_COL_INDEX, INTEGER_COL_INDEX, PivotType.COUNT);
        res.getColumnName(1).contains("COUNT:");
    }
}
