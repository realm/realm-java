package performance.realm.io.performance;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "database2.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "t1";
    public static final String COLUMN_INDEX = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_EMAIL = "email";

    private static final String DATABASE_CREATE = String.format(
            "create table %s (%s INTEGER, %s VARCHAR(100), %s VARCHAR(100));",
            TABLE_NAME, COLUMN_INDEX, COLUMN_NAME, COLUMN_EMAIL);
    private static final String DATABASE_DROP = String.format("drop table if exists %s;", TABLE_NAME);

    public SQLiteHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(DATABASE_DROP);
        sqLiteDatabase.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {

    }
}
