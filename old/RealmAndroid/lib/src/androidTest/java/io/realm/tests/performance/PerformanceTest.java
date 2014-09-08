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

package io.realm.tests.performance;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.test.AndroidTestCase;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.ColumnType;
import io.realm.ImplicitTransaction;
import io.realm.ReadTransaction;
import io.realm.Row;
import io.realm.SharedGroup;
import io.realm.Table;
import io.realm.TableOrView;
import io.realm.TableQuery;
import io.realm.TableView;
import io.realm.WriteTransaction;
import io.realm.tests.typed.entities.User;
import io.realm.typed.Realm;
import io.realm.typed.RealmList;

public class PerformanceTest extends AndroidTestCase {

    public void testPerformance() {

        final int listSize = 10000;
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
        Realm realm = null;
        try {
            realm = new Realm(getContext().getFilesDir());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        realm.clear();

        timer = System.currentTimeMillis();
        try {
            realm.beginWrite();
            for(int i = 0; i < listSize; i++) {
                User user = realm.create(User.class);

                user.setId(i);
                user.setName("John Doe"+(i/3));
                user.setEmail("john@doe.com");
           }
            realm.commit();
        } catch(Throwable t) {
            t.printStackTrace();
            fail();
        }

        timings.put("RealmList_Add", (System.currentTimeMillis() - timer));

        timer = System.currentTimeMillis();
        RealmList<User> realmList = realm.where(User.class).findAll();
        for(int i = 0; i < listSize; i++) {
            User u = realmList.get(i);

            int id = u.getId();
            String name = u.getName();
            String email = u.getEmail();

//            if (id != i)
//            {
//                fail("read does not match write (id)");
//            }
//            if (("John Doe"+(i/3)).compareTo(name) != 0)
//            {
//                fail("read does not match write (name)");
//            }


        }
        timings.put("RealmList_Get", (System.currentTimeMillis() - timer));


        System.out.println("################################ Testing raw");

        realm.clear();
        realm = null;

        timer = System.currentTimeMillis();
        String filePath = new File(getContext().getFilesDir(),  "default.realm").getAbsolutePath();
        try {

            SharedGroup sg = new SharedGroup(filePath,  SharedGroup.Durability.FULL);
            ImplicitTransaction transaction = sg.beginImplicitTransaction();
            transaction.promoteToWrite();
            Table table = transaction.getTable("User");
            table.addColumn(ColumnType.INTEGER, "id");
            table.addColumn(ColumnType.STRING, "user");
            table.addColumn(ColumnType.STRING, "email");

            table = transaction.getTable("User");

            for(int i = 0; i < listSize; i++) {
                Row row = table.getRow(table.addEmptyRow());
                row.setLong(0,i);
                row.setString(1,"John Doe"+(i/3));
                row.setString(2,"john@doe.com");
            }
            transaction.commitAndContinueAsRead();


            timings.put("RealmList_RawW", (System.currentTimeMillis() - timer));

            timer = System.currentTimeMillis();

            TableOrView dataStore =  transaction.getTable("User");
            TableQuery query = dataStore.where();
            TableView tv = query.findAll();

            for(int i = 0; i < listSize; i++) {
                long v = tv.getLong(0,i);
                String s1 = tv.getString(1,i);
                String s2 = tv.getString(2,i);
            }
            } catch(Throwable t) {
            t.printStackTrace();
            fail();
        }
        timings.put("RealmList_RawG", (System.currentTimeMillis() - timer));

        // TightDB dyn

        System.out.println("################################ Testing dynamic interface");

        SharedGroup sg = new SharedGroup(this.getContext().getFilesDir().getPath()+"/perfTest.tightdb");

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

        SQLiteOpenHelper sqLiteOpenHelper = new SQLiteHelper(getContext());
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
        System.out.println("SQL ROWS " + i);

        // Output results
        System.out.println("New Interface:");
        System.out.println("Add: " + timings.get("RealmList_Add")+" ms");
        System.out.println("Get: " + timings.get("RealmList_Get")+" ms");

        // Output results
        System.out.println("RAW Interface:");
        System.out.println("Add: " + timings.get("RealmList_RawW")+" ms\t\t(x" + (timings.get("RealmList_Add").doubleValue() / timings.get("RealmList_RawW").doubleValue()) + ")");
        System.out.println("Get: " + timings.get("RealmList_RawG")+" ms\t\t(x" + (timings.get("RealmList_Get").doubleValue() / timings.get("RealmList_RawG").doubleValue()) + ")");

        System.out.println("Old Dyn Interface:");
        System.out.println("Add: " + timings.get("TightDB_Add") + " ms\t\t(x" + (timings.get("RealmList_Add").doubleValue() / timings.get("TightDB_Add").doubleValue()) + ")");
        System.out.println("Get: " + timings.get("TightDB_Get") + " ms\t\t(x" + (timings.get("RealmList_Get").doubleValue() / timings.get("TightDB_Get").doubleValue()) + ")");

        System.out.println("ArrayList Interface:");
        System.out.println("Add: " + timings.get("ArrayList_Add") + " ms\t\t(x" + (timings.get("RealmList_Add").doubleValue() / timings.get("ArrayList_Add").doubleValue()) + ")");
        System.out.println("Get: " + timings.get("ArrayList_Get") + " ms\t\t(x" + (timings.get("RealmList_Get").doubleValue() / timings.get("ArrayList_Get").doubleValue()) + ")");

        System.out.println("SQLite:");
        System.out.println("Add: " + timings.get("SQLite_Add") + " ms\t\t(x" + (timings.get("RealmList_Add").doubleValue() / timings.get("SQLite_Add").doubleValue()) + ")");
        System.out.println("Get: " + timings.get("SQLite_Get") + " ms\t\t(x" + (timings.get("RealmList_Get").doubleValue() / timings.get("SQLite_Get").doubleValue()) + ")");

    }

}
