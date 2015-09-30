package io.realm.internal.test;

import java.util.Date;

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.Realm;
import io.realm.RealmFieldType;
import io.realm.entities.AllTypes;
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
    public static RealmFieldType getColumnType(Object o){

        if (o instanceof Boolean)
            return RealmFieldType.BOOLEAN;
        if (o instanceof String)
            return RealmFieldType.STRING;
        if (o instanceof Long)
            return RealmFieldType.INTEGER;
        if (o instanceof Float)
            return RealmFieldType.FLOAT;
        if (o instanceof Double)
            return RealmFieldType.DOUBLE;
        if (o instanceof Date)
            return RealmFieldType.DATE;
        if (o instanceof byte[])
            return RealmFieldType.BINARY;

        return RealmFieldType.UNSUPPORTED_MIXED;
    }


    /**
     * Creates an empty table with 1 column of all our supported column types, currently 9 columns
     * @return
     */
    public static Table getTableWithAllColumnTypes(){

        Table t = new Table();

        t.addColumn(RealmFieldType.BINARY, "binary");
        t.addColumn(RealmFieldType.BOOLEAN, "boolean");
        t.addColumn(RealmFieldType.DATE, "date");
        t.addColumn(RealmFieldType.DOUBLE, "double");
        t.addColumn(RealmFieldType.FLOAT, "float");
        t.addColumn(RealmFieldType.INTEGER, "long");
        t.addColumn(RealmFieldType.UNSUPPORTED_MIXED, "mixed");
        t.addColumn(RealmFieldType.STRING, "string");
        t.addColumn(RealmFieldType.UNSUPPORTED_TABLE, "table");

        return t;
    }

    public static void populateForMultiSort(Realm typedRealm) {
        DynamicRealm dynamicRealm = DynamicRealm.getInstance(typedRealm.getConfiguration());
        populateForMultiSort(dynamicRealm);
        dynamicRealm.close();
        typedRealm.refresh();
    }

    public static void populateForMultiSort(DynamicRealm realm) {
        realm.beginTransaction();
        realm.clear(AllTypes.CLASS_NAME);
        DynamicRealmObject object1 = realm.createObject(AllTypes.CLASS_NAME);
        object1.setLong(AllTypes.FIELD_LONG, 5);
        object1.setString(AllTypes.FIELD_STRING, "Adam");

        DynamicRealmObject object2 = realm.createObject(AllTypes.CLASS_NAME);
        object2.setLong(AllTypes.FIELD_LONG, 4);
        object2.setString(AllTypes.FIELD_STRING, "Brian");

        DynamicRealmObject object3 = realm.createObject(AllTypes.CLASS_NAME);
        object3.setLong(AllTypes.FIELD_LONG, 4);
        object3.setString(AllTypes.FIELD_STRING, "Adam");
        realm.commitTransaction();
    }
}
