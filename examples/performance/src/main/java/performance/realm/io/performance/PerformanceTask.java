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

package performance.realm.io.performance;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import io.realm.ResultList;
import io.realm.internal.ColumnType;
import io.realm.internal.ReadTransaction;
import io.realm.internal.SharedGroup;
import io.realm.internal.Table;
import io.realm.internal.WriteTransaction;
import performance.realm.io.performance.entities.User;

public class PerformanceTask extends AsyncTask<Integer, String, String> {
    private Activity activity;


    public PerformanceTask(Activity activity) {
        this.activity = activity;
    }

    @Override
    protected String doInBackground(Integer... params) {
        final int listSize = params[0];
        long timer;
        Map<String, Long> timings = new HashMap<String, Long>();

        // ArrayList

        System.out.println("################################ Testing ArrayList");

        List<User> arrayList = new ArrayList<User>(listSize);

        timer = System.currentTimeMillis();
        for(int i = 0; i < listSize; i++) {
            User user = new User();

            user.setId(i);
            user.setName("John Doe");
            user.setEmail("john@doe.com");

            arrayList.add(user);
        }
        timings.put("ArrayList_Add", (System.currentTimeMillis() - timer));

        timer = System.currentTimeMillis();
        for(int i = 0; i < listSize; i++) {
            User u = arrayList.get(i);
        }
        timings.put("ArrayList_Get", (System.currentTimeMillis() - timer));


        // RealmList

        System.out.println("################################ Testing new interface");

        Realm.setDefaultDurability(SharedGroup.Durability.FULL);
        Realm realm = new Realm(activity);
        realm.clear();

        timer = System.currentTimeMillis();
        try {
            //Debug.startMethodTracing("writes");
            realm.beginWrite();
            for(int i = 0; i < listSize; i++) {
                User user = realm.create(User.class);

                user.setId(i);
                user.setName("John Doe");
                user.setEmail("john@doe.com");

                // realm.add(user);

            }
            realm.commit();
            //Debug.stopMethodTracing();
        } catch(Throwable t) {
            t.printStackTrace();
            return null;
        }

        timings.put("RealmList_Add", (System.currentTimeMillis() - timer));

        timer = System.currentTimeMillis();
        //Debug.startMethodTracing("reads");
        ResultList<User> realmList = realm.where(User.class).findAll();
        for(int i = 0; i < listSize; i++) {
            // IUser u = realmList.getTest(i, IUser.class);
            User u = realmList.get(i);
            //       System.out.println(u.getId());

            u.getId();
            u.getName();
            u.getEmail();

        }
        //Debug.stopMethodTracing();
        timings.put("RealmList_Get", (System.currentTimeMillis() - timer));


        // TightDB dyn

        System.out.println("################################ Testing dynamic interface");

        SharedGroup sg = new SharedGroup(activity.getFilesDir().getPath()+"/perfTest.tightdb");

        //Debug.startMethodTracing("dyn");
        WriteTransaction wt = sg.beginWrite();
        try {
            if (!wt.hasTable("test")) {
                System.out.println("Creating new table");
                Table users = wt.getTable("test");
                users.addColumn(ColumnType.INTEGER, "id");
                users.addColumn(ColumnType.STRING, "name");
                users.addColumn(ColumnType.STRING, "email");
            }
            wt.getTable("test").clear();
            wt.commit();
        } catch(Throwable t) {
            t.printStackTrace();
            wt.rollback();
        }

        timer = System.currentTimeMillis();
        wt = sg.beginWrite();
        try {
            Table users = wt.getTable("test");
            for (int i = 0; i < listSize; i++) {
                User user = new User();

                user.setId(i);
                user.setName("John Doe");
                user.setEmail("john@doe.com");
                users.add(user.getId(), user.getName(), user.getEmail());
            }
            wt.commit();
        } catch(Throwable t) {
            t.printStackTrace();
            wt.rollback();
        }
        //Debug.stopMethodTracing();
        timings.put("TightDB_Add", (System.currentTimeMillis() - timer));
        timer = System.currentTimeMillis();

        ReadTransaction rt = sg.beginRead();
        Table users = rt.getTable("test");
        for(int i = 0; i < listSize; i++) {
            User u = new User();
            u.setId(((Long)users.getLong(0, i)).intValue());
            u.setName(users.getString(1, i));
            u.setEmail(users.getString(2, i));
        }
        rt.endRead();
        timings.put("TightDB_Get", (System.currentTimeMillis() - timer));


        // SQLite

        System.out.println("################################ Testing SQLite");

        SQLiteOpenHelper sqLiteOpenHelper = new SQLiteHelper(activity);
        SQLiteDatabase database = sqLiteOpenHelper.getWritableDatabase();

        database.execSQL("DELETE FROM t1 WHERE 1=1");

        timer = System.currentTimeMillis();


        SQLiteStatement stmt = database.compileStatement("INSERT INTO t1 VALUES(?1, ?2, ?3)");
        database.beginTransaction();
        for (int i = 0; i < listSize; ++i) {
            stmt.clearBindings();
            stmt.bindLong(1, i);
            stmt.bindString(2, "John Doe");
            stmt.bindString(3, "john@doe.com");
            stmt.executeInsert();
        }
        database.setTransactionSuccessful();
        database.endTransaction();
        stmt.close();

        timings.put("SQLite_Add", (System.currentTimeMillis() - timer));
        timer = System.currentTimeMillis();


        Cursor cursor = database.rawQuery(
                String.format("SELECT * FROM t1"),
                null);
        int i = 0;
        if(cursor.moveToFirst()) {
            do {
                User user = new User();
                user.setId(cursor.getInt(0));
                user.setName(cursor.getString(1));
                user.setEmail(cursor.getString(2));
                i++;
            } while(cursor.moveToNext());
        }

        timings.put("SQLite_Get", (System.currentTimeMillis() - timer));

        final String newLine = System.getProperty("line.separator");

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SQL ROWS ").append(i).append(newLine);

        // Output results
        stringBuilder.append("New Interface:").append(newLine);
        stringBuilder.append("Add: ").append(timings.get("RealmList_Add")).append(" ms").append(newLine);
        stringBuilder.append("Get: ").append(timings.get("RealmList_Get")).append(" ms").append(newLine);

        stringBuilder.append("Old Dyn Interface:").append(newLine);
        stringBuilder.append("Add: ").append(timings.get("TightDB_Add")).append(" ms\t\t(x").append(timings.get("RealmList_Add").doubleValue() / timings.get("TightDB_Add").doubleValue()).append(")").append(newLine);
        stringBuilder.append("Get: ").append(timings.get("TightDB_Get")).append(" ms\t\t(x").append(timings.get("RealmList_Get").doubleValue() / timings.get("TightDB_Get").doubleValue()).append(")").append(newLine);

        stringBuilder.append("ArrayList Interface:").append(newLine);
        stringBuilder.append("Add: ").append(timings.get("ArrayList_Add")).append(" ms\t\t(x").append(timings.get("RealmList_Add").doubleValue() / timings.get("ArrayList_Add").doubleValue()).append(")").append(newLine);
        stringBuilder.append("Get: ").append(timings.get("ArrayList_Get")).append(" ms\t\t(x").append(timings.get("RealmList_Get").doubleValue() / timings.get("ArrayList_Get").doubleValue()).append(")").append(newLine);

        stringBuilder.append("SQLite:").append(newLine);
        stringBuilder.append("Add: ").append(timings.get("SQLite_Add")).append(" ms\t\t(x").append(timings.get("RealmList_Add").doubleValue() / timings.get("SQLite_Add").doubleValue()).append(")").append(newLine);
        stringBuilder.append("Get: ").append(timings.get("SQLite_Get")).append(" ms\t\t(x").append(timings.get("RealmList_Get").doubleValue() / timings.get("SQLite_Get").doubleValue()).append(")").append(newLine);

        return stringBuilder.toString();
    }

    @Override
    protected void onPostExecute(String s) {
        TextView textView = (TextView) activity.findViewById(R.id.resultText);
        textView.setText(s);
        Toast.makeText(activity, "Done!", Toast.LENGTH_LONG).show();
    }
}
