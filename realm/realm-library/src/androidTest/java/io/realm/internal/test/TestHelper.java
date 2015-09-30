package io.realm.internal.test;

import java.util.Date;

import io.realm.Realm;
import io.realm.entities.AllTypes;
import io.realm.internal.ColumnType;
import io.realm.internal.Table;


/**
 * Class holds helper methods for the test cases
 *
 */
public class TestHelper {


    /**
     * Returns the corresponding column type for an object.
     * @param o
     * @return
     */
    public static ColumnType getColumnType(Object o){

        if (o instanceof Boolean)
            return ColumnType.BOOLEAN;
        if (o instanceof String)
            return ColumnType.STRING;
        if (o instanceof Long)
            return ColumnType.INTEGER;
        if (o instanceof Float)
            return ColumnType.FLOAT;
        if (o instanceof Double)
            return ColumnType.DOUBLE;
        if (o instanceof Date)
            return ColumnType.DATE;
        if (o instanceof byte[])
            return ColumnType.BINARY;

        return ColumnType.MIXED;
    }


    /**
     * Creates an empty table with 1 column of all our supported column types, currently 9 columns
     * @return
     */
    public static Table getTableWithAllColumnTypes(){

        Table t = new Table();

        t.addColumn(ColumnType.BINARY, "binary");
        t.addColumn(ColumnType.BOOLEAN, "boolean");
        t.addColumn(ColumnType.DATE, "date");
        t.addColumn(ColumnType.DOUBLE, "double");
        t.addColumn(ColumnType.FLOAT, "float");
        t.addColumn(ColumnType.INTEGER, "long");
        t.addColumn(ColumnType.MIXED, "mixed");
        t.addColumn(ColumnType.STRING, "string");
        t.addColumn(ColumnType.TABLE, "table");

        return t;
    }

    public static void populateForMultiSort(Realm testRealm) {
        testRealm.beginTransaction();
        testRealm.clear(AllTypes.class);
        AllTypes object1 = testRealm.createObject(AllTypes.class);
        object1.setColumnLong(5);
        object1.setColumnString("Adam");

        AllTypes object2 = testRealm.createObject(AllTypes.class);
        object2.setColumnLong(4);
        object2.setColumnString("Brian");

        AllTypes object3 = testRealm.createObject(AllTypes.class);
        object3.setColumnLong(4);
        object3.setColumnString("Adam");
        testRealm.commitTransaction();
    }
}
