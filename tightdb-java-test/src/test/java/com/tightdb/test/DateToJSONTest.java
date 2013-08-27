package com.tightdb.test;

import static org.testng.AssertJUnit.*;
import java.util.Calendar;
import java.util.Date;
import org.testng.annotations.Test;



import com.tightdb.ColumnType;
import com.tightdb.Table;

public class DateToJSONTest {
    @Test
    public void shouldExportJSONContainingSomeValues() {

        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int year = cal.get(Calendar.YEAR);

        Table t = new Table();

        t.addColumn(ColumnType.ColumnTypeDate, "date");
        t.addColumn(ColumnType.ColumnTypeString, "name");

        t.add(date, "name1");   

        //JSON must contain the current year
        assertTrue(t.toJson().contains(""+year));

        //JSON should not contain the next yeaer
        assertFalse(t.toJson().contains(""+year+1));

        Date date2 = new Date();
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        //Month is zero-indexed
        int month2 = cal2.get(Calendar.MONTH)+1;

        t.add(date2, "name");
        t.add(new Date(), "name");
        t.add(new Date(), "name");
        t.add(new Date(), "name");
        t.add(new Date(), "name");

        assertTrue(t.toJson().contains("name"));
        
        System.out.println("Month: " + month2);
        System.out.println(t.toJson());
        

        assertTrue(t.toJson().contains(""+month2));
    }
}
