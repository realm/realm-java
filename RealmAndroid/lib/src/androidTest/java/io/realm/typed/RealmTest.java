package io.realm.typed;

import android.test.AndroidTestCase;

import java.util.Date;

import io.realm.typed.entities.AllColumns;
import io.realm.typed.entities.User;


public class RealmTest extends AndroidTestCase {

    public void testRealm() {

        Realm realm = new Realm(getContext());

        try {
            realm.beginWrite();


            // Clear everything of this type, to do a clean test
            realm.clear();

            // Insert
            for (int i = 0; i < 120; i++) {

                User user = realm.create(User.class);

                user.setId(i);
                user.setName("Rasmus");
                user.setEmail("ra@realm.io");

                user.setId(10);

            }

            realm.commit();

        } catch(Throwable t) {
            t.printStackTrace();
            realm.rollback();
        }

        RealmList<User> users = realm.where(User.class).findAll();


        // Get
        User user1 = users.get(100);
        assertEquals("Rasmus", user1.getName());


        try {

            realm.beginWrite();

            user1.setName("TestName");

            realm.commit();

        } catch(Throwable t) {
            realm.rollback();
        }

        users = realm.where(User.class).findAll();

        // Get
        user1 = users.get(100);

        assertEquals("TestName", user1.getName());

        assertEquals(120, users.size());

        // Iterable
        for(User user : users) {
            System.out.println(user.getId());
        }

        try {

            realm.beginWrite();
            users = realm.where(User.class).findAll();
            user1 = users.get(100);
            user1.setId(100);

            realm.commit();

        } catch(Throwable t) {
            realm.rollback();
        }

        // Query
        RealmList<User> results = realm.where(User.class).equalTo("id", 10).findAll();

        assertEquals(119, results.size());
        assertEquals(10, results.get(0).getId());

    }


    public void testAllColumnsCreate() {

        Realm realm = new Realm(this.getContext());

        realm.beginWrite();

        realm.clear();

        AllColumns obj = realm.create(AllColumns.class);

        obj.setColumnString("dsfs");
        obj.setColumnLong(1);
        obj.setColumnFloat(1.1F);
        obj.setColumnDouble(1.1);
        obj.setColumnBoolean(true);
        obj.setColumnDate(new Date());
        obj.setColumnBinary(new byte[20]);

        realm.commit();

        RealmList<AllColumns> result = realm.where(AllColumns.class).findAll();

        assertEquals(1, result.size());

    }

    public void testAllColumnsAdd() {

        Realm realm = new Realm(this.getContext());

        realm.beginWrite();

        realm.clear();

        AllColumns obj = new AllColumns();

        obj.setColumnString("dsfs");
        obj.setColumnLong(1);
        obj.setColumnFloat(1.1F);
        obj.setColumnDouble(1.1);
        obj.setColumnBoolean(true);
        obj.setColumnDate(new Date());
        obj.setColumnBinary(new byte[20]);

        realm.add(obj);


        realm.commit();

        RealmList<AllColumns> result = realm.where(AllColumns.class).findAll();

        assertEquals(1, result.size());

    }

}
