package com.realm.typed;

import android.test.AndroidTestCase;

import com.realm.typed.entities.User;

import java.util.List;


public class RealmTest extends AndroidTestCase {

    public void testRealm() {

        // Init
        Realm<User> realm = new Realm<User>(User.class, this.getContext());
        // Notice that Realm implements List, which means that it can be used in a lot of existing code


        // Insert
        for(int i = 0; i < 120; i++) {
            User user = new User();
            user.setId(i);
            user.setName("Rasmus");
            user.setEmail("ra@realm.io");

            realm.add(user);

            user.setId(10);
        }



        // Get
        User user1 = realm.get(100);
        assertEquals(user1.getName(), "Rasmus");
        user1.setName("TestName");

        assertEquals(user1.getName(), "TestName");


        assertEquals(120, realm.size());

        // Iterable
        for(User user : realm) {
            System.out.println(user.getId());
        }

        // Query
        Realm<User> results = realm.where().equalTo("id", 33).find();
        assertEquals(1, results.size());
        assertEquals(33, results.get(0).getId());

    }

}
