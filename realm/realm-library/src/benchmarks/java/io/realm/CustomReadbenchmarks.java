package io.realm;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;

import org.junit.runner.RunWith;

import dk.ilios.spanner.AfterExperiment;
import dk.ilios.spanner.BeforeExperiment;
import dk.ilios.spanner.Benchmark;
import dk.ilios.spanner.CustomMeasurement;
import dk.ilios.spanner.junit.SpannerRunner;
import io.realm.entities.AllTypes;

@RunWith(SpannerRunner.class)
public class CustomReadbenchmarks {

    private Realm realm;
    private AllTypes realmObject;
    private AllTypes javaObject;

    private SQLiteDatabase db;
    private SQLiteDatabaseHelper helper;
    private Cursor cursor;
    private int strColumnIndex;
    private int intColumnIndex;
    private int boolColumnIndex;

    private int reps = 100000;

    @BeforeExperiment
    public void before() {

        // Realm setup
        RealmConfiguration config = TestHelper.createConfiguration(InstrumentationRegistry.getTargetContext(), "bench");
        Realm.deleteRealm(config);
        realm = Realm.getInstance(config);
        realm.beginTransaction();
        realm.clear(AllTypes.class);
        realmObject = realm.createObject(AllTypes.class);
        realmObject.setColumnLong(1);
        realmObject.setColumnString("foo");
        realmObject.setColumnBoolean(true);
        realm.commitTransaction();

        // Java setup
        javaObject = new AllTypes();
        javaObject.setColumnLong(1);
        javaObject.setColumnString("foo");
        javaObject.setColumnBoolean(true);

        // SQLite setup
        helper = new SQLiteDatabaseHelper(InstrumentationRegistry.getTargetContext());
        db = helper.getWritableDatabase();
        db.beginTransaction();
        db.execSQL("DELETE FROM Simple");
        db.execSQL("INSERT INTO Simple VALUES('Foo', 1, 1)");
        db.setTransactionSuccessful();
        db.endTransaction();
        cursor = db.query("Simple", new String[]{"str", "int", "bool"}, null, null, null, null, null);
        cursor.moveToFirst();
        strColumnIndex = cursor.getColumnIndexOrThrow("str");
        intColumnIndex = cursor.getColumnIndexOrThrow("int");
        boolColumnIndex = cursor.getColumnIndexOrThrow("bool");
    }

    @AfterExperiment
    public void after() {
        realm.close();
        cursor.close();
        db.close();
    }

    @CustomMeasurement(units = "ns")
    public double readRealmString() {
        String str;
        long start = System.nanoTime();
        for (int i = 0; i < reps; i++) {
            str = realmObject.getColumnString();
        }
        long end = System.nanoTime();
        return (end - start)/(double) reps;
    }

    @CustomMeasurement(units = "ns")
    public double readRealmInt() {
        long value;
        long start = System.nanoTime();
        for (int i = 0; i < reps; i++) {
            value = realmObject.getColumnLong();
        }
        long end = System.nanoTime();
        return (end - start)/(double) reps;
    }

    @CustomMeasurement(units = "ns")
    public double readRealmBoolean() {
        boolean bool;
        long start = System.nanoTime();
        for (int i = 0; i < reps; i++) {
            bool = realmObject.isColumnBoolean();
        }
        long end = System.nanoTime();
        return (end - start)/(double) reps;
    }

    @CustomMeasurement(units = "ns")
    public double readJavaString() {
        String str;
        long start = System.nanoTime();
        for (int i = 0; i < reps; i++) {
            str = javaObject.getColumnString();
        }
        long end = System.nanoTime();
        return (end - start)/(double) reps;
    }

    @CustomMeasurement(units = "ns")
    public double readJavaInt() {
        long value;
        long start = System.nanoTime();
        for (int i = 0; i < reps; i++) {
            value = javaObject.getColumnLong();
        }
        long end = System.nanoTime();
        return (end - start)/(double) reps;
    }

    @CustomMeasurement(units = "ns")
    public double readJavaBoolean() {
        boolean bool;
        long start = System.nanoTime();
        for (int i = 0; i < reps; i++) {
            bool = javaObject.isColumnBoolean();
        }
        long end = System.nanoTime();
        return (end - start)/(double) reps;
    }

    @CustomMeasurement(units = "ns")
    public double readCursorString() {
        String str;
        long start = System.nanoTime();
        for (int i = 0; i < reps; i++) {
            str = cursor.getString(strColumnIndex);
        }
        long end = System.nanoTime();
        return (end - start)/(double) reps;
    }

    @CustomMeasurement(units = "ns")
    public double readCursorInt() {
        int value;
        long start = System.nanoTime();
        for (int i = 0; i < reps; i++) {
            value = cursor.getInt(intColumnIndex);
        }
        long end = System.nanoTime();
        return (end - start)/(double) reps;
    }

    @CustomMeasurement(units = "ns")
    public double readCursorBoolean() {
        boolean bool;
        long start = System.nanoTime();
        for (int i = 0; i < reps; i++) {
            bool = cursor.getInt(boolColumnIndex) == 1;
        }
        long end = System.nanoTime();
        return (end - start)/(double) reps;
    }
}
