package io.realm.examples.performance.sqlite;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class EmployeeDatabaseHelper extends SQLiteOpenHelper {

    private final static String TAG = EmployeeDatabaseHelper.class.getName();

    public static final String TABLE_EMPLOYEES = "Employee";

    public static final String COLUMN_ID    = "_id";
    public static final String COLUMN_NAME  = "name";
    public static final String COLUMN_AGE   = "age";
    public static final String COLUMN_HIRED = "hired";

    private static final String DATABASE_NAME = "sqlite_employees.db";
    private static final int DATABASE_VERSION = 1;
    public static final String[] ALL_COLUMNS = new String[]{ COLUMN_ID, COLUMN_NAME, COLUMN_AGE, COLUMN_HIRED };

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_EMPLOYEES + "("
            + COLUMN_ID
            + " integer primary key autoincrement, "
            + COLUMN_NAME
            + " text not null, "
            + COLUMN_AGE
            + " integer, "
            + COLUMN_HIRED
            + " integer);";

    private AssetManager am;

    public EmployeeDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        am = context.getAssets();
    }

    @Override
    public void onCreate(android.database.sqlite.SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(android.database.sqlite.SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(EmployeeDatabaseHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EMPLOYEES);
        onCreate(db);
    }
}