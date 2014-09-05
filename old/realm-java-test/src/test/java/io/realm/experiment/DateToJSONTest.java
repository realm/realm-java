/*
 * Copyright 2014 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.realm.experiment;

import static org.testng.AssertJUnit.*;
import java.util.Calendar;
import java.util.Date;

import io.realm.ColumnType;
import io.realm.Table;
import org.testng.annotations.Test;

public class DateToJSONTest {
    @Test
    public void shouldExportJSONContainingSomeValues() {

        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int year = cal.get(Calendar.YEAR);

        Table t = new Table();

        t.addColumn(ColumnType.DATE, "date");
        t.addColumn(ColumnType.STRING, "name");

        t.add(date, "name1");

        //JSON must contain the current year
        assertTrue(t.toJson().contains(""+year));

        //JSON should not contain the next year
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

       // System.out.println("Month: " + month2);
       // System.out.println(t.toJson());


        assertTrue(t.toJson().contains(""+month2));
    }
}
