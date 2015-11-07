package io.realm;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;

import org.junit.runner.RunWith;

import dk.ilios.spanner.AfterExperiment;
import dk.ilios.spanner.BeforeExperiment;
import dk.ilios.spanner.Benchmark;
import dk.ilios.spanner.junit.SpannerRunner;
import io.realm.entities.AllTypes;

@RunWith(SpannerRunner.class)
public class ReadBenchmarks {

    private Realm realm;
    private AllTypes realmObject;
    private AllTypes javaObject;

    private SQLiteDatabase db;
    private SQLiteDatabaseHelper helper;
    private Cursor cursor;
    private int strColumnIndex;
    private int intColumnIndex;
    private int boolColumnIndex;

    @BeforeExperiment
    public void before() {

        // Realm setup
        RealmConfiguration config = TestHelper.createConfiguration(InstrumentationRegistry.getTargetContext(), "bench");
        Realm.deleteRealm(config);
        realm = Realm.getInstance(config);
        realm.beginTransaction();
        realm.clear(io.realm.entities.AllTypes.class);
        realmObject = realm.createObject(io.realm.entities.AllTypes.class);
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

    @Benchmark
    public void readRealmString(int reps) {
        String str;
        for (int i = 0; i < reps; i++) {
            str = realmObject.getColumnString();
        }
    }

    @Benchmark
    public void readRealmInt(int reps) {
        long value;
        for (int i = 0; i < reps; i++) {
            value = realmObject.getColumnLong();
        }
    }

    @Benchmark
    public void readRealmBoolean(int reps) {
        boolean bool;
        for (int i = 0; i < reps; i++) {
            bool = realmObject.isColumnBoolean();
        }
    }

    @Benchmark
    public void readJavaString(int reps) {
        String str;
        for (int i = 0; i < reps; i++) {
            str = javaObject.getColumnString();
        }
    }

    @Benchmark
    public void readJavaInt(int reps) {
        long value;
        for (int i = 0; i < reps; i++) {
            value = javaObject.getColumnLong();
        }
    }

    @Benchmark
    public void readJavaBoolean(int reps) {
        boolean bool;
        for (int i = 0; i < reps; i++) {
            bool = javaObject.isColumnBoolean();
        }
    }

    @Benchmark
    public void readCursorString(int reps) {
        String str;
        for (int i = 0; i < reps; i++) {
            str = cursor.getString(strColumnIndex);
        }
    }

    @Benchmark
    public void readCursorInt(int reps) {
        int value;
        for (int i = 0; i < reps; i++) {
            value = cursor.getInt(intColumnIndex);
        }
    }

    @Benchmark
    public void readCursorBoolean(int reps) {
        boolean bool;
        for (int i = 0; i < reps; i++) {
            bool = cursor.getInt(boolColumnIndex) == 1;
        }
    }
}
