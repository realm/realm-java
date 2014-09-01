package io.realm.experiment;

import java.util.Date;

import io.realm.TableQuery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import io.realm.ColumnType;
import io.realm.Table;

public class ColumnTypeQueryTest {
    private Table t;
    private TableQuery q;

    @BeforeMethod
    public void init() {
        t  = new Table();
        t.addColumn(ColumnType.DATE, "Date");
        t.addColumn(ColumnType.STRING, "String");
        t.addColumn(ColumnType.INTEGER, "Long");

        t.add(new Date(), "I'm a String", 33);
        t.add(new Date(), "Second String", 458);

        q = t.where();
    }

    @Test(expectedExceptions=IllegalArgumentException.class)

    public void filterStringOnDateColumn() {
        q.equalTo(0, "I'm a String").findAll();
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void filterLongOnStringColumn() {
        q.equalTo(1, 23).findAll();
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void filterStringOnIntColumn() {
        q.equalTo(2, "I'm a String").findAll();
    }


}
