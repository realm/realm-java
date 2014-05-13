package io.realm.typed;

import android.test.AndroidTestCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.ColumnType;
import io.realm.ReadTransaction;
import io.realm.SharedGroup;
import io.realm.Table;
import io.realm.WriteTransaction;
import io.realm.typed.entities.User;

public class PerformanceTest extends AndroidTestCase {

    public void testPerformance() {

        final int listSize = 10000;
        long timer;
        Map<String, Long> timings = new HashMap<String, Long>();

        // ArrayList

        System.out.println("Testing ArrayList");

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

        System.out.println("Testing new interface");

        RealmList<User> realmList = Realms.list(this.getContext(), User.class);

        timer = System.currentTimeMillis();
        try {
            realmList.beginWrite();
            for(int i = 0; i < listSize; i++) {
                User user = new User();

                user.setId(i);
                user.setName("John Doe");
                user.setEmail("john@doe.com");

                realmList.add(user);
            }
            realmList.commit();
        } catch(Throwable t) {
            realmList.rollback();
            t.printStackTrace();
            fail();
        }

        timings.put("RealmList_Add", (System.currentTimeMillis() - timer));

        timer = System.currentTimeMillis();
        for(int i = 0; i < listSize; i++) {
            User u = realmList.get(i);
        }
        timings.put("RealmList_Get", (System.currentTimeMillis() - timer));


        // TightDB dyn

        System.out.println("Testing dynamic interface");

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


        // Output results
        System.out.println("New Interface:");
        System.out.println("Add: " + timings.get("RealmList_Add")+" ms");
        System.out.println("Get: " + timings.get("RealmList_Get")+" ms");

        System.out.println("Old Dyn Interface:");
        System.out.println("Add: " + timings.get("TightDB_Add") + " ms\t\t(x" + (timings.get("RealmList_Add") / timings.get("TightDB_Add")) + ")");
        System.out.println("Get: " + timings.get("TightDB_Get") + " ms\t\t(x" + (timings.get("RealmList_Get") / timings.get("TightDB_Get")) + ")");

        System.out.println("ArrayList Interface:");
        System.out.println("Add: " + timings.get("ArrayList_Add") + " ms\t\t(x" + (timings.get("RealmList_Add") / timings.get("ArrayList_Add")) + ")");
        System.out.println("Get: " + timings.get("ArrayList_Get") + " ms\t\t(x" + (timings.get("RealmList_Get") / timings.get("ArrayList_Get")) + ")");

    }

}
