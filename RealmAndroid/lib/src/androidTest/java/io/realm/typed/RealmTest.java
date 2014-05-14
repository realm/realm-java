package io.realm.typed;

import android.test.AndroidTestCase;

import io.realm.typed.entities.User;


public class RealmTest extends AndroidTestCase {

    public void testRealm() {

        Realm realm = new Realm(getContext());

        try {
            realm.beginWrite();


            // Clear everything of this type, to do a clean test
            realm.clear(User.class);

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

}
